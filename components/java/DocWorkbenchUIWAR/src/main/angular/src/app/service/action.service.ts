/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ActionData } from '../data/action-data';
import { DocumentData } from '../data/document-data';
import { CONSTANTS } from '../common/constants';
import { ConfigDataHelper } from '../utils/config-data-helper';
import { SessionService } from './session.service';

@Injectable()
export class ActionService {

  constructor(private httpClient: HttpClient, private configDataHelper: ConfigDataHelper,
    private sessionService:SessionService) { }

  getActionMapping(callback) {
    const parent = this;
    const url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) +
      CONSTANTS.APIS.DOCWBSERVICE.GET_ACTION_MAPPING;
    this.httpClient.get(url)
      .subscribe(
        data => {
          const actionDataList: ActionData[] = data['response'];
          callback(null, actionDataList);
        },
        error => {
          callback(error, null);
        }
      );
    return null;
  }

  saveAction(documentData: DocumentData[], callback) {
    const parent = this;
    const url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) +
      CONSTANTS.APIS.DOCWBSERVICE.SAVE_ACTION_LIST;
    // console.log("postData()");
    this.httpClient.post(url,
      documentData
    ).subscribe(
      data => {
        callback(null, data);
      },
      error => {
        callback(error, null);
      }
    );
  }

  updateAction(documentData: DocumentData[]) {
    const parent = this;
    const url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) +
      CONSTANTS.APIS.DOCWBSERVICE.SAVE_ACTION_LIST;
    return new Promise(function (fulfilled, rejected) {
      parent.httpClient.put(url,
        documentData
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

  getActions(docId: number, callback) {
    const parent = this;
    if (!parent.sessionService.getFeatureAccessModeDataFor(CONSTANTS.FEATURE_ID_CONFIG.ACTION_VIEW).isVisible) {
      callback(true, null);
    } else {
      let url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) +
        CONSTANTS.APIS.DOCWBSERVICE.GET_ACTION_LIST;
      let queryParams = '';
      if (docId > 0) {
        queryParams += 'docId=' + docId;
      }
      if (queryParams.length > 0) {
        url += '?' + queryParams;
      }

      this.httpClient.get(url)
        .subscribe(
          data => {
            const actionDataList: ActionData[] = data['response'];
            callback(null, actionDataList);
          },
          error => {
            callback(error, null);
          }
        );
    }
    return null;
  }

}
