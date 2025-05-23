# ===============================================================================================================#
# Copyright 2023 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

import copy
import re
import traceback
from common.file_util import FileUtil
from rules.rules_property.rule_property_base_class import (
    RESPONSE_DICT, RES_VALUE_DICT, RulePropertyBaseClass)
from common.ainauto_logger_factory import AinautoLoggerFactory
from service.api_caller import ApiCaller
logger = AinautoLoggerFactory().get_logger()


class Rule0001SysPropertyClassifyDocument(RulePropertyBaseClass):
    def __init__(self) -> None:
        super().__init__()
        self._api_caller_obj: ApiCaller = ApiCaller()
        self._ocr_parser_obj = None
        self._rule_input = None

    def pre_hook_doc_ocr_parser_obj(self, ocr_parser_obj):
        self._ocr_parser_obj = ocr_parser_obj

    def pre_hook_rule_config_input(self, rule_config_input: dict):
        self._rule_input = rule_config_input

    def extract(self, doc_data_dict: dict) -> RESPONSE_DICT:
        response_dict = copy.deepcopy(RESPONSE_DICT)
        response_val_dict = copy.deepcopy(RES_VALUE_DICT)

        def _get_classification():
            result_data = None
            for key, val in self._rule_input.get('match_class').items():
                if self._ocr_parser_obj:
                    # ---- OCR PARSER BASED
                    regions_res = self._ocr_parser_obj.get_bbox_for([val])
                    if not regions_res["error"]:
                        result_data = key
                        break
                else:
                    doc_file_path = doc_data_dict.get('doc_copy_path') if doc_data_dict.get(
                        'doc_copy_path', None) else doc_data_dict.get('doc_original_path')
                    txt = FileUtil.read_file(doc_file_path)
                    if not txt:
                        result_data = key
                        break
                    for pattern_txt in val.get('anchorText')[0]:
                        if re.search(pattern_txt.lower(), txt.lower()):
                            result_data = key
                            break
                    if result_data:
                        break
            return result_data

        try:
            response_val_dict["text"] = _get_classification()
            response_val_dict['confidencePct'] = 80
            response_dict['values'] = [response_val_dict]
        except Exception as e:
            response_dict['message']['error'] += [e.args[0]]
            full_trace_error = traceback.format_exc()
            logger.error(full_trace_error)
        return response_dict
