# ===============================================================================================================#
# Copyright 2023 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#
import infy_fs_utils
import infy_dpp_sdk
import time
import datetime
import calendar
from typing import List
import uvicorn
from fastapi import Depends, FastAPI, Request, status
from fastapi.exceptions import RequestValidationError
from fastapi.responses import PlainTextResponse
from fastapi.middleware.cors import CORSMiddleware
from common.ainauto_logger_factory import AinautoLoggerFactory
from common.app_config_manager import AppConfigManager
from dependencies import basic_authorize
from routes.api import router as api_router
from fastapi.responses import JSONResponse
from fastapi.encoders import jsonable_encoder

app_config = AppConfigManager().get_app_config()
logger = AinautoLoggerFactory().get_logger()

STORAGE_ROOT_PATH = app_config['STORAGE']['STORAGE_ROOT_PATH']
CONTAINER_ROOT_PATH = app_config['CONTAINER']['CONTAINER_ROOT_PATH']
storage_config_data = infy_fs_utils.data.StorageConfigData(
    **{
        "storage_root_uri": f"file://{STORAGE_ROOT_PATH}",
        "storage_server_url": "",
        "storage_access_key": "",
        "storage_secret_key": ""
    })

if not infy_fs_utils.manager.FileSystemManager().has_fs_handler(infy_dpp_sdk.common.Constants.FSH_DPP):
    infy_fs_utils.manager.FileSystemManager().add_fs_handler(
        infy_fs_utils.provider.FileSystemHandler(storage_config_data), infy_dpp_sdk.common.Constants.FSH_DPP)
# Configure client properties
client_config_data = infy_dpp_sdk.ClientConfigData(
    **{
        "container_data": {
            "container_root_path": f"{CONTAINER_ROOT_PATH}",
        }
    })
infy_dpp_sdk.ClientConfigManager().load(client_config_data)


app = FastAPI(title='Infosys docwb search service',
              #   dependencies=[Depends(basic_authorize)],
              openapi_url="/api/v1/model/openapi.json", docs_url="/api/v1/model/docs",
              description="Infosys docwb search service",
              version=app_config['DEFAULT']['service_version'])

origins = [
    "*"
]


# @app.exception_handler(RequestValidationError)
# async def validation_exception_handler(request, exc):
#     return PlainTextResponse(str(exc), status_code=400)


@app.exception_handler(RequestValidationError)
def http_exception_handler(request: Request, exc: RequestValidationError):
    start_time = time.time()
    date = datetime.datetime.utcnow()
    utc_time = calendar.timegm(date.utctimetuple())
    date_time_stamp = datetime.datetime.fromtimestamp(
        utc_time).strftime("%Y-%m-%d %I:%M:%S %p")
    # print(exc.errors())
    response_list = []
    for index, err in enumerate(exc.errors()):
        print(err)
        if err['msg'] == 'extra fields not permitted':
            # err['msg'] = err['msg'] + ' location: ' + \
            #     err['loc'][0] + ' ,extra field: ' + err['loc'][1]
            field_loc_list = [loc for loc in err['loc']
                              if loc not in ['body', '__root__']]
            field_path = '.'.join(field_loc_list)
            err['msg'] = field_path + " " + err['msg']
            code = 1013
        elif err['msg'] == 'field required':
            field_loc_list = [loc for loc in err['loc']
                              if loc not in ['body', '__root__']]
            field_path = '.'.join(field_loc_list)
            err['msg'] = field_path + " " + err['msg']
            code = 1015
        else:
            code = err['type'].split('.')[1]
        response_dict = {
            "code": code,
            "message": err['msg']
        }
        response_list.append(response_dict)
        # TODO: It's defect-multiple error return only last error raised
        break
    elapsed_time = round(time.time() - start_time, 3)
    # response = GetProjectIdResultData(response=response_list,
    #                                   responseCde=999, responseMsg="Failure",
    #                                   timestamp=date_time_stamp, responseTimeInSecs=(elapsed_time))
    response = None
    json_compatible_data = jsonable_encoder(response)
    return JSONResponse(content=json_compatible_data)


app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(api_router)


# @app.get("/")
# def hello_world():
#     return 'Infosys Document Data Extraction Service'


if __name__ == '__main__':
    uvicorn.run("main:app", host="0.0.0.0", port=int(app_config['DEFAULT']['port']),
                log_level=int(app_config['DEFAULT']['logging_level']), reload=True)
    logger.info("App is running....")
