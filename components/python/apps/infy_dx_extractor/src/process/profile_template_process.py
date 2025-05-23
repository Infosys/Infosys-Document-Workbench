# ===============================================================================================================#
# Copyright 2022 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

import copy

from common.ainauto_logger_factory import AinautoLoggerFactory
from common.app_const import SessionKey
from common.app_session_store import AppSessionStore
from common.common_util import CommonUtil, Singleton
from rules.rules_profile.rule_profile_base_class import RuleProfileBaseClass

logger = AinautoLoggerFactory().get_logger()
app_session_store = AppSessionStore()


class ProfileTemplateProcess(metaclass=Singleton):
    def __init__(self) -> None:
        self._master_config = app_session_store.get_data(
            SessionKey.MASTER_CONFIG_FILE_DATA)
        self._extractor_config_dict = app_session_store.get_data(
            SessionKey.EXT_CONFIG_FILE_DATA)
        self.__validate_profile_def()

    def get_doc_profile(self, doc_data_dict, group_records, ocr_parser_obj):
        def _execute_rule_profile():
            rule_class = CommonUtil.get_rule_class_instance(
                rule_name, rc_entity_name='rules_profile')
            rule_instance: RuleProfileBaseClass = rule_class()
            rule_result = rule_instance.template_method(
                copy.deepcopy(doc_data_dict), copy.deepcopy(ecd),
                copy.deepcopy(self._master_config), ocr_parser_obj, group_records)
            return rule_result

        selected_profile = None
        enabled_profiles = [x for x in self._extractor_config_dict.get(
            'documentTemplates', []) if x.get("enabled")]
        for ecd in enabled_profiles:
            rule_name = ecd.get('profileMatchRule').get('ruleName')
            rule_response = _execute_rule_profile()
            doc_data_dict['extraction_profile_rules_exe_history'].append(
                {rule_name: rule_response})
            if rule_response['output']['selected']:
                selected_profile = rule_response['output']['profileDict']
                doc_data_dict['message'] = rule_response['output']['message']
                break
        return selected_profile

    def __validate_profile_def(self):
        default_profile_count = 0
        for template in self._extractor_config_dict['documentTemplates']:
            if not template.get('enabled'):
                continue
            def_list = template['profileMatchRule']['ruleInput']['profileMatchDefinitions']
            default_profile_count += 1 if not def_list else 0
        if default_profile_count > 1:
            error_msg = 'Found more than one profile having the EMPTY profileMatchDefinitions.'
            error_msg += ' Please setup `profileMatchDefinitions` properly in extractor config file.'
            raise Exception(error_msg)
