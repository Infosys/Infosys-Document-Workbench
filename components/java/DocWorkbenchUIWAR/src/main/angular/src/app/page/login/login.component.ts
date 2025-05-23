/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Component, OnInit } from '@angular/core';
import { AuthenticationService } from '../../service/authentication.service';
import { Router } from '@angular/router';
import { ActivatedRoute } from '@angular/router';
import { SessionService } from '../../service/session.service';
import { UserData } from '../../data/user-data';
import { ConfigDataHelper } from '../../utils/config-data-helper';
import { CONSTANTS } from '../../common/constants';
import { BaseComponent } from '../../base.component';
import { NiaTelemetryService } from '../../service/nia-telemetry.service';


@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent extends BaseComponent implements OnInit {
  getClassName(): string {
    return "LoginComponent";
  }
  private static NOT_VALID_USER_ERROR = 'error/user';
  title: string;
  constructor(private route: ActivatedRoute, private router: Router,
    private authenticationService: AuthenticationService, public sessionService: SessionService,
    public configDataHelper: ConfigDataHelper, public niaTelemetryService:NiaTelemetryService) {
      super(sessionService, configDataHelper, niaTelemetryService);
    const parent = this;
    const title = parent.configDataHelper.getValue(CONSTANTS.CONFIG.TITLE);
    parent.title = title;
    document.title = title;

    parent.route.params.subscribe(params => {
      parent.tenantIdFromUrl = params['tenantId'];
    });
  }

  private tenantIdFromUrl = '';
  registrationUrl: string = location.origin + location.pathname + '#/registration';
  model: any = {
    username: '',
    passwrd: '',
    // tenantId: "",
    errorMessage: ''
  }; // For binding to view

  ngOnDestroy() {
    this.triggerTelemetryEvents(CONSTANTS.TELEMETRY_EVENT.END);
  }

  login() {
    // this.loading = true;
    const parent = this;
    if (parent.tenantIdFromUrl) {
      parent.callLoginService(parent.tenantIdFromUrl);
    } else {
      parent.callLoginService(parent.configDataHelper.getValue(CONSTANTS.CONFIG.TENANT_ID));
    }
  }

  private callLoginService(tenantId: string) {
    const parent = this;
    let promise = parent.authenticationService.login(parent.model.username, parent.model.passwrd, tenantId);
    return promise.then(function (data) {
      console.log(data);
      if (data == true) {
        let promise = parent.sessionService.getLoggedInUserDetailsPromise();
        return promise.then(function (value) {
          let userData: UserData = value as UserData;
          if (userData && userData.userTypeCde == 1) {
            parent.niaTelemetryService.initiateTelemetryService().then(function(data){
              if(data){
                console.log("Telemetry initiated after user login")
                parent.triggerTelemetryEvents(CONSTANTS.TELEMETRY_EVENT.START);
                parent.triggerTelemetryEvents(CONSTANTS.TELEMETRY_EVENT.IMPRESSION);
              }
            });
            const tokens: string[] = parent.router.url.split('returnUrl=');
            if (tokens.length == 2) {
              parent.router.navigate([decodeURIComponent(tokens[1])])
                .then(function (data) {
                  // alert(location);
                  //location.reload(); //This is needed to reload sidebar after login
                });
            } else {
              parent.router.navigate(['/home/dashboard'])
                .then(function (data) {
                  // alert(location);
                  // location.reload(); //This is needed to reload sidebar after login
                });
            }

          } else {
            parent.router.navigate([LoginComponent.NOT_VALID_USER_ERROR]);
          }
        }
        ).catch(function (error) {
          parent.model.errorMessage = error;
          parent.model.passwrd='';
        });
      }
    }).catch(function (error) {
      parent.model.errorMessage = error;
      parent.model.passwrd='';
      // alert(error);
    });
  }

  addLocationHash() {
    // document.getElementById("myText").value = location.hash;
    alert(location.hash);
    return true;   // Returns Value
  }
}
