# ===============================================================================================================#
# Copyright 2023 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

from process.dpp_process.dpp_profile_attribute_process import DppProfileAttributeProcess
from rules.rules_profile.rule_profile_base_class import (RuleProfileBaseClass)
from common.ainauto_logger_factory import AinautoLoggerFactory

logger = AinautoLoggerFactory().get_logger()


class Rule0002CusDppProfileDefinition(RuleProfileBaseClass):
    def __init__(self) -> None:
        super().__init__()
        self._doc_data_dict = None
        self._master_config = None
        self._group_records = None
        self._attr_ext_prov_config = None

    def pre_hook_master_config(self, master_config):
        self._master_config = master_config

    def pre_hook_group_records(self, group_records):
        self._group_records = group_records

    def extract(self, doc_data_dict: dict, extractor_config_dict: dict) -> tuple:
        profile_input = extractor_config_dict['profileMatchRule']['ruleInput']
        attr_res_dict = DppProfileAttributeProcess(
            profile_input, doc_data_dict).extract_attributes()
        return attr_res_dict, {}
