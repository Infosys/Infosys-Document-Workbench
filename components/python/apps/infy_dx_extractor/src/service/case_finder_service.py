# ===============================================================================================================#
# Copyright 2023 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#


from infy_docwb_case_finder.docwb_case_finder import DocwbCaseFinder
from infy_docwb_case_finder.data.config_param_data import ConfigParamData
from infy_docwb_case_finder.data.config_param_data import DocwbConfigData

from common.common_util import Singleton


class CaseFinderService(metaclass=Singleton):
    def __init__(self) -> None:
        self.__tenant_id_map = {}

    def get_instance(self, tenant_id) -> DocwbCaseFinder:
        return self.__tenant_id_map.get(tenant_id)

    def create_instance(self, tenant_id, service_config):
        if not self.__tenant_id_map.get(tenant_id):
            self.__tenant_id_map[tenant_id] = DocwbCaseFinder(ConfigParamData(DocwbConfigData(
                end_point=service_config.get('end_point'), username=service_config.get('username'),
                password=service_config.get('password'), tenant_id=tenant_id)))
