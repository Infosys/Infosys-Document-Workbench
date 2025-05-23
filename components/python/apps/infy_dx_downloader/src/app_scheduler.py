# ===============================================================================================================#
# Copyright 2025 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

import os
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
from os import path

app_config = AppConfigManager().get_app_config()
logger = AinautoLoggerFactory().get_logger()
app_message_handler = AppMessageHandler()

MESSAGE_STOP = "stop"


class AppArgs:
    def __init__(self) -> None:
        self.__master_config = None
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


class AppScheduler:
    def __init__(self):
        self.__is_process_running = False
        self.__is_shutdown_initiated = False

    def __get_app_args(self):
        app_args = AppArgs()
        app_args.master_config = app_config["APP_ARGS"]["master_config"]
        return app_args

    def __schedule_helper(self):
        if self.__is_process_running:
            return
        self.__check_and_prepare_for_shutdown()
        if self.__is_shutdown_initiated:
            return
        self.__is_process_running = True
        obj = AppExecutor()
        try:
            request_file_path, master_config, batch_files_list, response_dict = obj.start_executor(
                self.__get_app_args())
        except Exception as e:
            logger.warning(
                f"!!!IMPORTANT. Take action on missing files. {e.args}")
            request_file = e.args[0]['request_file']
            FileUtil.move_file(request_file, f"{request_file}.FAILED")
            self.__is_process_running = False
            return

        if not request_file_path and len(batch_files_list) == 0:
            logger.info("No item to process!")
            self.__is_process_running = False
            return

        if response_dict["is_exec_success"]:
            self.__generate_current_stage_comp_file(
                master_config, response_dict.get('output_path'))
        self.__is_process_running = False

    def __generate_current_stage_comp_file(self, master_config, ouput_path):
        stage_folder = FileUtil.create_dirs_if_absent(
            f'{master_config["default"]["work_folder_path"]}/{app_config["WORKFLOW"]["current_scheduler_stage_folder"]}')
        stage_file = f'{stage_folder}/{os.path.basename(ouput_path)}.txt'
        FileUtil.write_to_file(ouput_path, stage_file)
        logger.info(
            f"App Downloader - Scheduler stage file generated - {stage_file}")

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
