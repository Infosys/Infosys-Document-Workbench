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


class RuleSysDeduplicateValues(RuleAttributeBaseClass):
    def __init__(self, config_params_dict: dict) -> None:
        super().__init__(config_params_dict)

    def extract(self, raw_attribute_data_list: list, business_attribute_data: dict) -> BIZ_ATTRIBUTE_DATA_DICT:

        def _append_if_not_exist(item_list: list, item):
            #  06/06/2023: Below fix to include empty value fields
            # if item and str(item).strip():
            if item is not None:
                item_list_lower = [str(v).lower() for v in item_list]
                if not str(item).lower() in item_list_lower:
                    item_list.append(item)
                    return True
            return False

        def _get_text_object_list_val():
            for value_obj in attr_val_type_obj:
                attr_val_id = value_obj['id']
                _append_if_not_exist(attr_val_ids_participated, attr_val_id)
                if value_obj.get('state', "") == "":
                    text_val = value_obj['text']
                else:
                    text_val = f"{value_obj['text']}-{value_obj['state']}" if value_obj['text'] else f"{value_obj['state']}"
                is_updated = _append_if_not_exist(
                    temp_attribute_values, text_val)
                if is_updated:
                    _append_if_not_exist(attr_val_ids_selected, attr_val_id)
                #  : 11/14/2023: consider attribute id as sourced even if duplicate value is found
                # That is because attribute value can be found from multiple source.
                # e.g LLM gives single value with multiple
                _append_if_not_exist(attr_val_ids_sourced, attr_val_id)
        #

        def _get_text_confidence_val():
            avg_confidencePct = -1
            confidencePct = [value_obj.get('confidencePct')
                             for value_obj in attr_val_type_obj if value_obj.get('confidencePct')]
            if len(confidencePct) > 0:
                avg_confidencePct = round(
                    sum(confidencePct)/len(confidencePct), 2)
            return avg_confidencePct
            #
        try:
            temp_attribute_values, attr_val_ids_selected, attr_val_ids_participated = [], [], []
            attr_val_ids_sourced = []
            avg_confidence_val = -1
            for doc_attribute_data in raw_attribute_data_list:
                for attr_val_dict in doc_attribute_data['values']:
                    attr_val_type_obj = attr_val_dict.get(
                        attr_val_dict.get('type'))
                    if isinstance(attr_val_type_obj, list):
                        _get_text_object_list_val()
                        avg_confidence_val = _get_text_confidence_val()
            business_attribute_data['confidence_pct'] = avg_confidence_val
            business_attribute_data['attribute_value_ids']['selected'] += attr_val_ids_selected
            business_attribute_data['attribute_value_ids']['participated'] += attr_val_ids_participated
            business_attribute_data['attribute_value_ids']['sourced'] += attr_val_ids_sourced
        except Exception as e:
            business_attribute_data['message']['error'] += [e.args[0]]
            full_trace_error = traceback.format_exc()
            logger.error(full_trace_error)

        return business_attribute_data
