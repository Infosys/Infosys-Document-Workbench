/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Component, OnInit, Output, EventEmitter, Input, SimpleChange, OnChanges, OnDestroy } from '@angular/core';
import { ToastrService } from 'ngx-toastr';
import { ActionService } from '../../service/action.service';
import { EmailService } from '../../service/email.service';
import { DataService } from '../../service/data.service';
import { DocumentData } from '../../data/document-data';
import { ActionData } from '../../data/action-data';
import { TempData } from '../../data/temp-data';
import { AttributeData } from '../../data/attribute-data';
import { EmailData } from '../../data/email-data';
import { MessageInfo } from '../../utils/message-info';
import { TemplateService } from '../../service/template.service';
import { SessionService } from '../../service/session.service';
import { FormControl } from '@angular/forms';
import { BaseComponent } from '../../base.component';
import { ConfigDataHelper } from '../../utils/config-data-helper';
import { NiaTelemetryService } from '../../service/nia-telemetry.service';

@Component({
  selector: 'app-email-outbound-compose',
  templateUrl: './email-outbound-compose.component.html',
  styleUrls: ['./email-outbound-compose.component.scss']
})
export class EmailOutboundComposeComponent extends BaseComponent implements OnInit, OnChanges, OnDestroy {
  getClassName(): string {
    return "EmailOutboundComposeComponent";
  }

  @Output() close = new EventEmitter<string>();
  @Output() sentEmailAdded = new EventEmitter<boolean>();
  @Input('minheight') minheight: number;
  @Input('textareaheight') textareaheight: number;
  @Input('closeButton') closeButton: boolean;
  @Input('popupButton') popupButton = true;
  private docActionSubscription: any;
  closeResult: string;
  templateDataList: TempData[] = [];
  emailSendStatus = '';
  documentData: DocumentData;
  docId;
  templateNameSelected: string;
  templateText = '';
  lastAddedActionNameCde: number;
  draftData: EmailData;
  to = '';
  cC = '';
  attr: String;
  subject: String;
  isBtnSendClicked: boolean;
  isDataLoaded: boolean;
  ccAddressValue = '';
  ccAddressIdVal: string;
  ccAddressIdValue = '';
  ccAddressList: string[];
  ccAddressIdList: string[];
  toAddress = '';
  toAddressList: string[];
  toAddressIdVal: string;
  toAddressIdList: string[];
  bccAddressId = '';
  i = 0;
  isRecommendedTemplate = false;
  fileAttachments: FileList[] = [];
  formData: FormData;

  emptyArray: any[] = [];
  placeholderValue = '';
  editor_modules = {
    toolbar: [
      ['bold', 'italic', 'underline', 'strike'],        // toggled buttons
      ['blockquote', 'code-block'],

      [{ 'list': 'ordered' }, { 'list': 'bullet' }],
      [{ 'indent': '-1' }, { 'indent': '+1' }],          // outdent/indent

      [{ 'color': this.emptyArray.slice() }, { 'background': this.emptyArray.slice() }],          // dropdown with defaults from theme
      [{ 'font': this.emptyArray.slice() }],
      [{ 'align': this.emptyArray.slice() }],

      ['clean'],                                         // remove formatting button

      ['link', 'image']                                  // link and image
    ]
  };
  emailBody = new FormControl();
  emailBodyLoaded = false;

  constructor(private actionService: ActionService, private emailService: EmailService,
    private templateService: TemplateService, private dataService: DataService,
    private toastr: ToastrService, private msgInfo: MessageInfo,
    public sessionService: SessionService, public configDataHelper: ConfigDataHelper, public niaTelemetryService:NiaTelemetryService) {
      super(sessionService, configDataHelper, niaTelemetryService)
    }

  private _isDataReady: boolean;

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
        const to = changes['document'].currentValue;
        this.documentData = to;
        if (this.documentData != null) {
          this.docId = this.documentData.docId;
          this.emailSendStatus = '';
          this.getValues();
          this.getTemplateData();
        }
      }
    }
  }


  ngOnInit() {
    const parent = this;
    // Get publication of new action added event
    this.docActionSubscription = this.dataService.docActionAddedEvent.subscribe(message => {
      if (message) {
        parent.getActionTaskList(parent.documentData.docId);
      }
    });
    this.getValues();
    window.onbeforeunload = (ev) => {
      // finally return the message to browser api.
      const dialogText = this.msgInfo.getMessage(130);
      ev.returnValue = dialogText;
      return dialogText;
    };
  }

  ngOnDestroy() {
    this.docActionSubscription.unsubscribe();
    window.onbeforeunload = null;
  }


  private getValues() {
    const parent = this;
    this.getDraft(function (isCallDone) {
      if (isCallDone) {
        parent.attr = parent.draftData.emailTo;
        console.log(parent.draftData);
        parent.to = parent.draftData.emailTo;
        parent.cC = parent.draftData.emailCC;
        parent.bccAddressId = parent.draftData.emailBCC;
        console.log(parent.bccAddressId + '1 in get values');
        if (parent.draftData.emailSubject != null && parent.draftData.emailSubject.length > 0) {
          parent.subject = parent.draftData.emailSubject;
        }
      }
      isCallDone = false;
    });
  }

  sentAttachmentAdded(attachmentFileList: FileList[]) {
    const parent = this;

    parent.fileAttachments = attachmentFileList;
    const formData = new FormData();

    if (parent.fileAttachments.length > 0) {
      let fileCount = 0;
      const file = File;
      for (let i = 0; i < parent.fileAttachments.length; i++) {
        file[i] = parent.fileAttachments[i];
        fileCount += 1;
        formData.append('file' + fileCount, file[i]);
      }
      parent.formData = formData;
    } else {
      parent.formData = undefined;
    }
  }


  sendEmail(toUpdated, cCUpdated) {

    if (this.templateText != null && this.templateText.length > 2000000) {
      this.toastr.error(this.msgInfo.getMessage(128));
    } else {

      if (this.isBtnSendClicked) {
        return;
      }
      const parent = this;

      if (parent.formData == undefined) {
        parent.formData = new FormData();
      }

      parent.dataService.publishDocActionAddedEvent(true);
      console.log(parent.formData);
      this.emailService.sendMail(this.documentData.docId, toUpdated, cCUpdated,
        this.bccAddressId, this.subject, this.templateText, this.templateNameSelected, parent.formData, function (error, data) {
          if (!error) {

            const emailData: any = data;
            console.log('emailData', emailData);
            if (emailData.responseMsg == parent.msgInfo.getMessage(106)) {
              parent.sentEmailAdded.emit(true);
              parent.closeWindow();
              parent.toastr.success(parent.msgInfo.getMessage(106));
            } else if (emailData.responseMsg == parent.msgInfo.getMessage(122)) {
              parent.closeWindow();
              parent.toastr.error(parent.msgInfo.getMessage(122));
            } else if (emailData.responseMsg == parent.msgInfo.getMessage(123)) {
              parent.closeWindow();
              parent.toastr.error(parent.msgInfo.getMessage(123));
            } else if (emailData.responseMsg == parent.msgInfo.getMessage(124)) {
              parent.closeWindow();
              parent.toastr.error(parent.msgInfo.getMessage(124));
            } else if (emailData.responseMsg === parent.msgInfo.getMessage(132)) {
              parent.toastr.error(parent.msgInfo.getMessage(132));
              parent.isBtnSendClicked = false;
              return;
            }
          } else {
            parent.closeWindow();
            parent.toastr.error(parent.msgInfo.getMessage(107));
            console.log(error);
          }
        });
      this.isBtnSendClicked = true;
    }
  }

  closeWindow() {
    this.close.emit('window closed');
  }

  /************************* PRIVATE METHODS **************************/

  private getActionTaskList(docId: number) {
    // this.actionDataList = [];//Set to Empty list before call to make UI blank out faster
    const parent = this;
    this.actionService.getActions(docId, function (error, data) {
      if (!error) {
        const documentDataList: DocumentData[] = data;
        if (documentDataList.length > 0) {

          const actionDataList: ActionData[] = documentDataList[0].actionDataList;
          parent.lastAddedActionNameCde = actionDataList[actionDataList.length - 1].actionNameCde;
        }
      }
    });
  }

  modalWindow() {
    // To check whether emailbody is saved or not.
    if (this.emailBody.dirty) {
      if (!confirm(this.msgInfo.getMessage(131))) {
        return;
      }
    }
    this.close.emit('window closed');
    this.closeButton = false;
    this.popupButton = true;
  }

  private getTemplateData() {
    const parent = this;
    this.templateService.getTemplateData(this.docId, function (error, data) {
      if (!error) {
        if (data) {
          const tempDataList: TempData[] = data;
          parent.templateDataList = tempDataList;
          parent.isDataLoaded = true;

          console.log(parent.templateDataList);
          if (parent.draftData.emailOutboundId == 0) { // To check draft exists or not.
            for (let i = 0; i < parent.templateDataList.length; i++) {
              parent.isRecommendedTemplate = data[i].isRecommendedTemplate;
              if (parent.isRecommendedTemplate) {
                parent.templateNameSelected = parent.templateDataList[i].templateName;
                parent.templateText = parent.templateDataList[i].templateHtml;
                parent.emailBodyLoaded = true;
                break;
              }
            }
          }
        } else {
          parent.isDataLoaded = true;
        }
      }
    });
  }

  ProcessTemplate() {
    const parent = this;
    if ((parent.templateDataList != null) || (parent.templateDataList.length != 0)) {
      const filteredData = this.templateDataList.filter(function (p) {
        if (p.templateName == parent.templateNameSelected) {
          return p.templateHtml;
        }
        return null;
      });
      if ((filteredData != null) || (filteredData.length != 0)) {
        this.templateText = filteredData[0].templateHtml;
      } else {
        this.templateText = '';
      }
    } else {
      this.templateText = '';
    }
  }

  saveDraft(toUpdated, ccUpdated) {
    if (this.templateText != null && this.templateText.length > 2000000) {
      this.toastr.error(this.msgInfo.getMessage(128));
    } else {
      const parent = this;

      console.log(parent.formData);
      if (parent.formData !== undefined) {
        parent.formData = new FormData();
        parent.toastr.error(parent.msgInfo.getMessage(129));
      } else {
        if (parent.draftData) {
          parent.emailService.updateDraft(parent.documentData.docId, toUpdated, ccUpdated,
            parent.bccAddressId, parent.templateText, parent.subject, parent.draftData.docOutgoingEmailRelId, function (error, data) {
              if (!error) {
                const emailData: any = data;
                console.log('emailData', emailData);
                if (emailData.responseMsg == parent.msgInfo.getMessage(125)) {
                  parent.emailBody.markAsPristine();
                  parent.toastr.success(parent.msgInfo.getMessage(125));
                } else if (emailData.responseMsg == parent.msgInfo.getMessage(122)) {
                  parent.toastr.error(parent.msgInfo.getMessage(122));
                } else if (emailData.responseMsg == parent.msgInfo.getMessage(123)) {
                  parent.toastr.error(parent.msgInfo.getMessage(123));
                } else if (emailData.responseMsg == parent.msgInfo.getMessage(124)) {
                  parent.toastr.error(parent.msgInfo.getMessage(124));
                } else if (emailData.responseMsg == parent.msgInfo.getMessage(132)) {
                  parent.toastr.error(parent.msgInfo.getMessage(132));
                }
              } else {
                console.log(error);
                parent.toastr.error(parent.msgInfo.getMessage(109));

              }

            });
        } else {
          parent.emailService.addDraft(parent.documentData.docId, toUpdated, ccUpdated, parent.bccAddressId,
            parent.templateText, parent.subject, function (error, data) {
              if (!error) {
                parent.emailBody.markAsPristine();
                parent.toastr.success(parent.msgInfo.getMessage(108));
              } else if (this.emailData.responseMsg == parent.msgInfo.getMessage(132)) {
                parent.toastr.error(parent.msgInfo.getMessage(132));
              } else {
                console.log(error);
                parent.toastr.error(parent.msgInfo.getMessage(109));
              }

            });
        }
      }

    }
  }

  getDraft(callback) {

    const parent = this;
    parent.i++;

    if (parent.i === 1) {
      this.emailService.getEmailDraftData(this.docId, function (error, data) {
        if (!error) {
          parent.draftData = data;

          if (parent.draftData != null) {
            parent.templateText = parent.draftData.emailBodyHtml;
            parent.emailBodyLoaded = true;
            if (parent.draftData.emailSubject != null) {
              if ((parent.draftData.emailSubject).startsWith('RE')) {
                parent.subject = parent.draftData.emailSubject;
                callback(true);
              }

            } else {
              parent.subject = 'RE: ' + parent.draftData.emailSubject;
              callback(true);
            }
          }
        }
      });
    } else {
      callback(false);
    }

  }

  getValueForName(name: string) {
    let result = '';
    if ((this.documentData.attributes != null) || (this.documentData.attributes.length !== 0)) {
      const attributeData: AttributeData[] = this.documentData.attributes.filter(function (a) {
        if (a.attrNameTxt == name) {
          return true;
        }
      });
      if (attributeData != null && attributeData.length > 0) {
        result = attributeData[0].attrValue;
      }
    }
    return result;
  }

  // After data loaded making emailbody as pristine.
  onEditorContentChange() {
    if (this.emailBodyLoaded) {
      this.emailBody.markAsPristine();
      this.emailBodyLoaded = false;
    }
  }

}
