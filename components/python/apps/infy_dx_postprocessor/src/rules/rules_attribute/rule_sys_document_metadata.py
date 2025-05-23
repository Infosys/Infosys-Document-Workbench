# ===============================================================================================================#
# Copyright 2020 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

import copy
import traceback
import os

from common.logger_factory import LoggerFactory
from rules.rules_attribute.rule_attribute_base_class import (
    BIZ_ATTRIBUTE_DATA_DICT, RuleAttributeBaseClass)

import infy_common_utils.format_converter as format_converter
from infy_common_utils.format_converter import FormatConverter, ConvertAction

format_converter.format_converter_jar_home = os.environ['FORMAT_CONVERTER_HOME']

logger = LoggerFactory().get_logger()


class RuleSysDocumentMetadata(RuleAttributeBaseClass):
    """Rule to extract the DOCWB File Receive Date"""

    def __init__(self, config_params_dict: dict) -> None:
        super().__init__(config_params_dict)
        self.__doc_data_dict = {}

    def pre_hook_doc_data(self, doc_data_dict):
        self.__doc_data_dict = doc_data_dict

    def extract(self, raw_attribute_data_list: list, business_attribute_data: dict) -> BIZ_ATTRIBUTE_DATA_DICT:
        def _get_pdf_char_count(file_path):
            if not os.path.splitext(file_path)[1].lower() == '.pdf':
                raise ValueError('Only PDF files supported')
            config_param_dict = {
                "pages": [1]
            }
            result = FormatConverter.execute(
                file_path, convert_action=ConvertAction.PDF_TO_TXT,
                config_param_dict=config_param_dict)
            return len(result)
        try:
            new_business_attribute_data = copy.deepcopy(
                business_attribute_data)
            attachment_file_path = self.__doc_data_dict.get('doc_copy_path') if self.__doc_data_dict.get(
                'doc_copy_path', None) else self.__doc_data_dict.get('doc_original_path')
            if str(attachment_file_path).lower().endswith(".pdf"):
                content_len = _get_pdf_char_count(attachment_file_path)
                new_business_attribute_data['attribute_values'].append(
                    "PDF Native" if content_len > 25 else "PDF Scanned")
                new_business_attribute_data['confidence_pct'] = 80
        except Exception as e:
            new_business_attribute_data['message']['error'] += [e.args[0]]
            full_trace_error = traceback.format_exc()
            logger.error(full_trace_error)

        return new_business_attribute_data
