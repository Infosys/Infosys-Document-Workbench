/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Injectable } from '@angular/core';
import { LocalSessionData } from '../data/local-session-data';

@Injectable()
export class LocalSessionService {

  private localSessionDataKey: string;
  public static UNDEFINED_NUMBER = -100;

  constructor() {
    const browserUrl = location.pathname;
    this.localSessionDataKey = 'lsd-' + browserUrl.replace(/\//g, '').replace('index.html', '').split(';')[0];
   }

  getLocalSessionData() {
    const localSessionData: LocalSessionData = JSON.parse(localStorage.getItem(this.localSessionDataKey));
    if (localSessionData == null) {
      return new LocalSessionData(LocalSessionService.UNDEFINED_NUMBER,
        LocalSessionService.UNDEFINED_NUMBER,
        LocalSessionService.UNDEFINED_NUMBER,
        LocalSessionService.UNDEFINED_NUMBER,
        LocalSessionService.UNDEFINED_NUMBER,
        LocalSessionService.UNDEFINED_NUMBER);
    }
    return localSessionData;
  }

  updateLocalSessionData(localSessionData: LocalSessionData) {
    localStorage.setItem(this.localSessionDataKey, JSON.stringify(localSessionData));
  }

  clearLocalSessionData() {
    localStorage.removeItem(this.localSessionDataKey);
  }

}
