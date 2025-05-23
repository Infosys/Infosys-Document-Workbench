/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { BaseComponent } from '../../base.component';
import { CONSTANTS } from '../../common/constants';
import { SessionService } from '../../service/session.service';
import { AuthenticationService } from '../../service/authentication.service';
import { ConfigDataHelper } from '../../utils/config-data-helper';
import { NiaTelemetryService } from '../../service/nia-telemetry.service';

@Component({
  selector: 'app-logout',
  templateUrl: './logout.component.html',
  styleUrls: ['./logout.component.scss']
})
export class LogoutComponent extends BaseComponent implements OnInit {
  getClassName(): string {
    return "LogoutComponent";
  }
  constructor(private router: Router, private authenticationService: AuthenticationService,
    public sessionService: SessionService, public configDataHelper: ConfigDataHelper, public niaTelemetryService: NiaTelemetryService) {
    super(sessionService, configDataHelper, niaTelemetryService);
  }
  returnUrl: string;
  model:any={
    isDataLoaded: false
  }

  ngOnInit() {
    const parent = this;
    parent.triggerTelemetryEvents(CONSTANTS.TELEMETRY_EVENT.START);
    parent.triggerTelemetryEvents(CONSTANTS.TELEMETRY_EVENT.IMPRESSION);
    parent.triggerTelemetryEvents(this.bmodel.TELE_EVENTS.INTERACT, this.bmodel.TELEID.HEADER.LOGOUT);
    // Telemetry end event should be called after some delay for correct processing
    setTimeout(function () {
      parent.triggerTelemetryEvents(CONSTANTS.TELEMETRY_EVENT.END)
      parent.logout();
      parent.model.isDataLoaded = true;
    }, 1000)

    const tokens: string[] = this.router.url.split('returnUrl=');
    if (tokens.length == 2) {
      this.returnUrl = decodeURIComponent(tokens[1]);
    }
  }

  logout() {
    console.log('logout()')
    const parent = this;
    parent.sessionService.terminateServerSession().then(function (data) {
      parent.authenticationService.cleanupClient()
    }).catch(function (error) {
      parent.authenticationService.cleanupClient()
    });
  }

  ngAfterViewInit(): void {
    // As session is already terminated, no telemetry calls in this block
  }

  ngOnDestroy() {
    // As session is already terminated, no telemetry calls in this block
  }

}
