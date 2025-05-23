# ===============================================================================================================#
# Copyright 2023 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

from common.ainauto_logger_factory import AinautoLoggerFactory
from common.app_const import SessionKey
from common.app_session_store import AppSessionStore
from common.file_util import FileUtil
from process.dpp_process.dpp_attribute_process_base import DppAttributeProcessBase

logger = AinautoLoggerFactory().get_logger()
app_session_store = AppSessionStore()


class DppProfileAttributeProcess(DppAttributeProcessBase):
    def __init__(self, profile_data_dict, doc_data_dict) -> None:
        super().__init__(profile_data_dict, doc_data_dict)
        self.__profile_data_dict = profile_data_dict
        extractor_config_dict = app_session_store.get_data(
            SessionKey.EXT_CONFIG_FILE_DATA)
        self.__attr_ext_prov_config = extractor_config_dict.get(
            'attributeExternalProviders')

    def extract_attributes(self):
        attr_res_dict = {}

        def _get_llm_answer(proc_output_data, attr_provider_obj):
            new_attr_res_dict = {}
            for output_data in proc_output_data.get('output', []):
                found_attr_name_list = [x for x in attr_provider_obj.get(
                    'attributes') if x.get('attribute_key') == output_data.get('attribute_key')]
                if not found_attr_name_list:
                    continue
                val_conf_pct = -1
                model_output = output_data.get('model_output')
                attribute_name = found_attr_name_list[0]['attribute_name']
                val = ""
                if isinstance(model_output, dict):
                    val = model_output.get('answer')
                    val_conf_pct = model_output.get('confidence_pct', -1)
                else:
                    val = model_output
                if new_attr_res_dict.get(attribute_name):
                    new_attr_res_dict[attribute_name].append(
                        {"text": [val], "confidencePct": val_conf_pct})
                else:
                    new_attr_res_dict[attribute_name] = [
                        {"text": [val], "confidencePct": val_conf_pct}]
            return new_attr_res_dict

        for attr_provider_obj in self.__get_provider_to_attr_dict().values():
            provider_props = attr_provider_obj.get(
                'provider_data', {}).get('properties')
            processor_exec_list, proc_output_data = self.execute_pipeline(
                provider_props, self.get_req_file())
            processor_name = f"reader_{provider_props.get('pipeline_name')}"
            proc_output_data = proc_output_data.get(
                'context_data', {}).get(processor_name)
            if not proc_output_data:
                raise Exception(
                    f"DPP processor(s) is not executed successfully. Because, not found the processor {processor_name} context data in above printed document_data.json file.")
            attr_res_dict = _get_llm_answer(
                proc_output_data, attr_provider_obj)
        return attr_res_dict

    def __get_provider_to_attr_dict(self):
        attr_external_provider_dict = {}
        profile = self.__profile_data_dict
        for attr_obj in profile['profileMatchDefinitions']:
            attribute_name = attr_obj.get('name')
            attr_ext_pro_data = attr_obj.get(
                'profileMatchAttributeExternalProvider')
            if attr_ext_pro_data:
                self.get_attr_ext_provider_data(
                    self.__attr_ext_prov_config, attr_ext_pro_data, attribute_name, attr_external_provider_dict)
        return attr_external_provider_dict
