/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ValData } from '../data/val-data';
import { CONSTANTS } from '../common/constants';
import { ConfigDataHelper } from '../utils/config-data-helper';


@Injectable()
export class ValService {

    private valMap = new Map();

    constructor(private httpClient: HttpClient, private configDataHelper: ConfigDataHelper) { }

    /**
   *
   * @param entity val-type|extract-type|lock-status|task-status|action-name|attribute-name|param-name
   * @param callback
   */
    getValList(entity, callback) {
        const parent = this;

        if (!parent.valMap.has(entity)) {
            const url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) +
                CONSTANTS.APIS.DOCWBSERVICE.GET_STATUS_VAL + entity;
            this.httpClient.get(url)
                .subscribe(data => {
                    const valDataList: ValData[] = data['response'];
                    parent.valMap.set(entity, valDataList);
                    callback(null, valDataList);
                },
                    error => {
                        callback(error, null);
                    }
                );
        } else {
            callback(null, parent.valMap.get(entity));
        }
    }
}
