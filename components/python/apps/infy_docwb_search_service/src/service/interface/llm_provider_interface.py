# ===============================================================================================================#
# Copyright 2025 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

import abc


class LlmProviderInterface(metaclass=abc.ABCMeta):
    def __init__(self, config_data: dict) -> None:
        pass

    @abc.abstractmethod
    def get_answer(self, query: str, template: str, db_file_path: str,
                   context_template: str, chunk_metadata: dict) -> str:

        NotImplementedError

    @abc.abstractmethod
    def get_answer_from_text(self, query: str, template: str, combined_text_path: str) -> str:

        NotImplementedError
