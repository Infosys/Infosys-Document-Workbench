/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Component, OnInit } from '@angular/core';
import { ProductService } from '../../service/product.service';
import { UtilityService } from '../../service/utility.service';
import { ProductData } from '../../data/product-data';
import { formatDate } from '@angular/common';
import { CONSTANTS } from '../../common/constants';
import { ConfigDataHelper } from '../../utils/config-data-helper';

@Component({
  selector: 'app-footer',
  templateUrl: './footer.component.html',
  styleUrls: ['./footer.component.scss']
})
export class FooterComponent implements OnInit {

  constructor(private productService: ProductService,
    private utilityService: UtilityService,
    public configDataHelper: ConfigDataHelper,) { }

  model: any = {
    webProductData: ProductData,
    apiProductData: ProductData,
    browserType: '',
    currentYear: formatDate(new Date(), 'yyyy', 'en')
  };

  private recurringTimer: any;
  private isWebVersionValidationEnabled: false;
  private webVersionCheckIntervalInSecs: number;

  ngOnInit() {
    const parent = this;
    this.isWebVersionValidationEnabled = parent.configDataHelper.getValue('application.webVersionValidation.enabled');
    this.webVersionCheckIntervalInSecs = parent.configDataHelper.getValue('application.webVersionValidation.checkIntervalInSecs');
    this.getData();
  }

  ngAfterViewInit() {
    if (this.isWebVersionValidationEnabled) {
      this.startRecurringTimer();
    }
  }

  ngOnDestroy() {
    if (this.isWebVersionValidationEnabled) {
      this.stopRecurringTimer();
    }

  }

  /************************* PRIVATE METHODS *************************/
  private getData() {
    const parent = this;
    parent.productService.getApiProductData(function (error, data) {
      if (!error) {
        parent.model.apiProductData = data;
      }
    });
    parent.productService.getWebProductData().then(function (data) {
      parent.model.webProductData = data;
    });
    const browserType = parent.utilityService.getBrowserType();

    if (browserType === CONSTANTS.BROWSER_TYPE.INTERNET_EXPLORER) {
      parent.model.browserType = 'IE';
    } else if (browserType === CONSTANTS.BROWSER_TYPE.CHROME) {
      parent.model.browserType = 'Chrome';
    } else if (browserType === CONSTANTS.BROWSER_TYPE.FIREFOX) {
      parent.model.browserType = 'Firefox';
    }
  }

  private validateWebVersion() {
    const parent = this;
    return new Promise(function (fulfilled, rejected) {
      const currentWebVersion = parent.model.webProductData.productVersion + '.' + parent.model.webProductData.buildVersion;
      parent.productService.getWebProductData()
        .then(function (data) {
          const latestWebProductData = data as ProductData;
          const latestWebVersion = latestWebProductData.productVersion + '.' + latestWebProductData.buildVersion;
          let validationMessage = ''
          if (latestWebVersion != currentWebVersion) {
            validationMessage += '                             !!  WARNING  !!'
            validationMessage += '\nNew version is available. Please refresh this page.'
            validationMessage += '\nCurrent Version : ' + currentWebVersion
            validationMessage += '\nServer Version   : ' + latestWebVersion
          }
          fulfilled(validationMessage)
        })
        .catch(function (error) {
          rejected(error)
        });
    });
  }

  private startRecurringTimer() {
    console.log('startRecurringTimer')
    const parent = this;
    // Set timer loop interval as 10% of check interval. So, that for larger check interval
    // e.g. 10 mins, timer interval is 1 min; and for smaller check interval e.g. 10 secs,
    // timer interval is 1 sec.
    const timerIntervalInSecs = 0.1 * parent.webVersionCheckIntervalInSecs;
    // The main logic is to show a modal alert to user if validation fails. But user may click on
    // `OK` button after a few minutes only. So, we should pause validation logic even though
    // timer will run as usual.
    // Hence, we need to capture if alert was consumed by user and at what time. With the last
    // consumed time of the alert, we can ensure next alert is shown only after the minimum
    // gap between two alerts.
    let isAlertConsumed = true;
    let lastAlertConsumedDtm = new Date();
    this.recurringTimer = setInterval(() => {
      console.log('recurringTimer')
      const currentDtm = new Date();
      if (isAlertConsumed) {
        if ((currentDtm.getTime() - lastAlertConsumedDtm.getTime()) / 1_000 >= parent.webVersionCheckIntervalInSecs) {
          isAlertConsumed = false;
          parent.validateWebVersion().then(function (dataAsValidationMessage) {
            if (dataAsValidationMessage) {
              alert(dataAsValidationMessage);
            }
            lastAlertConsumedDtm = new Date()
            isAlertConsumed = true
          }).catch(function (error) {
            console.log("Error ", error)
          });
        }
      }

    }, timerIntervalInSecs * 1_000);
  }

  private stopRecurringTimer() {
    console.log('stopRecurringTimer')
    clearInterval(this.recurringTimer)
  }
}
