/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CONSTANTS } from '../common/constants';
import { ConfigDataHelper } from '../utils/config-data-helper';
@Injectable()
export class AppVariableService {
    constructor(private httpClient: HttpClient, private configDataHelper: ConfigDataHelper) { }
    getAppVariableDataPromise(appVarKey:string) {
        const parent = this;
        const url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) +
            CONSTANTS.APIS.DOCWBSERVICE.GET_APP_VARIABLE_DATA+"/"+appVarKey;
        return new Promise(function (fulfilled, rejected) {
            parent.httpClient.get(url)
                .subscribe(data => {
                    fulfilled(data['response'] );
                },
                    error => {
                        rejected(error);
                    }
                );


        });
    }

    editAppVariablePromise(data) {
        const parent = this;
        const url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) +
            CONSTANTS.APIS.DOCWBSERVICE.EDIT_APP_VARIABLE_DATA;
        return new Promise(function (fulfilled, rejected) {
            parent.httpClient.post(url,
                data
            ).subscribe(
                _data => {
                    fulfilled(_data);
                },
                _error => {
                    rejected(_error);
                }
            );
        });
    }

}
