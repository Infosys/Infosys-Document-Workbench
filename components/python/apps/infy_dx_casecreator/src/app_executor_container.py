# ===============================================================================================================#
# Copyright 2025 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

import argparse
import concurrent.futures
import copy
import json
import os
import socket
import time
import traceback
from pathlib import Path
from itertools import groupby

from common.app_config_manager import AppConfigManager
from common.common_util import CommonUtil
from common.file_util import FileUtil
from common.logger_factory import LoggerFactory
from process.create_case_process import CreateCaseProcess
from process.telemetry_process import TelemetryProcess
from service.document_service import DocumentService

app_config = AppConfigManager().get_app_config()
logger = LoggerFactory().get_logger()
about_app = AppConfigManager().get_about_app()


class AppExecutorContainer():

    def __init__(self):
        self.__telemetry_process = None

    def parse_args(self):
        parser = argparse.ArgumentParser()
        parser.add_argument('--postprocessor_outfile_path', default=None, required=True,
                            help='Request json file')
        parser.add_argument(
            '--request_id', default=None, help='Request id to be appended to the output file saved')
        parser.add_argument(
            '--master_config', default=None, required=True, help='Master config file path')
        parser.add_argument(
            '--casecreator_outfile_path', default=None,
            help='File Path to save the response')
        args = parser.parse_args()
        return args

    def start(self, request_id, request_file_path, master_config, output_location):
        self.__case_creator_config = master_config['case_creator']

        def _validate_path(file_path: str):
            if not os.path.exists(os.path.abspath(file_path)):
                logger.error(f"File not found : {file_path}")
                raise FileNotFoundError(file_path)
            return file_path
        if not self.__telemetry_process:
            self.__telemetry_process = TelemetryProcess(master_config)
        logger.info("START: Execute All Records.")
        response_dict = {
            'output_path': output_location,
            'is_exec_success': False
        }
        raw_extracted_data_dict_list = next(FileUtil.read_json(
            _validate_path(request_file_path)))
        # write summary file at the level of response output path
        log_folder_path = Path(output_location).parent.absolute()
        log_file_path = f'{log_folder_path}/{request_id}_{socket.gethostname()}_batch_extraction_summary.json'

        raw_extracted_data_dict_sublists = [[]]
        tenant_ids_doc_type_cde_combinations = {}
        pos = 1
        for item in raw_extracted_data_dict_list['records']:
            if item['docwb_config_attribute'].get('tenant_id'):
                if tenant_ids_doc_type_cde_combinations.get(item['docwb_config_attribute'].get('tenant_id')+'__'+str(item['docwb_config_attribute'].get('doc_type_cde'))):
                    raw_extracted_data_dict_sublists[tenant_ids_doc_type_cde_combinations.get(item['docwb_config_attribute'].get(
                        'tenant_id')+'__'+str(item['docwb_config_attribute'].get('doc_type_cde')))].append(item)
                else:
                    tenant_ids_doc_type_cde_combinations[item['docwb_config_attribute'].get(
                        'tenant_id')+'__'+str(item['docwb_config_attribute'].get('doc_type_cde'))] = pos
                    pos += 1
                    raw_extracted_data_dict_sublists.append([item])
            else:
                raw_extracted_data_dict_sublists[0].append(item)

        case_created_list = []
        for items in raw_extracted_data_dict_sublists:
            if len(items) > 0 and items[0]['docwb_config_attribute'].get('tenant_id'):
                self.__case_creator_config["tenant_id"] = items[0]['docwb_config_attribute'].get(
                    'tenant_id')
                self.__case_creator_config["doc_type_cde"] = items[0]['docwb_config_attribute'].get(
                    'doc_type_cde')
            if len(items) > 0:
                create_case_obj = CreateCaseProcess(self.__case_creator_config)
                # --------------------- Manage dynamic queue creation/assignment ---------------------
                create_case_obj.manage_new_queue(items)
                create_case_obj.manage_queue_assignment(
                    items)

                # --------------------- Start case creation process ---------------------

                try:
                    summary_dict = FileUtil.load_json(
                        log_file_path) if os.path.exists(log_file_path) else {}
                except:
                    summary_dict = {}
                previous_run_response_dict = {}
                if os.path.exists(output_location):
                    case_respone = next(FileUtil.read_json(output_location))
                    if case_respone:
                        for case_record in case_respone.get('records'):
                            previous_run_response_dict[case_record['doc_id']
                                                       ] = case_record
                with concurrent.futures.ThreadPoolExecutor(
                        max_workers=int(
                            app_config['THREAD']['thread_pool_max_worker_count']),
                        thread_name_prefix="th_create_case") as executor:
                    # group the records in list of list format
                    sorted_records = sorted(
                        items, key=lambda record: record.get('doc_group_id'))
                    grouped_records = [list(result) for _, result in groupby(
                        sorted_records, key=lambda record: record.get('doc_group_id'))]
                    thread_pool_dict = {
                        executor.submit(
                            create_case_obj.execute,
                            doc_data_list,
                            summary_dict,
                            previous_run_response_dict,
                            self.__telemetry_process
                        ): doc_data_list for doc_data_list in grouped_records
                    }
                    for future in concurrent.futures.as_completed(thread_pool_dict):
                        updated_doc_data_list, summary_dict = future.result()
                        if updated_doc_data_list:
                            case_created_list.extend(updated_doc_data_list)

        if case_created_list:
            doc_service_obj = DocumentService(master_config)
        # --------------------- Update status to configured upstream, if any ---------------------
            if master_config['case_creator'].get('status_update_to_upstream_enabled'):
                is_status_updated = doc_service_obj.update_post_casecreate_status(
                    case_created_list)
                if is_status_updated is not None:
                    for case_data in case_created_list:
                        if not case_data.get('docwb_case_data', {}).get('status_updated_to_upstream', False):
                            case_data['docwb_case_data']['status_updated_to_upstream'] = is_status_updated
                            case_data['docwb_case_data']['upstream_status'] = master_config['doc_status_updater']['post_case_create_status']

        # --------------------- Generate attibute source files  ---------------------
            attribute_src_file_list = doc_service_obj.write_attribute_source_to_filedb(
                case_created_list)

        # --------------------- Generate batch output file  ---------------------
            case_created_list = sorted(
                case_created_list, key=lambda y: y['doc_num'])
            final_output_structure = {}
            final_output_structure.update(about_app)
            final_output_structure["records"] = case_created_list
            CommonUtil.update_app_info(final_output_structure, about_app)

            FileUtil.write_to_json(
                final_output_structure, output_location, is_exist_archive=True)
        FileUtil.write_to_json(
            summary_dict, log_file_path)
        logger.debug("App Executor Container Process Completed...")

        logger.info("********** Execution Summary **********")
        logger.info(json.dumps(
            {'case_creator': summary_dict.get('case_creator', {})}, indent=4))

        # --------------------- input/output record validation  ---------------------
        logger.info(f"Processed records count- {len(case_created_list)}")
        if len(raw_extracted_data_dict_list['records']) != len(case_created_list):
            diff_list = list(set([x['doc_id'] for x in raw_extracted_data_dict_list['records']]) - set(
                [x['doc_id'] for x in case_created_list]))
            logger.error(
                f"Generated output records count {len(case_created_list)} is not matching with Input records count {len(raw_extracted_data_dict_list['records'])}.")
            logger.error(f'Missing doc_id(s): {diff_list}')
            return response_dict

        if len(raw_extracted_data_dict_list['records']) != len(attribute_src_file_list):
            logger.error(
                f"Generated attribute source output records count {len(attribute_src_file_list)} is not matching with Input records count {len(raw_extracted_data_dict_list['records'])}.")
            return response_dict

        if os.path.exists(output_location):
            response_dict["is_exec_success"] = True
        logger.info("END: Execute all records.")
        return response_dict

    def get_input_params(self, args):
        if not args.request_id and not args.casecreator_outfile_path:
            raise Exception(
                "Either provide request_id or casecreator_outfile_path")

        master_config = FileUtil.load_json(
            args.master_config)

        if args.request_id:
            request_id = args.request_id
        else:
            request_id = f'R-{FileUtil.get_uuid()}'
        request_file_path = f"{master_config['default']['work_folder_path']}/{request_id}_case_creator_request.json"
        request_dict = FileUtil.load_json(args.postprocessor_outfile_path)
        CommonUtil.update_app_info(request_dict, about_app)

        FileUtil.write_to_json(
            request_dict, request_file_path, is_exist_archive=True)

        if args.casecreator_outfile_path:
            output_location = args.casecreator_outfile_path
        else:
            out_file_name = f'/{args.request_id}_{app_config["DEFAULT"]["output_file_suffix"]}.json'
            output_location = master_config["case_creator"]["output_path_root"] + out_file_name

        logger.info("request_id: {},\nrequest_file_path: {},\nmaster_config: {},\noutput_location: {}".format(
            request_id, request_file_path, args.master_config, output_location))
        return request_id, request_file_path, master_config, output_location


if __name__ == "__main__":
    status = 1
    try:
        obj = AppExecutorContainer()
        args = obj.parse_args()
        # Disable proxies by setting NO_PROXY to '*'
        logger.info("Disabling proxy settings")
        os.environ["NO_PROXY"] = "*"
        request_id, request_file_path, master_config, output_location = obj.get_input_params(
            args)
        response_dict = obj.start(
            request_id, request_file_path, master_config, output_location)
        logger.info(f"response_dict - {json.dumps(response_dict, indent=4)}")
        print(response_dict.get('output_path'))
        if response_dict.get('is_exec_success'):
            status = 0
        exit(status)
    except Exception as ex:
        full_trace_error = traceback.format_exc()
        logger.error(full_trace_error)
        print(full_trace_error)
        exit(status)
