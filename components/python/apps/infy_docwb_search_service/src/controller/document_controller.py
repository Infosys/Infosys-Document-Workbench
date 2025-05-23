# ===============================================================================================================#
# Copyright 2025 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

import os
import json
import infy_fs_utils
import infy_dpp_sdk
from fastapi import APIRouter, HTTPException
from data.api_schema_data import ResponseData, DocumentData
from common.app_config_manager import AppConfigManager
from fastapi.responses import FileResponse
from starlette.responses import Response


router = APIRouter(prefix="/api/v1/documents",
                   responses={404: {"description": "Not found"}})


@router.post("/doc", tags=["documents"],
             summary="Fetch a document by filename")
async def get_document(file_name: DocumentData):
    app_config = AppConfigManager().get_app_config()
    file_sys_handler = infy_fs_utils.manager.FileSystemManager(
    ).get_fs_handler(infy_dpp_sdk.common.Constants.FSH_DPP)
    config_file_path = app_config['STORAGE']["dpp_input_config_file_path"]
    input_config_data = json.loads(
        file_sys_handler.read_file(config_file_path))
    folder_path = input_config_data['variables']['RESOURCE_PATH']
    storage_root_path = app_config['STORAGE']['STORAGE_ROOT_PATH']

    file_path = os.path.join(
        storage_root_path, folder_path.lstrip('/'), file_name.file_name)
    file_path = os.path.normpath(file_path)

    if not os.path.exists(file_path):
        raise HTTPException(status_code=404, detail="File not found")

    file_name = os.path.basename(file_path)
    file_header = f'inline; filename="{file_name}"'
    return FileResponse(file_path, media_type='application/octet-stream', headers={'Content-Disposition': file_header})
