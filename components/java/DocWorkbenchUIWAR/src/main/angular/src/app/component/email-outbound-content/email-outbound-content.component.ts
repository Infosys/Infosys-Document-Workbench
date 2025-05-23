/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { DataService } from '../../service/data.service';
import { ActionService } from '../../service/action.service';
import { DocumentData } from '../../data/document-data';
import { EmailData } from '../../data/email-data';
import { NgbModal, ModalDismissReasons, NgbModalOptions } from '@ng-bootstrap/ng-bootstrap';
import { AttachmentService } from '../../service/attachment.service';
import { AttachmentData } from '../../data/attachment-data';
import { UtilityService } from '../../service/utility.service';
import { DomSanitizer } from '@angular/platform-browser';

@Component({
  selector: 'app-email-outbound-content',
  templateUrl: './email-outbound-content.component.html',
  styleUrls: ['./email-outbound-content.component.scss']
})

export class EmailOutboundContentComponent implements OnInit {
  @Input('minheight') minheight: number;
  @Output() close = new EventEmitter<string>();
  @Input('closeButton') closeButton: boolean;
  @Input('selectedEmailData') selectedEmailData: EmailData;
  closeResult: string;
  documentData: DocumentData;
  emailDataList: EmailData[] = [];
  isDataLoaded: boolean = false;

  //For disabling mouse click and keystrokes outside modal window
  ngbModalOptions: NgbModalOptions = {
    backdrop: 'static',
    keyboard: false
  };

  constructor(protected sanitizer: DomSanitizer, private dataService: DataService, private actionService: ActionService, private attachmentService: AttachmentService, private utilityService: UtilityService, private modalService: NgbModal) { }

  model: any = {
    emailBodyHtml: "",
    emailBodyText: ""
  }; // For binding to view

  ngOnInit() {
    if (this.selectedEmailData.emailBodyHtml == null) {
      this.model.emailBodyText = this.selectedEmailData.emailBodyText;
      (this.model.emailBodyText.replace("\n", "<br />")).trim()
    } else {
      let emailHtml: string = this.transform(this.selectedEmailData.emailBodyHtml);
      this.model.emailBodyHtml = emailHtml;
    }
  }

  transform(htmlString: string): any {
    return this.sanitizer.bypassSecurityTrustHtml(htmlString);
  }

  viewAttachments(emailOutboundId: number, attachmentId: number, fileName: string) {
    console.log("*****************" + attachmentId)
    this.attachmentService.viewAttachmentEmail(emailOutboundId, attachmentId, fileName, function (error, response) {
      // if (!error) {
      //   let attachmentDataList : AttachmentData[] = response;
      //   parent.attachmentDataList = response;
      // }
    });
  }

  open(content) {
    this.modalService.open(content, this.ngbModalOptions).result.then((result) => {
      this.closeResult = `Closed with: ${result}`;
    }, (reason) => {
      this.closeResult = `Dismissed ${this.getDismissReason(reason)}`;
    });
  }

  modalWindow() {
    this.close.emit('window closed');
    this.closeButton = false;
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

  getMaskedFileName(fileName: string, limit: number) {
    let parent = this;
    return parent.utilityService.getTruncatedFileName(fileName, limit);
  }

}
