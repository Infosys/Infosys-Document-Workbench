# ===============================================================================================================#
# Copyright 2020 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

import traceback

from common.logger_factory import LoggerFactory
from rules.rules_attribute.rule_attribute_base_class import (
    BIZ_ATTRIBUTE_DATA_DICT, RuleAttributeBaseClass)

logger = LoggerFactory().get_logger()


class RuleSysListLastItem(RuleAttributeBaseClass):
    def __init__(self, config_params_dict: dict) -> None:
        super().__init__(config_params_dict)
        self._business_attribute_data_list = None

    def pre_hook_business_attribute_data_list(self, business_attribute_data_list):
        self._business_attribute_data_list = business_attribute_data_list

    def extract(self, raw_attribute_data_list: list, business_attribute_data: dict) -> BIZ_ATTRIBUTE_DATA_DICT:

        try:
            business_attribute_data['attribute_value_ids']['selected'] = []
            for attributes in self._business_attribute_data_list:
                if attributes['attribute_name'] == business_attribute_data['attribute_name'] + '::list':
                    business_attribute_data['attribute_values'] = [
                        attributes['attribute_values'][-1]]
                    business_attribute_data['confidence_pct'] = attributes['confidence_pct']
                    business_attribute_data['attribute_value_ids']['selected'] = [
                        attributes['attribute_value_ids']['selected'][-1]]
                    business_attribute_data['attribute_value_ids']['participated'].extend(
                        attributes['attribute_value_ids']['participated'])
                    break
        except Exception as e:
            business_attribute_data['message']['error'] += [e.args[0]]
            full_trace_error = traceback.format_exc()
            logger.error(full_trace_error)
        return business_attribute_data
