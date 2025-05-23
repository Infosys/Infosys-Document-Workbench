/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Component, OnInit, OnDestroy } from '@angular/core';
import { UserData } from '../../data/user-data';
import { ValData } from '../../data/val-data';
import { ActivatedRoute} from '@angular/router';
import { SessionService } from '../../service/session.service';
import { QueueData } from '../../data/queue-data';
import { ValService } from '../../service/val.service';
import { DataService } from '../../service/data.service';
import { BaseComponent } from '../../base.component';
import { ConfigDataHelper } from '../../utils/config-data-helper';
import { NiaTelemetryService } from '../../service/nia-telemetry.service';

@Component({
  selector: 'app-breadcrumb',
  templateUrl: './breadcrumb.component.html',
  styleUrls: ['./breadcrumb.component.scss']

})
export class BreadcrumbComponent extends BaseComponent implements OnInit, OnDestroy {
  getClassName(): string {
    return "BreadcrumbComponent";
  }
  private routeEventsSubscription: any;
  private queueDataList: QueueData[] = [];
  private attachmentId: number;
  private selectedAttachmentData;
  private extractionPathFound=false;


  model: any = {
    breadcrumbItemsUrl: undefined,
    breadcrumbItems: undefined,
    isHomePage: true,
    homeUrl: '',
    showNextPrevBtn: false

  }; // For binding to view


  constructor(private route: ActivatedRoute, private valService: ValService, public sessionService: SessionService,
    private dataService: DataService, public configDataHelper: ConfigDataHelper, public niaTelemetryService:NiaTelemetryService) {
    super(sessionService, configDataHelper, niaTelemetryService);
    const parent = this;
    parent.model.homeUrl = location.origin + location.pathname + '#/home';
    let splittedURL: string[];
    parent.routeEventsSubscription = parent.route.url.subscribe(activeUrl => { // for catching any URL changes and extracting it
      const url: string = window.location.hash.split('?')[0];
      splittedURL = url.split('/');
      if (splittedURL.length > 2) {
        parent.model.isHomePage = false;
      } else {
        parent.model.isHomePage = true;
      }
      const promise = parent.sessionService.getLoggedInUserDetailsPromise();  // this func has cashed data of user
      return promise.then(function (value) {
        parent.queueDataList = (value as UserData).queueDataList;
        parent.setBreadcrumbValue(splittedURL);
      });
    });
  }

  ngOnInit() {
    const parent = this;
    parent.selectedAttachmentData = parent.dataService.selectedAttachmentData.subscribe(attachmentData => {
      if (attachmentData && attachmentData.attachmentId === parent.attachmentId) {
        const nextIndex = parent.model.breadcrumbItems.length;
        parent.model.breadcrumbItems[nextIndex] =  parent.extractionPathFound?'Extraction Path '+parent.attachmentId:''+attachmentData.fileName ;
        parent.model.breadcrumbItemsUrl[nextIndex] = '*';
      }
    });
  }

  ngOnDestroy() {
    this.routeEventsSubscription.unsubscribe();
    this.selectedAttachmentData.unsubscribe();
  }

  private setBreadcrumbValue(splittedURL: string[]) {
    const parent = this;
    let routeValue = '';
    parent.model.breadcrumbItems = [];
    parent.model.breadcrumbItemsUrl = [];
    const adminIndex = splittedURL.indexOf('configuration');
    const profileIndex = splittedURL.indexOf('profile');
    const dashboardIndex = splittedURL.indexOf('dashboard');
    const lastIndex = splittedURL.length - 1;
    const worklistIndex = splittedURL.indexOf('worklist');
    const isWorklistValidIndex = (worklistIndex > -1 && worklistIndex + 1 < lastIndex);
    const workdataIndex = splittedURL.indexOf('workdata');
    if(workdataIndex != -1){
      parent.model.showNextPrevBtn = true;
    }
    else{
      parent.model.showNextPrevBtn = false;
    }
    const attachmentIndex = splittedURL.indexOf('attachment');
    const extractionpathIndex = splittedURL.indexOf('extractionpath');
    const QueueManageIndex = splittedURL.indexOf('managequeue');
    const isAttachmentIndexValid = attachmentIndex > -1 || extractionpathIndex >-1;
    if(dashboardIndex === lastIndex){
      parent.model.breadcrumbItems[0] = 'Dashboard';
      parent.model.breadcrumbItemsUrl[0] = '*';
    }
    else if (adminIndex > -1 && adminIndex < lastIndex) {
      parent.model.breadcrumbItems[0] = 'Administration';
      parent.model.breadcrumbItemsUrl[0] = '*';
      switch (splittedURL[adminIndex + 1]) {
        case 'userlist':
          parent.model.breadcrumbItems[1] = 'Manage Users';
          parent.model.breadcrumbItemsUrl[1] = '*';
          break;
        case 'rbaclist':
          parent.model.breadcrumbItems[1] = 'Manage RBAC';
          parent.model.breadcrumbItemsUrl[1] = '*';
          break;
        case 'queuelist': //For Admin users only
          parent.model.breadcrumbItems[1] = 'Manage All Queues';
          parent.model.breadcrumbItemsUrl[1] = '*';
          break;
      }
    }else if(QueueManageIndex=== lastIndex){
      parent.model.breadcrumbItems[0] = 'Manage My Queues';
      parent.model.breadcrumbItemsUrl[0] = '*';
    }
     else if (profileIndex === lastIndex) {
      parent.model.breadcrumbItems[0] = 'Profile';
      parent.model.breadcrumbItemsUrl[0] = '*';
    } else if (isWorklistValidIndex || (workdataIndex > -1 && workdataIndex + 1 < lastIndex)) {
      parent.model.breadcrumbItems[0] = 'My Queues';
      parent.model.breadcrumbItemsUrl[0] = '*';
      routeValue = routeValue.concat(parent.model.homeUrl + '/');
      // setting the routeback to an initial constant value which is not going to change at any case
      const worklistRouteValue = routeValue.concat('worklist/');
      const queueNameCdeIndex = [worklistIndex, workdataIndex].filter(index => index > -1)[0] + 1;
      const queueNameCde = splittedURL[queueNameCdeIndex];
      for (let i = 0; i < parent.queueDataList.length; i++) {
        if (+queueNameCde === parent.queueDataList[i].queueNameCde) { // matching the extracted URL with the queue code from the backend
          parent.model.breadcrumbItems[1] = parent.queueDataList[i].queueNameTxt;
          parent.model.breadcrumbItemsUrl[1] = worklistRouteValue.concat(queueNameCde.concat('/100'));
          break;
        }
      }
      parent.valService.getValList('task-status', function (error, data) { // getting the taskList code and text from backend
        if (data) {
          const taskList: ValData[] = data;
          // Getting docId or taskStatusCde
          const temp = splittedURL[queueNameCdeIndex + 1];
          parent.model.breadcrumbItemsUrl[2] = '*';
          if (isWorklistValidIndex) { // checking the URL is for case no or for taskList code
            for (let i = 0; i < taskList.length; i++) {
              if (+temp === taskList[i].cde) {
                parent.model.breadcrumbItems[2] = taskList[i].txt;
                break;
              }
            }
          } else {
            parent.model.breadcrumbItems[2] = 'Case ' + temp;
            if (isAttachmentIndexValid) {
              const newAttachmentIdx = (attachmentIndex>-1?attachmentIndex:extractionpathIndex);
              parent.extractionPathFound = extractionpathIndex>-1?true:false;
              parent.attachmentId = +splittedURL[newAttachmentIdx + 1];
              parent.model.breadcrumbItemsUrl[2] = routeValue + 'workdata/' + queueNameCde + '/' + temp;
            }
          }
        }
      });
    }
  }
}
