/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { ConfigDataHelper } from '../../utils/config-data-helper';
import { BaseComponent } from '../../base.component';
import { CONSTANTS } from '../../common/constants';
import { SessionService } from '../../service/session.service';
import { NiaTelemetryService } from '../../service/nia-telemetry.service';
import { UserData } from '../../data/user-data';

@Component({
  selector: 'app-error-page',
  templateUrl: './error-page.component.html',
  styleUrls: ['./error-page.component.scss']
})
export class ErrorPageComponent extends BaseComponent implements OnInit {
  getClassName(): string {
    return "ErrorPageComponent";
  }
  constructor(private route: ActivatedRoute, private router: Router,
    public sessionService:SessionService, public configDataHelper: ConfigDataHelper, public niaTelemetryService:NiaTelemetryService) {
    super(sessionService, configDataHelper, niaTelemetryService);
  }

  readonly is_Not_Valid_User = 'user';
  readonly is_Not_Valid_Page = 'page';

  model: any = {
    userName: '',
    isNotValidUser: false,
    isNotValidPage: false,
    returnUrl: ''
  };

  ngOnInit() {
    const parent = this;
    this.triggerTelemetryEvents(CONSTANTS.TELEMETRY_EVENT.START);
    this.route.params.subscribe(params => {
      this.model.isNotValidPage = false;
      this.model.isNotValidUser = false;
      const reason = params['reason'];
      if (reason === this.is_Not_Valid_User) {
        this.model.isNotValidUser = true;
      } else if (reason === this.is_Not_Valid_Page) {
        this.model.isNotValidPage = true;
      }
      const tokens: string[] = this.router.url.split('returnUrl=');
      if (tokens.length === 2) {
        this.model.returnUrl = decodeURIComponent(tokens[1]);
      }

      this.sessionService.getLoggedInUserDetailsPromise()
      .then(function (value) {
        const userData: UserData = value as UserData;
        parent.model.userName = userData.userName
      });

    });
  }

  ngAfterViewInit(): void {
    this.triggerTelemetryEvents(CONSTANTS.TELEMETRY_EVENT.IMPRESSION);
  }

  ngOnDestroy() {
    this.triggerTelemetryEvents(CONSTANTS.TELEMETRY_EVENT.END);
  }

}
