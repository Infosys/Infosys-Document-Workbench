/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Component, OnInit, Input, Output, EventEmitter, Injectable } from '@angular/core';
import { AttributeData } from '../../data/attribute-data';
import { ActionData } from '../../data/action-data';
import { ActionService } from '../../service/action.service';
import { DataService } from '../../service/data.service';
import { DocumentData } from '../../data/document-data';
import { ToastrService } from 'ngx-toastr';
import { NgbModalOptions } from '@ng-bootstrap/ng-bootstrap';
import { MessageInfo } from '../../utils/message-info';
import { CONSTANTS } from '../../common/constants';
import { UtilityService } from '../../service/utility.service';
import { AttachmentData } from '../../data/attachment-data';
import { BaseComponent } from '../../base.component';
import { SessionService } from '../../service/session.service';
import { ConfigDataHelper } from '../../utils/config-data-helper';
import { NiaTelemetryService } from '../../service/nia-telemetry.service';

@Injectable()
export class Documents {
  private docId: number;
  public get documentId(): number {
    return this.docId;
  }
  public set documentId(value: number) {
    this.docId = value;
  }
  private attributes: Attribute[];
  public get attribute(): Attribute[] {
    return this.attributes;
  }
  public set attribute(value: Attribute[]) {
    this.attributes = value;
  }
  private attachments: Attachment[];
  public get attachment(): Attachment[] {
    return this.attachments;
  }
  public set attachment(value: Attachment[]) {
    this.attachments = value;
  }
}

@Injectable()
export class Attribute {
  id: number;
  attrNameCde: number;
  attrNameTxt: String;
  attrValue: String;
  confidencePct: number;
  attributes: NestedAttribute[];
}

@Injectable()
export class NestedAttribute {
  id: number;
  attrNameTxt: String;
  attrValue: String;
  confidencePct: number;
  attributes: NestedAttribute[];
}

@Injectable()
export class Attachment {
  attachmentId: number;
  attributes: Attribute[];
}

@Injectable()
export class ParamValueData {
  document: Documents;
}


@Component({
  selector: 'app-re-extract-confirm',
  templateUrl: './re-extract-confirm.component.html',
  styleUrls: ['./re-extract-confirm.component.scss'],
  providers: [NestedAttribute, Attribute, Attachment, Documents, ParamValueData]
})
export class ReExtractConfirmComponent extends BaseComponent implements OnInit {
  getClassName(): string {
    return "ReExtractConfirmComponent";
  }
  @Input('minheight') minheight: number;
  @Output() close = new EventEmitter<string>();
  closeResult: string;
  documentData: DocumentData;
  attachAttrList: AttributeData[] = [];
  attachDataList: AttachmentData[] = [];
  attributeDataEditList: AttributeData[] = [];
  attributeDataList: AttributeData[] = [];
  paramValueData: ParamValueData;
  actionDataList: ActionData[];
  executeStatusMessage = '';
  areRowsSelected: boolean;
  isDataLoaded: boolean;
  isSaveAllowed: boolean;
  isEmailAttributeSelected: boolean;
  baseAttrDataList: AttributeData[] = [];

  constructor(private actionService: ActionService, private msgInfo: MessageInfo,
    private dataService: DataService, private toastr: ToastrService, private utilityService: UtilityService,
    public sessionService: SessionService, public configDataHelper: ConfigDataHelper, public niaTelemetryService:NiaTelemetryService) {
      super(sessionService, configDataHelper, niaTelemetryService);
     }
  @Input()
  set document(docData: DocumentData) {
    this.documentData = docData;
  }
  @Input()
  set isRowSelected(areRowsSelected: boolean) {
    this.areRowsSelected = areRowsSelected;
  }
  @Input()
  set isEmailAttrSelected(isEmailAttrSelected: boolean) {
    this.isEmailAttributeSelected = isEmailAttrSelected;
  }
  @Input()
  set attrEditList(attrEditDataList: AttributeData[]) {
    this.attributeDataEditList = attrEditDataList;
  }
  @Input()
  set baseAttrList(baseAttributeList: AttributeData[]) {
    this.baseAttrDataList = baseAttributeList;
  }
  @Input()
  set attrDataList(attrDataList: AttributeData[]) {
    this.attributeDataList = attrDataList;
  }
  @Input()
  set attachmentAttrDataList(attachAttrList: AttributeData[]) {
    this.attachAttrList = attachAttrList;
  }
  @Input()
  set attachmentDataList(attachmentDataList: AttachmentData[]) {
    this.attachDataList = attachmentDataList;
  }
  // For disabling mouse click and keystrokes outside modal window
  ngbModalOptions: NgbModalOptions = {
    backdrop: 'static',
    keyboard: false
  };

  ngOnInit() {
    const parent = this;
    parent.isSaveAllowed = false;
    parent.isDataLoaded = false;
    parent.actionService.getActionMapping(function (error, data) {
      if (!error) {
        const actionDataList: ActionData[] = data;
        parent.actionDataList = actionDataList;
        parent.checkForBaseAttribute();
        parent.isDataLoaded = true;
      }
    });
  }

  checkForBaseAttribute() {
    const parent = this;
    // parent.paramValueData = parent.getParamData();
    if (parent.areRowsSelected || parent.isEmailAttributeSelected) {
      parent.isSaveAllowed = true;
    } else {
      parent.isSaveAllowed = false;
    }
  }

  closeWindow() {
    this.close.emit('window closed');
  }

  cancelWindow() {
    this.close.emit(CONSTANTS.EVENT.CANCEL);
  }

  executeAction() {
    const parent = this;
    parent.paramValueData = parent.getParamData();
    const actionDataList: ActionData[] = parent.actionDataList.filter(a => a.actionNameCde === CONSTANTS.ACTION_NAME_CDE.RE_EXTRACT_DATA);
    const documentDataList: DocumentData[] = [];
    actionDataList[0].taskTypeCde = 2;
    if (parent.paramValueData != null) {
      const paramValue = JSON.stringify(parent.paramValueData);
      console.log(paramValue);
      actionDataList[0].mappingList[0].paramValue = paramValue;
    }
    documentDataList.push(parent.documentData);
    documentDataList[0].actionDataList = actionDataList;
    parent.actionService.saveAction(documentDataList, function (error, data) {
      if (!error) {
        // parent.executeStatusMessage = "Saved successfully";
        parent.dataService.publishDocActionAddedEvent(true);
        console.log('Save successful');
        parent.toastr.success(parent.msgInfo.getMessage(101));
        parent.closeWindow();
      } else {
        parent.executeStatusMessage = 'Error while saving data!!';
        console.log(error);
      }
    });
  }

  private getInnerAttributes(attrDataList: AttributeData[]): NestedAttribute[] {
    const nestedAttrDatas: NestedAttribute[] = [];
    if (attrDataList != null && attrDataList.length > 0) {
      attrDataList.forEach(data => {
        let nestedAttrData: NestedAttribute;
        nestedAttrData = new NestedAttribute();
        nestedAttrData.attrNameTxt = data.attrNameTxt;
        nestedAttrData.attrValue = data.attrValue;
        nestedAttrData.id = data.id;
        nestedAttrData.confidencePct = data.confidencePct;
        nestedAttrData.attributes = this.getInnerAttributes(data.attributes);
        nestedAttrDatas.push(nestedAttrData);
      });
    }
    return nestedAttrDatas;
  }

  private getAttributes(attrDataList: AttributeData[]): Attribute[] {
    const attrDatas: Attribute[] = [];
    for (let i = 0; i < attrDataList.length; i++) {
      let attrData: Attribute;
      let nestedAttrDatas: NestedAttribute[] = [];
      if (attrDataList[i].attrNameCde === CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE ||
        attrDataList[i].attrNameCde === CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE_TABLE) {
        nestedAttrDatas = this.getInnerAttributes(attrDataList[i].attributes);
      }
      attrData = new Attribute();
      if (attrDataList[i].id > 0) {
        attrData.id = attrDataList[i].id;
        attrData.attrNameCde = attrDataList[i].attrNameCde;
        attrData.attrNameTxt = attrDataList[i].attrNameTxt;
        attrData.attrValue = attrDataList[i].attrValue;
        attrData.confidencePct = attrDataList[i].confidencePct;
        attrData.attributes = nestedAttrDatas;
        attrDatas.push(attrData);
      }
    }
    if (attrDatas.length > 0) {
      return attrDatas;
    } else {
      return null;
    }
  }

  private getParamData(): ParamValueData {
    const parent = this;
    let attachmentAttributes = [];
    let docAttributes = [];
    if (parent.areRowsSelected) {
      attachmentAttributes = parent.attributeDataEditList.filter(data => data.attachmentId > 0);
      docAttributes = parent.attributeDataEditList.filter(data => data.attachmentId === undefined);
    }
    const attrMap = new Map<number, AttributeData[]>();
    for (let i = 0; i < attachmentAttributes.length; i++) {
      let attributeDataList: AttributeData[] = [];
      if (attrMap.has(attachmentAttributes[i].attachmentId)) {
        attributeDataList = attrMap.get(attachmentAttributes[i].attachmentId);
        if (attributeDataList.length > 0) {
          attrMap.delete(attachmentAttributes[i].attachmentId);
          attributeDataList.push(attachmentAttributes[i]);
          attrMap.set(attachmentAttributes[i].attachmentId, attributeDataList);
        }
      } else {
        attributeDataList.push(attachmentAttributes[i]);
        attrMap.set(attachmentAttributes[i].attachmentId, attributeDataList);
      }
    }
    const attachmentList = [];
    attrMap.forEach((value: AttributeData[], key: number) => {
      const attachmentData = new Attachment();
      attachmentData.attachmentId = key;
      const attachAttrMap = parent.getAttrMap(value);
      const tempAttrDataList1: AttributeData[] = [];
      let tempAttrDataList2: AttributeData[] = [];
      attachAttrMap.forEach((value1: AttributeData[], key1: number) => {
        if (value1.filter(attrData => (attrData.attrNameCde === CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE ||
          attrData.attrNameCde === CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE_TABLE)).length > 0) {
          // const attrData = parent.attachAttrList.filter(a => a.attributes.filter(b => b.id === key1)[0])[0];
          let attrData = null;
          parent.attachAttrList.forEach(a => {
            const attribute = a.attributes;
            if (attribute != null && attribute.length > 0) {
              attrData = attribute.filter(b => b.id === key1)[0];
            }
          });
          if (attrData != null) {
            tempAttrDataList1.push(attrData);
          }
        } else {
          tempAttrDataList1.push(value1[0]);
        }
      });
      if (tempAttrDataList1.filter(attrData => attrData.attrNameCde === CONSTANTS.ATTR_NAME_CDE.DOCUMENT_TYPE).length > 0) {
        tempAttrDataList2 = parent.attachAttrList.filter(a => a.attachmentId === key)[0].attributes;
      } else {
        tempAttrDataList2 = tempAttrDataList1;
      }
      tempAttrDataList2 = parent.getTemporaryListFromSelection(tempAttrDataList2);
      if (tempAttrDataList2 != null) {
        const tempAttrList = parent.getAttributes(tempAttrDataList2);
        if (tempAttrList != null) {
          attachmentData.attributes = tempAttrList;
        } else {
          attachmentData.attributes = [];
        }
      }
      attachmentList.push(attachmentData);
    });
    const documentData = new Documents();
    let attributeList = [];
    if (docAttributes != null) {
      const docAttrMap = parent.getAttrMap(docAttributes);
      const tempAttrDataList1: AttributeData[] = [];
      let tempAttrDataList2: AttributeData[] = [];
      docAttrMap.forEach((value: AttributeData[], key: number) => {
        if (value.filter(attrData => (attrData.attrNameCde === CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE ||
          attrData.attrNameCde === CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE_TABLE)).length > 0) {
          const attrData = parent.attributeDataList.filter(a => a.id === key)[0];
          tempAttrDataList1.push(attrData);
        } else {
          tempAttrDataList1.push(value[0]);
        }
      });
      if (tempAttrDataList1.filter(attrData => attrData.attrNameCde === CONSTANTS.ATTR_NAME_CDE.CATEGORY).length > 0) {
        tempAttrDataList2 = parent.attributeDataList;
      } else {
        tempAttrDataList2 = tempAttrDataList1;
      }
      tempAttrDataList2 = parent.getTemporaryListFromSelection(tempAttrDataList2);
      if (tempAttrDataList2 != null) {
        attributeList = parent.getAttributes(tempAttrDataList2);
      }
      if (attributeList === null && parent.isEmailAttributeSelected) {
        attributeList = [];
      }
    }
    if (attributeList != null) {
      documentData.attribute = attributeList;
    } else {
      documentData.attribute = null;
    }
    if (attachmentList != null) {
      documentData.attachment = attachmentList;
    }
    let paramValueData: ParamValueData;
    if (documentData != null) {
      paramValueData = new ParamValueData();
      paramValueData.document = documentData;
    }
    return paramValueData;
  }

  // Method is for mapping the relId with the attribute data.
  getAttrMap(attributeList: AttributeData[]): Map<number, AttributeData[]> {
    const attrMap = new Map<number, AttributeData[]>();
    for (let i = 0; i < attributeList.length; i++) {
      let attributeDataList: AttributeData[] = [];
      if (attrMap.has(attributeList[i].id)) {
        attributeDataList = attrMap.get(attributeList[i].id);
        if (attributeDataList.length > 0) {
          attrMap.delete(attributeList[i].id);
          attributeDataList.push(attributeList[i]);
          attrMap.set(attributeList[i].id, attributeDataList);
        }
      } else {
        attributeDataList.push(attributeList[i]);
        attrMap.set(attributeList[i].id, attributeDataList);
      }
    }
    return attrMap;
  }

  private getTemporaryListFromSelection(tempAttrList: AttributeData[]): AttributeData[] {
    const parent = this;
    const attrDataList = parent.utilityService.createDuplicateList(tempAttrList);
    if (parent.utilityService.isListHasValue(parent.baseAttrDataList)) {
      if (parent.baseAttrDataList.filter(data => !data.optionChecked).length > 0) {
        for (let i = 0; i < parent.baseAttrDataList.length; i++) {
          if (!parent.baseAttrDataList[i].optionChecked) {
            const index = attrDataList.findIndex(data => data.id === parent.baseAttrDataList[i].id);
            if (index !== -1) {
              attrDataList.splice(index, 1);
              return attrDataList;
            }
          }
        }
      }
    }
    return attrDataList;
  }
}

