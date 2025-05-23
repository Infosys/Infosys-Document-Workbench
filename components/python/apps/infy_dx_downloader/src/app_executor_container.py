# ===============================================================================================================#
# Copyright 2025 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

import json
import os
import argparse
import traceback
from os import path
from pathlib import Path
from common.common_util import CommonUtil
from common.file_util import FileUtil
from common.app_const import *
from common.ainauto_logger_factory import AinautoLoggerFactory
from common.app_config_manager import AppConfigManager

app_config = AppConfigManager().get_app_config()
logger = AinautoLoggerFactory().get_logger()
about_app = AppConfigManager().get_about_app()


class AppExecutor:

    def parse_args(self):
        parser = argparse.ArgumentParser()
        parser.add_argument(
            '--master_config', default=None, help='Master config file path')
        parser.add_argument(
            '--request_id', default=None, help='Request id to be appended to the output file saved ')
        args = parser.parse_args()
        return args

    def start_executor(self, app_args):
        try:
            # Sample code for upstream integration
            # Get files from upstream to specific input container folder
            # "downloader": {
            #     "upstream_path": "c:/workarea/documentworkbench/data/upstream",
            #     "tenant_id_folder_name": "t02"
            # }
            # if app_args.upstream_path:
            #     destination_path = FileUtil.create_dirs_if_absent(
            #         f'{app_args.data_folder_path}/{app_args.tenant_id_folder_name}')
            #     for files in os.listdir(app_args.upstream_path):
            #         FileUtil.move_file(os.path.join(
            #             app_args.upstream_path, files), destination_path)

            # Generate a batch request file
            req_id, preprocess_request_file_path, master_config_file, output_loc, batch_files_list = AppExecutor(
            ).get_output_params(app_args)

            response_dictn = {
                'output_path': None,
                'is_exec_success': False
            }
            if preprocess_request_file_path:
                response_dictn["is_exec_success"] = True
                response_dictn["output_path"] = f'{output_loc}/{req_id}_{app_config[L_DEFAULT][OUTPUT_FILE_SUFFIX]}.json'
            if not preprocess_request_file_path:
                logger.info(f"No item to process!")
                return preprocess_request_file_path, master_config_file, batch_files_list, response_dictn

            return preprocess_request_file_path, master_config_file, batch_files_list, response_dictn
        except Exception:
            logger.error(traceback.format_exc())
            print(traceback.format_exc())

    def get_output_params(self, args):
        master_config = FileUtil.load_json(args.master_config)
        request_file_path = None
        data_folder_path = master_config["downloader"]["input_path_root"]
        max_batch_size = master_config["downloader"]["max_batch_size"]
        downloader_outfile_path = master_config["downloader"]["output_path_root"]
        downloader_queue_path = master_config["downloader"]["downloader_queue_path"]
        is_ext_req_enabled = master_config["downloader"].get(
            "external_request")

        def _generate_file_lock(file_path):
            is_locked = False
            processed_files_unique_value = FileUtil.get_file_path_str_hash_value(
                file_path)
            processed_files_list = [os.path.basename(f) for f in FileUtil.get_files(
                downloader_queue_path, file_format="**/*", recursive=True)]
            if processed_files_unique_value not in processed_files_list:
                processed_file_path = FileUtil.create_dirs_if_absent(
                    downloader_queue_path)+'/'+processed_files_unique_value
                with open(processed_file_path, 'x') as f:
                    f.write(file_path)
                is_locked = True
            return is_locked
        if data_folder_path:
            def _get_file_data_list():
                data_folder_path_tmp, file_format = [], "**/*"  # "*.*"
                if data_folder_path:
                    data_folder_path_tmp = data_folder_path
                uuid = FileUtil.get_uuid()
                data_files = FileUtil.get_files(
                    data_folder_path_tmp, file_format=file_format, recursive=True)
                # to get Container folder
                count = 0
                batch_file_list = []
                active_container_folder = None
                container_folder_level = master_config["downloader"]["container_folder_level"]
                for file_path in data_files:
                    rel_path = os.path.relpath(
                        file_path, data_folder_path_tmp)
                    rel_path_copy = rel_path
                    components = []
                    while rel_path_copy:
                        base_path = os.path.relpath(
                            rel_path_copy, os.path.dirname(rel_path_copy))
                        components.append(base_path)
                        rel_path_copy = os.path.dirname(rel_path_copy)
                    if container_folder_level > 0 and len(components) >= container_folder_level-1:
                        full_dir_path = os.path.join(
                            data_folder_path_tmp, *components[-1:-container_folder_level:-1])
                        if len(components) >= container_folder_level:
                            full_dir_path = os.path.join(
                                full_dir_path, components[-container_folder_level])
                    else:
                        full_dir_path = os.path.join(
                            data_folder_path_tmp, components[0])
                    # Batch request
                    container_folder_next = full_dir_path
                    if container_folder_next != active_container_folder:
                        # new container folder has started
                        if os.path.isfile(container_folder_next):
                            if count >= int(max_batch_size):
                                break
                        if count >= int(max_batch_size):
                            break
                        else:
                            if not _generate_file_lock(container_folder_next):
                                continue
                    count += 1
                    batch_file_list.append(file_path)
                    active_container_folder = container_folder_next

                return batch_file_list, uuid, f'R-{uuid}', None

            def _get_external_req_file_data_list():
                found_request_file = []
                found_files = FileUtil.get_files(data_folder_path, "*.json")
                failed_req_files = FileUtil.get_files(
                    data_folder_path, "*.json.FAILED")
                if failed_req_files:
                    logger.warning(
                        f"!!!IMPORTANT. Found FAILED request files {failed_req_files}")
                uuid = FileUtil.get_uuid()
                r_id = f'R-{uuid}'
                ext_req_file = None
                for f_file in found_files:
                    if _generate_file_lock(f_file):
                        ext_req_file = f_file
                        request_data = FileUtil.load_json(ext_req_file)
                        found_request_file = [x.get('filePath') for x in request_data.get(
                            'files') if x.get('filePath')]
                        r_id = request_data.get("requestId")
                        uuid = r_id[2:]
                        break
                return found_request_file, uuid, r_id, ext_req_file

            if not downloader_outfile_path:
                raise Exception("Output file path invalid")
            output_location = FileUtil.create_dirs_if_absent(
                os.path.dirname(downloader_outfile_path))+'/'+os.path.basename(downloader_outfile_path)
            copy_path_files = []
            original_files, uuid, request_id, ext_req_file = _get_external_req_file_data_list(
            ) if is_ext_req_enabled else _get_file_data_list()
            if args.request_id:
                request_id = args.request_id
            for orig_file in original_files:
                logger.info(f'Processing started for file {orig_file}')
                record_data = {
                    'input_path_root': data_folder_path,
                    'doc_original_path': orig_file,
                    'doc_original_sub_path': os.path.relpath(orig_file, data_folder_path)
                }
                if is_ext_req_enabled:
                    record_data['external_request_file_path'] = ext_req_file
                copy_path_files.append(record_data)

            if copy_path_files:
                request_dict = {}
                request_dict.update(about_app)
                request_dict['records'] = copy_path_files
                CommonUtil.update_app_info(request_dict, about_app)
                request_file_path = f'{output_location}/{request_id}_{app_config[L_DEFAULT][OUTPUT_FILE_SUFFIX]}.json'
                logger.info(
                    "Saving request file: {}".format(request_file_path))
                FileUtil.save_to_json(
                    request_file_path, request_dict, is_exist_archive=True)
        else:
            raise Exception(
                "Either provide Data file/folder or provide request_file and request id")
        logger.info("request_id: {},\nrequest_file_path: {},\nmaster_config: {},\noutput_location: {}".format(
            request_id, request_file_path, args.master_config, output_location))
        return request_id, request_file_path, master_config, output_location, original_files


if __name__ == "__main__":
    # To run the script, pass the correct arguements in python.code-workspace

    status = 1
    obj = AppExecutor()
    args = obj.parse_args()
    request_file_path, master_config, batch_files_list, response_dict = obj.start_executor(
        args)
    logger.info(f"response_dict - {json.dumps(response_dict, indent=4)}")
    print(response_dict.get('output_path'))
    if not request_file_path and len(batch_files_list) == 0:
        print("No item to process!")
        status = 99  # soft fail
    elif response_dict.get('is_exec_success'):
        status = 0
    exit(status)
