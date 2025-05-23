# ===============================================================================================================#
# Copyright 2020 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

import json
import os
from os import path
import socket
import traceback
import argparse
import time
from pathlib import Path
from common.app_session_store import AppSessionStore
from common.file_util import FileUtil
from process.processor import Processor
from process.telemetry_process import TelemetryProcess
from common.app_const import *

from common.ainauto_logger_factory import AinautoLoggerFactory
from common.app_config_manager import AppConfigManager
from common.common_util import CommonUtil

app_config = AppConfigManager().get_app_config()
logger = AinautoLoggerFactory().get_logger()
about_app = AppConfigManager().get_about_app()
app_session_store = AppSessionStore()


class AppExecutor:
    def __init__(self):
        self.processorObj = Processor()
        self.__telemetry_process = None

    def parse_args(self):
        parser = argparse.ArgumentParser()
        parser.add_argument('--preprocessor_outfile_path',
                            default=None, help='Request json file')
        parser.add_argument('--indexer_outfile_path',
                            default=None, help='Request json file')
        # rd_profile_name made option as it can be preconfiged/auto detected by rd config
        parser.add_argument('--rd_profile_name',
                            default=None, help='RD profile name')
        parser.add_argument(
            '--request_id', default=None, help='Request id to be appended to the output file saved')
        parser.add_argument(
            '--master_config', default=None, required=True, help='Master config file path')
        parser.add_argument(
            '--extractor_outfile_path', default=None,
            help='File Path to save the response')
        args = parser.parse_args()
        return args

    def execute(self, request_id, extractor_request_file, master_config, output_location, rd_profile_name=None):
        try:
            if not self.__telemetry_process:
                self.__telemetry_process = TelemetryProcess(master_config)
            logger.info("START: Execute All Records.")
            processor_dict, response_dict = {}, {}
            # ------------------ setting app session data ------------------
            app_session_store.set_data(SessionKey.EXT_CONFIG_FILE_DATA, FileUtil.load_json(
                master_config['extractor']['rd_config_file_path']))
            app_session_store.set_data(
                SessionKey.MASTER_CONFIG_FILE_DATA, master_config)

            # processor_dict['rd_config_profile_name'] = rd_profile_name if rd_profile_name else args.rd_profile_name
            processor_dict['rd_config_profile_name'] = rd_profile_name
            preprocess_output_dict = FileUtil.load_json(
                extractor_request_file.strip())
            processor_dict['output_location'] = master_config['extractor'].get('output_path_root') if master_config['extractor'].get(
                'output_path_root') else master_config['default']['work_folder_path']
            processor_dict['records'] = preprocess_output_dict['records']
            # write summary file at the level of response output path
            log_folder_path = Path(output_location).parent.absolute()
            processor_dict[
                'log_file_path'] = f'{log_folder_path}/{request_id}_{socket.gethostname()}_batch_extraction_summary.json'

            for ocr_name, _ in preprocess_output_dict['records'][0]['ocr_files'].items():
                response_dict = self.processorObj.process(
                    processor_dict, ocr_name, output_location, self.__telemetry_process)
                if not response_dict.get("is_exec_success"):
                    break
            logger.info("END: Execute all records.")
            return response_dict
        except Exception:
            full_trace_error = traceback.format_exc()
            logger.error(full_trace_error)
            print(full_trace_error)

    def get_input_params(self, args):
        if not args.request_id and not args.extractor_outfile_path:
            raise Exception(
                "Either provide request_id or extractor_outfile_path")

        master_config = FileUtil.load_json(
            args.master_config)

        if args.request_id:
            request_id = args.request_id
        else:
            request_id = f'R-{FileUtil.get_uuid()}'
        request_file_path = f"{master_config['default']['work_folder_path']}/{request_id}_extractor_request.json"

        # Run extractor based on indexer or preprocessor output file
        cli_req_file_path = args.indexer_outfile_path if hasattr(
            args, 'indexer_outfile_path') and args.indexer_outfile_path else args.preprocessor_outfile_path
        request_dict = FileUtil.load_json(cli_req_file_path)

        CommonUtil.update_app_info(request_dict, about_app)

        FileUtil.write_to_json(
            request_dict, request_file_path, is_exist_archive=True)

        if args.extractor_outfile_path:
            # output_location = args.extractor_outfile_path
            output_location = FileUtil.create_dirs_if_absent(
                os.path.dirname(args.extractor_outfile_path))+'/'+os.path.basename(args.extractor_outfile_path)
        else:
            # TODO: For handling multiple OCR tool, we need code changes.
            out_file_name = f'/{args.request_id}_{app_config[L_DEFAULT][OUTPUT_FILE_SUFFIX]}.json'
            output_location = master_config["extractor"]["output_path_root"] + out_file_name

        logger.info("request_id: {},\nrequest_file_path: {},\nmaster_config: {},\noutput_location: {}".format(
            request_id, request_file_path, args.master_config, output_location))
        return request_id, request_file_path, master_config, output_location


if __name__ == "__main__":
    # To run the script, pass the correct arguements in python.code-workspace
    # Below are the following options to pass arguements in combination
    # 1. --master_config, --preprocessor_outfile_path, --extractor_outfile_path, --rd_profile_name
    # 2. --master_config, --preprocessor_outfile_path, --request_id, --rd_profile_name
    # python app_executor_container.py --master_config "" --rd_profile_name ""   "" --preprocessor_outfile_path ""
    start_time = time.time()
    status = 1
    obj = AppExecutor()
    args = obj.parse_args()
    request_id, request_file_path, master_config, output_location = obj.get_input_params(
        args)
    response_dict = obj.execute(
        request_id, request_file_path, master_config, output_location, args.rd_profile_name)
    logger.info(
        f"[End] - Extractor execution elapse time is {round((time.time() - start_time)/60, 4)} mins")
    logger.info(f"response_dict - {json.dumps(response_dict, indent=4)}")
    print(response_dict.get('output_path'))
    if response_dict.get('is_exec_success'):
        status = 0
    exit(status)
