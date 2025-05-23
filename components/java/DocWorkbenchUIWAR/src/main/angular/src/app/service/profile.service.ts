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
export class ProfileService {

  constructor(private httpClient: HttpClient, private configDataHelper: ConfigDataHelper) { }

  changePassword(oldPassword: String, newPassword: String) {
    let parent = this;
    return new Promise(function (fulfilled, rejected) {

      let url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) +
        CONSTANTS.APIS.DOCWBSERVICE.EDIT_USER_PASSWRD;
      url += "?";
      // parent.httpClient.put(url, {params: {
      //   'oldPassword': oldPassword,
      //   'newPassword': newPassword
      // }}
      const data = {
        "oldPassword": oldPassword,
        "newPassword": newPassword
      }
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
