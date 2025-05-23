/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { RecommendedActionData } from '../data/recommended-action-data';
import { CONSTANTS } from '../common/constants';
import { ConfigDataHelper } from '../utils/config-data-helper';


@Injectable()
export class RecommendedActionService {

  constructor(private httpClient: HttpClient, private configDataHelper: ConfigDataHelper) { }

  getRecommendedAction(docId: number, callback) {
    const parent = this;
    let url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) +
     CONSTANTS.APIS.DOCWBSERVICE.GET_RECOMMENDED_ACTION;
    let queryParams: string = "";
    if (docId) {
      queryParams += "docId=" + docId;
    }
    if (queryParams.length > 0) {
      url += "?" + queryParams;
    }

    this.httpClient.get(url)
      .subscribe(
        data => {
          let recommendedActionDataList : RecommendedActionData[] = data['response'];
          callback(null, recommendedActionDataList);
        },
        error => {
          callback(error, null);
        }
      );
    return null;
  }

  getRecommendedActionPromise() {
    const parent = this;
    return new Promise(function (fulfilled, rejected) {

      let url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) +
       CONSTANTS.APIS.DOCWBSERVICE.GET_RECOMMENDED_ACTION;
      parent.httpClient.get(url)
        .subscribe(data => {
          let recommendedActionData: RecommendedActionData = data['response'];
          fulfilled(recommendedActionData);
        },
          error => {
            rejected(null);
          });
    });
  }

}
