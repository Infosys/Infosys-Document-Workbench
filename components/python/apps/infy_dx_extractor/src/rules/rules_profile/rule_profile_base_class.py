# ===============================================================================================================#
# Copyright 2020 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

import copy
from abc import ABC, abstractmethod
import re

from common.ainauto_logger_factory import AinautoLoggerFactory

logger = AinautoLoggerFactory().get_logger()
MATCHED_PROFILE_DICT: dict = {
    "selected": False,
    "profileDict": None,
    "confidencePct": None,
    "message": {
        "error": [],
        "warning": [],
        "info": []
    },
    "debugInfo": []
}


class RuleProfileBaseClass(ABC):
    """
    The Abstract Class defines a template method that contains a skeleton of
    some algorithm, composed of calls to (usually) abstract primitive
    operations.

    Concrete subclasses should implement these operations, but leave the
    template method itself intact.
    """

    def __init__(self) -> None:
        super().__init__()
        self._current_profile_name = None

    # ------------------------template_method---------------------------
    def template_method(self, doc_data_dict: dict, extractor_config_dict: dict, master_config: dict,
                        ocr_parser_obj: object, group_records):
        """
        The template method defines the skeleton of an algorithm.
        """
        self.pre_hook_doc_ocr_parser_obj(ocr_parser_obj)
        self.pre_hook_master_config(master_config)
        self.pre_hook_group_records(group_records)

        profile_input = extractor_config_dict['profileMatchRule']['ruleInput']
        matched_by_name = (
            extractor_config_dict['profile'] == master_config['extractor']['rd_profile_name'])
        matched_by_default_def = (
            len(profile_input['profileMatchDefinitions']) == 0)
        result_data = copy.deepcopy(MATCHED_PROFILE_DICT)
        # ----- Scenario 1 - Profile match with the exact profile name given or by default profile.
        if matched_by_name or matched_by_default_def:
            result_data['selected'] = True
            result_data['profileDict'] = extractor_config_dict
            result_data['confidencePct'] = 90
        else:
            match_def_value_dict, debug_info_dict = self.extract(copy.deepcopy(
                doc_data_dict), copy.deepcopy(extractor_config_dict))
            is_selected, conf = self.__apply_condition(
                profile_input, match_def_value_dict)
            if is_selected:
                result_data['selected'] = True
                result_data['profileDict'] = extractor_config_dict
                result_data['confidencePct'] = conf
                result_data['debugInfo'] += [debug_info_dict]
        result_dict = {
            "input": {'profile': extractor_config_dict.get('profile')},
            "output": result_data,
        }
        return result_dict

    # ------------------------abstractmethod---------------------------
    # These operations have to be implemented in subclasses.
    @abstractmethod
    def extract(self, doc_data_dict: dict, extractor_config_dict: dict) -> tuple:
        raise NotImplementedError

    # ------------------------hooks---------------------------
    # These are "hooks." Subclasses may override them.
    def pre_hook_doc_ocr_parser_obj(self, ocr_parser_obj):
        pass

    def pre_hook_master_config(self, master_config):
        pass

    def pre_hook_group_records(self, group_records):
        pass

    # ------------------------private methods---------------------------
    def __apply_condition(self, profile_input, match_def_value_dict):
        conditions = profile_input.get('conditions', {})
        final_result, conf_list = True if conditions else False, []
        for op in list(conditions):
            result, conf = self.__get_result_for(
                conditions, match_def_value_dict, op)
            if conf > 0:
                conf_list.append(conf)
            final_result = (final_result and result)
        final_conf = sum(conf_list)/len(conf_list) if conf_list else 0
        final_conf = round(final_conf, 2)
        return final_result, final_conf

    def __get_result_for(self, conditions, match_def_value_dict, op_type):
        result_list = []
        default_mat_def_list = [{"text": [""], "confidencePct": 100.0}]
        try:
            for rd_name, match_def_val_list in match_def_value_dict.items():
                # Set default value
                if not match_def_val_list:
                    match_def_val_list = default_mat_def_list
                result_list_temp = []
                is_all_matched = True
                for mat_def_res_dict in match_def_val_list:
                    filter_cond = [x for x in conditions.get(
                        op_type, {}) if x.get('name') == rd_name]
                    match_count = 0
                    for op_data in filter_cond:
                        try:
                            match_str = " ".join(mat_def_res_dict.get('text')) if isinstance(
                                mat_def_res_dict.get('text'), list) else mat_def_res_dict.get('text')
                            is_matched = re.search(
                                op_data['expectedValue'], match_str, re.IGNORECASE)
                        except Exception as e:
                            raise Exception(
                                f"Please configure profile name `{self._current_profile_name}` property of `conditions` properly for {rd_name}; {e.args}")
                        if not is_matched:
                            is_all_matched = False
                            continue
                        match_count += 1
                        if op_type == 'or':
                            result_list.append(
                                {'conf': mat_def_res_dict.get('confidencePct', 0), 'result': True})
                            raise RuntimeError
                        result_list_temp.append(
                            {'conf': mat_def_res_dict.get('confidencePct', 0), 'result': True})
                    if match_count == len(filter_cond):
                        break
                if op_type == 'and' and is_all_matched:
                    # If `and` condition, then all of it's given pattern should match to one text.
                    result_list += result_list_temp
        except RuntimeError:
            pass

        if op_type == 'or' and result_list:
            return result_list[0]['result'], int(result_list[0]['conf'])
        if op_type == 'and' and result_list and len(conditions.get(op_type, {})) == len(result_list):
            avg_conf = sum([int(x.get('conf', 0))
                            for x in result_list])/len(result_list)
            return True, avg_conf
        return False, 0
