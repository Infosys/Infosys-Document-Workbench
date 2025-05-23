# ===============================================================================================================#
# Copyright 2020 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

import os
from pathlib import PurePath
import schedule
import traceback
import time
import argparse
from common.app_const import *
from app_executor_container import AppExecutor
from common.file_util import FileUtil
from common.ainauto_logger_factory import AinautoLoggerFactory
from common.app_config_manager import AppConfigManager
from common.app_message_handler import AppMessageHandler

app_config = AppConfigManager().get_app_config()
logger = AinautoLoggerFactory().get_logger()
app_message_handler = AppMessageHandler()

MESSAGE_STOP = "stop"

EXTERNAL_REQ_FILE_SAMPLE = {
    "requestId": "R-GUID7",
    "files": [{"filePath": ""}]
}


class AppArgs:
    def __init__(self) -> None:
        self.__master_config = None
        self.__preprocessor_outfile_path = None
        self.__request_file = None
        self.__request_id = None

    @property
    def request_id(self):
        return self.__request_id

    @request_id.setter
    def request_id(self, value):
        self.__request_id = value

    @property
    def master_config(self):
        return self.__master_config

    @master_config.setter
    def master_config(self, value):
        self.__master_config = value

    @property
    def preprocessor_outfile_path(self):
        return self.__preprocessor_outfile_path

    @preprocessor_outfile_path.setter
    def preprocessor_outfile_path(self, value):
        self.__preprocessor_outfile_path = value

    @property
    def request_file(self):
        return self.__request_file

    @request_file.setter
    def request_file(self, value):
        self.__request_file = value


class AppScheduler:
    def __init__(self):
        self.__is_process_running = False
        self.__is_shutdown_initiated = False
        self.__prev_stage_file = None
        self.__work_location = None
        self.__downloader_queue_path = None

    def __get_app_args(self):
        app_args = AppArgs()
        app_args.master_config = app_config["APP_ARGS"]["master_config"]
        master_config_data = FileUtil.load_json(app_args.master_config)
        app_args.preprocessor_outfile_path = master_config_data["pre_processor"]["output_path_root"]
        self.__work_location = master_config_data["default"]["work_folder_path"]
        self.__downloader_queue_path = master_config_data["downloader"]["downloader_queue_path"]
        for stage_file in FileUtil.get_files(
                f'{self.__work_location}/{app_config["WORKFLOW"]["prev_scheduler_stage_folder"]}', '*.txt'):
            req_file_list = FileUtil.get_files(master_config_data["pre_processor"]["output_path_root"],
                                               os.path.basename(stage_file).replace(".txt", ""))
            if req_file_list:
                app_args.request_id = os.path.basename(
                    req_file_list[0]).split("_")[0]
                app_args.request_file = req_file_list[0]
                self.__prev_stage_file = stage_file
                break
            else:
                self.__move_stage_file_to_error(stage_file)
        return app_args

    def __move_stage_file_to_error(self, stage_file):
        FileUtil.move_file(stage_file, FileUtil.create_dirs_if_absent(
            f'{self.__work_location}/{app_config["WORKFLOW"]["current_scheduler_stage_folder_error"]}'))

    def __generate_current_stage_comp_file(self, master_config, ouput_path):
        stage_folder = FileUtil.create_dirs_if_absent(
            f'{master_config["default"]["work_folder_path"]}/{app_config["WORKFLOW"]["current_scheduler_stage_folder"]}')
        stage_file = f'{stage_folder}/{os.path.basename(ouput_path)}.txt'
        FileUtil.write_to_file(ouput_path, stage_file)
        logger.info(
            f"App Extractor - Scheduler stage file generated - {stage_file}")

    def __schedule_helper(self):
        if self.__is_process_running:
            return
        self.__check_and_prepare_for_shutdown()
        if self.__is_shutdown_initiated:
            return
        self.__is_process_running = True
        obj = AppExecutor()
        app_args = self.__get_app_args()
        if not app_args.request_file:
            logger.info("No item to process!")
            self.__is_process_running = False
            return
        try:
            request_id, request_file_path, master_config, output_location, original_files_dict = obj.get_input_params(
                app_args, is_scheduler_flow=True)
        except Exception as e:
            logger.warning(
                f"!!!IMPORTANT. Take action on missing files. {e.args}")
            FileUtil.move_file(self.__prev_stage_file,
                               f"{self.__prev_stage_file}.FAILED")
            self.__is_process_running = False
            return

        if not request_file_path:
            logger.info("No item to process!")
            self.__is_process_running = False
            return
        new_output_location = f'{output_location}/{request_id}_{app_config[L_DEFAULT][OUTPUT_FILE_SUFFIX]}.json'
        response_dict = obj.start_executor(
            request_id, request_file_path, master_config, new_output_location, original_files_dict)
        if response_dict.get('is_exec_success'):
            orig_records = original_files_dict.get("original_records", [])
            if orig_records:
                destination_path = FileUtil.create_dirs_if_absent(
                    f'{output_location}/{request_id}_files')
                container_folder_level = master_config["pre_processor"]["container_folder_level"]
                input_path_root = master_config["pre_processor"]["input_path_root"]

                request_file_set = set()
                if original_files_dict.get('external_request_file'):
                    request_file_set.add(
                        original_files_dict.get('external_request_file'))
                for file_path in orig_records:
                    path_list = str(PurePath(file_path)).lower().replace(
                        str(PurePath(input_path_root)).lower(), "").split("\\")
                    move_me = input_path_root + \
                        "/".join(path_list[:container_folder_level+1])
                    request_file_set.add(move_me)

                for x in request_file_set:
                    try:
                        # ----- Moving original files from input location to output location `{request_id}_files` folder
                        folder_path = f"{str(PurePath(x).parent).lower()}\\".replace(
                            f"{str(PurePath(input_path_root)).lower()}\\", "")
                        new_destination_path = FileUtil.create_dirs_if_absent(
                            os.path.join(str(PurePath(destination_path)), folder_path))
                        FileUtil.move_file(x, new_destination_path)

                        # ----- Removing the file lock that is generated in downloader component -----
                        lock_file = f"{self.__downloader_queue_path}/{FileUtil.get_file_path_str_hash_value(x)}"
                        if os.path.exists(lock_file):
                            FileUtil.remove_files([lock_file])
                    except Exception as e:
                        logger.error(e)

            self.__generate_current_stage_comp_file(
                master_config, response_dict.get('output_path'))
            FileUtil.remove_files([self.__prev_stage_file])
        else:
            self.__move_stage_file_to_error(self.__prev_stage_file)
        self.__is_process_running = False

    def __check_and_prepare_for_shutdown(self):
        if (app_message_handler.retrieve_message(MESSAGE_STOP) == MESSAGE_STOP):
            message_dtm = app_message_handler.get_message_dtm(
                MESSAGE_STOP)
            logger.info(
                f"Application is shutting down in response to '{MESSAGE_STOP}' message received on '{message_dtm}'")
            app_message_handler.unregister_message(MESSAGE_STOP)
            logger.info(
                f"'{MESSAGE_STOP}' message is unregisterd.")
            self.__is_shutdown_initiated = True

    def start_scheduler(self, every_seconds: int = 10):
        try:
            schedule.every(int(every_seconds)).seconds.do(
                self.__schedule_helper)
            while 1 and not self.__is_shutdown_initiated:
                schedule.run_pending()
                time.sleep(1)
        except Exception as ex:
            full_trace_error = traceback.format_exc()
            logger.error(full_trace_error)
            logger.error("Unexpected Error : {}".format(
                ex))


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument('--message', default=None,
                        help='Send a message to a running application instance')
    args = parser.parse_args()
    if args.message:
        if args.message.lower() == MESSAGE_STOP:
            app_message_handler.register_message(args.message)
        else:
            logger.info(f"'{args.message}' message is invalid!")
            os.sys.exit(0)
        logger.info(f"'{args.message}' message is registered.")
        os.sys.exit(0)
    AppScheduler().start_scheduler()
