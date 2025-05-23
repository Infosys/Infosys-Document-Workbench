# ===============================================================================================================#
# Copyright 2020 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#
from common.app_config_manager import AppConfigManager
from common.ainauto_logger_factory import AinautoLoggerFactory
import os
import schedule
import traceback
import time
import argparse
from common.app_const import *
from app_executor_container import AppExecutor
from common.file_util import FileUtil
from common.app_message_handler import AppMessageHandler
from pydantic import BaseModel
app_config = AppConfigManager().get_app_config()
logger = AinautoLoggerFactory().get_logger()
app_message_handler = AppMessageHandler()

MESSAGE_STOP = "stop"


class AppArgs(BaseModel):
    data_file_path: str = None
    data_folder_path: str = None
    extractor_outfile_path: str = None
    preprocessor_outfile_path: str = None
    indexer_outfile_path: str = None
    master_config: str = None
    rd_profile_name: str = None
    request_id: str = None


class AppScheduler:
    def __init__(self):
        self.__is_process_running = False
        self.__prev_stage_file = None
        self.__work_location = None
        self.__is_shutdown_initiated = False

    def __get_app_args(self):
        app_args = AppArgs()
        app_args.master_config = app_config["APP_ARGS"]["master_config"]
        master_config_data = FileUtil.load_json(app_args.master_config)
        self.__work_location = master_config_data["default"]["work_folder_path"]
        prev_stage = app_config[app_config["WORKFLOW"]
                                ["wf.pss.enabled"]]
        for stage_file in FileUtil.get_files(
                f'{self.__work_location}/{prev_stage["prev_scheduler_stage_folder"]}', '*.txt'):
            req_file_list = FileUtil.get_files(master_config_data["pre_processor"]["output_path_root"],
                                               os.path.basename(stage_file).replace(".txt", ""))
            if req_file_list:
                app_args.request_id = os.path.basename(
                    req_file_list[0]).split("_")[0]
                setattr(
                    app_args, prev_stage['req_file_arg_name'], req_file_list[0])
                self.__prev_stage_file = stage_file
                break
            else:
                self.__move_stage_file_to_error(stage_file)
        app_args.rd_profile_name = master_config_data["extractor"]["rd_profile_name"]
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
        if not app_args.preprocessor_outfile_path:
            logger.info("No item to process!")
            self.__is_process_running = False
            return
        request_id, request_file_path, master_config, output_location = obj.get_input_params(
            app_args)
        response_dict = obj.execute(
            request_id, request_file_path, master_config, output_location, app_args.rd_profile_name)
        if response_dict["is_exec_success"]:
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
