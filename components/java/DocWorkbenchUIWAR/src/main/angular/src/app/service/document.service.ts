/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { DocumentData } from '../data/document-data';
import { DocumentUserData } from '../data/document-user-data';
import { DocCountData } from '../data/doccount-data';
import { UserData } from '../data/user-data';
import { CONSTANTS } from '../common/constants';
import { EnumTaskStatus } from '../common/task-status.enum';
import { ConfigDataHelper } from '../utils/config-data-helper';
import { SessionService } from './session.service';
import { ToastrService } from 'ngx-toastr';


export class GetDocListReqData {

  constructor(
    public queueNameCde?: number,
    public taskStatusCde?: number,
    public taskStatusOperator?: string,
    public lockStatusCde?: number,
    public pageNumber?: number,
    public appUserId?: number,
    public attrNameCdes?: string,
    public attachmentAttrNameCdes?: string,
    public searchText?: string,
    public fileNameKey?: string,
    public fileNameVal?: string,
    public assignedToVal?: string,
    public assignedToKey?: string,
    public fromDate?: string,
    public toDate?: string,
    public caseVal?: string,
    public caseKey?: string,
    public sortByAttrNameCde?: string,
    public sortOrder?: string
  ) { }
}
@Injectable()
export class DocumentService {

  constructor(private httpClient: HttpClient, private configDataHelper: ConfigDataHelper,
    private sessionService: SessionService, private toastr: ToastrService) { }



  getDocumentList(requestData: GetDocListReqData, callback) {

    const parent = this;
    if (!parent.sessionService.getFeatureAccessModeDataFor(CONSTANTS.FEATURE_ID_CONFIG.CASE_LIST).isVisible) {
      callback(true, null);
      return;
    }
    let url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) +
      CONSTANTS.APIS.DOCWBSERVICE.GET_DOCUMENT_LIST;
    let queryParams = '';
    queryParams += 'appUserId=' + requestData.appUserId;
    //searchCriteria

    if (requestData.searchText && !requestData.caseKey) {
      queryParams = '';
      queryParams += '&toEventDtm=' + requestData.toDate;
      queryParams += '&fromEventDtm=' + requestData.fromDate;
      if (requestData.assignedToVal && requestData.assignedToKey
        && requestData.fileNameVal && requestData.fileNameKey) {
        queryParams += '&searchCriteria=' + requestData.assignedToKey + ':' + requestData.assignedToVal
          + ';' + requestData.fileNameKey + ':' + requestData.fileNameVal;
      } else if (requestData.assignedToVal && requestData.assignedToKey) {
        queryParams += '&searchCriteria=' + requestData.assignedToKey + ':' + requestData.assignedToVal;
      }
      else if (requestData.fileNameVal && requestData.fileNameKey) {
        queryParams += '&searchCriteria=' + requestData.fileNameKey + ':' + requestData.fileNameVal;
      }
      else {
        queryParams += '&searchCriteria=' + requestData.searchText;
      }

    }

    queryParams += '&queueNameCde=' + requestData.queueNameCde;

    if (requestData.pageNumber >= 0) {
      queryParams += '&pageNumber=' + requestData.pageNumber;
    }

    queryParams += '&attrNameCdes=' + requestData.attrNameCdes;
    queryParams += '&attachmentAttrNameCdes=' + requestData.attachmentAttrNameCdes;

    if (requestData.taskStatusCde > EnumTaskStatus.UNDEFINED && requestData.taskStatusOperator != null) {
      queryParams += '&taskStatusCde=' + requestData.taskStatusCde;
      queryParams += '&taskStatusOperator=' + requestData.taskStatusOperator;
    }
    if (requestData.lockStatusCde > 0) {
      queryParams += '&lockStatusCde=' + requestData.lockStatusCde;
    }
    if (requestData.searchText && requestData.caseKey) {
      if (requestData.caseVal) {
        //queryParams += '&docId=' + requestData.caseVal;
        queryParams += '&searchCriteria=' + requestData.caseKey + ':' + requestData.caseVal;
      } else {
        //queryParams = '';
        parent.toastr.error("Please provide case number. Format expected for Case# Search is key:value");
      }
    }
    if (requestData.sortOrder != '') {
      queryParams += '&sortOrder=' + requestData.sortOrder;
    }
    if (requestData.sortByAttrNameCde != '') {
      queryParams += '&sortByAttrNameCde=' + requestData.sortByAttrNameCde;
    }

    url += '?' + queryParams;

    const docSubscription = this.httpClient.get(url)
      .subscribe(
        data => {
          const documentDataList = data;
          callback(null, documentDataList);
        },
        error => {
          callback(error, null);
        }
      );
    return docSubscription;
  }

  getDocumentData(queueNameCde: number, docId: number) {
    const parent = this;
    return new Promise(function (fulfilled, rejected) {
      let url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) +
        CONSTANTS.APIS.DOCWBSERVICE.GET_DOCUMENT_LIST;
      let queryParams = '';
      queryParams += 'queueNameCde=' + queueNameCde;
      if (docId > 0) {
        queryParams += '&docId=' + docId;
      }
      url += '?' + queryParams;
      parent.httpClient.get(url)
        .subscribe(
          data => {
            const documentData: DocumentData[] = data['response'];
            fulfilled(documentData);
          },
          error => {
            rejected(error);
          }
        );
    });
  }

  getDocumentUserData(docId: number, inputDocUserData?:DocumentUserData[] ) {
    const parent = this;
    return new Promise(function (fulfilled, rejected) {
      if (inputDocUserData!=undefined && inputDocUserData.length>0){
        fulfilled(inputDocUserData);
      }else{
        let url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) +
          CONSTANTS.APIS.DOCWBSERVICE.GET_DOCUMENT_USER_LIST;
        let queryParams = 'docId=' + docId;
        url += '?' + queryParams;
        parent.httpClient.get(url)
          .subscribe(
            data => {
              const documentUserData: DocumentUserData[] = data['response'];
              fulfilled(documentUserData);
            },
            error => {
              rejected(error);
            }
          );
      }
    });
  }

  getDocCount(assignmentCount: boolean, queueNameCde: number, assignedTo: string, callback) {

    const parent = this;
    let url = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) +
      CONSTANTS.APIS.DOCWBSERVICE.GET_QUEUE_COUNT;

    let queryParams = [];
    if (assignmentCount !== null)
      queryParams.push('assignmentCount=' + assignmentCount);
    if (queueNameCde !== null)
      queryParams.push('queueNameCde=' + queueNameCde);
    if (assignedTo !== null)
      queryParams.push('assignedTo=' + assignedTo);
    if (queryParams.length > 0) {
      url += '?' + queryParams[0];
      for (var i = 1; i < queryParams.length; i++) {
        url += '&' + queryParams[i];
      }
    }
    this.httpClient.get(url)
      .subscribe(
        data => {
          const docCountList: DocCountData[] = data['response'];
          callback(null, docCountList);
        },
        error => {
          callback(error, null);
        }
      );
  }

  assignUserToDoc(prevAppUserId: number, appUserId: number, docId: number, callback,) {
    const parent = this;
    const url = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) +
      CONSTANTS.APIS.DOCWBSERVICE.ADD_USER_TO_DOC;
    console.log(prevAppUserId, docId, appUserId);
    const data = [{
      'appUserId': appUserId,
      'docAppUserRelId': 0,
      'docId': docId,
      'prevAppUserId': prevAppUserId
    }];
    this.httpClient.post(url,
      data
    ).subscribe(
      data => {
        callback(null, data);
      },
      error => {
        callback(error, null);
      }
    );
  }

  assignUserToDocPromise(prevAppUserId: number, appUserId: number, docId: number, docRoleTypeCde: number) {
    const parent = this;
    return new Promise(function (fulfilled, rejected) {
      const url = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) +
        CONSTANTS.APIS.DOCWBSERVICE.ADD_USER_TO_DOC;
      console.log(prevAppUserId, docId, appUserId);
      const data = [{
        'appUserId': appUserId,
        'docAppUserRelId': 0,
        'docId': docId,
        'prevAppUserId': prevAppUserId,
        'docRoleTypeCde': docRoleTypeCde
      }];
      parent.httpClient.post(url,
        data
      ).subscribe(
        data => {
          fulfilled(data);
        },
        error => {
          rejected(error);
        }
      );
    });
  }

  closeDocCase(docId: number, appUserId: number, queueNameCde: number, callback) {
    const parent = this;
    let url = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) +
      CONSTANTS.APIS.DOCWBSERVICE.CLOSE_CASE_FOR_DOC;
    let queryParams = '';

    queryParams += 'queueNameCde=' + queueNameCde;

    url += '?' + queryParams;
    const data = [{

      'appUserId': appUserId,
      'docAppUserRelId': 0,
      'docId': docId,
      'prevAppUserId': 0

    }];

    this.httpClient.post(url,
      data
    ).subscribe(
      data => {
        callback(null, data);
      },
      error => {
        callback(error, null);
      }
    );
  }

  getQueueUsersList(queueNameCde: number, callback) {
    const parent = this;
    let url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) +
      CONSTANTS.APIS.DOCWBSERVICE.GET_USER_LIST_FOR_QUEUE;
    let userList: UserData[];
    let queryParams = '';

    queryParams += 'queueNameCde=' + queueNameCde;

    url += '?' + queryParams;
    this.httpClient.get(url)
      .subscribe(
        data => {
          userList = data['response'];
          callback(null, userList);
        },
        error => {
          callback(error, null);
        }
      );
    return null;
  }

  getQueueUsersListPromise(queueNameCde: number) {
    const parent = this;
    return new Promise(function (fulfilled, rejected) {
      let url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) +
        CONSTANTS.APIS.DOCWBSERVICE.GET_USER_LIST_FOR_QUEUE;
      let userList: UserData[];
      let queryParams = '';

      queryParams += 'queueNameCde=' + queueNameCde;

      url += '?' + queryParams;
      parent.httpClient.get(url)
        .subscribe(
          data => {
            userList = data['response'];
            fulfilled(userList);
          },
          error => {
            rejected(error);
          }
        );
    });
  }

  getNextOrPrevId(appUserId, queueNameCde: number, taskStatusCde: number, taskStatusOperator: string,
    sortOrder: string, searchCriteria: string, callback) {
    const parent = this;
    let url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) +
      CONSTANTS.APIS.DOCWBSERVICE.GET_DOCUMENT_LIST;
    let queryParams = '';
    queryParams += 'appUserId=' + appUserId;
    queryParams += '&queueNameCde=' + queueNameCde;
    queryParams += '&sortOrder=' + sortOrder;
    queryParams += '&searchCriteria=' + searchCriteria;
    queryParams += '&taskStatusCde=' + taskStatusCde;
    queryParams += '&taskStatusOperator=' + taskStatusOperator;

    url += '?' + queryParams;
    const NextDocumentId = this.httpClient.get(url)
      .subscribe(
        data => {
          const nextId: DocumentData[] = data['response'];
          callback(null, nextId);
        },
        error => {
          callback(error, null);
        }
      );
    return NextDocumentId;
  }
}
