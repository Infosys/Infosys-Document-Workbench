/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import {
  Component, OnInit, Input, Output, EventEmitter, OnChanges, SimpleChange, ViewEncapsulation,
  OnDestroy,
  ChangeDetectorRef
} from '@angular/core';
import { ToastrService } from 'ngx-toastr';
import { AttributeData } from '../../data/attribute-data';
import { AttributeService } from '../../service/attribute.service';
import { DocumentData } from '../../data/document-data';
import { NgbModal, ModalDismissReasons, NgbModalOptions } from '@ng-bootstrap/ng-bootstrap';
import { UtilityService } from '../../service/utility.service';
import { MessageInfo } from '../../utils/message-info';
import { CONSTANTS } from '../../common/constants';
import { AttachmentService } from '../../service/attachment.service';
import { AttachmentData } from '../../data/attachment-data';
import { DataService } from '../../service/data.service';
import { ActionService } from '../../service/action.service';
import { AttributeHelper } from '../../utils/attribute-helper';
import { AttributeValidationData } from '../../data/attribute-validation-data';
import { AttributeAttributeMappingData } from '../../data/attribute-attribute-mapping-data';
import { NiaSortableColumnService } from '../nia-sortable-column/nia-sortable-column.service';
import { NiaSortableColumnData } from '../nia-sortable-column/nia-sortable-column-data';
import { AttributeSortKeyData } from '../../data/attribute-sort-key';
import { AnnotationService, EDData } from './annotation.service';
import { ExtractedDataHelper } from './extracted-data-helper';
import { ReExtractionService } from './re-extract.service';
import { BaseComponent } from '../../base.component';
import { SessionService } from '../../service/session.service';
import { ConfigDataHelper } from '../../utils/config-data-helper';
import { NiaTelemetryService } from '../../service/nia-telemetry.service';
import { Router, Event, NavigationEnd, NavigationError } from '@angular/router';

@Component({
  selector: 'app-extracted-data',
  templateUrl: './extracted-data.component.html',
  styleUrls: ['./extracted-data.component.scss'],
  encapsulation: ViewEncapsulation.None // For innerhtml styles to be applied.
})
export class ExtractedDataComponent extends BaseComponent implements OnInit, OnChanges, OnDestroy {
  getClassName(): string {
    return "ExtractedDataComponent";
  }
  private static TOTAL_COLUMN = 5;
  private TOOLTIP = 'For multiple values:\nPress {Enter} followed by {----} followed by {Enter}. \
  \nE.g.\nItem1\n----\nItem2';
  @Output() close = new EventEmitter<string>();
  @Input() isAddReqd = true;
  @Input() isDeleteReqd = true;
  @Input()
  set attributeAttributeMapping(attributeAttributeMapping: AttributeAttributeMappingData[]) {
    this.model.attributeAttributeMapping = attributeAttributeMapping;
  }
  @Input('minheight')
  set minheight(minheight: number) {
    this.model.minheight = minheight;
  }
  @Input('closeButton')
  set closeButton(closeButton: boolean) {
    this.model.closeButton = closeButton;
  }
  @Input()
  set isDocTypeFile(isDocTypeFile: boolean) {
    this.model.isDocTypeFile = isDocTypeFile;
  }
  @Input('popupButton')
  set popupButton(popupButton: boolean) {
    this.model.popupButton = popupButton;
  }

  @Input()
  set document(docData: DocumentData) {
  }

  @Input()
  set isDataReady(isDataReady: boolean) {
    this.model._isDataReady = isDataReady;
  }

  @Input()
  set queueNameCde(queueNameCde: number) {
    this.model.queueNameCde = queueNameCde;
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

  model: any = {
    attributeDataList: undefined,
    reExtractAttributeList: undefined,
    attributeDataValList: undefined,
    documentData: undefined,
    isBtnReExtractDisabled: undefined,
    isAddClicked: undefined,
    isAddAllowed: undefined,
    isReExtractionAllowed: false,
    isSaveAllowed: undefined,
    isCancelAllowed: undefined,
    isDataLoaded: false,
    isAttributesNotified: undefined,
    attachmentDataList: undefined,
    attachmentAttrDataList: undefined,
    attachmentAnnAttrDataList: undefined,
    attachmentAttrDataListFiltered: undefined,
    isAttachmentDataLoaded: false,
    areRowsSelected: false,
    selectedAttachmentId: undefined,
    isAddAttachmentAllowed: false,
    isAddAttachmentAttrClicked: false,
    isEmailAttrSelected: false,
    headerColspan: undefined,
    baseAttrDataList: [],
    annotatedAttributeDataList: undefined,
    addAttributeDataList: undefined,
    addAttributeValList: undefined,
    ERROR_MSG_ATTR_NAME_TXT_REQUIRED: undefined,
    ERROR_MSG_ATTR_NAME_TXT_INVALID: undefined,
    ERROR_MSG_ATTR_VALUE_REQUIRED: undefined,
    ERROR_MSG_ATTR_VALUE_DUPLICATE: undefined,
    attributeAttributeMapping: [],
    OPERATION_TYPE_EDIT: CONSTANTS.EXTRACTED_DATA_OPERATION_TYPE.ROW_LEVEL.EDIT,
    OPERATION_TYPE_OPTION_CHECKED: CONSTANTS.EXTRACTED_DATA_OPERATION_TYPE.ROW_LEVEL.OPTION_CHECKED,
    isCaseEditAllowed: false,
    minheight: undefined,
    closeButton: undefined,
    _isDataReady: undefined,
    popupButton: true,
    isEDReadyToModify: false,
    isTableSorted: undefined,
    nonTabularSortKeyMap: {},
    tabularColOrderKeyMap: {},
    currentTableColOrderMap: {},
    isReadOnlyTabularView: false,
    readOnlyTabularTxtCde: undefined,
    isReExtractionSliderEnabled: false,
    reExtractMinHeight: CONSTANTS.POP_OUT.RE_EXTRACT_CONFIRM,
    ERROR_MSG_ATTR_NAME_TXT_EXTRA_SPACES: undefined,
    queueNameCde:undefined,
    currentAttachmentId:0,
    sectionToAttrNameToValueMap: {},
    ATTR_VAL_SPLIT_DELIMITER: CONSTANTS.ATTRIBUTES.DELIMITER,
    ATTR_NAME_SUFFIX_LIST: CONSTANTS.ATTRIBUTES.ATTR_NAME_SUFFIX_LIST,
    sectionNum: 0,
    isExtractedDataTabSelected:true,
    isQnATabSelected:false,
    extractedDataTabCssDisplay:"block",
    qNaCssDisplay:"none",
    attachmentAttrWithDocIdDataList:undefined,
    ERROR_MSG_ATTR_VALUE_LIMIT: undefined
  }; // For binding to view
  private count = -1;
  private closeResult: string;
  private reExtractActionCompletedEvent;
  private annotationOpData;
  private annotationIndexData;
  private isReextractionPendingExist: boolean;
  private attributeDataEditList: AttributeData[];
  private attributeNameValuesList = [] as any;
  private attributesNotificationList: DocumentData;
  private attributeList: AttributeData[];
  private headerLevelEditDataList: AttributeData[];
  private isDocServiceLoaded = false;
  private deleteAttributeList: AttributeData[] = [];
  private attributeValidationResult = true;
  private selectedAttrNameTxtList = [];
  private selectedAttrNameCde;
  private _isComponentInitialLoad = false;
  private _annotationDataReceived = undefined;
  private _isAnnotationNotSupported = false;
  private _bsEDComponentEvent: any;
  private _isEDOnAddMode = false;
  // IE Variables
  private thWidthMap = new Map<string, Object[]>();
  private panelArrowElement;
  private attachmentId = 0;
  private __attrSourceImgData = {};
  private _selectedEDSourceNames = [];
  private _renderPageForEDSourceArray = [];

  // For disabling mouse click and keystrokes outside modal window
  ngbModalOptions: NgbModalOptions = {
    backdrop: 'static',
    keyboard: false
  };

  constructor(private attributeService: AttributeService, private modalService: NgbModal, private dataService: DataService,
    private toastr: ToastrService, private utilityService: UtilityService, private msgInfo: MessageInfo,
    private attachmentService: AttachmentService, private actionService: ActionService, private attributeHelper: AttributeHelper,
    private niaSortTableService: NiaSortableColumnService, private annotationService: AnnotationService,
    private extractedDataHelper: ExtractedDataHelper, private reextractService: ReExtractionService,
    private changeDetector: ChangeDetectorRef, public sessionService: SessionService, private router: Router,
    public configDataHelper: ConfigDataHelper, public niaTelemetryService:NiaTelemetryService) {
      super(sessionService, configDataHelper, niaTelemetryService);

    }


  routeToEP() {
    let url = '/home/workdata/' + this.model.queueNameCde + '/' + this.model.documentData.docId;
    url+='/extractionpath/'+this.attachmentId;
    if(this.attachmentId>0){
      this.router.navigate([url]);
    }
  }

  ngOnInit() {
    this.getAttributesSortOrderKeyFromDB();
    this.reExtractActionCompletedEvent = this.dataService.reExtractActionCompletedEvent.subscribe(message => {
      if (message) {
        this.refreshComponent();
      }
    });
    if (this.model.popupButton) {
      this.annotationOpData = this.dataService.annotationOpData.subscribe(annotationData => {
        if (annotationData !== null) {
          this.model.isReExtractionSliderEnabled = false;
          const edDataObj = this.createEDDataObj();
          if (annotationData.annotation.op !== CONSTANTS.OPERATION_TYPE.DELETE) {
            const attrData = new AttributeData(null, null, annotationData.attachmentId, null,
              null, annotationData.annotation.text, annotationData.annotation.quote,
              CONSTANTS.ATTRIBUTES.EXTRACT_TYPE_CDE.MANUALLY_CORRECTED, null,
              CONSTANTS.ATTRIBUTES.CONFIDENCE_PCT.MAX, null, null, '', null, new AttributeValidationData(null, undefined));
            if (annotationData.annotation.attrId !== undefined) {
              attrData['attrId'] = annotationData.annotation.attrId;
            }
            this.annotationService.processAnnotatedData(attrData, this.model, edDataObj);
          } else {
            this.annotationService.deleteAttrForAnnotation(annotationData, this.model, edDataObj);
          }
          this.restoreDataFromEDDataObj(edDataObj);
        }
      });
      this.annotationIndexData = this.dataService.annotationIndexList.subscribe(annotationData => {
        if (annotationData !== null) {
          this._annotationDataReceived = annotationData;
          this.annotationService.addAnnIndexToAttribute(annotationData, this.model);
          this.changeDetector.detectChanges();
        }
      });

      this._bsEDComponentEvent = this.dataService.postToEDComponent.subscribe(data => {
        if (data !== null) {
          if (data instanceof Object) {
            if (data.hasOwnProperty('isFileComponentLoaded')) {
              this.model.isEDReadyToModify = data.isFileComponentLoaded;
            }
            if (data.hasOwnProperty('isAnnotationNotSupported')) {
              this._isAnnotationNotSupported = data.isAnnotationNotSupported;
            }
            if (data.hasOwnProperty('attachmentId')) {
              this.attachmentId = data.attachmentId;
              this.model.currentAttachmentId = data.attachmentId;
              this.refreshComponent();
            }
            if (data.hasOwnProperty('isPDFZoomFactorChanged')) {
              this.onPDFZoomManageSelectedAttribute();
            }
            if (data.hasOwnProperty('pdfPageRendered')) {
              this.onPDFPageRenderedManageSelectedAttribute(data['pdfPageRendered']);
            }
          }
        }
      });
    }
  }

  private captureMultiValAttributes(){
    // Capturing Attributes with Multiple Values of Document and Case Level
    const parent = this;
    if(parent.model.attachmentAttrDataList.length>0){
      for(let attrData of parent.model.attachmentAttrDataList){
        // For attachment level attributes, sectionNum = attachmentId
        const sectionNum = attrData.attachmentId

        parent.model.sectionToAttrNameToValueMap[sectionNum] = {}
        for(let itr of attrData.attributes){
          if(itr.attrNameTxt!= null && itr.attrNameTxt.endsWith(parent.model.ATTR_NAME_SUFFIX_LIST)){
            parent.model.sectionToAttrNameToValueMap[sectionNum][itr.attrNameTxt] =  itr.attrValue;
          }
          else if(itr.attrNameTxt=="" || itr.attrNameCde==44){
            if(itr.attributes.length>0){
                for(let attr of itr.attributes){
                  if(attr.attrNameTxt!= null && attr.attrNameTxt.endsWith(parent.model.ATTR_NAME_SUFFIX_LIST)){
                    parent.model.sectionToAttrNameToValueMap[sectionNum][attr.attrNameTxt] = attr.attrValue;
                  }
                }
            }
          }
        }
      }
    }

    if(parent.model.documentData){
      // For case level attributes, sectionNum = 0
      const sectionNum = 0

      parent.model.sectionToAttrNameToValueMap[sectionNum] = {}
      for(let docData of parent.model.documentData.attributes){

        if(docData.attrNameTxt!= null && docData.attrNameTxt.endsWith(parent.model.ATTR_NAME_SUFFIX_LIST)){
          parent.model.sectionToAttrNameToValueMap[sectionNum][docData.attrNameTxt] = docData.attrValue;
        }
        if(docData.attributes != null){
          for(let docDataAttr of docData.attributes){
            if(docDataAttr.attrNameTxt!= null && docDataAttr.attrNameTxt.endsWith(parent.model.ATTR_NAME_SUFFIX_LIST)){
              parent.model.sectionToAttrNameToValueMap[sectionNum][docDataAttr.attrNameTxt] = docDataAttr.attrValue;
            }
            else if(docDataAttr.attrNameTxt=="" || docDataAttr.attrNameCde==44){
              if(docDataAttr.attributes.length>0){
                  for(let attr of docDataAttr.attributes){
                    if(attr.attrNameTxt!= null && attr.attrNameTxt.endsWith(parent.model.ATTR_NAME_SUFFIX_LIST)){
                      parent.model.sectionToAttrNameToValueMap[sectionNum][attr.attrNameTxt] = attr.attrValue;
                    }
                  }
              }
            }
          }
        }
      }
    }
    console.log("attachment sectionToAttrNameToValueMap--->",parent.model.sectionToAttrNameToValueMap);
  }

  setSectionNum(val){
    this.model.sectionNum = val;
  }

  ngOnChanges(changes: { [propKey: string]: SimpleChange }) {
    if (changes['isDataReady'] && this.model._isDataReady) {
      this.model.attributeDataList = [];
      this.model.isAddAllowed = false;
      this.model.isAddAttachmentAllowed = false;
      this.model.isDataLoaded = false;
      this.model.isAttachmentDataLoaded = false;
      this.model.isAttributesNotified = false;
      this.model.attributeDataList = [];
      this.model.headerColspan = ExtractedDataComponent.TOTAL_COLUMN - 2;
      this.model.ERROR_MSG_ATTR_NAME_TXT_REQUIRED = this.msgInfo.getMessage(167);
      this.model.ERROR_MSG_ATTR_NAME_TXT_INVALID = this.msgInfo.getMessage(168);
      this.model.ERROR_MSG_ATTR_VALUE_REQUIRED = this.msgInfo.getMessage(169);
      this.model.ERROR_MSG_ATTR_VALUE_DUPLICATE = this.msgInfo.getMessage(173);
      this.model.ERROR_MSG_ATTR_NAME_TXT_EXTRA_SPACES = this.msgInfo.getMessage(176);
      this.model.ERROR_MSG_ATTR_VALUE_LIMIT = this.msgInfo.getMessage(187);
      this.model.isEDReadyToModify = !this.model.popupButton;
      if (changes['document']) {
        this.model.documentData = changes['document'].currentValue;
      }
      this._isComponentInitialLoad = changes['attachmentAttrDataList'] &&
        this.utilityService.isListHasValue(changes['attachmentAttrDataList'].currentValue);
      if (!this.model.popupButton) {
        this.refreshComponent();
      }
    }
  }
  ngOnDestroy() {
    if (this.reExtractActionCompletedEvent != null) {
      this.reExtractActionCompletedEvent.unsubscribe();
    }
    if (this.annotationOpData != null) {
      this.annotationOpData.unsubscribe();
    }
    if (this.annotationIndexData != null) {
      this.annotationIndexData.unsubscribe();
    }
    if (this._bsEDComponentEvent != null) {
      this._bsEDComponentEvent.unsubscribe();
    }
    // To reset the last published value as the behavior subject always stores the last value published.
    this.dataService.publishAttributeOpData(CONSTANTS.OBSERVABLE_NULL);
    this.dataService.publishExtractedDataCustomEvent(CONSTANTS.OBSERVABLE_NULL);
  }

  addModal(attachmentId?: number) {
    if (this.getFeature(this.bmodel.FID.ATTRIBUTE_CREATE).isEnabled && this.extractedDataHelper.isCUDOperationAllowed(this.model) && this.attributeValidationResult) {

      if (this.isReextractionPendingExist) {
        this.toastr.error(this.msgInfo.getMessage(149));
        return;
      }
      if (!this.extractedDataHelper.checkAttributeValidData(this.model.annotatedAttributeDataList) || this.model.isAddClicked ||
        this.model.isAddAttachmentAttrClicked) {
        if (this.model.isSaveAllowed) {
          this.model.isSaveAllowed = false;
          if (!this.isListHasValue(this.model.addAttributeDataList) ||
            this.checkValidManualAddAttribute(this.model.addAttributeDataList[this.model.addAttributeDataList.length - 1])) {
            const attributeData = new AttributeData(null, null, null, null, null, null, '',
              CONSTANTS.ATTRIBUTES.EXTRACT_TYPE_CDE.MANUALLY_CORRECTED, null,
              CONSTANTS.ATTRIBUTES.CONFIDENCE_PCT.MAX, null, null, null, null,
              new AttributeValidationData(null, null), true);
            attributeData.attrNameCde = +this.selectedAttrNameCde > 0 ?
              +this.selectedAttrNameCde : CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE;
            attributeData['attrId'] = this.extractedDataHelper.counter();
            attributeData['isLastAddedAttribute'] = true;
            this.attributeValidationResult = false;
            this.model.addAttributeDataList.push(attributeData);
          }
        }

        return;
      }
      this.model.isReExtractionSliderEnabled = false;
      let selectedAttrDataList = [];
      if (attachmentId !== undefined && attachmentId > 0) {
        this.model.selectedAttachmentId = attachmentId;
        this.model.isAddAttachmentAttrClicked = true;
        if (this.utilityService.isListHasValue(this.model.attachmentAttrDataList)) {
          selectedAttrDataList = this.model.attachmentAttrDataList.filter(a => a.attachmentId === attachmentId);
        }
        if (this.utilityService.isListHasValue(selectedAttrDataList)) {
          selectedAttrDataList = selectedAttrDataList[0].attributes;
        }
      } else {
        this.model.isAddClicked = true;
        selectedAttrDataList = this.model.attributeDataList;
      }
      this.notifyEDState(); // Extracted Data state
      const edDataObj = this.createEDDataObj();
      this.extractedDataHelper.filterNonListedAttributes(selectedAttrDataList, this.model, edDataObj, true, this.attributeNameValuesList);
      this.restoreDataFromEDDataObj(edDataObj);
      this.extractedDataHelper.enableDisableFields(this.model, this.attributeValidationResult, this.attributeDataEditList,
        this.deleteAttributeList);
    }

  }

  cancelModal() {
    if (this.model.isReExtractionAllowed) {
      this.headerLevelEditDataList = [];
      this.reextractService.cancelReExtract(this.model);
    } else {
      // if (this.alertUser()) {
      //   this.model.isCancelAllowed = false;
      this.refreshComponent(true);
      // }
    }
  }

  saveModal() {
    this.model.isSaveAllowed = false;
    if (this.isReextractionPendingExist) {
      this.toastr.error(this.msgInfo.getMessage(149));
      return;
    }
    let promiseAll = [];
    let isManageMultiAttribute = false;
    if (this.isListHasValue(this.model.addAttributeDataList) || this.isListHasValue(this.model.annotatedAttributeDataList)) {
      promiseAll.push(this.addAttributes());
    }
    const editAttributeList = this.generateModifiedAttributeList(CONSTANTS.OPERATION_TYPE.EDIT);
    if (this.isListHasValue(editAttributeList[0])) {
      promiseAll.push(this.editAttributes(editAttributeList[0]));
    }
    const deleteAttributeList = this.generateModifiedAttributeList(CONSTANTS.OPERATION_TYPE.DELETE);
    if (this.isListHasValue(deleteAttributeList[0])) {
      promiseAll.push(this.deleteNormalAttributes(deleteAttributeList[0]));
    }
    if (this.isListHasValue(deleteAttributeList[1]) || this.isListHasValue(editAttributeList[1])) {
      isManageMultiAttribute = true;
      promiseAll = promiseAll.concat(this.manageMultiAttributes(deleteAttributeList[1], editAttributeList[1], true));
    }
    if (this.isListHasValue(deleteAttributeList[2]) || this.isListHasValue(editAttributeList[2])) {
      const length = deleteAttributeList[2].length > editAttributeList[2].length
        ? deleteAttributeList[2].length : editAttributeList[2].length;
      for (let i = 0; i < length; i++) {
        const delList = this.isListHasValue(deleteAttributeList[2][i]) ? deleteAttributeList[2][i] : [];
        const editList = this.isListHasValue(editAttributeList[2][i]) ? editAttributeList[2][i] : [];
        if (this.isListHasValue(delList) || this.isListHasValue(editList)) {
          isManageMultiAttribute = true;
          promiseAll = promiseAll.concat(this.manageMultiAttributes(delList, editList, false));
        }
      }
    }
    const parent = this;
    Promise.all(promiseAll).then(function (data) {
      if (parent.model.isDocTypeFile) {
        parent.dataService.publishExtractedDataCustomEvent(CONSTANTS.CUSTOM_EVENT.EXTRACTED_DATA.SAVE_COMPLETED);
      }
      if (promiseAll.length > 0) {
        parent.toastr.success(parent.msgInfo.getMessage(164));
      }
      parent.refreshComponent();
    }).catch(function (error) {
      if (isManageMultiAttribute) {
        parent.toastr.error(parent.msgInfo.getMessage(165));
        parent.refreshComponent();
      } else {
        parent.toastr.error(parent.msgInfo.getMessage(166));
      }
    });

  }

  refreshComponent(isRefreshOrCancelClicked?: boolean, event?: string) {
    let isRefreshAllowed = true;
    if (isRefreshOrCancelClicked) {
      if (this.alertUser()) {
        this.model.isCancelAllowed = false;
      } else {
        isRefreshAllowed = false;
      }
    }
    if (isRefreshAllowed) {
      this.onEDRefresh();
      const isLHSRefreshRequired = this.validateTabularSaveToRefreshLHS(event);
      this.model.isReadOnlyTabularView = false;
      this._isEDOnAddMode = false;
      this.model.isAddClicked = false;
      this.model.isAddAttachmentAttrClicked = false;
      this.model.areRowsSelected = false;
      this.model.selectedAttachmentId = undefined;
      this.model.isEmailAttrSelected = false;
      this.model.isBtnReExtractDisabled = true;
      this.selectedAttrNameCde = undefined;
      this.isDocServiceLoaded = false;
      this.isReextractionPendingExist = false;
      this.model.baseAttrDataList = [];
      this.model.annotatedAttributeDataList = [];
      this.model.addAttributeDataList = [];
      this.deleteAttributeList = [];
      this.selectedAttrNameTxtList = [];
      this.attributeValidationResult = true;
      this.model.isReExtractionAllowed = false;
      const parent = this;
      if (this.isDocTypeFile && this.model.popupButton && !parent._isComponentInitialLoad
        && !parent._isAnnotationNotSupported && isLHSRefreshRequired) {
        parent.dataService.publishExtractedDataCustomEvent(CONSTANTS.CUSTOM_EVENT.EXTRACTED_DATA.REFRESH_COMPLETED);
      }
      parent.getData(function () {
        parent.extractedDataHelper.enableDisableFields(parent.model, parent.attributeValidationResult, parent.attributeDataEditList,
          parent.deleteAttributeList);
        parent.isCaseEditable();
        if (parent.model.documentData !== undefined) {
          if (parent.model.isDocTypeFile && parent._annotationDataReceived) {
            parent.annotationService.addAnnIndexToAttribute(parent._annotationDataReceived, parent.model);
          }
          parent.actionService.getActions(parent.model.documentData.docId, function (error, data) {
            let tempDataList = [];
            if (!error && data[0] !== undefined) {
              tempDataList = data[0].actionDataList;
            }
            parent.isReextractionPendingExist = parent.utilityService.isReExtractPending(tempDataList);
            parent.model.isReExtractionSliderEnabled = !parent.isReextractionPendingExist;
            parent.captureMultiValAttributes();
            parent.applyCustomSort();

          });
        }
      });


    }



  }

  onRowChecboxClick(attachmentId?: number) {
    this.reextractService.onRowChecboxClick(this.model, this.headerLevelEditDataList, attachmentId);
  }

  onHeadRowChecboxClick(attachmentId?: number) {
    this.headerLevelEditDataList = this.reextractService.onHeadRowChecboxClick(this.model, this.headerLevelEditDataList, attachmentId);
  }

  open(content, isReextractFlow) {

    if (this.alertUser()) {
      const parent = this;
      if (isReextractFlow) {
        if (parent.reextractService.validateToReextract(parent.model)) {
          parent.model.reExtractMinHeight = 0;
          if (parent.isListHasValue(parent.model.baseAttrDataList)) {
            parent.model.reExtractMinHeight = CONSTANTS.POP_OUT.RE_EXTRACT_CONFIRM;
          }
          parent.openTemplate(content);
        }
      } else {
        parent.openTemplate(content);
      }
    }
  }

  openTemplate(content, attrData?, attributeDataList?) {
    const parent = this;
    if (attrData) {
      if (!parent.model.isCaseEditAllowed) {
        parent.model.isReadOnlyTabularView = true;
        parent.model.readOnlyTabularTxtCde = 0;
      } else if (parent.extractedDataHelper.isUnsavedDataExist(this.model, this.attributeDataEditList,
        this.deleteAttributeList)) {
        parent.model.isReadOnlyTabularView = true;
        parent.model.readOnlyTabularTxtCde = 172;
      }

      attributeDataList.some(attr => {
        if (parent.attributeHelper.isKeyAttribute(attr.attrNameCde)) {
          parent.model.currentTableColOrderMap = parent.model.tabularColOrderKeyMap[attr.attrNameCde + '_' + attr.attrValue];
          return true;
        }
      });
      parent.model.attachmentAttrDataListFiltered = [JSON.parse(JSON.stringify(attrData))];
    }
    parent.utilityService.toggleWhenIEPdfEmbedLayer(parent.model.closeButton);
    parent.modalService.open(content, parent.ngbModalOptions).result.then((result) => {
      parent.closeResult = `Closed with: ${result}`;
      parent.utilityService.toggleWhenIEPdfEmbedLayer(parent.model.closeButton);
      if (result !== CONSTANTS.EVENT.CANCEL) {
        parent.refreshComponent(result);
      }
    }, (reason) => {
      parent.closeResult = `Dismissed ${parent.getDismissReason(reason)}`;
    });
  }

  modalWindow() {
    this.close.emit('window closed');
    this.model.closeButton = false;
    this.model.popupButton = true;
  }

  getHtmlTextConversion(htmlText: string) {
    let convertedHtmlText = '';
    if (htmlText !== undefined) {
      convertedHtmlText = htmlText.replace(/<\/?[^>]+>/ig, ' ');
    }
    return convertedHtmlText;
  }

  getAllowedValues(selectedAttrNameCde, attrData: AttributeData) {
    this.extractedDataHelper.getAllowedValues(selectedAttrNameCde, attrData, this.model, this.attributeNameValuesList);
  }

  // To check whether given attrNameCde is Multi attribute.
  isMultiAttribute(attrNameCde: number): boolean {
    return this.attributeHelper.isMultiAttribute(attrNameCde);
  }

  // To change attribute row icon from delete to undo in UI.
  deleteAttribute(attributeData: AttributeData) {
    const edDataObj = this.createEDDataObj();
    this.extractedDataHelper.deleteAttribute(attributeData, this.model, edDataObj);
    this.restoreDataFromEDDataObj(edDataObj);
    this.getFeature(this.bmodel.FID.ATTRIBUTE_DELETE).isEnabled
  }

  // To perform undo delete operation from UI.
  undoDelete(attributeData: AttributeData) {
    attributeData.deleteClicked = false;
    attributeData.attrValue = attributeData['attrValueOrg'];
    attributeData.attrNameValidation.attrValueValidationResult = undefined;
    this.extractedDataHelper.publishAttributeOpDataEvent(CONSTANTS.OPERATION_TYPE.UNDO_DELETE, attributeData, this.model.isDocTypeFile,
      true);
    this.deleteAttributeList = this.deleteAttributeList.filter(attrData => attrData !== attributeData);
    this.extractedDataHelper.isSaveButtonAllowed(this.model, this.attributeDataEditList, this.deleteAttributeList);
  }

  // To check delete icon allowed from UI (To avoid hardcoding of attrNameCde)
  isDeleteAllowed(attributeData: AttributeData): boolean {
    return !(attributeData.attrNameCde === CONSTANTS.ATTR_NAME_CDE.DOCUMENT_TYPE
      || attributeData.attrNameCde === CONSTANTS.ATTR_NAME_CDE.CATEGORY)
      && !attributeData.deleteClicked;
  }

  validateIsEDReadyToModify(): boolean {
    return (this.model.isDocTypeFile && !this.model.isEDReadyToModify);
  }

  // Check list has values from UI
  isListHasValue(objList: any[]): boolean {
    return this.utilityService.isListHasValue(objList);
  }

  // To remove selected attribute from newly added list in UI using cancel button.
  removeManuallyAddedAttribute(attrData: AttributeData): void {
    this.extractedDataHelper.removeManuallyAddedAttribute(attrData, this.model, this.attributeList, this.attributeDataEditList,
      this.deleteAttributeList);
    this.attributeValidationResult = this.extractedDataHelper.checkIfNoValidationErrExists(this.model, this.attributeDataEditList);
  }

  // To remove selected attribute from annotated list in UI using cancel button.
  removeAnnotatedAttribute(attrData: AttributeData): void {
    this.extractedDataHelper.removeAnnotatedAttribute(attrData, this.model, this.attributeDataEditList, this.deleteAttributeList);
    this.attributeValidationResult = this.extractedDataHelper.checkIfNoValidationErrExists(this.model, this.attributeDataEditList);
  }

  // To send the attribute add event based on condition to the file-content for annotations
  notifyAttributeAddEvent(attrData: AttributeData) {
    this.extractedDataHelper.notifyAttributeAddEvent(attrData, this.model);
  }

  // For Dynamic row size in UI for textarea input.
  getRowSize(content: any) {
    const textareaElements = document.querySelectorAll('.get-row-size');
    let textareaWidth = textareaElements[0].clientWidth;
    if (!this.model.popupButton) {
      textareaWidth = textareaElements[textareaElements.length - 1].clientWidth;
    }
    // 5.20 is padding & margin width
    textareaWidth -= 5.20;
    let i = 1;
    if (this.utilityService.isStringHasValue(content)) {
      const offset = CONSTANTS.ATTRIBUTES.ATTR_VALUE_TEXTBOX_OFFSET / textareaWidth;
      const contentArray = content.split(/\n/);
      if (contentArray.length > 1) {
        i = contentArray.length;
        contentArray.forEach(contentLine =>
          i += Math.floor(contentLine.length * offset));
      } else {
        if (content.length > offset) {
          i = Math.ceil(content.length * offset);
        }
      }
    }
    if (i > 2) {
      i = CONSTANTS.INPUT.TEXTAREA.OFFSET;
    }
    return i;
  }

  // To updates the attribute validation from UI.
  updateAttributeValidationResult(attrData: AttributeData, obj1, obj2) {
    this.model.isCancelAllowed = true;
    if (obj1) {
      attrData.attrNameValidation.attrNameTxtValidationResult = this.checkErrorExist(obj1);
      if (this.utilityService.isStringHasValue(attrData.attrNameTxt)) {
        this.extractedDataHelper.validateAttrNameTxt(attrData, this.model, this.selectedAttrNameTxtList);
      } else {
        attrData.attrNameValidation.attrNameTxtValidationResult = { 'attrNameTxtRequired': true };
      }
    }
    if (obj2) {
      this.isAttributeChanged(attrData);
      attrData.attrNameValidation.attrValueValidationResult = this.checkErrorExist(obj2);
      const attrValue = attrData.attrValue;
      if (this.utilityService.isStringHasValue(attrValue)) {
        const values = attrValue.toLowerCase().split(CONSTANTS.ATTRIBUTES.DELIMITER);
        if (this.utilityService.getIfDuplicatesExist(values)) {
          attrData.attrNameValidation.attrValueValidationResult = { 'attrValueDuplicate': true };
        }
        if (attrValue.length >= 300) {
          attrData.attrNameValidation.attrValueValidationResult = { 'attrValueLimit': true };
       }
      } else {
        attrData.attrNameValidation.attrValueValidationResult = { 'attrValueRequired': true };
      }
    }
    let isValid = true;
    if (this.isListHasValue(this.model.addAttributeDataList)) {
      isValid = this.extractedDataHelper.checkAttributeValidData(this.model.addAttributeDataList, true);
    }
    // If already invalid then avoid checking for annotate attributes.
    if (isValid && this.isListHasValue(this.model.annotatedAttributeDataList)) {
      isValid = this.extractedDataHelper.checkAttributeValidData(this.model.annotatedAttributeDataList);
    }
    let isFinalValid = false;
    if (this.extractedDataHelper.isUnsavedDataExist(this.model, this.attributeDataEditList, this.deleteAttributeList)) {
      isFinalValid = this.extractedDataHelper.checkValidData(isValid, this.attributeDataEditList);
      this.attributeValidationResult = isFinalValid;
    } else {
      this.attributeValidationResult = true;
    }
    this.model.isSaveAllowed = isFinalValid;
  }

  // AnnotateChange event
  moveAnnotateDataToAddAttributeData(attrData: AttributeData) {
    const edDataObj = this.createEDDataObj();
    attrData['index'] = undefined;
    this.annotationService.moveAnnotateDataToAddAttributeData(attrData, this.model, edDataObj);
    this.restoreDataFromEDDataObj(edDataObj);
  }

  // dbEditListGeneration
  modifiedDbData(attributeData: AttributeData) {
    this.model.isReExtractionSliderEnabled = false;
    this.model.isCancelAllowed = true;
    this.extractedDataHelper.notifyAttributeAddEvent(attributeData, this.model);
  }

  isKeyAttrDisableRequired(attrData: AttributeData): boolean {
    return this.attributeHelper.isKeyAttribute(attrData.attrNameCde) && (this.model.isAddClicked || this.model.isAddAttachmentAttrClicked)
      || this.validateIsEDReadyToModify();
  }

  // To hide re extract button if user modifies data
  hideReExtract() {
    this.model.isReExtractionSliderEnabled = false;
  }

  // To check whether that selected checkbox or text area or input box is to be disabled or not.
  isRowOperationAllowed(operationName, attributeData?: AttributeData) {
    let isDisableRequired = false;
    if (operationName === CONSTANTS.EXTRACTED_DATA_OPERATION_TYPE.ROW_LEVEL.EDIT) {
      isDisableRequired = this.model.isReExtractionAllowed || attributeData.deleteClicked;
      if (!isDisableRequired && this.isListHasValue(attributeData.allowedValues)) {
        isDisableRequired = this.isKeyAttrDisableRequired(attributeData);
      }
    }
    if (operationName === CONSTANTS.EXTRACTED_DATA_OPERATION_TYPE.ROW_LEVEL.OPTION_CHECKED) {
      isDisableRequired = !attributeData['isOptionCheckAllowed'];
    }
    return !this.model.isCaseEditAllowed || this.isReextractionPendingExist || isDisableRequired || this.validateIsEDReadyToModify();
  }

  // To enable Re-Extract after toggle changed
  enableReExtract() {
    const parent = this;
    if (parent.model.isReExtractionAllowed) {
      /**Re-extraction not allowed when pending action present on case */
      parent.isPendingActions().then(function (isExist) {
        if (isExist) {
          parent.toastr.error(parent.msgInfo.getMessage(147));
          parent.model.isReExtractionAllowed = false;
        } else {
          parent.annotationService.notifyFCToSwitchReadMode(parent.model.isDocTypeFile,
            CONSTANTS.CUSTOM_EVENT.EXTRACTED_DATA.READ_ONLY_ACTIVATED);
        }
      });
    } else {
      parent.annotationService.notifyFCToSwitchReadMode(parent.model.isDocTypeFile,
        CONSTANTS.CUSTOM_EVENT.EXTRACTED_DATA.READ_ONLY_DEACTIVATED);
    }
  }

  // To check Header is clicked for add/delete operation
  isCUDOperationAllowed() {
    return this.extractedDataHelper.isCUDOperationAllowed(this.model);
  }

  isShowAttrIndex(attribute: AttributeData) {
    let isShow = true;
    if (this.utilityService.isAValidValue(attribute.attrNameValidation.attrNameTxtValidationResult) ||
      this.utilityService.isAValidValue(attribute.attrNameValidation.attrValueValidationResult)) {
      isShow = false;
      attribute['index'] = undefined;
    } else {
      isShow = this.utilityService.isAValidValue(attribute['index']) ? true : false;
    }
    return isShow;
  }


  getTitle(attributeData: any) {
    return this.utilityService.getTitle(attributeData);
  }

  /************************* IE METHODS STARTS*************************/
  // To stick table head using dom based on scrolling for replacing position:sticky(combination of fixed and relative) css.
  stickTableHead() {
    if (this.utilityService.getBrowserType() === CONSTANTS.BROWSER_TYPE.INTERNET_EXPLORER) {
      const id: string = this.getTableId();
      const theadTrElements = document.querySelector(id + ' > thead > tr');
      const staticElements = document.querySelectorAll(id + ' > thead > tr > th');
      const offset = document.querySelectorAll('.ed-content-text')[+id.substring(id.length - 1)].scrollTop;
      // If scrolling offset value will be populated.
      if (offset > 0) {
        theadTrElements['style'].position = 'fixed';
        theadTrElements['style'].zIndex = 1;
        for (let i = 0; i < staticElements.length - 1; i++) {
          staticElements[i]['style'].minWidth = this.thWidthMap.get(id)[i] + 'px';
          staticElements[i]['style'].top = '';
        }
      } else {
        for (let i = 0; i < staticElements.length; i++) {
          staticElements[i]['style'].minWidth = '';
          staticElements[i]['style'].top = '';
        }
        theadTrElements['style'].position = 'relative';
        theadTrElements['style'].zIndex = '';
      }
    }
  }

  // To store table default th cell based on table size by accessing dom.
  storeTableHeadWidth() {
    if (this.utilityService.getBrowserType() === CONSTANTS.BROWSER_TYPE.INTERNET_EXPLORER) {
      const id = this.getTableId();
      const tdElements = document.querySelectorAll(id + ' tr[data-relative-parent]')[0].children;
      const existingThWidths = this.thWidthMap.get(id);
      let isThWidthNeeded = true;
      // To avoid multiple assigning for same width.
      if (this.isListHasValue(existingThWidths) && existingThWidths[0] === tdElements[0].clientWidth) {
        isThWidthNeeded = false;
      }
      // Table head width fix after click on collapse when scroll present.
      const arrowElement = document.querySelectorAll('.navbar-inverse > div')[0].querySelector('button').outerHTML;
      if (isThWidthNeeded || this.panelArrowElement !== arrowElement) {
        const thWidths = [];
        for (let i = 0; i < tdElements.length; i++) {
          thWidths.push(tdElements[i].clientWidth);
        }
        this.thWidthMap.set(id, thWidths);
        this.stickTableHead();
      }
      this.panelArrowElement = arrowElement;
    }
  }

  private highlightSelectedAttributeRow(attributeData){
    console.log("highlightSelectedAttributeRow");
    const parent = this;
    const attrNameTxt = attributeData["attrNameTxt"];
    if(parent._selectedEDSourceNames.indexOf(attrNameTxt)>-1){
      attributeData["isHighlighted"] = true;
    }else{
      attributeData["isHighlighted"] = false;
    }
  }

  private getOriginalImgData(){
    console.log("getOriginalImgData")
    const parent = this;
    if (parent.utilityService.isListHasValue(parent.model.attachmentAnnAttrDataList)){
      const foundAttrDataList = JSON.parse(parent.model.attachmentAnnAttrDataList[0]["attrValue"]);
      if(parent.utilityService.isListHasValue(foundAttrDataList)){
        for(const foundAttrData of foundAttrDataList){
          const c:HTMLCanvasElement = document.querySelector("div[data-page-number='"+foundAttrData['page']+"']>div>canvas") as HTMLCanvasElement;
          if(c){
            const annKey = foundAttrData['page'];
            const ctx = c.getContext("2d");
            if(!(annKey in parent.__attrSourceImgData)){
              parent.__attrSourceImgData[annKey] = ctx.getImageData(0, 0, c.width, c.height);
            }
          }
        }
      }
    }
  }

  manageSelectedAttribute(attributeData:AttributeData, isNavigateToPage){
    const parent = this;
    const contentAnnotation = parent.getContentAnnotateData(attributeData);
    if(isNavigateToPage && contentAnnotation && contentAnnotation.length>0){
      parent.navigateToPDFPage(contentAnnotation[0],attributeData, false);
    }
    parent.annotateAttrNameSource(attributeData.attrNameTxt, !isNavigateToPage);
    parent.highlightSelectedAttributeRow(attributeData);
  }

  expandSelectedAttributeRow(attributeData){
    const parent = this;
    if (!parent.checksourceBbox(attributeData.attrNameTxt)){
      parent.toastr.info(parent.msgInfo.getMessage(180));
      return;
    }
    attributeData["shouldShowMore"] = (attributeData["shouldShowMore"])?false:true;
  }

  private annotateAttrNameSource(attrNameTxt, isZoomFactorChanged){
    const parent = this;
    console.log("annotateAttrNameSource",attrNameTxt)

    if (!parent.checksourceBbox(attrNameTxt)){
      parent.toastr.info(parent.msgInfo.getMessage(179));
      return;
    }

    parent.getOriginalImgData();
    if (parent.utilityService.isListHasValue(parent.model.attachmentAnnAttrDataList)){
      const annotations = JSON.parse(parent.model.attachmentAnnAttrDataList[0]["attrValue"]);
      const foundAttrDataList = annotations.filter(x=>x["text"]===attrNameTxt);
      if(parent.utilityService.isListHasValue(foundAttrDataList)){
        for(const foundAttrData of foundAttrDataList){
          const c:HTMLCanvasElement = document.querySelector("div[data-page-number='"+foundAttrData['page']+"']>div>canvas") as HTMLCanvasElement;
          if(c){
            const sdw = c.width/foundAttrData["pageBbox"][2];
            const sdh = c.height/foundAttrData["pageBbox"][3];
            const ctx = c.getContext("2d");
            const fillTxtMargin = 2;
            ctx.lineWidth = 1;
            ctx.strokeStyle = "red";
            ctx.fillStyle = "red";
            const annKey = foundAttrData['page'];
            if(isZoomFactorChanged || parent._selectedEDSourceNames.indexOf(attrNameTxt)==-1){
              // ---- Add annotation ----
              const l = foundAttrData['sourceBbox'][0]*sdw;
              const t = foundAttrData['sourceBbox'][1]*sdh;
              const w = foundAttrData['sourceBbox'][2]*sdw;
              const h = foundAttrData['sourceBbox'][3]*sdh;
              ctx.fillText(foundAttrData["text"],l,t-fillTxtMargin, w)
              ctx.strokeRect(l,t,w,h);
              if(parent._selectedEDSourceNames.indexOf(attrNameTxt)==-1){
                parent._selectedEDSourceNames.push(attrNameTxt);
              }
            } else if (parent._selectedEDSourceNames.indexOf(attrNameTxt)>-1 && annKey in parent.__attrSourceImgData){
              // ---- Remove annotation ----
              parent._selectedEDSourceNames.splice(parent._selectedEDSourceNames.indexOf(attrNameTxt),1);
              ctx.putImageData(parent.__attrSourceImgData[foundAttrData['page']], 0, 0);
              const redrawAnnList = annotations.filter(x=>parent._selectedEDSourceNames.indexOf(x["text"])>-1 && x['page']==annKey);
              for(const annData1 of redrawAnnList){
                const l = annData1['sourceBbox'][0]*sdw;
                const t = annData1['sourceBbox'][1]*sdh;
                const w = annData1['sourceBbox'][2]*sdw;
                const h = annData1['sourceBbox'][3]*sdh;
                ctx.fillText(annData1["text"],l,t-fillTxtMargin, w)
                ctx.strokeRect(l,t,w,h);
              }
              parent._renderPageForEDSourceArray = parent._renderPageForEDSourceArray.filter(x=>{
                const annKey1 = attrNameTxt+"_"+foundAttrData['page']+"_"+foundAttrData['sourceBbox'].join('_');
                const newKey = x['annotation']['text']+"_"+x['annotation']['page']+"_"+x['annotation']['sourceBbox'].join('_');
                return newKey != annKey1;
              });
            }
          }
        }
      }
    }
  }

  getContentAnnotateData(attributeData:AttributeData){
    const attrNameTxt = attributeData.attrNameTxt;
    let annData = [];
    const parent = this;
    if (parent.utilityService.isListHasValue(parent.model.attachmentAnnAttrDataList)){
      const annotations = JSON.parse(parent.model.attachmentAnnAttrDataList[0]["attrValue"]);
      const foundAttrDataList = annotations.filter(x=>x["text"]===attrNameTxt);
      if(parent.utilityService.isListHasValue(foundAttrDataList)){
        annData = foundAttrDataList.filter(y=>y["page"]>0);
      }
    }
    return annData;
  }

  navigateToPDFPage(annotateData, attributeData, isClickEvent){
    if(!isClickEvent){
      const isExist = this._renderPageForEDSourceArray.filter(x=>x["annotation"]["text"]===annotateData["text"] && x["annotation"]["page"]===annotateData["page"] && x["annotation"]["sourceBbox"].join("_")===annotateData["sourceBbox"].join("_"));
      if(isExist.length==0){
        this._renderPageForEDSourceArray.push({"annotation":annotateData, "attribute":attributeData});
      }
    }
    this.dataService.publishAttributeOpData({
      'renderPdfPage': annotateData["page"]
    })
  }

  selectTab(selectedTabName: String){
    const parent = this;
    if (selectedTabName=="ExtractedData"){
      parent.model.isExtractedDataTabSelected = true;
      parent.model.isQnATabSelected =false;
      parent.model.extractedDataTabCssDisplay="block";
      parent.model.qNaCssDisplay="none";
    }
    if (selectedTabName=="QnA"){
      parent.model.isQnATabSelected = true;
      parent.model.isExtractedDataTabSelected = false;
      parent.model.extractedDataTabCssDisplay="none";
      parent.model.qNaCssDisplay="block";
    }

  }
  /************************* IE METHODS ENDS*************************/

  /************************* PRIVATE METHODS *************************/
  private getData(callback) {
    const parent = this;
    parent.model.isDataLoaded = false;
    parent.model.isAttachmentDataLoaded = false;
    parent.model.isAttributesNotified = false;
    parent.model.isTableSorted = false;
    parent.model.attributeDataValList = [];
    parent.attributeDataEditList = [];
    parent.headerLevelEditDataList = [];
    parent.model.reExtractAttributeList = [];
    if (parent.model.documentData != null) {
      const attrNameCdes = '';
      const docId = parent.model.documentData.docId;
      const promiseArray = [];
      promiseArray.push(parent.fetchAttributeList(docId));
      promiseArray.push(parent.fetchAttributeNameValueList(attrNameCdes));
      Promise.all(promiseArray).then(function () {
        if (parent.model.documentData['attachmentCount'] > 0) {
          parent.getAttachmentData(docId).then(() => parent.getAttributesNotification()).then(() => {
            parent.subProcessToGetData();
            callback();
          }).catch(() => {
            parent.subProcessToGetData();
            callback();
          });
        } else {
          parent.model.isAttachmentDataLoaded = true;
          parent.model.attachmentAttrDataList = [];
          parent.getAttributesNotification().then(() => {
            parent.subProcessToGetData();
            callback();
          }).catch(() => {
            parent.subProcessToGetData();
            callback();
          });
        }
      }).catch(err => {
        parent.model.attributeDataList = [];
        parent.model.isDataLoaded = true;
      });
    } else {
      parent.model.isDataLoaded = true;
      callback();
    }
  }

  private subProcessToGetData() {
    this.model.attributeDataList = this.attributeHelper.filteredAttrDataList(this.model.attributeDataList,false);
    this.model.attributeDataList = this.processAttrDataList(this.model.attributeDataList);
    if (this.model.isDocTypeFile && this.isListHasValue(this.model.attachmentAttrDataList)) {
      this.extractedDataHelper.postToFileContentComponent(this.model.attachmentAttrDataList[0].attributes,
        this.model.attributeAttributeMapping);
    }
    this.model.isDataLoaded = true;
  }

  private fetchAttributeList(docId: number) {
    const parent = this;
    return new Promise<void>(function (fulfilled, rejected) {

      parent.attributeService.getDocumentAttributes(docId)
        .then(function (data) {
          parent.model.attributeDataList = parent.createAttributeValidationObject(data as AttributeData[]);
          parent.isDocServiceLoaded = true;
          fulfilled();
        })
        .catch(function (error) {
          rejected();
        });
    });
  }

  private fetchAttributeNameValueList(attrNameCdes: string) {
    const parent = this;
    return new Promise<void>(function (fulfilled, rejected) {
      parent.attributeService.getAttributeNameValues(attrNameCdes)
        .then(function (data) {
          parent.attributeNameValuesList = data as AttributeData[];
          fulfilled();
        })
        .catch(function (error) {
          rejected();
        });
    });
  }

  private getAttributesNotification() {
    if (this.isDocServiceLoaded && this.model.isAttachmentDataLoaded) {
      const parent = this;
      return new Promise<void>(function (fulfilled, rejected) {
        parent.attributeService.getAttributesNotification(parent.model.documentData.docId)
          .then(function (data) {
            parent.attributesNotificationList = data as DocumentData;
            if (!parent.model.isDocTypeFile) {
              const attrDataList = parent.attributesNotificationList.attributes;
              if (parent.utilityService.isListHasValue(attrDataList) &&
                parent.utilityService.isListHasValue(parent.model.attributeDataList)) {
                parent.setAttrNotification(attrDataList, parent.model.attributeDataList);
              }
            }
            const attachmentDataList = parent.attributesNotificationList.attachments;
            if (parent.utilityService.isListHasValue(attachmentDataList) &&
              parent.utilityService.isListHasValue(parent.model.attachmentAttrDataList)) {
              for (let i = 0; i < attachmentDataList.length; i++) {
                const attachmentData: AttachmentData = attachmentDataList[i];
                for (let j = 0; j < parent.model.attachmentAttrDataList.length; j++) {
                  const attachmentAttrData: AttributeData = parent.model.attachmentAttrDataList[j];
                  if (attachmentData.attachmentId === attachmentAttrData.attachmentId) {
                    parent.setAttrNotification(attachmentData.attributes, attachmentAttrData.attributes);
                    break;
                  }
                }
              }
            }
            parent.model.isAttributesNotified = true;
            fulfilled();
          })
          .catch(function () {
            rejected();
          });
      });
    }
  }

  private isPendingActions() {
    const parent = this;
    let isExist = false;
    return new Promise(function (fulfilled, rejected) {
      if (parent.model.documentData != null) {
        parent.actionService.getActions(parent.model.documentData.docId, function (error, data) {
          if (!error) {
            const documentDataList: DocumentData[] = data;
            if (documentDataList.length > 0) {
              const filterList = documentDataList[0].actionDataList.filter(documentData =>
                documentData.taskStatusCde !== CONSTANTS.ACTION_TASK_STATUS_CDE.COMPLETED
                && documentData.taskStatusCde !== CONSTANTS.ACTION_TASK_STATUS_CDE.FAILED);
              if (filterList != null && filterList.length > 0) {
                isExist = true;
              }
            }
          }
          fulfilled(isExist);
        });
      } else {
        fulfilled(isExist);
      }
    });
  }

  private getDismissReason(reason: any): string {
    let dismissReason: string;
    if (reason === ModalDismissReasons.ESC) {
      dismissReason = 'by pressing ESC';
    }
    if (reason === ModalDismissReasons.BACKDROP_CLICK) {
      dismissReason = 'by clicking on a backdrop';
    } else {
      dismissReason = `with: ${reason}`;
    }
    return dismissReason;
  }

  private deleteNormalAttributes(attributeDataList: AttributeData[]) {
    const parent = this;
    return new Promise<void>(function (fulfilled, rejected) {
      const documentData: DocumentData[] = parent.convertAttrDataToDocumentData(attributeDataList);
      if (documentData.length > 0) {
        parent.attributeService.deleteAttributeData(documentData).then(function (_data) {
          fulfilled();
        }).catch(function () {
          rejected();
        });
      }
    });
  }

  private validateTabularSaveToRefreshLHS(event) {
    return (event !== CONSTANTS.EVENT.TABULAR_DATA_SAVE ||
      (event === CONSTANTS.EVENT.TABULAR_DATA_SAVE && this.extractedDataHelper.isUnsavedDataExist(this.model, this.attributeDataEditList,
        this.deleteAttributeList)));
  }

  private applyCustomSort() {
    const parent = this;
    const id = +!this.model.popupButton;
    const niaSortableColumnData = new NiaSortableColumnData('1', 'custom', 'extracted-data-table-' + id, '1',
      this.model.nonTabularSortKeyMap, undefined, 'ED-' + id, undefined, 'attribute-header', '2', true);
    parent.niaSortTableService.applySort(niaSortableColumnData);
    parent.model.isTableSorted = true;
  }

  // To get attachment data from service for respective docId.
  private getAttachmentData(docId: number) {
    const parent = this;
    return new Promise<void>(function (fulfilled, rejected) {
      if (parent._isComponentInitialLoad) {
        // console.log('RHS : loaded from parent attachement');
        // Logic to set attachment sequence number for visualization
        let attachmentDataList = parent._attachmentDataListFromParent; // AttachmentData[]
        let counter = 1
        for (let index = 0; index < attachmentDataList.length; index++) {
          const attachmentId = attachmentDataList[index].attachmentId
          if (parent._attachmentAttrDataListFromParent.filter(a => a.attachmentId === attachmentId).length > 0) {
            attachmentDataList[index].displayNumber = counter++
          }
        }
        parent.model.attachmentDataList = parent._attachmentDataListFromParent;
        parent.getAttachmentAttributeDataList(parent._attachmentAttrDataListFromParent);
        parent._isComponentInitialLoad = false;
        fulfilled();
      } else {
        // console.log('RHS : loaded from child refresh');
        parent.attributeService.getAttachmentAttributes(docId).then(function (data: AttributeData[]) {
          parent.getAttachmentAttributeDataList(data);
          fulfilled();
        }).catch(function (error) {
          parent.model.isAttachmentDataLoaded = true;
          rejected();
        });
      }
    });
  }

  private getAttachmentAttributeDataList(data: AttributeData[]) {
    const parent = this;
    parent.model.attachmentAttrDataList = this.utilityService.createDuplicateList(data);
    if (parent.model.isDocTypeFile) {
      const attachments = this.extractedDataHelper.getMainAttachmentData(
        this.utilityService.createDuplicateList(this.model.attachmentDataList));
      if (this.isListHasValue(attachments)) {
        const attachmentId = parent.attachmentId > 0 ? parent.attachmentId : attachments[0].attachmentId;
        parent.model.attachmentAttrDataList = this.model.attachmentAttrDataList.filter
          (attachAttr => attachAttr.attachmentId === attachmentId);
      }
    }
    parent.model.attachmentAttrWithDocIdDataList=this.utilityService.createDuplicateList(parent.model.attachmentAttrDataList);
    
    parent.model.attachmentAttrDataList.forEach(function (attrData) {
      parent.model.attachmentAnnAttrDataList = attrData.attributes.filter(attributeData =>
        attributeData.attrNameCde === CONSTANTS.ATTR_NAME_CDE.CONTENT_ANNOTATION);
      attrData.attributes = parent.attributeHelper.filteredAttrDataList(attrData.attributes,false);
      attrData.attributes = parent.processAttrDataList(attrData.attributes, true);
      attrData.attributes = parent.createAttributeValidationObject(attrData.attributes);
    });
    parent.model.isAttachmentDataLoaded = true;

    parent.model.attachmentAttrWithDocIdDataList.forEach(function (attrData) {
      attrData.attributes = parent.attributeHelper.filteredAttrDataList(attrData.attributes,true);
      attrData.attributes = parent.processAttrDataList(attrData.attributes, true);
      attrData.attributes = parent.createAttributeValidationObject(attrData.attributes);
    });
  }

  // To process attribute data such as Document type or category type should come first and setting allowed values.
  private processAttrDataList(attributeDataList: AttributeData[], isAttachmentAttrData?: boolean) {
    let tempAttrDataList: AttributeData[] = attributeDataList;
    let tempAttrDataList1: AttributeData[] = [];
    if (this.model.isDocTypeFile || isAttachmentAttrData) {
      tempAttrDataList1 = tempAttrDataList.filter(a => a.attrNameCde === CONSTANTS.ATTR_NAME_CDE.DOCUMENT_TYPE);
      tempAttrDataList = tempAttrDataList.filter(a => a.attrNameCde !== CONSTANTS.ATTR_NAME_CDE.DOCUMENT_TYPE);
    } else {
      tempAttrDataList1 = tempAttrDataList.filter(a => a.attrNameCde === CONSTANTS.ATTR_NAME_CDE.CATEGORY);
      tempAttrDataList = tempAttrDataList.filter(a => a.attrNameCde !== CONSTANTS.ATTR_NAME_CDE.CATEGORY);
    }
    let startIndex = 0;
    if (tempAttrDataList1.length > 0) {
      attributeDataList[0] = tempAttrDataList1[0];
      startIndex = 1;
    }
    for (let i = 0; i < tempAttrDataList.length; i++) {
      attributeDataList[startIndex + i] = tempAttrDataList[i];
    }
    if (attributeDataList.length > 0 && this.attributeNameValuesList.length > 0) {
      for (let i = 0; i < attributeDataList.length; i++) {
        for (let j = 0; j < this.attributeNameValuesList.length; j++) {
          if (attributeDataList[i].attrNameCde === this.attributeNameValuesList[j].attrNameCde) {
            attributeDataList[i].allowedValues = this.attributeNameValuesList[j].allowedValues;
            break;
          } else {
            attributeDataList[i].allowedValues = [];
          }
        }
      }
    }
    return attributeDataList;
  }

  // To get selected attributes into request format for edit/delete operation.
  private convertAttrDataToDocumentData(attributeDataList: any[]): DocumentData[] {
    const attachmentAttrDataEditList: AttachmentData[] = [];
    const attributeDataEditList: AttributeData[] = [];
    const attachmentMap = new Map<number, AttributeData[]>();
    attributeDataList.forEach(function (attrData) {
      const tempAttrData = attrData;
      const attachmentId = tempAttrData.attachmentId;
      if (attachmentId !== undefined && attachmentId > 0) {
        let attributes: AttributeData[] = attachmentMap.get(attachmentId);
        if (attributes === undefined) {
          attributes = [];
        }
        attributes.push(tempAttrData);
        attachmentMap.set(attachmentId, attributes);
      } else {
        attributeDataEditList.push(tempAttrData);
      }
    });
    attachmentMap.forEach((value: AttributeData[], key: number) => {
      attachmentAttrDataEditList.push(new AttachmentData(key, null, 0, '', value, null));
    });
    const documentData: DocumentData[] = [];
    documentData.push(new DocumentData(this.model.documentData.docId, null,
      null, null, null, null, null, null, null, null, null, null, attributeDataEditList, attachmentAttrDataEditList));
    return documentData;
  }

  // To check case editable and allow user to perform Add/Edit/Delete.
  private isCaseEditable() {
    if (this.model.documentData != null) {
      const parent = this;
      const promise = parent.utilityService.isCaseEditable(parent.model.documentData);
      return promise.then(function (value) {
        parent.model.isCaseEditAllowed = value;
      });
    }
  }

  // Assign notification for each attributes
  private setAttrNotification(attrDataList: AttributeData[], attributeDataList: AttributeData[]) {
    for (let i = 0; i < attrDataList.length; i++) {
      const attrData: AttributeData = attrDataList[i];
      for (let j = 0; j < attributeDataList.length; j++) {
        const attributeData: AttributeData = attributeDataList[j];
        if (attrData.id === attributeData.id) {
          if (attributeData.attributes !== null) {
            this.setAttrNotification(attrData.attributes, attributeData.attributes);
          } else if (attrData.notification !== null) {
            attributeData.notification = attrData.notification;
          } else {
            attributeData.notification = '';
          }
          break;
        }
      }
    }
  }

  // To check whether selected attribute data is allowed for edit operation.
  private updateConfPctExtractTypeCde(attributeDataEditList: AttributeData[]): void {
    for (let i = 0; i < attributeDataEditList.length; i++) {
      attributeDataEditList[i].confidencePct = CONSTANTS.ATTRIBUTES.CONFIDENCE_PCT.MAX;
      attributeDataEditList[i].extractTypeCde = CONSTANTS.ATTRIBUTES.EXTRACT_TYPE_CDE.MANUALLY_CORRECTED;
    }
  }

  /* To create delete attribute list with 3 types of list
      1. Normal attributes(consist of both Email and Attachment level).
      2. Email level multi attributes.
      3. Attachment level multi attributes. */
  private generateModifiedAttributeList(opType) {
    const tempEmailAttributeDataList: AttributeData[] = this.utilityService.createDuplicateList(this.model.attributeDataList);
    const tempAttachmentAttributeDataList: AttributeData[] = this.utilityService.createDuplicateList(this.model.attachmentAttrDataList);
    const emailAttributeList = this.filterModifiedAttributeList(tempEmailAttributeDataList, opType);
    let normalAttributeDeleteList: AttributeData[] = emailAttributeList[0];
    const emailMultiAttributeDeleteList: AttributeData[] = emailAttributeList[1];
    const attachmentMultiAttributeList = [];
    tempAttachmentAttributeDataList.forEach(attachmentData => {
      const attachmentAttributeList = this.filterModifiedAttributeList(
        this.utilityService.createDuplicateList(attachmentData.attributes), opType);
      normalAttributeDeleteList = normalAttributeDeleteList.concat(attachmentAttributeList[0]);
      if (this.isListHasValue(attachmentAttributeList[1])) {
        attachmentMultiAttributeList.push(attachmentAttributeList[1]);
      } else {
        attachmentMultiAttributeList.push([]);
      }
    });
    const deleteAttributeList = [normalAttributeDeleteList, emailMultiAttributeDeleteList, attachmentMultiAttributeList];
    return deleteAttributeList;
  }

  // To filter normal and multi attributes for delete operation.
  private filterModifiedAttributeList(attrDataList: AttributeData[], opType) {
    const normalAttibuteList: AttributeData[] = attrDataList.filter(attrData => {
      if (opType === CONSTANTS.OPERATION_TYPE.DELETE) {
        return attrData.deleteClicked;
      } else {
        return attrData.isAttrValueChanged;
      }
    });
    const multiAttibuteList: AttributeData[] = [];
    attrDataList.forEach(attrData => {
      if (this.isMultiAttribute(attrData.attrNameCde)) {
        attrData.attributes = attrData.attributes.filter(subAttrData => {
          if (opType === CONSTANTS.OPERATION_TYPE.DELETE) {
            return subAttrData.deleteClicked;
          } else {
            return subAttrData.isAttrValueChanged;
          }
        });
        if (this.isListHasValue(attrData.attributes)) {
          multiAttibuteList.push(attrData);
        }
      }
    });
    const attributes = [normalAttibuteList, multiAttibuteList];
    return attributes;
  }

  // To perform add attributes both normal and multi attributes.
  private addAttributes() {
    const parent = this;
    return new Promise<void>(function (fulfilled, rejected) {

      let attachmentData: any;
      let attributes = [];
      const isManualAddAttributeAvailable = parent.isListHasValue(parent.model.addAttributeDataList);
      if (isManualAddAttributeAvailable &&
        !parent.checkValidManualAddAttribute(parent.model.addAttributeDataList[parent.model.addAttributeDataList.length - 1])) {
        rejected();
        return;
      }
      if (isManualAddAttributeAvailable) {
        attributes = parent.groupMultiAttributeDataList();
      }
      if (parent.isListHasValue(parent.model.annotatedAttributeDataList)) {
        if (parent.isListHasValue(attributes)) {
          attributes = attributes.concat(parent.model.annotatedAttributeDataList);
        } else {
          attributes = parent.model.annotatedAttributeDataList;
        }
      }
      // To get repeated normal Attribute count.
      const repeatedAttrNameCde = attributes.reduce((a, e) => {
        a[e.attrNameCde] = ++a[e.attrNameCde] || 0;
        return a;
      }, {});
      delete repeatedAttrNameCde[44]; // To remove multi attr repeated.
      let isRepeatedAttrNameCdeAvailable = false;
      for (const value of Object.values(repeatedAttrNameCde)) {
        if (value > 0) {
          isRepeatedAttrNameCdeAvailable = true;
          break;
        }
      }
      if (isRepeatedAttrNameCdeAvailable) {
        parent.toastr.error(parent.msgInfo.getMessage(155));
        rejected();
        return;
      }
      if (parent.isListHasValue(attributes)) {
        if (parent.model.isDocTypeFile) {
          const attachmentList = parent.extractedDataHelper.getMainAttachmentData(parent.model.attachmentDataList);
          parent.model.selectedAttachmentId = attachmentList[0].attachmentId;
        }
        if (parent.model.selectedAttachmentId > 0) {
          attachmentData = [{
            'attachmentId': parent.model.selectedAttachmentId,
            'attributes': attributes
          }];
          attributes = [];
        }
      }
      const requestData = [{
        'docId': parent.model.documentData.docId,
        'attributes': attributes,
        'attachments': attachmentData
      }];
      parent.attributeService.addAttributeData(requestData).then(function (data) {
        if (data['responseCde'] === CONSTANTS.ERROR_CDE.MULTI_ATTRIBUTE_ALREADY_EXIST) {
          rejected();
        } else {
          fulfilled();
        }
      }).catch(function (error) {
        rejected();
      });
    });
  }

  // To perform edit operation for normal attributes.
  private editAttributes(attrDataList) {
    const parent = this;
    return new Promise<void>(function (fulfilled, rejected) {
      parent.updateConfPctExtractTypeCde(attrDataList);
      const documentData: DocumentData[] = parent.convertAttrDataToDocumentData(attrDataList);
      parent.attributeService.editAttributeData(documentData).then(function (_data) {
        fulfilled();
      }).catch(function (error) {
        rejected();
      });

    });
  }

  // To perform edit/delete multi attribute service call.
  private manageMultiAttributes(deleteAttributeDataList: AttributeData[],
    editAttributeDataList: AttributeData[], isEmailAttribute: boolean) {
    const promiseAll = [];
    const parent = this;
    const attrMap = parent.getEditAndDeleteMappedAttributes(editAttributeDataList, deleteAttributeDataList);
    // const keys = attrMap.keys();
    attrMap.forEach((value: any, key: number) => {
      let delList = [];
      let editList = [];
      const attributes = attrMap.get(key);
      delList = attributes['delList'];
      editList = attributes['editList'];
      let delAttrData = null;
      let editAttrData = null;
      if (parent.isListHasValue(delList)) {
        delAttrData = deleteAttributeDataList.filter(delAttr => delAttr.id === key)[0];
        delAttrData.attributes = delList;
      }
      if (parent.isListHasValue(editList)) {
        parent.updateConfPctExtractTypeCde(editList);
        editAttrData = editAttributeDataList.filter(editAttr => editAttr.id === key)[0];
        editAttrData.attributes = editList;
      }
      const requestData = this.generateMultiAttributeRequest(delAttrData, editAttrData, isEmailAttribute);
      promiseAll.push(new Promise<void>(function (fulfilled, rejected) {
        if (requestData.attribute != null || requestData.attachment != null) {
          parent.attributeService.manageAttributeData(requestData).then(function (_data) {
            fulfilled();
          }).catch(function (_error) {
            rejected();
          });
        } else {
          fulfilled();
        }
      }));
    });
    return promiseAll;
  }

  private getEditAndDeleteMappedAttributes(editAttributeList: AttributeData[], deleteAttributeList: AttributeData[]) {
    const attrOpMap = new Map<number, any>();
    this.addValuesToMap(editAttributeList, attrOpMap, CONSTANTS.OPERATION_TYPE.EDIT);
    this.addValuesToMap(deleteAttributeList, attrOpMap, CONSTANTS.OPERATION_TYPE.DELETE);
    return attrOpMap;
  }

  private addValuesToMap(attributeList: AttributeData[], attrOpMap: Map<number, any>, opType) {
    let attributes = { 'editList': [], 'delList': [] };
    attributeList.forEach(attr => {
      attributes = { 'editList': [], 'delList': [] };
      if (attrOpMap.has(attr.id)) {
        attributes = attrOpMap.get(attr.id);
        if (opType === CONSTANTS.OPERATION_TYPE.DELETE) {
          attributes.delList = attributes.delList.concat(attr.attributes);
        } else {
          attributes.editList = attributes.editList.concat(attr.attributes);
        }
        attrOpMap.set(attr.id, attributes);
      } else {
        if (opType === CONSTANTS.OPERATION_TYPE.DELETE) {
          attributes.delList = attr.attributes;
        } else {
          attributes.editList = attr.attributes;
        }
        attrOpMap.set(attr.id, attributes);
      }
    });
  }

  // To create request object for multi attribute api.
  private generateMultiAttributeRequest(deleteAttributeData: AttributeData, editAttributeData: AttributeData, isEmailAttribute: boolean) {
    let attribute = null;
    let attachment = null;
    let id;
    let attrNameCde;
    let attrNameTxt;
    let extractTypeCde;
    let attachmentId;
    const deleteAttributes = [];
    const editAttributes = [];
    if (deleteAttributeData !== null && this.isListHasValue(deleteAttributeData.attributes)) {
      id = deleteAttributeData.id;
      attrNameCde = deleteAttributeData.attrNameCde;
      attrNameTxt = deleteAttributeData.attrNameTxt;
      extractTypeCde = deleteAttributeData.extractTypeCde;
      attachmentId = deleteAttributeData.attachmentId;
      deleteAttributeData.attributes.forEach(attrData => deleteAttributes.push({ 'id': attrData.id }));
    }
    if (editAttributeData !== null && this.isListHasValue(editAttributeData.attributes)) {
      id = editAttributeData.id;
      attrNameCde = editAttributeData.attrNameCde;
      attrNameTxt = editAttributeData.attrNameTxt;
      extractTypeCde = editAttributeData.extractTypeCde;
      attachmentId = editAttributeData.attachmentId;
      editAttributeData.attributes.forEach(attrData =>
        editAttributes.push({
          'attrValue': attrData.attrValue, 'id': attrData.id, 'attributes': null,
          'attrNameTxt': attrData.attrNameTxt, 'confidencePct': attrData.confidencePct
        }));
    }
    attribute = {
      'id': id,
      'attrNameCde': attrNameCde,
      'attrNameTxt': attrNameTxt,
      'extractTypeCde': extractTypeCde,
      'addAttributes': [],
      'editAttributes': editAttributes,
      'deleteAttributes': deleteAttributes
    };
    if (!isEmailAttribute) {
      attachment = {
        'attachmentId': attachmentId,
        'attribute': attribute
      };
      attribute = null;
    }
    const requestData = {
      'docId': this.model.documentData.docId,
      'attribute': attribute,
      'attachment': attachment
    };
    return requestData;
  }

  // Validation for new Attributes
  private checkValidManualAddAttribute(attrData: AttributeData) {
    let isValid = true;
    // Value will be only null if we give cancel while adding new row.
    if (!(this.model.isAddClicked || this.model.isAddAttachmentAttrClicked)) {
      return isValid;
    }

    // If user didn't select any attribute name txt in dropdown(If no normal attribute to add).
    if (attrData.attrNameCde === null) {
      this.toastr.error(this.msgInfo.getMessage(163));
      isValid = false;
      return isValid;
    }

    // To avoid last added data mismatch after click on save button.
    if (!this.model.isSaveAllowed) {
      const selectedAttrNameCde = attrData.attrNameCde;
      if (!this.isMultiAttribute(selectedAttrNameCde)) {
        this.model.attributeDataValList = this.model.attributeDataValList.filter(attributeData =>
          attributeData.attrNameCde !== +selectedAttrNameCde);
      }
      if (this.isListHasValue(this.model.attributeDataValList)) {
        this.selectedAttrNameCde = undefined;
        if (this.model.attributeDataValList.length > 1) {
          this.selectedAttrNameCde = this.model.attributeDataValList[0].attrNameCde;
        }
      }
    }
    delete attrData['isLastAddedAttribute'];
    return isValid;
  }

  // To group added multi attributes
  private groupMultiAttributeDataList(): AttributeData[] {
    let attributeDataList = this.utilityService.createDuplicateList(this.model.addAttributeDataList);
    const subAttributes = attributeDataList.filter(attrData => this.isMultiAttribute(attrData.attrNameCde));
    attributeDataList = attributeDataList.filter(attrData => !this.isMultiAttribute(attrData.attrNameCde));
    subAttributes.forEach(attrData => attrData.attrNameCde = null);
    if (this.isListHasValue(subAttributes)) {
      const attribute = {
        'attrNameCde': CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE,
        'confidencePct': CONSTANTS.ATTRIBUTES.CONFIDENCE_PCT.UNDEFINED,
        'extractTypeCde': CONSTANTS.ATTRIBUTES.EXTRACT_TYPE_CDE.DIRECT_COPY,
        'attributes': subAttributes
      };
      attributeDataList.push(attribute);
    }
    return attributeDataList;
  }

  // To check id referene error in given object
  private checkErrorExist(obj) {
    let validationResult;
    if (obj !== undefined && obj.invalid) {
      validationResult = obj.errors;
    }
    return validationResult;
  }

  // To create validation object to show error for db datas.
  private createAttributeValidationObject(attrDataList: AttributeData[]) {
    // Set validation inner parameter as undefined because of valid obj comes from db;
    attrDataList.forEach(attrData => {
      if (this.isMultiAttribute(attrData.attrNameCde)) {
        attrData.attributes = this.createAttributeValidationObject(attrData.attributes);
      } else {
        attrData.attrNameValidation = new AttributeValidationData(undefined, undefined);
      }
    });
    return attrDataList;
  }

  // To alert the user either clicks on cancel or pop up button or refresh button
  private alertUser(id = 159) {
    let isRefreshRequired = true;
    if (this.extractedDataHelper.isUnsavedDataExist(this.model, this.attributeDataEditList, this.deleteAttributeList)
      || this.model.isSaveAllowed) {
      isRefreshRequired = confirm(this.msgInfo.getMessage(id));
    }
    return isRefreshRequired;
  }

  private notifyEDState() {
    if (this.model.isDocTypeFile && !this._isAnnotationNotSupported && !this._isEDOnAddMode) {
      // Prevent it from duplicate publish.
      this._isEDOnAddMode = true;
      this.dataService.publishExtractedDataCustomEvent(CONSTANTS.CUSTOM_EVENT.EXTRACTED_DATA.ADD_CLICKED);
    }
  }
  private getAttributesSortOrderKeyFromDB() {
    const parent = this;
    parent.attributeService.getAttributeSortkeyData(CONSTANTS.CACHE_ENTITY.ATTR_SORTKEY).then(
      (attributeSortKeyData: AttributeSortKeyData[]) => {
        if (parent.utilityService.isListHasValue(attributeSortKeyData)) {
          attributeSortKeyData.forEach(attr => {
            attr.attrNameValues.forEach(attrNameValue => {
              attrNameValue.attributes.forEach(attribute => {
                const mapKey = attr.attrNameCde + '_' + attrNameValue.attrValue;
                if (attribute.nonTabular) {
                  parent.model.nonTabularSortKeyMap[mapKey.toLowerCase()] = attribute.nonTabular.map(s =>
                    s.attrNameTxt.trim().toLowerCase());
                }
                if (attribute.tabular) {
                  parent.getTabularColOrderMap(mapKey, attribute);
                }
              });
            });
          });
        }
      });
  }

  private getTabularColOrderMap(mapKey, attribute) {
    attribute.tabular.forEach(tabAttr => {
      const regExpArray = [];
      tabAttr.orderColumnUsingAnyOfRegExp.forEach(regExp => {
        if (regExp.flag) {
          regExpArray.push(new RegExp(regExp.pattern, regExp.flag));
        } else {
          regExpArray.push(new RegExp(regExp.pattern));
        }
      });
      const colOrderData = {
        'colOrderRegExp': regExpArray,
        'colNames': tabAttr.attributes.map(s => s.attrNameTxt.trim())
      };
      if (this.model.tabularColOrderKeyMap[mapKey]) {
        this.model.tabularColOrderKeyMap[mapKey].push(colOrderData);
      } else {
        this.model.tabularColOrderKeyMap[mapKey] = [colOrderData];
      }
    });
  }

  // To send private variables to other component using EDData Obj.
  private createEDDataObj() {
    return new EDData(this.attributeDataEditList, this.deleteAttributeList,
      this.isReextractionPendingExist, this.attributeList, this.selectedAttrNameCde, this.selectedAttrNameTxtList,
      this.attributeValidationResult);
  }

  // To assign private variable value from other component using EDData Obj.
  private restoreDataFromEDDataObj(edDataObj: EDData) {
    this.attributeDataEditList = edDataObj.attributeDataEditList;
    this.deleteAttributeList = edDataObj.deleteAttributeList;
    this.isReextractionPendingExist = edDataObj.isReextractionPendingExist;
    this.attributeList = edDataObj.attributeList;
    this.selectedAttrNameCde = edDataObj.selectedAttrNameCde;
    this.selectedAttrNameTxtList = edDataObj.selectedAttrNameTxtList;
    this.attributeValidationResult = edDataObj.attributeValidationResult;
  }

  // To check whether attribute changed if changed then set hidden property value has true to show in UI.
  private isAttributeChanged(attrData: AttributeData) {
    attrData.isAttrValueChanged = false;
    const originalValue = attrData['attrValueOrg'];
    if (attrData['attrId']) {
      attrData.isAttrValueChanged = true;
    }
    // Don't use helper method for originalValue check, it may have empty space or null.
    if (originalValue !== undefined) {
      // To avoid duplicates in list for changing same attribute twice
      this.attributeDataEditList = this.attributeHelper.removeAttribute(attrData, this.attributeDataEditList);
      const attribute = JSON.parse(JSON.stringify(attrData));
      this.extractedDataHelper.trimValue(attribute);
      if (originalValue !== attribute.attrValue) {
        attrData.isAttrValueChanged = true;
        this.attributeDataEditList.push(attrData);
      }
    }
  }

  private checksourceBbox(attrNameTxt:string){
    const parent = this;
    let issourceBboxFound = false;
    if (parent.utilityService.isListHasValue(parent.model.attachmentAnnAttrDataList)){
      const annotations = JSON.parse(parent.model.attachmentAnnAttrDataList[0]["attrValue"]);
      const foundAttrDataList = annotations.filter(x=>x["text"]===attrNameTxt);
      if(parent.utilityService.isListHasValue(foundAttrDataList)){
        for(const foundAttrData of foundAttrDataList){
          if(parent.utilityService.isListHasValue(foundAttrData['sourceBbox'])){
            issourceBboxFound = true;
            break;
          }
        }
      }
    }
    return issourceBboxFound;
  }

  private onPDFPageRenderedManageSelectedAttribute(data){
    console.log("onPDFPageRenderedManageSelectedAttribute",data);
    const parent = this;
    let removeItems=[];
    for(let i=0; i<parent._renderPageForEDSourceArray.length;i++){
      const pageData=parent._renderPageForEDSourceArray[i]
      if(pageData['annotation']['page']===data['pageNumber']){
        parent.manageSelectedAttribute(pageData['attribute'], false);
        removeItems.push(i);
      }
    }
    for(const idx of removeItems){
      parent._renderPageForEDSourceArray.splice(idx,1);
    }
  }

  private onPDFZoomManageSelectedAttribute(){
    console.log("onPDFZoomManageSelectedAttribute");
    const parent = this;
    const previousSelectedNames = JSON.parse(JSON.stringify(parent._selectedEDSourceNames));
    parent.resetEDSourceAnnotateData();
    for(const attrName of previousSelectedNames){
      parent.annotateAttrNameSource(attrName, true);
    }
  }

  private onEDRefresh(){
    console.log("onEDRefresh");
    const parent = this;
    const previousSelectedNames = JSON.parse(JSON.stringify(parent._selectedEDSourceNames));
    for(const attrName of previousSelectedNames){
      parent.annotateAttrNameSource(attrName, false);
    }
    parent.resetEDSourceAnnotateData();
  }

  private resetEDSourceAnnotateData(){
    const parent = this;
    parent.__attrSourceImgData = {};
    parent._selectedEDSourceNames = [];
  }

  /************************* IE PRIVATE METHODS STARTS*************************/
  private getTableId() {
    let index = 1;
    if (this.model.popupButton) {
      index = 0;
    }
    return '#extracted-data-table-' + index;
  }
  /************************* IE PRIVATE METHODS ENDS*************************/

}
