/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Component, OnInit, HostListener } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { DataService } from '../../service/data.service';
import { DocumentService } from '../../service/document.service';
import { DocumentData } from '../../data/document-data';
import { CONSTANTS } from '../../common/constants';
import { UtilityService } from '../../service/utility.service';
import { AttributeService } from '../../service/attribute.service';
import { AttributeAttributeMappingData } from '../../data/attribute-attribute-mapping-data';
import { AttachmentService } from '../../service/attachment.service';
import { AttachmentData } from '../../data/attachment-data';
import { AttributeData } from '../../data/attribute-data';
import { BaseComponent } from '../../base.component';
import { SessionService } from '../../service/session.service';
import { ConfigDataHelper } from '../../utils/config-data-helper';
import { NiaTelemetryService } from '../../service/nia-telemetry.service';
import { LocalSessionData } from '../../data/local-session-data';
import { LocalSessionService } from '../../service/local-session.service';

@Component({
  selector: 'app-work-data',
  templateUrl: './work-data.component.html',
  styleUrls: ['./work-data.component.scss']
})
export class WorkDataComponent extends BaseComponent implements OnInit {
  getClassName(): string {
    return "WorkDataComponent";
  }
  private static CASE_DATA_HEIGHT_OFFSET = 155;
  private static CASE_DATA_TOP_COMPONENT_HEIGHT_OFFSET = 0.6; // 60%

  private static CASE_DATA_TOP_COMPONENT_HEIGHT_OFFSET_FILE = 1; // 100%

  private static DOC_CONTENT_HEADER_HEIGHT_OFFSET = 42;
  private static DOC_CONTENT_SENT_DETAILS_HEIGHT_OFFSET = 85;

  private static CASE_DATA_MIDDLE_COMPONENT_HEIGHT_OFFSET = 0.38;
  private static CASE_DATA_MIDDLE_COMPONENT_HEIGHT_OFFSET_FILE = 0.3;

  private static HEADER_HEIGHT_OFFSET = 60;
  private static HEADER_HEIGHT_OFFSET_FILE = 75;
  private static SENT_EMAIL_HEIGHT_OFFSET = 60;

  private static EXTRACTED_DATA_FOOTER_HEIGHT_OFFSET = 3;
  private static NOT_VALID_PAGE_ERROR = 'error/page';

  docId: number;
  documentData: DocumentData;
  isDataReady = false;
  modelCaseDetailsTopHeight: number;
  modelDocContentHeight: number;
  modelDocContentBodyHeight: number;
  modelCaseDetailsMiddleHeight: number;
  modelTaskListHeight: number;
  modelSentEmailListHeight: number;

  modelFileContentHeight: number;
  modelFileExtractedDataHeight: number;
  modelCaseDetailsHeight = 40;
  isDocTypeFile = false;
  isCaseEmail = false;
  isAddReqd = true;
  isDeleteReqd = true;
  increaseFileExtractedDataHeight=false;  

  model: any = {
    attributeAttributeMapping: [],
    attachmentDataList: [],
    attachmentAttrDataList: [],
    // Attachment list is created for sending specific attachment data to extracted-data in email flow.
    selectedAttachmentDataList: [],
    queueNameCde: Number,
    isEnableAnnotation: false,
    shouldLoadExtractionPathView: false,
    docUserDataList:[]
  };

  constructor(private route: ActivatedRoute, private dataService: DataService,
    private documentService: DocumentService, private utilityService: UtilityService, private attributeService: AttributeService,
    private attachmentService: AttachmentService, private router: Router, public sessionService: SessionService,
    public configDataHelper: ConfigDataHelper, public niaTelemetryService:NiaTelemetryService,
    private localSessionService: LocalSessionService) {
    super(sessionService, configDataHelper, niaTelemetryService);
    const parent = this;
    this.route.params.subscribe(params => {
      parent.isDataReady = false;
      parent.model.queueNameCde = +params['queueNameCde'];
      const localSessionData: LocalSessionData = parent.localSessionService.getLocalSessionData();
      localSessionData.lastWorkedQueueCde = +params['queueNameCde'];
      parent.localSessionService.updateLocalSessionData(localSessionData);
      parent.docId = +params['documentId'];
      let attachmentId;
      let isAttachmentPresent = window.location.hash.indexOf('attachment') > -1;
      if (window.location.hash.indexOf('extractionpath') > -1) {
        parent.model.shouldLoadExtractionPathView = true;
        isAttachmentPresent = true;
      }

      if (parent.model.shouldLoadExtractionPathView) {
        WorkDataComponent.EXTRACTED_DATA_FOOTER_HEIGHT_OFFSET = 175;
      } else {
        WorkDataComponent.EXTRACTED_DATA_FOOTER_HEIGHT_OFFSET = 3;
      }
      if (isAttachmentPresent) {
        attachmentId = +params['attachmentId'];
        if (Number.isNaN(attachmentId)) {
          parent.router.navigate([WorkDataComponent.NOT_VALID_PAGE_ERROR]);
          return;
        }
        this.model.isEnableAnnotation = false;
      }
      const promiseAll = [];
      if (parent.docId > 0 && parent.model.queueNameCde > 0) {
        promiseAll.push(parent.documentService.getDocumentData(parent.model.queueNameCde, parent.docId));
        promiseAll.push(parent.attributeService.getAttributeAttributeMapping(CONSTANTS.CACHE_ENTITY.ATTR_ATTR_MAPPING));
        // if (!isAttachmentPresent) {
        promiseAll.push(parent.utilityService.getDocType(parent.model.queueNameCde));
        promiseAll.push(parent.documentService.getDocumentUserData(parent.docId));
        // }
        // INFO : Incase of service call returns data and want the get it via promise follow below approach.
        Promise.all(promiseAll).then(function (data) {
          // DocumentData
          const documentDataList: DocumentData[] = data[0];
          if (parent.utilityService.isListHasValue(documentDataList)) {
            parent.documentData = documentDataList[0];
            parent.dataService.publishQueueNameCde(parent.model.queueNameCde, documentDataList[0].taskStatusCde);
          } else {
            parent.router.navigate([WorkDataComponent.NOT_VALID_PAGE_ERROR]);
            return;
          }

          // Attr Attr Map Data
          parent.model.attributeAttributeMapping = data[1];
          // Doctype Data
          if (data[2] === CONSTANTS.DOC_TYPE.FILE || isAttachmentPresent) {
            parent.isDocTypeFile = true;
            // Below line commented as there is a fix to be made for some pdfs not rendering the text layer,
            // temporarily added "parent.model.isEnableAnnotation = false" to display the document tab on load
            //parent.model.isEnableAnnotation = !parent.model.shouldLoadExtractionPathView;
            parent.model.isEnableAnnotation = false
          }
          if (data[2] === CONSTANTS.DOC_TYPE.EMAIL) {
            parent.isCaseEmail = true;
            parent.model.isEnableAnnotation = false;
          }
          parent.model.docUserDataList = data[3];
          parent.resizeChildComponents();

          parent.attachmentService.getAttachmentList(parent.docId).then(function (attachmentDataList: AttachmentData[]) {
            if (parent.utilityService.isListHasValue(attachmentDataList)) {
              parent.model.attachmentDataList = attachmentDataList;
              // If attachment is present send selected attachments to extracted data component.
              if (isAttachmentPresent) {
                const selectedAttachmentDataList = attachmentDataList.filter(attachData => attachData.attachmentId === attachmentId);
                if (parent.utilityService.isListHasValue(selectedAttachmentDataList)) {
                  parent.model.selectedAttachmentDataList = selectedAttachmentDataList;
                  parent.dataService.publishSelectedAttachmentDataEvent(selectedAttachmentDataList[0]);
                } else {
                  parent.router.navigate([WorkDataComponent.NOT_VALID_PAGE_ERROR]);
                  return;
                }
              } else {
                parent.model.selectedAttachmentDataList = parent.model.attachmentDataList;
              }
              parent.attributeService.getAttachmentAttributes(parent.docId).then(function (attrData) {
                parent.model.attachmentAttrDataList = attrData;
                parent.isDataReady = true;
              });
            } else {
              parent.isDataReady = true;
            }
          }).catch(function (attachmentData) {
            parent.isDataReady = true;
          });
        }).catch(function (data) {
          parent.documentData = null;
          parent.resizeChildComponents();
          parent.isDataReady = true;
        });
      } else {
        parent.router.navigate([WorkDataComponent.NOT_VALID_PAGE_ERROR]);
      }
    });
  }

  ngOnInit() {
    this.triggerTelemetryEvents(CONSTANTS.TELEMETRY_EVENT.START);
    // this.resizeChildComponents();
  }

  ngAfterViewInit() {
    this.triggerTelemetryEvents(CONSTANTS.TELEMETRY_EVENT.IMPRESSION);
  }

  ngOnDestroy() {
    this.triggerTelemetryEvents(CONSTANTS.TELEMETRY_EVENT.END);
  }

  @HostListener('window:resize', ['$event'])
  onResize(event) {
    this.resizeChildComponents();
  }

  private resizeChildComponents() {
    const windowHeight = window.innerHeight;
    const modelCaseDataHeight = (windowHeight - WorkDataComponent.CASE_DATA_HEIGHT_OFFSET);
    const vhConversionValue = 100 / windowHeight;

    if (this.isDocTypeFile) {
      this.modelCaseDetailsMiddleHeight = (modelCaseDataHeight * WorkDataComponent.CASE_DATA_MIDDLE_COMPONENT_HEIGHT_OFFSET_FILE);

      this.modelCaseDetailsTopHeight = modelCaseDataHeight * WorkDataComponent.CASE_DATA_TOP_COMPONENT_HEIGHT_OFFSET_FILE;
      const modelFileContentHeight = this.modelCaseDetailsTopHeight - WorkDataComponent.DOC_CONTENT_HEADER_HEIGHT_OFFSET;
      this.modelFileContentHeight = modelFileContentHeight * vhConversionValue;

      this.modelFileExtractedDataHeight = (this.modelFileContentHeight * 0.7) +
        (WorkDataComponent.EXTRACTED_DATA_FOOTER_HEIGHT_OFFSET * vhConversionValue);

      this.modelTaskListHeight = (this.modelCaseDetailsMiddleHeight - WorkDataComponent.HEADER_HEIGHT_OFFSET_FILE) * vhConversionValue;
      
      
      if(this.increaseFileExtractedDataHeight){
        this.modelFileExtractedDataHeight+=14        
      }      

    } else {
      this.modelCaseDetailsMiddleHeight = (modelCaseDataHeight * WorkDataComponent.CASE_DATA_MIDDLE_COMPONENT_HEIGHT_OFFSET);

      this.modelCaseDetailsTopHeight = modelCaseDataHeight * WorkDataComponent.CASE_DATA_TOP_COMPONENT_HEIGHT_OFFSET;
      const modelDocContentHeight = this.modelCaseDetailsTopHeight - WorkDataComponent.DOC_CONTENT_HEADER_HEIGHT_OFFSET;
      this.modelDocContentHeight = modelDocContentHeight * vhConversionValue;

      this.modelDocContentBodyHeight = (modelDocContentHeight - WorkDataComponent.DOC_CONTENT_SENT_DETAILS_HEIGHT_OFFSET)
        * vhConversionValue;

      this.modelTaskListHeight = (this.modelCaseDetailsMiddleHeight - WorkDataComponent.HEADER_HEIGHT_OFFSET) * vhConversionValue;
      this.modelSentEmailListHeight = (this.modelCaseDetailsMiddleHeight - WorkDataComponent.SENT_EMAIL_HEIGHT_OFFSET) * vhConversionValue;

    }
  }
  minHeightHandler(isComponentMinimized: boolean){
    this.increaseFileExtractedDataHeight=isComponentMinimized;
    this.resizeChildComponents()    
  }
}
