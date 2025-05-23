/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Component, OnInit } from '@angular/core';
import { RegistrationService } from '../../service/registration.service';
import { ActivatedRoute } from '@angular/router';
import { ConfigDataHelper } from '../../utils/config-data-helper';
import { CONSTANTS } from '../../common/constants';
import { SessionService } from '../../service/session.service';
import { BaseComponent } from '../../base.component';
import { NiaTelemetryService } from '../../service/nia-telemetry.service';

@Component({
  selector: 'app-registration',
  templateUrl: './registration.component.html',
  styleUrls: ['./registration.component.scss']
})
export class RegistrationComponent extends BaseComponent implements OnInit {
  getClassName(): string {
    return "RegistrationComponent";
  }
  private tenantId = 'none';
  private tenantIdFromUrl = '';
  title: string;
  model: any = {
    passwrd: '',
    confirmPasswrd: '',
    emailid: '',
    loginid: '',
    fullname: '',
    usertype: '',
    isFormSubmitted: false,
    isUserRegistered: false,
    isDiffPassword: false,
    errorCde: 0,
    errorTxt: '',
    loginUrl: location.origin + location.pathname + '#/login'
  };

  constructor(private route: ActivatedRoute, private regService: RegistrationService,
    public configDataHelper: ConfigDataHelper,  public sessionService: SessionService, public niaTelemetryService:NiaTelemetryService) {
      super(sessionService, configDataHelper, niaTelemetryService)
    const parent = this;
    parent.tenantId = parent.configDataHelper.getValue(CONSTANTS.CONFIG.TENANT_ID);
    this.route.params.subscribe(params => {
      parent.tenantIdFromUrl = params['tenantId'];
    });
    const title = parent.configDataHelper.getValue(CONSTANTS.CONFIG.TITLE);
    parent.title = title;
    document.title = title;

  }
  ngOnInit() {
  }

  addNewUser() {
    const parent = this;
    let tenantIdForCall = parent.tenantId;
    if (parent.tenantIdFromUrl) {
      tenantIdForCall = parent.tenantIdFromUrl;
    }

    if (parent.model.passwrd === parent.model.confirmPasswrd) {
      parent.model.isDiffPassword = false;
      // console.log(1);
      const promise = parent.regService.addNewUser(
        parent.model.emailid, parent.model.fullname, parent.model.loginid, parent.model.passwrd,
        parent.model.usertype, tenantIdForCall
      );
      return promise.then(result => {
        parent.model.errorCde = result['response']['errCde'];
        parent.model.errorTxt = result['response']['errTxt'];
        parent.model.isFormSubmitted = true;
        if (result['responseCde'] === 0) {
          parent.model.isUserRegistered = true;
        }
      }).catch(error => {
        console.log(error);
      });
    } else {
      parent.model.isDiffPassword = true;
    }
  }
}
