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


class RuleSysConsolidateValues(RuleAttributeBaseClass):
    def __init__(self, config_params_dict: dict) -> None:
        super().__init__(config_params_dict)

    def extract(self, raw_attribute_data_list: list, business_attribute_data: dict) -> BIZ_ATTRIBUTE_DATA_DICT:
        try:
            # remove following condition to override in custom rules.
            if business_attribute_data['attribute_values']:
                return None
            selected_counter = 0
            try:
                selected_ids = list(
                    business_attribute_data['attribute_value_ids']['selected'])
                # enable below line to override in custom rules.
                # business_attribute_data['attribute_value_ids']['selected'] = []
                for raw_attribute_data in raw_attribute_data_list:
                    for attr_type_val in raw_attribute_data['values']:
                        for attr_val in attr_type_val[attr_type_val['type']]:
                            if attr_val['id'] in selected_ids:
                                text_val = attr_val['text'] if attr_val.get(
                                    'state', "") == "" else f"{attr_val['text']}-{attr_val['state']}"
                                if attr_val.get('state', "") == "":
                                    text_val = attr_val['text']
                                else:
                                    text_val = f"{attr_val['text']}-{attr_val['state']}" if attr_val['text'] else f"{attr_val['state']}"
                                business_attribute_data['attribute_values'].append(
                                    text_val)
                                # enable below lines to override in custom rules.
                                # business_attribute_data['confidence_pct'] = 80
                                # business_attribute_data['attribute_value_ids']['selected'] += [
                                #         attr_val['id']]
                                selected_counter += 1
                                if len(selected_ids) == selected_counter:
                                    raise RuntimeWarning('Iteration Stopped.')
            except Exception:
                # Known exception to stop the multi-level iteration at once
                pass
        except Exception as e:
            business_attribute_data['message']['error'] += [e.args[0]]
            full_trace_error = traceback.format_exc()
            logger.error(full_trace_error)
        return business_attribute_data
