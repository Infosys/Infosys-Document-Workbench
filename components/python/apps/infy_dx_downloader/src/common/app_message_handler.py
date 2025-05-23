# ===============================================================================================================#
# Copyright 2025 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

import os
import time


class AppMessageHandler():
    """Class to handle messages from external source"""
    __MESSAGE_FILE_EXTENSION = ".appmessage"

    def __init__(self) -> None:
        self.__script_dir = os.path.dirname(os.path.realpath(__file__))

    def register_message(self, message: str):
        """Adds a message to the registry (file-system)"""
        def write_to_file(content, file_path):
            with open(file_path, "w") as f:
                f.write(content)
            return
        message_file_path = self.__get_message_file_path(message)
        write_to_file('', message_file_path)

    def retrieve_message(self, message: str):
        """Reads a message from the registry (file-system)"""
        def read_from_file(file_path):
            with open(file_path, "r") as f:
                return f.read()
        message_file_path = self.__get_message_file_path(message)
        if os.path.exists(message_file_path):
            content = read_from_file(message_file_path)
            if content:
                return content
            return message
        return None

    def get_message_dtm(self, message: str):
        """Returns date and time of message from registry (file-system)"""
        message_file_path = self.__get_message_file_path(message)
        if os.path.exists(message_file_path):
            last_modified_dtm_since_epoch = os.path.getmtime(message_file_path)
            return time.ctime(last_modified_dtm_since_epoch)
        return None

    def unregister_message(self, message: str):
        """Deletes a message from the registry (file-system)"""
        message_file_path = self.__get_message_file_path(message)
        if os.path.exists(message_file_path):
            os.remove(message_file_path)

    def __get_message_file_path(self, message):
        """Returns the location of message in the registry (file-system)"""
        _message_file_path = f"{self.__script_dir}/{message.lower()}{self.__MESSAGE_FILE_EXTENSION}"
        return _message_file_path
