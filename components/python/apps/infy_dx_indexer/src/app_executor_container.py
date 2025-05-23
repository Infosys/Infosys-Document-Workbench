# ===============================================================================================================#
# Copyright 2023 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

import argparse
import os
import infy_dpp_sdk
import infy_fs_utils
from common.app_config_manager import AppConfigManager
from common.file_util import FileUtil
from common.app_constants import *
from common.logger_factory import LoggerFactory

app_config = AppConfigManager().get_app_config()
logger = LoggerFactory().get_logger()


class AppExectutorContainer:
    def __init__(self) -> None:
        pass

    @classmethod
    def parser(cls):
        parser = argparse.ArgumentParser()
        parser.add_argument("--request_id",
                            default=None, required=True)
        parser.add_argument(
            "--master_config", default=None, required=True)
        parser.add_argument(
            "--preprocessor_outfile_path", default=None, required=True)
        args = parser.parse_args()
        return args

    @classmethod
    def get_input_params(cls, args):
        request_id = args.request_id
        master_config = args.master_config
        preprocessor_outfile_path = args.preprocessor_outfile_path
        if not (request_id or master_config or preprocessor_outfile_path):
            raise Exception(
                "Please provide `request_id`, 'preprocessor_outfile_path', 'master_config' argument. ")
        master_config = FileUtil.load_json(master_config)
        return request_id, master_config, preprocessor_outfile_path

    def __configure_storage_data(self, data_root_path):
        storage_config_data = infy_fs_utils.data.StorageConfigData(
            **{
                "storage_root_uri": f"file://{data_root_path}",
                "storage_server_url": "",
                "storage_access_key": "",
                "storage_secret_key": ""
            })
        # Configure client properties
        client_config_data = infy_dpp_sdk.ClientConfigData(
            **{
                "container_data": {
                    "container_root_path": f"{data_root_path}/{app_config[CONTAINER]['CONTAINER_ROOT_PATH']}",
                }
            })
        file_sys_handler = infy_fs_utils.provider.FileSystemHandler(
            storage_config_data)
        infy_fs_utils.manager.FileSystemManager().add_fs_handler(
            file_sys_handler,
            infy_dpp_sdk.common.Constants.FSH_DPP)

        infy_dpp_sdk.ClientConfigManager().load(client_config_data)

        logging_config_data = infy_fs_utils.data.LoggingConfigData(
            **{
                # "logger_group_name": "my_group_1",
                "logging_level": 10,
                "logging_format": "",
                "logging_timestamp_format": "",
                "log_file_data": {
                    "log_file_dir_path": "/logs",
                    "log_file_name_prefix": f'{app_config[DEFAULT][service_name]}',
                    # "log_file_name_suffix": "1",
                    "log_file_extension": ".log"

                }})
        infy_fs_utils.manager.FileSystemLoggingManager().add_fs_logging_handler(
            infy_fs_utils.provider.FileSystemLoggingHandler(
                logging_config_data, file_sys_handler),
            infy_dpp_sdk.common.Constants.FSLH_DPP)

    def __do_orchestration(self, request_file_path,
                           input_config_file_path, storage_root_path, previous_run_file=''):
        err_msg_str = ''
        try:
            self.__configure_storage_data(storage_root_path)
        except Exception as e:
            err_msg_str = f"Error in __configure_storage_data: {e}"
            logger.info(f"Error in __configure_storage_data: {e}")
            return '', err_msg_str
        try:
            dpp_orchestrator = infy_dpp_sdk.orchestrator.OrchestratorNative(
                input_config_file_path=input_config_file_path)
            response_data_list = dpp_orchestrator.run_batch(context_data={'docwb_preprocessor': {
                'request_file_path': request_file_path}})
        except Exception as e:
            err_msg_str = f"Error in __do_orchestration run batch: {e}"
            logger.info(f"Error in __do_orchestration: {e}")
            return '', err_msg_str

        req_closer_output_file_path = response_data_list[0].context_data.get(
            'request_closer').get('output_file_path')
        return req_closer_output_file_path, err_msg_str

    def start(self, request_id, master_config, preprocessor_outfile_path, running_status_file_dir):
        try:
            err = ''
            storage_root_path = master_config.get(
                'indexer').get('storage_root_path')
            input_config_file_path = master_config.get(
                'indexer').get('input_config_file_path').replace(
                    '//', '/').replace('\\', '/').replace(storage_root_path, '')
            preprocessor_outfile_path = os.path.relpath(
                preprocessor_outfile_path, storage_root_path)
            # print(
            #     f"BEFORE preprocessor_outfile_path={preprocessor_outfile_path}")
            preprocessor_outfile_path = "/"+preprocessor_outfile_path.replace(
                '//', '/').replace('\\', '/')
            # print(f"preprocessor_outfile_path={preprocessor_outfile_path}")
            running_status_file_list = FileUtil.get_files(
                running_status_file_dir, '.json')
            if running_status_file_list:
                running_status_file_path = [
                    x for x in running_status_file_list if os.path.basename(x).endswith('run.json')]
                if running_status_file_path:
                    running_status_file_path = running_status_file_path[0]
            else:
                running_status_file_path = ''
            output_file_path, err_msg = self.__do_orchestration(
                preprocessor_outfile_path, input_config_file_path, storage_root_path, running_status_file_path)
            if not err_msg:
                err = err_msg
        except Exception as e:
            err = e
            # logger.info(f"Error = {e}")
        return output_file_path, err

    def do_processing(self):
        status = 0
        obj = AppExectutorContainer()
        _args = obj.parser()
        _request_id, _master_config, _preprocessor_outfile_path = obj.get_input_params(
            _args)
        # logger.info(
        # f"preprocessor_outfile_path={_preprocessor_outfile_path}")
        running_status_file_dir = ''
        _output_file_path, err = obj.start(
            _request_id, _master_config, _preprocessor_outfile_path,
            running_status_file_dir)
        print(f"{_output_file_path}")
        if err:
            status = 1
        return status


if __name__ == "__main__":
    # logger.info('...Infy dx Indexer App Started ...')
    status = AppExectutorContainer().do_processing()
    exit(status)
