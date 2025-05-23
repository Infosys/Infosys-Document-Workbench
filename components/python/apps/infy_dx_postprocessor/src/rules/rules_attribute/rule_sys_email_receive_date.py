# ===============================================================================================================#
# Copyright 2023 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#
import traceback
from datetime import datetime
from dateutil import tz
from common.logger_factory import LoggerFactory
from rules.rules_attribute.rule_attribute_base_class import (
    BIZ_ATTRIBUTE_DATA_DICT, RuleAttributeBaseClass)

logger = LoggerFactory().get_logger()


class RuleSysEmailReceiveDate(RuleAttributeBaseClass):
    """Rule to extract the DOCWB File Receive Date"""

    def __init__(self, config_params_dict: dict) -> None:
        super().__init__(config_params_dict)

    def extract(self, raw_attribute_data_list: list, business_attribute_data: dict) -> BIZ_ATTRIBUTE_DATA_DICT:
        try:
            selected_counter = 0
            try:
                # selected_ids = list(
                #     business_attribute_data['attribute_value_ids']['selected'])
                business_attribute_data['attribute_value_ids']['selected'] = []
                for raw_attribute_data in raw_attribute_data_list:
                    for attr_type_val in raw_attribute_data['values']:
                        for attr_val in attr_type_val[attr_type_val['type']]:
                            text_val = attr_val['text'] if attr_val.get(
                                'state', "") == "" else f"{attr_val['text']}-{attr_val['state']}"
                            if attr_val.get('state', "") == "":
                                text_val = attr_val['text']
                            else:
                                text_val = f"{attr_val['text']}-{attr_val['state']}" if attr_val['text'] else f"{attr_val['state']}"

                            date_object = datetime.strptime(
                                text_val, '%a, %d %b %Y %H:%M:%S %z')

                            # convert datetime object to standard date time with time zone format
                            date_formatted = date_object.astimezone(tz.tzutc()).strftime(
                                '%a %b %d %Y %H:%M:%S %Z')
                            business_attribute_data['attribute_values'].append(
                                date_formatted)
                            # business_attribute_data['confidence_pct'] = 80
                            business_attribute_data['attribute_value_ids']['selected'] += [
                                attr_val['id']]
            except Exception:
                # Known exception to stop the multi-level iteration at once
                pass
        except Exception as e:
            business_attribute_data['message']['error'] += [e.args[0]]
            full_trace_error = traceback.format_exc()
            logger.error(full_trace_error)
        return business_attribute_data
