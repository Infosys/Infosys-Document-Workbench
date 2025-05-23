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
DEFAULT_DOC_TYPE = "Unknown"
CATEGORY_TO_ATTR_MAP: dict = {}


class RuleSysCategory(RuleAttributeBaseClass):
    def __init__(self, config_params_dict: dict) -> None:
        super().__init__(config_params_dict)

    def extract(self, raw_attribute_data_list: list, business_attribute_data: dict) -> BIZ_ATTRIBUTE_DATA_DICT:
        try:
            attr_value = DEFAULT_DOC_TYPE
            for raw_attribute_data in raw_attribute_data_list:
                attr_value_temp = ''
                for key, vals in CATEGORY_TO_ATTR_MAP.items():
                    if raw_attribute_data['name'] in vals:
                        attr_value_temp = key
                        break
                if attr_value_temp:
                    attr_value = attr_value_temp
                    break
            business_attribute_data['attribute_values'].append(
                attr_value)
            business_attribute_data['confidence_pct'] = 80
        except Exception as e:
            business_attribute_data['message']['error'] += [e.args[0]]
            full_trace_error = traceback.format_exc()
            logger.error(full_trace_error)

        return business_attribute_data
