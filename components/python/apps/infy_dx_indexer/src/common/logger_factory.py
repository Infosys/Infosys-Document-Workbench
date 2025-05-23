# ===============================================================================================================#
# Copyright 2023 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

"""Module containing LoggerFactory"""

import os
import logging
import time
import sys
import socket
from common.singleton import Singleton
from common.app_config_manager import AppConfigManager


class LoggerFactory(metaclass=Singleton):
    """Factory class to get singleton logger object at application level"""

    __LOG_FORMAT = '%(asctime)s.%(msecs)03d %(levelname)s [%(threadName)s] '
    __LOG_FORMAT += '[%(module)s] [%(funcName)s:%(lineno)d] %(message)s'
    __TIMESTAMP_FORMAT = '%Y-%m-%d %H:%M:%S'

    def __init__(self):
        self.__app_config = AppConfigManager().get_app_config()
        log_level = int(self.__app_config['DEFAULT']['logging_level'])
        log_to_console = self.__app_config['DEFAULT']['log_to_console'] == 'true'

        formatter = logging.Formatter(
            self.__LOG_FORMAT, datefmt=self.__TIMESTAMP_FORMAT)

        # Create logger object and store at class level
        self.__logger = logging.getLogger(__name__)
        # Set level at overall level
        self.__logger.setLevel(log_level)

        # Add sysout hander
        if log_to_console:
            console_handler = logging.StreamHandler(sys.stdout)
            console_handler.setFormatter(formatter)
            self.__logger.addHandler(console_handler)

        # NullHandler to avoid any low level library issue when Console handler also turned off.
        handler = logging.NullHandler()
        self.__logger.addHandler(handler)

        self.__logger.info("Logging module initialized")
        self.__logger.info("HOSTNAME : %s", socket.gethostname())

    def get_logger(self):
        """Returns an instance of logger object"""
        return self.__logger
