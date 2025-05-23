# ===============================================================================================================#
# Copyright 2025 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#
import json
import calendar
import datetime
import os
import time
import infy_dpp_sdk
import infy_fs_utils
from .endpoint_base import API_RESPONSE_CDE_FAILURE, \
    API_RESPONSE_MSG_SUCCESS, API_RESPONSE_CDE_SUCCESS
from fastapi import APIRouter
from data.api_schema_data import ResponseData, DocBasedQueryRequestData, QueryResponseData, \
    DocBasedTemplateQueryRequestData
from common.app_config_manager import AppConfigManager
from common.ainauto_logger_factory import AinautoLoggerFactory


# APIRouter creates path operations for module
router = APIRouter(prefix="/api/v1/model/inference",
                   responses={404: {"description": "Not found"}})


def get_files(request_file_path_list, file_sys_handler):
    storage_service_obj = file_sys_handler
    path_list = []
    for request_file_path in request_file_path_list:
        files_list = storage_service_obj.list_files(request_file_path)
        for file in files_list:
            path_list.append(file)
            path_list = list(set(path_list))
    return path_list


@router.post("/qna", response_model=ResponseData, tags=["inference"],
             summary="Generate answer to the query based on chunks of the document id given")
async def get_qna(request_data_obj: DocBasedQueryRequestData):
    return await schema_api(request_data_obj)


@router.post("/prompt", response_model=ResponseData, tags=["inference"],
             summary="Generate answer to the query based on prompt and chunks of the document id given")
async def get_qna(request_data_obj: DocBasedTemplateQueryRequestData):
    return await schema_api(request_data_obj, is_prompt=True)


async def schema_api(request_data_obj, is_prompt=False):
    # logger = AinautoLoggerFactory().get_logger()
    app_config = AppConfigManager().get_app_config()
    file_sys_handler = infy_fs_utils.manager.FileSystemManager(
    ).get_fs_handler(infy_dpp_sdk.common.Constants.FSH_DPP)
    start_time = time.time()
    date = datetime.datetime.utcnow()
    utc_time = calendar.timegm(date.utctimetuple())
    date_time_stamp = datetime.datetime.fromtimestamp(
        utc_time).strftime("%Y-%m-%d %I:%M:%S %p")
    # pydantic object to dictionary
    input_data = request_data_obj.dict()
    working_file_path_list = input_data["db_name"]
    config_file_path = app_config['STORAGE']["dpp_input_config_file_path"]
    if len(working_file_path_list) == 0:
        response_data = {}
        response_msg = "ERROR: Document id not found."
        response_cde = API_RESPONSE_CDE_FAILURE
    elif is_prompt and not input_data.get('content'):
        response_data = {}
        response_msg = "ERROR: Please provide content."
        response_cde = API_RESPONSE_CDE_FAILURE
    elif not input_data.get("question", "").strip():
        response_data = {}
        response_msg = "ERROR: Please provide Question."
        response_cde = API_RESPONSE_CDE_FAILURE
    else:
        document_id_list = [os.path.basename(x)
                            for x in working_file_path_list]
        answers = []

        input_config_data = json.loads(file_sys_handler.read_file(
            config_file_path))
        retriever_query_config_data = input_config_data[
            'processor_input_config']['QueryRetriever']['queries'][0]

        retriever_only = input_data.get('retriever_only', False)
        reader_config_data = input_config_data['processor_list'][1]
        if retriever_only:
            reader_config_data['enabled'] = False
        else:
            reader_config_data['enabled'] = True

        query_dict = {
            "attribute_key": "generic_attribute_key",
            "question": input_data["question"].strip(),
            "top_k": input_data["top_k"],
            "pre_filter_fetch_k": retriever_query_config_data.get('pre_filter_fetch_k', 10),
            "filter_metadata":  input_data.get("filter_metadata", {}),
            "threshold": retriever_query_config_data.get('threshold', None),
        }
        input_config_data['processor_input_config']['QueryRetriever']['queries'] = [
            query_dict]
        for llm_name, llm_detail in input_config_data['processor_input_config']['Reader']['llm'].items():
            if llm_detail.get('enabled'):
                get_llm_config = llm_detail.get('configuration')
                get_llm_config['temperature'] = input_data["temperature"]
                cache = llm_detail.get('cache')
                if cache:
                    cache['enabled'] = input_data["from_cache"]
        if is_prompt:
            prompt_template = input_config_data['processor_input_config']['Reader']["inputs"][0].get(
                "prompt_template")
            existing_content_list = input_config_data['processor_input_config']['Reader']["named_prompt_templates"].get(prompt_template)[
                'content']
            input_config_data['processor_input_config']['Reader']["named_prompt_templates"].get(
                prompt_template)['content'] = input_data.get('content')

        for document_id in document_id_list:
            # doc_work_folder_id = f"D-{document_id}"//remove | rename dbname

            # --old_code_start--
            # document_data_json_file_path_list = [file for file in file_sys_handler.list_files(
            #     f"/data/work/{doc_work_folder_id}") if file.endswith('document_data.json')]
            # if len(document_data_json_file_path_list) > 0:
            #     document_data_json_file_path = document_data_json_file_path_list[0]
            # else:
            #     response_data = {}
            #     response_msg = f"ERROR: Document data file of {document_id} not found."
            #     response_cde = API_RESPONSE_CDE_FAILURE
            #     elapsed_time = round(time.time() - start_time, 3)
            #     return ResponseData(response=response_data, responseCde=response_cde,
            #                         responseMsg=str(response_msg), timestamp=date_time_stamp,
            #                         responseTimeInSecs=(elapsed_time))
            # document_data_json = json.loads(
            #     file_sys_handler.read_file(document_data_json_file_path))
            # --old_code_end--

            # --new_code_start--
            metadata = infy_dpp_sdk.data.MetaData(
                standard_data=infy_dpp_sdk.data.StandardData(
                    filepath=infy_dpp_sdk.data.ValueData()))
            document_data = infy_dpp_sdk.data.DocumentData(metadata=metadata)
            document_data.document_id = document_id
            context_data = {
            }
            response_data = infy_dpp_sdk.data.ProcessorResponseData(
                document_data=document_data, context_data=context_data)
            document_data_json = json.loads(response_data.json(indent=4))
            # --new_code_end--

            # Updating config file with api i/p i.e.query,from_cache, temperature and content if is_prompt
            file_sys_handler.write_file(
                config_file_path, json.dumps(input_config_data, indent=4))

            try:
                # ---------------------Run the inference pipeline  LOGIC STARTS------------------------------ #

                dpp_orchestrator = infy_dpp_sdk.orchestrator.OrchestratorNativeBasic(
                    input_config_file_path=config_file_path)
                response_data_list = dpp_orchestrator.run_batch([infy_dpp_sdk.data.DocumentData(**document_data_json.
                                                                                                get('document_data'))],
                                                                [document_data_json.get('context_data')])

                # dpp_orchestrator = infy_dpp_sdk.orchestrator.OrchestratorNative(
                #     input_config_file_path=config_file_path)
                # response_data_list = dpp_orchestrator.run_batch()

                if is_prompt:
                    # Restore the content in config file to original content
                    input_config_data['processor_input_config']['Reader']["named_prompt_templates"].get(
                        prompt_template)['content'] = existing_content_list
                file_sys_handler.write_file(
                    config_file_path, json.dumps(input_config_data, indent=4))
                for processor_response_data in response_data_list:
                    response_data = processor_response_data.dict()
                    model_output = response_data.get('context_data', {}).get(
                        'reader', {}).get('output', [{}])[0].get('model_output', {})
                    if isinstance(model_output, dict):
                        answer = response_data.get('context_data', {}).get('reader', {}).get(
                            'output', [{}])[0].get('model_output', {}).get('answer', '')
                        chunk_id = model_output.get('sources')[0].get(
                            'chunk_id') if model_output.get('sources') else ""
                        page_num = model_output.get('sources')[0].get(
                            'page_no') if model_output.get('sources') else 0
                        segment_num = model_output.get('sources')[0].get(
                            'sequence_no') if model_output.get('sources') else 0
                    else:
                        answer = model_output
                        chunk_id = ""
                        page_num = 0
                        segment_num = 0
                    answers.append({
                        "doc_id": document_id,
                        "doc_name": response_data.get('context_data', {}).get('query_retriever', {}).get('queries', [{}])[0].get('top_k_matches', [{}])[0].get('meta_data', {}).get('doc_name', ''),
                        "answer": answer,
                        "chunk_id": chunk_id,
                        "page_num": page_num,
                        "segment_num": segment_num,
                        "source_metadata": response_data.get('context_data', {}).get('reader', {}).get('output', [{}])[0].get('source_metadata', []),
                        "embedding_model_name": response_data.get('context_data', {}).get('query_retriever', {}).get('queries', [{}])[0].get('embedding_model', ''),
                        "distance_metric": response_data.get('context_data', {}).get('query_retriever', {}).get('queries', [{}])[0].get('distance_metric', ''),
                        "top_k": input_data["top_k"],
                        "top_k_list": response_data.get('context_data', {}).get('query_retriever', {}).get('queries', [{}])[0].get('top_k_matches', [{}]),
                        "top_k_aggregated": response_data.get('context_data', {}).get('reader', {}).get('output', [{}])[0].get('retriever_output', [{}]).get('top_k', 0) if not retriever_only else 0,
                        "llm_model_name": response_data.get('context_data', {}).get('reader', {}).get('output', [{}])[0].get('model_name', ''),
                        "llm_total_attempts": response_data.get('context_data', {}).get('reader', {}).get('output', [{}])[0].get('total_attempts', 1) if not retriever_only else 0,
                        "llm_response": {
                            "response": json.dumps(model_output),
                            "from_cache": response_data.get('context_data', {}).get('reader', {}).get('output', [{}])[0].get('used_cache', False)
                        },
                        "llm_prompt": {"prompt_template":  response_data.get('context_data', {}).get('reader', {}).get('output', [{}])[0].get('model_input', {}).get('prompt_template', ''),
                                       "context": response_data.get('context_data', {}).get('reader', {}).get('output', [{}])[0].get('model_input', {}).get('template_var_to_value_dict', {}).get('context', ''),
                                       "query": input_data.get("question", '').strip(),
                                       "parameters": {
                            "temperature": input_data.get("temperature", '')
                        }},
                        "version": "0.0.5",
                        "error": response_data.get('message_data').get('message') if response_data.get('message_data') else ""
                    })

            except Exception as e:
                response_data = {}
                response_msg = e
                response_cde = API_RESPONSE_CDE_FAILURE

        if len(answers) > 0:
            try:
                response_data = QueryResponseData(answers=answers)
                response_cde = API_RESPONSE_CDE_SUCCESS
                response_msg = API_RESPONSE_MSG_SUCCESS
            except Exception as e:
                response_data = {}
                response_msg = e
                response_cde = API_RESPONSE_CDE_FAILURE

    elapsed_time = round(time.time() - start_time, 3)
    # response_data = response_data
    # response_cde = response_cde
    # response_msg = response_msg
    response = ResponseData(response=response_data, responseCde=response_cde,
                            responseMsg=str(response_msg), timestamp=date_time_stamp,
                            responseTimeInSecs=(elapsed_time))
    return response
