/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Component, OnInit, Input, Output, EventEmitter, SimpleChange, OnChanges } from '@angular/core';
import { ToastrService } from 'ngx-toastr';
import { ActionData } from '../../data/action-data';
import { DocumentData } from '../../data/document-data';
import { ActionService } from '../../service/action.service';
import { AttributeService } from '../../service/attribute.service';
import { DataService } from '../../service/data.service';
import { MessageInfo } from '../../utils/message-info';
import { AttributeData } from '../../data/attribute-data';
import { ReExtractedAttributeData } from '../../data/re-extracted-attribute-data';
import { ReExtractedData } from '../../data/re-extracted-data';
import { ReExtractedAttachmentData } from '../../data/re-extracted-attachment-data';
import { AttachmentService } from '../../service/attachment.service';
import { AttachmentData } from '../../data/attachment-data';
import { CONSTANTS } from '../../common/constants';
import { ManageAttributeDataList } from '../../data/manage-attribute-data-list';
import { ManageAttachmentAttributeDataList } from '../../data/manage-attachment-attribute-data-list';
import { ManageAttributeData } from '../../data/manage-attribute-data';
import { NgbModal, ModalDismissReasons, NgbModalOptions } from '@ng-bootstrap/ng-bootstrap';
import { AttributeHelper } from '../../utils/attribute-helper';
import { NiaSortableColumnData } from '../nia-sortable-column/nia-sortable-column-data';
import { NiaSortableColumnService } from '../nia-sortable-column/nia-sortable-column.service';
import { UtilityService } from './../../service/utility.service';
import { BaseComponent } from '../../base.component';
import { SessionService } from '../../service/session.service';
import { ConfigDataHelper } from '../../utils/config-data-helper';
import { NiaTelemetryService } from '../../service/nia-telemetry.service';
@Component({
  selector: 'app-re-extract-review',
  templateUrl: './re-extract-review.component.html',
  styleUrls: ['./re-extract-review.component.scss']
})
export class ReExtractReviewComponent extends BaseComponent implements OnInit, OnChanges {
  getClassName(): string {
    return "ReExtractReviewComponent";
  }
  constructor(private actionService: ActionService, private attributeService: AttributeService,
    private dataService: DataService, private attachmentService: AttachmentService,
    private toastr: ToastrService, private msgInfo: MessageInfo, private modalService: NgbModal,
    private attributeHelper: AttributeHelper, private niaSortableColumnService: NiaSortableColumnService,
    private utilityService: UtilityService, public sessionService: SessionService, public configDataHelper: ConfigDataHelper, public niaTelemetryService:NiaTelemetryService) {
      super(sessionService, configDataHelper, niaTelemetryService);
    }

  model: any = {
    isDocTypeFile: Boolean,
    isDataLoaded: Boolean,
    isDataSaved: true,
    isCompletedStatus: Boolean,
    requestAttributes: ReExtractedData,
    isNodata: false,
    isSaveClicked: false,
    isPartialSave: false,
    currentJsonValue: String,
    previousJsonValue: String
  }; // For binding to view

  @Input() minheight: number;
  @Output() close = new EventEmitter<string>();
  @Input() closeButton: boolean;
  @Input() reExtractedActionData: ActionData;
  @Input()
  set isDocTypeFile(isDocTypeFile: boolean) {
    this.model.isDocTypeFile = isDocTypeFile;
  }
  @Input()
  set isDataReady(isDataReady: boolean) {
    this._isDataReady = isDataReady;
  }
  @Input()
  set document(docData: DocumentData) {
    this.documentData = docData;
  }
  @Input() isEnableAnnotation: boolean;

  private _isDataReady: boolean;
  private documentData: DocumentData;
  private comparedAttachmentId: number[] = [];
  private responseAttributes: ReExtractedData;
  private uncheckedAttributes: string[] = [];
  private closeResult: string;
  private attachmentAttrDataList: AttributeData[] = [];
  private attrDbDataList: AttributeData[] = [];

  // For disabling mouse click and keystrokes outside modal window
  ngbModalOptions: NgbModalOptions = {
    backdrop: 'static',
    keyboard: false
  };

  ngOnChanges(changes: { [propKey: string]: SimpleChange }) {
    const parent = this;
    if (changes['document']) {
      if (parent._isDataReady) {
        parent.model.isDataLoaded = false;
        parent.model.isCompletedStatus = false;
        parent.model.isSaveClicked = false;
        parent.model.isPartialSave = false;
        if (+parent.reExtractedActionData.taskStatusCde === CONSTANTS.ACTION_TASK_STATUS_CDE.COMPLETED) {
          // This block for read only popup
          parent.model.isCompletedStatus = true;
          parent.createCNAttributesFromSnapShot();
          parent.model.isDataLoaded = true;
        } else {
          const promiseAll = [parent.attributeService.getDocumentAttributes(parent.documentData.docId),
          parent.attributeService.getAttachmentAttributes(parent.documentData.docId)];
          Promise.all(promiseAll).then(function (data) {
            if (parent.utilityService.isAValidValue(data[0])) {
              parent.attrDbDataList = data[0] as AttributeData[];
            }
            if (parent.utilityService.isAValidValue(data[1])) {
              parent.attachmentAttrDataList = data[1] as AttributeData[];
            }

            parent.createCurrentAndNewAttributes();

            parent.isReExtracted();
            parent.applyASCTableSort();
            parent.model.isDataLoaded = true;
          }, error => {
            parent.model.isDataLoaded = true;
          });

        }
      }
    }
  }

  getTitle(attributeData: any) {
    return this.utilityService.getTitle(attributeData);
  }

  private applyASCTableSort() {
    const parent = this;
    const niaSortableColumnData = new NiaSortableColumnData('1', 'asc', 're-extract-result-0', undefined, undefined, undefined, 'RER-0',
      undefined, 'attribute-header', '1', true);
    parent.niaSortableColumnService.applySort(niaSortableColumnData);
  }

  /**Used for combining the LHS and RHS attributs in single Object to populate in view */
  private createCurrentAndNewAttributes() {
    const parent = this;
    // Frame the request and response attributes from action result and mapping param.
    parent.getReqResAttributes();
    // Document Level Comparison
    const requestAttributes = parent.model.requestAttributes.attributes;
    const responseAttributes = parent.responseAttributes.attributes;
    if (requestAttributes != null && responseAttributes != null) {
      const savedAttrNames = parent.getAttrNamesFromList(
        new ReExtractedData(0, this.reExtractedActionData.docActionRelId, [], [], null), 0, [], parent.attrDbDataList);
      parent.compareAttributes(requestAttributes, responseAttributes, savedAttrNames);
    }
    // Attachment Level Comparison
    const requestAttachmentAttributes = parent.model.requestAttributes.attachments;
    const responseAttachmentAttributes = parent.responseAttributes.attachments;
    parent.compareAttachementAttributes(requestAttachmentAttributes, responseAttachmentAttributes);
    if (parent.model.requestAttributes.attachments.length > 0) {
      // After Comparison, update attachment name to respective attachment id
      parent.updateAttachmentName(parent.documentData.docId, parent.model.requestAttributes.attachments);
      parent.model.requestAttributes.attachments.forEach(element => {
        parent.manageDuplicateMultiAttrAction(element.attributes);
      });
    }
    console.log('after Comparison');
    if (+parent.reExtractedActionData.taskStatusCde !== CONSTANTS.ACTION_TASK_STATUS_CDE.COMPLETED) {
      parent.populateUserDataToAttibuteData(parent.model.requestAttributes.attributes, parent.attrDbDataList);
      parent.populateUserDataToAttaAttibuteData(parent.model.requestAttributes.attachments);
    }
    console.log(parent.model.requestAttributes);
  }

  private populateUserDataToAttibuteData(requestAttributes: ReExtractedAttributeData[], attrDbDataList: AttributeData[]) {
    if (requestAttributes) {
      requestAttributes.forEach((lhsAttr: ReExtractedAttributeData) => {
        if (lhsAttr.attrNameCde === CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE && lhsAttr.attributes) {
          return lhsAttr.attributes.forEach((lhsSubAttr: ReExtractedAttributeData) => {
            return attrDbDataList.some(dbAttr => {
              if (dbAttr.attrNameCde === CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE && dbAttr.attributes) {
                return dbAttr.attributes.some((dbSubAttr: any) => {
                  if (lhsSubAttr.attrNameTxt === dbSubAttr.attrNameTxt) {
                    lhsSubAttr.createByUserTypeCde = dbSubAttr.createByUserTypeCde;
                    lhsSubAttr.createByUserFullName = dbSubAttr.createByUserFullName;
                    lhsSubAttr.createByUserLoginId = dbSubAttr.createByUserLoginId;
                    lhsSubAttr.lastModByUserFullName = dbSubAttr.lastModByUserFullName;
                    lhsSubAttr.lastModByUserLoginId = dbSubAttr.lastModByUserLoginId;
                    lhsSubAttr.lastModByUserTypeCde = dbSubAttr.lastModByUserTypeCde;
                    return true;
                  }
                });
              }
            });
          });
        } else {
          return attrDbDataList.some((dbAttr: any) => {
            if (dbAttr.attrNameCde !== CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE
              && lhsAttr.attrNameCde === dbAttr.attrNameCde && dbAttr.attrNameTxt === lhsAttr.attrNameTxt) {
              lhsAttr.createByUserTypeCde = dbAttr.createByUserTypeCde;
              lhsAttr.createByUserFullName = dbAttr.createByUserFullName;
              lhsAttr.createByUserLoginId = dbAttr.createByUserLoginId;
              lhsAttr.lastModByUserFullName = dbAttr.lastModByUserFullName;
              lhsAttr.lastModByUserLoginId = dbAttr.lastModByUserLoginId;
              lhsAttr.lastModByUserTypeCde = dbAttr.lastModByUserTypeCde;
              return true;
            }
          });
        }
        console.log('requestAttributes userdata populated');
        console.log(requestAttributes);
      });
      console.log('requestAttributes');
      console.log(requestAttributes);
    }

  }

  private populateUserDataToAttaAttibuteData(requestAttachments: ReExtractedAttachmentData[]) {
    const parent = this;
    if (requestAttachments) {
      requestAttachments.forEach(reqAttachment => {
        const attachAttributes = parent.attachmentAttrDataList.filter(dbAttach => dbAttach.attachmentId === reqAttachment.attachmentId);
        if (attachAttributes && attachAttributes.length > 0) {
          parent.populateUserDataToAttibuteData(reqAttachment.attributes, attachAttributes[0].attributes);
        }
      });
    }
  }

  getRHSAttribute(attrNameCde: number, lhsAttribute: ReExtractedAttributeData, rhsAttachAttributes: ReExtractedAttributeData[]) {
    let rhsAttribute = null;
    if (lhsAttribute.isDeleted) {
      rhsAttachAttributes.forEach((rhsAttr: ReExtractedAttributeData) => {
        if (attrNameCde === CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE &&
          rhsAttr.attrNameCde === CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE_TABLE
          && rhsAttr.isAdded && lhsAttribute.groupingNameTxtInUI === rhsAttr.groupingNameTxtInUI) {
          rhsAttribute = rhsAttr;
        } else if (attrNameCde === CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE_TABLE &&
          rhsAttr.attrNameCde === CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE
          && rhsAttr.attributes) {
          rhsAttr.attributes.forEach((rhsSubAttr: ReExtractedAttributeData) => {
            if (rhsSubAttr.isAdded && lhsAttribute.groupingNameTxtInUI === rhsSubAttr.groupingNameTxtInUI) {
              rhsAttribute = rhsSubAttr;
            }
          });
        }
      });
    }
    return rhsAttribute;
  }

  private manageDuplicateMultiAttrAction(requestAttributes: any) {
    requestAttributes.forEach((rhsAttr: ReExtractedAttributeData) => {
      if (rhsAttr.attrNameCde === CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE_TABLE && rhsAttr.isAdded) {
        requestAttributes.forEach((lhsAttr: ReExtractedAttributeData) => {
          if (lhsAttr.attrNameCde === CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE && lhsAttr.attributes) {
            lhsAttr.attributes.forEach((lhsSubAttr: ReExtractedAttributeData) => {
              if (lhsSubAttr.isDeleted && lhsSubAttr.attrNameTxt === rhsAttr.attrNameTxt) {
                lhsSubAttr.groupingNameTxtInUI = 'grp_' + lhsSubAttr.attrNameTxt;
                rhsAttr.groupingNameTxtInUI = 'grp_' + lhsSubAttr.attrNameTxt;
                rhsAttr.isHiddenInUI = true;
              }
            });
          }
        });
      } else if (rhsAttr.attrNameCde === CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE && rhsAttr.attributes) {
        rhsAttr.attributes.forEach((rhsSubAttr: ReExtractedAttributeData) => {
          if (rhsSubAttr.isAdded) {
            requestAttributes.forEach((lhsAttr: ReExtractedAttributeData) => {
              if (lhsAttr.isDeleted && lhsAttr.attrNameCde === CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE_TABLE &&
                lhsAttr.attrNameTxt === rhsSubAttr.attrNameTxt) {
                lhsAttr.groupingNameTxtInUI = 'grp_' + lhsAttr.attrNameTxt;
                rhsSubAttr.groupingNameTxtInUI = 'grp_' + lhsAttr.attrNameTxt;
                rhsSubAttr.isHiddenInUI = true;
              }
            });
          }
        });
      }
    });
  }

  private isReExtracted() {
    const parent = this;
    const data = parent.model.requestAttributes;
    let isEmail = true;
    let isDoc = true;
    if (data.attributes != null && data.attributes.length > 0) {
      isEmail = false;
    }
    if (data.attachments != null && data.attachments.length > 0) {
      isDoc = false;
      data.attachments.forEach(att => {
        if (!(att != null && att.attributes.length > 0)) {
          isDoc = true;
        } else {
          isDoc = false;
        }
      });
    }
    if (isEmail && isDoc) {
      parent.model.isNodata = true;
    }
  }

  /**
   * Used for retriving current and new attributes value from action resulta and action mapping list
   */
  private getReqResAttributes() {
    const parent = this;
    parent.reExtractedActionData.paramList.forEach(pValues => {
      parent.model.requestAttributes = JSON.parse(pValues.paramValue).document as ReExtractedData;
    });
    const actionResultJObj = JSON.parse(parent.reExtractedActionData.actionResult);
    parent.responseAttributes = actionResultJObj.document as ReExtractedData;
  }

  /**
   * Used for retriving current and new attributes value from snapshot
   */
  private createCNAttributesFromSnapShot() {
    const parent = this;
    parent.model.requestAttributes = JSON.parse(parent.reExtractedActionData.snapShot) as ReExtractedData;
  }

  /**Used for comparing new and current attributes in documents level */
  private compareAttributes(requestAttributes: ReExtractedAttributeData[], responseAttributes: ReExtractedAttributeData[],
    attrNames?: String[]) {
    const parent = this;
    parent.compareIsAttributesUpdated(requestAttributes, responseAttributes);
    parent.compareIsAttributesDeleted(requestAttributes, responseAttributes);
    parent.compareIsAttributesAdded(requestAttributes, responseAttributes, attrNames);
  }

  /**Used for comparing new and current attributes added, updated or deleted in attachement level */
  private compareAttachementAttributes(requestAttachments: ReExtractedAttachmentData[], responseAttachments: ReExtractedAttachmentData[]) {
    const parent = this;
    if (responseAttachments == null || responseAttachments.length === 0) {
      requestAttachments.forEach(reqAttachment => {
        const req = reqAttachment.attributes;
        parent.compareIsAttributesDeleted(req, null);
      });
      return true;
    }
    responseAttachments.forEach(resAttachment => {
      const res = resAttachment.attributes;
      const attachmentsList = parent.attachmentAttrDataList.filter(attachmentAttr =>
        attachmentAttr.attachmentId === +resAttachment.attachmentId);
      const savedAttrNames = parent.getAttrNamesFromList(
        new ReExtractedData(0, parent.reExtractedActionData.docActionRelId, [], [], null),
        +resAttachment.attachmentId, parent.utilityService.isListHasValue(attachmentsList) ? attachmentsList[0].attributes : []);

      requestAttachments.forEach(reqAttachment => {
        const req = reqAttachment.attributes;
        if (+resAttachment.attachmentId === +reqAttachment.attachmentId) {
          reqAttachment.annotations = resAttachment.annotations;
          parent.compareAttributes(req, res, savedAttrNames);
          parent.comparedAttachmentId.push(+resAttachment.attachmentId);
          return true;
        }
      });
    });
    // This block of code will check and add is there new attachment added while re-extraction
    responseAttachments.forEach(resAttachment => {
      const res = resAttachment.attributes;
      if (parent.comparedAttachmentId.indexOf(+resAttachment.attachmentId) < 0) {
        const multiAttributes: ReExtractedAttributeData[] = [];
        const newAttachment = new ReExtractedAttachmentData(resAttachment.attachmentId, '', 0, [], resAttachment.annotations);
        let attrName = '';
        res.forEach(resAttribute => {
          if (+resAttribute.attrNameCde !== CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE &&
            +resAttribute.attrNameCde !== CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE_TABLE) {
            // This block of code check normal attribute level Comparison
            const newAttribute = new ReExtractedAttributeData(0, 0, true, false, false, false,
              '', 0, resAttribute.attrNameTxt, '', '', 0, 0, CONSTANTS.ATTRIBUTES.EXTRACT_TYPE_CDE.MANUALLY_CORRECTED,
              resAttribute.attrValue, resAttribute.confidencePct, resAttribute.attrNameCde, '', '', -1, '', '', -1, '', '', []);
            newAttachment.attributes.push(newAttribute);
          } else if (+resAttribute.attrNameCde === CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE) {
            // This block of code check multiple attribute level Comparison
            attrName = resAttribute.attrNameTxt;
            resAttribute.attributes.forEach(resa => {
              const newSubAttribute = new ReExtractedAttributeData(0, 0,
                true, false, false, false, '', 0, resa.attrNameTxt, '', '', 0, 0, CONSTANTS.ATTRIBUTES.EXTRACT_TYPE_CDE.MANUALLY_CORRECTED,
                resa.attrValue, resa.confidencePct, resa.attrNameCde, '', '', -1, '', '', -1, '', '', []);
              multiAttributes.push(newSubAttribute);
            });
          }
        });
        if (multiAttributes.length > 0) {
          const newAttribute = new ReExtractedAttributeData(CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE, 0,
            false, false, false, false, '',
            0, attrName, '', '', 0, 0, CONSTANTS.ATTRIBUTES.EXTRACT_TYPE_CDE.MANUALLY_CORRECTED, '',
            0, 0, '', '', -1, '', '', -1, '', '', []);
          newAttribute.attributes = multiAttributes;
          newAttachment.attributes.push(newAttribute);
          requestAttachments.push(newAttachment);
        } else {
          requestAttachments.push(newAttachment);
        }
      }
    });
  }

  /**Sub method for identifiying the document and attachment level attributes are updated or not */
  private compareIsAttributesUpdated(reqAttributes: ReExtractedAttributeData[], resAttributes: ReExtractedAttributeData[]) {
    const parent = this;
    if (resAttributes == null || reqAttributes == null) {
      return;
    }
    resAttributes.forEach(resAttribute => {
      reqAttributes.forEach(reqAttribute => {
        if (reqAttribute.attrNameCde !== CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE &&
          reqAttribute.attrNameCde !== CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE_TABLE) {
          // This block of code check normal attribute level Comparison
          if (resAttribute.attrNameCde === CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE &&
            this.utilityService.isListHasValue(resAttribute.attributes)) {
            resAttribute.attributes.forEach(attr => {
              parent.checkIsUpdated(reqAttribute, attr);
            });
          } else {
            parent.checkIsUpdated(reqAttribute, resAttribute);
          }
        } else if (+reqAttribute.attrNameCde === CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE && resAttribute.attrNameTxt !== CONSTANTS.EMPTY) {
          // This block of code check multiple attribute level Comparison
          if (+resAttribute.attrNameCde === CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE
            || +resAttribute.attrNameCde === CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE_TABLE) {
            parent.compareIsAttributesUpdated(reqAttribute.attributes, resAttribute.attributes);
          } else {
            if (this.utilityService.isListHasValue(reqAttribute.attributes)) {
              reqAttribute.attributes.forEach(attr => {
                parent.checkIsUpdated(attr, resAttribute);
              });
            }
          }
        } else if (+reqAttribute.attrNameCde === CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE_TABLE &&
          reqAttribute.attrNameTxt.toLowerCase() === resAttribute.attrNameTxt.toLowerCase() &&
          +resAttribute.attrNameCde === CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE_TABLE) {
          // This block of code check multiple attribute table level Comparison
          let reqAttrJsonStrWithoutID = '';
          if (reqAttribute.attributes != null) {
            const oldAttr: ReExtractedAttributeData[] = JSON.parse(JSON.stringify(reqAttribute.attributes));
            reqAttrJsonStrWithoutID = JSON.stringify(parent.removeItemFromJSON(oldAttr, 'id'));
          }
          if (reqAttribute.attributes.length > 0 && resAttribute.attributes.length > 0) {
            reqAttribute.attrValueTemp = JSON.stringify(reqAttribute.attributes);
            reqAttribute.attrValue = reqAttrJsonStrWithoutID;
            reqAttribute.newAttrValue = JSON.stringify(resAttribute.attributes);
            reqAttribute.newConfidencePct = resAttribute.confidencePct;
            const reqWithConfPct: ReExtractedAttributeData[] = JSON.parse(reqAttrJsonStrWithoutID);
            const resWithConfPct: ReExtractedAttributeData[] = JSON.parse(JSON.stringify(resAttribute.attributes));
            if (reqAttrJsonStrWithoutID !== JSON.stringify(resAttribute.attributes) &&
              JSON.stringify(parent.removeItemFromJSON(reqWithConfPct, 'confidencePct')) !==
              JSON.stringify(parent.removeItemFromJSON(resWithConfPct, 'confidencePct'))) {
              reqAttribute.attributes = resAttribute.attributes;
              reqAttribute.attributes.forEach(attribute => {
                attribute.isUpdated = true;
              });
              reqAttribute.isUpdated = true;
            }
          }
        }
      });
    });
  }

  private removeItemFromJSON(attributes: ReExtractedAttributeData[], itemName: any): ReExtractedAttributeData[] {
    const parent = this;
    if (attributes != null) {
      attributes.forEach(element => {
        if (!element.attrValue) {
          element.attrValue = '';
        }
        if (element[itemName]) {
          delete element[itemName];
        }
        element.attributes = parent.removeItemFromJSON(element.attributes, itemName);
      });
    }
    return attributes;
  }



  private checkIsUpdated(reqAttribute: ReExtractedAttributeData, resAttribute: ReExtractedAttributeData) {
    if (reqAttribute.attrNameTxt.toLowerCase() === resAttribute.attrNameTxt.toLowerCase()) {
      reqAttribute.newAttrValue = resAttribute.attrValue;
      reqAttribute.newConfidencePct = resAttribute.confidencePct;
      if (reqAttribute.attrValue !== resAttribute.attrValue) {
        reqAttribute.isUpdated = true;
      }
    }
  }

  /**Sub method for identifiying the document and attachment level attributes are Added or not */
  private compareIsAttributesAdded(reqAttributes: ReExtractedAttributeData[], resAttributes: ReExtractedAttributeData[],
    savedAttrNames?: String[]) {
    const parent = this;
    if (resAttributes == null || reqAttributes == null) {
      return;
    }
    resAttributes.forEach(resAttribute => {
      if (+resAttribute.attrNameCde !== CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE &&
        +resAttribute.attrNameCde !== CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE_TABLE &&
        !(this.utilityService.isListHasValue(savedAttrNames) && savedAttrNames.findIndex(attr => attr.toLowerCase() ===
          resAttribute.attrNameTxt.toLowerCase()) >= 0)) {
        // This block of code check normal attribute level Comparison
        const index = reqAttributes.findIndex(a => {
          if (a.attrNameCde !== CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE) {
            return a.attrNameTxt.toLowerCase() === resAttribute.attrNameTxt.toLowerCase();
          }
          return this.utilityService.isListHasValue(a.attributes.
            filter(attr => attr.attrNameTxt.toLowerCase() === resAttribute.attrNameTxt.toLowerCase()));
        });
        if (index === undefined || +index < 0) {
          const newAttibute = new ReExtractedAttributeData(0, 0,
            true, false, false, false, '', 0, resAttribute.attrNameTxt, '', '', 0, 0,
            CONSTANTS.ATTRIBUTES.EXTRACT_TYPE_CDE.MANUALLY_CORRECTED, resAttribute.attrValue,
            resAttribute.confidencePct, resAttribute.attrNameCde, '', '', -1, '', '', -1, '', '', []);
          reqAttributes.push(newAttibute);
        }
      } else if (+resAttribute.attrNameCde === CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE) {
        // This block of code check multiple attribute level Comparison
        const multiAttributes: ReExtractedAttributeData[] = [];
        const tempReqAttributes: ReExtractedAttributeData[] = reqAttributes.filter(a =>
          a.attrNameCde === CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE);
        resAttribute.attributes.forEach(resa => {
          const newAttibute = new ReExtractedAttributeData(0, 0,
            true, false, false, false, '', 0, resa.attrNameTxt, '', '', 0, 0,
            CONSTANTS.ATTRIBUTES.EXTRACT_TYPE_CDE.MANUALLY_CORRECTED,
            resa.attrValue, resa.confidencePct, resa.attrNameCde, '', '', -1, '', '', -1, '', '', []);
          const index = reqAttributes.findIndex(a => {
            if (a.attrNameCde !== CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE) {
              return a.attrNameTxt.toLowerCase() === resa.attrNameTxt.toLowerCase();
            }
            return this.utilityService.isListHasValue(a.attributes.
              filter(attr => attr.attrNameTxt.toLowerCase() === resa.attrNameTxt.toLowerCase()));
          });
          if ((index === undefined || +index < 0) &&
            !(this.utilityService.isListHasValue(savedAttrNames) && savedAttrNames.findIndex(attr => attr.toLowerCase() ===
              resa.attrNameTxt.toLowerCase()) >= 0)) {
            multiAttributes.push(newAttibute);
          }
        });
        if (multiAttributes.length > 0) {
          /**If attribute text is empty from response/action result attribute mark it as Added */
          if (resAttribute.attrNameTxt !== CONSTANTS.EMPTY && tempReqAttributes != null && tempReqAttributes.length > 0) {
            reqAttributes.forEach(a => {
              if (a.attrNameCde === CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE &&
                a.attrNameTxt.toLowerCase() === resAttribute.attrNameTxt.toLowerCase()) {
                multiAttributes.forEach(newAttribute => a.attributes.push(newAttribute));
              }
            });
          } else {
            const newAttibutes = new ReExtractedAttributeData(CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE, 0,
              false, false, false, false,
              '', 0, resAttribute.attrNameTxt, '', '', 0, 0, CONSTANTS.ATTRIBUTES.EXTRACT_TYPE_CDE.MANUALLY_CORRECTED, '', 0, 0,
              '', '', -1, '', '', -1, '', '', multiAttributes);
            reqAttributes.push(newAttibutes);
          }
        }
      } else if (+resAttribute.attrNameCde === CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE_TABLE) {
        // This block of code check multi attribute table level Comparison
        const index = reqAttributes.findIndex(a => a.attrNameTxt.toLowerCase() === resAttribute.attrNameTxt.toLowerCase());

        if (index === undefined || +index < 0) {
          const newAttrValue = JSON.stringify(resAttribute.attributes);
          const multiAttributes: ReExtractedAttributeData[] = [];
          resAttribute.attributes.forEach(resa => {
            resa.isAdded = true;
            multiAttributes.push(resa);
          });
          if (multiAttributes.length > 0) {
            const newAttibute = new ReExtractedAttributeData(CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE_TABLE,
              0, true, false, false, false,
              '', 0, resAttribute.attrNameTxt, '', '', 0, 0, CONSTANTS.ATTRIBUTES.EXTRACT_TYPE_CDE.MANUALLY_CORRECTED, newAttrValue,
              resAttribute.confidencePct, resAttribute.attrNameCde, '', '', -1, '', '', -1, '', '', multiAttributes);
            reqAttributes.push(newAttibute);
          }
        }
      }
    });
  }

  /**Sub method for identifiying the document and attachment level attributes are deleted or not */
  private compareIsAttributesDeleted(reqAttributes: ReExtractedAttributeData[], resAttributes: ReExtractedAttributeData[]) {
    const parent = this;
    if (reqAttributes == null) {
      return;
    }
    reqAttributes.forEach(reqAttribute => {
      if (+reqAttribute.attrNameCde !== CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE &&
        +reqAttribute.attrNameCde !== CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE_TABLE) {
        const index = (resAttributes != null) ? resAttributes.findIndex(a => {
          if (a.attrNameCde !== CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE) {
            return a.attrNameTxt.toLowerCase() === reqAttribute.attrNameTxt.toLowerCase();
          }
          return this.utilityService.isListHasValue(a.attributes.filter(
            attr => attr.attrNameTxt.toLowerCase() === reqAttribute.attrNameTxt.toLowerCase()));
        }) : -1;
        if (+index < 0 && !reqAttribute.isUpdated) {
          reqAttribute.isDeleted = true;
        }
      } else if (+reqAttribute.attrNameCde === CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE) {
        const tempResAttributes: ReExtractedAttributeData[] = (resAttributes != null) ? resAttributes.filter(a =>
          +a.attrNameCde === CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE) : [];

        reqAttribute.attributes.forEach(subAttribute => {
          // /**If attribute text is empty from current attribute mark it as deleted */
          // if (reqAttribute.attrNameTxt === CONSTANTS.EMPTY) {
          // subAttribute.isDeleted = true;
          // } else {
          if (tempResAttributes.length === 0 && !subAttribute.isUpdated) {
            subAttribute.isDeleted = true;
          } else {
            if (this.utilityService.isListHasValue(resAttributes)) {
              resAttributes.forEach(attribute => {
                if (attribute.attrNameCde !== CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE) {
                  if (attribute.attrNameTxt.toLowerCase() === subAttribute.attrNameTxt.toLowerCase() && !subAttribute.isUpdated) {
                    // subAttribute.isDeleted = true;
                  }
                } else {
                  tempResAttributes.forEach(function (attr) {
                    const index = attr.attributes.findIndex(a => a.attrNameTxt.toLowerCase() === subAttribute.attrNameTxt.toLowerCase());
                    if (+index < 0 && !subAttribute.isUpdated) {
                      subAttribute.isDeleted = true;
                    }
                  });
                }
              });
            }
          }
        });
      } else if (+reqAttribute.attrNameCde === CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE_TABLE) {
        const tempResAttributes: ReExtractedAttributeData[] = (resAttributes != null) ? resAttributes.filter(a =>
          +a.attrNameCde === CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE_TABLE &&
          a.attrNameTxt.toLowerCase() === reqAttribute.attrNameTxt.toLowerCase()) : [];
        if (tempResAttributes.length > 0) {
          tempResAttributes.forEach(function (a) {
            if (a.attributes.length === 0) {
              reqAttribute.attrValue = JSON.stringify(reqAttribute.attributes);
              if (!reqAttribute.isUpdated) {
                reqAttribute.isDeleted = true;
              }
            }
          });
        } else {
          reqAttribute.attrValue = JSON.stringify(reqAttribute.attributes);
          if (!reqAttribute.isUpdated) {
            reqAttribute.isDeleted = true;
          }
        }
      }
    });
  }

  /**used for mapping attachment name to respective attachemetn id*/
  private updateAttachmentName(docId: number, attachments: ReExtractedAttachmentData[]) {
    const parent = this;
    parent.attachmentService.getAttachmentList(docId).then(function (data) {
      const attachmentDataList: AttachmentData[] = data as AttachmentData[];
      attachments.forEach(attachment => {
        attachmentDataList.forEach((resList, index) => {
          if (+attachment.attachmentId === +resList.attachmentId) {
            attachment.attachmentName = resList.fileName;
            attachment.attachmentSequence = index + 1;
            return true;
          }
        });
      });
    });
  }

  /**This method called in re-exctract popup while clicking save button.
   * Here will filter add, delete and update atributes from the object which is used for populating value in UI.
   * Late call the API service for respective action executions
   */
  public saveAttributes() {
    const parent = this;
    parent.model.isDataSaved = false;
    // parent.model.isSaveClicked = true;
    parent.model.requestAttributes.docId = parent.documentData.docId;
    parent.model.requestAttributes.docActionRelId = parent.reExtractedActionData.docActionRelId;
    const deletedAttributes: ReExtractedData = parent.filterAttributes(CONSTANTS.RE_EXTRACT_CONFIG.ISDELETED,
      JSON.parse(JSON.stringify(parent.model.requestAttributes)));
    const addedAttributes: ReExtractedData = parent.filterAttributes(CONSTANTS.RE_EXTRACT_CONFIG.ISADDED,
      JSON.parse(JSON.stringify(parent.model.requestAttributes)));
    const updatedAttributes: ReExtractedData = parent.filterAttributes(CONSTANTS.RE_EXTRACT_CONFIG.ISUPDATED,
      JSON.parse(JSON.stringify(parent.model.requestAttributes)));

    console.log('unchecked');
    console.log(parent.uncheckedAttributes);
    console.log('addedAttributes');
    console.log(addedAttributes);
    console.log('deletedAttributes');
    console.log(deletedAttributes);
    console.log('updatedAttributes');
    console.log(updatedAttributes);
    parent.filterDuplicateNormalAttributes(parent.attrDbDataList, addedAttributes, deletedAttributes);
    parent.getAnnotationAttribute(addedAttributes, updatedAttributes, deletedAttributes);
    const addedNormalAttributes: DocumentData = parent.filterNormalAttributes(JSON.parse(JSON.stringify(addedAttributes)));
    const addedMultiAttributes: ReExtractedData = parent.filterMultiAttibutes(JSON.parse(JSON.stringify(addedAttributes)));

    const deletedNormalAttributes: DocumentData = parent.filterNormalAttributes(JSON.parse(JSON.stringify(deletedAttributes)));
    const deletedMultiAttributes: ReExtractedData = parent.filterMultiAttibutes(JSON.parse(JSON.stringify(deletedAttributes)));

    const updatedNormalAttributes: DocumentData = parent.filterNormalAttributes(JSON.parse(JSON.stringify(updatedAttributes)));
    const updatedMultiAttributes: ReExtractedData = parent.filterMultiAttibutes(JSON.parse(JSON.stringify(updatedAttributes)));

    let manageMultiAttrData = [];

    manageMultiAttrData = manageMultiAttrData.concat(parent.consolidateAttachmentMultiAttrData(
      JSON.parse(JSON.stringify(addedMultiAttributes)), JSON.parse(JSON.stringify(updatedMultiAttributes)),
      JSON.parse(JSON.stringify(deletedMultiAttributes))));
    manageMultiAttrData = manageMultiAttrData.concat(parent.consolidateMultiAttrData(
      JSON.parse(JSON.stringify(addedMultiAttributes)),
      JSON.parse(JSON.stringify(updatedMultiAttributes)), JSON.parse(JSON.stringify(deletedMultiAttributes))));

    console.log('manageMultiAttrData');
    console.log(manageMultiAttrData);
    /** Use ADD/DELETE/UPDATE API for noraml attributes as its doing separate calls */
    const promiseForAdd = parent.addToDataBase(addedNormalAttributes);
    const promiseForUpdate = parent.updateToDataBase(updatedNormalAttributes);
    const promiseForDelete = parent.deleteToDataBase(deletedNormalAttributes);

    const promiseAll = [promiseForAdd, promiseForUpdate, promiseForDelete];
    /** Use Manage Multi Attibute API to updates all add/delete/update altogether at singel call*/
    manageMultiAttrData.forEach(attrData => {
      promiseAll.push(parent.manageAttrToDataBase(attrData));
    });

    /** saveSnapshot should be called in both success and failure scenario*/
    Promise.all(promiseAll).then(function (data) {
      parent.completeActionAndSaveSnapshot().then(
        function (_data) {
          parent.toastr.success(parent.msgInfo.getMessage(156));
          parent.closeWindow(true);
        }).catch(function (error) { parent.toastr.error(parent.msgInfo.getMessage(143)); });
      parent.model.isDataSaved = true;
    }).catch(function (error) {
      parent.model.isPartialSave = true;
      parent.completeActionAndSaveSnapshot().catch(function (err) { parent.toastr.error(parent.msgInfo.getMessage(143)); });
      parent.model.isDataSaved = true;
    });

  }

  private completeActionAndSaveSnapshot() {
    const parent = this;
    const savedAttributesSnapShot: ReExtractedData = parent.filterAttributes(CONSTANTS.RE_EXTRACT_CONFIG.ISSAVE,
      JSON.parse(JSON.stringify(parent.model.requestAttributes)));
    const actionData: ActionData = new ActionData(0, null, null, parent.reExtractedActionData.docActionRelId,
      0, 900, '', '', 0, '', JSON.stringify(savedAttributesSnapShot), [], []);
    const updateActionStatus = [new DocumentData(parent.documentData.docId,
      0, null, 0, null, 0, 0, 0, null, null, null, [actionData], [], [])];
    console.log('Snap Shot');
    console.log(updateActionStatus);

    return parent.updateActionToDB(updateActionStatus);
  }

  private getAnnotationAttribute(addedAttributes: ReExtractedData, updatedAttributes: ReExtractedData, deletedAttributes: ReExtractedData) {
    if (this.utilityService.isListHasValue(this.attachmentAttrDataList) && (this.utilityService.isListHasValue(addedAttributes.attachments)
      || this.utilityService.isListHasValue(updatedAttributes.attachments) ||
      this.utilityService.isListHasValue(deletedAttributes.attachments))) {
      this.attachmentAttrDataList.forEach(attachmentAttr => {
        const attachmentId = attachmentAttr.attachmentId;
        this.filterDuplicateAttachAttributes(attachmentAttr.attributes, addedAttributes, deletedAttributes, attachmentId);
        if (this.isEnableAnnotation) {
          // If annotation not enabled at application level dont process content annotation attributes(47).
          const annotationAttributes = JSON.parse(JSON.stringify(attachmentAttr.attributes.filter(attribute => attribute.attrNameCde ===
            CONSTANTS.ATTR_NAME_CDE.CONTENT_ANNOTATION_ANNOTATOR
            || attribute.attrNameCde === CONSTANTS.ATTR_NAME_CDE.CONTENT_ANNOTATION)));
          const isAnnotationAttrPresent = this.utilityService.isListHasValue(annotationAttributes);
          this.processAnnotationAttrBasedOnOp(deletedAttributes, attachmentId, annotationAttributes, CONSTANTS.OPERATION_TYPE.DELETE);
          this.processAnnotationAttrBasedOnOp(updatedAttributes, attachmentId, annotationAttributes, CONSTANTS.OPERATION_TYPE.EDIT);
          this.processAnnotationAttrBasedOnOp(addedAttributes, attachmentId, annotationAttributes, CONSTANTS.OPERATION_TYPE.ADD);
          if (isAnnotationAttrPresent) {
            if (this.utilityService.isListHasValue(JSON.parse(annotationAttributes[0].attrValue))) {
              this.addAttributeToList(updatedAttributes, attachmentId, annotationAttributes);
            } else {
              this.addAttributeToList(deletedAttributes, attachmentId, annotationAttributes);
            }
          } else {
            if (this.utilityService.isListHasValue(annotationAttributes)
              && this.utilityService.isListHasValue(JSON.parse(annotationAttributes[0].attrValue))) {
              this.addAttributeToList(addedAttributes, attachmentId, annotationAttributes);
            }
          }
        }
      });
    }
  }

  private addAttributeToList(attributes: ReExtractedData, attachmentId: number, annotationAttributes: any) {
    if (this.utilityService.isListHasValue(attributes.attachments)) {
      attributes.attachments.forEach(attach => {
        if (attach.attachmentId === attachmentId) {
          attach.attributes.push(annotationAttributes[0]);
        }
      });
    } else {
      const attributeDataList = [];
      attributeDataList.push(new ReExtractedAttributeData(annotationAttributes[0].attrNameCde, annotationAttributes[0].docId,
        false, false, false, false, '', annotationAttributes[0].id, '',
        annotationAttributes[0].attrValue, '', annotationAttributes[0].confidencePct, annotationAttributes[0].extractTypeCde,
        CONSTANTS.ATTRIBUTES.EXTRACT_TYPE_CDE.MANUALLY_CORRECTED, annotationAttributes[0].attrValue,
        CONSTANTS.ATTRIBUTES.CONFIDENCE_PCT.UNDEFINED, annotationAttributes[0].attrNameCde, '', '', -1, '', '', -1, '', '', []));
      const attachment = new ReExtractedAttachmentData(attachmentId, '', 0, attributeDataList, []);
      attributes.attachments.push(attachment);
    }
  }

  private processAnnotationAttrBasedOnOp(attributeDataList: ReExtractedData, attachmentId: number,
    annotationAttributes: AttributeData[], opType: string) {
    attributeDataList.attachments.forEach(attrAttachment => {
      if (attrAttachment.attachmentId === attachmentId) {
        const normalAttributes = attrAttachment.attributes.filter
          (attrData => attrData.attrNameCde !== CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE &&
            attrData.attrNameCde !== CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE_TABLE);
        const multiAttributes = attrAttachment.attributes.filter
          (attrData => attrData.attrNameCde === CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE &&
            attrData.attrNameCde !== CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE_TABLE);
        let attributes = [];
        multiAttributes.forEach(attr => { attributes = attributes.concat(attr.attributes); });
        attributes = attributes.concat(normalAttributes);
        this.modifyAnnotationAttrBasedOnOp(annotationAttributes, attributes, opType, attrAttachment.annotations, attachmentId);
      }
    });
  }

  private modifyAnnotationAttrBasedOnOp(annotationAttributes: AttributeData[], attributes: AttributeData[],
    opType: string, annotationDataList, attachmentId: number) {
    switch (opType) {
      case CONSTANTS.OPERATION_TYPE.ADD: {
        if (this.utilityService.isListHasValue(attributes)) {
          const annotations = this.utilityService.isListHasValue(annotationAttributes) ? JSON.parse(annotationAttributes[0].attrValue) : [];
          attributes.forEach(attr => {
            const annotationDbData = annotationDataList.filter(ann => ann.label === attr.attrNameTxt);
            if (!this.utilityService.isListHasValue(annotations.filter(ann => ann.text === attr.attrNameTxt))) {
              const annotation = {
                quote: attr.attrValue,
                text: attr.attrNameTxt,
                createdByTypeCde: 2,
                occurrenceNum: this.getOccurrenceNum(annotationDbData)
              };
              annotations.push(annotation);
            }
          });
          if (this.utilityService.isListHasValue(annotationAttributes)) {
            annotationAttributes[0].attrValue = JSON.stringify(annotations);
          } else {
            annotationAttributes.push(new AttributeData(CONSTANTS.ATTR_NAME_CDE.CONTENT_ANNOTATION, this.documentData.docId, attachmentId,
              false, 0, '', JSON.stringify(annotations), CONSTANTS.ATTRIBUTES.EXTRACT_TYPE_CDE.MANUALLY_CORRECTED, '',
              CONSTANTS.ATTRIBUTES.CONFIDENCE_PCT.UNDEFINED, [], [], '', false, undefined));
          }
        }
        break;
      }
      case CONSTANTS.OPERATION_TYPE.EDIT: {
        if (this.utilityService.isListHasValue(attributes)) {
          let annotations = this.utilityService.isListHasValue(annotationAttributes) ? JSON.parse(annotationAttributes[0].attrValue) : [];
          attributes.forEach(attr => {
            const annotationDbData = annotationDataList.filter(ann => ann.label === attr.attrNameTxt);
            annotations = annotations.filter(ann =>
              ann.text !== attr.attrNameTxt
            );
            const annotation = {
              quote: attr.attrValue,
              text: attr.attrNameTxt,
              createdByTypeCde: 2,
              occurrenceNum: this.getOccurrenceNum(annotationDbData)
            };
            annotations.push(annotation);
          });
          if (this.utilityService.isListHasValue(annotationAttributes)) {
            annotationAttributes[0].attrValue = JSON.stringify(annotations);
          } else {
            annotationAttributes.push(new AttributeData(CONSTANTS.ATTR_NAME_CDE.CONTENT_ANNOTATION, this.documentData.docId, attachmentId,
              false, 0, '', JSON.stringify(annotations), CONSTANTS.ATTRIBUTES.EXTRACT_TYPE_CDE.MANUALLY_CORRECTED, '',
              CONSTANTS.ATTRIBUTES.CONFIDENCE_PCT.UNDEFINED, [], [], '', false, undefined));
          }
        }
        break;
      }
      case CONSTANTS.OPERATION_TYPE.DELETE: {
        if (this.utilityService.isListHasValue(annotationAttributes) && this.utilityService.isListHasValue(attributes)) {
          let annotations = JSON.parse(annotationAttributes[0].attrValue);
          attributes.forEach(attr => {
            annotations = annotations.filter(ann =>
              ann.text !== attr.attrNameTxt
            );
          });
          annotationAttributes[0].attrValue = JSON.stringify(annotations);
        }
        break;
      }
    }
  }

  private getOccurrenceNum(annotationDbData: any) {
    let occurrenceNum = 0;
    if (this.utilityService.isListHasValue(annotationDbData)) {
      occurrenceNum = this.utilityService.isAValidValue(annotationDbData[0].occurrenceNum) ?
        annotationDbData[0].occurrenceNum : 0;
    }
    return occurrenceNum;
  }

  private filterDuplicateAttachAttributes(attributes: AttributeData[],
    addedAttributes: ReExtractedData, deletedAttributes: ReExtractedData, attachmentId?: number) {
    const deletedAttrNames = this.getAttrNamesFromList(deletedAttributes, attachmentId);
    const savedAttrNames = this.getAttrNamesFromList(
      new ReExtractedData(0, this.reExtractedActionData.docActionRelId, [], [], null), attachmentId, attributes);
    addedAttributes.attachments.forEach(attachment => {
      if (attachment.attachmentId === attachmentId) {
        attachment.attributes = this.getFilteredAttrs(attachment.attributes, deletedAttrNames, savedAttrNames);
      }
    });
  }

  private filterDuplicateNormalAttributes(attributes: AttributeData[],
    addedAttributes: ReExtractedData, deletedAttributes: ReExtractedData) {
    const deletedAttrNames = this.getAttrNamesFromList(deletedAttributes);
    const savedAttrNames = this.getAttrNamesFromList(
      new ReExtractedData(0, this.reExtractedActionData.docActionRelId, [], [], null), 0, [], attributes);
    addedAttributes.attributes = this.getFilteredAttrs(addedAttributes.attributes, deletedAttrNames, savedAttrNames);
  }

  private getFilteredAttrs(attributes: ReExtractedAttributeData[], deletedAttrNames: string[], savedAttrNames: string[]) {
    attributes = attributes.filter(attrData => {
      if (attrData.attrNameCde === CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE) {
        attrData.attributes = attrData.attributes.filter(attr => {
          if (this.doesAttrNameExist(deletedAttrNames, attr, savedAttrNames)) {
            return false;
          }
          return true;
        });
        return this.utilityService.isListHasValue(attrData.attributes);
      } else {
        return !(this.doesAttrNameExist(deletedAttrNames, attrData, savedAttrNames));
      }
    });
    return attributes;
  }

  private doesAttrNameExist(deletedAttrNames: string[], attr: ReExtractedAttributeData, savedAttrNames: string[]) {
    return deletedAttrNames.findIndex(attrName => attrName.toLowerCase() ===
      attr.attrNameTxt.toLowerCase()) < 0 && savedAttrNames.findIndex(attrName => attrName.toLowerCase() ===
        attr.attrNameTxt.toLowerCase()) >= 0;
  }

  private getAttrNamesFromList(attributeReExtractedData: ReExtractedData, attachmentId?: number,
    attachAttrDbDataList?: AttributeData[], attrDataList?: AttributeData[]) {
    const attrNames: string[] = [];
    let attachAttrDataList = [];
    let attrDbDataList = [];
    if (attachmentId > 0) {
      if (this.utilityService.isListHasValue(attributeReExtractedData.attachments)) {
        attachAttrDataList = attributeReExtractedData.attachments.filter(attach => attach.attachmentId === attachmentId);
        if (this.utilityService.isListHasValue(attachAttrDataList)) {
          attachAttrDataList = attachAttrDataList[0].attributes;
        }
      } else if (this.utilityService.isListHasValue(attachAttrDbDataList)) {
        attachAttrDataList = attachAttrDbDataList;
      }
    } else {
      if (this.utilityService.isListHasValue(attributeReExtractedData.attributes)) {
        attrDbDataList = attributeReExtractedData.attributes;
      } else if (this.utilityService.isListHasValue(attrDataList)) {
        attrDbDataList = attrDataList;
      }
    }
    if (this.utilityService.isListHasValue(attachAttrDataList)) {
      attachAttrDataList.forEach(attribute => {
        if (attribute.attrNameCde === CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE) {
          attribute.attributes.forEach(attr => attrNames.push(attr.attrNameTxt));
        } else {
          attrNames.push(attribute.attrNameTxt);
        }
      });
    } else if (this.utilityService.isListHasValue(attrDbDataList)) {
      attrDbDataList.forEach(attribute => {
        if (attribute.attrNameCde === CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE) {
          attribute.attributes.forEach(attr => attrNames.push(attr.attrNameTxt));
        } else {
          attrNames.push(attribute.attrNameTxt);
        }
      });
    }
    return attrNames;
  }


  private subConsolidateAttachmentMultiAttrData(multiAttributeData: ReExtractedData, doAttrTxt: string): any[] {
    const manageAttachData = [];
    multiAttributeData.attachments.forEach(attachment => {
      attachment.attributes.forEach(attachAttr => {
        const attrData = attachAttr;
        const manageAttachAttrDataList = new ManageAttributeDataList(attrData.id, attrData.attrNameCde, attrData.attrNameTxt,
          attrData.attrValue, CONSTANTS.ATTRIBUTES.EXTRACT_TYPE_CDE.MANUALLY_CORRECTED,
          CONSTANTS.ATTRIBUTES.EXTRACT_TYPE_TXT.MANUALLY_CORRECTED, CONSTANTS.ATTRIBUTES.CONFIDENCE_PCT.MAX, null, null, null);
        attachAttr.attributes = this.updateManuallyAddedProperties(attachAttr.attributes);
        if (doAttrTxt === CONSTANTS.RE_EXTRACT_CONFIG.ADDATTRIBUTE) {
          manageAttachAttrDataList.addAttributes = attachAttr.attributes;
        } else if (doAttrTxt === CONSTANTS.RE_EXTRACT_CONFIG.UPDATEATTRIBUTE) {
          /** For Multi Attribute Table updates - softdelete the existing records and add the new records */
          if (attrData.attrNameCde === CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE) {
            manageAttachAttrDataList.editAttributes = attachAttr.attributes;
          } else if (attrData.attrNameCde === CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE_TABLE) {
            manageAttachAttrDataList.addAttributes = attachAttr.attributes;
            if (attrData.attrValueTemp) {
              manageAttachAttrDataList.deleteAttributes = JSON.parse(attrData.attrValueTemp);
            }
          }
        } else if (doAttrTxt === CONSTANTS.RE_EXTRACT_CONFIG.DELETEATTRIBUTE) {
          manageAttachAttrDataList.deleteAttributes = attachAttr.attributes;
        }
        const consolidatedAttachAttrDataList = new ManageAttachmentAttributeDataList(attachment.attachmentId,
          manageAttachAttrDataList);
        const consolidateAttrData = new ManageAttributeData(multiAttributeData.docId, this.reExtractedActionData.docActionRelId,
          null, consolidatedAttachAttrDataList);
        manageAttachData.push(consolidateAttrData);
      });
    });
    return manageAttachData;
  }

  private updateManuallyAddedProperties(attributes: ReExtractedAttributeData[]): ReExtractedAttributeData[] {
    attributes.forEach(attribute => {
      attribute.confidencePct = CONSTANTS.ATTRIBUTES.CONFIDENCE_PCT.MAX;
      attribute.extractTypeCde = CONSTANTS.ATTRIBUTES.EXTRACT_TYPE_CDE.MANUALLY_CORRECTED;
      attribute.extractTypeTxt = CONSTANTS.ATTRIBUTES.EXTRACT_TYPE_TXT.MANUALLY_CORRECTED;
      this.updateManuallyAddedProperties(attribute.attributes);
    });
    return attributes;
  }

  private consolidateAttachmentMultiAttrData(addedMultiAttributeData: ReExtractedData, updatedMultiAttributeData: ReExtractedData,
    deletedMultiAttributeData: ReExtractedData): any[] {
    let manageAttachData = [];
    const parent = this;
    let isAddedUpdated = false;
    if (addedMultiAttributeData != null && updatedMultiAttributeData != null &&
      addedMultiAttributeData.attachments.length > 0 && updatedMultiAttributeData.attachments.length > 0) {
      addedMultiAttributeData.attachments.forEach(addAttachment => {
        updatedMultiAttributeData.attachments.forEach(updateAttachment => {
          if (+addAttachment.attachmentId === +updateAttachment.attachmentId) {
            addAttachment.attributes.forEach(addAttachAttr => {
              updateAttachment.attributes.forEach(updateAttachAttr => {
                if (+addAttachAttr.id === +updateAttachAttr.id) {
                  isAddedUpdated = true;
                  const attrData = addAttachAttr;
                  const manageAttachAttrDataList = new ManageAttributeDataList(attrData.id, attrData.attrNameCde, attrData.attrNameTxt,
                    attrData.attrValue, CONSTANTS.ATTRIBUTES.EXTRACT_TYPE_CDE.MANUALLY_CORRECTED, attrData.extractTypeTxt,
                    CONSTANTS.ATTRIBUTES.CONFIDENCE_PCT.MAX, addAttachAttr.attributes, updateAttachAttr.attributes, null);
                  const consolidatedAttachAttrDataList = new ManageAttachmentAttributeDataList(addAttachment.attachmentId,
                    manageAttachAttrDataList);
                  const consolidateAttrData = new ManageAttributeData(addedMultiAttributeData.docId,
                    this.reExtractedActionData.docActionRelId, null, consolidatedAttachAttrDataList);
                  manageAttachData.push(consolidateAttrData);
                }
              });
            });
          }
        });
      });
    }
    if (!isAddedUpdated && addedMultiAttributeData != null && addedMultiAttributeData.attachments.length > 0) {
      manageAttachData = manageAttachData.concat(parent.subConsolidateAttachmentMultiAttrData(
        addedMultiAttributeData, CONSTANTS.RE_EXTRACT_CONFIG.ADDATTRIBUTE));
    }
    if (!isAddedUpdated && updatedMultiAttributeData != null && updatedMultiAttributeData.attachments.length > 0) {
      manageAttachData = manageAttachData.concat(parent.subConsolidateAttachmentMultiAttrData(
        updatedMultiAttributeData, CONSTANTS.RE_EXTRACT_CONFIG.UPDATEATTRIBUTE));
    }
    if (deletedMultiAttributeData != null && deletedMultiAttributeData.attachments.length > 0) {
      manageAttachData = manageAttachData.concat(parent.subConsolidateAttachmentMultiAttrData(
        deletedMultiAttributeData, CONSTANTS.RE_EXTRACT_CONFIG.DELETEATTRIBUTE));
    }
    // console.log('consolidateAttachmentMultiAttrData');
    // console.log(manageAttachData);
    return manageAttachData;
  }

  private subConsolidateMultiAttrData(multiAttributeData: ReExtractedData, doAttrTxt: string): any[] {
    const manageData = [];
    multiAttributeData.attributes.forEach(attribute => {
      const attrData = attribute;
      const manageAttrDataList = new ManageAttributeDataList(attrData.id, attrData.attrNameCde, attrData.attrNameTxt,
        attrData.attrValue, CONSTANTS.ATTRIBUTES.EXTRACT_TYPE_CDE.MANUALLY_CORRECTED,
        CONSTANTS.ATTRIBUTES.EXTRACT_TYPE_TXT.MANUALLY_CORRECTED, CONSTANTS.ATTRIBUTES.CONFIDENCE_PCT.MAX, null, null, null);
      attrData.attributes = this.updateManuallyAddedProperties(attrData.attributes);
      if (doAttrTxt === CONSTANTS.RE_EXTRACT_CONFIG.ADDATTRIBUTE) {
        manageAttrDataList.addAttributes = attrData.attributes;
      } else if (doAttrTxt === CONSTANTS.RE_EXTRACT_CONFIG.UPDATEATTRIBUTE) {
        /** For Multi Attribute Table updates - softdelete the existing records and add the new records */
        if (attrData.attrNameCde === CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE) {
          manageAttrDataList.editAttributes = attrData.attributes;
        } else if (attrData.attrNameCde === CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE_TABLE) {
          manageAttrDataList.addAttributes = attrData.attributes;
          if (attrData.attrValueTemp) {
            manageAttrDataList.deleteAttributes = JSON.parse(attrData.attrValueTemp);
          }
        }
      } else if (doAttrTxt === CONSTANTS.RE_EXTRACT_CONFIG.DELETEATTRIBUTE) {
        manageAttrDataList.deleteAttributes = attrData.attributes;
      }
      const consolidateAttrData = new ManageAttributeData(multiAttributeData.docId, this.reExtractedActionData.docActionRelId,
        manageAttrDataList, null);
      manageData.push(consolidateAttrData);
    });
    return manageData;
  }

  private consolidateMultiAttrData(addedMultiAttributeData: ReExtractedData,
    updatedMultiAttributeData: ReExtractedData, deletedMultiAttributeData: ReExtractedData): any[] {
    let manageAttachData = [];
    const parent = this;
    let isAddedUpdated = false;
    if (addedMultiAttributeData != null && updatedMultiAttributeData != null &&
      addedMultiAttributeData.attributes.length > 0 && updatedMultiAttributeData.attributes.length > 0) {
      addedMultiAttributeData.attributes.forEach(addAttribute => {
        updatedMultiAttributeData.attributes.forEach(updateAttribute => {
          if (+addAttribute.id === +updateAttribute.id) {
            isAddedUpdated = true;
            const attrData = addAttribute;
            const manageAttrDataList = new ManageAttributeDataList(attrData.id, attrData.attrNameCde, attrData.attrNameTxt,
              attrData.attrValue, CONSTANTS.ATTRIBUTES.EXTRACT_TYPE_CDE.MANUALLY_CORRECTED, attrData.extractTypeTxt,
              CONSTANTS.ATTRIBUTES.CONFIDENCE_PCT.MAX, addAttribute.attributes, updateAttribute.attributes, null);
            const consolidateAttrData = new ManageAttributeData(addedMultiAttributeData.docId, parent.reExtractedActionData.docActionRelId,
              manageAttrDataList, null);
            manageAttachData.push(consolidateAttrData);
          }
        });
      });
    }
    if (!isAddedUpdated && addedMultiAttributeData != null && addedMultiAttributeData.attributes.length > 0) {
      manageAttachData = manageAttachData.concat(parent.subConsolidateMultiAttrData(addedMultiAttributeData,
        CONSTANTS.RE_EXTRACT_CONFIG.ADDATTRIBUTE));
    }
    if (!isAddedUpdated && updatedMultiAttributeData != null && updatedMultiAttributeData.attributes.length > 0) {
      manageAttachData = manageAttachData.concat(parent.subConsolidateMultiAttrData(updatedMultiAttributeData,
        CONSTANTS.RE_EXTRACT_CONFIG.UPDATEATTRIBUTE));
    }
    if (deletedMultiAttributeData != null && deletedMultiAttributeData.attributes.length > 0) {
      manageAttachData = manageAttachData.concat(parent.subConsolidateMultiAttrData(deletedMultiAttributeData,
        CONSTANTS.RE_EXTRACT_CONFIG.DELETEATTRIBUTE));
    }
    // console.log('consolidateMultiAttrData');
    // console.log(manageAttachData);
    return manageAttachData;
  }


  private filterNormalAttributes(documentData: any): DocumentData {
    documentData.attachments.forEach((element, index) => {
      element.attributes = element.attributes.filter(innerAttributes =>
        (+innerAttributes.attrNameCde !== CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE
          && +innerAttributes.attrNameCde !== CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE_TABLE));
    });

    documentData.attributes = documentData.attributes.filter(innerAttributes =>
      (+innerAttributes.attrNameCde !== CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE
        && +innerAttributes.attrNameCde !== CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE_TABLE));

    documentData = this.filterEmptyMultiAttributes(documentData);
    documentData = this.filterEmptyAttachments(documentData);
    return this.convetToDocumentData(documentData);
  }

  private filterMultiAttibutes(documentData: any): ReExtractedData {
    documentData.attachments.forEach((element, index) => {
      element.attributes = element.attributes.filter(innerAttributes =>
        (+innerAttributes.attrNameCde === CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE
          || +innerAttributes.attrNameCde === CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE_TABLE));
    });

    documentData.attributes = documentData.attributes.filter(innerAttributes =>
      (+innerAttributes.attrNameCde === CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE
        || +innerAttributes.attrNameCde === CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE_TABLE));

    documentData = this.filterEmptyMultiAttributes(documentData);
    documentData = this.filterEmptyAttachments(documentData);
    return documentData;
  }

  private convetToDocumentData(reExtractedData: ReExtractedData): DocumentData {
    this.attributeHelper.removeAttributeDataOptionFields(reExtractedData);
    return JSON.parse(JSON.stringify(reExtractedData)) as DocumentData;
  }

  /**used for handling the user unchecked attributes in re-extract popup. Later used for filter and ignore Db save */
  public updateUncheckedAttibutes(event) {
    const id: string = event.target.id;
    const index = this.uncheckedAttributes.indexOf(id);
    if (index > -1) {
      this.uncheckedAttributes.splice(index, 1);
    } else {
      this.uncheckedAttributes.push(id);
    }
    console.log(this.uncheckedAttributes);
  }

  /**Used for filtering the add, delete and update attribute seperatly based on key param */
  private filterAttributes(key: string, reExtractedData: ReExtractedData): ReExtractedData {
    const parent = this;
    reExtractedData = parent.filterAttributesData(key, reExtractedData, 'email_'); // Document level
    reExtractedData = parent.filterAttachmentAttributes(key, reExtractedData, 'attachment_'); // Attachment level
    return reExtractedData;
  }
  /**Filter add, delete, update on document level */
  private filterAttributesData(key: string, reExtractedData: any, level: string): ReExtractedData {
    const parent = this;
    if (reExtractedData.attributes == null || reExtractedData.attributes.length === 0) {
      reExtractedData.attributes = [];
      return reExtractedData;
    }
    reExtractedData.attributes.forEach(function (attributes, index) {
      if ((+attributes.attrNameCde !== CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE_TABLE &&
        +attributes.newAttrNameCde !== CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE_TABLE) &&
        attributes.attributes != null && attributes.attributes.length > 0) {
        if (attributes.attrNameCde) {
          attributes.attributes.forEach((innerAttributes, innerIndex) => {
            const parentAttributes: ReExtractedAttributeData = reExtractedData.attributes[index];
            parent.SubFilterAttributesData(innerAttributes, parentAttributes, key, innerIndex, level + attributes.attrNameCde + '_');
          });
        }
        attributes.attributes = parent.filterNullInArray(attributes.attributes);
      } else {
        parent.SubFilterAttributesData(attributes, reExtractedData, key, index, level);
        if (+attributes.attrNameCde === CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE_TABLE ||
          +attributes.newAttrNameCde === CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE_TABLE) {
          if (key !== CONSTANTS.RE_EXTRACT_CONFIG.ISSAVE) {
            attributes.attrValue = '';
          }
        }
      }
    });
    reExtractedData.attributes = parent.filterNullInArray(reExtractedData.attributes);
    reExtractedData = parent.filterEmptyMultiAttributes(reExtractedData);
    return reExtractedData;
  }
  /**Filter add, delete, update on attachment level */
  private filterAttachmentAttributes(key: string, reExtractedData: ReExtractedData, level: string): ReExtractedData {
    const parent = this;
    if (reExtractedData.attachments == null || reExtractedData.attachments.length === 0) {
      return reExtractedData;
    }
    reExtractedData.attachments.forEach(function (attachments, i) {
      parent.filterAttributesData(key, attachments, level + (i + 1) + '_');
    });
    if (key !== CONSTANTS.RE_EXTRACT_CONFIG.ISSAVE) {
      reExtractedData = parent.filterEmptyAttachments(reExtractedData);
    }
    return reExtractedData;
  }

  /**sub methoed for filtering the add, delete and update attribute seperatly based on key param */
  private SubFilterAttributesData(attributeData: ReExtractedAttributeData, parentAttributes: any, key: string,
    index: number, level: string) {
    const parent = this;
    if (key === CONSTANTS.RE_EXTRACT_CONFIG.ISSAVE) {
      /**This block of code used to prepare request for snapshot updates */
      const keyArray: string[] = [CONSTANTS.RE_EXTRACT_CONFIG.ISADDED, CONSTANTS.RE_EXTRACT_CONFIG.ISDELETED,
      CONSTANTS.RE_EXTRACT_CONFIG.ISUPDATED];
      keyArray.forEach(akey => {
        if (attributeData.hasOwnProperty(akey) && attributeData[akey]) {
          if (parent.uncheckedAttributes.indexOf(parent.createCheckboxId(attributeData, level, index)) > -1) {
            attributeData.remark = CONSTANTS.RE_EXTRACT_CONFIG.REJECTED;
          } else {
            attributeData.remark = CONSTANTS.RE_EXTRACT_CONFIG.ACCEPTED;
          }
        }
      });
    } else {
      if (!(attributeData.hasOwnProperty(key) && attributeData[key] &&
        parent.uncheckedAttributes.indexOf(parent.createCheckboxId(attributeData, level, index)) < 0)) {
        delete parentAttributes.attributes[index];
      } else if (key !== CONSTANTS.RE_EXTRACT_CONFIG.ISDELETED) {
        parent.mapNewValuesToCurrent(attributeData);
      }
    }
  }

  /**Created Id which is same as made in UI checkbox */
  public createCheckboxId(attribute: ReExtractedAttributeData, level: string, index: number): string {
    let id: string = '_' + attribute.attrNameTxt;
    if (attribute.attrNameCde || +attribute.attrNameCde === 0) {
      id = attribute.attrNameCde.toString() + '_' + attribute.attrNameTxt;
    }
    id = level + id;
    return id;
  }

  /**Used for removing the parent multiattribute if its inner attribute array is empty */
  private filterEmptyMultiAttributes(object: any): ReExtractedData {
    const parent = this;
    object.attributes.forEach(function (attributes, index) {
      if ((attributes.attrNameCde === CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE
        || attributes.attrNameCde === CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE_TABLE) && attributes.attributes.length < 1) {
        delete object.attributes[index];
      }
    });
    object.attributes = parent.filterNullInArray(object.attributes);
    return object;
  }

  private filterEmptyAttachments(object: ReExtractedData): ReExtractedData {
    const parent = this;
    if (object.hasOwnProperty('attachments')) {
      object.attachments.forEach(function (attributes, index) {
        if (attributes.attributes.length === 0) {
          delete object.attachments[index];
        }
      });
    }
    object.attachments = parent.filterNullInArray(object.attachments);
    return object;
  }

  /**This funcation will remove null object values from array of objects */
  private filterNullInArray(array) {
    let index = -1, resIndex = -1;
    const arrLength = array ? array.length : 0;
    const result = [];
    while (++index < arrLength) {
      const value = array[index];
      if (value) {
        result[++resIndex] = value;
      }
    }
    return result;
  }

  /**Have current and new object in it. To store it in DB need to update new attribute value to current attribute */
  private mapNewValuesToCurrent(attributes: ReExtractedAttributeData) {
    if (attributes.newAttrNameCde) {
      attributes.attrNameCde = attributes.newAttrNameCde;
    }
    attributes.attrValue = attributes.newAttrValue;
    attributes.confidencePct = CONSTANTS.ATTRIBUTES.CONFIDENCE_PCT.MAX;
    attributes.extractTypeCde = CONSTANTS.ATTRIBUTES.EXTRACT_TYPE_CDE.MANUALLY_CORRECTED;
  }

  /**Service call to DB save on Add attributes */
  private addToDataBase(documentData: DocumentData) {
    const parent = this;
    return new Promise<void>(function (fulfilled, rejected) {
      if ((documentData.attributes != null && documentData.attributes.length > 0) ||
        (documentData.attachments != null && documentData.attachments.length > 0)) {
        parent.attributeService.addAttributeData([documentData]).then(function (data) {
          if (data['responseCde'] === CONSTANTS.ERROR_CDE.MULTI_ATTRIBUTE_ALREADY_EXIST) {
            /** Have Commented the toastr as consolidated message needed */
            // parent.toastr.error(parent.msgInfo.getMessage(141));
            // return;
            rejected();
          } else {
            // parent.toastr.success(parent.msgInfo.getMessage(110));
            fulfilled();
          }
        }).catch(function (error) {
          // parent.toastr.error(parent.msgInfo.getMessage(111));
          rejected();
        });
      } else {
        fulfilled();
      }
    });
  }

  /**Service call to DB save on Update attributes */
  private updateToDataBase(documentData: DocumentData) {
    const parent = this;
    return new Promise<void>(function (fulfilled, rejected) {
      if ((documentData.attributes != null && documentData.attributes.length > 0) ||
        (documentData.attachments != null && documentData.attachments.length > 0)) {

        parent.attributeService.editAttributeData([documentData]).then(function (_data) {
          /** Have Commented the toastr as consolidated message needed */
          // parent.toastr.success(parent.msgInfo.getMessage(112));
          fulfilled();
        }).catch(function (error) {
          // parent.toastr.error(parent.msgInfo.getMessage(113));
          rejected();
        });
      } else {
        fulfilled();
      }
    });
  }

  /**Service call to DB save on Multi attributes  44/45*/
  private manageAttrToDataBase(documentData: ManageAttributeData) {
    const parent = this;
    return new Promise<void>(function (fulfilled, rejected) {
      if (documentData.attribute != null || documentData.attachment != null) {
        parent.attributeService.manageAttributeData(documentData).then(function (_data) {
          fulfilled();
        }).catch(function (_error) {
          rejected();
        });
      } else {
        fulfilled();
      }
    });
  }

  /**Service call to DB save on Delete attributes */
  private deleteToDataBase(documentData: DocumentData) {
    const parent = this;
    return new Promise<void>(function (fulfilled, rejected) {
      if ((documentData.attributes != null && documentData.attributes.length > 0) ||
        (documentData.attachments != null && documentData.attachments.length > 0)) {
        parent.attributeService.deleteAttributeData([documentData]).then(function (_data) {
          /** Have Commented the toastr as consolidated message needed */
          // parent.toastr.success(parent.msgInfo.getMessage(114));
          fulfilled();
        }).catch(function (error) {
          // parent.toastr.error(parent.msgInfo.getMessage(115));
          rejected();
        });
      } else {
        fulfilled();
      }
    });
  }

  /**Service call to DB update on action status and snapshot */
  private updateActionToDB(documentData: DocumentData[]) {
    const parent = this;
    return new Promise<void>(function (fulfilled, rejected) {
      if (documentData.length > 0) {
        parent.actionService.updateAction(documentData).then(function (_data) {
          // parent.toastr.success(parent.msgInfo.getMessage(142));
          fulfilled();
        }).catch(function (error) {
          // parent.toastr.error(parent.msgInfo.getMessage(143));
          rejected();
        });
      } else {
        fulfilled();
      }
    });
  }

  ngOnInit() {
  }

  closeWindow(isActionCompleted?: boolean) {
    this.close.emit('window closed');
    if (isActionCompleted) {
      this.dataService.publishReExtractActionCompleteEvent(true);
    }
  }

  open(content, currentJsonValue: string, previousJsonValue: string) {
    currentJsonValue = (!currentJsonValue) ? JSON.stringify({ '': '' }) : currentJsonValue;
    previousJsonValue = (!previousJsonValue) ? JSON.stringify({ '': '' }) : previousJsonValue;
    this.model.currentJsonValue = JSON.parse(currentJsonValue);

    this.model.previousJsonValue = JSON.parse(previousJsonValue);

    this.modalService.open(content, this.ngbModalOptions).result.then((result) => {
      this.closeResult = `Closed with: ${result}`;
    }, (reason) => {
      this.closeResult = `Dismissed ${this.getDismissReason(reason)}`;
    });

  }

  testJSON(item: any): boolean {
    return this.utilityService.testJSON(item);
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


}
