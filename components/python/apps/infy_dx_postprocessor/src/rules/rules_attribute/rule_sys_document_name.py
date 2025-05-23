# ===============================================================================================================#
# Copyright 2020 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

import copy
import traceback

from common.logger_factory import LoggerFactory
from rules.rules_attribute.rule_attribute_base_class import (
    BIZ_ATTRIBUTE_DATA_DICT, RuleAttributeBaseClass)

logger = LoggerFactory().get_logger()


class RuleSysDocumentName(RuleAttributeBaseClass):
    """Rule to extract the DOCWB File Receive Date"""

    def __init__(self, config_params_dict: dict) -> None:
        super().__init__(config_params_dict)
        self.__doc_data_dict = {}

    def pre_hook_doc_data(self, doc_data_dict):
        self.__doc_data_dict = doc_data_dict

    def extract(self, raw_attribute_data_list: list, business_attribute_data: dict) -> BIZ_ATTRIBUTE_DATA_DICT:
        try:
            new_business_attribute_data = copy.deepcopy(
                business_attribute_data)
            new_business_attribute_data['attribute_values'].append(
                self.__doc_data_dict['doc_name'])
            new_business_attribute_data['confidence_pct'] = 100
        except Exception as e:
            new_business_attribute_data['message']['error'] += [e.args[0]]
            full_trace_error = traceback.format_exc()
            logger.error(full_trace_error)

        return new_business_attribute_data
