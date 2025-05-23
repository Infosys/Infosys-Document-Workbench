/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Component, OnInit, Input, OnChanges, SimpleChange } from '@angular/core';
import { ToastrService } from 'ngx-toastr';
import { ActionService } from '../../service/action.service';
import { RecommendedActionService } from '../../service/recommended-action.service';
import { DataService } from '../../service/data.service';
import { DocumentData } from '../../data/document-data';
import { AttributeData } from '../../data/attribute-data';
import { ActionData } from '../../data/action-data';
import { RecommendedActionData } from '../../data/recommended-action-data';
import { MessageInfo } from '../../utils/message-info';
import { AttributeHelper } from '../../utils/attribute-helper';
import { AttributeService } from '../../service/attribute.service';
import { CONSTANTS } from '../../common/constants';
import { UtilityService } from '../../service/utility.service';
// import { ParamAttrMappingData } from '../../data/param-attr-mapping-data';

@Component({
  selector: 'app-action-recommended',
  templateUrl: './action-recommended.component.html',
  styleUrls: ['./action-recommended.component.scss']
})
export class ActionRecommendedComponent implements OnInit, OnChanges {

  constructor(private actionService: ActionService, private dataService: DataService,
    private recommendedActionService: RecommendedActionService, private toastr: ToastrService, private msgInfo: MessageInfo,
    private attributeService: AttributeService, private attributeHelper: AttributeHelper, private utilityService: UtilityService) { }
  document: DocumentData;
  recommendedActionData: RecommendedActionData;
  attributeDataList: AttributeData[];
  attrValue = '';
  attrConfidence = 0;
  actionDataList: ActionData[] = [];
  attrNameCdeList: number[] = [];
  actionNameCdeSelected: number = null;
  actionNameTxt: string;
  executeStatusMessage = '';
  actionData: ActionData;
  isDataLoaded = false;
  private _isDataReady: boolean;
  isBtnActionClicked: boolean;
  attachmentAttrDataList: AttributeData[];
  attrNameTxtList: string[] = [];
  attrValueList: string[] = [];
  attrConfidencePctList: number[] = [];

  @Input()
  set documentData(docData: DocumentData) {

  }

  @Input()
  set isDataReady(isDataReady: boolean) {
    this._isDataReady = isDataReady;
  }

  ngOnChanges(changes: { [propKey: string]: SimpleChange }) {
    const parent = this;
    if (changes['documentData']) {
      if (this._isDataReady) {
        const to = changes['documentData'].currentValue;
        parent.document = to;
        parent.actionNameCdeSelected = null;
        parent.actionData = null;
        parent.isDataLoaded = false;
        parent.attrNameCdeList = [];
        if (parent.document != null) {
          parent.getActionList();
        }
      }
    }
  }

  ngOnInit() {

  }

  getActionCde() {
    const parent = this;
    this.recommendedActionService.getRecommendedAction(parent.document.docId, function (error, data) {
      if (!error && data != null) {
        parent.recommendedActionData = data;
        if (parent.attrConfidence >= parent.recommendedActionData.recommendedPct
          && parent.attrConfidence < parent.recommendedActionData.confidencePct) {
          parent.actionNameCdeSelected = parent.recommendedActionData.actionNameCde;
          if (parent.actionNameCdeSelected != null) {
            parent.getAction();
          }
        }
      }
      parent.isDataLoaded = true;
    });
  }

  getAction() {
    const parent = this;
    this.actionService.getActionMapping(function (error, data) {
      if (!error) {
        const actionDataList: ActionData[] = data;
        parent.recommendedActionData.actionDataList = [];
        for (let i = 0; i < actionDataList.length; i++) {
          if (actionDataList[i].actionNameCde === parent.actionNameCdeSelected) {
            if (actionDataList[i].mappingList[0] === undefined) {
              parent.actionData = actionDataList[i];
              parent.actionNameTxt = actionDataList[i].actionNameTxt;
              parent.recommendedActionData.actionDataList.push(parent.actionData);
            } else if (parent.attrNameCdeList.includes(actionDataList[i].mappingList[0].attrNameCde)) {
              parent.actionData = actionDataList[i];
              parent.actionNameTxt = actionDataList[i].actionNameTxt;
              parent.recommendedActionData.actionDataList.push(parent.actionData);
            }
          }
        }
      }
    });
  }

  executeAction() {
    if (this.isBtnActionClicked) {
      return;
    }
    const parent = this;
    parent.actionService.getActions(parent.document.docId, function (getActionServiceError, getActionServiceData) {
      let tempDataList = [];
      if (!getActionServiceError && getActionServiceData[0] !== undefined) {
        tempDataList = getActionServiceData[0].actionDataList;
      }
      if (parent.utilityService.isReExtractPending(tempDataList)) {
        parent.toastr.error(parent.msgInfo.getMessage(148));
        parent.isDataLoaded = true;
      } else {
        parent.recommendedActionData.actionDataList[0].taskTypeCde = 2;
        parent.isDataLoaded = false;
        for (let i = 0; i < parent.recommendedActionData.actionDataList[0].mappingList.length; i++) {
          const mappingListObj = parent.recommendedActionData.actionDataList[0].mappingList[i];
          mappingListObj.paramValue = parent.attrValueList[parent.attrNameTxtList.indexOf(mappingListObj.attrNameTxt)];
        }
        const documentDataList: DocumentData[] = [];
        documentDataList.push(parent.document);
        documentDataList[0].actionDataList = parent.recommendedActionData.actionDataList;

        // console.log(JSON.stringify(dataToSave));
        this.actionService.saveAction(documentDataList, function (error, data) {
          if (!error) {
            // parent.executeStatusMessage = "Saved successfully";
            parent.dataService.publishDocActionAddedEvent(true);
            console.log('Save successful');
            parent.toastr.success(parent.msgInfo.getMessage(101));
            parent.actionNameTxt = null;
            parent.isDataLoaded = true;
          } else {
            parent.toastr.error(parent.msgInfo.getMessage(102));
            console.log(error);
            parent.isDataLoaded = true;
          }

        });
      }
      this.isBtnActionClicked = true;
    });

  }

  /*------------------------------PRIVATE METHODS-------------------------------*/

  private getActionList() {
    const parent = this;
    const docId = parent.document.docId;
    parent.actionService.getActions(docId, function (error, data) {
      parent.actionDataList = [];
      parent.actionNameTxt = null;
      if (!error) {
        parent.actionDataList = data;
        let flag = false;
        if (!(parent.utilityService.isListHasValue(parent.actionDataList))) {
          flag = true;
        } else if (data[0].actionDataList.filter(documentData =>
          documentData.actionNameCde !== CONSTANTS.ACTION_NAME_CDE.RE_EXTRACT_DATA).length === 0) {
          flag = true;
        }
        if (flag) {
          parent.attributeService.getDocumentAttributes(docId).then(function (docAttrData) {
            if (docAttrData) {
              const attributeDataList = docAttrData as AttributeData[];
              parent.attributeDataList = parent.attributeHelper.filteredAttrDataList(attributeDataList,false);
              parent.attributeHelper.generateList(parent.attributeDataList, parent.attrNameCdeList, parent.attrNameTxtList,
                parent.attrValueList, parent.attrConfidencePctList);
            }
            parent.fetchAttachmentAttributes(docId);
          }).catch(function (docAttrError) {
            parent.attributeDataList = [];
          });
        } else {
          parent.isDataLoaded = true;
        }
      }
    });
  }

  // To get attachment attributes
  private fetchAttachmentAttributes(docId: number) {
    const parent = this;
    parent.attributeService.getAttachmentAttributes(docId).then(function (data) {
      if (data) {
        parent.attachmentAttrDataList = data as AttributeData[];
        parent.attachmentAttrDataList.forEach(function (attrData) {
          attrData.attributes = parent.attributeHelper.filteredAttrDataList(attrData.attributes,false);
          parent.attributeHelper.generateList(attrData.attributes, parent.attrNameCdeList, parent.attrNameTxtList,
            parent.attrValueList, parent.attrConfidencePctList);
        });
      }
    })
      .catch(function (error) {
        parent.attachmentAttrDataList = [];
      });
    let index;
    if (parent.attrNameCdeList.includes(CONSTANTS.ATTR_NAME_CDE.CATEGORY)) {
      index = parent.attrNameCdeList.indexOf(CONSTANTS.ATTR_NAME_CDE.CATEGORY);
    } else if (parent.attrNameCdeList.includes(CONSTANTS.ATTR_NAME_CDE.DOCUMENT_TYPE)) {
      index = parent.attrNameCdeList.indexOf(CONSTANTS.ATTR_NAME_CDE.DOCUMENT_TYPE);
    }
    if (index !== undefined) {
      parent.attrValue = parent.attrValueList[index];
      parent.attrConfidence = parent.attrConfidencePctList[index];
    }
    if (parent.attrValue != null && parent.attrValue.length > 0) {
      parent.getActionCde();
    } else {
      parent.isDataLoaded = true;
    }
  }

}
