/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Component, OnInit, Input, Output, EventEmitter, OnChanges, SimpleChange } from '@angular/core';
import { EmailService } from '../../service/email.service';
import { DocumentData } from '../../data/document-data';
import { EmailData } from '../../data/email-data';
import { NgbModal, ModalDismissReasons, NgbModalOptions } from '@ng-bootstrap/ng-bootstrap';
import { UtilityService } from '../../service/utility.service';
import { BaseComponent } from '../../base.component';
import { SessionService } from '../../service/session.service';
import { ConfigDataHelper } from '../../utils/config-data-helper';
import { NiaTelemetryService } from '../../service/nia-telemetry.service';
@Component({
  selector: 'app-email-outbound-list',
  templateUrl: './email-outbound-list.component.html',
  styleUrls: ['./email-outbound-list.component.scss']
})
export class EmailOutboundListComponent extends BaseComponent implements OnInit, OnChanges {
  getClassName(): string {
    return "EmailOutboundListComponent";
  }
  @Input('minheight') minheight: number;
  @Output() close = new EventEmitter<string>();
  @Input('closeButton') closeButton: boolean;
  @Input('popupButton') popupButton = true;
  isBtnComposeEmailEnabled: boolean;
  closeResult: string;
  selectedEmailData: EmailData;
  documentData: DocumentData;
  isDataLoaded: boolean;
  emailDataList: EmailData[] = [];

  // For disabling mouse click and keystrokes outside modal window
  ngbModalOptions: NgbModalOptions = {
    backdrop: 'static',
    keyboard: false
  };

  constructor(private emailService: EmailService, private modalService: NgbModal,
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
      this.emailDataList = [];
      this.isDataLoaded = false;
      this.documentData = null;
      if (this._isDataReady) {
        const to = changes['document'].currentValue;
        this.documentData = to;

        if (this.documentData != null) {
          this.getSentEmailList(this.documentData.docId);
          this.isCaseEditable();
        } else {
          this.isDataLoaded = true;
        }
      }
    }
  }

  ngOnInit() {
  }

  private sentEmailAdded(isEmailSent: any) {
    console.log(isEmailSent);
    if (isEmailSent) {
      this.getSentEmailList(this.documentData.docId);
    }
  }


  private getSentEmailList(docId: number) {
    this.isDataLoaded = false;
    this.emailDataList = []; // Set to Empty list before call to make UI blank out faster
    const parent = this;
    this.emailService.getSentEmailList(docId, function (error, data) {
      if (!error) {
        parent.emailDataList = data;
        if (parent.emailDataList.length > 0) {
          parent.emailDataList = parent.emailDataList.reverse();
        }
      }
      parent.isDataLoaded = true;
    });
  }

  openSentEmailContent(content, emailData: EmailData) {
    this.selectedEmailData = emailData;
    this.modalService.open(content, this.ngbModalOptions).result.then((result) => {
      this.closeResult = `Closed with: ${result}`;
    }, (reason) => {
      this.closeResult = `Dismissed ${this.getDismissReason(reason)}`;
    });
  }

  open(content) {
    this.modalService.open(content, this.ngbModalOptions).result.then((result) => {
      this.closeResult = `Closed with: ${result}`;
      this.getSentEmailList(this.documentData.docId);
    }, (reason) => {
      this.closeResult = `Dismissed ${this.getDismissReason(reason)}`;
    });
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

  modalWindow() {
    this.close.emit('window closed');
    this.closeButton = false;
    this.popupButton = true;
  }

  private isCaseEditable() {
    const parent = this;

    const promise = this.utilityService.isCaseEditable(parent.documentData);
    return promise.then(function (value) {
      if (value) {
        parent.isBtnComposeEmailEnabled = false;
      } else {
        parent.isBtnComposeEmailEnabled = true;
      }
    });
  }
}

