# ===============================================================================================================#
# Copyright 2020 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

import os
import time
import traceback
import concurrent.futures
from common.app_const import *
from common.common_util import CommonUtil
from common.file_util import FileUtil
from process.telemetry_process import TelemetryProcess, LogLevel
from service.ocr_generator_service import OcrGeneratorService
from service.image_generator_service import ImageGeneratorService
from common.ainauto_logger_factory import AinautoLoggerFactory
from common.app_config_manager import AppConfigManager
from pathlib import Path
import imageio
import json
app_config = AppConfigManager().get_app_config()
logger = AinautoLoggerFactory().get_logger()
about_app = AppConfigManager().get_about_app()

PREPROCESSOR_DICT = {
    ConfProp.OCR_TOOL: {
        "tesseract": True,
        "abbyy": False,
        "azure_cv_read": False,
        "azure_cv_ocr": True
    },
    ConfProp.OUTPUT_LOCATION: '',
    ConfProp.INPUT_FILE_PATHS: [''],
    ConfProp.PAGES: ''
}


class PrePreprocessor:

    def __init__(self, ocr_provider_settings, ocr_tool, master_config, telemetry_proc_obj):
        self.__preproc_config_params = master_config.get('pre_processor', {})
        self.__casecreate_config_params = master_config.get('case_creator', {})
        self.__validate_dependencies(ocr_tool)
        self.ocr_service_obj = OcrGeneratorService(
            ocr_provider_settings, ocr_tool, self.__preproc_config_params)
        self.__cache_enabled = self.__preproc_config_params.get(
            'cache_enabled', False)
        self.image_generator_obj = ImageGeneratorService()
        self.__telemetry_process: TelemetryProcess = telemetry_proc_obj
        self.__container_folder_level = self.__preproc_config_params.get(
            'container_folder_level')

    def execute(self, preprocessor_dict: PREPROCESSOR_DICT, log_file_path, if_move=False):
        logger.info("Execution Started.....")
        output_location = preprocessor_dict['output_location']
        response_dict = {
            'output_path': output_location,
            'is_exec_success': False
        }
        work_location = preprocessor_dict['work_folder_path']
        ocr_provider_enabled_dict = preprocessor_dict[ConfProp.OCR_TOOL]
        record_data_dict_list = []

        def _processing_input_doc(input_record: dict, input_copy_path_files: list):
            message_dict = {"info": [], "error": [], "warning": []}
            # upstream response file does not contain doc_copy_path, it only contains doc_original_path, hence making below change
            doc_file = input_record.get('doc_copy_path') if input_record.get(
                'doc_copy_path') else input_record.get('doc_original_path')
            upstream_doc_attr = input_record.get('upstream_doc_attribute')
            doc_id = upstream_doc_attr['document_id'] if upstream_doc_attr and upstream_doc_attr.get('document_id') else FileUtil.get_uuid(
            )
            work_folder_id = f"D-{doc_id}"
            folder_tid_map = self.__get_folder_tenant_id_mapping(
                input_record.get('doc_original_sub_path'))
            # ---------------------- Telemetry: START Event ----------------------
            telemetry_data = {'telemetry': {
                "tenant_id": folder_tid_map.get('tenant_id')}}
            self.__telemetry_process.post_telemetry_event_start(
                preprocessor_dict['doc_batch_id'], doc_id, telemetry_data)
            # ---------------------- Begin Record Processing ----------------------
            try:
                if not os.path.exists(doc_file):
                    logger.info(f"File not found to work: {doc_file}")
                    return None
                logger.info("Document to OCR Process Started...")
                sub_path = ''
                try:
                    input_path_root = input_record.get(
                        ConfProp.INPUT_ROOT_FOLDER, '')
                    sub_path = os.path.relpath(doc_file, os.path.commonpath(
                        [doc_file, input_path_root])) if input_path_root != '' else ''
                except Exception as e:
                    # Harmless error
                    pass

                work_doc_file, error_val = FileUtil.copy_to_work_dir(
                    work_location, work_folder_id, '', doc_file)
                if error_val:
                    raise Exception(error_val)

                enabled_providers = [provider_name for provider_name,
                                     val in ocr_provider_enabled_dict.items() if isinstance(val['selected'], bool) and val['selected']]
                # MAX file size common for all providers, if multiple providers enabled.
                max_file_size = self.__get_max_image_file_size(
                    enabled_providers)
                # MIN/MAX pixel is common for all providers, if multiple providers enabled.
                min_max_pixel_list = self.__get_min_max_image_pixel(
                    enabled_providers)
                image_file_list, dpi, valid_image_dimension = self.__convert_doc_to_images(
                    work_doc_file, preprocessor_dict['pages'], max_file_size, min_max_pixel_list)
                if valid_image_dimension:
                    all_generated_ocr_file_list = self.ocr_service_obj.generate_ocr(
                        ocr_provider_enabled_dict, enabled_providers,
                        image_file_list, work_doc_file, preprocessor_dict['pages'])
                    self.image_generator_obj.plot_bbox_on_image(
                        all_generated_ocr_file_list)
                else:
                    all_generated_ocr_file_list = []
                    for x in enabled_providers:
                        all_generated_ocr_file_list.append(
                            {"provider_name": x, "generate_ocr_response": []})

                ocr_files, cache_ocr_files = {}, {}
                for generate_ocr_res_dict in all_generated_ocr_file_list:
                    ocr_files[generate_ocr_res_dict['provider_name']] = {os.path.basename(
                        ocr_res['output_doc']).split(".")[0]: ocr_res['output_doc'] for ocr_res in generate_ocr_res_dict[
                        'generate_ocr_response']}
                    cache_ocr_files[generate_ocr_res_dict['provider_name']] = {os.path.basename(
                        ocr_res['output_doc']).split(".")[0]: ocr_res.get('is_ocr_from_cache', False) for ocr_res in generate_ocr_res_dict[
                        'generate_ocr_response']}
                ocr_tool_input_file_type = {name: val.get(
                    "input_file_type", "image") for name, val in ocr_provider_enabled_dict.items() if name in enabled_providers}
                _, found_restricted_file_format = self.__is_docwb_restricted_file_format(
                    doc_file)
                if found_restricted_file_format:
                    message_dict["error"] += [
                        "This document format is restricted in docwb."]
                    logger.error(f"Found restricted file {doc_file}")
                if image_file_list and not valid_image_dimension:
                    message_dict["info"] += [
                        f"The document/image dimension is not in the expected limit of {min_max_pixel_list}. Hence setting `is_ocr_based:false`"]
                is_ocr_based = self.__is_ocr_supported_file_format(doc_file)
                if not is_ocr_based:
                    message_dict["info"] += [
                        f"The document is not part of docwb ocr supported formats - {app_config['DEFAULT']['docwb_ocr_supported_file_types']} . Hence setting `is_ocr_based:false`"]

                doc_original_sub_path_tmp = input_record.get(
                    'doc_original_sub_path')
                components = []
                while doc_original_sub_path_tmp:
                    base_path = os.path.relpath(
                        doc_original_sub_path_tmp, os.path.dirname(doc_original_sub_path_tmp))
                    components.append(base_path)
                    doc_original_sub_path_tmp = os.path.dirname(
                        doc_original_sub_path_tmp)
                doc_name = components[-1]
                if self.__container_folder_level > 0 and len(components) >= (self.__container_folder_level)-1:
                    path_till_container_folder = ''
                    if len(components[-1:-(self.__container_folder_level):-1]) > 0:
                        path_till_container_folder = os.path.join(
                            *components[-1:-(self.__container_folder_level):-1])
                        doc_name = os.path.relpath(input_record.get(
                            'doc_original_sub_path'), path_till_container_folder)
                    if len(components) >= self.__container_folder_level and len(components[-(self.__container_folder_level)::-1]) > 1:
                        container_folder_path = os.path.join(
                            path_till_container_folder, components[-(self.__container_folder_level)])
                        doc_name = os.path.relpath(input_record.get(
                            'doc_original_sub_path'), container_folder_path)
                else:
                    doc_name = input_record.get('doc_original_sub_path')
                new_record = {
                    "workflow": input_record.get('workflow', []),
                    "created_dtm": FileUtil.get_current_datetime(),
                    "doc_id": doc_id,
                    "doc_group_id": input_record.get('doc_group_id'),
                    "doc_batch_id": preprocessor_dict['doc_batch_id'],
                    "doc_work_folder_id": work_folder_id,
                    "doc_num": input_copy_path_files.index(doc_file)+1,
                    "doc_name": doc_name,
                    'input_path_root': input_path_root,
                    "doc_original_path": input_record['doc_original_path'],
                    "doc_original_sub_path": input_record.get('doc_original_sub_path'),
                    "doc_copy_path": doc_file,
                    "doc_sub_path": sub_path,
                    "doc_work_location": f"{work_doc_file}_files",
                    "upstream_doc_attribute": upstream_doc_attr,
                    "image_files": {
                        os.path.basename(image_file).split(".")[0]: image_file for image_file in image_file_list
                    },
                    "image_dpi": dpi,
                    "image_bbox": {
                        os.path.basename(image_file).split(".")[0]: self.__get_image_bbox(image_file) for image_file in image_file_list
                    },
                    "ocr_tool_input_file_type": ocr_tool_input_file_type,
                    "ocr_files": ocr_files,
                    "cache_enabled": self.__cache_enabled,
                    "cache_ocr_files": cache_ocr_files,
                    "docwb_query_attribute": folder_tid_map,
                    "telemetry": telemetry_data.get('telemetry'),
                    "doc_properties": {
                        "is_restricted_file_format": found_restricted_file_format,
                        "is_ocr_based": (valid_image_dimension and is_ocr_based),
                        "size": FileUtil.get_file_size_in_human_readable(doc_file)
                    },
                    "message": message_dict
                }
                new_record = {**input_record, **new_record}
                record_data_dict_list.append(new_record)
                if if_move:
                    FileUtil.delete_file(doc_file)
                else:
                    FileUtil.delete_file(work_doc_file)
                msg_str, log_level = ("Failed", LogLevel.ERROR) if found_restricted_file_format else (
                    "Success", LogLevel.INFO)
                self.__update_summary(
                    summary_dict, doc_id, msg_str, "pre_processor")
                # ---------------------- Telemetry: :LOG Event ----------------------
                self.__telemetry_process.post_telemetry_event_log(
                    preprocessor_dict['doc_batch_id'], doc_id, log_level, message=msg_str,
                    additional_config_param=telemetry_data)
                logger.info("Document to OCR Process Completed...")
            except Exception as e:
                full_trace_error = traceback.format_exc()
                logger.error(full_trace_error)
                self.__update_summary(
                    summary_dict, doc_id, "Failed", "pre_processor")
                # ---------------------- Telemetry: :LOG Event ----------------------
                self.__telemetry_process.post_telemetry_event_log(
                    preprocessor_dict['doc_batch_id'], doc_id, LogLevel.ERROR, message="Failed",
                    additional_config_param=telemetry_data)
            # ---------------------- Telemetry: END Event ----------------------
            self.__telemetry_process.post_telemetry_event_end(
                preprocessor_dict['doc_batch_id'], doc_id, telemetry_data)
            return doc_id

        input_records = preprocessor_dict.get('input_file_paths')
        if not input_records:
            logger.error("input_file_paths is not found in request file")
            return response_dict
        logger.info(f"No. of files found - {len(input_records)}")
        # upstream response file does not contain doc_copy_path, it only contains doc_original_path, hence making below change
        input_copy_path_files = [x.get('doc_copy_path') if x.get(
            'doc_copy_path') else x.get('doc_original_path') for x in input_records]

        # Multiple Threads to Handle ONE Profile -> MANY Input Files
        logger.info(
            "START: Processing Input docs and generate ocr files.")
        start_time = time.time()
        try:
            summary_dict = FileUtil.load_json(
                log_file_path) if os.path.exists(log_file_path) else {}
        except:
            summary_dict = {}
        with concurrent.futures.ThreadPoolExecutor(
                max_workers=int(app_config[L_DEFAULT][MAX_WORKERS]),
                thread_name_prefix="th_processing_input_doc") as executor:
            thread_pool_dict = {
                executor.submit(
                    _processing_input_doc,
                    record,
                    input_copy_path_files
                ): record for record in input_records
            }
            for future in concurrent.futures.as_completed(thread_pool_dict):
                # future.result()
                pass
        record_data_dict_list = sorted(
            record_data_dict_list, key=lambda y: y['doc_num'])
        response_data_dict = {}
        response_data_dict.update(about_app)
        response_data_dict['records'] = record_data_dict_list
        CommonUtil.update_app_info(response_data_dict, about_app)

        FileUtil.save_to_json(
            output_location, response_data_dict, is_exist_archive=True)

        FileUtil.save_to_json(log_file_path, summary_dict)

        logger.info(
            f"Total time taken for  # {len(record_data_dict_list)} docs is {round((time.time() - start_time)/60, 4)} mins")
        logger.info("END: Processing Input docs and generate ocr files.")

        logger.info("********** Execution Summary **********")
        logger.info(json.dumps(
            {'pre_processor': summary_dict.get('pre_processor', {})}, indent=4))
        try:
            self.__validate_output_records(
                input_records, record_data_dict_list)
        except Exception as e:
            logger.error(e)
            return response_dict

        batch_summary_list = list(
            set(summary_dict.get('pre_processor', {}).values()))
        if 'Failed' in batch_summary_list:
            logger.error(
                f'Batch Failed. Find execution summary for more information')
            return response_dict
        response_dict["is_exec_success"] = True
        return response_dict

    def __get_folder_tenant_id_mapping(self, doc_original_sub_path):
        doc_original_sub_path_tmp = doc_original_sub_path
        components = []
        # setting default value from masterconfig -> case_creator object
        result = {
            'tenant_id': self.__casecreate_config_params.get('tenant_id'),
            'doc_type_cde': self.__casecreate_config_params.get('doc_type_cde')
        }
        while doc_original_sub_path_tmp:
            base_path = os.path.relpath(
                doc_original_sub_path_tmp, os.path.dirname(doc_original_sub_path_tmp))
            components.append(base_path)
            doc_original_sub_path_tmp = os.path.dirname(
                doc_original_sub_path_tmp)
        folder_tenant_id_mapping = self.__preproc_config_params.get(
            'folder_tenant_id_mapping')
        for item in folder_tenant_id_mapping:
            if len(components) > item['folder_level']:
                if components[-item['folder_level']] == item['folder_name']:
                    result["tenant_id"] = item['tenant_id']
                    result["doc_type_cde"] = item['doc_type_cde']
                    break
        return result

    def __get_image_bbox(self, img_path):
        w, h = FileUtil.get_img_wh(img_path)
        return [0, 0, w, h]

    def __is_docwb_restricted_file_format(self, input_file):
        doc_ext = '*'+FileUtil.get_file_exe(input_file)
        supported_types = app_config["DEFAULT"]["docwb_restricted_file_types"].split(
            ",")
        is_supported = doc_ext.lower() in list(
            map(lambda x: x.lower(), supported_types))
        return doc_ext, is_supported

    def __is_ocr_supported_file_format(self, input_file):
        doc_ext = '*'+FileUtil.get_file_exe(input_file)
        supported_types = app_config["DEFAULT"]["docwb_ocr_supported_file_types"].split(
            ",")
        is_supported = doc_ext.lower() in list(
            map(lambda x: x.lower(), supported_types))
        return is_supported

    def __validate_output_records(self, input_records, record_data_dict_list):
        def _check_in_out_record_count():
            if len(input_records) != len(record_data_dict_list):
                diff_list = list(set([x['doc_copy_path'] for x in input_records]) - set(
                    [x['doc_copy_path'] for x in record_data_dict_list]))
                logger.error(f'Missing document(s): {diff_list}')
                raise Exception(
                    f"Generated output records count {len(record_data_dict_list)} is not matching with Input records count {len(input_records)}.")

        def _check_img_ocr_file():
            ocr_miss_counter, empty_ocr_counter = 0, 0
            for record in record_data_dict_list:
                doc_properties = record.get('doc_properties', {})
                if not doc_properties.get('is_restricted_file_format', False) and not doc_properties.get('is_ocr_based'):
                    continue
                for ocr_tool_name in list(record['ocr_files']):
                    if len(list(record['image_files'].values())) != len(list(record['ocr_files'][ocr_tool_name].values())):
                        logger.error(
                            f"OCR file is missing for the record document id : {record['doc_id']}")
                        ocr_miss_counter = ocr_miss_counter+1

                    for file_path in list(record['ocr_files'][ocr_tool_name].values()):
                        if os.stat(file_path).st_size == 0:
                            logger.error(
                                f"The OCR file {file_path} is empty")
                            empty_ocr_counter = empty_ocr_counter+1
            if ocr_miss_counter > 0:
                raise FileNotFoundError(
                    "The count mismatch for image and ocr files.")
            if empty_ocr_counter > 0:
                raise Exception("Empty OCR files are generated.")
        _check_in_out_record_count()
        _check_img_ocr_file()

    def __validate_dependencies(self, ocr_tool):
        error_text = None
        if not os.path.exists(os.environ['FORMAT_CONVERTER_HOME']):
            error_text = "'{}' has invalid path".format(
                os.environ['FORMAT_CONVERTER_HOME'])

        if ocr_tool == 'tesseract' and not os.path.exists(os.environ.get("TESSERACT_PATH")):
            error_text = "'{}' has invalid path".format(
                os.environ['TESSERACT_PATH'])

        if error_text is not None:
            logger.error(error_text)
            raise Exception(error_text)

    def __get_max_image_file_size(self, ocr_tools):
        file_sizes = []
        for ocr_tool in ocr_tools:
            if ocr_tool == OcrType.AZURE_READ:
                file_sizes.append(
                    int(app_config[L_DEFAULT][ImageFileSizeMb.AZURE_READ]))
            elif ocr_tool == OcrType.AZURE_OCR:
                file_sizes.append(
                    int(app_config[L_DEFAULT][ImageFileSizeMb.AZURE_OCR]))
            elif ocr_tool == OcrType.TESSERACT:
                file_sizes.append(
                    int(app_config[L_DEFAULT][ImageFileSizeMb.TESSERACT]))
            else:
                file_sizes.append(
                    int(app_config[L_DEFAULT][ImageFileSizeMb.ABBYY]))
        return min(file_sizes)

    def __get_min_max_image_pixel(self, ocr_tools):
        def _min_max_list(mm_str):
            min_str, max_str = mm_str.split(",")
            return min_str.strip().split("*"), max_str.strip().split("*")
        ocr_mm_limit_list = []
        for ocr_tool in ocr_tools:
            if ocr_tool == OcrType.AZURE_READ:
                ocr_mm_limit_list.append(list(_min_max_list(
                    app_config[L_DEFAULT][ImagePixel.AZURE_READ])))
            elif ocr_tool == OcrType.AZURE_OCR:
                ocr_mm_limit_list.append(list(_min_max_list(
                    app_config[L_DEFAULT][ImagePixel.AZURE_OCR])))
            elif ocr_tool == OcrType.TESSERACT:
                ocr_mm_limit_list.append(list(_min_max_list(
                    app_config[L_DEFAULT][ImagePixel.TESSERACT])))
            else:
                ocr_mm_limit_list.append(list(_min_max_list(
                    app_config[L_DEFAULT][ImagePixel.ABBYY])))
        max_minpixel = max(ocr_mm_limit_list, key=lambda x: x[0][0])
        return max_minpixel

    def __convert_doc_to_images(self, work_doc_file, pages, max_file_size, min_max_pixel_list):
        dpi, valid_image_dimension = {}, True
        doc_file_ext = None if os.path.isdir(
            work_doc_file) else FileUtil.get_file_exe(work_doc_file)
        in_supporting_file_dir = f"{work_doc_file}_files"
        if doc_file_ext is None:
            FileUtil.copy_recursively(work_doc_file, in_supporting_file_dir)
        else:
            in_supporting_file_dir = FileUtil.create_dirs_if_absent(
                in_supporting_file_dir)
        image_file_list = []

        ocr_supported_imgs = [img_ext.lower().replace('*', '') for img_ext in app_config["DEFAULT"]
                              ["docwb_ocr_supported_file_types"].split(",")]
        if doc_file_ext is None:
            all_recursive_files = FileUtil.get_files(
                in_supporting_file_dir, file_format="**/*.*", recursive=True)
            for doc_file in all_recursive_files:
                new_image_file_list, _, _ = self.__convert_doc_to_images(
                    doc_file, pages, max_file_size, min_max_pixel_list)
                image_file_list += new_image_file_list
        elif doc_file_ext.lower() == FileExtension.PDF:
            logger.info(
                "Found input PDF file, hence PDF to image conversion started.")
            temps_pages = pages
            out_range_pages = []
            is_unimplemented_error = False
            for dpi in range(300, 100, -50):
                is_unimplemented_error = False
                logger.info(
                    f"Start PDF2IMG for PDF- {work_doc_file}, dpi- {dpi}, pages- {temps_pages}")
                image_file_list, error = self.image_generator_obj.convert_pdf_to_image(
                    os.environ['FORMAT_CONVERTER_HOME'],
                    work_doc_file, in_supporting_file_dir, pages=temps_pages, dpi=dpi)

                if error and 'OutOfMemoryError' in error:
                    continue
                elif error and 'not implemented in PDFBox' in error:
                    logger.error(error)
                    is_unimplemented_error = True
                    break
                elif not image_file_list and not error:
                    # made this fix because of this error in pdfbox
                    # "Numbers of source Raster bands and source color space components do not match"
                    is_unimplemented_error = True
                    break
                elif error:
                    logger.error(error)

                out_range_pages = self.__get_size_out_of_range_images(
                    image_file_list, max_file_size, min_max_pixel_list)
                if not out_range_pages:
                    break
                temps_pages = ",".join(out_range_pages)

            valid_image_dimension = (len(out_range_pages) == 0)
            if is_unimplemented_error:
                # PDFbox unimplemented issue, hence consider the scenario as non-ocr based
                valid_image_dimension = False
            elif len(image_file_list) == 0:
                raise Exception(
                    f"Found no images for PDF {work_doc_file}")
            else:
                dpi = {os.path.basename(image_file).split(
                    ".")[0]: dpi for image_file in image_file_list}

            logger.info(
                f"PDF to Images generated to {in_supporting_file_dir}")
        elif doc_file_ext.lower() == FileExtension.ZIP:
            logger.info(
                "Found input ZIP file, hence ZIP to image conversion started.")
            unzipped_files = FileUtil.unzip_file_to_path(
                work_doc_file, in_supporting_file_dir)
            for doc_file in unzipped_files:
                new_image_file_list, _, _ = self.__convert_doc_to_images(
                    doc_file, pages, max_file_size, min_max_pixel_list)
                image_file_list += new_image_file_list
        elif doc_file_ext.lower() in ocr_supported_imgs:
            save_image_path = f'{in_supporting_file_dir}/1.jpg'
            image_file_list += [FileUtil.convert_image_to_jpg(
                work_doc_file, save_image_path)]
        else:
            valid_image_dimension = False
        return image_file_list, dpi, valid_image_dimension

    def __get_size_out_of_range_images(self, image_file_list, max_file_size, min_max_pixel_list):
        min_px_list, max_px_list = (min_max_pixel_list)
        min_px_list = list(map(int, min_px_list))
        max_px_list = list(map(int, max_px_list))
        out_range_pages = []
        for image in image_file_list:
            page_num = os.path.basename(image).split(".")[0]
            try:
                if FileUtil.get_file_size_in_mb(image) > max_file_size:
                    out_range_pages.append(page_num)
                    continue

                img_obj = imageio.imread(image)
                (height, width) = img_obj.shape[:2]
                if width < min_px_list[0] or width > max_px_list[0]:
                    out_range_pages.append(page_num)
                    continue
                if height < min_px_list[1] or height > max_px_list[1]:
                    out_range_pages.append(page_num)
                    continue
            except Exception:
                out_range_pages.append(page_num)
        return out_range_pages

    def __update_summary(self, summary_dict, doc_id, status, column_name):
        if summary_dict.get(column_name):
            summary_dict[column_name][doc_id] = status
        else:
            summary_dict[column_name] = {
                doc_id: status
            }
