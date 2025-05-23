# ===============================================================================================================#
# Copyright 2023 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

from common.common_util import Singleton


class AppSessionStore(metaclass=Singleton):
    def __init__(self) -> None:
        self.__store = {}

    def set_data(self, key, value):
        self.__store[key] = value

    def get_data(self, key):
        return self.__store.get(key)

    def delete_data(self, key):
        if key in self.__store:
            del self.__store[key]

    def clear_data(self):
        self.__store.clear()
