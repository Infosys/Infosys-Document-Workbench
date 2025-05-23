# ===============================================================================================================#
# Copyright 2025 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

import time
import os
from infy_gen_ai_sdk.embedding.provider.st.st_service import StService
from common.singleton import Singleton
from common.app_config_manager import AppConfigManager
# from sentence_transformers import SentenceTransformer


app_config = AppConfigManager().get_app_config()
MODEL_CATEGORY_SENTENCE_TRANSFORMER = "MODEL_SENTENCE_TRANSFORMER"
MODEL_NAME_PREFIX = "model_name_"
MODEL_PATH_PREFIX = "model_path_"


class SentenceTransformerService(metaclass=Singleton):

    def __init__(self):
        model_to_obj_dict = {}
        overall_elapsed_time = 0
        for i in range(1, 100):
            model_name_key = f"{MODEL_NAME_PREFIX}{i}"
            model_name = app_config[MODEL_CATEGORY_SENTENCE_TRANSFORMER].get(
                model_name_key, False)
            model_path_key = f"{MODEL_PATH_PREFIX}{i}"
            model_path = app_config[MODEL_CATEGORY_SENTENCE_TRANSFORMER].get(
                model_path_key, False)
            if model_name and model_path:
                start_time = time.time()
                # Store in dictionary using lowercase
                # model_to_obj_dict[model_name] = SentenceTransformer(model_path)
                model_to_obj_dict[model_name] = StService(model_name=model_name,
                                                          model_home_path=os.path.dirname(model_path))
                elapsed_time = round(time.time() - start_time, 3)
                # print(f'Load time for model {model_name}: {elapsed_time} secs')
                overall_elapsed_time += elapsed_time

        print(f'Total load time for all models: {overall_elapsed_time} secs')
        self.__model_to_obj_dict = model_to_obj_dict

    def generate_embedding(self, text, model_name) -> dict:
        #     result = {
        #         'embedding': [],
        #         'size': 0,
        #         'error_message': None,
        #         'model_name': model_name
        #     }
        model_obj = self.__model_to_obj_dict.get(model_name, None)
        result = model_obj.generate_embedding(text, model_name)
    #     if not model_obj:
    #         result['error_message'] = f'Model not found: {model_name}'
    #     else:
    #         embedding_as_numpy = model_obj.encode(text)
    #         embedding_as_list = embedding_as_numpy.astype(float).tolist()
    #         result['embedding'] = embedding_as_list
    #         result['size'] = len(embedding_as_list)

        return result
