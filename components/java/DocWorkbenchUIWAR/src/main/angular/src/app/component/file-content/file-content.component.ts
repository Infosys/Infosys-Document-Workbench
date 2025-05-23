/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import {
  Component, EventEmitter, Input,
  OnChanges, OnDestroy, OnInit, Output,
  SimpleChange
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { NgbModal, NgbModalOptions } from '@ng-bootstrap/ng-bootstrap';
import { ToastrService } from 'ngx-toastr';
import { SessionService } from '../../service/session.service';
import { CONSTANTS } from '../../common/constants';
import { AttachmentData } from '../../data/attachment-data';
import { AttributeData } from '../../data/attribute-data';
import { DocumentData } from '../../data/document-data';
import { RangeData } from '../../data/range-data';
import { ActionService } from '../../service/action.service';
import { AttachmentService } from '../../service/attachment.service';
import { AttributeService } from '../../service/attribute.service';
import { DataService } from '../../service/data.service';
import { UtilityService } from '../../service/utility.service';
import { AttributeHelper } from '../../utils/attribute-helper';
import { ConfigDataHelper } from '../../utils/config-data-helper';
import { MessageInfo } from '../../utils/message-info';
import { BaseComponent } from '../../base.component';
import { NiaAnnotatorUtil } from '../nia-document-annotator/nia-annotator-util';
import { NiaTelemetryService } from '../../service/nia-telemetry.service';

@Component({
  selector: 'app-file-content',
  templateUrl: './file-content.component.html',
  styleUrls: ['./file-content.component.scss']
})
export class FileContentComponent extends BaseComponent implements OnInit, OnDestroy, OnChanges {
  getClassName(): string {
    return "FileContentComponent";
  }
  private static URL_WORK_DATA = '/home/workdata';
  @Input()
  set isDataReady(isDataReady: boolean) {
    this.model._isDataReady = isDataReady;
  }
  @Input() height: number;
  @Input() width: number;
  @Input() closeButton: boolean;
  @Input() popupButton = true;
  @Input() documentData: DocumentData;
  @Input() isEnableAnnotation: boolean;
  @Input() isCaseEmail: boolean;
  @Input() minbodyheight: number;

  @Input()
  set isDocTypeFile(isDocTypeFile: boolean) {
    this.model.isDocTypeFile = isDocTypeFile;
  }

  // To simulate the DB call while page load, attachmentDataList received separately from parent component
  private _attachmentDataListFromParent: AttachmentData[];
  @Input()
  set attachmentDataList(attachmentDataList: AttachmentData[]) {
    this._attachmentDataListFromParent = attachmentDataList;
  }
  // To simulate the DB call while page load, attachmentAttrDataList received separately from parent component
  private _attachmentAttrDataListFromParent: AttributeData[];
  @Input()
  set attachmentAttrDataList(attachmentAttrDataList: AttributeData[]) {
    this._attachmentAttrDataListFromParent = attachmentAttrDataList;
  }

  @Input()
  set queueNameCde(queueNameCde: number) {
    this.model.queueNameCde = queueNameCde;
  }

  @Output() close = new EventEmitter<string>();
  model: any = {
    _isDataReady: undefined,
    isDataLoaded: undefined,
    isShowDataLoad: false,
    fileContent: undefined,
    fileUrl: undefined,
    fileName: '',
    docId: undefined,
    attachmentId: undefined,
    isAnnotationVisible: undefined,
    isAnnotatorReadOnly: false,
    annotationList: undefined,
    isCaseEditable: undefined,
    isAnnotationNotEnabled: true,
    options: [],
    groupedAttachmentList: [],
    isDocTypeFile: Boolean,
    fileViewToggle: '',
    fileViewer: '',
    annotationInfoMsg: undefined,
    isPlainTextVersionExists: false,
    isOrgPlainTxt: false,
    isEmailTabSelected: false,
    isEmailAttachmentTabSelected: false,
    queueNameCde: undefined,
    annotatorHeight: 0,
    renderPdfPage: 1,
    foundTextLayer: false,
    fileReaderMode: CONSTANTS.FILE_READER_MODE,
    fileReaderModeSet: CONSTANTS.FILE_READER_MODE.DOCUMENT,
    email: {
      fromId: '',
      receievedDate: '',
      toId: '',
      ccId: '',
      subject: '',
      sentiment: ''
    }
  };
  private inputAnnotationList = [];
  private annotationDbList = [];
  private annotationAttribute: AttributeData;
  private editedAnnotationList = [];
  private closeResult: string;
  private attachmentId: number;
  private fileDataFromDB = undefined;
  private attachmentAttrDBDataList = [];
  private attributeOpEvent;
  private attributeOpData;
  private reExtractActionCompletedEvent;
  private isReextractionPendingExist: boolean;
  private _isComponentInitialLoad = false;
  private _isEDOnEditMode = false;
  private isAnnSaveCompleted = true;
  private attrNameTxtList = [];
  private attachmentData = undefined;
  private txtAttachmentData = undefined;
  private txtAttachmentId = 0;
  private TxtFileDataFromDB = undefined;
  private isReExtractMode = false;
  private simpleAnnotationList = [];
  private orgAnnWhenPlnTxtExists = this.configDataHelper.getValue('origDocAnnWhenPlainTxtAvailable');
  private orgAnnWhenPlnTxtNotExists = this.configDataHelper.getValue('origDocAnnWhenPlainTxtUnavailable');
  private orgIsAnnotatorReadOnly = true;
  private orgIsAnnNotEnabled = false;
  private prevModel = {
    isAnnotationNotEnabled: undefined,
    isAnnotationVisible: undefined
  };
  private isUserOpPerformed = false;
  private attributeDataList: AttributeData[];
  private selectedAttachId = 0;
  private routeEventsSubscription;
  private isAnnotaionAddFeatureAllowed = false;
  private shouldLoadAnnotationView = true;
  // For disabling mouse click and keystrokes outside modal window
  ngbModalOptions: NgbModalOptions = {
    backdrop: 'static',
    keyboard: false
  };

  constructor(private modalService: NgbModal, private attributeService: AttributeService,
    private attachmentService: AttachmentService, private msgInfo: MessageInfo, private dataService: DataService,
    private toastr: ToastrService, private utilityService: UtilityService, private actionService: ActionService,
    private niaAnnotatorUtil: NiaAnnotatorUtil, private attributeHelper: AttributeHelper,
    public configDataHelper: ConfigDataHelper, private router: Router, private route: ActivatedRoute,
    public sessionService: SessionService, public niaTelemetryService: NiaTelemetryService) {
    super(sessionService, configDataHelper, niaTelemetryService);
    this.model.isViewFeatureAllowed = this.sessionService.getFeatureAccessModeDataFor(this.bmodel.FID.ATTACHMENT_VIEW).isVisible;
    const parent = this;
    let splittedURL: string[];
    parent.routeEventsSubscription = parent.route.url.subscribe(activeUrl => { // for catching any URL changes and extracting it
      const url: string = window.location.hash;
      splittedURL = url.split('/');
      const attachmentIndex = splittedURL.indexOf('attachment');
      const isAttachmentIndexValid = attachmentIndex > -1;
      if (isAttachmentIndexValid) {
        parent.selectedAttachId = +splittedURL[attachmentIndex + 1];
      }
    });
  }

  ngOnInit() {
    const parent = this;
    parent.reExtractActionCompletedEvent = parent.dataService.reExtractActionCompletedEvent.subscribe(message => {
      if (message) {
        parent.refreshComponent(true);
      }
    });
    parent.attributeOpData = parent.dataService.attributeOpData.subscribe(data => {
      if (data !== CONSTANTS.OBSERVABLE_NULL) {
        if (data.operation !== undefined) {
          parent.showAttrAnnotation(data);
        } else if (data.renderPdfPage !== undefined) {
          console.log("data.renderPdfPage", data.renderPdfPage)
          parent.manageTextLayer(false, false);
          parent.model.renderPdfPage = data.renderPdfPage;
        } else {
          parent.model.options = data;
        }
      }
    });


    parent.attributeOpEvent = parent.dataService.extractedDataCustomEvent.subscribe(data => {
      if (data !== CONSTANTS.OBSERVABLE_NULL) {
        if (data === CONSTANTS.CUSTOM_EVENT.EXTRACTED_DATA.READ_ONLY_DEACTIVATED && !parent.isReextractionPendingExist) { // -1
          parent.setReExtractModeFalse();
        } else if (data === CONSTANTS.CUSTOM_EVENT.EXTRACTED_DATA.REFRESH_COMPLETED) { // 0
          parent.refreshComponent(false);
        } else if (data === CONSTANTS.CUSTOM_EVENT.EXTRACTED_DATA.SAVE_COMPLETED) { // 1
          parent.saveAnnotationData();
        } else if (!parent.isAnnotaionAddFeatureAllowed || data === CONSTANTS.CUSTOM_EVENT.EXTRACTED_DATA.READ_ONLY_ACTIVATED && !parent.isReextractionPendingExist) { // 2
          parent.model.isAnnotatorReadOnly = true;
          parent.isReExtractMode = true;
        } else if (data === CONSTANTS.CUSTOM_EVENT.EXTRACTED_DATA.ADD_CLICKED) { // 3
          parent._isEDOnEditMode = true;
        }
      }
    });
    window.onbeforeunload = (ev) => {
      // finally return the message to browser api.
      if (parent.getIfAnnotationModified()['isModified']) {
        const dialogText = this.msgInfo.getMessage(130);
        ev.returnValue = dialogText;
        return dialogText;
      }
    };
  }

  ngOnDestroy() {
    window.onbeforeunload = null;
    this.attributeOpEvent.unsubscribe();
    this.attributeOpData.unsubscribe();
    this.reExtractActionCompletedEvent.unsubscribe();
    this.routeEventsSubscription.unsubscribe();
    // To reset the last published value as the behavior subject always stores the last value published.
    this.dataService.publishAnnotationOpData(CONSTANTS.OBSERVABLE_NULL);
    this.dataService.publishAnnotationIndexList(CONSTANTS.OBSERVABLE_NULL);
    this.dataService.publishToEDComponentEvent(CONSTANTS.OBSERVABLE_NULL);
  }

  // tslint:disable-next-line: use-life-cycle-interface
  ngOnChanges(changes: { [propKey: string]: SimpleChange }) {
    const parent = this;
    if (changes['isDataReady'] && parent.model._isDataReady) {
      parent.setAnnotatorConfigValues();
      parent._isComponentInitialLoad = (parent.model.isDocTypeFile && changes['attachmentAttrDataList'] &&
        this.utilityService.isListHasValue(changes['attachmentAttrDataList'].currentValue) &&
        changes['attachmentAttrDataList'].firstChange) || (!parent.model.isDocTypeFile);
      if (parent._isComponentInitialLoad) {
        parent.attributeDataList = parent.documentData.attributes;
        const promise = parent.utilityService.isCaseEditable(parent.documentData);
        promise.then(function (value) {
          parent.model.isCaseEditable = value;
          parent.model.isEmailTabSelected = !parent.model.isDocTypeFile;
          parent.model.annotatorHeight = parent.model.isDocTypeFile ? parent.height : parent.minbodyheight;
          parent.refreshComponent(true);
        }).catch(error => parent.refreshComponent(true));
      }
    }
  }

  //  : Unused method as popout button disabled for this component.
  open(content, popout?) {
    if (popout && this.model.isAnnotationVisible) {
      return;
    }
    if (this.getIfAnnotationModified()['isModified']) {
      if (!confirm(this.msgInfo.getMessage(159))) {
        return;
      }
    }
    this.modalService.open(content, this.ngbModalOptions).result.then((result) => {
      this.closeResult = `Closed with: ${result}`;
      this.refreshComponent(true);
    }, (reason) => {
      this.closeResult = `Dismissed with: ${reason}`;
      this.refreshComponent(true);
    });
  }

  modalWindow() {
    // TODO Rachana
    if (this.getIfAnnotationModified()['isModified']) {
      this.close.emit('window closed');
      this.closeButton = false;
      this.popupButton = true;
    } else {
      this.close.emit('window closed');
      this.closeButton = false;
      this.popupButton = true;
    }
  }

  refreshComponent(isRefreshFileReqd: boolean) {
    this.isAnnotaionAddFeatureAllowed = this.getFeature(this.bmodel.FID.ATTRIBUTE_ANNOTAION_CREATE).isEnabled;
    if (!this.model.isViewFeatureAllowed) {
      this.dataService.publishToEDComponentEvent({
        'isFileComponentLoaded': true,
        'isAnnotationNotSupported': true,
        'attachmentId': 0
      });
      return;
    }
    this.dataService.publishToEDComponentEvent({ 'isFileComponentLoaded': false });
    if (isRefreshFileReqd) {
      this.model.isDataLoaded = false;
      this.model.isAnnotatorReadOnly = false;
      this.isReExtractMode = false;
      this.model.annotationInfoMsg = undefined;
      this.model.fileContent = undefined;
      this.model.fileUrl = undefined;
      this.getAttachmnetsListBasedOnGrp();
      if (this.model.isEmailTabSelected) {
        this.getEmailContent(false);
        this.sendPubToEDAfterLoad();
        this.model.isDataLoaded = true;
      } else {
        let selectedAttachmentList = this.model.groupedAttachmentList.filter(attachList =>
          attachList.filter(attachment => attachment.attachmentId === this.selectedAttachId).length > 0
        )[0];
        if (!this.utilityService.isListHasValue(selectedAttachmentList)) {
          selectedAttachmentList = this.model.groupedAttachmentList[0];
        }
        this.getFileFromDb(selectedAttachmentList);
      }
    } else {
      this.model.isShowDataLoad = true;
      if (!this.model.isEmailTabSelected) {
        this.getAttachmentAttributesDb(this.documentData.docId).then(data => {
          this.getContentAnnotationFromAttachAttributes();
          this.dataService.publishToEDComponentEvent({
            'isFileComponentLoaded': true,
            'isAnnotationNotSupported': this.model.isAnnotationNotEnabled
          });
          if (this.isAnnSaveCompleted) {
            this.model.isShowDataLoad = false;
          }
        });
      } else {
        this.getEmailContent(false);
        this.dataService.publishToEDComponentEvent({
          'isFileComponentLoaded': true,
          'isAnnotationNotSupported': this.model.isAnnotationNotEnabled
        });
        if (this.isAnnSaveCompleted) {
          this.model.isShowDataLoad = false;
        }
      }
    }
  }

  downloadAttachments() {
    let isFeatureEnabled = this.getFeature(this.bmodel.FID.ATTACHMENT_VIEW).isEnabled;
    if (!isFeatureEnabled) {
      return
    }
    const attachmentData = this.model.fileViewToggle === 2 ? this.txtAttachmentData : this.attachmentData;
    this.attachmentService.viewAttachment(this.documentData.docId, attachmentData.attachmentId,
      attachmentData.fileName, function (error, response) {
      });
  }

  openAttachment(isPopup: boolean) {
    let isFeatureEnabled = this.getFeature(this.bmodel.FID.ATTACHMENT_VIEW).isEnabled;
    if (!isFeatureEnabled) {
      return
    }
    let url = "";
    const attachmentData = this.model.fileViewToggle === 2 ? this.txtAttachmentData : this.attachmentData;
    this.attachmentService.openAttachment(this.documentData.docId, attachmentData.attachmentId,
      attachmentData.fileName, isPopup).then(function (data: string) {
        url = data;
      });
    return url;
  }

  toggleAnnotator() {
    const parent = this;
    parent.isUserOpPerformed = true;
    if (parent.model.isAnnotationVisible) {
      if (!parent.model.isCaseEditable || !this.isAnnotaionAddFeatureAllowed) {
        parent.model.isAnnotatorReadOnly = true;
      }
    }
    // parent.toggleTextLayer();
    parent.manageTextLayer(true, true);
  }

  getMaskedFileName(fileName: string, limit: number) {
    const splittedName = fileName.split('\\');
    return this.utilityService.getTruncatedFileName(splittedName[splittedName.length - 1], limit);
  }

  private getAttributeValue(attrNameCde: number, attachmentId: number) {
    // If attachmentId==0, will fetch case level attributes
    let result = '';
    // Set attribute data list at case level
    let searchAttributeDataList = this.attributeDataList
    if (attachmentId > 0) {
      // Set attribute data list at provided attachment level
      const attachmentAttrDataList = this._attachmentAttrDataListFromParent.filter(attachData =>
        attachData.attachmentId === attachmentId);
      if (this.utilityService.isListHasValue(attachmentAttrDataList)) {
        searchAttributeDataList = attachmentAttrDataList[0].attributes
      }

    }

    if (this.utilityService.isListHasValue(searchAttributeDataList)) {
      const attributeDataList: AttributeData[] = searchAttributeDataList.filter(attributeData => attributeData.attrNameCde === attrNameCde);
      if (this.utilityService.isListHasValue(attributeDataList)) {
        result = attributeDataList[0].attrValue;
      }
    }
    return result;
  }

  getEmailContent(isClickEvent: boolean) {
    const parent = this;
    parent.model.attachmentId = 0;
    if (isClickEvent) {
      parent.navigate(parent.model.attachmentId);
    } else {
      parent.dataService.publishToEDComponentEvent({
        'attachmentId': parent.model.attachmentId
      });
      parent.setModelValuesToShowAnn();
      const emailBodyStorageData = parent.getEmailBodyStorageInfo()
      const emailBodyAttachmentId = emailBodyStorageData.attachmentId
      parent.attachmentData=emailBodyStorageData.attachmentData 
    
      parent.model.email.fromId = parent.getAttributeValue(CONSTANTS.ATTR_NAME_CDE.FROM_ID, emailBodyAttachmentId)
      parent.model.email.receievedDate = parent.getAttributeValue(CONSTANTS.ATTR_NAME_CDE.RECEIVED_DATE, emailBodyAttachmentId)
      parent.model.email.toId = parent.getAttributeValue(CONSTANTS.ATTR_NAME_CDE.EMAIL_TO_ID, emailBodyAttachmentId)
      parent.model.email.ccId = parent.getAttributeValue(CONSTANTS.ATTR_NAME_CDE.EMAIL_CC_ID, emailBodyAttachmentId)
      parent.model.email.subject = parent.getAttributeValue(CONSTANTS.ATTR_NAME_CDE.SUBJECT, emailBodyAttachmentId)
      parent.model.email.sentiment = parent.getAttributeValue(CONSTANTS.ATTR_NAME_CDE.SENTIMENT, emailBodyAttachmentId)

      if (emailBodyAttachmentId == 0) {
        const content = parent.getAttributeValue(CONSTANTS.ATTR_NAME_CDE.CONTENT_HTML, 0)
        if (parent.utilityService.isStringHasValue(content)) {
          parent.model.fileContent = content;
          parent.model.fileViewer = parent.niaAnnotatorUtil.FILE_VIEWER_HTML;
        } else {
          parent.model.fileContent = parent.getAttributeValue(CONSTANTS.ATTR_NAME_CDE.CONTENT_TXT, 0)
          parent.model.fileViewer = parent.niaAnnotatorUtil.FILE_VIEWER_TXT;
        }
        parent.setAnnotatorStateBeforeLoad();
        parent.model.isEmailTabSelected = true;
      } else {
        const promiseAll = [];
        promiseAll.push(parent.promiseFileContent(emailBodyAttachmentId));
        Promise.all(promiseAll).then(function (result) {
          parent.model.fileContent = result[0].content;
          if (emailBodyStorageData.format == ".html") {
            parent.model.fileViewer = parent.niaAnnotatorUtil.FILE_VIEWER_HTML;
          } else if (emailBodyStorageData.format == ".txt") {
            parent.model.fileViewer = parent.niaAnnotatorUtil.FILE_VIEWER_TXT;
          }
          parent.setAnnotatorStateBeforeLoad();
          parent.model.isEmailTabSelected = true;
        });
      }
    }
  }

  toggleView(value: number) {
    const parent = this;
    parent.model.fileViewToggle = value;
    switch (value) {
      case 1: {
        parent.prevModel.isAnnotationNotEnabled = parent.model.isAnnotationNotEnabled;
        parent.prevModel.isAnnotationVisible = parent.model.isAnnotationVisible;
        parent.setFileViewer(parent.fileDataFromDB.content, parent.fileDataFromDB.blob).then(function (data) {
          if (data) {
            parent.model.isAnnotationNotEnabled = parent.orgIsAnnNotEnabled;
            if (!parent.model.isAnnotationNotEnabled) {
              if (parent.orgIsAnnotatorReadOnly) {
                parent.model.isAnnotatorReadOnly = parent.orgIsAnnotatorReadOnly;
              } else {
                parent.model.isAnnotatorReadOnly = !parent.isReExtractMode && parent.model.isCaseEditable && parent.isAnnotaionAddFeatureAllowed ?
                  parent.orgIsAnnotatorReadOnly : !parent.orgIsAnnotatorReadOnly;
              }
            } else {
              parent.model.isAnnotationVisible = false;
            }
          } else {
            parent.setAnnotationNotSupported();
          }
          parent.toggleTextLayer();
        });
        break;
      }
      case 2: {
        parent.model.annotationInfoMsg = undefined;
        parent.model.isAnnotationNotEnabled = parent.prevModel.isAnnotationNotEnabled;
        if (parent.orgIsAnnNotEnabled && (!parent.isUserOpPerformed ||
          (parent.model.isAnnotationVisible !== parent.prevModel.isAnnotationVisible))) {
          parent.model.isAnnotationVisible = parent.prevModel.isAnnotationVisible;
        }
        parent.setFileViewer(parent.TxtFileDataFromDB.content, parent.TxtFileDataFromDB.blob).then(function (data) {
          if (data) {
            if (!parent.isReExtractMode && parent.model.isCaseEditable && this.isAnnotaionAddFeatureAllowed) {
              parent.model.isAnnotatorReadOnly = false;
            } else {
              parent.model.isAnnotatorReadOnly = true;
            }
          }
        });
        break;
      }
    }
  }

  onAnnotationChange(data) {
    const parent = this;
    if (parent.isReextractionPendingExist || !parent.isAnnotaionAddFeatureAllowed) {
      parent.model.isAnnotatorReadOnly = true;
    }
    if (data != null) {
      if (data.length !== undefined && data.length >= 1) {
        parent.editedAnnotationList = parent.utilityService.createDuplicateList(data[0]);
        const annData = {
          'attachmentId': parent.attachmentId,
          'annotations': data[0]
        };
        parent.dataService.publishAnnotationIndexList(annData);
      } else {
        const annData = {
          'attachmentId': parent.attachmentId,
          'annotation': data
        };
        if (data.op === CONSTANTS.OPERATION_TYPE.ADD) {
          if (CONSTANTS.ATTRIBUTES.ATTR_NAME_TXT.DOCUMENT_TYPE.toLowerCase() !== data.text.toLowerCase()) {
            if (parent.utilityService.isListHasValue(parent.getDuplicateAnns(data))) {
              parent.toastr.error(parent.msgInfo.getMessage(173));
            } else {
              const tempSavedList = parent.editedAnnotationList.filter
                (ann => ann.text.toLowerCase() === data.text.toLowerCase() &&
                  (ann.op === undefined || ann.op === CONSTANTS.OPERATION_TYPE.EDIT || ann.op === CONSTANTS.OPERATION_TYPE.DELETE));
              if (parent.utilityService.isListHasValue(tempSavedList) || parent.getIfAttrExists(data)) {
                data.op = CONSTANTS.OPERATION_TYPE.EDIT;
              }
              const tempUnSavedList = parent.editedAnnotationList.filter
                (ann => ann.text.toLowerCase() === data.text.toLowerCase() && ann.attrId !== undefined);
              if (data.op !== CONSTANTS.OPERATION_TYPE.EDIT) {
                const attrId = tempUnSavedList.length > 0 ? tempUnSavedList[0].attrId : undefined;
                data.attrId = attrId !== undefined ? tempUnSavedList[0].attrId : parent.counter();
              }
              data.createdByTypeCde = CONSTANTS.USER_TYPE_CDE.USER;
              parent.editedAnnotationList.push(data);
              parent.dataService.publishAnnotationOpData(annData);
            }
          } else {
            parent.toastr.error(parent.msgInfo.getMessage(154));
          }
          parent.model.annotationList = parent.utilityService.createDuplicateList(parent.editedAnnotationList);
        } else {
          parent.processDeletedAnnotation(data, annData);
        }
      }
    }
  }

  onTextLayerRendered(data) {
    console.log("onTextLayerRendered", data);
    const textLayerSourceData = data['source'];
    if (this.utilityService.isListHasValue(textLayerSourceData['textContentItemsStr'])) {
      this.model.foundTextLayer = true;
      if (this.shouldLoadAnnotationView && this.isEnableAnnotation) {
        this.model.isAnnotationVisible = this.getFeature(this.bmodel.FID.ATTRIBUTE_ANNOTAION_CREATE).isEnabled;
        this.toggleAnnotator();
      } else {
        this.onZoomFactorChange(data);
      }
    }
    else {
      this.onZoomFactorChange(data);
    }
  }

  onZoomFactorChange(data) {
    this.dataService.publishToEDComponentEvent({
      'isPDFZoomFactorChanged': true
    });
  }

  onPageRendered(data) {
    this.dataService.publishToEDComponentEvent({
      'pdfPageRendered': data
    });
  }
  onPageChanged(event){
    console.log("onPageChanged",event)
    this.model.renderPdfPage =event;
  }

  validateToShowAnnotationInfoMsg(): boolean {
    return (this.model.annotationInfoMsg !== undefined &&
      (this.model.fileViewer !== this.niaAnnotatorUtil.FILE_VIEWER_PDF ||
        this.isScannedPDF() || this.model.isAnnotationVisible));
  }

  getFileFromDb(attachmentList, clickEvent?: boolean) {
    const parent = this;
    if (attachmentList[0].attachmentId !== parent.model.attachmentId) {
      if (clickEvent) {
        parent.navigate(attachmentList[0].attachmentId);
      } else {
        parent.model.isEmailTabSelected = false;
        parent.model.isPlainTextVersionExists = false;
        parent.model.isOrgPlainTxt = false;
        parent.txtAttachmentId = 0;
        parent.TxtFileDataFromDB = undefined;
        parent.attachmentId = 0;
        parent.fileDataFromDB = undefined;
        if (parent.utilityService.isListHasValue(attachmentList)) {
          parent.setIfPlainTextVersionExists(attachmentList);
          const originalAttachments = attachmentList.filter(attachment =>
            attachment.extractTypeCde === CONSTANTS.ATTRIBUTES.EXTRACT_TYPE_CDE.DIRECT_COPY);
          const convertedAttachments = attachmentList.filter(attachment =>
            attachment.extractTypeCde === CONSTANTS.ATTRIBUTES.EXTRACT_TYPE_CDE.CUSTOM_LOGIC
            && !(parent.utilityService.isListHasValue(attachment.attributes) && attachment.attributes[0].attrValue
              === CONSTANTS.FILE_METADATA.PLAIN_TXT));
          if (parent.utilityService.isListHasValue(convertedAttachments)) {
            parent.attachmentData = convertedAttachments[0];
          } else {
            parent.attachmentData = originalAttachments[0];
          }
          parent.attachmentId = parent.attachmentData.attachmentId;
          parent.dataService.publishToEDComponentEvent({
            'attachmentId': parent.selectedAttachId > 0 ? parent.selectedAttachId : parent.attachmentId
          });
          parent.model.fileName = parent.attachmentData.fileName;
          parent.model.docId = parent.documentData.docId;
          parent.model.attachmentId = parent.attachmentId;
          if (parent.attachmentId != null) {
            const promiseAll = [];
            promiseAll.push(parent.getAttachmentAttributesDb(parent.documentData.docId));
            promiseAll.push(parent.promiseFileContent(parent.attachmentId));
            if (parent.txtAttachmentId > 0) {
              promiseAll.push(parent.promiseFileContent(parent.txtAttachmentId));
            }
            parent.setModelValuesToShowAnn();
            parent.model.fileViewToggle = parent.model.isPlainTextVersionExists ? 2 : 1;
            parent.model.isDataLoaded = true;
            parent.model.isShowDataLoad = true;
            Promise.all(promiseAll).then(function (result) {
              parent.getContentAnnotationFromAttachAttributes();
              if (result.length > 2) {
                parent.model.isAnnotationNotEnabled = !parent.isEnableAnnotation;
                parent.model.isAnnotationVisible = parent.isEnableAnnotation;
                if (!parent.model.isCaseEditable || parent.isReExtractMode || !parent.isAnnotaionAddFeatureAllowed) {
                  parent.model.isAnnotatorReadOnly = true;
                }
                parent.TxtFileDataFromDB = result[2];
                parent.fileDataFromDB = result[1];
                parent.setFileViewer(result[2].content, result[2].blob).then(viewerData => {
                  if (viewerData) {
                    parent.sendPubToEDAfterLoad();
                  }
                });
              } else if (result.length > 1) {
                parent.fileDataFromDB = result[1];
                parent.setFileViewer(result[1].content, result[1].blob).then(data => {
                  if (!data) {
                    parent.setAnnotationNotSupported();
                  }
                  // Code for handling if original is a plain text file.
                  if (result[1].blob['data']['type'] === parent.niaAnnotatorUtil.FILE_TYPE.TXT) {
                    parent.model.isOrgPlainTxt = true;
                    parent.setAnnotatorStateBeforeLoad();
                  }
                  parent.sendPubToEDAfterLoad();
                });
              }
            });

            parent.model.email.fromId = parent.getAttributeValue(CONSTANTS.ATTR_NAME_CDE.FROM_ID, parent.attachmentId)
            parent.model.email.receievedDate = parent.getAttributeValue(CONSTANTS.ATTR_NAME_CDE.RECEIVED_DATE, parent.attachmentId)
            parent.model.email.toId = parent.getAttributeValue(CONSTANTS.ATTR_NAME_CDE.EMAIL_TO_ID, parent.attachmentId)
            parent.model.email.ccId = parent.getAttributeValue(CONSTANTS.ATTR_NAME_CDE.EMAIL_CC_ID, parent.attachmentId)
            parent.model.email.subject = parent.getAttributeValue(CONSTANTS.ATTR_NAME_CDE.SUBJECT, parent.attachmentId)
            parent.model.email.sentiment = parent.getAttributeValue(CONSTANTS.ATTR_NAME_CDE.SENTIMENT, parent.attachmentId)
            if (parent.attachmentData.fileName.toLowerCase() == "emailbody.txt" || parent.attachmentData.fileName.toLowerCase() == "emailbody.html") {
              parent.model.isEmailAttachmentTabSelected = true;
            }
          }
        }
      }
    } else {
      parent.model.isDataLoaded = true;
      this.dataService.publishToEDComponentEvent({ 'isFileComponentLoaded': true });
    }
  }

  // -------------PRIVATE METHODS--------------//
  private getAttachmnetsListBasedOnGrp() {
    this.model.groupedAttachmentList = [];
    const tempAttachList: Array<AttachmentData[]> = [];
    const mappedAttachments = new Map<String, AttachmentData[]>();

    let emailBodyAttachmentId = 0
    if (this.isCaseEmail) {
      // If email body is stored as attachment, get its attachment id
      const emailBodyStorageData = this.getEmailBodyStorageInfo()
      emailBodyAttachmentId = emailBodyStorageData.attachmentId
    }

    let emailBodyAttachmentGroupName = ''
    this._attachmentDataListFromParent.forEach(attachment => {
      if (emailBodyAttachmentId > 0 && (attachment.attachmentId === emailBodyAttachmentId)) {
        emailBodyAttachmentGroupName = attachment.groupName
        return // Skip current record as attachment will be consumed as email body
      }
      if (mappedAttachments.has(attachment.groupName)) {
        const attachmentList = mappedAttachments.get(attachment.groupName);
        attachmentList.push(attachment);
        mappedAttachments.set(attachment.groupName, attachmentList);
      } else {
        mappedAttachments.set(attachment.groupName, [attachment]);
      }
    });

    // Remove all records related to email attachment's groupName
    if (emailBodyAttachmentGroupName) {
      mappedAttachments.delete(emailBodyAttachmentGroupName)
    }

    for (const value of mappedAttachments.values()) {
      tempAttachList.push(value);
    }
    tempAttachList.sort((list1, list2) => {
      return list1[0].attachmentId - list2[0].attachmentId;
    });
    let counter = 2
    for (let index = 0; index < tempAttachList.length; index++) {
      tempAttachList[index][0].displayNumber = counter++
    }
    let tempAttachListClone: Array<AttachmentData[]> = [];
    for (let i = 0; i < tempAttachList.length; i++) {
      let innertempList: AttachmentData[] = []
      for (let j = 0; j < tempAttachList[i].length; j++) {
        innertempList.push(Object.assign({}, tempAttachList[i][j]))
      }
      tempAttachListClone.push(innertempList)
    }

    this.model.groupedAttachmentList = tempAttachListClone;


  }

  private navigate(attachmentId?: number) {
    this.modalWindow();
    let url = FileContentComponent.URL_WORK_DATA + '/' + this.model.queueNameCde + '/' + this.documentData.docId;
    if (attachmentId > 0) {
      url += '/attachment/' +
        attachmentId;
    }
    this.router.navigate([url]);
  }

  private setAnnotatorStateBeforeLoad() {
    const parent = this;
    parent.model.isAnnotationNotEnabled = !parent.isEnableAnnotation;
    parent.model.isAnnotationVisible = parent.isEnableAnnotation;
    if (!parent.model.isCaseEditable || parent.isReExtractMode || !parent.isAnnotaionAddFeatureAllowed) {
      parent.model.isAnnotatorReadOnly = true;
    } else {
      parent.model.isAnnotatorReadOnly = false;
    }
  }

  private setModelValuesToShowAnn() {
    if (this.isEnableAnnotation) {
      if (this.model.isPlainTextVersionExists) {
        this.setValWhenPlnTxtExists(this.orgAnnWhenPlnTxtExists);
      } else {
        this.setValWhenPlnTxtNotExists(this.orgAnnWhenPlnTxtNotExists);
      }
    } else {
      this.model.isAnnotationNotEnabled = true;
      this.model.isAnnotationVisible = false;
    }
  }

  private setValWhenPlnTxtNotExists(configVal: number) {
    if (configVal === CONSTANTS.ANNOTATION_CONFIG.EDITABLE) {
      this.orgIsAnnNotEnabled = this.model.isAnnotationNotEnabled = false;
      this.model.isAnnotationVisible = true;
      if (!this.isReExtractMode && this.model.isCaseEditable && this.isAnnotaionAddFeatureAllowed) {
        this.model.isAnnotatorReadOnly = false;
      } else {
        this.model.isAnnotatorReadOnly = true;
      }
    } else {
      this.setValWhenPlnTxtExists(configVal);
    }
  }

  private setValWhenPlnTxtExists(configVal: number) {
    switch (configVal) {
      case CONSTANTS.ANNOTATION_CONFIG.HIDDEN: {
        this.orgIsAnnNotEnabled = this.model.isAnnotationNotEnabled = true;
        this.model.isAnnotationVisible = false;
        break;
      }
      case CONSTANTS.ANNOTATION_CONFIG.READONLY: {
        this.orgIsAnnNotEnabled = this.model.isAnnotationNotEnabled = false;
        this.model.isAnnotationVisible = true;
        this.orgIsAnnotatorReadOnly = this.model.isAnnotatorReadOnly = true;
        break;
      }
    }
  }

  private sendPubToEDAfterLoad() {
    const parent = this;
    parent.toggleTextLayer();
    if (parent.model.isCaseEditable) {
      parent.dataService.publishToEDComponentEvent({
        'isFileComponentLoaded': true,
        'isAnnotationNotSupported': parent.model.isAnnotationNotEnabled
      });
    }
    parent.model.isShowDataLoad = false;
  }

  private readFileAndUpdateModel(fileData: any) {
    const parent = this;
    return new Promise(function (fulfilled, rejected) {
      const file = fileData['data'];
      const reader = new FileReader();
      if (file['type'] === parent.niaAnnotatorUtil.FILE_TYPE.TXT || file['type'] === parent.niaAnnotatorUtil.FILE_TYPE.HTML) {
        reader.readAsText(file);
      } else {
        reader.readAsDataURL(file);
      }
      reader.onload = () => {
        fulfilled(reader.result);
      };
    });
  }

  private setFileViewer(content, fileData: any) {
    const parent = this;
    parent.model.annotationInfoMsg = undefined;
    return new Promise(function (fulfilled, rejected) {
      switch (fileData['data']['type']) {
        case parent.niaAnnotatorUtil.FILE_TYPE.PDF: {
          const browserType = parent.utilityService.getBrowserType();
          if (browserType === CONSTANTS.BROWSER_TYPE.INTERNET_EXPLORER) {
            parent.model.fileUrl = fileData['fileUrl'];
            parent.model.fileContent = '';
          } else {
            parent.model.fileUrl = '';
            parent.model.fileContent = content;
          }
          parent.model.fileViewer = parent.niaAnnotatorUtil.FILE_VIEWER_PDF;
          const isScanned = parent.isScannedPDF();
          parent.model.fileReaderModeSet = CONSTANTS.FILE_READER_MODE.DOCUMENT;
          if (isScanned !== undefined && !isScanned) {
            parent.model.fileReaderModeSet = parent.isEnableAnnotation && parent.getFeature(parent.bmodel.FID.ATTRIBUTE_ANNOTAION_VIEW).isVisible ? CONSTANTS.FILE_READER_MODE.ANNOTATION : CONSTANTS.FILE_READER_MODE.DOCUMENT;
            parent.model.annotationInfoMsg = parent.isEnableAnnotation ? parent.msgInfo.getMessage(170) : undefined;
          } else {
            fulfilled(false);
          }
          break;
        }
        case parent.niaAnnotatorUtil.FILE_TYPE.TXT: {
          parent.model.fileReaderModeSet = parent.isEnableAnnotation && parent.getFeature(parent.bmodel.FID.ATTRIBUTE_ANNOTAION_VIEW).isVisible ? CONSTANTS.FILE_READER_MODE.ANNOTATION : CONSTANTS.FILE_READER_MODE.DOCUMENT;
          parent.model.fileContent = content;
          parent.model.fileViewer = parent.niaAnnotatorUtil.FILE_VIEWER_TXT;
          break;
        }
        case parent.niaAnnotatorUtil.FILE_TYPE.HTML: {
          parent.model.fileContent = content;
          parent.model.fileViewer = parent.niaAnnotatorUtil.FILE_VIEWER_HTML;
          break;
        }
        default: {
          parent.model.fileReaderModeSet = CONSTANTS.FILE_READER_MODE.DOCUMENT;
          parent.model.fileContent = content;
          parent.model.fileViewer = parent.niaAnnotatorUtil.FILE_VIEWER_EMBED;
          fulfilled(false);
          break;
        }
      }
      fulfilled(true);
    });
  }

  private setAnnotationNotSupported() {
    this.model.isAnnotationNotEnabled = true;
    this.model.isAnnotationVisible = false;
    // Disable it as scanned pdf finding logic changed.
    // this.model.annotationInfoMsg = this.msgInfo.getMessage(171);
  }

  private toggleTextLayer() {
    // If PDF file, when annotation ON show textlayer ELSE image layer
    if (this.model.fileViewer === this.niaAnnotatorUtil.FILE_VIEWER_PDF && !this.isScannedPDF() && document.querySelector('.pdfViewer')) {
      const classList = document.querySelector('.pdfViewer').classList;
      if (this.model.isAnnotationVisible) {
        classList.remove('textlayer-off');
        classList.add('textlayer-on');
      } else {
        classList.remove('textlayer-on');
        classList.add('textlayer-off');
      }
    }
  }

  loadForAnnotateInfo() {
    if (!this.isEnableAnnotation || this.model.isEmailTabSelected || this.model.isOrgPlainTxt || this.isHtmlViewerEnabled()) {
      return false;
    }
    return (this.model.fileReaderModeSet === this.model.fileReaderMode.ANNOTATION && this.model.annotationInfoMsg == undefined && !(
      this.model.fileViewToggle === 2 || this.model.isOrgPlainTxt));
  }

  private isHtmlViewerEnabled() {
    return (this.model.fileViewer == this.niaAnnotatorUtil.FILE_VIEWER_HTML);
  }

  manageTextLayer(showTextLayer, annotationLayer) {
    this.model.fileReaderModeSet = showTextLayer ? CONSTANTS.FILE_READER_MODE.TEXTLAYER : CONSTANTS.FILE_READER_MODE.DOCUMENT;
    this.model.annotationInfoMsg = undefined;
    this.model.isAnnotationVisible = false;
    this.shouldLoadAnnotationView = annotationLayer;
    if (annotationLayer) {
      this.model.fileReaderModeSet = CONSTANTS.FILE_READER_MODE.ANNOTATION;
      this.model.annotationInfoMsg = this.model.foundTextLayer ? this.msgInfo.getMessage(170) : this.msgInfo.getMessage(171);
      this.model.annotationInfoMsg = (!this.isEnableAnnotation || this.model.fileViewToggle === 2 || this.model.isOrgPlainTxt || this.isHtmlViewerEnabled()) ? undefined : this.model.annotationInfoMsg;
      this.model.isAnnotationVisible = this.isEnableAnnotation && this.getFeature(this.bmodel.FID.ATTRIBUTE_ANNOTAION_VIEW).isEnabled ? true : false;
      // this.model.isAnnotatorReadOnly = (!this.model.isCaseEditable || !this.isAnnotaionAddFeatureAllowed);
    }
    if (this.model.fileViewer === this.niaAnnotatorUtil.FILE_VIEWER_PDF && document.querySelector('.pdfViewer')) {
      const classList = document.querySelector('.pdfViewer').classList;
      if (showTextLayer) {
        classList.remove('textlayer-off');
        classList.add('textlayer-on');
      } else {
        classList.remove('textlayer-on');
        classList.add('textlayer-off');
      }
    }
    if (this.model.fileViewer === this.niaAnnotatorUtil.FILE_VIEWER_EMBED && document.querySelector('#embedContent')) {
      console.log("document.querySelector('#embedContent')", document.querySelector('#embedContent'))
      const classList = document.querySelector('#embedContent').classList;
      if (showTextLayer) {
        classList.remove('show');
        classList.add('hide');
      } else {
        classList.remove('hide');
        classList.add('show');
      }

    }
  }

  private isScannedPDF() {
    const parent = this;
    let isScannedPdf = !(parent.model.foundTextLayer || parent.model.isOrgPlainTxt || parent.model.fileViewToggle === 2);
    // DISABLED BELOW LOGIC AS NGX TEXTLAYER CALL BACK USED FOR IDENTIFYING THE PDF SCANNED/NATVIE.
    // if (parent._attachmentDataListFromParent.length > 0) {
    //   const fileMetadata: AttributeData[] = parent._attachmentDataListFromParent.
    //     filter(attach => attach.attachmentId === parent.attachmentId)[0].attributes;
    //   if (parent.utilityService.isListHasValue(fileMetadata)) {
    //     parent.isPDFScanned = (fileMetadata[0].attrValue === CONSTANTS.FILE_METADATA.PDF_SCANNED);
    //   }
    // }
    // if(!parent.isPDFScanned){
    //   const documentFileMetadata = parent.documentData.attributes.filter(x=>x["attrNameCde"]==33 && x["attrValue"] ===CONSTANTS.FILE_METADATA.PDF_SCANNED)
    //   if (parent.utilityService.isListHasValue(documentFileMetadata)) {
    //     parent.isPDFScanned = true;
    //   }
    // }
    // console.log('isScannedPDF', isScannedPdf);
    return isScannedPdf;
  }

  // To get attachment attributes from service for respective docId & requested attachmentId.
  private getAttachmentAttributesDb(docId: number) {
    const parent = this;
    parent.checkActionStatusAndSetAnnotationMode();
    return new Promise<void>(function (fulfilled, rejected) {
      if (parent._isComponentInitialLoad) {
        console.log('LHS : loaded from parent attachement');
        parent.attachmentAttrDBDataList = parent._attachmentAttrDataListFromParent.filter(attachData =>
          attachData.attachmentId === parent.attachmentId);
        parent._isComponentInitialLoad = false;
        if (parent.utilityService.isAValidValue(parent.attachmentAttrDBDataList[0])) {
          parent.attrNameTxtList = parent.attributeHelper.getAttrNameTxtList(parent.attachmentAttrDBDataList[0].attributes, []);
        }
        fulfilled();
      } else {
        console.log('LHS : loaded from child refresh');
        if (parent._attachmentDataListFromParent.length > 0) {
          parent.attributeService.getAttachmentAttributes(docId, parent.attachmentId).then(function (data: AttributeData[]) {
            parent.attachmentAttrDBDataList = data;
            if (parent.utilityService.isAValidValue(parent.attachmentAttrDBDataList[0])) {
              parent.attrNameTxtList = parent.attributeHelper.getAttrNameTxtList(parent.attachmentAttrDBDataList[0].attributes, []);
            }
            fulfilled();
          }).catch(function (error) {
            rejected();
          });
        } else {
          rejected();
        }
      }
    });
  }

  private setIfPlainTextVersionExists(attachmentList: AttachmentData[]) {
    const parent = this;
    const txtAttachmentAttrDataDbList = parent.utilityService.createDuplicateList(attachmentList).
      filter(attachData => parent.utilityService.isListHasValue(attachData.attributes)
        && attachData.attributes[0].attrValue === CONSTANTS.FILE_METADATA.PLAIN_TXT);
    if (parent.utilityService.isListHasValue(txtAttachmentAttrDataDbList)) {
      parent.model.isPlainTextVersionExists = true;
      parent.txtAttachmentData = txtAttachmentAttrDataDbList[0];
      parent.txtAttachmentId = parent.txtAttachmentData.attachmentId;
    }
  }

  private getContentAnnotationFromAttachAttributes() {
    const parent = this;
    if (parent.utilityService.isListHasValue(parent.attachmentAttrDBDataList) && parent.isAnnSaveCompleted) {
      parent.annotationDbList = [];
      parent.attachmentAttrDBDataList.forEach(attrData => {
        if (parent.utilityService.isListHasValue(attrData.attributes) && attrData.attachmentId === parent.attachmentId) {
          const annAttrSimpleDataList = attrData.attributes.filter(attributeData =>
            attributeData.attrNameCde === CONSTANTS.ATTR_NAME_CDE.CONTENT_ANNOTATION);
          const annotationAttrDataList = attrData.attributes.filter(attributeData =>
            attributeData.attrNameCde === CONSTANTS.ATTR_NAME_CDE.CONTENT_ANNOTATION_ANNOTATOR);
          if (parent.utilityService.isListHasValue(annAttrSimpleDataList) &&
            !parent.utilityService.isListHasValue(annotationAttrDataList)) {
            parent.simpleAnnotationList = parent.utilityService.createDuplicateList(annAttrSimpleDataList);
            annAttrSimpleDataList.forEach(annAttrSimpleData => {
              const annotations = annAttrSimpleData.attrValue;
              parent.annotationDbList = parent.annotationDbList.concat(JSON.parse(annotations));
            });
          } else {
            if (parent.utilityService.isListHasValue(annotationAttrDataList)) {
              parent.annotationAttribute = annotationAttrDataList[0];
              const annotations = annotationAttrDataList[0].attrValue;
              parent.annotationDbList = JSON.parse(annotations);
            } else {
              parent.annotationDbList = [];
            }
            parent.inputAnnotationList = parent.utilityService.createDuplicateList(parent.annotationDbList);
            parent.editedAnnotationList = parent.utilityService.createDuplicateList(parent.annotationDbList);
          }
          parent.model.annotationList = parent.utilityService.createDuplicateList(parent.annotationDbList);
        }
      });
    }
  }

  private promiseFileContent(attachmentId) {
    const parent = this;
    return new Promise(function (fulfilled, rejected) {
      parent.attachmentService.getFileContent(parent.documentData.docId, attachmentId)
        .then(function (value) {
          if (value['data'] instanceof (Blob)) {
            parent.readFileAndUpdateModel(value).then(function (data) {
              fulfilled({
                'content': data,
                'blob': value
              });
            });
          }
        }).catch(function () {
          rejected();
        });
    });
  }

  private processDeletedAnnotation(data, annData) {
    const parent = this;
    let annotationList = [];
    annotationList = parent.utilityService.createDuplicateList(parent.editedAnnotationList);
    delete data['_local'];
    const modifiedAnnList = annotationList.filter(annotationData => {
      const valRangeData: RangeData = new RangeData(annotationData.ranges[0].start, annotationData.ranges[0].startOffset,
        annotationData.ranges[0].end, annotationData.ranges[0].endOffset);
      const annRangeData: RangeData = new RangeData(data.ranges[0].start, data.ranges[0].startOffset,
        data.ranges[0].end, data.ranges[0].endOffset);
      if (annotationData.quote === data.quote
        && annotationData.text.toLowerCase() === data.text.toLowerCase() && annotationData.id === data.id
        && JSON.stringify(valRangeData) === JSON.stringify(annRangeData)
      ) {
        if ((data.attrId !== undefined && annotationData.attrId === data.attrId) || annotationData.op === CONSTANTS.OPERATION_TYPE.EDIT) {
          return false;
        }
        annotationData.op = data.op;
      }
      return true;
    });
    annData['editCount'] = modifiedAnnList.filter
      (ann => ann.text.toLowerCase() === data.text.toLowerCase()
        && ann.quote.toLowerCase() === data.quote.toLowerCase()
        && (ann.op !== CONSTANTS.OPERATION_TYPE.DELETE
        )).length;
    annData['count'] = modifiedAnnList.filter
      (ann => ann.text.toLowerCase() === data.text.toLowerCase()
        && (ann.op !== CONSTANTS.OPERATION_TYPE.DELETE
        )).length;
    if (annData.count <= 0) {
      modifiedAnnList.forEach(ann => {
        if (ann.text.toLowerCase() === data.text.toLowerCase()) {
          ann.op = data.op;
        }
      });
    }
    parent.dataService.publishAnnotationOpData(annData);
    parent.editedAnnotationList = modifiedAnnList;
    parent.model.annotationList = parent.utilityService.createDuplicateList(parent.editedAnnotationList);
  }

  private getIfAnnotationModified() {
    const parent = this;
    let isModified = false;
    let op = '';
    const editedAnnotations = parent.utilityService.createDuplicateList(parent.editedAnnotationList);
    editedAnnotations.forEach(ann => delete ann['index']);
    if (parent.utilityService.isListHasValue(parent.inputAnnotationList)) {
      if (parent.utilityService.isListHasValue(editedAnnotations)) {
        if (editedAnnotations.filter(ann => ann.op !== CONSTANTS.OPERATION_TYPE.DELETE).length > 0) {
          if (JSON.stringify(parent.inputAnnotationList) !== JSON.stringify(editedAnnotations)) {
            isModified = true;
            op = CONSTANTS.OPERATION_TYPE.EDIT;
          }
        } else {
          isModified = true;
          op = CONSTANTS.OPERATION_TYPE.DELETE;
        }
      }
    } else {
      if (parent.utilityService.isListHasValue(editedAnnotations)) {
        isModified = true;
        op = CONSTANTS.OPERATION_TYPE.ADD;
      }
    }
    return { 'isModified': isModified, 'op': op };
  }

  private saveAnnotationData() {
    const parent = this;
    const data = parent.getIfAnnotationModified();
    if (data.isModified) {
      parent.isAnnSaveCompleted = false;
      switch (data.op) {
        case CONSTANTS.OPERATION_TYPE.ADD: {
          const annotations = parent.getStringifiedAnns();
          if (parent.utilityService.isStringHasValue(annotations)) {
            parent.addAnnotationToDb(annotations);
          } else {
            parent.addAnnotationToDb('[]', true);
          }
          break;
        }
        case CONSTANTS.OPERATION_TYPE.EDIT: {
          parent.annotationAttribute.attrValue = parent.getStringifiedAnns();
          parent.updateAnnotationToDb();
          break;
        }
        case CONSTANTS.OPERATION_TYPE.DELETE: {
          parent.deleteAnnotationFromDb();
          break;
        }
      }
    }
  }

  private getStringifiedAnns() {
    const parent = this;
    const annotations = parent.utilityService.createDuplicateList(parent.editedAnnotationList.
      filter(ann => ann.op !== CONSTANTS.OPERATION_TYPE.DELETE));
    let data = '';
    if (parent.utilityService.isListHasValue(annotations)) {
      annotations.forEach(element => {
        delete element['op'];
        delete element['index'];
        delete element['attrId'];
        delete element['_local'];
        const range = element['ranges'][0];
        range.start = range.end = '';
      });
      data = JSON.stringify(annotations);
    }
    return data;
  }

  private deleteAnnotationFromDb() {
    const parent = this;
    const requestData = parent.createSaveAnnAttrApiRequest([parent.annotationAttribute]);
    parent.attributeService.deleteAttributeData(requestData).then(function (_data) {
      parent.setAnnSaveAndGetData();
      parent.toastr.success(parent.msgInfo.getMessage(158));
    }).catch(function (error) {
      parent.toastr.error(parent.msgInfo.getMessage(102));
    });
  }

  private setAnnSaveAndGetData() {
    const parent = this;
    parent.model.isShowDataLoad = true;
    parent.getAttachmentAttributesDb(parent.documentData.docId).then(data => {
      parent.isAnnSaveCompleted = true;
      parent.getContentAnnotationFromAttachAttributes();
      parent.model.isShowDataLoad = false;
    });
  }

  private updateAnnotationToDb() {
    const parent = this;
    const requestData = parent.createSaveAnnAttrApiRequest([parent.annotationAttribute]);
    parent.attributeService.editAttributeData(requestData).then(function (_data) {
      parent.setAnnSaveAndGetData();
      parent.toastr.success(parent.msgInfo.getMessage(157));
    }).catch(function (error) {
      parent.toastr.error(parent.msgInfo.getMessage(102));
    });
  }

  private addAnnotationToDb(data, isDeleteOp?: boolean) {
    const parent = this;
    if (data !== undefined) {
      let attachmentData: any;
      let attributes = [];
      attributes = [
        {
          'attrNameCde': CONSTANTS.ATTR_NAME_CDE.CONTENT_ANNOTATION_ANNOTATOR,
          'attrValue': data,
          'confidencePct': CONSTANTS.ATTRIBUTES.CONFIDENCE_PCT.UNDEFINED,
          'extractTypeCde': CONSTANTS.ATTRIBUTES.EXTRACT_TYPE_CDE.DIRECT_COPY,
          'attributes': null
        }
      ];
      attachmentData = [{
        'attachmentId': parent.attachmentId,
        'attributes': attributes
      }];
      attributes = [];
      const requestData = [{
        'docId': parent.documentData.docId,
        'attributes': attributes,
        'attachments': attachmentData
      }];
      parent.attributeService.addAttributeData(requestData).then(function (responseData) {
        if (responseData['responseCde'] === CONSTANTS.ERROR_CDE.MULTI_ATTRIBUTE_ALREADY_EXIST) {
          parent.toastr.error(parent.msgInfo.getMessage(102));
          return;
        } else {
          parent.model.isShowDataLoad = true;
          if (!isDeleteOp) {
            parent.deleteAllSimpleAnnAttrs().then(onComplete => {
              parent.getAttachmentAttributesDb(parent.documentData.docId).then(_data => {
                parent.isAnnSaveCompleted = true;
                parent.getContentAnnotationFromAttachAttributes();
                parent.model.isShowDataLoad = false;
              });
              parent.toastr.success(parent.msgInfo.getMessage(157));
            });
          } else {
            parent.getAttachmentAttributesDb(parent.documentData.docId).then(_data => {
              parent.isAnnSaveCompleted = true;
              parent.getContentAnnotationFromAttachAttributes();
              parent.deleteAllSimpleAnnAttrs().then(onComplete => {
                parent.getAttachmentAttributesDb(parent.documentData.docId).then(_deleteData => {
                  parent.deleteAnnotationFromDb();
                });
              });
            });
          }
        }
      }).catch(function (error) {
        parent.toastr.error(parent.msgInfo.getMessage(102));
      });
    }
  }

  private createSaveAnnAttrApiRequest(annotationAttributeList): any {
    const parent = this;
    const attachmentData = [{
      'attachmentId': parent.attachmentId,
      'attributes': annotationAttributeList
    }];
    const attributes = [];
    const requestData = [{
      'docId': parent.documentData.docId,
      'attributes': attributes,
      'attachments': attachmentData
    }];
    return requestData;
  }

  private deleteAllSimpleAnnAttrs() {
    const parent = this;
    return new Promise<void>(function (fulfilled, rejected) {
      if (parent.utilityService.isListHasValue(parent.simpleAnnotationList)) {
        const requestData = parent.createSaveAnnAttrApiRequest(parent.simpleAnnotationList);
        parent.attributeService.deleteAttributeData(requestData).then(function (_data) {
          parent.simpleAnnotationList = [];
          fulfilled();
        }).catch(function (error) {
          rejected();
        });
      } else {
        fulfilled();
      }
    });
  }

  private getDuplicateAnns(annotation) {
    const parent = this;
    let duplicateList = [];
    duplicateList = parent.editedAnnotationList.filter(value => {
      if (
        value.text.toLowerCase() === annotation.text.toLowerCase()) {
        const valRangeData: RangeData = new RangeData(value.ranges[0].start, value.ranges[0].startOffset,
          value.ranges[0].end, value.ranges[0].endOffset);
        const annRangeData: RangeData = annotation.ranges[0];
        return JSON.stringify(valRangeData) === JSON.stringify(annRangeData);
      }
      return false;
    });
    return duplicateList;
  }

  private showAttrAnnotation(attrData) {
    const parent = this;
    switch (attrData.operation) {
      case CONSTANTS.OPERATION_TYPE.ADD: {
        if (attrData.attribute.attrId > 0) {
          parent.editedAnnotationList = parent.editedAnnotationList.filter
            (ann => !(ann.op === CONSTANTS.OPERATION_TYPE.ADD && ann.attrId === attrData.attribute.attrId));
        }
        attrData.attrValue = attrData.attribute.attrValue;
        parent.generateAnnotations(attrData);
        break;
      }
      case CONSTANTS.OPERATION_TYPE.DELETE: {
        parent.handleDeleteAnns(attrData);
        break;
      }
      case CONSTANTS.OPERATION_TYPE.EDIT: {
        let values: string[] = [];
        values = attrData.attribute.attrValue.toLowerCase().split(CONSTANTS.ATTRIBUTES.DELIMITER);
        const originalAttrValArray: string[] = attrData.attribute.attrValueOrg ?
          attrData.attribute.attrValueOrg.toLowerCase().split(CONSTANTS.ATTRIBUTES.DELIMITER) : [];
        const valArrayToAnnotate = values.filter(val => !originalAttrValArray.includes(val)
          && parent.editedAnnotationList.filter(ann => ann.text.toLowerCase() === attrData.attribute.attrNameTxt.toLowerCase() &&
            parent.utilityService.getIfStringsMatch(val, ann.quote.toLowerCase())).length <= 0);
        const valArrayToNotAnnotate = values.filter(val => originalAttrValArray.includes(val));
        if ((this.utilityService.isListHasValue(valArrayToAnnotate) ||
          this.utilityService.isListHasValue(valArrayToNotAnnotate))
          || (attrData.attribute.attrValue.toLowerCase() !== attrData.attribute.attrValueOrg.toLowerCase())) {
          parent.editedAnnotationList = parent.editedAnnotationList.filter
            (ann => parent.processAttrAnnForEdit(ann, attrData, valArrayToNotAnnotate, valArrayToAnnotate, values));
        }

        if (parent.utilityService.isListHasValue(valArrayToAnnotate)) {
          attrData.attrValue = valArrayToAnnotate.join(CONSTANTS.ATTRIBUTES.DELIMITER);
          parent.generateAnnotations(attrData);
        }
        break;
      }
      case CONSTANTS.OPERATION_TYPE.UNDO_DELETE: {
        parent.handleDeleteAnns(attrData);
        break;
      }
    }
    parent.model.annotationList = parent.utilityService.createDuplicateList(parent.editedAnnotationList);
  }


  private processAttrAnnForEdit(ann, attrData,
    valArrayToNotAnnotate: string[], valArrayToAnnotate: string[], values: string[]) {
    const parent = this;
    let isReturn = true;
    if (ann.text.toLowerCase() === attrData.attribute.attrNameTxt.toLowerCase()) {
      if (!parent.utilityService.isListHasValue(valArrayToNotAnnotate) &&
        parent.utilityService.isListHasValue(valArrayToAnnotate) && (ann.op !== CONSTANTS.OPERATION_TYPE.EDIT)) {
        ann.op = CONSTANTS.OPERATION_TYPE.DELETE;
      } else {
        if (parent.utilityService.isListHasValue(valArrayToNotAnnotate) &&
          !parent.utilityService.isListHasValue(valArrayToAnnotate)
          && valArrayToNotAnnotate.filter(val => parent.utilityService.getIfStringsMatch(val, ann.quote.toLowerCase())).length <= 0) {
          if (ann.op === CONSTANTS.OPERATION_TYPE.EDIT) {
            isReturn = values.filter(val => parent.utilityService.
              getIfStringsMatch(val, ann.quote.toLowerCase())).length <= 0 ? false : true;
          } else {
            ann.op = CONSTANTS.OPERATION_TYPE.DELETE;
          }
        }
      }
      if (ann.op === CONSTANTS.OPERATION_TYPE.EDIT &&
        values.filter(val => parent.utilityService.getIfStringsMatch(val, ann.quote.toLowerCase())).length <= 0) {
        isReturn = false;
      } else if (valArrayToNotAnnotate.filter(val =>
        parent.utilityService.getIfStringsMatch(val, ann.quote.toLowerCase())).length <= 0 &&
        valArrayToAnnotate.filter(val => parent.utilityService.getIfStringsMatch(val, ann.quote.toLowerCase())).length <= 0
        && ann.op !== CONSTANTS.OPERATION_TYPE.EDIT) {
        ann.op = CONSTANTS.OPERATION_TYPE.DELETE;
      } else if (valArrayToNotAnnotate.filter(val => parent.utilityService.getIfStringsMatch
        (val, ann.quote.toLowerCase())).length > 0 && ann.op === CONSTANTS.OPERATION_TYPE.DELETE) {
        delete ann.op;
      }
    }
    return isReturn;
  }

  private handleDeleteAnns(attrData) {
    const parent = this;
    if (!attrData.status) {
      parent.editedAnnotationList = parent.editedAnnotationList.
        filter(ann => !(attrData.attribute.attrId !== undefined && ann.attrId === attrData.attribute.attrId &&
          this.utilityService.getIfStringsMatch(ann.quote, attrData.attribute.attrValue)));
    } else {
      parent.handleDelSavedAnns(attrData);
    }
  }

  private handleDelSavedAnns(attrData) {
    const parent = this;
    parent.editedAnnotationList = parent.editedAnnotationList.filter(ann => {
      if (ann.text.toLowerCase() === attrData.attribute.attrNameTxt.toLowerCase() &&
        this.utilityService.getIfStringsMatch(ann.quote, attrData.attribute.attrValue)) {
        if (ann.op === undefined) {
          ann.op = attrData.operation;
        } else if (ann.op === CONSTANTS.OPERATION_TYPE.EDIT) {
          return false;
        } else if (ann.op === CONSTANTS.OPERATION_TYPE.DELETE && attrData.operation === CONSTANTS.OPERATION_TYPE.UNDO_DELETE) {
          delete ann.op;
        }
      }
      return true;
    });
  }

  private generateAnnotations(data): void {
    if (this.utilityService.isStringHasValue(data.attrValue)) {
      const valArray = data.attrValue.split(CONSTANTS.ATTRIBUTES.DELIMITER);
      valArray.forEach(value => {
        let id = 1;
        if (this.utilityService.isListHasValue(this.editedAnnotationList)) {
          id += this.editedAnnotationList[this.editedAnnotationList.length - 1].id;
        }
        this.generateAnnsFromDOM(value, id, data);
      });
    }
  }

  private generateAnnsFromDOM(value: string, id: number, data) {
    const selector = this.model.isPlainTextVersionExists ? this.niaAnnotatorUtil.SELECTOR :
      this.niaAnnotatorUtil.getSelectorBasedOnViewer(this.model.fileViewer);
    const ranges = this.niaAnnotatorUtil.findTextRangeFromTextlayer(value,
      selector);
    ranges.forEach(range => {
      const quote = range['quote'];
      delete range['quote'];
      const annotateData: any = {
        'quote': quote,
        'ranges': [range],
        'text': data.attribute.attrNameTxt,
        'id': id++,
        'op': data.operation,
        'attrId': data.attribute.attrId,
        'createdByTypeCde': CONSTANTS.USER_TYPE_CDE.USER
      };
      if (this.getDuplicateAnns(annotateData).length <= 0) {
        this.editedAnnotationList.push(annotateData);
      }
    });
  }

  private checkActionStatusAndSetAnnotationMode() {
    const parent = this;
    if (parent.documentData !== undefined) {
      parent.actionService.getActions(parent.documentData.docId, function (error, data) {
        let tempDataList = [];
        if (!error && data[0] !== undefined) {
          tempDataList = data[0].actionDataList;
        }
        parent.isReextractionPendingExist = parent.utilityService.isReExtractPending(tempDataList);
        if (parent.isReextractionPendingExist || !parent.isAnnotaionAddFeatureAllowed) {
          parent.model.isAnnotatorReadOnly = true;
          parent.isReExtractMode = true;
        } else {
          parent.setReExtractModeFalse();
        }
      });
    }
  }

  private setReExtractModeFalse() {
    const parent = this;
    parent.isReExtractMode = false;
    if (parent.isAnnotaionAddFeatureAllowed && parent.model.isCaseEditable && (parent.model.fileViewToggle !== 1 ||
      (parent.model.fileViewToggle === 1 && (parent.model.isOrgPlainTxt || (!parent.model.isPlainTextVersionExists &&
        !(parent.orgAnnWhenPlnTxtExists === CONSTANTS.ANNOTATION_CONFIG.READONLY ||
          parent.orgAnnWhenPlnTxtNotExists === CONSTANTS.ANNOTATION_CONFIG.READONLY)))))) {
      parent.model.isAnnotatorReadOnly = false;
    }
  }

  private getIfAttrExists(annotation) {
    return this.utilityService.isListHasValue(this.attrNameTxtList.filter(name => name.toLowerCase() === annotation.text.toLowerCase()));
  }

  private setAnnotatorConfigValues() {
    const validVals = [CONSTANTS.ANNOTATION_CONFIG.HIDDEN, CONSTANTS.ANNOTATION_CONFIG.READONLY];
    this.orgAnnWhenPlnTxtExists = this.utilityService.isAValidValue(this.orgAnnWhenPlnTxtExists) &&
      validVals.includes(this.orgAnnWhenPlnTxtExists)
      ? this.orgAnnWhenPlnTxtExists : CONSTANTS.ANNOTATION_CONFIG.READONLY;
    validVals.push(CONSTANTS.ANNOTATION_CONFIG.EDITABLE);
    this.orgAnnWhenPlnTxtNotExists = this.utilityService.isAValidValue(this.orgAnnWhenPlnTxtNotExists) &&
      validVals.includes(this.orgAnnWhenPlnTxtNotExists)
      ? this.orgAnnWhenPlnTxtNotExists : CONSTANTS.ANNOTATION_CONFIG.EDITABLE;
  }

  private getEmailBodyStorageInfo() {
    const parent = this;
    var attachId: number = 0; // 0 means its stored as case attribute
    var endsWith = "";
    let firstHtmlAttachId = 0;
    let firstTxtAttachId = 0;
    var htmlAttachmentData=undefined;
    var txtAttachmentData=undefined;
    var emailAttachmentData=undefined;
    parent._attachmentDataListFromParent.forEach(attachment => {
      if (attachment.fileName.toLowerCase() == "emailbody.html") {
        if (firstHtmlAttachId == 0) {
          firstHtmlAttachId = attachment.attachmentId;
          htmlAttachmentData=attachment;
        }
      } else if (attachment.fileName.toLowerCase() == "emailbody.txt") {
        if (firstTxtAttachId == 0) {
          firstTxtAttachId = attachment.attachmentId;
          txtAttachmentData=attachment;
        }
      }
    });
    if (firstHtmlAttachId > 0) {
      attachId = firstHtmlAttachId;
      endsWith = ".html";
      emailAttachmentData=htmlAttachmentData;
      
    } else if (firstTxtAttachId > 0) {
      attachId = firstTxtAttachId;
      endsWith = ".txt";
      emailAttachmentData=txtAttachmentData;
    }

    return {
      'attachmentId': attachId,
      'format': endsWith,
      'attachmentData':emailAttachmentData
    }
  }

  // -----------UTIL METHODS------------- //

  private counter() {
    return Math.floor(100000 + new Date().getMilliseconds() * 900000);
  }
}
