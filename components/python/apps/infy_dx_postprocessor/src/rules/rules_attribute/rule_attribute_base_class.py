# ===============================================================================================================#
# Copyright 2022 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

import copy
from abc import ABC, abstractmethod
from common.logger_factory import LoggerFactory

logger = LoggerFactory().get_logger()
BIZ_ATTRIBUTE_DATA_DICT: dict = {
    "attribute_name": "",
    "docwb_attribute_name_cde": 44,
    "docwb_attribute_type_cde": 1,
    "docwb_extract_type_cde": 1,
    "attribute_values": [],
    "confidence_pct": 0,
    "attribute_value_ids": {
        "participated": [],
        "sourced": [],
        "selected": []
    },
    "message": {
        "error": [],
        "warning": [],
        "info": []
    }
}


class RuleAttributeBaseClass(ABC):
    """
    The Abstract Class defines a template method that contains a skeleton of
    some algorithm, composed of calls to (usually) abstract primitive
    operations.

    Concrete subclasses should implement these operations, but leave the
    template method itself intact.
    """

    def __init__(self, config_params_dict: dict) -> None:
        super().__init__()

    # ------------------------template_method---------------------------
    def template_method(self, business_attribute_name: str, doc_data_dict: dict,
                        business_attribute_data_list: list, extracted_attribute_names: list,
                        rule_execution_history: list, group_records):
        """
        The template method defines the skeleton of an algorithm.
        """
        self.pre_hook_doc_data(doc_data_dict)
        self.pre_hook_business_attribute_data_list(
            business_attribute_data_list)
        # self.pre_hook_executed_rule_list(executed_rule_list)
        self.pre_hook_executed_rule_message(
            self.__filter_executed_rule_message(rule_execution_history))
        self.pre_hook_group_records(group_records)
        business_attribute_data = self.__filter_business_attribute_data(
            business_attribute_data_list, business_attribute_name)
        raw_attribute_data_list = self.__filter_doc_attribute_data(
            doc_data_dict, extracted_attribute_names)
        # executes rule implemented logic here
        # Commenting empty check to execute the rules even when raw_attribute_names is empty.
        # For example, for document type the raw_attribute_names can be empty but the 'Unknow' type will set from custom rule.
        # attr_data = None
        # if raw_attribute_data_list:
        attr_data = self.extract(
            copy.deepcopy(raw_attribute_data_list), copy.deepcopy(business_attribute_data))
        result = {
            "input": business_attribute_data,
            "output": attr_data,
        }
        return result

    # ------------------------abstractmethod---------------------------
    # These operations have to be implemented in subclasses.
    @abstractmethod
    def extract(self, raw_attribute_data_list: list,
                business_attribute_data: dict) -> BIZ_ATTRIBUTE_DATA_DICT:
        raise NotImplementedError

    # ------------------------hooks---------------------------
    # These are "hooks." Subclasses may override them.
    def pre_hook_doc_data(self, doc_data_dict):
        pass
        # logger.debug(f"doc_data - {doc_data_dict}")

    def pre_hook_business_attribute_data_list(self, business_attribute_data_list):
        pass
        # logger.debug(
        # f"business_attribute_data_list - {business_attribute_data_list}")

    def pre_hook_executed_rule_message(self, executed_rule_message):
        pass
        # logger.debug(f"executed_rule_list - {executed_rule_message}")

    def pre_hook_group_records(self, group_records):
        pass

    # ------------------------private methods---------------------------
    def __filter_business_attribute_data(self, business_attribute_data_list, business_attribute_name):
        business_attribute_data = next(
            business_attribute_data for business_attribute_data in business_attribute_data_list if business_attribute_name == business_attribute_data['attribute_name']
        )
        return business_attribute_data

    def __filter_doc_attribute_data(self, doc_data_dict, extracted_attribute_names):
        doc_attr_data_list = [attr_data_dict for attr_data_dict in doc_data_dict['raw_attributes']
                              if attr_data_dict['name'] in extracted_attribute_names]
        return doc_attr_data_list

    def __filter_executed_rule_message(self, rule_execution_history: dict):
        new_rule_msg_obj = {}
        for k, v in rule_execution_history.items():
            new_rule_msg_obj[k] = v.get('message', {})
        return new_rule_msg_obj
