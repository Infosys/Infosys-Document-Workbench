# ===============================================================================================================#
# Copyright 2020 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

import copy
from abc import ABC, abstractmethod
from typing import List

from common.ainauto_logger_factory import AinautoLoggerFactory

logger = AinautoLoggerFactory().get_logger()
RES_VALUE_DICT = {"text": "", "confidencePct": None}
RESPONSE_DICT: dict = {
    "values": [RES_VALUE_DICT],
    "message": {
        "error": [],
        "warning": [],
        "info": []
    }
}


class RulePropertyBaseClass(ABC):
    """
    The Abstract Class defines a template method that contains a skeleton of
    some algorithm, composed of calls to (usually) abstract primitive
    operations.

    Concrete subclasses should implement these operations, but leave the
    template method itself intact.
    """

    def __init__(self) -> None:
        super().__init__()

    # ------------------------template_method---------------------------
    def template_method(self, doc_data_dict: dict, master_config: dict, ocr_parser_obj: object,
                        group_records: List = None, rule_config_input: dict = None):
        """
        The template method defines the skeleton of an algorithm.
        """
        self.pre_hook_doc_ocr_parser_obj(ocr_parser_obj)
        self.pre_hook_master_config(copy.deepcopy(master_config))
        self.pre_hook_group_records(copy.deepcopy(
            group_records) if group_records else None)
        self.pre_hook_rule_config_input(copy.deepcopy(
            rule_config_input) if rule_config_input else None)
        result_data = self.extract(copy.deepcopy(doc_data_dict))
        result_dict = {
            "input": f"<dict record of doc_id:{doc_data_dict.get('doc_id')}>",
            "output": result_data,
        }
        return result_dict

    # ------------------------abstractmethod---------------------------
    # These operations have to be implemented in subclasses.
    @abstractmethod
    def extract(self, doc_data_dict: dict) -> RESPONSE_DICT:
        raise NotImplementedError

    # ------------------------hooks---------------------------
    # These are "hooks." Subclasses may override them.
    def pre_hook_doc_ocr_parser_obj(self, ocr_parser_obj):
        pass

    def pre_hook_master_config(self, master_config):
        pass

    def pre_hook_rule_config_input(self, rule_config_input: dict):
        pass

    def pre_hook_group_records(self, group_records: List):
        pass
