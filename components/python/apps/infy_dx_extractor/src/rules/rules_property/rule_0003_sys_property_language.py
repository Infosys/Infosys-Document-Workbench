# ===============================================================================================================#
# Copyright 2020 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

import os
import copy
import traceback
from rules.rules_property.rule_property_base_class import (
    RESPONSE_DICT, RulePropertyBaseClass)
from common.ainauto_logger_factory import AinautoLoggerFactory
from service.api_caller import ApiCaller

logger = AinautoLoggerFactory().get_logger()


class Rule0003SysPropertyLanguage(RulePropertyBaseClass):
    def __init__(self) -> None:
        super().__init__()
        self._api_caller_obj: ApiCaller = ApiCaller()
        self._ocr_parser_obj = None

    def pre_hook_doc_ocr_parser_obj(self, ocr_parser_obj):
        self._ocr_parser_obj = ocr_parser_obj

    def extract(self, doc_data_dict: dict) -> RESPONSE_DICT:
        response_dict = copy.deepcopy(RESPONSE_DICT)
        try:
            attachment_file_path = doc_data_dict.get('doc_copy_path') if doc_data_dict.get(
                'doc_copy_path', None) else doc_data_dict.get('doc_original_path')
            file_content = None
            # ---- GET FILE CONTENT OF PDF/IMAGES
            if str(attachment_file_path).lower().endswith(".pdf"):
                file_content = self.__get_pdf_text(attachment_file_path)
            elif self._ocr_parser_obj:
                file_content = self.__get_ocr_parser_text()
            # ---- GET LANGUAGES OF FILE CONTENT
            if file_content:
                response_dict['values'], error = self.__get_language(
                    file_content)
                if error:
                    response_dict['message']['error'] += [error]
            else:
                response_dict['message']['info'] += [
                    f"Executed rule {self.__class__.__name__}; Record doc_id:{doc_data_dict.get('doc_id')}; Document format is not supported"]
        except Exception as e:
            response_dict['message']['error'] += [e.args[0]]
            full_trace_error = traceback.format_exc()
            logger.error(full_trace_error)
        return response_dict

    def __get_pdf_text(self, file_path):
        config_param_dict = {"pages": [1]}
        result = self._api_caller_obj.call_format_converter_for_pdf_to_text(
            file_path, config_param_dict)
        result = result.get('format_converter', {}).get('output', '')
        return result

    def __get_ocr_parser_text(self):
        token_line = 2
        result = self._ocr_parser_obj.get_tokens_from_ocr(
            token_type_value=token_line, pages=[1])
        token_line_text = "\n".join([x['text'] for x in result])
        return token_line_text

    def __get_language(self, text):
        result = self._api_caller_obj.call_lang_detector(text)
        result = result.get('language_detector', {}).get('output', {})
        lang_list, error = [], result.get('error')
        if error:
            return lang_list, error
        for x in result.get('fields'):
            for y in x:
                lang_list.append(
                    {'text': y.get('language_name'), 'confidencePct': y.get('probability')})
        return lang_list, error
