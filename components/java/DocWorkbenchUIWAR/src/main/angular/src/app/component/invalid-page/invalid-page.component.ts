/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Component, OnInit } from '@angular/core';
import { ConfigDataHelper } from '../../utils/config-data-helper';
import { BaseComponent } from '../../base.component';
import { CONSTANTS } from '../../common/constants';
import { SessionService } from '../../service/session.service';
import { NiaTelemetryService } from '../../service/nia-telemetry.service';

@Component({
  selector: 'app-invalid-page',
  templateUrl: './invalid-page.component.html',
  styleUrls: ['./invalid-page.component.scss']
})
export class InvalidPageComponent extends BaseComponent implements OnInit {
  getClassName(): string {
    return "InvalidPageComponent";
  }
  constructor(public sessionService:SessionService, public configDataHelper: ConfigDataHelper, 
    public niaTelemetryService:NiaTelemetryService) {
    super(sessionService, configDataHelper, niaTelemetryService);
  }

  ngOnInit() {
    this.triggerTelemetryEvents(CONSTANTS.TELEMETRY_EVENT.START);
  }

  ngAfterViewInit(): void {
    this.triggerTelemetryEvents(CONSTANTS.TELEMETRY_EVENT.IMPRESSION);
  }

  ngOnDestroy() {
    this.triggerTelemetryEvents(CONSTANTS.TELEMETRY_EVENT.END);
  }

}
