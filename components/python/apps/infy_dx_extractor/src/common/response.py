# ===============================================================================================================#
# Copyright 2022 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

from common.app_const import *


class Response:

    @staticmethod
    def response(atr_id, attr_name, error=None, warn=None, info=None,
                 vals=[], rd_found=[], rd_not_found=[], debug_info=[]):
        if len(vals) < 1:
            error = "No data extracted for attribute: "+attr_name
        return {
            ResProp.ID: atr_id,
            ResProp.ATTR_NAME: attr_name,
            ResProp.MSG: {ResProp.ERROR: error, ResProp.WARN: warn, ResProp.INFO: info},
            ResProp.VALS: vals,
            ResProp.ADD_DATA: {ResProp.RD_FND: rd_found,
                               ResProp.RD_NT_FND: rd_not_found,
                               ResProp.DEBUG_INFO: debug_info
                               }}

    @staticmethod
    def val_structure(val_id=None, type_obj=None, error=None, warn=None, info=None,
                      val=None, bbox=None, if_handwritten=None, page=None, key_text=None,
                      confidence=None, rd_found=None, extraction_technique=None):

        if type_obj == ValType.CHECKBOX or type_obj == ValType.RADIO:
            val_type = ResProp.SLCTMRK_OBJ
        elif type_obj == ValType.TXT:
            val_type = ResProp.TXT_OBJ
        else:
            val_type = ResProp.TAB_OBJ

        bbox = bbox if bbox and isinstance(bbox, list) else []

        if error:
            type_obj = None
            ext_tech = {}
            add_data = {}
        else:
            ext_tech = {
                ResProp.DEF: {
                    ResProp.CONF: confidence,
                    ResProp.SLCTD: True
                }
            } if not extraction_technique else extraction_technique
            add_data = {
                ResProp.RD_FND: rd_found,
            }
            if val_type == ResProp.TXT_OBJ:
                type_obj = [{
                    ResProp.ID: val_id,
                    ResProp.TXT: val,
                    ResProp.CONF: confidence,
                    ResProp.BBOX: bbox,
                    ResProp.HNDWRTN: if_handwritten,
                    ResProp.PAGE: page
                }]
            elif val_type == ResProp.SLCTMRK_OBJ:
                # default confidence score.
                type_obj = [{
                    ResProp.ID: val_id,
                    ResProp.TXT: key_text,
                    ResProp.CONF: 90,
                    ResProp.STATE: val,
                    ResProp.HNDWRTN: if_handwritten,
                    ResProp.BBOX: bbox,
                    ResProp.PAGE: page
                }]
            else:
                val_type = [{
                    ResProp.ID: val_id,
                    "value": val
                }]

        return [{
            ResProp.MSG: {
                ResProp.ERROR: error,
                ResProp.WARN: warn,
                ResProp.INFO: info
            },
            ResProp.TYPE: val_type,
            val_type: type_obj,
            ResProp.ADD_DATA: add_data,
            ResProp.EXT_TECH: ext_tech
        }]
