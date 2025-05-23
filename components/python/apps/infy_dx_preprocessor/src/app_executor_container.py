# ===============================================================================================================#
# Copyright 2020 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

import copy
import json
import os
import socket
import time
import argparse
import traceback
from pathlib import Path
from common.common_util import CommonUtil
from common.file_util import FileUtil
from common.app_const import *
from common.ainauto_logger_factory import AinautoLoggerFactory
from common.app_config_manager import AppConfigManager
from process.telemetry_process import TelemetryProcess

app_config = AppConfigManager().get_app_config()
logger = AinautoLoggerFactory().get_logger()
about_app = AppConfigManager().get_about_app()


class AppExecutor:
    from process.preprocessor import PrePreprocessor

    def __init__(self):
        self.__telemetry_process = None

    def parse_args(self):
        parser = argparse.ArgumentParser()
        parser.add_argument('--request_file', default=None,
                            help='Request json file')
        parser.add_argument(
            '--request_id', default=None, help='Request id to be appended to the output file saved')
        parser.add_argument(
            '--master_config', default=None, help='Master config file path')
        parser.add_argument(
            '--preprocessor_outfile_path', default=None,
            help='File Path to save the response')
        args = parser.parse_args()
        return args

    def start_executor(self, request_id, preprocess_request_file_path, master_config, output_location,
                       original_files_dict):
        preprocessor_dict, response_dict = {}, {}
        try:
            if not self.__telemetry_process:
                self.__telemetry_process = TelemetryProcess(master_config)
            if not preprocess_request_file_path:
                logger.info(f"No item to process!")
                return response_dict
            logger.info("START: Execute All Records.")
            start_time = time.time()
            request_dict = FileUtil.load_json(
                preprocess_request_file_path.strip())

            group_uuid_dict = dict()

            if master_config['pre_processor'].get('container_folder_level'):
                container_folder_level = master_config['pre_processor'].get(
                    'container_folder_level')
            else:
                container_folder_level = 1
            for x in request_dict.get('records'):
                rel_path = os.path.relpath(
                    x.get('doc_original_path'), x.get('input_path_root'))
                rel_path_copy = rel_path
                components = []
                while rel_path_copy:
                    base_path = os.path.relpath(
                        rel_path_copy, os.path.dirname(rel_path_copy))
                    components.append(base_path)
                    rel_path_copy = os.path.dirname(rel_path_copy)
                if container_folder_level > 0 and len(components) >= container_folder_level-1:
                    full_dir_path = os.path.join(
                        x.get('input_path_root'), *components[-1:-container_folder_level:-1])
                    if len(components) >= container_folder_level:
                        if os.path.isdir(os.path.join(full_dir_path, components[-container_folder_level])):
                            full_dir_path = os.path.join(
                                full_dir_path, components[-container_folder_level])
                        elif FileUtil.get_file_exe(os.path.join(full_dir_path, components[-container_folder_level])).lower() == FileExtension.ZIP:
                            full_dir_path = os.path.join(
                                full_dir_path, components[-container_folder_level])
                else:
                    full_dir_path = os.path.join(
                        x.get('input_path_root'), components[0])

                if os.path.isdir(full_dir_path) and full_dir_path != os.path.join(x.get('input_path_root'), *components[-1:-container_folder_level:-1]):
                    if full_dir_path not in group_uuid_dict:
                        group_uuid_dict[full_dir_path] = FileUtil.get_uuid()
                elif os.path.isfile(full_dir_path):
                    doc_file_ext = FileUtil.get_file_exe(full_dir_path)
                    if doc_file_ext.lower() == FileExtension.ZIP:
                        group_uuid_dict[full_dir_path] = FileUtil.get_uuid()
                if full_dir_path in group_uuid_dict:
                    group_uuid = group_uuid_dict[full_dir_path]
                else:
                    group_uuid = FileUtil.get_uuid()
                x['doc_group_id'] = f'G-{group_uuid}'

            if preprocess_request_file_path:
                x['doc_original_sub_path'] = os.path.relpath(
                    x.get('doc_original_path'), x.get('input_path_root'))

            preprocessor_dict[ConfProp.WORK_LOCATION] = master_config['default']['work_folder_path']
            preprocessor_dict[ConfProp.OUTPUT_LOCATION] = output_location
            preprocessor_dict[ConfProp.INPUT_FILE_PATHS] = request_dict[ConfProp.RECORDS]
            preprocessor_dict['doc_batch_id'] = request_id
            preprocessor_dict[ConfProp.PAGES] = request_dict['pages'] if request_dict.get(
                'pages') else master_config.get('pre_processor', {}).get('pages', '')
            preprocessor_dict[ConfProp.OCR_TOOL] = request_dict[ConfProp.OCR_TOOL]if request_dict.get(
                ConfProp.OCR_TOOL) else master_config['default'][ConfProp.OCR_TOOL]
            preprocessor_dict['original_files_dict'] = original_files_dict
            if request_dict.get('ocr_provider_settings'):
                ocr_provider_settings = request_dict['ocr_provider_settings']
            else:
                ocr_provider_settings = master_config['default']['ocr_provider_settings']

            # write summary file at the level of response output path
            log_folder_path = Path(output_location).parent.absolute()
            log_file_path = f'{log_folder_path}/{request_id}_{socket.gethostname()}_batch_extraction_summary.json'
            # logger.info("Preprocessor Dict: {}".format(preprocessor_dict))
            response_dict = self.PrePreprocessor(
                ocr_provider_settings, preprocessor_dict[ConfProp.OCR_TOOL], master_config, self.__telemetry_process
            ).execute(preprocessor_dict, log_file_path)

            logger.info(
                f"{len(preprocessor_dict['input_file_paths'])} files Total time taken for is {round((time.time() - start_time)/60, 4)} mins")
            logger.info("END: Execute all records.")
            return response_dict
        except Exception:
            logger.error(traceback.format_exc())
            print(traceback.format_exc())

    def get_input_params(self, args, is_scheduler_flow=False):
        master_config = FileUtil.load_json(args.master_config)
        request_file_path, original_files_dict = None, {}
        if not (args.request_file or args.request_id):
            raise Exception(
                "Please provide both `request_file` and `request_id` arguments. ")

        request_id = args.request_id
        request_file_path = args.request_file
        requst_data = FileUtil.load_json(request_file_path)
        ext_req_file = None
        output_location = args.preprocessor_outfile_path
        if not output_location:
            output_location = master_config["pre_processor"][ConfProp.OUTPUT_ROOT_FOLDER]
        if not is_scheduler_flow and os.path.isdir(output_location):
            output_location = f'{output_location}/{args.request_id}_{app_config[L_DEFAULT][OUTPUT_FILE_SUFFIX]}.json'

        copy_path_files, original_files = [], []
        req_uuid = request_id[2:]
        for rec in requst_data.get('records', []):
            # ----- original files copied to work location input uuid folder
            record = copy.deepcopy(rec)
            ext_req_file = record.get('external_request_file_path')
            orig_file = record.get('doc_original_path')
            original_files.append(orig_file)
            input_path_root = record.get('input_path_root')
            doc_file_ext = FileUtil.get_file_exe(orig_file)
            if doc_file_ext.lower() == FileExtension.ZIP:
                logger.info(
                    "Found input ZIP file, hence unzipping to input folder")
                temp_folder_path = FileUtil.create_dirs_if_absent(
                    f"{master_config['pre_processor']['temp_folder_path']}/D-{req_uuid}-{FileUtil.get_uuid().split('-')[0]}")
                unzipped_files = FileUtil.unzip_file_to_path(
                    orig_file, temp_folder_path)
                for x in unzipped_files:
                    input_path = FileUtil.create_dirs_if_absent(
                        f"{master_config['default']['work_folder_path']}/input/D-{req_uuid}-{FileUtil.get_uuid().split('-')[0]}")
                    input_file = f'{input_path}/{os.path.basename(x)}'
                    doc_copy_path, error_val = FileUtil.copy_file(
                        orig_file, input_file)
                    record['doc_original_sub_path'] = os.path.relpath(os.path.join(
                        orig_file, *x.split("D-")[1].split('/')[1:]), input_path_root)
                    record['doc_copy_path'] = doc_copy_path
                    copy_path_files.append(record)
            else:
                input_path = FileUtil.create_dirs_if_absent(
                    f"{master_config['default']['work_folder_path']}/input/D-{req_uuid}-{FileUtil.get_uuid().split('-')[0]}")
                input_file = f'{input_path}/{os.path.basename(orig_file)}'
                doc_copy_path, error_val = FileUtil.copy_file(
                    orig_file, input_file)
                if error_val:
                    raise Exception(error_val)
                record['doc_original_sub_path'] = os.path.relpath(
                    orig_file, input_path_root)
                record['doc_copy_path'] = doc_copy_path
                copy_path_files.append(record)

        original_files_dict['original_records'] = original_files
        original_files_dict['external_request_file'] = ext_req_file
        if copy_path_files:
            request_dict = {}
            request_dict.update(about_app)
            request_dict['records'] = copy_path_files
            CommonUtil.update_app_info(request_dict, about_app)
            request_file_path = f"{master_config['default']['work_folder_path']}/{request_id}_pre_processor_request.json"
            logger.info(
                "Saving request file: {}".format(request_file_path))
            FileUtil.save_to_json(
                request_file_path, request_dict, is_exist_archive=True)
        logger.info("request_id: {},\nrequest_file_path: {},\nmaster_config: {},\noutput_location: {}".format(
            request_id, request_file_path, args.master_config, output_location))
        return request_id, request_file_path, master_config, output_location, original_files_dict


if __name__ == "__main__":
    # To run the script, pass the correct arguements in python.code-workspace
    # Below are the following options to pass arguements in combination
    # 1. --master_config, --request_file, --request_id, --preprocessor_outfile_path
    # python app_executor_container.py --master_config "" --data_file_path "" --preprocessor_outfile_path ""

    status = 1
    obj = AppExecutor()
    args = obj.parse_args()
    request_id, request_file_path, master_config, output_location, original_files_dict = obj.get_input_params(
        args)
    response_dict = obj.start_executor(
        request_id, request_file_path, master_config, output_location, original_files_dict)
    logger.info(f"response_dict - {json.dumps(response_dict, indent=4)}")
    print(response_dict.get('output_path'))
    if response_dict.get('is_exec_success'):
        status = 0
    exit(status)
