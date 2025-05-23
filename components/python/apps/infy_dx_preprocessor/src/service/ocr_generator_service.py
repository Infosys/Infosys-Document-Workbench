# ===============================================================================================================#
# Copyright 2020 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

import os
import socket
import time
import traceback
from os import path
import copy

from infy_ocr_generator.ocr_generator import OcrGenerator
from infy_ocr_generator.providers.abbyy_ocr_data_service_provider import \
    AbbyyOcrDataServiceProvider
from infy_ocr_generator.providers.azure_ocr_data_service_provider import \
    AzureOcrDataServiceProvider
from infy_ocr_generator.providers.azure_read_ocr_data_service_provider import \
    AzureReadOcrDataServiceProvider
from infy_ocr_generator.providers.tesseract_ocr_data_service_provider import \
    TesseractOcrDataServiceProvider
import concurrent.futures
from common.app_const import *
from common.file_util import FileUtil
from common.common_util import Singleton
from common.cache_manager import CacheManager
from common.ainauto_logger_factory import AinautoLoggerFactory
from common.app_config_manager import AppConfigManager

app_config = AppConfigManager().get_app_config()
logger = AinautoLoggerFactory().get_logger()


class OcrGeneratorService(metaclass=Singleton):

    def __init__(self, provider_settings, ocr_tool, config_params):
        self._data_service_provider_dict = self.__init_data_service_provider_objects(
            provider_settings, ocr_tool)
        self.__config_params = config_params
        self.__cache_manager = CacheManager(config_params)

    def generate_ocr(
            self, ocr_provider_enabled_dict, enabled_providers, image_file_list, work_doc_file, pages):
        def _manage_cache_files(doc_data_list, provider_name):
            # if any cache ocr file, copy the same to image location.
            # so that ocr_generator will skip generating it again for existing files.
            cache_doc_list = []
            if self.__config_params.get('cache_enabled'):
                for doc_data in doc_data_list:
                    try:
                        cache_file_path = self.__cache_manager.get(
                            doc_data['doc_path'], provider_name)
                        if cache_file_path:
                            cache_doc_list.append(doc_data['doc_path'])
                            for cache_file in FileUtil.get_files(cache_file_path, "*"):
                                FileUtil.copy_file(
                                    cache_file, os.path.dirname(doc_data['doc_path']))
                    except Exception as e:
                        logger.error(e)
            return cache_doc_list

        def _check_for_rerun(receive_res_result):
            should_rerun = False
            for receive_res_data in receive_res_result:
                api_status = receive_res_data.get(
                    'receive_api', {}).get('response', {}).get('status')

                logger.info(
                    f"Input record is {receive_res_data.get('input_doc')}")
                logger.info(
                    f"Azure read api receive respone is {api_status if api_status else ''}")
                if api_status in ['running', 'notStarted']:
                    should_rerun = True
                    break
            return should_rerun

        def _execute_ocr_provider(provider_name, doc_data_list):
            provider_obj = self._data_service_provider_dict.get(
                f"{provider_name}_ocr_service_provider")
            if provider_name == OcrType.AZURE_READ:
                submit_req_res = provider_obj.submit_request(
                    doc_data_list=doc_data_list)
                log_file_prefix = path.abspath(
                    app_config[L_DEFAULT][AZURE_SUB_REQ_LOG])
                time_stamp = FileUtil.get_datetime_str()
                # Due to multi-thread adding timestamp to azure submit json
                log_file_path = f'{log_file_prefix}_{socket.gethostname()}_{time_stamp}.json'
                FileUtil.save_to_json(log_file_path, submit_req_res)

                is_first_run = True
                rr_counter = 0
                logger.info(
                    f"Azure Read sub/res api called for records {doc_data_list}")
                while(is_first_run or _check_for_rerun(receive_res_result)):
                    is_first_run = False
                    # make delay between submit_req and receive_res async call
                    time.sleep(
                        int(app_config[L_DEFAULT][AZURE_READ_API_DELAY])
                    )
                    receive_res_result = provider_obj.receive_response(
                        submit_req_res)
                    rr_counter = +1
                    logger.info(
                        f"Azure receive response counter is {rr_counter}")
                return provider_obj.generate(api_response_list=receive_res_result)
            else:
                return provider_obj.generate(doc_data_list=doc_data_list)

        def _check_ocr_file(doc_data_list, generate_ocr_res_list):
            ocr_missing_list, ocr_non_missing_list = [], []
            if not generate_ocr_res_list:
                return doc_data_list
            for ocr_res_data in generate_ocr_res_list:
                if not ocr_res_data.get('output_doc'):
                    ocr_missing_list += [
                        doc_data for doc_data in doc_data_list if doc_data['doc_path'] == ocr_res_data['input_doc']]
                # else:
                #     ocr_non_missing_list.append(ocr_res_data)
            return ocr_missing_list

        def _manage_and_retry_ocr_generate(provider_name, doc_data_list):
            generate_ocr_res_list = []
            try:
                generate_ocr_res_list = _execute_ocr_provider(
                    provider_name, doc_data_list)
                if provider_name != OcrType.AZURE_READ or provider_name != OcrType.AZURE_OCR:
                    return generate_ocr_res_list
                # Current Retry method allowed only for External API (Azure read/ocr)
                max_retry_count = app_config['DEFAULT']['max_retry_limit_post_ocr_failure']
                for i in range(int(max_retry_count)):
                    ocr_missing_list = _check_ocr_file(
                        doc_data_list, generate_ocr_res_list)
                    if ocr_missing_list:
                        # retry for the ocr missing document
                        # passing full `doc_data_list` as ocr_generator has internal logic to skip for existing ocr files.
                        logger.warning(
                            f"Retries ocr generation instance {i+1} for ocr_missing_list - {ocr_missing_list}")
                        generate_ocr_res_list = _execute_ocr_provider(
                            provider_name, doc_data_list)
                    else:
                        # break the loop when ocr available for all document
                        break
            except Exception as e:
                logger.error(e)
            return generate_ocr_res_list

        def _generate_ocr_child(image_file_list, work_doc_file, provider_name, pages):
            # For Abbyy Provider, The Ocr is generated using PDF input document(s).
            # Hence pdf to img generated to "*_files" supporting directory and ocr also generated to this location.
            try:
                word_doc_list = [
                    work_doc_file] if provider_name == OcrType.ABBYY else image_file_list
                if provider_name == OcrType.AZURE_READ and \
                        ocr_provider_enabled_dict[provider_name][ConfProp.INPUT_FILE_TYPE] == FileFormat.PDF:
                    doc_data_list = [
                        {"doc_path": work_doc_file, "pages": pages}]
                else:
                    doc_data_list = [
                        {
                            "doc_path": file_path, "pages": FileUtil.get_pages_from_filename(file_path)
                        } for file_path in word_doc_list
                    ]
                logger.info(
                    f"OCR Generator Called for {provider_name} provider...")
                # Tesseract/Azure(read) - image only
                # Get cache data from bucket
                cache_doc_list = _manage_cache_files(
                    doc_data_list, provider_name)
                generate_ocr_res_list = _manage_and_retry_ocr_generate(
                    provider_name, doc_data_list)

                if self.__config_params.get('cache_enabled'):
                    for ocr_res_data in generate_ocr_res_list:
                        ocr_res_data['is_ocr_from_cache'] = False
                        if not ocr_res_data.get('output_doc'):
                            continue
                        if ocr_res_data.get('input_doc') in cache_doc_list:
                            ocr_res_data['is_ocr_from_cache'] = True
                        if not self.__cache_manager.get(ocr_res_data.get('input_doc'), provider_name):
                            self.__cache_manager.add(
                                ocr_res_data.get('input_doc'), [ocr_res_data.get('output_doc')], provider_name)
            except Exception as e:
                logger.error(traceback.format_exc())
                generate_ocr_res_list = []
            return {"provider_name": provider_name, "generate_ocr_response": generate_ocr_res_list}
        all_generated_ocr_file_list = []
        # Generate OCR files based on all enabled providers
        # Multiple Threads to Handle MANY Input files -> MANY Providers
        logger.info("START: Generate OCR file(s).")
        start_time = time.time()
        with concurrent.futures.ThreadPoolExecutor(
                max_workers=int(app_config[L_DEFAULT][MAX_WORKERS]),
                thread_name_prefix="th_generate_ocr") as executor:
            thread_pool_dict = {
                executor.submit(
                    _generate_ocr_child,
                    image_file_list,
                    work_doc_file,
                    provider_name,
                    pages
                ): provider_name for provider_name in enabled_providers
            }
            for future in concurrent.futures.as_completed(thread_pool_dict):
                generate_ocr_res_dict = future.result()
                all_generated_ocr_file_list.append(generate_ocr_res_dict)
        logger.info(
            f"Total time taken for #Providers {len(enabled_providers)} to #Doc {len(image_file_list)} is {round((time.time() - start_time)/60, 4)} mins")
        logger.info("END: Generate OCR file(s).")
        return all_generated_ocr_file_list

    def __init_data_service_provider_objects(self, provider_settings, ocr_tool):

        def _format_azure_config_param(config_params_dict, provider_key):
            temp_dict = config_params_dict[f"azure_{provider_key}"]
            new_dict = {}
            new_dict['computer_vision'] = {
                'subscription_key': temp_dict.pop('subscription_key'),
                f'api_{provider_key}': temp_dict
            }
            return {"azure": new_dict}

        data_service_provider_dict = {}
        if ocr_tool.get(OcrType.TESSERACT, {}).get('selected'):
            data_service_provider_dict['tesseract_ocr_service_provider'] = OcrGenerator(
                data_service_provider=TesseractOcrDataServiceProvider(
                    config_params_dict={'tesseract': {
                        'pytesseract_path': os.environ['TESSERACT_PATH']}},
                    logger=logger)
            )
        if ocr_tool.get(OcrType.AZURE_READ, {}).get('selected'):
            data_service_provider_dict["azure_read_ocr_service_provider"] = OcrGenerator(
                data_service_provider=AzureReadOcrDataServiceProvider(
                    config_params_dict=_format_azure_config_param(
                        provider_settings, 'read'), logger=logger)
            )
        if ocr_tool.get(OcrType.AZURE_OCR, {}).get('selected'):
            data_service_provider_dict["azure_ocr_ocr_service_provider"] = OcrGenerator(
                data_service_provider=AzureOcrDataServiceProvider(
                    config_params_dict=_format_azure_config_param(
                        provider_settings, 'ocr'), logger=logger)
            )
        if ocr_tool.get(OcrType.ABBYY, {}).get('selected'):
            data_service_provider_dict["abbyy_ocr_service_provider"] = OcrGenerator(
                data_service_provider=AbbyyOcrDataServiceProvider(
                    config_params_dict={OcrType.ABBYY: provider_settings[OcrType.ABBYY]}, logger=logger)
            )

        return data_service_provider_dict
