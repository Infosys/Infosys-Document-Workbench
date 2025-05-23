# ===============================================================================================================#
# Copyright 2023 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

import re
import copy
import traceback
from rules.rules_property.rule_property_base_class import (
    RESPONSE_DICT, RES_VALUE_DICT, RulePropertyBaseClass)
from common.ainauto_logger_factory import AinautoLoggerFactory
from service.question_answering_service import QuestionAnsweringService
logger = AinautoLoggerFactory().get_logger()


class Rule1004CusPropertyLoanCompanyName(RulePropertyBaseClass):
    def __init__(self) -> None:
        super().__init__()
        self._qa_service: QuestionAnsweringService = QuestionAnsweringService()

    def extract(self, doc_data_dict: dict) -> RESPONSE_DICT:
        response_dict = copy.deepcopy(RESPONSE_DICT)
        response_val_dict = copy.deepcopy(RES_VALUE_DICT)
        try:
            doc_file_path = doc_data_dict.get('doc_copy_path') if doc_data_dict.get(
                'doc_copy_path', None) else doc_data_dict.get('doc_original_path')
            response_val_dict["text"] = self._qa_service.get_answer(
                doc_file_path, 'what is the company name?')
            response_val_dict['confidencePct'] = 80
        except Exception:
            pass

        response_dict['values'] = [response_val_dict]
        return response_dict
