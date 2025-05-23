# ===============================================================================================================#
# Copyright 2025 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

import os
import urllib3
from urllib.parse import urlencode
import json
import traceback
import concurrent.futures
from common.file_util import FileUtil
from common.logger_factory import LoggerFactory
from common.app_config_manager import AppConfigManager

app_config = AppConfigManager().get_app_config()
logger = LoggerFactory().get_logger()


class DocumentService:
    def __init__(self, master_config):
        self.__master_config = master_config

    def update_post_casecreate_status(self, doc_list):
        http = urllib3.PoolManager()
        upstream_url = self.__master_config['doc_status_updater'].get(
            "upstream_url")
        doc_list = [
            {
                "document_id": doc_data['doc_id'],
                "status": self.__master_config['doc_status_updater']['post_case_create_status'],
            } for doc_data in doc_list if not doc_data.get('status_updated_to_upstream', False)
        ]
        if not doc_list:
            return None
        request = {"documents": doc_list}
        logger.debug(f"Request: {upstream_url}")
        logger.debug(f"Request Data: {request}")
        encoded_data = json.dumps(request).encode('utf-8')
        headers = {
            'Content-Type': 'application/json',
            "Authorization": f"Basic {self.__master_config['doc_status_updater']['authorization']}"
        }
        response = http.request("post", upstream_url,
                                body=encoded_data, headers=headers)
        logger.debug(f"Response Status: {response.status}")
        return True if response.status == 200 else False

    def write_attribute_source_to_filedb(self, records):
        def _write_to_filedb(doc_data_dict):
            try:
                docwb_case_num = doc_data_dict['docwb_case_data']['case_num']
                if not docwb_case_num:
                    return None
                doc_id_suffix = doc_data_dict['doc_id'].split("-")[-1]
                docwbdx_filedb_path_root = FileUtil.create_dirs_if_absent(self.__master_config[
                    'case_creator']['docwbdxfiledb_path_root'])
                docwbdx_filedb_path = f"{docwbdx_filedb_path_root}/{docwb_case_num}_{doc_id_suffix}_attribute_source.json"
                doc_data_dict['docwb_case_data']['docwbdxfiledb_path'] = docwbdx_filedb_path
                if not os.path.exists(docwbdx_filedb_path):
                    FileUtil.write_to_json(doc_data_dict, docwbdx_filedb_path)
                else:
                    logger.warning(
                        f"File already exist in this location {docwbdx_filedb_path}. Hence skipping to write it again.")
                return docwbdx_filedb_path
            except Exception as e:
                full_trace_error = traceback.format_exc()
                logger.error(full_trace_error)
                return None
        attr_src_file_list = []
        with concurrent.futures.ThreadPoolExecutor(
                max_workers=int(
                    app_config['THREAD']['thread_pool_max_worker_count']),
                thread_name_prefix="th_attr_source_fdb") as executor:
            thread_pool_dict = {
                executor.submit(
                    _write_to_filedb,
                    doc_data_dict
                ): doc_data_dict for doc_data_dict in records
            }
            for future in concurrent.futures.as_completed(thread_pool_dict):
                output_path = future.result()
                if output_path:
                    attr_src_file_list.append(output_path)
        return attr_src_file_list
