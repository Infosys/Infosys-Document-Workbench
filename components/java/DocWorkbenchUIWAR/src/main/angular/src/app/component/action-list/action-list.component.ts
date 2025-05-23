/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Component, OnInit, Output, EventEmitter, Input, OnChanges, SimpleChange } from '@angular/core';
import { DataService } from '../../service/data.service';
import { ActionService } from '../../service/action.service';
import { ActionData } from '../../data/action-data';
import { DocumentData } from '../../data/document-data';
import { NgbModal, ModalDismissReasons, NgbModalOptions } from '@ng-bootstrap/ng-bootstrap';
import { CONSTANTS } from '../../common/constants';
import { UtilityService } from '../../service/utility.service';
import { SessionService } from '../../service/session.service';
import { BaseComponent } from '../../base.component';
import { ConfigDataHelper } from '../../utils/config-data-helper';
import { NiaTelemetryService } from '../../service/nia-telemetry.service';
import { DocumentUserData } from '../../data/document-user-data';

@Component({
  selector: 'app-action-list',
  templateUrl: './action-list.component.html',
  styleUrls: ['./action-list.component.scss']
})
export class ActionListComponent extends BaseComponent implements OnInit, OnChanges {
  getClassName(): string {
    return "ActionListComponent";
  }
  @Output() close = new EventEmitter<string>();
  @Output() isComponentMinimized = new EventEmitter<boolean>();
  @Input() closeButton: boolean;
  @Input() minheight: number;
  @Input() isDocTypeFile: boolean;
  @Input() popupButton = true;
  @Input()
  set docUserDataList(docUserDataList:DocumentUserData[]){
    this.model.docUserDataList = docUserDataList
  }

  @Input()
  set isEnableAnnotation(isEnableAnnotation: boolean) {
    this.model.isEnableAnnotation = isEnableAnnotation;
  }
  closeResult: string;
  documentData: DocumentData;
  actionDataList: ActionData[] = [];
  isDataLoaded: boolean;
  reextractAction: number = CONSTANTS.ACTION_NAME_CDE.RE_EXTRACT_DATA;
  forYourReview: number = CONSTANTS.ACTION_TASK_STATUS_CDE.FOR_YOUR_REVIEW;
  completed: number = CONSTANTS.ACTION_TASK_STATUS_CDE.COMPLETED;
  reExtractedActionData: ActionData;
  private docActionSubscription: any;

  model: any = {
    isEnableAnnotation: Boolean,
    docUserDataList: undefined,
    minimizeAction:false,
    actionDisplay:"block"
  };

  // For disabling mouse click and keystrokes outside modal window
  ngbModalOptions: NgbModalOptions = {
    backdrop: 'static',
    keyboard: false
  };
  reExtractActionCompletedEvent: any;

  constructor(private dataService: DataService, private actionService: ActionService, private modalService: NgbModal,
    private utilityService: UtilityService, public sessionService: SessionService,
    public configDataHelper: ConfigDataHelper, public niaTelemetryService:NiaTelemetryService) {
    super(sessionService, configDataHelper, niaTelemetryService);
  }


  _isDataReady: boolean;

  @Input()
  set document(docData: DocumentData) {
  }

  @Input()
  set isDataReady(isDataReady: boolean) {
    this._isDataReady = isDataReady;
  }

  ngOnChanges(changes: { [propKey: string]: SimpleChange }) {

    if (changes['document']) {
      if (this._isDataReady) {
        this.isDataLoaded = false;
        this.actionDataList = [];
        const to = changes['document'].currentValue;
        this.documentData = to;
        this.getActionTaskList();
      }
    }
  }

  ngOnInit() {
    const parent = this;
    // //Get publication of new action added event
    this.docActionSubscription = this.dataService.docActionAddedEvent.subscribe(message => {
      if (message) {
        parent.getActionTaskList();
      }
    });

    this.reExtractActionCompletedEvent = this.dataService.reExtractActionCompletedEvent.subscribe(message => {
      if (message) {
        parent.getActionTaskList();
      }
    });
  }

  ngOnDestroy() {
    this.docActionSubscription.unsubscribe();
    this.reExtractActionCompletedEvent.unsubscribe();
  }

  public refreshComponent() {
    this.getActionTaskList();
  }

  /************************* PRIVATE METHODS *************************/

  private getActionTaskList() {
    this.isDataLoaded = false;
    this.actionDataList = []; // Set to Empty list before call to make UI blank out faster
    const parent = this;
    if (parent.documentData != null) {
      parent.actionService.getActions(parent.documentData.docId, function (error, data) {
        if (!error) {
          const documentDataList: DocumentData[] = data;
          if (documentDataList.length > 0) {
            parent.actionDataList = documentDataList[0].actionDataList;
            if (parent.actionDataList.length > 0) {
              parent.actionDataList = parent.actionDataList.reverse();
            }
          }
        }
        parent.isDataLoaded = true;
      });
    } else {
      parent.isDataLoaded = true;
    }
  }

  open(content) {
    this.utilityService.toggleWhenIEPdfEmbedLayer(this.closeButton);
    this.modalService.open(content, this.ngbModalOptions).result.then((result) => {
      this.closeResult = `Closed with: ${result}`;
      this.utilityService.toggleWhenIEPdfEmbedLayer(this.closeButton);
      console.log('result', result);
      this.getActionTaskList();
    }, (reason) => {
      this.closeResult = `Dismissed ${this.getDismissReason(reason)}`;
    });
  }

  openReExtractedModal(content, actionData: ActionData) {
    this.reExtractedActionData = actionData;
    this.utilityService.toggleWhenIEPdfEmbedLayer(this.closeButton);
    this.modalService.open(content, this.ngbModalOptions).result.then((result) => {
      this.closeResult = `Closed with: ${result}`;
      this.utilityService.toggleWhenIEPdfEmbedLayer(this.closeButton);
    }, (reason) => {
      this.closeResult = `Dismissed ${this.getDismissReason(reason)}`;
    });
  }

  private getDismissReason(reason: any): string {
    if (reason == ModalDismissReasons.ESC) {
      return 'by pressing ESC';
    } else if (reason == ModalDismissReasons.BACKDROP_CLICK) {
      return 'by clicking on a backdrop';
    } else {
      return `with: ${reason}`;
    }
  }

  modalWindow() {
    this.close.emit('window closed');
    this.closeButton = false;
    this.popupButton = true;
  }

  actionHeightChange(){
    this.model.minimizeAction=!this.model.minimizeAction
    if (this.model.minimizeAction){
      this.model.actionDisplay="none"
    }else{
      this.model.actionDisplay="block"
    }
    this.isComponentMinimized.emit(this.model.minimizeAction);
  }

}
