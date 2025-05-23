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
from app_executor_container import AppExecutorContainer
from common.file_util import FileUtil

from common.logger_factory import LoggerFactory
from common.app_config_manager import AppConfigManager
from common.app_message_handler import AppMessageHandler


app_config = AppConfigManager().get_app_config()
logger = LoggerFactory().get_logger()
app_message_handler = AppMessageHandler()

MESSAGE_STOP = "stop"


class AppArgs:
    def __init__(self) -> None:
        self.__master_config = None
        self.__data_folder_path = None
        self.__casecreator_outfile_path = None
        self.__rd_profile_name = None
        self.__postprocessor_outfile_path = None
        self.data_file_path = None
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
    def data_folder_path(self):
        return self.__data_folder_path

    @data_folder_path.setter
    def data_folder_path(self, value):
        self.__data_folder_path = value

    @property
    def casecreator_outfile_path(self):
        return self.__casecreator_outfile_path

    @casecreator_outfile_path.setter
    def casecreator_outfile_path(self, value):
        self.__casecreator_outfile_path = value

    @property
    def rd_profile_name(self):
        return self.__rd_profile_name

    @rd_profile_name.setter
    def rd_profile_name(self, value):
        self.__rd_profile_name = value

    @property
    def postprocessor_outfile_path(self):
        return self.__postprocessor_outfile_path

    @postprocessor_outfile_path.setter
    def postprocessor_outfile_path(self, value):
        self.__postprocessor_outfile_path = value


class AppScheduler:

    def __init__(self):
        self.__is_process_running = False
        self.__is_shutdown_initiated = False

    def __get_app_args(self):
        app_args = AppArgs()
        app_args.master_config = app_config["APP_ARGS"]["master_config"]
        master_config_data = FileUtil.load_json(app_args.master_config)
        self.__work_location = master_config_data["default"]["work_folder_path"]
        for stage_file in FileUtil.get_files(
                f'{self.__work_location}/{app_config["WORKFLOW"]["prev_scheduler_stage_folder"]}', '*.txt'):
            req_file_list = FileUtil.get_files(master_config_data["post_processor"]["output_path_root"],
                                               os.path.basename(stage_file).replace(".txt", ""))
            if req_file_list:
                app_args.request_id = os.path.basename(
                    req_file_list[0]).split("_")[0]
                app_args.postprocessor_outfile_path = req_file_list[0]
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
        app_args = self.__get_app_args()
        if not app_args.postprocessor_outfile_path:
            logger.info("No item to process!")
            self.__is_process_running = False
            return
        obj = AppExecutorContainer()
        request_id, request_file_path, master_config, output_location = obj.get_input_params(
            app_args)
        response_dict = obj.start(
            request_id, request_file_path, master_config, output_location)
        if response_dict.get('is_exec_success'):
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
