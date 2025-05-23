# ===============================================================================================================#
# Copyright 2025 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

import os
import urllib
from common.app_config_manager import AppConfigManager

app_config = AppConfigManager().get_app_config()

DATA_ROOT_PATH = app_config['DEFAULT']['DATA_ROOT_PATH']

SHOW_PRIVATE_API = True if app_config['DEFAULT']['show_private_api'] == 'True' else False
API_RESPONSE_CDE_FAILURE = 999
API_RESPONSE_MSG_FAILURE = "Failure"
API_RESPONSE_CDE_SUCCESS = 0
API_RESPONSE_MSG_SUCCESS = "Success"
