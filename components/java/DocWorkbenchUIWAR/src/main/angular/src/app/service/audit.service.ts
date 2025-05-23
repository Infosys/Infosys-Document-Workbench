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
export class AuditService {

  constructor(private httpClient: HttpClient, private configDataHelper: ConfigDataHelper) { }

  getAuditForDocument(docId: number, pageNumber: number, callback) {
    const parent = this;
    let url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) + CONSTANTS.APIS.DOCWBSERVICE.GET_AUDIT_DOCUMENT;
    let queryParams: string = "";
    if (docId > 0) {
      queryParams += "docId=" + docId;
    }
    if (pageNumber != null) {
      queryParams += "&pageNumber=" + pageNumber;
    }
    if (queryParams.length > 0) {
      url += "?" + queryParams;
    }

    this.httpClient.get(url)
      .subscribe(
        data => {
          let auditDataList = data;
          callback(null, auditDataList);
        },
        error => {
          callback(error, null);
        }
      );
    return null;
  }

  getAuditForUser(appUserId: number, pageNumber: number, callback) {
    const parent = this;
    let url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) + CONSTANTS.APIS.DOCWBSERVICE.GET_AUDIT_USER;
    let queryParams: string = "";
    if (appUserId > 0) {
      queryParams += "appUserId=" + appUserId;
    }
    if (pageNumber != null) {
      queryParams += "&pageNumber=" + pageNumber;
    }
    if (queryParams.length > 0) {
      url += "?" + queryParams;
    }

    this.httpClient.get(url)
      .subscribe(
        data => {
          let auditDataList = data;
          callback(null, auditDataList);
        },
        error => {
          callback(error, null);
        }
      );
    return null;
  }

  getAuditForAppVariable(appVarKey:string, pageNumber: number, callback) {
    const parent = this;
    let url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) + CONSTANTS.APIS.DOCWBSERVICE.GET_AUDIT_APP_VARIABLE;
    let queryParams: string = "";
    if (appVarKey !=null) {
      queryParams += "appVariableKey=" + appVarKey;
    }
    if (pageNumber != null) {
      queryParams += "&pageNumber=" + pageNumber;
    }
    if (queryParams.length > 0) {
      url += "?" + queryParams;
    }

    this.httpClient.get(url)
      .subscribe(
        data => {
          let auditDataList = data;
          callback(null, auditDataList);
        },
        error => {
          callback(error, null);
        }
      );
    return null;
  }

  getDocLevelAuditForCurrentUser(pageNumber: number) {
    const parent = this;
    return new Promise(function (fulfilled, rejected) {
    let url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) + CONSTANTS.APIS.DOCWBSERVICE.GET_AUDIT_DOCUMENT_CURRENT_USER;
    let queryParams: string = "";
    
    if (pageNumber != null) {
      queryParams += "pageNumber=" + pageNumber;
    }
    if (queryParams.length > 0) {
      url += "?" + queryParams;
    }

    parent.httpClient.get(url)
      .subscribe(
        data => {
          let auditDataList = data['response'];
          console.log("auditDataList",auditDataList)
          fulfilled(auditDataList);
        },
        error => {
          rejected(error);
        }
      );
    });
  }
}





  


