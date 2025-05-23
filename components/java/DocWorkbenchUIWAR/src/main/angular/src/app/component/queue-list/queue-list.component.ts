/** =============================================================================================================== *
 * Copyright 2022 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Component, ElementRef, HostListener, Input, OnInit} from '@angular/core';
import { BaseComponent } from '../../base.component';
import { SessionService } from '../../service/session.service';
import { AdminService } from '../../service/admin.service';
import { ConfigDataHelper } from '../../utils/config-data-helper';
import { NiaTelemetryService } from '../../service/nia-telemetry.service';
import { formatDate } from '@angular/common';
import { MessageInfo } from '../../utils/message-info';
import { ToastrService } from 'ngx-toastr';
import { PageEvent } from '@angular/material/paginator';
import { CurrentUserQueueData } from '../../data/user-queue-data';
import { CONSTANTS } from '../../common/constants';
@Component({
  selector: 'app-queue-list',
  templateUrl: './queue-list.component.html',
  styleUrls: ['./queue-list.component.scss']
})
export class QueueListComponent extends BaseComponent implements OnInit {
  getClassName(): string {
    return "QueueListComponent";
  }

  constructor(private adminService: AdminService,
    public niaTelemetryService: NiaTelemetryService,
    public sessionService: SessionService,
    public configDataHelper: ConfigDataHelper,
    private msgInfo: MessageInfo,
    private toastr: ToastrService,
    private elementRef: ElementRef) {
    super(sessionService, configDataHelper, niaTelemetryService)
  }
  public model: any = {
    wrapperHeightInPx: Number,
    conenteHeightInPx: Number,
    queueDataList: [],
    isDataLoaded: false,
    visibilityDate: '',
    isOpenQueueChecked: true,
    isClosedQueueChecked: false,
    isPersonalQueueList: false,
    isAdminQueueList: false,
    tableTotalContentSize: 0,
    pageSize: 25,
    isSaveAllowed: false
  }

  @Input() maxAllowedHeightInPx: number;
  @Input() maxAllowedWidthInPx: number;
  @Input()
  set queueListMode(queueListMode: string) {
    console.log("queueListMode", queueListMode);
    if (queueListMode == 'general') {
      this.model.isAdminQueueList = true;
    }
    else if (queueListMode == 'personal') {
      this.model.isPersonalQueueList = true;
    }

  }

  private tempqueueDataList: any = [];
  private queueStatus: string = 'open';
  private currentPageNumber: number = 1;

  ngOnInit() {
    this.triggerTelemetryEvents(CONSTANTS.TELEMETRY_EVENT.START);
    this.queueStatus = 'open';
    this.getQueueList(this.queueStatus);
  }

  ngAfterViewInit() {
    this.triggerTelemetryEvents(CONSTANTS.TELEMETRY_EVENT.IMPRESSION);
  }

  ngOnDestroy() {
    this.triggerTelemetryEvents(CONSTANTS.TELEMETRY_EVENT.END);
  }

  ngAfterContentInit() {
    // This method will be called only once in component lifecycle, NOT called on page resize.
    this.updateProperties();
  }

  @HostListener('window:resize', ['$event'])
  onResize(event) {
    // This method will be called only on page resize and NOT on component load.
    this.updateProperties();
  }

  private updateProperties() {
    this.model.wrapperHeightInPx = this.maxAllowedHeightInPx;
    this.model.contentHeightInPx = this.maxAllowedHeightInPx - 30; // Calculated using trial-error
    // This is the technique to update variables in SCSS
    this.elementRef.nativeElement.style.setProperty('--content-height', this.model.contentHeightInPx + 'px')

    // This is the technique to update variables in SCSS
    const m = getComputedStyle(document.querySelector(':root')).getPropertyValue('--docwb-main-content-width')
    console.log('user-list->updateProperties()', m)
    this.elementRef.nativeElement.style.setProperty('--queue-table-width', m + 'px')
  }

  refreshComponent() {
    this.getQueueList(this.queueStatus);
    this.model.isSaveAllowed = false;
  }
  getQueueList(queueStatus: string) {
    const parent = this;
    this.model.isDataLoaded = false;
    const promise = parent.adminService.getPersonalQueueListPromise(queueStatus);
    promise.then(function (value) {
      const localQueueDataList = value as CurrentUserQueueData;
      parent.populateQueueData(localQueueDataList);
      parent.showPageData(parent.currentPageNumber);
      parent.model.isDataLoaded = true;
    }).catch(function (error) {
      parent.model.isDataLoaded = true;
    });

  }
  private populateQueueData(localQueueDataList) {
    const parent = this;
    parent.tempqueueDataList = [];
    for (let i = 0; i < localQueueDataList.length; i++) {
      localQueueDataList[i].isDateValueChanged = false;
      localQueueDataList[i].isClosureDateValueChanged = false;
      if (localQueueDataList[i].userQueueHideAfterDtm != null) {
        // since date picker wants a date  in below format to render it in the datepicker
        // eg. Wed Sep 28 2022 00:00:00 GMT+0530 (India Standard Time)
        localQueueDataList[i].userQueueHideAfterDtm = new Date(localQueueDataList[i].userQueueHideAfterDtm);
      }
      if (localQueueDataList[i].queueClosedDtm != null) {
        localQueueDataList[i].queueClosedDtm = new Date(localQueueDataList[i].queueClosedDtm);
      }
      if (localQueueDataList[i].queueHideAfterDtm != null) {
        localQueueDataList[i].queueHideAfterDtm = new Date(localQueueDataList[i].queueHideAfterDtm);
      }
      let tempq: CurrentUserQueueData = Object.assign({}, localQueueDataList[i]);
      parent.tempqueueDataList.push(tempq);

    }
    parent.model.queueDataList = localQueueDataList;
    parent.model.tableTotalContentSize = parent.model.queueDataList.length;
    console.log(parent.model.tableTotalContentSize);
  }
  private showPageData(pageNumber: number) {
    let startIndex = (pageNumber - 1) * this.model.pageSize;
    let endIndex = Math.min((startIndex + this.model.pageSize - 1), this.model.queueDataList.length)
    for (let i = 0; i < this.model.queueDataList.length; i++) {
      if (i >= startIndex && i <= endIndex) {
        this.model.queueDataList[i].isVisible = true;
      }
      else {
        this.model.queueDataList[i].isVisible = false;
      }

    }
  }
  saveVisibilityDate() {
    const parent = this;
    let promiseAll = [];
    for (let i = 0; i < parent.model.queueDataList.length; i++) {
      if (parent.tempqueueDataList[i].userQueueHideAfterDtm == parent.model.queueDataList[i].userQueueHideAfterDtm) {
        continue;
      } else {
        const queueData: CurrentUserQueueData = parent.model.queueDataList[i];
        let visibilityDate: String = null;
        if (queueData.userQueueHideAfterDtm != null) {
          visibilityDate = formatDate(queueData.userQueueHideAfterDtm, 'yyyy-MM-dd 23:59:59', 'en');
        }
        const promise = parent.adminService.updateQueueVisibilityDate(queueData.queueNameCde,
          visibilityDate);
        promiseAll.push(promise);
      }
    }
    Promise.all(promiseAll).then(function (result) {
      parent.toastr.success(parent.msgInfo.getMessage(116));
      parent.refreshComponent()
    }).catch(error => {
      parent.toastr.error(error);
    });
  }

  setHideAfterDtm(queueData: CurrentUserQueueData, dateStr: string) {
    let calDateObj: any = null;
    this.model.isSaveAllowed =false;
    let tempQueueData: CurrentUserQueueData = this.tempqueueDataList.filter(x => x.queueNameCde
      == queueData.queueNameCde)[0]
    if (dateStr != null) {
      calDateObj = new Date(dateStr);
      calDateObj.setHours(23, 59, 59);
    }
    queueData.userQueueHideAfterDtm = calDateObj;
    if (calDateObj == null && tempQueueData.userQueueHideAfterDtm != null) {
      queueData.isDateValueChanged = true;
    }
    else if (tempQueueData.userQueueHideAfterDtm == calDateObj.toString()) {
      queueData.isDateValueChanged = false;
    } else {
      queueData.isDateValueChanged = true;
    }

    let isDateValueChangedCount = this.model.queueDataList.filter(x =>
      x.isDateValueChanged).length
    if (isDateValueChangedCount > 0) {
      this.model.isSaveAllowed = true;
    }
  }
  onQueueClick(event: string) {
    const parent = this;
    if (event == 'closed') {
      this.queueStatus = 'closed';
      parent.getQueueList(this.queueStatus);
    }
    if (event == 'open') {
      this.queueStatus = 'open';
      parent.getQueueList(this.queueStatus);
    }
  }

  setQueueClosureDtm(queueData: CurrentUserQueueData, calDateObj: string) {
    let calDateObj1: any = null;
    let queueHideAfterDate: any = null;
    this.model.isSaveAllowed =false;
    if (calDateObj != null) {
      calDateObj1 = new Date(calDateObj);
      calDateObj1.setHours(23, 59, 59);
    }
    let tempQueueData: CurrentUserQueueData = this.tempqueueDataList.filter(x => x.queueNameCde
      == queueData.queueNameCde)[0]

    if (calDateObj1 == null && tempQueueData.queueClosedDtm != null) {
      queueData.isClosureDateValueChanged = true;
    }
    else if (tempQueueData.queueClosedDtm == calDateObj1.toString()) {
      queueData.isClosureDateValueChanged = false;
    } else {
      queueData.isClosureDateValueChanged = true;
    }
    queueData.queueClosedDtm = calDateObj1;
    if (calDateObj1 != null) {
      let defaultDays = this.configDataHelper.getValue('queue.queueVisibilityAfterClosureInDays');
      queueHideAfterDate = new Date(calDateObj1);
      // Default visibilty date i.e. queueHideAfterDate is 7 days from queueClosedDtm
      queueHideAfterDate.setDate(queueHideAfterDate.getDate() + defaultDays);
    }
    this.setqueueClosureVisibility(queueData, queueHideAfterDate);
    let closureDateValueChangedCount = this.model.queueDataList.filter(x =>
      x.isClosureDateValueChanged).length
    let isDateValueChangedCount = this.model.queueDataList.filter(x =>
        x.isDateValueChanged).length
    if (closureDateValueChangedCount+isDateValueChangedCount > 0) {
      this.model.isSaveAllowed = true;
    }
  }
  setqueueClosureVisibility(queueData: CurrentUserQueueData, dateStr: string) {
    let calDateObj: any = null;
    this.model.isSaveAllowed =false;
    let tempQueueData: CurrentUserQueueData = this.tempqueueDataList.filter(x => x.queueNameCde
      == queueData.queueNameCde)[0]
    if (dateStr != null) {
      calDateObj = new Date(dateStr);
      calDateObj.setHours(23, 59, 59);
    }
    queueData.queueHideAfterDtm = calDateObj;
    if (calDateObj == null && tempQueueData.queueHideAfterDtm != null) {
      queueData.isDateValueChanged = true;
    }
    else if (tempQueueData.queueHideAfterDtm == calDateObj.toString()) {
      queueData.isDateValueChanged = false;
    } else {
      queueData.isDateValueChanged = true;
    }
    let closureDateValueChangedCount = this.model.queueDataList.filter(x =>
      x.isClosureDateValueChanged).length
    let isDateValueChangedCount = this.model.queueDataList.filter(x =>
      x.isDateValueChanged).length
    if (closureDateValueChangedCount+isDateValueChangedCount > 0) {
      this.model.isSaveAllowed = true;
    }
  }
  saveQueueClosureDetails() {
    const parent = this;
    let promiseAll = [];
    for (let i = 0; i < parent.model.queueDataList.length; i++) {
      if (!parent.model.queueDataList[i].isVisible) {
        continue;
      }
      if (parent.model.queueDataList[i].isDateValueChanged
        || parent.model.queueDataList[i].isClosureDateValueChanged) {
        const queueData: CurrentUserQueueData = parent.model.queueDataList[i];
        let queueHideAfterDate: String = null;
        let queueClosedDate: String = null;
        if (queueData.queueHideAfterDtm != null) {
          queueHideAfterDate = formatDate(queueData.queueHideAfterDtm, 'yyyy-MM-dd 23:59:59', 'en');
        }
        if (queueData.queueClosedDtm != null) {
          queueClosedDate = formatDate(queueData.queueClosedDtm, 'yyyy-MM-dd 23:59:59', 'en');
        }
        const promise = parent.adminService.updateQueueClosureDetails(queueData.queueNameCde,
          queueHideAfterDate, queueClosedDate);
        promiseAll.push(promise);
      }
    }
    if (promiseAll.length > 0) {
      Promise.all(promiseAll).then(function (result) {
        parent.toastr.success(parent.msgInfo.getMessage(116));
        parent.refreshComponent()
      }).catch(error => {
        parent.toastr.error(error);
      });
    } else {
      parent.toastr.error("No item saved")
    }

  }

  onChangePage(pe: PageEvent) {
    console.log(pe.pageIndex);
    console.log(pe.pageSize);
    this.currentPageNumber = pe.pageIndex + 1;
    this.showPageData(this.currentPageNumber);
  }

  convertDateFormat(closeDate:string){
    let queueClosedDtm :any=null;
    if(closeDate!=null){
      queueClosedDtm=formatDate(new Date(closeDate),'dd-MMM-yyyy','en');
    }
    return queueClosedDtm;
  }
}

// export interface CurrentUserQueueData {
//   queueNameCde: number;
//   queueNameTxt: string;
//   userQueueHideAfterDtm: string;
//   queueHideAfterDtm: string;
//   queueClosedDtm: string;
//   isDateValueChanged: boolean;
//   isClosureDateValueChanged: boolean;
//   isVisible: boolean;
// }
