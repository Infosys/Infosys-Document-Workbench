# ===============================================================================================================#
# Copyright 2022 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#
import os
import time
import traceback

from common.ainauto_logger_factory import AinautoLoggerFactory
from common.app_config_manager import AppConfigManager
from infy_telemetry_sdk import docwb_telemetry
from infy_telemetry_sdk.docwb_telemetry import (EVENT_DATA, DocwbTelemetry,
                                                LogLevel, TelemetryEvent)

from common.common_util import CommonUtil
from common.file_util import FileUtil

logger = AinautoLoggerFactory().get_logger()
app_config = AppConfigManager().get_app_config()
docwb_telemetry.docwb_telemetry_jar_home = os.environ.get(
    "INFY_DOCWB_TELEMETRY_JAR_HOME", "")
MASTER_CONFIG_COMP_NAME = 'pre_processor'
SERVICE_NAME = app_config['DEFAULT']['service_name']


class TelemetryProcess():
    """Class for telemetry event process"""

    def __init__(self, master_config) -> None:
        producer_data = {
            "id": "workflow",
            "pid": SERVICE_NAME,
            "ver": app_config['DEFAULT']['service_version']
        }
        self.__telemetry_enabled = master_config[MASTER_CONFIG_COMP_NAME].get(
            'telemetry_enabled', False)
        self.__telemetry_event_duration = {}
        actor_id = f"[APP]-{SERVICE_NAME}"
        if self.__telemetry_enabled:
            telemetry_config_data = master_config['default']['telemetry_settings']
            if not telemetry_config_data["configData"]["actor"].get("id"):
                telemetry_config_data["configData"]["actor"]["id"] = actor_id
            if not telemetry_config_data["configData"]["context"].get("sid"):
                telemetry_config_data["configData"]["context"]["sid"] = FileUtil.get_uuid(
                )
            telemetry_config_data["configData"]["context"]["cdata"] += [
                {"type": "maker", "id": actor_id},
                {"type": "checker", "id": actor_id}
            ]
            self.__docwb_telemetry: DocwbTelemetry = DocwbTelemetry(
                telemetry_config_data, producer_data)

    def post_telemetry_event_log(self, request_id, doc_id, log_level: LogLevel = LogLevel.INFO, message: str = '',
                                 params: dict = {}, additional_config_param: dict = None):
        if not self.__telemetry_enabled:
            return
        start = time.time()
        tele_data = self.__get_telemetry_data(
            request_id, doc_id, additional_config_param)
        page_id = f'{SERVICE_NAME}/#/{tele_data["page_id"]}'
        if log_level.value == LogLevel.ERROR.value:
            params["id"] = app_config['TELEMETRY']['LOG_EVENT_FAILURE_ID']
        else:
            params["id"] = app_config['TELEMETRY']['LOG_EVENT_SUCCESS_ID']
        event_data: EVENT_DATA = {
            "type": "workflow",
            "level": log_level.value,
            "message": message,
            "pageid": page_id,
            "params": params
        }
        try:
            result = self.__docwb_telemetry.execute(
                TelemetryEvent.LOG, event_data, tele_data['context_data'],
                additional_config_param=additional_config_param)
            logger.info(f"Telemetry result -  {result}")
        except Exception:
            full_trace_error = traceback.format_exc()
            logger.error(full_trace_error)
        logger.info(f"Telemetry elapsed time in secs {time.time() - start}")

    def post_telemetry_event_start(self, request_id: str, doc_id: str, additional_config_param: dict):
        if not self.__telemetry_enabled:
            return
        start = time.time()
        tele_data = self.__get_telemetry_data(
            request_id, doc_id, additional_config_param)
        page_id = f'{SERVICE_NAME}/#/{tele_data["page_id"]}'
        self.__telemetry_event_duration[page_id] = start
        event_data: EVENT_DATA = {
            "type": "workflow",
            "pageid": page_id,
            "duration": start
        }
        try:
            result = self.__docwb_telemetry.execute(
                TelemetryEvent.START, event_data, context_data_list=tele_data['context_data'],
                additional_config_param=additional_config_param)
            logger.info(f"Telemetry result -  {result}")
        except Exception:
            full_trace_error = traceback.format_exc()
            logger.error(full_trace_error)
        logger.info(f"Telemetry elapsed time in secs {time.time() - start}")

    def post_telemetry_event_end(self,  request_id: str, doc_id: str, additional_config_param: dict):
        if not self.__telemetry_enabled:
            return
        start = time.time()
        tele_data = self.__get_telemetry_data(
            request_id, doc_id, additional_config_param)
        page_id = f'{SERVICE_NAME}/#/{tele_data["page_id"]}'
        elapse_time = start-self.__telemetry_event_duration[page_id]
        event_data: EVENT_DATA = {
            "type": "workflow",
            "pageid": page_id,
            "contentId": page_id,
            "duration": elapse_time
        }
        try:
            result = self.__docwb_telemetry.execute(
                TelemetryEvent.END, event_data, context_data_list=tele_data['context_data'],
                additional_config_param=additional_config_param)
            logger.info(f"Telemetry result -  {result}")
        except Exception:
            full_trace_error = traceback.format_exc()
            logger.error(full_trace_error)
        logger.info(f"Telemetry elapsed time in secs {time.time() - start}")

    def __get_telemetry_data(self, request_id, doc_id, additional_config_param: dict):
        tel_data = {
            "page_id": f"{request_id}/{doc_id}",
            "context_data": [{"type": "request_id", "id": request_id}, {"type": "doc_id", "id": doc_id},
                             {"type": "tenantId", "id": additional_config_param.get('telemetry').get('tenant_id')}]
        }
        return tel_data
