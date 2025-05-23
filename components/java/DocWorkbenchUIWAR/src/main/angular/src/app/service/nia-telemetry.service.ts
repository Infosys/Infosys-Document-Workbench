/** =============================================================================================================== *
 * Copyright 2022 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Injectable } from '@angular/core';
import { TelemetryService } from '@infy-docwb/infy-telemetry-sdk';
import { CONSTANTS } from '../common/constants';
import { ConfigDataHelper } from '../utils/config-data-helper';
import { ProductService } from './product.service';
@Injectable()
export class NiaTelemetryService {
  static telemetryConfig = {
    'pdata': { 'id': '', 'pid': '', 'ver': ''},
    'channel': 'web-core',
    'uid': '',
    'did': '',
    'activeSessionPeriodLimit': 600, // In seconds
    'authtoken': '',
    'env': '',
    'sid': '',
    'batchsize': 1,
    'host': '',
    'endpoint': CONSTANTS.APIS.DOCWBWEB.POST_TELEMETRY_DATA,
    'apislug': '',
    'cdata': [{ 'type': '', 'id': '' }]
  }

  constructor(private telemetryService: TelemetryService, private productService: ProductService,
    private configDataHelper: ConfigDataHelper) { }

  initiateTelemetryService() {
    const parent = this;
    return new Promise(function (fulfilled, rejected) {
      const promiseAll = [];
      if (!parent.configDataHelper.getValue(CONSTANTS.CONFIG.TELEMETRY_ENABLED)) {
        fulfilled(false);
      }

      let authtokenJson = {
        'tokenName': 'web-core',
        'tokenExpireDtm': new Date(),
        'tokenValue': parent.configDataHelper.getValue(CONSTANTS.CONFIG.TENANT_ID)
      };
      authtokenJson = { ...authtokenJson, ...parent.configDataHelper.getValue(CONSTANTS.CONFIG.TELEMETRY_AUTHTOKEN) };
      NiaTelemetryService.telemetryConfig.authtoken = btoa(JSON.stringify(authtokenJson));
      if (NiaTelemetryService.telemetryConfig.authtoken == undefined || NiaTelemetryService.telemetryConfig.authtoken == null) {
        fulfilled(false);
      } else {
        NiaTelemetryService.telemetryConfig.env = parent.configDataHelper.getValue(CONSTANTS.CONFIG.ENV);
        NiaTelemetryService.telemetryConfig.host = parent.configDataHelper.getValue(CONSTANTS.CONFIG.TELEMETRYSERVICE_BASER_URL);
        const appContext = window.location.host + window.location.pathname;
        NiaTelemetryService.telemetryConfig.pdata.id = parent.configDataHelper.getValue(CONSTANTS.CONFIG.ENV) + "-web"
        NiaTelemetryService.telemetryConfig.pdata.pid = "web " + appContext + ";browser " + navigator.userAgent.toLowerCase();
        promiseAll.push(parent.productService.getWebProductData());
        Promise.all(promiseAll).then(function (data) {
          if (data[0]) {
            NiaTelemetryService.telemetryConfig.pdata.ver = data[0].productVersion + "." + data[0].buildVersion;
          }
          parent.telemetryService.setProviderImplementation('SUNBIRD', NiaTelemetryService.telemetryConfig);
          fulfilled(true);
        });
      }
    });
  }

  getTelemetryService() {
    return this.telemetryService;
  }

  terminateTelemetryService() {
    // this.telemetryService=undefined;
  }

  // Get sessionid from localstorage.
  // Since it is not updating latest sid in event data fetching and updating event data.
  public getTelemetrySessionId() {
    const sessionId = localStorage.getItem('session_id');
    console.log("sessionId", sessionId);
    return sessionId;
  }
}
