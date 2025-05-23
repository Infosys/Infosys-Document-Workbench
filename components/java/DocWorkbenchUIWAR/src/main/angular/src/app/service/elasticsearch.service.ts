/** =============================================================================================================== *
 * Copyright 2022 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Injectable } from '@angular/core';
import { ConfigDataHelper } from '../utils/config-data-helper';
import { CONSTANTS } from '../common/constants';
import { HttpClient } from '@angular/common/http';
import { QueryData } from '../data/query-data';

@Injectable()
export class ElasticsearchService {

  constructor(private httpClient: HttpClient, private configDataHelper: ConfigDataHelper) {
  }

  executeQuery(queryData: QueryData) {
    const parent = this;
    let url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBWEB_BASE_URL) + CONSTANTS.APIS.DOCWBWEB.QUERY_ES;
    return new Promise(function (fulfilled, rejected) {
      parent.httpClient.post(url,
        queryData
      ).subscribe(
        _data => {
          fulfilled(JSON.parse(_data['response']));
        },
        _error => {
          rejected(_error);
        }
      );
    });
  }
}
