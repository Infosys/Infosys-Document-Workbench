# ===============================================================================================================#
# Copyright 2025 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

import abc


class EmbeddingProviderInterface(metaclass=abc.ABCMeta):
    def __init__(self, config_data) -> None:
        pass

    @abc.abstractmethod
    def get_ranking(self, query: str, db_file_path_list: list) -> list:

        raise NotImplementedError
