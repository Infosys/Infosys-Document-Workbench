/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Component } from '@angular/core';
import { NiaTelemetryService } from './service/nia-telemetry.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
  animations: [
    // trigger('slideInOut', [
    //   state('in', style({
    //     transform: 'translate3d(0, 0, 0)'
    //   })),
    //   state('out', style({
    //     transform: 'translate3d(80%, 0, 0)'
    //   })),
    //   transition('in => out', animate('400ms ease-in-out')),
    //   transition('out => in', animate('400ms ease-in-out'))
    // ]),
  ]
})
export class AppComponent {
  userInactiveTimeoutId;
  constructor(private niaTelemetryService: NiaTelemetryService) {
    this.niaTelemetryService.initiateTelemetryService().then(function (data) {
      if (data) {
        console.log("Telemetry initiated from AppComponent");
      }
    });
  }
}





