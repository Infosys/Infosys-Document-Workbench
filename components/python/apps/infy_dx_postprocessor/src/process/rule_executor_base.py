# ===============================================================================================================#
# Copyright 2022 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

import copy
import traceback

from common.common_util import CommonUtil
from common.logger_factory import LoggerFactory
from rules.rules_attribute.rule_attribute_base_class import (
    BIZ_ATTRIBUTE_DATA_DICT, RuleAttributeBaseClass)
from rules.rules_case.rule_case_base_class import (
    DOCWB_CONFIG_ATTRIBUTE_DATA_DICT, RuleCaseBaseClass)
from process.telemetry_process import TelemetryProcess, LogLevel
logger = LoggerFactory().get_logger()


class RuleExecutorBase:
    def __init__(self):
        self.__telemetry_process = None

    def execute(self, rule_to_attr_template_config: dict,
                doc_data_dict: dict, summary_dict: dict, telemetry_process_obj: TelemetryProcess,
                master_config: dict, group_records):
        try:
            self.__telemetry_process = telemetry_process_obj
            # ---------------------- Telemetry: START Event ----------------------
            telemetry_data = {'telemetry': doc_data_dict['telemetry']}
            self.__telemetry_process.post_telemetry_event_start(
                doc_data_dict['doc_batch_id'], doc_data_dict['doc_id'], telemetry_data)
            # ------------------ Match - Extraction Profile VS Rule Template Profile ------------------
            matched_rule_template_list = [template for template in rule_to_attr_template_config.get('documentTemplates') if (
                template.get('profile').strip().lower() == doc_data_dict.get('extraction_profile').strip().lower() and template.get('enabled'))]
            if not matched_rule_template_list:
                raise Exception(
                    f"IMPORTANT!!!. No Rule profile template configured/enabled for the document extraction profile name {doc_data_dict.get('extraction_profile')}")
            logger.info(
                f"Rule execution started for document extraction profile name {doc_data_dict.get('extraction_profile')}")
            matched_rule_template = matched_rule_template_list[0]

            # ------------------ Execute rule definition configured ------------------
            # For each record all the configured rules will be executed.
            # All rules will get previous rule extracted business data in read-only mode along with raw_attribute_data.
            final_biz_attr_data_list = self.__execute_rules_attribute(
                doc_data_dict, matched_rule_template.get('ruleDefinitions', []), group_records)
            # ---- consolidate all the rule execution response ----
            new_doc_data_dict = self.__consolidate_business_attributes(
                copy.deepcopy(doc_data_dict),
                final_biz_attr_data_list)

            # x = [item for item in group_records if new_doc_data_dict['doc_id'] == item['doc_id']]
            # update group records
            for pos, item in enumerate(group_records):
                if item['doc_id'] == new_doc_data_dict['doc_id']:
                    group_records[pos] = new_doc_data_dict

            # ------------------ Execute case definition configured ------------------
            new_doc_data_dict = self.__execute_rules_case(
                new_doc_data_dict, matched_rule_template.get('docwbCaseDefinition', {}), master_config, group_records)

            # ------------------ Generate batch summary log ------------------

            self.__update_summary(
                summary_dict, doc_data_dict['doc_id'], "Success", "post_processor")
            # ---------------------- Telemetry: :LOG Event ----------------------
            self.__telemetry_process.post_telemetry_event_log(
                doc_data_dict['doc_batch_id'], doc_data_dict['doc_id'], LogLevel.INFO, message="Success",
                additional_config_param=telemetry_data)
            # ---------------------- Telemetry: END Event ----------------------
            self.__telemetry_process.post_telemetry_event_end(
                doc_data_dict['doc_batch_id'], doc_data_dict['doc_id'], telemetry_data)
            return new_doc_data_dict, summary_dict
        except Exception:
            logger.error('Error occurred in record with doc_id =',
                         doc_data_dict['doc_id'])
            full_trace_error = traceback.format_exc()
            logger.error(full_trace_error)
            self.__update_summary(
                summary_dict, doc_data_dict['doc_id'], "Failed", "post_processor")
            # ---------------------- Telemetry: :LOG Event ----------------------
            self.__telemetry_process.post_telemetry_event_log(
                doc_data_dict['doc_batch_id'], doc_data_dict['doc_id'], LogLevel.ERROR, message="Failed",
                additional_config_param=telemetry_data)
            return None, summary_dict

    def __execute_rules_attribute(self, doc_data_dict: dict, rule_definition_list: list, group_records):
        def _execute_rule_attribute():
            rule_class = CommonUtil.get_rule_class_instance(
                rule_name, rc_entity_name='rules_attribute')
            rule_instance: RuleAttributeBaseClass = rule_class({})
            rule_result = rule_instance.template_method(
                rule_config_obj["business_attribute_name"],
                copy.deepcopy(doc_data_dict),
                copy.deepcopy(biz_attribute_data_list),
                copy.deepcopy(rule_config_obj["raw_attribute_names"]),
                copy.deepcopy(rules_execution_history),
                copy.deepcopy(group_records)
            )
            return rule_result

        def _update_rule_response():
            rule_response_output = rule_response.get('output', None)
            if not rule_response_output:
                return False
            # all rules may not fill all values hence update it to placeholder dict
            biz_attr_data.update(rule_response_output)
            biz_attr_data['message'] = BIZ_ATTRIBUTE_DATA_DICT['message']
            return True

        def _update_final_biz_attr_data_list():
            final_biz_attr_data = {}
            final_biz_attr_data.update(biz_attr_data)
            final_biz_attr_data['id'] = CommonUtil.get_attr_id(
                doc_data_dict['doc_id'])
            final_biz_attr_data['contributed_rules'] = contributed_rules
            final_biz_attr_data['rules_execution_history'] = rules_execution_history
            final_biz_attr_data_list.append(final_biz_attr_data)

        def _make_biz_attr_data_list():
            biz_attribute_data_list = []
            for rule_config_dict in rule_definition_list:
                if not rule_config_dict['enabled']:
                    continue
                attribute_data_dict = copy.deepcopy(
                    BIZ_ATTRIBUTE_DATA_DICT)
                attribute_data_dict['attribute_name'] = rule_config_dict['business_attribute_name']
                attribute_data_dict['docwb_attribute_name_cde'] = rule_config_dict.get(
                    'docwb_attribute_name_cde')
                attribute_data_dict['docwb_attribute_type_cde'] = rule_config_dict.get(
                    'docwb_attribute_type_cde')
                attribute_data_dict['docwb_extract_type_cde'] = rule_config_dict.get(
                    'docwb_extract_type_cde')
                biz_attribute_data_list.append(attribute_data_dict)
            return biz_attribute_data_list

        final_biz_attr_data_list = []
        biz_attribute_data_list = _make_biz_attr_data_list()
        for rule_config_obj in rule_definition_list:
            biz_attr_data = [attr_data for attr_data in biz_attribute_data_list if attr_data['attribute_name']
                             == rule_config_obj["business_attribute_name"]]
            if not biz_attr_data:
                continue
            biz_attr_data = biz_attr_data[0]
            contributed_rules, rules_execution_history = [], {}
            is_attribute_cancelled = False
            # For selected business attribute, loop through all configured rules
            for i, rule_name in enumerate(rule_config_obj["rule_names"]):
                try:
                    if not is_attribute_cancelled:
                        rule_response = _execute_rule_attribute()
                        if rule_response.get('output'):
                            is_attribute_cancelled = rule_response.get(
                                'output', {}).get('cancelled', False)
                        rules_execution_history[rule_name] = rule_response
                        is_updated = _update_rule_response()
                        if is_updated:
                            contributed_rules.append(rule_name)
                    if i == len(rule_config_obj["rule_names"])-1:
                        _update_final_biz_attr_data_list()
                except Exception:
                    full_trace_error = traceback.format_exc()
                    logger.error(full_trace_error)

        return final_biz_attr_data_list

    def __execute_rules_case(self, doc_data_dict: dict, docwb_case_definition: dict, master_config: dict, group_records):
        def _execute_rule_case():
            rule_class = CommonUtil.get_rule_class_instance(
                rule_name, rc_entity_name='rules_case')
            rule_instance: RuleCaseBaseClass = rule_class(master_config)
            rule_result = rule_instance.template_method(
                copy.deepcopy(doc_data_dict),
                copy.deepcopy(docwb_config_attr_data_dict),
                copy.deepcopy(docwb_case_definition),
                copy.deepcopy(group_records))

            return rule_result
        rule_name = docwb_case_definition.get('rule_name')

        def _update_rule_response():
            rule_response_output = rule_response.get('output', None)
            if not rule_response_output:
                return False
            # all rules may not fill all values hence update it to placeholder dict
            docwb_config_attr_data_dict.update(rule_response_output)
            _ = docwb_config_attr_data_dict.pop('message')
            return True

        docwb_config_attr_data_dict = copy.deepcopy(
            DOCWB_CONFIG_ATTRIBUTE_DATA_DICT)

        # Set property value to empty list as the default value
        docwb_config_attr_data_dict['audit_data'] = []

        rule_response = _execute_rule_case()
        _update_rule_response()
        rules_execution_history = {}
        rules_execution_history[rule_name] = rule_response
        docwb_config_attr_data_dict['rules_execution_history'] = rules_execution_history
        doc_data_dict['docwb_config_attribute'] = doc_data_dict.get(
            'docwb_config_attribute', {})
        doc_data_dict['docwb_config_attribute'].update(
            docwb_config_attr_data_dict)
        return doc_data_dict

    def __get_attr_src_data(self, attr_id: str, data: list):
        raw_attr_id = attr_id[0: attr_id.rfind("_")]
        raw_attr_data = [x for x in data["raw_attributes"]
                         if x["id"] == raw_attr_id][0]
        attr_val_data = None
        for valData in raw_attr_data["values"]:
            attr_val_data = [
                y for y in valData[valData["type"]] if y["id"] == attr_id]
            if attr_val_data:
                attr_val_data = attr_val_data[0]
                break
        return attr_val_data

    def __get_text_bbox_list(self, selected_ids: list, doc_data_dict: dict):
        text_bbox_list = []
        for x in selected_ids:
            try:
                attr_src_data = self.__get_attr_src_data(x, doc_data_dict)
                if attr_src_data:
                    text_bbox_list.append({
                        "text": attr_src_data.get("text"),
                        "page": attr_src_data.get("page"),
                        "source_bbox": attr_src_data.get("bbox"),
                        "page_bbox": doc_data_dict.get("image_bbox", {}).get(str(attr_src_data.get("page")))
                    })
            except Exception:
                full_trace_error = traceback.format_exc()
                logger.error(full_trace_error)

        return text_bbox_list

    def __consolidate_business_attributes(
            self, doc_data_dict: dict, biz_attribute_data_list: list):
        pp_attribute_list = []
        for rule_ext_data_dict in biz_attribute_data_list:
            attr_values = rule_ext_data_dict.get("attribute_values")
            attr_value_text = ''
            try:
                # Conside value as "[not found]" when actual value in document is empty/couldnt extract.
                attr_value_text = "\n----\n".join(
                    attr_values) if attr_values and "".join(attr_values) else "[not found]"
            except Exception:
                message = f"Error occurred while setting business attribute value. doc_id={doc_data_dict['doc_id']}"
                message += f", attr_name={rule_ext_data_dict.get('attribute_name')}, attr_value={attr_values}. "
                message += f"Default value has been set."
                logger.error(message)
                full_trace_error = traceback.format_exc()
                logger.error(full_trace_error)
            biz_attribute = {
                "id": rule_ext_data_dict.get("id"),
                "name": rule_ext_data_dict.get('attribute_name'),
                "docwb_attribute_name_cde": rule_ext_data_dict.get('docwb_attribute_name_cde'),
                "docwb_attribute_type_cde": rule_ext_data_dict.get('docwb_attribute_type_cde'),
                "docwb_extract_type_cde": rule_ext_data_dict.get('docwb_extract_type_cde'),
                "text": attr_value_text,
                "text_list": attr_values,
                "text_bbox_list": self.__get_text_bbox_list(rule_ext_data_dict.get(
                    "attribute_value_ids", {}).get("sourced"), doc_data_dict),
                "text_list_confidence_pct": rule_ext_data_dict.get("confidence_pct"),
                "cancelled": rule_ext_data_dict.get("cancelled", False),
                "selected_attribute_ids": rule_ext_data_dict.get("attribute_value_ids", {}).get("selected"),
                "participated_attribute_ids": rule_ext_data_dict.get("attribute_value_ids", {}).get("participated"),
                "contributed_rules": rule_ext_data_dict.get("contributed_rules"),
                "rules_execution_history": rule_ext_data_dict.get("rules_execution_history")
            }
            # Delete optional properties if they've default values
            OPTIONAL_PROP_AND_DEFAULT_VALUE_LIST = (("cancelled", False),)
            for key, value in OPTIONAL_PROP_AND_DEFAULT_VALUE_LIST:
                if biz_attribute.get(key) == value:
                    biz_attribute.pop(key)

            pp_attribute_list.append(biz_attribute)
        doc_data_dict['business_attributes'] = pp_attribute_list
        return doc_data_dict

    def __update_summary(self, summary_dict, doc_id, status, column_name):
        if summary_dict.get(column_name):
            summary_dict[column_name][doc_id] = status
        else:
            summary_dict[column_name] = {
                doc_id: status
            }
