# ===============================================================================================================#
# Copyright 2020 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

import os
from common.common_util import Singleton
from common.app_const import *
import infy_common_utils.format_converter as format_converter
from infy_common_utils.format_converter import FormatConverter, ConvertAction
from common.ainauto_logger_factory import AinautoLoggerFactory
from common.app_config_manager import AppConfigManager

app_config = AppConfigManager().get_app_config()
logger = AinautoLoggerFactory().get_logger()


class ImageGeneratorService(metaclass=Singleton):

    def __init__(self):
        pass

    def convert_pdf_to_image(self, tool_path, pdf_file_path, output_path, pages, dpi='300'):
        pages = self.__get_page_list(pages)
        format_converter.format_converter_jar_home = tool_path
        config_param_dict = {
            "pages": pages,
            "dpi": dpi,
            "to_dir": output_path
        }
        # PDF_TO_IMAGE
        img_files, error = FormatConverter.execute(
            pdf_file_path, convert_action=ConvertAction.PDF_TO_IMAGE, config_param_dict=config_param_dict)

        if not img_files:
            img_files =[]

        return img_files, error

    def plot_bbox_on_image(self, all_generated_ocr_file_list):
        # TODO: Current plot bbox supports only hocr file
        # As abbyy introduce format converter change required to support this.
        for generate_ocr_res_dict in all_generated_ocr_file_list:
            provider_name = generate_ocr_res_dict['provider_name']
            generate_ocr_res_list = generate_ocr_res_dict['generate_ocr_response']
            if generate_ocr_res_list and provider_name == OcrType.TESSERACT:
                for ocr_res_dict in generate_ocr_res_list:
                    bbox_dir_path = os.path.join(
                        os.path.dirname(ocr_res_dict['input_doc']), 'bbox_files')
                    self.__plot_bbox(
                        os.path.abspath(
                            os.environ['FORMAT_CONVERTER_HOME']
                        ),
                        ocr_res_dict['input_doc'], ocr_res_dict['output_doc'], bbox_dir_path
                    )

    def __plot_bbox(self, tool_path, img_file_path, hocr_file_path, output_dir_path):
        try:
            from_file_path_1 = os.path.abspath(img_file_path)
            to_dir_path = os.path.abspath(output_dir_path)
            format_converter.format_converter_jar_home = tool_path
            config_param_dict = {
                "to_dir": to_dir_path,
                "hocr_file": os.path.abspath(hocr_file_path)
            }
            return FormatConverter.execute(
                from_file_path_1, convert_action=ConvertAction.PLOT_BBOX, config_param_dict=config_param_dict)

        except Exception as e:
            print(e)

    def __get_page_list(self, pages):
        page_list = []
        if not pages:
            return page_list
        try:
            for page in pages.split(','):
                if page.find('-') == -1:
                    page_list.append(int(page))
                elif page:
                    # TODO: need to handle all possible page ranges
                    page_range = [int(x) for x in page.split('-')]
                    for pg in range(page_range[0], page_range[1]+1):
                        page_list.append(pg)
        except Exception as e:
            pass
        return page_list
