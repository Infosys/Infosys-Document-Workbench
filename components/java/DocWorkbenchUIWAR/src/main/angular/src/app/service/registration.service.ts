/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Injectable } from '@angular/core';
import { CONSTANTS } from '../common/constants';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { ConfigDataHelper } from '../utils/config-data-helper';

@Injectable()
export class RegistrationService {

  constructor(private httpClient: HttpClient, private configDataHelper: ConfigDataHelper) { }

  addNewUser(userEmail: String, userFullName: String, userName: String, userPassword: String, userTypeCde: Number,tenantId:string) {
    const parent = this;
    return new Promise(function (fulfilled, rejected) {

      let url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) + CONSTANTS.APIS.DOCWBSERVICE.ADD_NEW_USER;
      // url += "?";
      const data = {
        "userEmail": userEmail,
        "userFullName": userFullName,
        "userName": userName,
        "userPassword": userPassword,
        "userTypeCde": userTypeCde,
        "tenantId": tenantId
        // "userTypeTxt": userTypeTxt
      }
      parent.httpClient.post(url, data
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
