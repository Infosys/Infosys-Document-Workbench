/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { TempData } from '../data/temp-data';
import { CONSTANTS } from '../common/constants';
import { ConfigDataHelper } from '../utils/config-data-helper';

@Injectable()
export class TemplateService {

    constructor(private httpClient: HttpClient, private configDataHelper: ConfigDataHelper) { }

    getTemplateData(docId: number, callback) {
        const parent = this;
        let url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) +
         CONSTANTS.APIS.DOCWBSERVICE.GET_TEMPLATES + docId;
        this.httpClient.get(url)
            .subscribe(
                data => {
                    let templateDataList: TempData[] = data['response'];
                    callback(null, templateDataList);
                },
                error => {
                    callback(error, null);
                }
            );
        return null;
    }
}
