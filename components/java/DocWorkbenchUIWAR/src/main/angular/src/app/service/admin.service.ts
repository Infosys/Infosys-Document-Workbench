/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { UserData } from '../data/user-data';
import { QueueData } from '../data/queue-data';
import { CONSTANTS } from '../common/constants';
import { ConfigDataHelper } from '../utils/config-data-helper';
import { SessionService } from './session.service';

@Injectable()
export class AdminService {

  constructor(private httpClient: HttpClient, private configDataHelper: ConfigDataHelper,
    private sessionService: SessionService) { }

  getUserList(callback) {
    const parent = this;
    let url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) + CONSTANTS.APIS.DOCWBSERVICE.GET_USER_LIST;
    let userList: UserData[];
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


  getUserListPromise() {
    const parent = this;
    return new Promise(function (fulfilled, rejected) {
      if (!parent.sessionService.getFeatureAccessModeDataFor(CONSTANTS.FEATURE_ID_CONFIG.USER_LIST).isVisible) {
        rejected();
      } else {
        let url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) + CONSTANTS.APIS.DOCWBSERVICE.GET_USER_LIST;
        parent.httpClient.get(url)
          .subscribe(data => {
            let userData: UserData = data['response'];
            fulfilled(userData);
          },
            error => {
              rejected(null);
            });
      }
    });

  }

  getQueueListPromise() {
    const parent = this;
    return new Promise(function (fulfilled, rejected) {

      let url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) + CONSTANTS.APIS.DOCWBSERVICE.GET_QUEUE_LIST;
      parent.httpClient.get(url)
        .subscribe(data => {
          let queueData: QueueData = data['response'];
          fulfilled(queueData);
        },
          error => {
            rejected(null);
          });
    });
  }


  saveRoleData(userId: number, roleTypeCde: number) {
    const parent = this;
    return new Promise(function (fulfilled, rejected) {

      let url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) + CONSTANTS.APIS.DOCWBSERVICE.EDIT_USER_ROLE;
      const data = [{
        "appUserId": userId,
        "userRoleType": roleTypeCde

      }];
      parent.httpClient.post(url,
        data
      ).subscribe(
        data => {
          fulfilled(data);
        },
        error => {
          rejected(null);
        });
    });
  }


  saveUserEnabledData(userId: number, accountEnabled: boolean) {
    const parent = this;
    return new Promise(function (fulfilled, rejected) {

      let url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) +
        CONSTANTS.APIS.DOCWBSERVICE.EDIT_USER_ENABLED_DATA;
      const data =

      {
        "accountEnabled": accountEnabled,
        "loginId": "",
        "queueDataList": [
          {
            "appUserId": 0,
            "queueNameCde": 0,
            "queueNameTxt": ""
          }
        ],
        "roleTypeTxt": "",
        "roleTypeCde": 0,
        "userEmail": "",
        "userFullName": "",
        "userId": userId,
        "userName": "",
        "userTypeCde": 0,
        "userTypeTxt": ""
      };
      parent.httpClient.put(url,
        data
      ).subscribe(
        data => {
          fulfilled(data);
        },
        error => {
          rejected(null);
        });
    });
  }


  addUserQueueData(appUserId: number, queueNameCde: number) {
    const parent = this;
    return new Promise(function (fulfilled, rejected) {

      let url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) +
        CONSTANTS.APIS.DOCWBSERVICE.ADD_USER_QUEUE_DATA;
      const data =
      {
        "appUserId": appUserId,
        "appUserQueueRelId": 0,
        "queueNameCde": queueNameCde,
        "queueNameTxt": ""
      };
      parent.httpClient.post(url,
        data
      ).subscribe(
        data => {
          fulfilled(data);
        },
        error => {
          rejected(null);
        });
    });
  }


  deleteUserQueueData(appUserQueueRelId: number) {
    const parent = this;
    return new Promise(function (fulfilled, rejected) {

      let url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) +
        CONSTANTS.APIS.DOCWBSERVICE.DELETE_USER_QUEUE_DATA;
      let queryParams: string = "";

      queryParams += appUserQueueRelId;

      url += "/" + queryParams;
      parent.httpClient.delete(url)
        .subscribe(
          data => {
            fulfilled(data);
          },
          error => {
            rejected(null);
          }
        );
    });
  }

  getQueueListForUser(appUserId: number) {
    const parent = this;
    return new Promise(function (fulfilled, rejected) {

      let url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) +
        CONSTANTS.APIS.DOCWBSERVICE.GET_QUEUE_LIST_FOR_USER;
      let queryParams: string = "";

      queryParams += "appUserId=" + appUserId;
      url += "?" + queryParams;
      parent.httpClient.get(url)
        .subscribe(data => {
          let queueDataList = data;
          fulfilled(queueDataList);
        },
          error => {
            rejected(null);
          });
    });
  }
  getListOfTeammates() {
    const parent = this;
    return new Promise(function (fulfilled, rejected) {
      let url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) +
        CONSTANTS.APIS.DOCWBSERVICE.GET_LIST_OF_TEAMMATES;
      parent.httpClient.get(url)
        .subscribe(data => {
          let teammateDataList = data['response'];
          fulfilled(teammateDataList);
        },
          error => {
            rejected(null);
          });
    });

  }

  getPersonalQueueListPromise(queueStatus:string) {
    const parent = this;
    return new Promise(function (fulfilled, rejected) {
      let url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL)
        + CONSTANTS.APIS.DOCWBSERVICE.GET_CURRENT_USER_QUEUE_LIST;
      let queryParams: string = "";
      queryParams += "queueStatus=" + queueStatus;
      url += "?" + queryParams;
      parent.httpClient.get(url)
        .subscribe(data => {
          fulfilled(data['response']);
        },
          error => {
            rejected(null);
          });
    });
  }

  updateQueueVisibilityDate(queueNameCde: number, userQueueHideAfterDtm: String) {
    let parent = this;
    return new Promise(function (fulfilled, rejected) {

      let url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) +
        CONSTANTS.APIS.DOCWBSERVICE.EDIT_QUEUE_VISIBILITY_DATE;
      
      const data =[ {
        "queueNameCde": queueNameCde,
        "userQueueHideAfterDtm": userQueueHideAfterDtm
      }]
      parent.httpClient.put(url, data
      ).subscribe(
        data => {
          fulfilled(data);
        },
        error => {
          rejected(error);
        });
    });
  }
  updateQueueClosureDetails(queueNameCde: number, queueHideAfterDate: String,queueClosedDate: String) {
    let parent = this;
    return new Promise(function (fulfilled, rejected) {

      let url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) +
        CONSTANTS.APIS.DOCWBSERVICE.EDIT_QUEUE_LIST;
      
      const data =[ {
        "queueNameCde": queueNameCde,
        "queueHideAfterDtm": queueHideAfterDate,
        "endDtm":queueClosedDate
      }]
      parent.httpClient.put(url, data
      ).subscribe(
        data => {
          fulfilled(data);
        },
        error => {
          rejected(error);
        });
    });
  }
}
