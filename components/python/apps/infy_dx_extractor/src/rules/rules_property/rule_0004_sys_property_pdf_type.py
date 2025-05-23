# ===============================================================================================================#
# Copyright 2020 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

import os
import copy
import traceback
from rules.rules_property.rule_property_base_class import (
    RESPONSE_DICT, RES_VALUE_DICT, RulePropertyBaseClass)
from common.ainauto_logger_factory import AinautoLoggerFactory
from service.api_caller import ApiCaller
logger = AinautoLoggerFactory().get_logger()


class PdfType(object):
    NATIVE = 'PDF Native'
    SCANNED = 'PDF Scanned'


class Rule0004SysPropertyPdfType(RulePropertyBaseClass):
    def __init__(self) -> None:
        super().__init__()
        self._api_caller_obj: ApiCaller = ApiCaller()

    def extract(self, doc_data_dict: dict) -> RESPONSE_DICT:
        response_dict = copy.deepcopy(RESPONSE_DICT)
        response_val_dict = copy.deepcopy(RES_VALUE_DICT)

        def _get_text(file_path):
            config_param_dict = {"pages": [1]}
            result = self._api_caller_obj.call_format_converter_for_pdf_to_text(
                file_path, config_param_dict)
            result = result.get('format_converter', {}).get('output', '')
            return result

        try:
            attachment_file_path = doc_data_dict.get('doc_copy_path') if doc_data_dict.get(
                'doc_copy_path', None) else doc_data_dict.get('doc_original_path')

            if str(attachment_file_path).lower().endswith(".pdf"):
                content_len = len(_get_text(attachment_file_path))
                response_val_dict["text"] = PdfType.NATIVE if content_len > 25 else PdfType.SCANNED
                response_val_dict['confidencePct'] = 80
                response_dict['values'] = [response_val_dict]
            else:
                response_dict['message']['info'] += [
                    f"Executed rule {self.__class__.__name__}; Record doc_id:{doc_data_dict.get('doc_id')}; PDF file format not found."]
        except Exception as e:
            response_dict['message']['error'] += [e.args[0]]
            full_trace_error = traceback.format_exc()
            logger.error(full_trace_error)
        return response_dict
