# ===============================================================================================================#
# Copyright 2020 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

import copy
from common.common_util import CommonUtil
from rules.rules_profile.rule_profile_base_class import (RuleProfileBaseClass)
from common.ainauto_logger_factory import AinautoLoggerFactory
from rules.rules_property.rule_property_base_class import RulePropertyBaseClass

logger = AinautoLoggerFactory().get_logger()


class Rule0001SysProfileDefinition(RuleProfileBaseClass):
    def __init__(self) -> None:
        super().__init__()
        self._ocr_parser_obj = None
        self._doc_data_dict = None
        self._master_config = None
        self._group_records = None

    def pre_hook_doc_ocr_parser_obj(self, ocr_parser_obj):
        self._ocr_parser_obj = ocr_parser_obj

    def pre_hook_master_config(self, master_config):
        self._master_config = master_config

    def pre_hook_group_records(self, group_records):
        self._group_records = group_records

    def extract(self, doc_data_dict: dict, extractor_config_dict: dict) -> tuple:
        self._doc_data_dict = doc_data_dict
        profile_input = extractor_config_dict['profileMatchRule']['ruleInput']
        # ----- Scenario - Profile match with the given keyword in OCR.
        match_def_value_dict, debug_info_dict = self.__get_match_def_val(
            profile_input)
        return match_def_value_dict, debug_info_dict

    def __get_match_def_val(self, profile_input) -> dict:
        match_def_value_dict = {}
        debug_info_dict = {}
        for x in profile_input['profileMatchDefinitions']:
            match_def_value_dict[x['name']] = []
            if self._ocr_parser_obj and x.get('profileMatchDefinition'):
                # ---- OCR PARSER BASED
                regions_res = self._ocr_parser_obj.get_bbox_for(
                    [x['profileMatchDefinition']])
                debug_info_dict['ocr_parser'] = {
                    'input': x['profileMatchDefinition'], 'output': regions_res}
                if not regions_res["error"]:
                    # match_def_value_dict[x['name']] = regions_res['regions']
                    values = []
                    for y in regions_res['regions']:
                        if y.get('regionBBox'):
                            values += y.get('regionBBox')
                    match_def_value_dict[x['name']] = values
            elif x.get('ruleName'):
                # ---- RULE BASED
                rule_in_out_result = self.__execute_rule(x['ruleName'])
                debug_info_dict[x['ruleName']] = rule_in_out_result
                rule_result = rule_in_out_result.get('output')
                if not rule_result.get('message', {}).get('error'):
                    match_def_value_dict[x['name']] = rule_result['values']
        return match_def_value_dict, debug_info_dict

    def __execute_rule(self, rule_name):
        rule_class = CommonUtil.get_rule_class_instance(
            rule_name, rc_entity_name='rules_property')
        rule_instance: RulePropertyBaseClass = rule_class()
        rule_result = rule_instance.template_method(copy.deepcopy(
            self._doc_data_dict), copy.deepcopy(self._master_config),
            self._ocr_parser_obj, self._group_records)
        return rule_result
