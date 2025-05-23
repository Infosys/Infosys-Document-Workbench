/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { TokenData } from '../data/token-data';
import { ResponseData } from '../data/response-data';
import { SessionService } from './session.service';
import { CONSTANTS } from '../common/constants';
import { UtilityService } from './utility.service';
import { ConfigDataHelper } from '../utils/config-data-helper';

@Injectable()
export class AuthenticationService {

  private userTokenDataKey: string;

  constructor(private httpClient: HttpClient, public sessionService: SessionService,
    private utilityService: UtilityService, private configDataHelper: ConfigDataHelper) {
    const browserUrl = location.pathname;
    this.userTokenDataKey = 'tokenData-' + browserUrl.replace(/\//g, '').replace('index.html', '').split(';')[0];
  }

  login(username: string, password: string, tenantId: string) {
    const parent = this;
    const tokenData: TokenData = JSON.parse(localStorage.getItem(parent.userTokenDataKey));
    if (tokenData && tokenData.token != null && tokenData.token.length > 0) {
      parent.cleanupClient();
    }
    return new Promise(function (fulfilled, rejected) {
      const url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) +
        CONSTANTS.APIS.DOCWBSERVICE.GET_AUTHORIZATION_TOKEN;
      const dataInput = {
        'authorization': 'Basic ' + btoa(username + ':' + password),
        'tenantId': tenantId

      };
      parent.httpClient.post(url, dataInput).subscribe(data => {
        const responseData: ResponseData = data as ResponseData;
        if (responseData && responseData.response) {
          const tokenData1: TokenData = responseData.response;
          if (tokenData1 && tokenData1.token != null && tokenData1.token.length > 0) {
            localStorage.setItem(parent.userTokenDataKey, JSON.stringify(tokenData1));

          } else {
            localStorage.removeItem(parent.userTokenDataKey);
            rejected(responseData.responseMsg);
          }
        } else {
          rejected(responseData.responseMsg);
        }
        fulfilled(true);
      }, error => {
        localStorage.removeItem(parent.userTokenDataKey);
        rejected(error);
      });
    });
  }


  cleanupClient() {
    this.sessionService.terminateClientSession();
    // remove from local storage
    localStorage.removeItem(this.userTokenDataKey);
  }

  isUserLoggedIn() {
    if (this.getAuthToken()) {
      return true;
    }
    return false;
  }

  getAuthToken() {
    const tokenData: TokenData = JSON.parse(localStorage.getItem(this.userTokenDataKey));
    if (tokenData && tokenData.token) {
      const tokenExpiryDtm = new Date(tokenData.expiryDtm)
      const currentDtm = new Date()
      if (tokenExpiryDtm > currentDtm) {
        return tokenData.token;
      }
      return null;
    }
    return null;
  }

  deleteAuthToken() {
    localStorage.removeItem(this.userTokenDataKey);
  }
}
