/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { UserData } from '../data/user-data';
import { CONSTANTS } from '../common/constants';
import { ConfigDataHelper } from '../utils/config-data-helper';
import { FeatureAccessMode } from '../data/feautre-access-mode';

@Injectable()
export class SessionService {

  private sessionDetails: UserData;
  private featureAuthMap = {};
  constructor(private httpClient: HttpClient, private configDataHelper: ConfigDataHelper) { }

  getLoggedInUserDetailsPromise() {
    const parent = this;
    return new Promise(function (fulfilled, rejected) {

      if (parent.sessionDetails == null) {
        let url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) +
          CONSTANTS.APIS.DOCWBSERVICE.GET_LOGGED_USER;
        // TODO: please enable below code at  line number 37, 68 to test access level combination for admin
        // url = "assets/mock-session-data.json";
        parent.httpClient.get(url)
          .subscribe(data => {
            if (data) {
              parent.sessionDetails = data['response'] as UserData;
              if (parent.sessionDetails.featureAuthDataList != null) {
                parent.sessionDetails.featureAuthDataList.map(item => {
                  parent.featureAuthMap[item.featureId] = item.accessLevelCde;
                });
              }
            }
            fulfilled(parent.sessionDetails);
          },
            error => {
              rejected(error);
            }
          );
        // return null;
      } else {
        fulfilled(parent.sessionDetails);
      }
    });
  }


  getLoggedInUserDetails(callback) {
    const parent = this;
    if (parent.sessionDetails == null) {
      let url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) +
        CONSTANTS.APIS.DOCWBSERVICE.GET_LOGGED_USER;
      // TODO: please enable below code at  line number 37, 68 to test access level combination for admin
      // url = "assets/mock-session-data.json";
      this.httpClient.get(url)
        .subscribe(data => {
          parent.sessionDetails = data['response'] as UserData;
          if (parent.sessionDetails.featureAuthDataList != null) {
            parent.sessionDetails.featureAuthDataList.map(item => {
              parent.featureAuthMap[item.featureId] = item.accessLevelCde;
            });
          }
          callback(null, parent.sessionDetails);
        },
          error => {
            callback(error, null);
          }
        );
      // return null;
    } else {
      callback(null, parent.sessionDetails);
    }
  }

  terminateServerSession() {
    console.log('terminateServerSession()')
    const parent = this;
    const url = this.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL)
      + CONSTANTS.APIS.DOCWBSERVICE.TERMINATE_SESSION;
    return new Promise(function (fulfilled, rejected) {
      parent.httpClient.post(url, null).subscribe(data => {
        fulfilled(data)
      } , error => {
        rejected(error)
      });
    });
  }

  terminateClientSession() {
    // Very important to clear all user related data
    this.sessionDetails = null;
    this.featureAuthMap = {};
  }


  getFeatureAccessModeDataFor(featureId: string): FeatureAccessMode {
    const parent = this;
    let featureAccessMode: FeatureAccessMode = new FeatureAccessMode();
    const accessLevelCde = parent.featureAuthMap[featureId];
    if (accessLevelCde != null) {
      const checkForRightToLeftBitPosition = (accessLevelCde, bitRightToLeftPosition) => {
        const accessLevelBinary = (accessLevelCde >>> 0).toString(2);
        const accessLevelEnabledBit = (accessLevelBinary.length >= bitRightToLeftPosition) ? accessLevelBinary.charAt(accessLevelBinary.length - bitRightToLeftPosition) : "0";
        return (accessLevelEnabledBit === "1") ? true : false;
      }
      // right to left. i.e. Visible -> Enabled
      featureAccessMode.isVisible = checkForRightToLeftBitPosition(accessLevelCde, 1);
      featureAccessMode.isEnabled = checkForRightToLeftBitPosition(accessLevelCde, 2);
    }
    // console.log("Access Mode Check for : ", featureId, featureAccessMode);
    return featureAccessMode;
  }
}
