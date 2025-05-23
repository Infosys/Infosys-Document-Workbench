/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Component, OnInit, HostListener } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { DataService } from '../../service/data.service';
import { CONSTANTS } from '../../common/constants';
import { UtilityService } from '../../service/utility.service';
import { BaseComponent } from '../../base.component';
import { SessionService } from '../../service/session.service';
import { ConfigDataHelper } from '../../utils/config-data-helper';
import { NiaTelemetryService } from '../../service/nia-telemetry.service';
import { LocalSessionData } from '../../data/local-session-data';
import { LocalSessionService } from '../../service/local-session.service';

@Component({
  selector: 'app-work-list',
  templateUrl: './work-list.component.html',
  styleUrls: ['./work-list.component.scss'],
})
export class WorkListComponent extends BaseComponent implements OnInit {
  getClassName(): string {
    return "WorkListComponent";
  }
  private static DOC_LIST_COMPONENT_HEIGHT_OFFSET = 260;

  modelDocListHeight: Number;
  modelIsDocTypeFile = false;
  private queueNameCde: number;

  constructor(private route: ActivatedRoute, private dataService: DataService, private utilityService: UtilityService,
    public sessionService: SessionService, public configDataHelper: ConfigDataHelper, public niaTelemetryService:NiaTelemetryService,
    private localSessionService: LocalSessionService) {
    super(sessionService, configDataHelper, niaTelemetryService);
    const parent = this;
    this.route.params.subscribe(params => {
      parent.queueNameCde = params['queueNameCde'];
      const localSessionData: LocalSessionData = parent.localSessionService.getLocalSessionData();
      localSessionData.lastWorkedQueueCde = +params['queueNameCde'];
      localSessionData.lastDocStatusCde = params['docStatusCde'];
      parent.localSessionService.updateLocalSessionData(localSessionData);
      parent.utilityService.getDocType(+parent.queueNameCde).then(function (value) {
        if (value === CONSTANTS.DOC_TYPE.FILE) {
          parent.modelIsDocTypeFile = true;
        } else {
          parent.modelIsDocTypeFile = false;
        }
        parent.dataService.publishQueueNameCde(params['queueNameCde'], params['docStatusCde']);
      });
    });
  }

  ngOnInit() {
    // console.log("Sized");
    this.resizeChildComponents();    
    this.triggerTelemetryEvents(CONSTANTS.TELEMETRY_EVENT.START);
  }

  ngAfterViewInit() {
    this.triggerTelemetryEvents(CONSTANTS.TELEMETRY_EVENT.IMPRESSION);
  }

  ngOnDestroy() {
    this.triggerTelemetryEvents(CONSTANTS.TELEMETRY_EVENT.END);
  }

  @HostListener('window:resize', ['$event'])
  onResize(event) {
    // console.log("Resized", event.target.innerHeight );
    this.resizeChildComponents();
  }

  private resizeChildComponents() {
    this.modelDocListHeight = window.innerHeight - WorkListComponent.DOC_LIST_COMPONENT_HEIGHT_OFFSET;
  }
}







