# ===============================================================================================================#
# Copyright 2022 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

import json
import os
import socket
import concurrent.futures
import traceback
import argparse
import time
from pathlib import Path
from itertools import groupby
from common.app_const import *
from process.rule_executor_base import RuleExecutorBase
from common.file_util import FileUtil
from common.common_util import CommonUtil

from common.logger_factory import LoggerFactory
from common.app_config_manager import AppConfigManager
from process.telemetry_process import TelemetryProcess

app_config = AppConfigManager().get_app_config()
logger = LoggerFactory().get_logger()
about_app = AppConfigManager().get_about_app()


class AppExecutorContainer():

    def __init__(self):
        self.__rule_executor_obj = RuleExecutorBase()
        self.__telemetry_process = None

    def parse_args(self):
        parser = argparse.ArgumentParser()
        parser.add_argument('--extractor_outfile_path', default=None, required=False,
                            help='Request json file')
        parser.add_argument('--extractor_multi_outfile_path', default=None, required=False,
                            help='Request file contains multi extractor components response file path')
        parser.add_argument(
            '--request_id', default=None, help='Request id to be appended to the output file saved')
        parser.add_argument(
            '--master_config', default=None, required=True, help='Master config file path')
        parser.add_argument(
            '--postprocessor_outfile_path', default=None,
            help='File Path to save the response')
        args = parser.parse_args()
        return args

    def start(self, request_id, request_file_path, master_config, output_location):
        def _validate_path(file_path: str):
            if not os.path.exists(os.path.abspath(file_path)):
                logger.error(f"File not found : {file_path}")
                raise FileNotFoundError(file_path)
            return file_path
        if not self.__telemetry_process:
            self.__telemetry_process = TelemetryProcess(master_config)
        logger.info("START: Execute All Records.")
        start_time = time.time()
        response_dict = {
            'output_path': output_location,
            'is_exec_success': False
        }
        raw_extracted_data_dict_list = next(FileUtil.read_json(
            _validate_path(request_file_path)))
        rule_to_attr_template_config = next(FileUtil.read_json(
            _validate_path(master_config['post_processor']['rule_to_attribute_mapping_config_path'])))
        namedRuleDefinitionsTemp = dict()
        for i in rule_to_attr_template_config["namedRuleDefinitions"]:
            namedRuleDefinitionsTemp[i["named_rule_def_id"]] = i

        for i in range(len(rule_to_attr_template_config["documentTemplates"])):
            for j in range(len(rule_to_attr_template_config["documentTemplates"][i]["ruleDefinitions"])):
                if "named_rule_def_id" in rule_to_attr_template_config["documentTemplates"][i]["ruleDefinitions"][j]:
                    def_id = rule_to_attr_template_config["documentTemplates"][
                        i]["ruleDefinitions"][j]["named_rule_def_id"]
                    temp = namedRuleDefinitionsTemp[def_id].copy()
                    temp.update(
                        rule_to_attr_template_config["documentTemplates"][i]["ruleDefinitions"][j])
                    # temp.pop("named_rule_def_id")
                    rule_to_attr_template_config["documentTemplates"][i]["ruleDefinitions"][j] = temp

        self.__validate_configured_rules(rule_to_attr_template_config)
        # write summary file at the level of response output path
        log_folder_path = Path(output_location).parent.absolute()
        log_file_path = f'{log_folder_path}/{request_id}_{socket.gethostname()}_batch_extraction_summary.json'

        final_extracted_data_dict_list = []

        try:
            summary_dict = FileUtil.load_json(
                log_file_path) if os.path.exists(log_file_path) else {}
        except:
            summary_dict = {}
        sorted_records = sorted(
            raw_extracted_data_dict_list['records'], key=lambda record: record.get('doc_group_id'))
        grouped_records = [list(result) for _, result in groupby(
            sorted_records, key=lambda record: record.get('doc_group_id'))]
        groupid_records_map = dict()
        for items in grouped_records:
            if items[0].get('doc_group_id'):
                groupid_records_map[items[0].get('doc_group_id')] = items
        with concurrent.futures.ThreadPoolExecutor(
                max_workers=int(
                    app_config['THREAD']['thread_pool_max_worker_count']),
                thread_name_prefix="th_rule_executor") as executor:
            thread_pool_dict = {
                executor.submit(
                    self.__rule_executor_obj.execute,
                    rule_to_attr_template_config,
                    raw_extracted_data_dict,
                    summary_dict,
                    self.__telemetry_process,
                    master_config,
                    groupid_records_map[raw_extracted_data_dict['doc_group_id']]
                ): raw_extracted_data_dict for raw_extracted_data_dict in raw_extracted_data_dict_list['records']
            }
            for future in concurrent.futures.as_completed(thread_pool_dict):
                new_doc_data_dict, summary_dict = future.result()
                if new_doc_data_dict:
                    final_extracted_data_dict_list.append(
                        new_doc_data_dict)

        if final_extracted_data_dict_list:
            # Sort based on doc_num to generate same results on rerun
            final_extracted_data_dict_list = sorted(
                final_extracted_data_dict_list, key=lambda y: y['doc_num'])

            response_data_dict = {}
            response_data_dict.update(about_app)
            response_data_dict['records'] = final_extracted_data_dict_list

            CommonUtil.update_app_info(response_data_dict, about_app)
            FileUtil.write_to_json(
                response_data_dict, output_location, is_exist_archive=True)
        FileUtil.write_to_json(
            summary_dict, log_file_path)
        logger.debug("App Executor Container Process Completed...")

        logger.info("********** Execution Summary **********")
        logger.info(json.dumps(
            {'post_processor': summary_dict.get('post_processor', {})}, indent=4))

        logger.info(
            f"Processed records count- {len(final_extracted_data_dict_list)}")
        if len(raw_extracted_data_dict_list['records']) != len(final_extracted_data_dict_list):

            logger.error(
                f"Generated output records count {len(final_extracted_data_dict_list)} is not matching with Input records count {len(raw_extracted_data_dict_list['records'])}.")

            diff_list = list(set([x['doc_id'] for x in raw_extracted_data_dict_list['records']]) - set(
                [x['doc_id'] for x in final_extracted_data_dict_list]))

            logger.error(
                f"Generated output records count {len(final_extracted_data_dict_list)} is not matching with Input records count {len(raw_extracted_data_dict_list['records'])}.")
            logger.error(f'Missing doc_id(s): {diff_list}')

            logger.info(F"Generated output file path : {output_location}")
            return response_dict

        if os.path.exists(output_location):
            response_dict["is_exec_success"] = True
        logger.info("END: Execute all records.")
        return response_dict

    def __validate_configured_rules(self, rule_to_attr_template_config):
        def _validate_attribute_rules(profile_template_config):
            for rule_obj in profile_template_config.get('ruleDefinitions', []):
                for rule_name in rule_obj['rule_names']:
                    try:
                        CommonUtil.get_rule_class_instance(
                            rule_name, rc_entity_name='rules_attribute')
                    except Exception:
                        rule_not_found_list.append(rule_name)
                        logger.error(f"Configured Rule not found :{rule_name}")
                        rc_full_trace_error = traceback.format_exc()
                        logger.error(rc_full_trace_error)

        def _validate_case_rules(profile_template_config):
            rule_name = profile_template_config.get(
                'docwbCaseDefinition', {}).get('rule_name')
            try:
                CommonUtil.get_rule_class_instance(
                    rule_name, rc_entity_name='rules_case')
            except Exception:
                rule_not_found_list.append(rule_name)
                logger.error(f"Configured Rule not found :{rule_name}")
                rc_full_trace_error = traceback.format_exc()
                logger.error(rc_full_trace_error)

        rule_not_found_list = []
        for profile_template_config in rule_to_attr_template_config.get('documentTemplates', []):
            if profile_template_config.get('enabled'):
                logger.info(
                    f"Rule Validation for Profile name : {profile_template_config['profile']}")
                _validate_attribute_rules(profile_template_config)
                _validate_case_rules(profile_template_config)
        if rule_not_found_list:
            raise Exception(
                f"Configured Rules not found: {','.join(rule_not_found_list)}")

    def get_input_params(self, args):
        if not args.request_id and not args.postprocessor_outfile_path:
            raise Exception(
                "Either provide request_id or postprocessor_outfile_path")

        master_config = FileUtil.load_json(
            args.master_config)

        if args.request_id:
            request_id = args.request_id
        else:
            request_id = f'R-{FileUtil.get_uuid()}'
        request_file_path = f"{master_config['default']['work_folder_path']}/{request_id}_post_processor_request.json"

        request_dict = self.__manage_request_file(args)
        CommonUtil.update_app_info(request_dict, about_app)

        FileUtil.write_to_json(
            request_dict, request_file_path, is_exist_archive=True)

        if args.postprocessor_outfile_path:
            # output_location = args.postprocessor_outfile_path
            output_location = FileUtil.create_dirs_if_absent(
                os.path.dirname(args.postprocessor_outfile_path))+'/'+os.path.basename(args.postprocessor_outfile_path)
        else:
            out_file_name = f'/{args.request_id}_{app_config[L_DEFAULT][OUTPUT_FILE_SUFFIX]}.json'
            output_location = master_config["post_processor"]["output_path_root"] + out_file_name

        logger.info("request_id: {},\nrequest_file_path: {},\nmaster_config: {},\noutput_location: {}".format(
            request_id, request_file_path, args.master_config, output_location))
        return request_id, request_file_path, master_config, output_location

    def __manage_request_file(self, args):
        def _merge_multi_files(extractor_multi_outfile_path):
            multi_file_data = FileUtil.load_json(extractor_multi_outfile_path)
            base_request_file_data = None
            for file_path in multi_file_data.get('request_file_list'):
                file_data = FileUtil.load_json(file_path)
                if not base_request_file_data:
                    base_request_file_data = file_data
                    continue
                # sort the records by doc_num
                base_records = sorted(base_request_file_data.get(
                    'records'), key=lambda d: d['doc_num'])
                cur_records = sorted(file_data.get(
                    'records'), key=lambda d: d['doc_num'])
                # loop base records and extend with current record
                for idx, b_record in enumerate(base_records):
                    c_record_filtered = cur_records[idx]
                    if c_record_filtered.get('workflow'):
                        b_record.get('workflow').extend([x for x in c_record_filtered.get(
                            'workflow') if x not in b_record.get('workflow')])
                    if c_record_filtered.get('raw_attributes'):
                        b_record.get('raw_attributes', []).extend(
                            c_record_filtered.get('raw_attributes'))
            return base_request_file_data
        if not args.extractor_multi_outfile_path:
            return FileUtil.load_json(args.extractor_outfile_path)
        return _merge_multi_files(args.extractor_multi_outfile_path)


if __name__ == "__main__":
    # To run the script, pass the correct arguements in python.code-workspace
    # Below are the following options to pass arguements in combination
    # 1. --master_config, --extractor_outfile_path, --postprocessor_outfile_path
    # 2. --master_config, --request_id --postprocessor_outfile_path
    # 3. --master_config, --extractor_multi_outfile_path, --postprocessor_outfile_path
    # python app_executor_container.py --master_config "" --postprocessor_outfile_path "" --extractor_outfile_path ""

    status = 1
    try:
        obj = AppExecutorContainer()
        args = obj.parse_args()
        request_id, request_file_path, master_config, output_location = obj.get_input_params(
            args)
        response_dict = obj.start(
            request_id, request_file_path, master_config, output_location)
        logger.info(f"response_dict - {json.dumps(response_dict, indent=4)}")
        print(response_dict.get('output_path'))
        if response_dict.get('is_exec_success'):
            status = 0
        exit(status)
    except Exception:
        full_trace_error = traceback.format_exc()
        logger.error(full_trace_error)
        print(full_trace_error)
        exit(status)
