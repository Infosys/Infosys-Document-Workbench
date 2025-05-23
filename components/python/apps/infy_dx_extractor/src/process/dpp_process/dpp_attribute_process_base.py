# ===============================================================================================================#
# Copyright 2023 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

from abc import abstractmethod
import os
import time
from common.file_util import FileUtil
from common.ainauto_logger_factory import AinautoLoggerFactory
from common.app_config_manager import AppConfigManager
from common.app_const import SessionKey
from common.app_const import *
from common.app_session_store import AppSessionStore
from service.dpp_pipeline_executor_service import DppPipelineExecutorService
import infy_dpp_sdk
import infy_fs_utils
config = AppConfigManager().get_app_config()
logger = AinautoLoggerFactory().get_logger()
app_session_store = AppSessionStore()


class DppAttributeProcessBase():

    def __init__(self, profile_data_dict, doc_data_dict) -> None:
        self.__doc_data_dict = doc_data_dict
        self.__profile_data_dict = profile_data_dict

    @abstractmethod
    def extract_attributes(self):
        raise NotImplementedError

    def get_req_file(self):
        master_config = app_session_store.get_data(
            SessionKey.MASTER_CONFIG_FILE_DATA)
        json_file_path = f"{master_config['extractor'].get('temp_folder_path')}/{FileUtil.get_uuid()}_request_data.json"
        logger.info(f"Request file path - {json_file_path}")
        data_root_path = master_config['default'].get('data_root_path')
        content = {"working_file_path_list": [os.path.dirname(self.__doc_data_dict.get(
            'doc_work_location')).replace(data_root_path, "")]}
        FileUtil.write_to_json(content, json_file_path)
        rel_path = json_file_path.replace(data_root_path, "")
        return rel_path

    def __initialize_and_run_pipeline(self, processor_input_config, request_file_path, provider_props):
        storage_root_uri = processor_input_config['variables']['DPP_STORAGE_URI']
        storage_config_data = infy_fs_utils.data.StorageConfigData(
            **{
                "storage_root_uri": storage_root_uri,
                "storage_server_url": "",
                "storage_access_key": "",
                "storage_secret_key": ""
            })
        file_sys_handler = infy_fs_utils.provider.FileSystemHandler(
            storage_config_data)

        infy_fs_utils.manager.FileSystemManager().add_fs_handler(file_sys_handler,
                                                                 infy_dpp_sdk.common.Constants.FSH_DPP)

        temp_storage_root_uri = storage_root_uri.replace('file://', '')
        # Configure client properties
        client_config_data = infy_dpp_sdk.ClientConfigData(
            **{
                "container_data": {
                    "container_root_path": f"{temp_storage_root_uri}/{config['CONTAINER']['CONTAINER_ROOT_PATH']}".replace('/./', '/'),
                }
            })
        infy_dpp_sdk.ClientConfigManager().load(client_config_data)

        # Setting loging details
        logging_config_data = infy_fs_utils.data.LoggingConfigData(
            **{
                # "logger_group_name": "my_group_1",
                "logging_level": f'{config[L_DEFAULT]["logging_level"]}',
                "logging_format": "",
                "logging_timestamp_format": "",
                "log_file_data": {
                    "log_file_dir_path": "/logs",
                    "log_file_name_prefix": f'{config[L_DEFAULT]["service_name"]}',
                    # "log_file_name_suffix": "1",
                    "log_file_extension": ".log"

                }})
        infy_fs_utils.manager.FileSystemLoggingManager().add_fs_logging_handler(
            infy_fs_utils.provider.FileSystemLoggingHandler(
                logging_config_data, file_sys_handler),
            infy_dpp_sdk.common.Constants.FSLH_DPP)

        # processor_input_config_path = provider_props.get(
        #     'processor_input_config_path').replace(temp_storage_root_uri, '')
        processor_input_config_path = os.path.relpath(provider_props.get(
            'processor_input_config_path'), temp_storage_root_uri)
        print("processor_input_config_path::", processor_input_config_path)
        dpp_orchestrator = infy_dpp_sdk.orchestrator.OrchestratorNative(
            input_config_file_path=processor_input_config_path)
        response_data_list = dpp_orchestrator.run_batch(context_data={'docwb_extractor': {
            'request_file_path': request_file_path}})

        infy_fs_utils.manager.FileSystemManager().delete_fs_handler(
            infy_dpp_sdk.common.Constants.FSH_DPP)
        infy_fs_utils.manager.FileSystemLoggingManager().delete_fs_logging_handler(
            infy_dpp_sdk.common.Constants.FSLH_DPP)
        # remove the temporary container
        FileUtil.remove_dir(
            f"{temp_storage_root_uri}/{config['CONTAINER']['CONTAINER_ROOT_PATH']}".replace('/./', '/'))

    def execute_pipeline(self, provider_props, request_file_path):
        logger.info(
            f"[Running] - Pipeline Name - {provider_props.get('pipeline_name')}")
        start_time = time.time()
        #  Start
        processor_input_config = FileUtil.load_json(
            provider_props.get('processor_input_config_path'))
        try:
            self.__initialize_and_run_pipeline(
                processor_input_config, request_file_path, provider_props)
        except Exception as e:
            logger.error(
                f"Error while initializing configuration for pipeline - {provider_props.get('pipeline_name')}")
            raise e
        processor_exec_list = [x['processor_name']
                               for x in processor_input_config['processor_list'] if x['enabled']]
        # End
        proc_output_file_path = f"{self.__doc_data_dict.get('doc_work_location')}/processor_response_data.json"
        logger.info(
            f"[End] - Pipeline Name - {provider_props.get('pipeline_name')} execution elapse time is {round((time.time() - start_time)/60, 4)} mins")
        logger.info(f"DPP output file - {proc_output_file_path}")
        logger.info(f"List of executed Processor(s) - {processor_exec_list}")

        if not os.path.exists(proc_output_file_path):
            raise FileNotFoundError(proc_output_file_path)
        proc_output_data = FileUtil.load_json(proc_output_file_path)
        return processor_exec_list, proc_output_data

    def get_attr_ext_provider_data(self, ext_provider_list, attr_ext_pro_data, attribute_name, resp_dict):
        for x in ext_provider_list:
            match_provider = (x.get('provider_name') ==
                              attr_ext_pro_data.get('provider_name'))
            match_pipeline = (x.get('properties').get(
                'pipeline_name') == attr_ext_pro_data.get('properties').get('pipeline_name'))
            if match_provider and match_pipeline:
                temp_key = f"{x.get('provider_name')}_{x.get('properties').get('pipeline_name')}"
                temp_prov_data = resp_dict.get(
                    temp_key)
                temp_attr_data = {'attribute_name': attribute_name,
                                  'attribute_key': attr_ext_pro_data.get('properties').get('attribute_key')}
                if temp_prov_data:
                    temp_prov_data.get(
                        'attributes').append(temp_attr_data)
                else:
                    resp_dict[temp_key] = {'attributes': [
                        temp_attr_data], 'provider_data': x}
                break
        return resp_dict
