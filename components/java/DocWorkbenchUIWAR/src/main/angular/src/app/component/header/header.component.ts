/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { AdminService } from '../../service/admin.service';
import { UserData } from '../../data/user-data';
import { SessionService } from '../../service/session.service';
import { Router } from '@angular/router';
import { ConfigDataHelper } from '../../utils/config-data-helper';
import { CONSTANTS } from '../../common/constants';
import { BaseComponent } from '../../base.component';
import { NiaTelemetryService } from '../../service/nia-telemetry.service';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent extends BaseComponent implements OnInit {
  getClassName(): string {
    return "HeaderComponent";
  }
  private menuState = 'block';
  @Output() showSidebar = new EventEmitter<boolean>();
  constructor(public sessionService: SessionService,
    private router: Router, public configDataHelper: ConfigDataHelper, public niaTelemetryService:NiaTelemetryService) {
      super(sessionService, configDataHelper, niaTelemetryService);
    }

  model: any = {
    userData: UserData,
    headerTitle: String,
    sideBarToggleIcon : 'left-arrow'
  };

  ngOnInit() {
    const parent = this;
    parent.getLoggedUser();
    const title = parent.configDataHelper.getValue(CONSTANTS.CONFIG.TITLE);
    parent.model.headerTitle = title;
    document.title = title;
  }


  toggleSidebar() {
    this.menuState = this.menuState === 'block' ? 'none' : 'block';
    if (this.menuState === 'block') {
      this.showSidebar.emit(true);
      this.model.sideBarToggleIcon = 'left-arrow';
    } else {
      this.showSidebar.emit(false);
      this.model.sideBarToggleIcon = 'right-arrow';
    }
  }

  doLogout() {
    this.router.navigate(['/logout'], {
      queryParams: {
        returnUrl: this.router.url
      }
    });
  }

  private getLoggedUser() {
    const parent = this;
    parent.sessionService.getLoggedInUserDetails(function (error, data) {
      if (data) {
        parent.model.userData = data as UserData;
      }
    });
  }
}
