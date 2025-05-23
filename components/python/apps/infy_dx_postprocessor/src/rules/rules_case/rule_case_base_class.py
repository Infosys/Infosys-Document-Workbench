# ===============================================================================================================#
# Copyright 2022 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

import copy
from abc import ABC, abstractmethod

from common.logger_factory import LoggerFactory

logger = LoggerFactory().get_logger()
DOCWB_CONFIG_ATTRIBUTE_DATA_DICT: dict = {
    "add_business_attribute_annotation": True,
    "assign_user_login_id_to_queue": [],
    "assign_case_to_user_login_id": "",
    "update_case_to_for_your_review": False,
    "update_case_to_closed": False,
    "audit_data": [
        {
            "activity": "",
            "value": ""
        }
    ],
    "doc_queue_name": "",
    "doc_queue_name_cde": 0,
    "attachment_file_path": "",
    "message": {
        "error": [],
        "warning": [],
        "info": []
    }
}


class RuleCaseBaseClass(ABC):
    """
    The Abstract Class defines a template method that contains a skeleton of
    some algorithm, composed of calls to (usually) abstract primitive
    operations.

    Concrete subclasses should implement these operations, but leave the
    template method itself intact.
    """

    def __init__(self, master_config_dict: dict) -> None:
        super().__init__()

    # ------------------------template_method---------------------------
    def template_method(self, doc_data_dict: dict, docwb_config_attr_data_dict: dict, docwb_case_definition: dict, group_records):
        """
        The template method defines the skeleton of an algorithm.
        """
        self.pre_hook_doc_data(doc_data_dict)
        self.pre_hook_group_records(group_records)
        attr_data = self.extract(copy.deepcopy(
            docwb_config_attr_data_dict), copy.deepcopy(docwb_case_definition.get('default')))
        result = {
            "input": docwb_config_attr_data_dict,
            "output": attr_data,
        }
        return result

    # ------------------------abstractmethod---------------------------
    # These operations have to be implemented in subclasses.
    @abstractmethod
    def extract(self, docwb_config_attr_data: dict, docwb_case_definition_default_data: dict) -> DOCWB_CONFIG_ATTRIBUTE_DATA_DICT:
        raise NotImplementedError

    # ------------------------hooks---------------------------
    # These are "hooks." Subclasses may override them.
    def pre_hook_doc_data(self, doc_data_dict):
        pass
        # logger.debug(f"doc_data - {doc_data_dict}")

    def pre_hook_group_records(self, group_records):
        pass
