# ===============================================================================================================#
# Copyright 2023 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

import copy

from common.ainauto_logger_factory import AinautoLoggerFactory
from common.app_const import SessionKey, ValType
from common.app_session_store import AppSessionStore
from common.common_util import CommonUtil, Singleton
from common.file_util import FileUtil
from common.response import Response
from rules.rules_property.rule_property_base_class import RulePropertyBaseClass

logger = AinautoLoggerFactory().get_logger()
app_session_store = AppSessionStore()


class FileTemplateProcess(metaclass=Singleton):
    def __init__(self) -> None:
        self._master_config = app_session_store.get_data(
            SessionKey.MASTER_CONFIG_FILE_DATA)

    def extract_attributes(self, doc_data_dict, ocr_parser_obj, file_templ_obj, extract_props_obj=None,
                           group_records=None, fn_callback=None):
        extracted_attr_list = []
        for attr_config in file_templ_obj.get('attributes'):
            extracted_attr_list += self.manage_attr_extraction(
                attr_config, doc_data_dict, ocr_parser_obj, extract_props_obj=extract_props_obj,
                group_records=group_records, fn_callback=fn_callback)
        return extracted_attr_list

    def manage_attr_extraction(self, attr_config, doc_data_dict, ocr_parser_obj, group_records=None,
                               extract_props_obj=None, fn_callback=None):
        extracted_attr_list = []
        attr_rule_config = attr_config.get('attributeRules', {})
        attr_def_config = attr_config.get('attributeDefinitions', [])
        if attr_rule_config and attr_rule_config.get('enabled'):
            result = self.__execute_rule(
                attr_rule_config, doc_data_dict, ocr_parser_obj, group_records)
            extracted_attr_list.append(self.__prepare_raw_attr_res(
                attr_config, doc_data_dict, result))
        elif attr_def_config and fn_callback:
            # record, group_records, ocr_parser_obj, selected_profile, out_location,file_template_process_obj: FileTemplateProcess
            rd_attr = {
                "attributes": [attr_config]
            }
            extracted_attr_list += fn_callback(doc_data_dict, group_records,
                                               ocr_parser_obj, rd_attr, extract_props_obj.output_dir)
        return extracted_attr_list

    def __prepare_raw_attr_res(self, attr_config, doc_data_dict, result):
        attr_id = FileUtil.get_attr_id(doc_data_dict['doc_id'])
        extracted_attr_list = []
        if not result.get('message', {}).get('error'):
            for val_data in result.get('output').get('values'):
                extracted_attr = Response.val_structure(
                    val_id=FileUtil.get_attr_val_id(attr_id), type_obj=ValType.TXT,
                    val=val_data.get('text'), confidence=val_data.get('confidencePct'))
                extracted_attr_list += extracted_attr
        return Response.response(
            atr_id=attr_id, attr_name=attr_config['attributeName'],
            vals=extracted_attr_list,
            error=result.get('message', {}).get('error'),
            warn=result.get('message', {}).get('warning'),
            info=result.get('message', {}).get('info'))

    def __execute_rule(self, rule_data_config, doc_data_dict, ocr_parser_obj, group_records):
        rule_name = rule_data_config.get('name')
        rule_class = CommonUtil.get_rule_class_instance(
            rule_name, rc_entity_name='rules_property')
        rule_instance: RulePropertyBaseClass = rule_class()
        rule_result = rule_instance.template_method(
            copy.deepcopy(doc_data_dict), copy.deepcopy(self._master_config),
            ocr_parser_obj, copy.deepcopy(group_records), rule_data_config.get('input'))
        return rule_result
