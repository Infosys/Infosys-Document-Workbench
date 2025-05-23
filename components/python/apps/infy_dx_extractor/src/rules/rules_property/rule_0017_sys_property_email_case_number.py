# ===============================================================================================================#
# Copyright 2023 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

import copy
from rules.rules_property.rule_property_base_class import (
    RESPONSE_DICT, RES_VALUE_DICT, RulePropertyBaseClass)
from common.ainauto_logger_factory import AinautoLoggerFactory
from service.api_caller import ApiCaller
from service.case_finder_service import CaseFinderService
from service.email_parser_service import EmailParserService
from infy_docwb_case_finder.data.case_finder_req_data import (
    CaseFinderReqData, EmailData)

logger = AinautoLoggerFactory().get_logger()
docwb_case_finder = CaseFinderService()


class Rule0017SysPropertyEmailCaseNumber(RulePropertyBaseClass):
    def __init__(self) -> None:
        super().__init__()
        self._api_caller_obj: ApiCaller = ApiCaller()
        self._ocr_parser_obj = None
        self.__rule_config_input = None
        self.__master_config = None

    def pre_hook_master_config(self, master_config):
        self.__master_config = master_config

    def pre_hook_doc_ocr_parser_obj(self, ocr_parser_obj):
        self._ocr_parser_obj = ocr_parser_obj

    def pre_hook_rule_config_input(self, rule_config_input: dict):
        self.__rule_config_input = rule_config_input

    def extract(self, doc_data_dict: dict) -> RESPONSE_DICT:
        tenant_id = doc_data_dict.get('docwb_query_attribute').get('tenant_id')
        docwb_case_finder.create_instance(
            tenant_id, self.__master_config.get('case_creator'))
        response_dict = copy.deepcopy(RESPONSE_DICT)
        response_val_dict = copy.deepcopy(RES_VALUE_DICT)
        try:
            doc_file_path = doc_data_dict.get('doc_copy_path') if doc_data_dict.get(
                'doc_copy_path', None) else doc_data_dict.get('doc_original_path')
            esubject = EmailParserService(doc_file_path).get_subject()
            tenant_id = doc_data_dict.get(
                'docwb_query_attribute').get('tenant_id')
            result = docwb_case_finder.get_instance(tenant_id).query_for(
                req_data=CaseFinderReqData(email=EmailData(subject=esubject)),
                additional_config_param_dict={'case_num_regex_pattern': self.__rule_config_input.get('case_num_regex_pattern')})
            response_val_dict["text"] = None
            if result.response and result.response.records:
                response_val_dict["text"] = result.response.records[0].docwb.case_number
            response_val_dict['confidencePct'] = 80
        except Exception:
            pass

        response_dict['values'] = [response_val_dict]
        return response_dict
