# ===============================================================================================================#
# Copyright 2020 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

import json
import os
import shutil
from common.file_util import FileUtil
from infy_common_utils.format_converter import FormatConverter, ConvertAction
import infy_common_utils.format_converter as format_converter
# from infy_borderless_table_extractor import extractor
from infy_table_extractor import bordered_table_extractor
from infy_table_extractor.bordered_table_extractor.providers.tesseract_data_service_provider import TesseractDataServiceProvider as borderTesseract
# from infy_borderless_table_extractor.providers.tesseract_data_service_provider import TesseractDataServiceProvider as boarderTesseract
from infy_table_extractor.interface import OutputFileFormat
from infy_field_extractor import text_extractor, checkbox_extractor, radio_button_extractor
from infy_field_extractor.providers.ocr_data_service_provider import OcrDataServiceProvider
from infy_field_extractor.providers.nativepdf_data_service_provider import NativePdfDataServiceProvider

# from infy_object_detector import object_detector
# from paragraph import paragraph_extractor
from common.ainauto_logger_factory import AinautoLoggerFactory
from common.app_config_manager import AppConfigManager
from common.properties.extract_props import ExtractProps
app_config = AppConfigManager().get_app_config()
logger = AinautoLoggerFactory().get_logger()


format_converter.format_converter_jar_home = os.environ.get(
    "FORMAT_CONVERTER_HOME")


class ApiCaller:
    object_detector_obj = None

    def __init__(self):
        self.tesseract_path = os.environ.get("TESSERACT_PATH")
        self.temp_path = app_config['DEFAULT']['temp_path']

        if self.tesseract_path:
            provider = borderTesseract(
                self.tesseract_path, logger=logger)
            self.bordered_tbl_obj = bordered_table_extractor.BorderedTableExtractor(
                provider, provider, FileUtil.create_dirs_if_absent(
                    f"{self.temp_path}/bordered_table_extractor"),
                logger, debug_mode_check=True)

        # if not self.object_detector_obj:
        #     self.object_detector_obj = object_detector.ObjectDetector(
        #         app_config['OBJECT_DETECTOR']['pickle_file_path'], app_config['OBJECT_DETECTOR']['hdf5_file_path'], logger)
        # self.para_obj = paragraph_extractor.ParagraphExtractor(
        #     tesseract_path, logger)

    def call_format_converter_for_pdf_to_json(self, from_file, config_param_dict):
        convert_action = ConvertAction.PDF_TO_JSON
        try:
            output = FormatConverter.execute(
                from_file, convert_action, config_param_dict)
        except Exception as e:
            logger.error(e)
        input = {"from_file": from_file,
                 "convert_action": convert_action.value,
                 "config_param_dict": config_param_dict}
        logger.info("API Request --> " + json.dumps(input))
        # logger.info("API Response --> " + json.dumps(output))
        return {"format_converter": {"input": input, "output": output[0]}}

    def call_format_converter_for_pdf_to_text(self, from_file, config_param_dict):
        convert_action = ConvertAction.PDF_TO_TXT
        try:
            output, _ = FormatConverter.execute(
                from_file, convert_action=convert_action,
                config_param_dict=config_param_dict)
        except Exception as e:
            logger.error(e)
        input = {"from_file": from_file,
                 "convert_action": convert_action.value,
                 "config_param_dict": config_param_dict}
        logger.info("API Request --> " + json.dumps(input))
        # logger.info("API Response --> " + json.dumps(output))
        return {"format_converter": {"input": input, "output": output}}

    def call_borderless_table_converter(self, image_path, output_dir, table_organization_dict={}, bbox=[]):
        # TODO : :  below logic needed code changes and
        # not in scope for current release hence commenting it to avoid run time error

        # output = None
        # if not bbox:
        #     bbox = []
        # image_path = [os.path.abspath(image_path)]
        # output_dir = os.path.abspath(output_dir)
        # try:
        #     tesseract_provider = borderTesseract(
        #         self.tesseract_path)
        #     bless_tbl_obj = extractor.Extractor(
        #         FileUtil.create_dirs_if_absent(
        #             f"{self.temp_path}/borderless_table_extractor"),
        #         token_rows_cols_provider=tesseract_provider,
        #         token_detection_provider=tesseract_provider,
        #         token_enhance_provider=tesseract_provider,
        #         pytesseract_path=self.tesseract_path, debug_mode_check=True
        #     )
        #     output = self.bless_tbl_obj.extract_all_fields(
        #         image_path, output_dir, table_organization_dict, within_bbox=bbox)
        # except Exception as e:
        #     logger.error(e)
        # input = {"image_path": image_path,
        #          "output_dir": output_dir,
        #          "table_organization_dict": table_organization_dict,
        #          "within_bbox": bbox}
        # logger.info("API Request --> " + json.dumps(input))
        # logger.info("API Response --> " + json.dumps(output))
        # return {"borderless_table_extractor": {"input": input, "output": output}}
        return {}

    def call_bordered_table_extractor(self, image_path, within_bbox, output_file_path):
        output = None
        config_param_dict = {'output': {'path': output_file_path,
                                        'format': [OutputFileFormat.EXCEL]}}
        try:

            output = self.bordered_tbl_obj.extract_all_fields(
                image_path, within_bbox, config_param_dict=config_param_dict)
        except Exception as e:
            logger.error(e)
        input = {"image_path": image_path,
                 "within_bbox": within_bbox,
                 "output_file_path": output_file_path}
        logger.info("API Request --> " + json.dumps(input))
        logger.info("API Response --> " + json.dumps(output))
        if self.bordered_tbl_obj.debug_mode_check == True and output is not None and output['error'] is None:
            source_folder = output['fields'][0]['debug_path']
            destination_folder = output_file_path + \
                '/' + os.path.basename(source_folder)
            shutil.copytree(source_folder, destination_folder)
        return {"bordered_table_extractor": {"input": input, "output": output}}

    def call_text_extractor(self, img_path, label, bbox, ocr_parser_obj, scaling_factor, extract_props_obj: ExtractProps,
                            page_no=None, provide_name='ocrTextParser'):
        field_list = [
            {
                "field_key": [label],
                "field_key_match": {"method": "normal", "similarityScore": 1},
                "field_value_bbox": bbox,
                "field_value_pos": "left"
            }
        ]
        config_params_dict = {
            'field_value_pos': "right",
            "page": page_no if page_no else {},
            "eliminate_list": [],
            "scaling_factor": scaling_factor,
            "within_bbox": []
        }

        output, file_data_list = None, []
        try:
            if provide_name == 'ocrTextParser':
                provider = OcrDataServiceProvider(
                    ocr_parser_obj, logger=logger)
            if provide_name == 'nativePdfParser':
                provider = NativePdfDataServiceProvider(logger=logger)
                file_data_list = [{
                    'path': extract_props_obj.original_file,
                    'pages': [page_no]
                }]

            txt_extractor_obj = text_extractor.TextExtractor(
                provider, provider, FileUtil.create_dirs_if_absent(f"{self.temp_path}/text_extractor"), logger)
            output = txt_extractor_obj.extract_custom_fields(
                image_path=img_path,
                text_field_data_list=field_list,
                config_params_dict=config_params_dict,
                file_data_list=file_data_list
            )
        except Exception as e:
            logger.error(e)
        input = {"ocr_parser_object": f"{type(ocr_parser_obj)}",
                 "text_field_data_list": field_list,
                 "config_params_dict": config_params_dict,
                 "scaling_factor": scaling_factor,
                 "technique": provide_name}
        logger.info("API Request --> " + json.dumps(input))
        logger.info("API Response --> " + json.dumps(output))
        return {"field_extractor": {"input": input, "output": output}}

    def call_checkbox_extractor(self, image_path, label, bbox, ocr_parser_obj, scaling_factor, page_no=None):
        output = None
        field_list = [
            {
                "field_key": [label],
                "field_key_match": {"method": "normal", "similarityScore": 1},
                "field_state_pos": "left",
                "field_state_bbox": bbox
            }
        ]
        config_params_dict = {
            'min_checkbox_text_scale': None,
            'max_checkbox_text_scale': None,
            'field_state_pos': None,
            'page': page_no if page_no else {},
            "eliminate_list": [],
            "scaling_factor": scaling_factor,
            "within_bbox": []
        }
        try:
            provider = OcrDataServiceProvider(
                ocr_parser_obj, logger=logger)
            checkbox_extractor_obj = checkbox_extractor.CheckboxExtractor(
                provider, provider, FileUtil.create_dirs_if_absent(f"{self.temp_path}/checkbox_extractor"), logger=logger)
            output = checkbox_extractor_obj.extract_custom_fields(
                image_path,
                checkbox_field_data_list=field_list,
                config_params_dict=config_params_dict
            )
        except Exception as e:
            logger.error(e)
        input = {
            "image_path": image_path,
            "ocr_parser_object": f"{type(ocr_parser_obj)}",
            "checkbox_field_data_list": field_list,
            "config_params_dict": config_params_dict,
            "scaling_factor": scaling_factor}
        logger.info("API Request --> " + json.dumps(input))
        logger.info("API Response --> " + json.dumps(output))
        return {"field_extractor": {"input": input, "output": output}}

    def call_radio_button_extractor(self, image_path, label, bbox, ocr_parser_obj, scaling_factor, page_no=None):
        output = None
        field_list = [
            {
                "field_key": [label],
                "field_key_match": {"method": "normal", "similarityScore": 1},
                "field_state_pos": "left",
                "field_state_bbox": bbox
            }
        ]
        config_params_dict = {
            'min_radius_text_scale': None,
            'max_radius_text_scale': None,
            'field_state_pos': None,
            'template_checked_folder': None,
            'template_unchecked_folder': None,
            'page': page_no if page_no else {},
            "eliminate_list": [],
            "scaling_factor": scaling_factor,
            "within_bbox": []
        }
        try:
            provider = OcrDataServiceProvider(
                ocr_parser_obj, logger=logger)
            radio_obj = radio_button_extractor.RadioButtonExtractor(
                provider, provider, FileUtil.create_dirs_if_absent(f"{self.temp_path}/radio_button_extractor"), logger)
            output = radio_obj.extract_custom_fields(
                image_path,
                radiobutton_field_data_list=field_list,
                config_params_dict=config_params_dict
            )
        except Exception as e:
            logger.error(e)
        input = {
            "image_path": image_path,
            "ocr_parser_object": f"{type(ocr_parser_obj)}",
            "radiobutton_field_data_list": field_list,
            "config_params_dict": config_params_dict,
            "scaling_factor": scaling_factor}
        logger.info("API Request --> " + json.dumps(input))
        logger.info("API Response --> " + json.dumps(output))
        return {"field_extractor": {"input": input, "output": output}}

    def call_paragraph_extractor(self, image_path, coordinates=[]):
        pass
        # TODO : : The below logic is not being used for the current release . So commenting out.
        # output = None
        # try:
        #     if len(coordinates) == 0:
        #         output = para_obj.extract(
        #             os.path.abspath(image_path))
        #     else:
        #         output = para_obj.extract_from_coordinates(
        #             os.path.abspath(image_path), coordinates)

        # except Exception as e:
        #     logger.error(e)
        # input = {"image_path": image_path,
        #          "coordinates": coordinates}
        # logger.info("API Request --> " + json.dumps(input))
        # logger.info("API Response --> " + json.dumps(output))
        # return {"paragraph_extractor": {"input": input, "output": output}}

    def call_lang_detector(self, text):
        # TODO : Below code needs to be implemented with a new library
        output = {"fields": [], "error": None}
        try:
            value = 'UNKNOWN_LANGUAGE'
            output["fields"] = [value]
        except Exception as e:
            logger.error(e)
            output["error"] = e.args[0]
        input = {"text": text}
        # logger.debug("API Request --> " + json.dumps(input))
        # logger.info("API Response --> " + json.dumps(output))
        return {"language_detector": {"input": input, "output": output}}

    def call_object_detector(self, image_path, detect_type, annotate_out_dir=None):
        output = None
        detect_type_dict = {}
        detect_type_dict[detect_type] = True
        detect_type_dict = {"detect_type": detect_type_dict}
        try:
            output = self.object_detector_obj.detect_for(
                [image_path], annotate_out_dir, detect_type_dict)
        except Exception as e:
            logger.error(e)
        input = {"image_path_list": [image_path],
                 "annotate_out_dir": annotate_out_dir,
                 "detect_type_dict": detect_type_dict}
        logger.info("API Request --> " + json.dumps(input))
        logger.info("API Response --> " + json.dumps(output))
        return {"object_detector": {"input": input, "output": output}}
