/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Component, OnInit, Input, Output, EventEmitter, OnChanges, SimpleChange } from '@angular/core';
import { DocumentData } from '../../data/document-data';
import { NgbModal, ModalDismissReasons, NgbModalOptions } from '@ng-bootstrap/ng-bootstrap';
import { AuditData } from '../../data/audit-data';
import { DocumentService } from '../../service/document.service';
import { ToastrService } from 'ngx-toastr';
import { MessageInfo } from '../../utils/message-info';
import { UserData } from '../../data/user-data';
import { UtilityService } from '../../service/utility.service';
import { SessionService } from '../../service/session.service';
import { Router } from '@angular/router';
import { CONSTANTS } from '../../common/constants';
import { BaseComponent } from '../../base.component';
import { ConfigDataHelper } from '../../utils/config-data-helper';
import { NiaTelemetryService } from '../../service/nia-telemetry.service';
import { AttributeService } from '../../service/attribute.service';
import { DocumentUserData } from '../../data/document-user-data';

@Component({
  selector: 'app-case-panel',
  templateUrl: './case-panel.component.html',
  styleUrls: ['./case-panel.component.scss']
})
export class CasePanelComponent extends BaseComponent implements OnInit, OnChanges {
  getClassName(): string {
    return "CasePanelComponent";
  }
  constructor(private modalService: NgbModal, private documentService: DocumentService, private toastr: ToastrService,
    private msgInfo: MessageInfo, private utilityService: UtilityService, public sessionService: SessionService,
    private router: Router, public configDataHelper: ConfigDataHelper, private attributeService: AttributeService, public niaTelemetryService:NiaTelemetryService) {
      super(sessionService, configDataHelper, niaTelemetryService)
     }

  @Output() close = new EventEmitter<string>();
  closeResult: string;
  documentData: DocumentData;
  isDataLoaded = false;
  auditData: AuditData;
  queueNameCdeSelected = 0;
  private _isDataReady: boolean;
  taskStatusCde = 0;
  subQueueNameCdeSelected = 0;
  isCaseclosed: boolean;
  isReassignAllowed: boolean=false;
  isCaseEditable: boolean;

  model: any = {
    caseOwnerLoginId:'',
    caseReviewerLoginId: '',
    docUserDataList:undefined
  }
  // For disabling mouse click and keystrokes outside modal window
  ngbModalOptions: NgbModalOptions = {
    backdrop: 'static',
    keyboard: false
  };

  @Input()
  set document(docData: DocumentData) {
  }

  @Input()
  set isDataReady(isDataReady: boolean) {
    this._isDataReady = isDataReady;
  }

  @Input()
  set docUserDataList(docUserDataList:DocumentUserData[]){
    this.model.docUserDataList = docUserDataList
  }

  ngOnChanges(changes: { [propKey: string]: SimpleChange }) {
    this.isDataLoaded = false;
    if (changes['document']) {
      if (this._isDataReady) {
        const to = changes['document'].currentValue;
        this.documentData = to;
        this.getDocUserData();
        this.queueNameCdeSelected = this.documentData['queueNameCde'];
        this.isCaseclosed = (this.documentData['taskStatusCde'] === CONSTANTS.ACTION_TASK_STATUS_CDE.COMPLETED);
        this.refreshComponent();
        this.isDataLoaded = true;
      }
    }
  }

  ngOnInit() {
  }

  closeCase() {
    const parent = this;
    this.documentService.closeDocCase(parent.documentData.docId, parent.documentData.appUserId,
      parent.queueNameCdeSelected, function (error, data) {
        if (!error) {
          if (data['responseCde'] === 0) {
            parent.toastr.success(parent.msgInfo.getMessage(105));
            parent.isCaseEditable = false;
            parent.refreshPage();
          } else if (data['responseCde'] === 107) {
            parent.toastr.error(parent.msgInfo.getMessage(138));
          }
        } else {
          parent.toastr.error(parent.msgInfo.getMessage(102));
        }
      });
  }

  open(content, isRefreshReqd) {
    this.utilityService.toggleWhenIEPdfEmbedLayer();
    this.modalService.open(content, this.ngbModalOptions).result.then((result) => {
      this.closeResult = `Closed with: ${result}`;
      this.utilityService.toggleWhenIEPdfEmbedLayer();
      if (isRefreshReqd && result !== 'cancel') {
        this.refreshPage();
      }
    }, (reason) => {
      this.closeResult = `Dismissed ${this.getDismissReason(reason)}`;
    });
  }

  private refreshPage() {
    // this method workaround to navigate to same URL. It will refresh the page without reloading the URL
    this.router.routeReuseStrategy.shouldReuseRoute = () => false;
    this.router.onSameUrlNavigation = 'reload';
    this.router.navigateByUrl(this.router.url);
  }

  private refreshComponent() {
    const parent = this;
    parent.isDataLoaded = false;
    parent.auditData = new AuditData(parent.documentData.docId,0, 0, '', '');
    const promise = parent.utilityService.isCaseEditable(parent.documentData);
    return promise.then(function (value) {
      parent.isCaseEditable = value;
    });
  }

  private getDocUserData(){
    const parent = this;
    for (let docUserData of parent.model.docUserDataList) {
      if(docUserData.docRoleTypeCde==CONSTANTS.DOC_ROLE_TYPE.CASE_OWNER){
        parent.isReassignAllowed = true;
        parent.model.caseOwnerLoginId = docUserData.userLoginId;
      }else if(docUserData.docRoleTypeCde==CONSTANTS.DOC_ROLE_TYPE.CASE_REVIEWER){
        parent.model.caseReviewerLoginId = docUserData.userLoginId;
      }
    }
  }

  private getDismissReason(reason: any): string {
    let dismissReason = `with: ${reason}`;
    if (reason === ModalDismissReasons.ESC) {
      dismissReason = 'by pressing ESC';
    } else if (reason === ModalDismissReasons.BACKDROP_CLICK) {
      dismissReason = 'by clicking on a backdrop';
    }
    return dismissReason;
  }


}




