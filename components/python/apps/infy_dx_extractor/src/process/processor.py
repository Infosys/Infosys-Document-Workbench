# ===============================================================================================================#
# Copyright 2020 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

import os
import shutil
import copy
import glob
import json
import traceback
import concurrent.futures
from itertools import groupby
from common.app_session_store import AppSessionStore
from process.dpp_process.dpp_attribute_process import DppAttributeProcess
from process.file_template_process import FileTemplateProcess
from process.profile_template_process import ProfileTemplateProcess

from service.api_caller import ApiCaller
from common.image_merger import ImageMerger
from common.file_annotate_helper import FileAnnotateHelper
from common.app_const import *
from common.response import Response
from common.file_util import FileUtil
from common.process_helper import ProcessHelper as phelper
from common.properties.extract_props import ExtractProps
from infy_ocr_parser import ocr_parser
from infy_ocr_generator import ocr_generator
from infy_ocr_generator.providers.tesseract_ocr_data_service_provider import TesseractOcrDataServiceProvider as TessractGenerator
from infy_ocr_generator.providers.azure_ocr_data_service_provider import AzureOcrDataServiceProvider as AzureGenerator
from infy_ocr_generator.providers.azure_read_ocr_data_service_provider import AzureReadOcrDataServiceProvider as AzureReadGenerator
from infy_ocr_parser.providers.tesseract_ocr_data_service_provider import TesseractOcrDataServiceProvider as TessractParser
from infy_ocr_parser.providers.azure_ocr_data_service_provider import AzureOcrDataServiceProvider as AzureParser
from infy_ocr_parser.providers.azure_read_ocr_data_service_provider import AzureReadOcrDataServiceProvider as AzureReadParser
from infy_ocr_generator.providers.abbyy_ocr_data_service_provider import AbbyyOcrDataServiceProvider as AbbyGenerator
from infy_ocr_parser.providers.abbyy_ocr_data_service_provider import AbbyyOcrDataServiceProvider as AbbyParser

from common.ainauto_logger_factory import AinautoLoggerFactory
from common.app_config_manager import AppConfigManager
from common.common_util import CommonUtil

from process.telemetry_process import TelemetryProcess, LogLevel

app_config = AppConfigManager().get_app_config()
logger = AinautoLoggerFactory().get_logger()
work_dir = None
about_app = AppConfigManager().get_about_app()
app_session_store = AppSessionStore()


class Processor:
    api_caller_obj = None

    def __init__(self):
        self.file_ann_obj = FileAnnotateHelper()
        self.api_caller_obj = ApiCaller()
        self.ocr_type_obj = None
        self.__telemetry_process = None

    def process(self, processor_dict, ocr_name, output_file_location,
                telemetry_process_obj: TelemetryProcess, if_move=False):
        response_dict = {
            'output_path': output_file_location,
            'is_exec_success': False
        }
        log_file_path = processor_dict['log_file_path']
        self.__telemetry_process = telemetry_process_obj
        summary_dict = FileUtil.load_json(
            log_file_path) if os.path.exists(log_file_path) else {}
        self.ocr_type_obj = {ocr_name: True}
        extracted_attr_list = []
        sorted_records = sorted(
            processor_dict['records'], key=lambda record: record.get('doc_group_id'))
        grouped_records = [list(result) for _, result in groupby(
            sorted_records, key=lambda record: record.get('doc_group_id'))]

        with concurrent.futures.ThreadPoolExecutor(
                max_workers=int(app_config['DEFAULT']['max_workers']),
                thread_name_prefix="th_process_record") as executor:
            thread_pool_dict = {
                executor.submit(
                    self.__manage_group_records,
                    records,
                    processor_dict,
                    summary_dict,
                    if_move
                ): records for records in grouped_records
            }
            for future in concurrent.futures.as_completed(thread_pool_dict):
                extracted_attr_list += future.result()
        extracted_attr_list = sorted(
            extracted_attr_list, key=lambda y: y['doc_num'])
        response_data_dict = {}
        response_data_dict.update(about_app)
        response_data_dict[ConfProp.RECORDS] = extracted_attr_list
        CommonUtil.update_app_info(response_data_dict, about_app)

        FileUtil.write_to_json(
            response_data_dict, output_file_location, is_exist_archive=True)
        FileUtil.write_to_json(
            summary_dict, log_file_path)

        logger.info("********** Execution Summary **********")
        logger.info(json.dumps(
            {'extractor': summary_dict.get('extractor', {})}, indent=4))

        logger.info(f"Processed records count- {len(extracted_attr_list)}")
        if len(processor_dict['records']) != len(extracted_attr_list):
            diff_list = list(set([x['doc_id'] for x in processor_dict['records']]) - set(
                [x['doc_id'] for x in extracted_attr_list]))
            logger.error(
                f"Generated output records count {len(extracted_attr_list)} is not matching with Input records count {len(processor_dict['records'])}.")
            logger.error(f'Missing doc_id(s): {diff_list}')
            return response_dict

        batch_summary_list = list(
            set(summary_dict.get('extractor', {}).values()))
        if 'Failed' in batch_summary_list:
            logger.error(
                f'Batch Failed. Find execution summary for more information')
            return response_dict
        response_dict["is_exec_success"] = True
        return response_dict

    def __manage_group_records(self, records, processor_dict, summary_dict, if_move):
        extracted_attr_list = []
        # STAGE 1: FileTemplate Processing
        extractor_config_dict = app_session_store.get_data(
            SessionKey.EXT_CONFIG_FILE_DATA)

        template_process_obj = ProfileTemplateProcess()
        file_template_process_obj = FileTemplateProcess()

        file_templ_obj = extractor_config_dict.get('fileTemplates', {})
        if file_templ_obj and file_templ_obj.get('enabled'):
            for record in records:
                ocr_parser_obj = None if not record['doc_properties'].get('is_ocr_based', True) else self._get_ocr_parser_obj(
                    list(record['ocr_files'].values()))
                extract_props_obj = ExtractProps()
                extract_props_obj.output_dir = FileUtil.create_dirs_if_absent(
                    processor_dict['output_location']+"/"+record['doc_id']+"/"+os.path.dirname(record['doc_sub_path']))
                record["raw_attributes"] = file_template_process_obj.extract_attributes(
                    record, ocr_parser_obj, file_templ_obj, extract_props_obj=extract_props_obj,
                    group_records=records, fn_callback=self._extract_profile_attributes_by_type)

        # STAGE 2: DocumentTemplate Processing
        for record in records:
            response = self.__process_record_worker(
                record, copy.deepcopy(
                    records), processor_dict, template_process_obj,
                file_template_process_obj, summary_dict, if_move)
            extracted_attr_list.append(response)
        return extracted_attr_list

    def __process_record_worker(self, record, group_records, processor_dict, template_process_obj: ProfileTemplateProcess,
                                file_template_process_obj: FileTemplateProcess, summary_dict, if_move):
        record['extraction_profile'] = ''
        record['extraction_profile_rules_exe_history'] = []
        record["message"] = record.get("message", {})
        # ---------------------- Telemetry: START Event ----------------------
        telemetry_data = {'telemetry': record['telemetry']}
        self.__telemetry_process.post_telemetry_event_start(
            record['doc_batch_id'], record['doc_id'], telemetry_data)
        try:
            # Each UUID is mapped to one input file and it supporting docs like
            # image and ocr files presents in subfolder which is named as file name.
            out_location = FileUtil.create_dirs_if_absent(
                processor_dict['output_location']+"/"+record['doc_id']+"/"+os.path.dirname(
                    record['doc_sub_path']))
            ocr_parser_obj = None if not record['doc_properties'].get('is_ocr_based', True) else self._get_ocr_parser_obj(
                list(record['ocr_files'].values()))
            # ----- Identify the profile ------

            selected_profile = template_process_obj.get_doc_profile(
                record, group_records, ocr_parser_obj)

            raw_attr = record.get("raw_attributes")
            if raw_attr:
                # Deleting to reorder this key-value
                del record["raw_attributes"]
                record["raw_attributes"] = raw_attr
            else:
                record["raw_attributes"] = []

            if selected_profile:
                # ----- Extract based on identified profile ------
                record['extraction_profile'] = selected_profile['profile']
                record["message"]["info"] += [
                    f"Profile matched - {selected_profile['profile']}"]
                extracted_attr_list_tmp = self._extract_profile_attributes_by_type(
                    record, group_records, ocr_parser_obj, selected_profile, out_location, file_template_process_obj)
                if if_move:
                    shutil.copy(record['doc_path'], out_location)
                # TODO: Update highlight bbox logic based on new output response template
                # if not self._is_image_file(uuid_dir_in_file):
                #     self.file_ann_obj.highlight_and_manage_bbox_file(
                #         selected_profile['output'].get('saveCopyWith'), record['doc_work_location'], extracted_attr_list_tmp, out_location)
                record["raw_attributes"] += extracted_attr_list_tmp
            else:
                # record["message"]["error"] += ["Document profile is not matched."]
                record["message"]["warning"] += [
                    "Document profile is not matched hence setting to `sys_none`"]
                selected_profile = 'sys_none'
                record["extraction_profile"] = selected_profile

            # extracted_attr_list.append(record)
            # TODO: Enable when individual record output.json is required in output location
            # FileUtil.write_to_json(record, out_location+"/output.json")
            logger.info("***********************************************")
            msg_str, log_level = ("Failed", LogLevel.ERROR) if not selected_profile else (
                "Success", LogLevel.INFO)
            self.__update_summary(
                summary_dict, record['doc_id'], msg_str, "extractor")
            # ---------------------- Telemetry: :LOG Event ----------------------
            self.__telemetry_process.post_telemetry_event_log(
                record['doc_batch_id'], record['doc_id'], log_level, message=msg_str,
                additional_config_param=telemetry_data)
        except Exception:
            full_trace_error = traceback.format_exc()
            logger.error(full_trace_error)
            self.__update_summary(
                summary_dict, record['doc_id'], "Failed", "extractor")
            # ---------------------- Telemetry: :LOG Event ----------------------
            self.__telemetry_process.post_telemetry_event_log(
                record['doc_batch_id'], record['doc_id'], LogLevel.ERROR, message="Failed",
                additional_config_param=telemetry_data)
        # ---------------------- Telemetry: END Event ----------------------
        self.__telemetry_process.post_telemetry_event_end(
            record['doc_batch_id'], record['doc_id'], telemetry_data)
        return record

    def _extract_profile_attributes_by_type(
            self, record, group_records, ocr_parser_obj, selected_profile, out_location,
            file_template_process_obj: FileTemplateProcess = None):
        supporting_file_dir = record['doc_work_location']
        img_merger_obj = ImageMerger(supporting_file_dir)
        # getter setter class
        extract_props_obj = ExtractProps()
        extract_props_obj.output_dir = out_location
        extract_props_obj.supporting_file_dir = supporting_file_dir
        extract_props_obj.original_file = record['doc_copy_path']

        if self._is_image_file(extract_props_obj.original_file):
            # when the input file type is images then set full path of images from *_files dir.
            extract_props_obj.image_file_path = f"{supporting_file_dir}/1.jpg"
        else:
            # when the input file type is other than images then set *_files dir.
            # ex., Incase PDF it converted to images to *_files dir.
            extract_props_obj.image_file_path = supporting_file_dir
            extract_props_obj.is_original_file_is_pdf = True

        attributes = []
        # ----- Extract attributes based on EXTERNAL LLM PROVIDERS (DPP) ------
        attributes += DppAttributeProcess(selected_profile,
                                          record).extract_attributes()
        # ----- Extract attributes based on EXTERNAL OCR PROVIDERS ------
        for attr_obj in selected_profile['attributes']:
            extracted_attr_list, debug_info = [], []
            rd_found, rd_not_found = [], []
            error, info = None, None
            atr_id = FileUtil.get_attr_id(record['doc_id'])
            try:
                atte_ext_provider_data = attr_obj.get(
                    ConfProp.ATTRS_EXT_PROVIDER)
                if atte_ext_provider_data and atte_ext_provider_data.get(ConfProp.ENABLED):
                    continue

                # ----- Extract attributes based on RULES ------
                if attr_obj.get(ConfProp.ATTRS_RULES) and attr_obj.get(ConfProp.ATTRS_RULES).get(ConfProp.ENABLED) and file_template_process_obj:
                    result = file_template_process_obj.manage_attr_extraction(
                        attr_obj, record, ocr_parser_obj, group_records)
                    attributes += result
                    continue

                # ----- Extract attributes based on RDs ------
                if not record['doc_properties'].get('is_ocr_based', True):
                    raise AttributeError('non-ocr based file')
                for attr_def_obj in attr_obj[ConfProp.ATTRS_DEF]:
                    if not attr_def_obj[ConfProp.ENABLED]:
                        continue

                    region_res = None
                    subtract_reg_def_list = []
                    if ConfProp.SUBTRACT_REG in attr_def_obj:
                        for sub_reg_name in attr_def_obj[ConfProp.SUBTRACT_REG]:
                            subtract_reg_def_list += [name_reg_obj[ConfProp.REG_DEFINITION]
                                                      for name_reg_obj in selected_profile.get(ConfProp.NAMED_REG, []) if name_reg_obj[ConfProp.NAME] == sub_reg_name]

                    if ConfProp.VAL_DEF in attr_def_obj and len(attr_def_obj[ConfProp.VAL_DEF]) > 0:
                        region_res = ocr_parser_obj.get_bbox_for(
                            attr_def_obj[ConfProp.VAL_DEF], subtract_reg_def_list)
                        ocr_parser_info = {"region_definition": attr_def_obj[ConfProp.VAL_DEF],
                                           "subtract_region_definition": subtract_reg_def_list}

                        attr_def_obj["ocr_parser_input"] = ocr_parser_info
                    extract_props_obj.value_type = attr_def_obj[ConfProp.VAL_TYPE]
                    extract_props_obj.merged_page_no = None

                    if not region_res and attr_def_obj[ConfProp.VAL_TYPE] in [ValType.B_LESS_TABLE, ValType.LANG]:
                        # output_tuple = self._extract_non_region_rel_data(
                        #    atr_id,attr_obj, ocr_parser_obj, img_merger_obj, extract_props_obj)
                        pass
                    else:
                        output_tuple = self._extract_region_data(
                            atr_id, attr_def_obj, ocr_parser_obj, img_merger_obj, extract_props_obj, region_res)

                    if output_tuple[0][-1][ResProp.MSG][ResProp.ERROR] is None:
                        extracted_attr_list += output_tuple[0]
                        rd_found.append(attr_def_obj[ConfProp.LABEL])
                        debug_info.append(output_tuple[1])
                    else:
                        rd_not_found.append(
                            attr_def_obj[ConfProp.LABEL]+": "+output_tuple[0][-1][ResProp.MSG][ResProp.ERROR])
                        debug_info.append([{'ocr_parser': {
                            "input": attr_def_obj.get("ocr_parser_input", {}), "output": region_res}}])
            except AttributeError as e:
                info = e.args[0]
            except Exception as e:
                full_trace_error = traceback.format_exc()
                logger.error(full_trace_error)
                error = e.args[0]

            attribute = Response.response(
                atr_id=atr_id,
                attr_name=attr_obj['attributeName'],
                vals=extracted_attr_list,
                rd_found=rd_found,
                rd_not_found=rd_not_found,
                debug_info=debug_info,
                info=info,
                error=error
            )
            attributes.append(attribute)
        return attributes

    def _get_ocr_parser_obj(self, ocr_file_list_arg):
        _, ocr_provider, _ = self._get_ocr_type_and_ext()
        ocr_file_dict = ocr_file_list_arg[0]
        ocr_parser_obj = ocr_parser.OcrParser(ocr_file_list=[ocr_file_dict[k] for k in sorted(ocr_file_dict)],
                                              data_service_provider=ocr_provider, logger=logger)
        return ocr_parser_obj

    def _extract_non_region_rel_data(
            self, atr_id, attr_obj, ocr_parser_obj, img_merger_obj, extract_props_obj: ExtractProps):
        attr_data_list = []
        val_type = attr_obj[ConfProp.VAL_TYPE]
        if val_type == ValType.B_LESS_TABLE:
            out_loc_temp = FileUtil.create_dirs_if_absent(
                extract_props_obj.output_dir+"/html")
            table_def = attr_obj[ConfProp.ADDITIONAL_PROP][ConfProp.TABLE_DEF]
            output = self.api_caller_obj.call_borderless_table_converter(
                extract_props_obj.image_file_path, out_loc_temp, table_def)
            # TODO:Handle borderless table output as a debug info.
            attr_data_list += Response.response(atr_id, attr_obj[ConfProp.LABEL],
                                                val_type
                                                )
        elif val_type == ValType.LANG:
            for file in FileUtil.get_files(extract_props_obj.original_file):
                with open(file, 'r') as file_obj:
                    content = file_obj.read()
                    output = self.api_caller_obj.call_lang_detector(content)
                    attr_data_list += Response.response(atr_id,
                                                        attr_obj[ConfProp.LABEL], val_type)
        return attr_data_list

    def _extract_region_data(
            self, atr_id, attr_obj, ocr_parser_obj, img_merger_obj, extract_props_obj: ExtractProps, ocr_parser_res):
        """call api to extract data using coordinates"""
        extract_data_list, debug_info = [], []
        error = phelper.get_val(ocr_parser_res, ResProp.ERROR)
        if error:
            extract_data_list = Response.val_structure(
                FileUtil.get_attr_val_id(atr_id), extract_props_obj.value_type, error=error)
            return extract_data_list, debug_info
        region_res = phelper.get_val(ocr_parser_res, ResProp.REGIONS)
        if not region_res:
            self._get_extracted_from_api(
                atr_id, None, extract_props_obj, attr_obj, ocr_parser_obj, None, extract_data_list, debug_info)
            return extract_data_list, debug_info
        # Based on ocr_parser response extract region data from respective page
        for regions_obj in region_res:
            reg_bbox_list = phelper.get_val(regions_obj, ResProp.REG_BBOX)
            if len(reg_bbox_list) > 1:
                try:
                    # recursive call to extract from merged images
                    reg_bbox_list.sort(key=lambda x: (x[ConfProp.PAGE]))
                    new_ocr_obj, mul_page_res, merged_img_path, merged_pno = self.get_multi_page_res_bbox(
                        img_merger_obj, reg_bbox_list, attr_obj, ocr_parser_obj, extract_props_obj)
                    extract_props_obj_cp = copy.deepcopy(extract_props_obj)
                    extract_props_obj_cp.image_file_path = merged_img_path
                    extract_props_obj_cp.merged_page_no = merged_pno
                    # native pdf supporting logic
                    reg_bbox_list_temp = copy.deepcopy(reg_bbox_list)
                    page_no_list, page_bbox_list = [], []
                    for reg_bbox_obj in reg_bbox_list_temp:
                        page_no_list.append(reg_bbox_obj[ResProp.PAGE])
                        page_bbox_list.append(reg_bbox_obj[ResProp.BBOX])
                    extract_props_obj_cp.multipage_no_list = page_no_list
                    extract_props_obj_cp.multipage_bbox_list = page_bbox_list

                    output_tuple = self._extract_region_data(
                        atr_id, attr_obj, new_ocr_obj, img_merger_obj, extract_props_obj_cp, mul_page_res)
                    extract_data_list += output_tuple[0]
                    debug_info += output_tuple[1]

                except Exception as e:
                    full_trace_error = traceback.format_exc()
                    logger.error(full_trace_error)
            else:
                self._get_extracted_from_api(
                    atr_id, reg_bbox_list[0], extract_props_obj, attr_obj,
                    ocr_parser_obj, regions_obj, extract_data_list, debug_info)
        return extract_data_list, debug_info

    def _get_extracted_from_api(
            self, atr_id, reg_bbox_obj, extract_props_obj: ExtractProps,
            attr_obj, ocr_parser_obj, regions_obj, extract_data_list, debug_info):
        reg_bbox = phelper.get_val(reg_bbox_obj, ResProp.BBOX)
        reg_bbox_page = extract_props_obj.merged_page_no if extract_props_obj.merged_page_no else phelper.get_val(
            reg_bbox_obj, ResProp.PAGE)
        img_path = extract_props_obj.image_file_path
        img_path_temp = os.path.abspath(img_path)
        if not reg_bbox_obj and os.path.isdir(img_path):
            for img_path_1 in FileUtil.get_files(img_path, app_config["DEFAULT"]["supported_img_types"]):
                extract_props_obj_cp = copy.deepcopy(extract_props_obj)
                extract_props_obj_cp.image_file_path = img_path_1
                self._get_extracted_from_api(
                    atr_id, reg_bbox_obj, extract_props_obj_cp, attr_obj, ocr_parser_obj,
                    regions_obj, extract_data_list, debug_info)
            return
        if os.path.isdir(img_path):
            img_path_temp = "{}/{}.jpg".format(img_path, reg_bbox_page)
        output, error, format_converter_output, value_list = None, None, None, []
        try:
            filter_from_page = None if extract_props_obj.merged_page_no else phelper.get_val(
                reg_bbox_obj, ResProp.PAGE)
            cur_img_w, cur_img_h = FileUtil.get_image_width_height(
                img_path_temp)
            scale_fact_obj = ocr_parser_obj.calculate_scaling_factor(
                cur_img_w, cur_img_h, page=reg_bbox_page)
            img_scaling_factor = scale_fact_obj["scalingFactor"]
            val_type = extract_props_obj.value_type
            info, key_text = None, None
            extraction_technique = None
            if val_type == ValType.TXT:
                output, extraction_technique = self.__get_text_data(extract_props_obj, img_path_temp, attr_obj,
                                                                    reg_bbox, ocr_parser_obj, img_scaling_factor, filter_from_page)
                value_list = output['field_extractor']['output']['fields']
                # if extract_props_obj.is_original_file_is_pdf:
                #     if extract_props_obj.merged_page_no:
                #         demerged_bbox_list = phelper.get_demerged_bboxes(
                #             extract_props_obj.multipage_bbox_list, reg_bbox)
                #         format_converter_output = []
                #         for idx, page_no in enumerate(extract_props_obj.multipage_no_list):
                #             img_path_temp = "{}/{}.jpg".format(
                #                 extract_props_obj.supporting_file_dir, page_no)
                #             cur_img_w, cur_img_h = FileUtil.get_image_width_height(
                #                 img_path_temp)
                #             config_param_dict = {"pages": [page_no], "bboxes": [demerged_bbox_list[idx]],
                #                                  "page_dimension": {"width": cur_img_w, "height": cur_img_h}}
                #             format_converter_output.append(
                #                 self.api_caller_obj.call_format_converter_for_pdf_to_json(
                #                     extract_props_obj.original_file, config_param_dict
                #                 )
                #             )
            elif val_type == ValType.CHECKBOX:
                output = self.api_caller_obj.call_checkbox_extractor(
                    img_path_temp, attr_obj[ConfProp.LABEL], reg_bbox, ocr_parser_obj,
                    img_scaling_factor, filter_from_page
                )
                value_list = output['field_extractor']['output']['fields']
            elif val_type == ValType.RADIO:
                output = self.api_caller_obj.call_radio_button_extractor(
                    img_path_temp, attr_obj[ConfProp.LABEL], reg_bbox, ocr_parser_obj,
                    img_scaling_factor, filter_from_page
                )
                value_list = output['field_extractor']['output']['fields']
            elif val_type == ValType.B_LESS_TABLE:
                out_loc_temp = FileUtil.create_dirs_if_absent(
                    extract_props_obj.output_dir+"/html")
                table_def = attr_obj[ConfProp.ADDITIONAL_PROP][ConfProp.TABLE_DEF]
                output = self.api_caller_obj.call_borderless_table_converter(
                    extract_props_obj.image_file_path, out_loc_temp, table_def, reg_bbox
                )
                value_list = output['borderedless_table_extractor']['output']['fields']
            elif val_type == ValType.B_TABLE:
                output = self.api_caller_obj.call_bordered_table_extractor(
                    img_path_temp, reg_bbox, extract_props_obj.output_dir
                )
                value_list = output['bordered_table_extractor']['output']['fields']
                info = value_list['fields'][0]["processing_msg"] if len(
                    value_list['fields']) > 0 else None
            elif(val_type == ValType.SIGNATURE or val_type == ValType.HANDWRITTEN):
                # TODO Prachi : Remove : the logic to filter the images containing _bbox as it is added upstream
                annotate_to_dir = phelper.get_val(
                    attr_obj[ConfProp.ADDITIONAL_PROP], ConfProp.ANNOTATE_OUT_DIR)
                if os.path.basename(img_path).find("_bbox") == -1:
                    output = self.api_caller_obj.call_object_detector(
                        img_path_temp, val_type, annotate_to_dir)
                # TODO Prachi : Remove : Added page number for highlighting bbox of extracted signature or handwritten
                if(reg_bbox_page == None):
                    if os.path.basename(img_path).find("_bbox") == -1:
                        reg_bbox_page = os.path.basename(img_path)[0]

        except Exception as e:
            full_trace_error = traceback.format_exc()
            logger.error(full_trace_error)
            error = e.args[0]

        for val_obj in value_list:
            possible_val_keys = ["field_value", "field_state",
                                 "table_value"]
            possible_bbox_keys = ["field_value_bbox",
                                  "field_state_bbox", "table_value_bbox"]
            for val_key, bbox_key in zip(possible_val_keys, possible_bbox_keys):
                if val_key in val_obj:
                    val = val_obj[val_key]
                if bbox_key in val_obj:
                    bbox = val_obj[bbox_key]

            res_list = Response.val_structure(
                val_id=FileUtil.get_attr_val_id(atr_id), type_obj=val_type, error=error, warn=scale_fact_obj["warnings"],
                info=info, val=val, bbox=bbox, page=reg_bbox_page, key_text=key_text,
                confidence=val_obj["field_value_confidence_pct"] if "field_value_confidence_pct" in val_obj else None,
                rd_found=attr_obj[ResProp.LABEL], extraction_technique=extraction_technique)

            debug_key = list(output.keys())[0]
            debug_info += [
                {
                    'ocr_parser': {"input": attr_obj.get("ocr_parser_input", {}), "output": reg_bbox_obj},
                    debug_key:  output[debug_key]
                }
            ]

            extract_data_list += res_list

    def __get_text_data(self, extract_props_obj, img_path, attr_obj, reg_bbox, ocr_parser_obj, img_scaling_factor, filter_from_page):
        def _get_text_data(provide_name):
            return self.api_caller_obj.call_text_extractor(
                img_path, attr_obj[ConfProp.LABEL], reg_bbox, ocr_parser_obj,
                img_scaling_factor, extract_props_obj, filter_from_page, provide_name=provide_name)

        DATA_SERVICE_PROVIDERS = ['ocrTextParser']
        useNativeParser = attr_obj.get('useNativeParser', False)
        if extract_props_obj.is_original_file_is_pdf and useNativeParser:
            DATA_SERVICE_PROVIDERS.append('nativePdfParser')
        output_data_list = []
        with concurrent.futures.ThreadPoolExecutor(
                max_workers=int(app_config['DEFAULT']['max_workers']),
                thread_name_prefix="th_text_extraction") as executor:
            thread_pool_dict = {
                executor.submit(
                    _get_text_data,
                    provide_name
                ): provide_name for provide_name in DATA_SERVICE_PROVIDERS
            }
            for future in concurrent.futures.as_completed(thread_pool_dict):
                output_data_list.append(future.result())

        ocr_data = [output_data for output_data in output_data_list if output_data['field_extractor']
                    ['input']['technique'] == 'ocrTextParser']
        output_data = ocr_data[0]
        extraction_technique = {
            "default": {
                ResProp.CONF: ocr_data[0]['field_extractor'].get(
                    'output', {}).get('fields', [{}])[0].get('field_value_confidence_pct', ''),
                ResProp.SLCTD: True
            }
        }
        if extract_props_obj.is_original_file_is_pdf and useNativeParser:
            nativepdf_data = [output_data for output_data in output_data_list if output_data['field_extractor']
                              ['input']['technique'] == 'nativePdfParser']
            is_val_exist = nativepdf_data[0]['field_extractor'].get(
                'output', {}).get('fields', [{}])[0].get('field_value', '')
            extraction_technique['nativePdfParser'] = {
                ResProp.CONF: nativepdf_data[0]['field_extractor'].get('output', {}).get('fields', [{}])[0].get('field_value_confidence_pct', ''),
                ResProp.SLCTD: False
            }
            if is_val_exist:
                output_data = nativepdf_data[0]
                extraction_technique['default'][ResProp.SLCTD] = False
                extraction_technique['nativePdfParser'][ResProp.SLCTD] = True

        return output_data, extraction_technique

    def get_multi_page_res_bbox(self, img_merger_obj, multi_page_res_bbox, attr_obj, ocr_parser_obj, extract_props_obj: ExtractProps):
        merged_page_dir = FileUtil.create_dirs_if_absent(
            extract_props_obj.image_file_path+f"/{MERGED_IMG_DIR_NAME}")
        multi_page_res_bbox = self._get_multipage_scaling_bbox(
            extract_props_obj.image_file_path, multi_page_res_bbox, ocr_parser_obj)
        merged_img_path = phelper.is_range_within_exist_file(
            merged_page_dir, multi_page_res_bbox)
        if not merged_img_path:
            merged_result = img_merger_obj.merge_images_for(
                multi_page_res_bbox)
            if merged_result["error"]:
                raise Exception(merged_result["error"])
            merged_img_path = merged_result["output"]["imagePath"]

        img_file_name = os.path.splitext(os.path.split(merged_img_path)[1])[0]
        ocr_type, ocr_provider, ocr_ext = self._get_ocr_type_and_ext()
        exist_ocr_name = f"{merged_page_dir}/{img_file_name}{ocr_ext.replace('*','')}"
        # Reuse existing image and ocr if region within it.
        ocr_file_list = [exist_ocr_name]
        if not os.path.exists(exist_ocr_name):
            ocr_type, gen_ocr_provider, _ = self._get_ocr_type_and_ext(
                True, config_param=self._get_ocrtype_config())
            ocr_gen_obj = ocr_generator.OcrGenerator(
                data_service_provider=gen_ocr_provider)
            doc_data_list = []
            for doc_path in glob.glob(merged_img_path):
                doc_data_list.append(
                    {'doc_path': doc_path, 'pages': 1})
            if ocr_type == OcrTypeLabel.AZURE_READ:
                submit_req_result = ocr_gen_obj.submit_request(
                    doc_data_list)
                re_res_result = ocr_gen_obj.receive_response(submit_req_result)
                ocr_file_list = ocr_gen_obj.generate(
                    api_response_list=re_res_result)
            else:
                ocr_file_list = ocr_gen_obj.generate(
                    doc_data_list=doc_data_list)

        ocr_parser_obj_temp = ocr_parser.OcrParser(ocr_file_list=[ocr_file_obj["output_doc"] for ocr_file_obj in ocr_file_list],
                                                   data_service_provider=ocr_provider, logger=logger)

        region_res = ocr_parser_obj_temp.get_bbox_for(
            attr_obj[ConfProp.VAL_DEF])
        return ocr_parser_obj_temp, region_res, merged_img_path, img_file_name

    def _get_multipage_scaling_bbox(self, img_file_path, region_bbox, ocr_parser_obj):
        region_bbox_temp = copy.deepcopy(region_bbox)
        for page_bbox in region_bbox_temp:
            w, h = FileUtil.get_image_width_height(
                f"{img_file_path}/{str(page_bbox['page'])}.jpg")
            scale_fact_obj = ocr_parser_obj.calculate_scaling_factor(
                w, h, page_bbox['page'])
            page_bbox["bbox"] = [round(i*scale_fact_obj["scalingFactor"]['hor'])
                                 if pos % 2 == 0 else round(i*scale_fact_obj["scalingFactor"]['ver'])
                                 for pos, i in enumerate(page_bbox["bbox"])]
            page_bbox["scalingFactor"] = scale_fact_obj["scalingFactor"]
        return region_bbox_temp

    def _get_ocr_type_and_ext(self, if_generator=False, config_param=None):
        ocr_type, ocr_ext = "", ""
        if bool(self.ocr_type_obj.get(OcrTypeLabel.TESSERACT, False)):
            if if_generator:
                data_service_provider = TessractGenerator(
                    config_params_dict=config_param, logger=logger)
                ocr_type, ocr_ext = OcrTypeLabel.TESSERACT, FileFormat.HOCR
            else:
                data_service_provider = TessractParser(logger=logger)
                ocr_type, ocr_ext = OcrTypeLabel.TESSERACT, FileFormat.HOCR
        elif bool(self.ocr_type_obj.get(OcrTypeLabel.ABBYY, False)):
            if if_generator:
                data_service_provider = AbbyGenerator(
                    config_params_dict=config_param, logger=logger)
                ocr_type, ocr_ext = OcrTypeLabel.ABBYY, FileFormat.XML
            else:
                data_service_provider = AbbyParser(logger=logger)
                ocr_type, ocr_ext = OcrTypeLabel.ABBYY, FileFormat.XML
        elif bool(self.ocr_type_obj.get(OcrTypeLabel.AZURE_OCR, False)):
            if if_generator:
                data_service_provider = AzureGenerator(
                    config_params_dict=config_param, logger=logger)
                ocr_type, ocr_ext = OcrTypeLabel.AZURE_OCR, FileFormat.JSON
            else:
                data_service_provider = AzureParser(logger=logger)
                ocr_type, ocr_ext = OcrTypeLabel.AZURE_OCR, FileFormat.JSON
        elif bool(self.ocr_type_obj.get(OcrTypeLabel.AZURE_READ, False)):
            if if_generator:
                data_service_provider = AzureReadGenerator(
                    config_params_dict=config_param, logger=logger)
                ocr_type, ocr_ext = OcrTypeLabel.AZURE_READ, FileFormat.JSON
            else:
                data_service_provider = AzureReadParser(logger=logger)
                ocr_type, ocr_ext = OcrTypeLabel.AZURE_READ, FileFormat.JSON

        return ocr_type, data_service_provider, ocr_ext

    def _get_ocrtype_config(self):
        return {
            "tesseract": {"pytesseract_path": os.environ["TESSERACT_PATH"]},
            "abbyy": {
                "customer_project_Id": app_config["ABBYY"]["customer_project_Id"],
                "license_path": app_config["ABBYY"]["license_path"],
                "license_password": app_config["ABBYY"]["license_password"]},
            "azureOcr": {
                "computer_vision": {
                    "subscription_key": app_config["AZURE_OCR"]["subscription_key"],
                    "api_ocr": {
                        "url": app_config["AZURE_OCR"]["url"],
                    }
                }
            },
            "azureRead": {
                "computer_vision": {
                    "subscription_key": app_config["AZURE_READ"]["subscription_key"],
                    "api_read": {
                        "url": app_config["AZURE_READ"]["url"],
                    }
                }
            }

        }

    def _is_image_file(self, file_path):
        file_path_obj = FileUtil.get_file_path_detail(file_path)
        uuid_dir_in_file_ext = file_path_obj["fileExtension"]
        return True if f"*{uuid_dir_in_file_ext}" in str(app_config["DEFAULT"]["supported_img_types"]).split(",") else False

    def __update_summary(self, summary_dict, doc_id, status, column_name):
        if summary_dict.get(column_name):
            summary_dict[column_name][doc_id] = status
        else:
            summary_dict[column_name] = {
                doc_id: status
            }
