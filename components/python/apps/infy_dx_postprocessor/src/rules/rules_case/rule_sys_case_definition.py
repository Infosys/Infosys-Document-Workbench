# ===============================================================================================================#
# Copyright 2020 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

import copy
import traceback
import json
import os

from common.logger_factory import LoggerFactory
from rules.rules_case.rule_case_base_class import (
    DOCWB_CONFIG_ATTRIBUTE_DATA_DICT, RuleCaseBaseClass)

logger = LoggerFactory().get_logger()

RAW_ATTR_NAME_IS_PRIMARY_DOC = "is_primary_document"
RAW_ATTR_NAME_IS_EMAIL = "is_email"
RAW_ATTR_NAME_RELATED_CASE_NUM = "related_case_num"


class RuleSysCaseDefinition(RuleCaseBaseClass):
    def __init__(self, master_config_dict: dict) -> None:
        super().__init__(master_config_dict)
        self.__master_config = master_config_dict
        self.__doc_data_dict = {}
        self.__group_records = None

    # ---- Prehook method - Enable to access record data ----
    def pre_hook_doc_data(self, doc_data_dict):
        self.__doc_data_dict = doc_data_dict

    def pre_hook_group_records(self, group_records):
        self.__group_records = group_records

    def extract(self, docwb_config_attr_data: dict, docwb_case_definition_default_data: dict) -> DOCWB_CONFIG_ATTRIBUTE_DATA_DICT:
        try:
            # code here to override new_docwb_config_attr_data props based on business rules.
            new_docwb_config_attr_data = copy.deepcopy(
                docwb_config_attr_data)
            new_docwb_config_attr_data.update(
                docwb_case_definition_default_data)
            new_docwb_config_attr_data["attachment_file_path"] = self.__doc_data_dict.get('doc_copy_path') if self.__doc_data_dict.get(
                'doc_copy_path', None) else self.__doc_data_dict.get('doc_original_path')
            new_docwb_config_attr_data["attachment_file_paths"] = []
            new_docwb_config_attr_data[RAW_ATTR_NAME_IS_PRIMARY_DOC] = False

            is_email = False
            for item in self.__doc_data_dict.get("raw_attributes"):
                if item.get("name") == RAW_ATTR_NAME_IS_EMAIL:
                    if self.__get_raw_attr_value(item) == "true":
                        is_email = True
                elif item.get("name") == RAW_ATTR_NAME_RELATED_CASE_NUM:
                    new_docwb_config_attr_data[RAW_ATTR_NAME_RELATED_CASE_NUM] = self.__get_raw_attr_value(
                        item)

            new_docwb_config_attr_data[RAW_ATTR_NAME_IS_PRIMARY_DOC] = self.__get_group_primary_doc(
                self.__doc_data_dict.get("doc_id"))
            new_docwb_config_attr_data = {
                **new_docwb_config_attr_data, **self.__doc_data_dict.get('docwb_query_attribute')}

            if new_docwb_config_attr_data[RAW_ATTR_NAME_IS_PRIMARY_DOC] and is_email:
                if os.path.splitext(new_docwb_config_attr_data["attachment_file_path"])[1] == ".json":
                    with open(new_docwb_config_attr_data["attachment_file_path"]) as f:
                        data = json.load(f)
                        if data.get("email") and data["email"].get("body_txt"):
                            body_txt = data["email"]["body_txt"]
                    email_body_file_path = os.path.dirname(
                        self.__doc_data_dict.get('doc_copy_path'))+"/EmailBody.txt"
                    with open(email_body_file_path, "w") as f:
                        f.write(body_txt)
                        new_docwb_config_attr_data["attachment_file_paths"].append(
                            email_body_file_path)

            new_docwb_config_attr_data["attachment_file_paths"].append(
                new_docwb_config_attr_data["attachment_file_path"])

        except Exception as e:
            new_docwb_config_attr_data['message']['error'] += [e.args[0]]
            full_trace_error = traceback.format_exc()
            logger.error(full_trace_error)
        return new_docwb_config_attr_data

    def __get_raw_attr_value(self, item):
        values = item.get("values")
        if values and values[0].get("text_obj_list"):
            return values[0].get("text_obj_list")[0].get("text")
        return None

    def __get_group_primary_doc(self, current_doc_id):
        is_primary_doc = False
        primary_doc_list, email_doc_list = [], []
        for record in self.__group_records:
            primary_doc_list += [record.get('doc_id') for item in record.get("raw_attributes") if item.get(
                "name") == RAW_ATTR_NAME_IS_PRIMARY_DOC and self.__get_raw_attr_value(item) == "true"]
            email_doc_list += [record.get('doc_id') for item in record.get("raw_attributes") if item.get(
                "name") == RAW_ATTR_NAME_IS_EMAIL and self.__get_raw_attr_value(item) == "true"]

        if current_doc_id in email_doc_list and current_doc_id in primary_doc_list:
            is_primary_doc = True
        elif not email_doc_list and current_doc_id in primary_doc_list:
            is_primary_doc = True
        return is_primary_doc
