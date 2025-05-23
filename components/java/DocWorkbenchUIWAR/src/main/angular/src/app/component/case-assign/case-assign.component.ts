/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Component, OnInit, Output, EventEmitter, Input } from '@angular/core';
import { ToastrService } from 'ngx-toastr';
import { UserData } from '../../data/user-data';
import { DocumentService } from '../../service/document.service';
import { MessageInfo } from '../../utils/message-info';
import { SessionService } from '../../service/session.service';
import { ConfigDataHelper } from '../../utils/config-data-helper';
import { BaseComponent } from '../../base.component';
import { NiaTelemetryService } from '../../service/nia-telemetry.service';
import { CONSTANTS } from '../../common/constants';
import { DocumentUserData } from '../../data/document-user-data';
import { DocumentData } from '../../data/document-data';

@Component({
  selector: 'app-case-assign',
  templateUrl: './case-assign.component.html',
  styleUrls: ['./case-assign.component.scss']
})
export class CaseAssignComponent extends BaseComponent implements OnInit {

  getClassName(): string {
    return "CaseAssignComponent";
  }
  constructor(private toastr: ToastrService, private documentService: DocumentService, private msgInfo: MessageInfo,
    public sessionService: SessionService, public configDataHelper: ConfigDataHelper, public niaTelemetryService: NiaTelemetryService) {
    super(sessionService, configDataHelper, niaTelemetryService);
  }

  @Output() close = new EventEmitter<string>();
  @Input() document: DocumentData;
  @Input()
  set docUserDataList(docUserDataList:DocumentUserData[]){
    this.model.docUserDataList = docUserDataList
  }
  @Input() queueNameCde: number;


  userData: UserData;
  userDataList: UserData[] = [];
  isDataLoaded = false;
  isFormValid: boolean;
  userFullName: string;
  executeStatusMessage = '';
  private prevOwnerId: number = 0;
  private prevReviewerId: number = 0;

  model: any = {
    userSelectedOwnerId: undefined,
    userSelectedReviewerId: undefined,
    isChangedOwner: false,
    isChangedReviewer: false,
    docUserDataList: undefined
  }; // For binding to view

  ngOnInit() {
    const parent = this;
    parent.documentService.getQueueUsersList(parent.queueNameCde, function (error, data) {
      if (data) {
        const userDataList = data;
        parent.userDataList = userDataList;
      } else {
        parent.userDataList = [];
      }
      parent.documentService.getDocumentUserData(parent.document.docId, parent.model.docUserDataList).then(function(data:any){
        parent.model.docUserDataList = data as DocumentUserData[];
        parent.manageCaseAssign();
        parent.isDataLoaded = true;
      }).catch(()=>{parent.isDataLoaded = true;});
    });
  }

  closeWindow() {
    this.close.emit('cancel');
  }

  assignCase() {
    const parent = this;
    const promiseAll = [];
    if (parent.model.isChangedOwner) {
      promiseAll.push(parent.documentService.assignUserToDocPromise(parent.prevOwnerId, parent.model.userSelectedOwnerId,
        parent.document.docId, CONSTANTS.DOC_ROLE_TYPE.CASE_OWNER));
    }
    if (parent.model.isChangedReviewer) {
      promiseAll.push(parent.documentService.assignUserToDocPromise(parent.prevReviewerId, parent.model.userSelectedReviewerId,
        parent.document.docId, CONSTANTS.DOC_ROLE_TYPE.CASE_REVIEWER));
    }
    Promise.all(promiseAll).then(function (data) {
      let isSuccess=true;
      for(let res of data){
        if(res['responseCde'] === 105){
          isSuccess=false;
        }
      }
      if (isSuccess) {
        parent.toastr.success(parent.msgInfo.getMessage(103));
        parent.close.emit('window closed');
      } else {
        parent.executeStatusMessage = 'Could not assign as case details changed. Please try again.';
      }
    }).catch(function (data) {
      parent.executeStatusMessage = 'Error while saving data!!';
    });
  }

  checkIfFormIsValid() {
    this.isFormValid = false;
    if (this.model.userSelectedOwnerId != undefined && this.model.userSelectedOwnerId != this.prevOwnerId) {
      this.model.isChangedOwner = true;
      this.isFormValid = true;
    }
    else {
      this.model.isChangedOwner = false;
    }
    if (this.model.userSelectedReviewerId != undefined && this.model.userSelectedReviewerId != this.prevReviewerId) {
      this.model.isChangedReviewer = true;
      this.isFormValid = true;
    }
    else {
      this.model.isChangedReviewer = false;
    }
  }

  checkIfReviewerDropdownAllowed() {
    if (this.model.userSelectedReviewerId == undefined) {
      return this.getFeature(this.bmodel.FID.CASE_REVIEW_USER_CREATE).isEnabled
    } else {
      return this.getFeature(this.bmodel.FID.CASE_REVIEW_USER_EDIT).isEnabled
    }
  }

  private manageCaseAssign(){
    const parent=this;
    for (let docUserData of parent.model.docUserDataList) {
      if (docUserData.docRoleTypeCde == CONSTANTS.DOC_ROLE_TYPE.CASE_OWNER) {
        parent.model.userSelectedOwnerId = docUserData.userId;
        parent.prevOwnerId = docUserData.userId;
      } else if (docUserData.docRoleTypeCde == CONSTANTS.DOC_ROLE_TYPE.CASE_REVIEWER) {
        parent.model.userSelectedReviewerId = docUserData.userId;
        parent.prevReviewerId = docUserData.userId;
      }
    }
    // if case owner not assinged, then auto suggest currect loggied in user in dropdown.
    if (parent.model.userSelectedOwnerId == undefined) {
      parent.sessionService.getLoggedInUserDetails(function (error, data: UserData) {
        if (data) {
          parent.model.userSelectedOwnerId = data.userId;
        }
      });
    }
    parent.checkIfFormIsValid();
  }
}
