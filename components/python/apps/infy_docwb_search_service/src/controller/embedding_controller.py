# ===============================================================================================================#
# Copyright 2025 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

import json
import time
from datetime import datetime, timezone
from schema.embedding_req_res_data import (
    EmbeddingData, GenerateEmbeddingRequestData, GenerateEmbeddingResponseData)
from schema.base_req_res_data import (ResponseCode, ResponseMessage)
from service.sentence_transformer_service import SentenceTransformerService
from common.app_config_manager import AppConfigManager
from .b_controller import BController

app_config = AppConfigManager().get_app_config()
sentence_transform_service_obj = SentenceTransformerService()


class EmbeddingController(BController):
    """Generate Embeddings"""

    __CONTROLLER_PATH = "/embeddings"

    def __init__(self, context_root_path: str = ''):
        super().__init__(context_root_path=context_root_path,
                         controller_path=self.__CONTROLLER_PATH)

        self.get_router().add_api_route(
            "/generate", self.generate_embedding, methods=["POST"], summary="Generates model embedding using requested model name",
            tags=["embedding"])

    def generate_embedding(self, request_data_obj: GenerateEmbeddingRequestData):
        print(request_data_obj)
        start_time = time.time()
        date = datetime.now(timezone.utc)
        date_time_stamp = date.strftime("%Y-%m-%d %I:%M:%S %p")
        input_data = request_data_obj.dict()

        try:
            json.loads(json.dumps(input_data))
        except ValueError:
            return "Invalid json"

        embedding_result = sentence_transform_service_obj.generate_embedding(input_data['text'],
                                                                             input_data['modelName'])
        error_message = embedding_result['error_message']
        response_cde = ResponseCode.SUCCESS
        response_msg = ResponseMessage.SUCCESS
        embedding_data = None
        if error_message:
            response_cde = ResponseCode.FAILURE
            response_msg = error_message
            embedding_data = EmbeddingData()
        else:
            embedding_data = EmbeddingData(
                modelName=embedding_result['model_name'],
                embedding=embedding_result['embedding'],
                size=embedding_result['size'],
            )

        elapsed_time = round(time.time() - start_time, 3)
        response = GenerateEmbeddingResponseData(response=embedding_data,
                                                 responseCde=response_cde, responseMsg=response_msg,
                                                 timestamp=date_time_stamp, responseTimeInSecs=(elapsed_time))
        return response
