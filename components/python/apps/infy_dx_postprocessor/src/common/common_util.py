# ===============================================================================================================#
# Copyright 2022 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

import os
import uuid
from importlib import import_module


class Singleton(type):
    _instances = {}

    def __call__(cls, *args, **kwargs):
        if cls not in cls._instances:
            cls._instances[cls] = super(
                Singleton, cls).__call__(*args, **kwargs)
        return cls._instances[cls]


class CommonUtil:
    @classmethod
    def get_new_uuid(cls):
        return str(uuid.uuid1())

    @classmethod
    def get_new_short_uuid(cls):
        return CommonUtil.get_new_uuid()[:8]

    @classmethod
    def get_attr_id(cls, doc_id: str):
        return f"{doc_id[:8]}_{CommonUtil.get_new_short_uuid()}"

    @classmethod
    def get_attr_val_id(cls, attr_id: str):
        return f"{attr_id}_{CommonUtil.get_new_short_uuid()}"

    @classmethod
    def get_rule_class_instance(cls, rc_module_name: str, rc_entity_name: str = None):

        # rc_module_name = reduce(lambda x, y: x + ('_' if y.isupper()
        #                                           else '') + y, rc_module_name).lower()
        rc_name = "".join([str(x).title() for x in rc_module_name.split('_')])
        if rc_entity_name:
            rule_mudule = import_module(
                f"rules.{rc_entity_name}.{rc_module_name}")
        else:
            rule_mudule = import_module(f"rules.{rc_module_name}")
        rule_class = getattr(rule_mudule, rc_name)

        return rule_class

    @classmethod
    def update_app_info(self, req_res_dict: dict, about_app: dict):
        req_res_dict.update(about_app)
        new_record_list = []
        for record in req_res_dict['records']:
            new_record = {**{'workflow': []}, **record}
            workflow_list = new_record.get("workflow")
            is_exist = any(x['service_name'] == about_app['service_name'] and x['service_version'] == about_app['service_version']
                           for x in workflow_list if x.get('service_name'))
            if not is_exist:
                workflow_list.append(about_app)
            new_record_list.append(new_record)
        req_res_dict['records'] = new_record_list
