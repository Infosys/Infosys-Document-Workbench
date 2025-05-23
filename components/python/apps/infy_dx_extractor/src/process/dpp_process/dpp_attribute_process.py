# ===============================================================================================================#
# Copyright 2023 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

from common.ainauto_logger_factory import AinautoLoggerFactory
from common.app_const import ResProp, SessionKey, ValType
from common.app_session_store import AppSessionStore
from common.file_util import FileUtil
from common.response import Response
from process.dpp_process.dpp_attribute_process_base import DppAttributeProcessBase

logger = AinautoLoggerFactory().get_logger()
app_session_store = AppSessionStore()

DPP_READER_PROCESSOR_NAME = 'reader'


class DppAttributeProcess(DppAttributeProcessBase):

    def __init__(self, profile_data_dict, doc_data_dict) -> None:
        super().__init__(profile_data_dict, doc_data_dict)
        self.__doc_data_dict = doc_data_dict
        self.__profile_data_dict = profile_data_dict
        extractor_config_dict = app_session_store.get_data(
            SessionKey.EXT_CONFIG_FILE_DATA)
        self.__attr_ext_prov_config = extractor_config_dict.get(
            'attributeExternalProviders')

    def extract_attributes(self):
        attr_res_list = []
        for attr_provider_obj in self.__get_provider_to_attr_dict().values():
            provider_props = attr_provider_obj.get(
                'provider_data', {}).get('properties')
            _, proc_output_data = self.execute_pipeline(
                provider_props, self.get_req_file())
            proc_output_data = proc_output_data.get(
                'context_data', {}).get(DPP_READER_PROCESSOR_NAME)
            attr_res_list += self.__prepare_raw_attr_res(
                proc_output_data, attr_provider_obj)
        return attr_res_list

    def __get_provider_to_attr_dict(self):
        attr_external_provider_dict = {}
        for attr_obj in self.__profile_data_dict.get('attributes'):
            attribute_name = attr_obj.get('attributeName')
            attr_ext_pro_data = attr_obj.get('attributeExternalProvider')
            if attr_ext_pro_data and attr_ext_pro_data.get('enabled'):
                self.get_attr_ext_provider_data(
                    self.__attr_ext_prov_config, attr_ext_pro_data, attribute_name, attr_external_provider_dict)
        return attr_external_provider_dict

    def __prepare_raw_attr_res(self, proc_output_data, attr_provider_obj):
        attr_res_list = []

        def _raw_attr_response(attr_id, val, val_conf_pct, val_source=None):
            extraction_tech = {'dpp_processor': {
                ResProp.CONF: 80, ResProp.SLCTD: True}}
            val_source = val_source if val_source and isinstance(
                val_source, dict) else {}
            source_metadata=[x for x in output_data.get('source_metadata', []) if x.get('chunk_id') == val_source.get('chunk_id', '')]
            source_metadata=source_metadata[0] if len(source_metadata)>0 else {}            
            return Response.val_structure(
                val_id=FileUtil.get_attr_val_id(attr_id), type_obj=ValType.TXT,
                val=val, confidence=val_conf_pct, extraction_technique=extraction_tech,
                page=val_source.get('page_no'), bbox=source_metadata.get('bbox'))

        for output_data in proc_output_data.get('output', []):
            attr_id = FileUtil.get_attr_id(self.__doc_data_dict['doc_id'])
            found_attr_name_list = [x for x in attr_provider_obj.get(
                'attributes') if x.get('attribute_key') == output_data.get('attribute_key')]
            if not found_attr_name_list:
                continue

            model_output = output_data.get('model_output')
            val_conf_pct = -1
            attribute_name = found_attr_name_list[0]['attribute_name']

            if isinstance(model_output, dict):
                val = model_output.get('answer')
                val_conf_pct = model_output.get('confidence_pct', -1)
                extracted_attr = []
                sources = model_output.get('sources', [])
                if not sources:
                    extracted_attr=_raw_attr_response(attr_id, val, val_conf_pct)
                    
                for val_source in sources:
                    extracted_attr += _raw_attr_response(
                        attr_id, val, val_conf_pct, val_source)
                attr_res_list.append(Response.response(
                    atr_id=attr_id, attr_name=attribute_name, vals=extracted_attr))
            else:
                val = model_output
                extracted_attr=_raw_attr_response(
                    attr_id, val, val_conf_pct)
                attr_res_list.append(Response.response(
                    atr_id=attr_id, attr_name=attribute_name, vals=extracted_attr))
        return attr_res_list
