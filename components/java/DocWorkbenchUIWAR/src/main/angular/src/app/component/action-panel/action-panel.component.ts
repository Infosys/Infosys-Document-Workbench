/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Component, OnInit, OnChanges, SimpleChange, Input } from '@angular/core';
import { DataService } from '../../service/data.service';
import { NgbModal, ModalDismissReasons, NgbModalOptions } from '@ng-bootstrap/ng-bootstrap';
import { DocumentData } from '../../data/document-data';
import { UtilityService } from '../../service/utility.service';
import { BaseComponent } from '../../base.component';
import { SessionService } from '../../service/session.service';
import { ConfigDataHelper } from '../../utils/config-data-helper';
import { NiaTelemetryService } from '../../service/nia-telemetry.service';
import { UserData } from '../../data/user-data';
import { ActionData } from '../../data/action-data';
import { AttributeService } from '../../service/attribute.service';
import { AttributeData } from '../../data/attribute-data';
import { CONSTANTS } from '../../common/constants';
import { ActionService } from '../../service/action.service';
import { ToastrService } from 'ngx-toastr';
import { MessageInfo } from '../../utils/message-info';
import { DocumentService } from '../../service/document.service';
import { AttributeHelper } from '../../utils/attribute-helper';
import { DocumentUserData } from '../../data/document-user-data';

@Component({
  selector: 'app-action-panel',
  templateUrl: './action-panel.component.html',
  styleUrls: ['./action-panel.component.scss']
})
export class ActionPanelComponent extends BaseComponent implements OnInit, OnChanges {
  getClassName(): string {
    return "ActionPanelComponent";
  }
  isBtnShowActionsClicked: boolean;
  private isBtnExecuteClicked = true;

  // For disabling mouse click and keystrokes outside modal window
  ngbModalOptions: NgbModalOptions = {
    backdrop: 'static',
    keyboard: false
  };

  constructor(private actionService: ActionService,private modalService: NgbModal, private dataService: DataService, private utilityService: UtilityService,
    public sessionService: SessionService,private toastr: ToastrService,private msgInfo: MessageInfo, private attributeHelper: AttributeHelper,public configDataHelper: ConfigDataHelper, private attributeService: AttributeService, 
    public niaTelemetryService:NiaTelemetryService, private documentService: DocumentService) {
    super(sessionService, configDataHelper, niaTelemetryService)
   }

  docId: number;
  document: DocumentData;
  _isDataReady: boolean;
  closeResult: string;
  private actionDataList: ActionData[] = [];
  private attachmentAttrDataList: AttributeData[] = [];
  private loggedInUserData: UserData;
  private caseReviewerId: number;
  
  @Input() isDocTypeFile: boolean;
  @Input()
  set docUserDataList(docUserDataList:DocumentUserData[]){
    this.model.docUserDataList = docUserDataList
  }

  @Input()
  set documentData(docData: DocumentData) {

  }

  @Input()
  set isDataReady(isDataReady: boolean) {
    this._isDataReady = isDataReady;
  }

  model: any = {
    ACTION_DATA_ENTRY_COMP: CONSTANTS.ACTION_NAME_CDE.DATA_ENTRY_COMPLETE,
    ACTION_DATA_ENTRY_APPRV: CONSTANTS.ACTION_NAME_CDE.DATA_ENTRY_APPROVE,
    ACTION_DATA_ENTRY_REJECT: CONSTANTS.ACTION_NAME_CDE.DATA_ENTRY_REJECT,
    userData: UserData,
    docUserDataList: undefined
  }

  ngOnChanges(changes: { [propKey: string]: SimpleChange }) {
    this.docId = 0;
    if (changes['documentData']) {
      if (this._isDataReady) {
        const to = changes['documentData'].currentValue;
        this.document = to;
        if (this.document != null) {
          this.docId = this.document.docId;
        }
      }
      if (this.document != null) {
        const parent = this;
        const promise = this.utilityService.isCaseEditable(parent.document);
        return promise.then(function (value) {
          if (value) {
            parent.isBtnShowActionsClicked = false;
          } else {
            parent.isBtnShowActionsClicked = true;

          }
        });
      }
    }
  }


  ngOnInit() {
    const parent = this;
    parent.getActionMapping();
    parent.actionService.getActions(this.docId, function (error, data) {
      let wholeSet = [];
      if (!error && data[0] !== undefined) {
        wholeSet = data[0].actionDataList;
        let lastSet = wholeSet[wholeSet.length - 1];
        if (lastSet.taskStatusCde == CONSTANTS.ACTION_TASK_STATUS_CDE.INPROGRESS) {
          parent.isBtnExecuteClicked = false;
        }
      }

    });

    parent.sessionService.getLoggedInUserDetails(function (error, data) {
      if (data) {
        let loggedInUserData = data as UserData;
        parent.loggedInUserData = loggedInUserData;
      }
    });

    for (let docUserData of parent.model.docUserDataList) {
      if(docUserData.docRoleTypeCde==CONSTANTS.DOC_ROLE_TYPE.CASE_OWNER){
      }else if(docUserData.docRoleTypeCde==CONSTANTS.DOC_ROLE_TYPE.CASE_REVIEWER){
        parent.caseReviewerId = docUserData.userId;
      }
    }

  }

  open(content) {
    if (this.isBtnShowActionsClicked) {
      return;
    }
    this.utilityService.toggleWhenIEPdfEmbedLayer();
    this.modalService.open(content, this.ngbModalOptions).result.then((result) => {
      this.closeResult = `Closed with: ${result}`;
      this.utilityService.toggleWhenIEPdfEmbedLayer();
    }, (reason) => {
      this.closeResult = `Dismissed ${this.getDismissReason(reason)}`;
    });
    // this.isBtnShowActionsClicked=true;
  }

  validateShowActionBtn() {
    return this.docId === 0 || this.isBtnShowActionsClicked;
  }

  validateApprvRejectBtn(){
    if(this.caseReviewerId == undefined){
      return false;
    }
    else if((this.loggedInUserData.userId === this.caseReviewerId && this.getFeature(this.bmodel.FID.ACTION_DATA_ENTRY_APPROVED).isEnabled && this.getFeature(this.bmodel.FID.ACTION_DATA_ENTRY_REJECT).isEnabled)){
      //this.isBtnShowActionsClicked = false;
      return true;
    }
    return false;
  }


  executeAction(actionNameCde: number) {
    if(actionNameCde == this.model.ACTION_DATA_ENTRY_APPRV){
      if(!this.getFeature(this.bmodel.FID.ACTION_DATA_ENTRY_APPROVED).isEnabled || (this.validateShowActionBtn() && !this.validateApprvRejectBtn())){
        return
      }
    }
    else if(actionNameCde == this.model.ACTION_DATA_ENTRY_REJECT){
      if(!this.getFeature(this.bmodel.FID.ACTION_DATA_ENTRY_REJECT).isEnabled || (this.validateShowActionBtn() && !this.validateApprvRejectBtn())){
        return
      }
    }
    else if(actionNameCde == this.model.ACTION_DATA_ENTRY_COMP){
      if(!this.getFeature(this.bmodel.FID.ACTION_DATA_ENTRY_COMPLETED).isEnabled || this.validateShowActionBtn()){
        return;
      }
    }
    const parent = this;
    parent.isBtnExecuteClicked = true;
    parent.actionService.getActions(parent.document.docId, function (getActionServiceError, getActionServiceData) {
      let tempDataList = [];
      if (getActionServiceError) {
        parent.isBtnExecuteClicked = false;
      }
      if (!getActionServiceError && getActionServiceData[0] !== undefined) {
        tempDataList = getActionServiceData[0].actionDataList;
      }
      if (parent.utilityService.isReExtractPending(tempDataList)) {
        parent.toastr.error(parent.msgInfo.getMessage(148));
      }
      else {
        const actionDataList: ActionData[] = parent.actionDataList.filter(function (p) {
          if (p.actionNameCde === actionNameCde) {
            return true;
          }
          return false;
        });
        actionDataList[0].taskTypeCde = 2;
        const documentDataList: DocumentData[] = [];
        documentDataList.push(parent.document);
        documentDataList[0].actionDataList = actionDataList;
        parent.actionService.saveAction(documentDataList, function (error, data) {
          if (!error) {
            parent.dataService.publishDocActionAddedEvent(true);
            console.log('Save successful');
            parent.toastr.success(parent.msgInfo.getMessage(101));
            parent.isBtnExecuteClicked = false;
            let cdata=parent.attributeHelper.getTelemetryEventActionParams(parent.document, parent.attachmentAttrDataList, parent.model.docUserDataList) as [];
            parent.triggerTelemetryEvents(parent.bmodel.TELE_EVENTS.INTERACT, parent.getSelectedActionName(actionNameCde), cdata);
          } else {
            parent.isBtnExecuteClicked = true;
            console.log(error);
          }
        });
      }
    })    
  }

  private getDismissReason(reason: any): string {
    if (reason === ModalDismissReasons.ESC) {
      return 'by pressing ESC';
    } else if (reason === ModalDismissReasons.BACKDROP_CLICK) {
      return 'by clicking on a backdrop';
    } else {
      return `with: ${reason}`;
    }
  }

  private getSelectedActionName(actionNameCde: number) {
    const parent = this;
    if (parent.model.ACTION_DATA_ENTRY_COMP == actionNameCde) {
      return CONSTANTS.TELEMETRY_INTERACT_NAME.ACT_PER.ACTION_DATA_ENTRY_COMP;
    }
    if (parent.model.ACTION_DATA_ENTRY_APPRV == actionNameCde) {
      return CONSTANTS.TELEMETRY_INTERACT_NAME.ACT_PER.ACTION_DATA_ENTRY_APPRV;
    }
    if (parent.model.ACTION_DATA_ENTRY_REJECT == actionNameCde) {
      return CONSTANTS.TELEMETRY_INTERACT_NAME.ACT_PER.ACTION_DATA_ENTRY_REJECT
    }
  }

  private getActionMapping() {
    const parent = this;
    parent.actionService.getActionMapping(function (error, data) {
      if (!error) {
        const actionDataList: ActionData[] = data;
        parent.actionDataList = actionDataList.filter(a => a.actionNameCde !== CONSTANTS.ACTION_NAME_CDE.RE_EXTRACT_DATA);
      }
    });
  }

}
