# ===============================================================================================================#
# Copyright 2023 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

import copy
import json
import re
import traceback
from common.file_util import FileUtil
from rules.rules_property.rule_property_base_class import (
    RESPONSE_DICT, RES_VALUE_DICT, RulePropertyBaseClass)
from common.ainauto_logger_factory import AinautoLoggerFactory
from service.api_caller import ApiCaller
from service.email_parser_service import EmailParserService
logger = AinautoLoggerFactory().get_logger()

DOC_MATCH_CLASS = {
    "primary": {
        "anchorText": [
            ["from:", "to:", "subject:", "bharat sanchar", "airtel"]
        ],
        "anchorTextMatch": {
            "method": "regex"
        },
        "pageNum": [
            1
        ]
    }
}


class Rule0002SysPropertyPrimaryDocument(RulePropertyBaseClass):
    def __init__(self) -> None:
        super().__init__()
        self._api_caller_obj: ApiCaller = ApiCaller()
        self._ocr_parser_obj = None
        self._group_records = None

    def pre_hook_doc_ocr_parser_obj(self, ocr_parser_obj):
        self._ocr_parser_obj = ocr_parser_obj

    def pre_hook_group_records(self, group_records):
        self._group_records = group_records

    def extract(self, doc_data_dict: dict) -> RESPONSE_DICT:
        response_dict = copy.deepcopy(RESPONSE_DICT)
        response_val_dict = copy.deepcopy(RES_VALUE_DICT)

        def _get_primary():
            result_data = "false"
            # ------ Set current record as primary when found only one document in group -----
            if (self._group_records and len(self._group_records) == 1):
                return "true"
            doc_file_path = doc_data_dict.get('doc_copy_path') if doc_data_dict.get(
                'doc_copy_path', None) else doc_data_dict.get('doc_original_path')
            try:
                if not self._ocr_parser_obj and doc_file_path.lower().endswith(".json"):
                    EmailParserService(doc_file_path)
                    result_data = "true"
                for _, val in DOC_MATCH_CLASS.items():
                    if self._ocr_parser_obj:
                        # ---- OCR PARSER BASED
                        regions_res = self._ocr_parser_obj.get_bbox_for([val])
                        if not regions_res["error"]:
                            result_data = "true"
                            break
                    else:
                        # ---- Text files
                        result_data = self.__get_from_txt_file(
                            doc_file_path, val)
                        if result_data:
                            break
            except:
                pass
            return result_data

        try:
            response_val_dict["text"] = _get_primary()
            response_val_dict['confidencePct'] = 80
            response_dict['values'] = [response_val_dict]
        except Exception as e:
            response_dict['message']['error'] += [e.args[0]]
            full_trace_error = traceback.format_exc()
            logger.error(full_trace_error)
        return response_dict

    def __get_from_txt_file(self, file_path, val):
        txt = FileUtil.read_file(file_path)
        result_data = None
        if not txt:
            result_data = "false"
        for pattern_txt in val.get('anchorText')[0]:
            if re.search(pattern_txt.lower(), txt.lower()):
                result_data = "true"
                break
        return result_data
