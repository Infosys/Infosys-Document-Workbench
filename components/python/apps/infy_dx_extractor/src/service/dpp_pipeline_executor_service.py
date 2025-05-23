# ===============================================================================================================#
# Copyright 2023 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#


import os
import json

import subprocess
import time
from common.ainauto_logger_factory import AinautoLoggerFactory
from common.app_config_manager import AppConfigManager
from common.app_const import SessionKey
from common.app_session_store import AppSessionStore

from common.file_util import FileUtil
config = AppConfigManager().get_app_config()
logger = AinautoLoggerFactory().get_logger()
app_session_store = AppSessionStore()

CONFIG_PARAM_DICT: dict = {
    "processor_deployment_config_path": "",
    "processor_input_config_path": "",
    "request_file_path": ""
}

VALIDATE_DEPLOYMENT_CONFIG_PATH_LIST = [
    'processor_home_dir', 'cli_controller_dir', 'venv_script_dir']


class DppPipelineExecutorService:

    def __init__(self, config_param_dict: CONFIG_PARAM_DICT) -> None:
        self._config_param_dict = config_param_dict
        processor_deployment_config_path = config_param_dict.get(
            'processor_deployment_config_path')
        logger.info(
            f"processor_deployment_config_path - {processor_deployment_config_path}")
        self._proc_deploy_config_file_data = FileUtil.load_json(
            processor_deployment_config_path)
        is_deployment_config_valid = self.__validate_dpp_deploymet_config(
            self._proc_deploy_config_file_data)

        processor_input_config_path = config_param_dict.get(
            'processor_input_config_path')
        logger.info(
            f"processor_input_config_path - {processor_input_config_path}")
        self._proc_input_config_file_data = FileUtil.load_json(
            processor_input_config_path)
        is_input_config_valid = self.__validate_dpp_input_config(self._proc_input_config_file_data,
                                                                 self._proc_deploy_config_file_data)
        if not is_deployment_config_valid or not is_input_config_valid:
            raise Exception(
                "Invalid dpp deployment or input config. Please check the above logs.")

    # ---------- Public Methods ---------

    def execute_pipeline(self):
        processor_exec_list = []
        processor_exec_output_dict = {}
        for processor_dict in self._proc_input_config_file_data.get('processor_list', []):
            if processor_dict.get('enabled'):
                start_time = time.time()
                processor_name = processor_dict.get('processor_name')
                logger.info(f"[Running] - Processor Name - {processor_name}")
                prev_processor_output_dict = processor_exec_output_dict.get(
                    processor_exec_list[-1], {}) if processor_exec_list else {}
                processor_exec_list.append(processor_name)
                proc_deployment_data = self._proc_deploy_config_file_data['processors'].get(
                    processor_name)
                self.__update_proc_args(
                    processor_dict, self._proc_input_config_file_data, proc_deployment_data, prev_processor_output_dict)
                output_variable_dict = self.__execute_processor(
                    proc_deployment_data)
                processor_exec_output_dict[processor_name] = output_variable_dict
                logger.info(
                    f"[End] - Processor Name - {processor_name} execution elapse time is {round((time.time() - start_time)/60, 4)} mins")
        return processor_exec_list, processor_exec_output_dict

    # ---------- Private Methods ---------
    def __execute_processor(self, processor_dict):
        def _dict_to_cli_args(proc_args):
            args_list = []
            for k, v in proc_args.items():
                args_list.append(f'--{k} "{v}"')
            return ' '.join(args_list)

        def _get_env(env_var_dict):
            new_env = os.environ.copy()
            for key, val in env_var_dict.items():
                new_env[key] = val if val else ""
            return new_env
        new_output_variables_dict = {}
        try:
            run_command = f"cd {processor_dict['venv_script_dir']} "
            run_command += f"&& {processor_dict['venv_activate_cmd']} "
            run_command += f"&& cd {processor_dict['cli_controller_dir']} "
            run_command += f"&& python {processor_dict['cli_controller_file']} "
            run_command += _dict_to_cli_args(processor_dict['args'])
            logger.info("[run cmd] - " + run_command)
            new_env = _get_env(processor_dict.get('env', {}))
            sub_process = subprocess.Popen(run_command, stdout=subprocess.PIPE, stderr=subprocess.PIPE,
                                           env=new_env, universal_newlines=True, shell=True)
            stdout, stderr = sub_process.communicate()
            if "completed" not in stdout:
                raise Exception(stderr)
            output_variables_dict = processor_dict['output']['variables']

            if output_variables_dict:
                for output_variable, processor_output_var in output_variables_dict.items():
                    before_keyword, keyword, after_keyword = stdout.partition(
                        processor_output_var)
                    processor_out = after_keyword.replace(
                        '=', '').split('\n')[0].strip()
                    new_output_variables_dict[output_variable] = processor_out

        except Exception as ex:
            raise Exception(ex)
        return new_output_variables_dict

    def __update_proc_args(self, processor_dict, proc_input_config_file_data, proc_deployment_data, prev_processor_output_dict):
        def _write_to_temp_file(data):
            master_config = app_session_store.get_data(
                SessionKey.MASTER_CONFIG_FILE_DATA)
            json_file_path = f"{master_config['extractor'].get('temp_folder_path')}/{FileUtil.get_uuid()}_pipln_in_config.json"
            logger.info(f"Processor input config file path - {json_file_path}")
            with open(json_file_path, "w") as json_file:
                json.dump(data, json_file, indent=4)
            rel_path = json_file_path.replace(
                master_config['default'].get('data_root_path'), "")
            return rel_path

        def _get_processor_input_config(processor_list, input_config_dict):
            found_proc = {}
            for x in processor_list:
                val = input_config_dict.get(x)
                if val:
                    found_proc[x] = val
            return found_proc

        filtered_proc_in_config = _get_processor_input_config(processor_dict.get(
            'processor_input_config_name_list'), proc_input_config_file_data.get('processor_input_config'))
        filtered_proc_in_config_file = _write_to_temp_file(
            filtered_proc_in_config)
        proc_variables = proc_input_config_file_data.get('variables', {})
        # updating variable with previous processor output variable result
        proc_variables.update(prev_processor_output_dict)
        proc_variables['DPP_SYS_PIPELINE_INPUT_CONFIG_PATH'] = filtered_proc_in_config_file
        proc_variables['DPP_SYS_PIPELINE_REQ_FILE_PATH'] = self._config_param_dict.get(
            'request_file_path')
        updated_arg_dict = {}
        for k, v in proc_deployment_data.get('args', {}).items():
            if '${' in v:
                f_v = v.replace('${', '').replace('}', '')
                v = proc_variables.get(f_v.upper() if f_v else f_v)
            updated_arg_dict[k] = v
        proc_deployment_data['args'] = updated_arg_dict

        updated_arg_dict = {}
        for k, v in proc_deployment_data.get('env', {}).items():
            if '${' in v:
                f_v = v.replace('${', '').replace('}', '')
                v = proc_variables.get(f_v.upper() if f_v else f_v)
            updated_arg_dict[k] = v
        proc_deployment_data['env'] = updated_arg_dict

    def __validate_dpp_deploymet_config(self, config_data):
        key_missing_dict, invalid_path_dict = {}, {}
        for key, value in config_data.get('processors', {}).items():
            if not value.get('enabled'):
                continue
            for x in VALIDATE_DEPLOYMENT_CONFIG_PATH_LIST:
                if not value.get(x):
                    self.__add_or_update_dict(key_missing_dict, key, x)
                elif not os.path.exists(value.get(x)):
                    self.__add_or_update_dict(invalid_path_dict, key, x)
        if key_missing_dict:
            logger.error(
                f"Invalid deployment config - key missing - {key_missing_dict}")
        if invalid_path_dict:
            logger.error(
                f"Invalid deployment config - path is not valid - {invalid_path_dict}")
        is_valid = not key_missing_dict and not invalid_path_dict
        return is_valid

    def __validate_dpp_input_config(self, input_config_data, deployment_config_data):
        enabled_processor_list = [k for k, v in deployment_config_data.get(
            'processors', {}).items() if v.get('enabled')]
        not_enabled_processors_list = []
        for x in input_config_data.get('processor_list', []):
            if not x.get('enabled'):
                continue
            if x.get('processor_name') not in enabled_processor_list:
                not_enabled_processors_list.append(x.get('processor_name'))
        if not_enabled_processors_list:
            logger.error(
                f"Invalid input config - processor(s) are not enabled in deployment config - {not_enabled_processors_list}")
        is_valid = not not_enabled_processors_list
        return is_valid

    def __add_or_update_dict(self, d, k, v):
        if k in d:
            d[k].append(v)
        else:
            d[k] = [v]
        return d
