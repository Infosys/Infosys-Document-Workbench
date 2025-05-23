# ===============================================================================================================#
# Copyright 2020 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

import os
import schedule
import traceback
import time
import argparse
from common.app_const import *
from common.file_util import FileUtil
from common.ainauto_logger_factory import AinautoLoggerFactory
from common.app_config_manager import AppConfigManager
from common.app_message_handler import AppMessageHandler
from app_executor_container import AppExectutorContainer

app_config = AppConfigManager().get_app_config()
logger = AinautoLoggerFactory().get_logger()
app_message_handler = AppMessageHandler()

MESSAGE_STOP = "stop"


class AppArgs:
    def __init__(self) -> None:
        # self.__request_file_path = None
        self.__processor_orchestrator_config_path = None

    # @property
    # def request_file_path(self):
    #     return self.__request_file_path

    # @request_file_path.setter
    # def request_file_path(self, value):
    #     self.__request_file_path = value

    @property
    def processor_orchestrator_config_path(self):
        return self.__processor_orchestrator_config_path

    @processor_orchestrator_config_path.setter
    def processor_orchestrator_config_path(self, value):
        self.__processor_orchestrator_config_path = value


class AppScheduler:
    def __init__(self):
        self.__is_process_running = False
        self.__is_shutdown_initiated = False
        self.__external_request = False

    def __get_app_args(self):
        app_args = AppArgs()
        # scheduler input from processor_orchestrator
        # app_args.request_file_path = ''
        processor_orchestrator_config_path = app_config['APP_ARGS']['processor_orchestrator_config_path']
        processor_orchestrator_key_file_path = app_config[
            'APP_ARGS']['processor_orchestrator_key_file_path']
        processor_orchestrator_key = app_config['APP_ARGS']['processor_orchestrator_key']
        if processor_orchestrator_config_path:
            app_args.processor_orchestrator_config_path = processor_orchestrator_config_path
        else:
            master_json_file = FileUtil.load_json(
                processor_orchestrator_key_file_path)
            temp = master_json_file
            keys_list = processor_orchestrator_key.split('.')
            for key in keys_list:
                temp = temp[key]
            app_args.processor_orchestrator_config_path = temp
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
            f"App Orchestrator - Scheduler stage file generated - {stage_file}")

    def __schedule_helper(self):
        if self.__is_process_running:
            return
        self.__check_and_prepare_for_shutdown()
        if self.__is_shutdown_initiated:
            return
        self.__is_process_running = True
        obj = AppExectutorContainer()
        try:
            _pipeline_config_path = obj.get_inputs(
                self.__get_app_args())
        except Exception as e:
            logger.warning(
                f"!!!IMPORTANT. Take action on missing files. {e.args}")
            # request_file = e.args[0]
            # FileUtil.move_file(request_file, f"{request_file}.FAILED")
            self.__is_process_running = False
            return
        # TODO: request file path need to check
        # _request_file_path = app_config['APP_ARGS']['request_file_path']
        # if not _request_file_path:
        #     logger.info("No item to process!")
        #     self.__is_process_running = False
        #     return
        processor_orchestrator_key_file_path = app_config[
            'APP_ARGS']['processor_orchestrator_key_file_path']
        master_config_data = FileUtil.load_json(
            processor_orchestrator_key_file_path)
        self.__work_location = master_config_data["default"]["work_folder_path"]
        for stage_file in FileUtil.get_files(
                f'{self.__work_location}/{app_config["WORKFLOW"]["prev_scheduler_stage_folder"]}', '*.txt'):
            req_file_list = FileUtil.get_files(master_config_data["pre_processor"]["output_path_root"],
                                               os.path.basename(stage_file).replace(".txt", ""))
            if req_file_list:
                _request_file_path = req_file_list[0]
                self.__prev_stage_file = stage_file
                break
            else:
                self.__move_stage_file_to_error(stage_file)
        _running_status_file_dir = app_config['APP_ARGS']['run_file_dir']
        response_path, err_msg = obj.start(
            _request_file_path, _pipeline_config_path, _running_status_file_dir)
        if not err_msg:
            self.__generate_current_stage_comp_file(
                master_config_data, response_path)
            FileUtil.remove_files([self.__prev_stage_file])

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
