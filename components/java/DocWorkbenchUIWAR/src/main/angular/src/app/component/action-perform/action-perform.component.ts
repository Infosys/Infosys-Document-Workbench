/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Component, OnInit, Output, Input, EventEmitter, OnChanges, SimpleChange } from '@angular/core';
import { ToastrService } from 'ngx-toastr';
import { ActionService } from '../../service/action.service';
import { DataService } from '../../service/data.service';
import { DocumentData } from '../../data/document-data';
import { AttributeData } from '../../data/attribute-data';
import { ActionData } from '../../data/action-data';
import { RecommendedActionService } from '../../service/recommended-action.service';
import { ParamAttrMappingData } from '../../data/param-attr-mapping-data';
import { MessageInfo } from '../../utils/message-info';
import { AttributeService } from '../../service/attribute.service';
import { CONSTANTS } from '../../common/constants';
import { AttributeHelper } from '../../utils/attribute-helper';
import { UtilityService } from '../../service/utility.service';
import { BaseComponent } from '../../base.component';
import { SessionService } from '../../service/session.service';
import { ConfigDataHelper } from '../../utils/config-data-helper';
import { DocumentService } from '../../service/document.service';
import { NiaTelemetryService } from '../../service/nia-telemetry.service';
import { DocumentUserData } from '../../data/document-user-data';

@Component({
  selector: 'app-action-perform',
  templateUrl: './action-perform.component.html',
  styleUrls: ['./action-perform.component.scss']
})
export class ActionPerformComponent extends BaseComponent implements OnInit, OnChanges {
  getClassName(): string {
    return "ActionPerformComponent";
  }
  constructor(private actionService: ActionService, private attributeService: AttributeService,
    private dataService: DataService, private recommendedActionService: RecommendedActionService,
    private toastr: ToastrService, private msgInfo: MessageInfo, private attributeHelper: AttributeHelper,
    private utilityService: UtilityService, public sessionService: SessionService,
    public configDataHelper: ConfigDataHelper, public niaTelemetryService: NiaTelemetryService,
    private documentService: DocumentService) {
    super(sessionService, configDataHelper, niaTelemetryService);
  }

  @Output() close = new EventEmitter<string>();
  @Output() isBtnShowActionEnabled = new EventEmitter<boolean>();
  isBtnExecuteClicked = false;
  isFormValid = false;
  isDataLoaded = false;
  actionDataList: ActionData[];
  paramAttrMappingDataList: ParamAttrMappingData[];
  document: DocumentData;
  attributeDataList: AttributeData[];
  attrDataList: AttributeData[] = [];
  attrValueList: string[] = [];
  attrNameCdeList: number[] = [];
  attrNameTxtList: string[] = [];
  actionNameCdeSelected = 1;
  attrNameCdeSelectedList: number[] = [];
  attrNameTxtSelectedList: string[] = [];
  attrValueSelectedList: string[] = [];
  executeStatusMessage = '';
  actionDataList1: ActionData[] = [];
  private _isDataReady: boolean;
  attachmentAttrDataList: AttributeData[] = [];

  @Input()
  set docUserDataList(docUserDataList:DocumentUserData[]){
    this.model.docUserDataList = docUserDataList
  }
  @Input()
  set documentData(docData: DocumentData) {
  }

  @Input()
  set isDataReady(isDataReady: boolean) {
    this._isDataReady = isDataReady;
  }

  model:any={
    docUserDataList: undefined
  }

  ngOnChanges(changes: { [propKey: string]: SimpleChange }) {
    this.attrNameCdeList = [];
    this.attrNameTxtList = [];
    this.attrValueList = [];
    const parent = this;
    if (changes['documentData']) {
      if (parent._isDataReady) {
        const to = changes['documentData'].currentValue;
        parent.document = to;
        if (parent.document != null) {
          const docId = parent.document.docId;
          parent.attributeService.getDocumentAttributes(docId).then(function (data) {
            if (data) {
              const attributeDataList = data as AttributeData[];
              parent.attributeDataList = parent.attributeHelper.filteredAttrDataList(attributeDataList,false);
              parent.attributeHelper.generateList(parent.attributeDataList, parent.attrNameCdeList, parent.attrNameTxtList,
                parent.attrValueList);
            }

            parent.fetchAttachmentAttributes(docId);
            parent.isDataLoaded = false;
          }).catch(function (error) {
            parent.attributeDataList = [];
          });
        } else {
          parent.isDataLoaded = true;
        }
      }
    }
  }

  ngOnInit() {
  }

  closeWindow() {
    this.isBtnShowActionEnabled.emit(false);
    this.close.emit('window closed');
  }

  executeAction() {
    // console.log(this.attrNameCdeSelectedList);
    if (this.isBtnExecuteClicked) {
      return;
    }
    const parent = this;
    parent.isBtnExecuteClicked = true;
    parent.actionService.getActions(parent.document.docId, function (getActionServiceError, getActionServiceData) {
      let tempDataList = [];
      if (getActionServiceError) {
        parent.isBtnExecuteClicked = false;
      }
      if (!getActionServiceError && getActionServiceData[0] !== undefined) {
        tempDataList = getActionServiceData[0].actionDataList;
      }
      if (parent.utilityService.isReExtractPending(tempDataList)) {
        parent.toastr.error(parent.msgInfo.getMessage(148));
        parent.closeWindow();
      } else {
        const actionDataList: ActionData[] = parent.actionDataList.filter(function (p) {
          if (p.actionNameCde === +parent.actionNameCdeSelected) {
            return true;
          }
          return false;
        });
        actionDataList[0].taskTypeCde = 2;
        actionDataList[0].mappingList = parent.paramAttrMappingDataList;
        for (let i = 0; i < parent.paramAttrMappingDataList.length; i++) {
          // Copy values from selected list to dataToSave object
          actionDataList[0].mappingList[i].attrNameCde = parent.attrNameCdeSelectedList[i];
          actionDataList[0].mappingList[i].attrNameTxt = parent.attrNameTxtSelectedList[i];
          actionDataList[0].mappingList[i].paramValue = parent.attrValueSelectedList[i];
        }
        const documentDataList: DocumentData[] = [];
        documentDataList.push(parent.document);
        documentDataList[0].actionDataList = actionDataList;

        parent.actionService.saveAction(documentDataList, function (error, data) {
          if (!error) {
            // parent.executeStatusMessage = "Saved successfully";
            parent.dataService.publishDocActionAddedEvent(true);
            console.log('Save successful');
            parent.toastr.success(parent.msgInfo.getMessage(101));
            let cdata=parent.attributeHelper.getTelemetryEventActionParams(parent.document, parent.attachmentAttrDataList, parent.model.docUserDataList) as [];
            const intractId = parent.bmodel.TELEID.ACT_PER.EXECUTE + '_' + parent.getSelectedActionName();
            parent.triggerTelemetryEvents(parent.bmodel.TELE_EVENTS.INTERACT, intractId, cdata = cdata);
            parent.closeWindow();
          } else {
            parent.executeStatusMessage = 'Error while saving data!!';
            parent.isBtnExecuteClicked = false;
            console.log(error);
          }
        });
      }
      parent.isBtnShowActionEnabled.emit(false);
    });
  }

  updateItemSelected(itemIndex: number, selectedIndex: number) {
    this.attrNameCdeSelectedList[itemIndex] = this.attrNameCdeList[selectedIndex];
    this.attrNameTxtSelectedList[itemIndex] = this.attrNameTxtList[selectedIndex];
    this.attrValueSelectedList[itemIndex] = this.attrValueList[selectedIndex];
    this.checkIfFormIsValid();
  }



  showParameters() {
    // console.log("apply filter");

    const parent = this;
    const filteredData = parent.actionDataList.filter(function (p) {
      if (p.actionNameCde === +parent.actionNameCdeSelected) {
        return true;
      }
      return false;
    });
    // console.log(filteredData);

    if (filteredData.length > 0) {
      parent.paramAttrMappingDataList = filteredData[0].mappingList;
      parent.attrNameCdeSelectedList = []; // Clear Array
      parent.attrNameTxtSelectedList = []; // Clear Array
      parent.attrValueSelectedList = []; // Clear Array
      const paramAttrMappingDataListLength = parent.paramAttrMappingDataList.length;
      parent.attrNameCdeSelectedList.length = paramAttrMappingDataListLength;
      parent.attrNameTxtSelectedList.length = paramAttrMappingDataListLength;
      parent.attrValueSelectedList.length = paramAttrMappingDataListLength;

      if (parent.paramAttrMappingDataList != null && paramAttrMappingDataListLength > 0) {
        // Assume form is valid
        parent.isFormValid = true;
        // console.log(this.attrNameCdeSelectedList);
        for (let i = 0; i < paramAttrMappingDataListLength; i++) {
          const paramAttrMappingDataListAttrNameCde = parent.paramAttrMappingDataList[i].attrNameCde;
          const paramAttrMappingDataListAttrNameTxt = parent.paramAttrMappingDataList[i].attrNameTxt;
          if (parent.attrNameCdeList.includes(paramAttrMappingDataListAttrNameCde) &&
            parent.attrNameTxtList.includes(paramAttrMappingDataListAttrNameTxt)) {
            parent.attrNameCdeSelectedList[i] = paramAttrMappingDataListAttrNameCde;
            parent.attrNameTxtSelectedList[i] = paramAttrMappingDataListAttrNameTxt;
            parent.attrValueSelectedList[i] = parent.attrValueList[parent.attrNameTxtList.indexOf(paramAttrMappingDataListAttrNameTxt)];
          }
        }
      }

    }
    parent.isDataLoaded = true;
    parent.checkIfFormIsValid();
    parent.isDataLoaded = true;
  }

  getSelectedActionName() {
    const parent = this;
    const filteredData = parent.actionDataList.filter(function (p) {
      if (p.actionNameCde === +parent.actionNameCdeSelected) {
        return true;
      }
      return false;
    });
    return (filteredData.length > 0 ? filteredData[0]["actionNameTxt"] : "").split(" ").join("_").toUpperCase();
  }


  /************************* PRIVATE METHODS *************************/
  private checkIfFormIsValid() {
    // console.log("checkIfFormIsValid");
    if (this.paramAttrMappingDataList != null && this.paramAttrMappingDataList.length > 0) {
      // Assume form is valid
      this.isFormValid = true;
      // console.log(this.attrNameCdeSelectedList);
      for (let i = 0; i < this.paramAttrMappingDataList.length; i++) {
        // console.log(this.attrNameCdeSelectedList[i]);

        if (this.attrNameCdeSelectedList[i] === undefined || this.attrNameCdeSelectedList[i] <= 0) {
          this.isFormValid = false;
          break;
        }
      }
    } else {
      this.isFormValid = true;
    }
  }

  private getActionMapping() {
    const parent = this;
    parent.actionService.getActionMapping(function (error, data) {
      if (!error) {
        const actionDataList: ActionData[] = data;
        let filterActionsList = new Set([CONSTANTS.ACTION_NAME_CDE.RE_EXTRACT_DATA,
                                     CONSTANTS.ACTION_NAME_CDE.DATA_ENTRY_COMPLETE,
                                     CONSTANTS.ACTION_NAME_CDE.DATA_ENTRY_APPROVE,
                                     CONSTANTS.ACTION_NAME_CDE.DATA_ENTRY_REJECT
                                    ])
        parent.actionDataList = actionDataList.filter(function (item) {
          if (filterActionsList.has(item.actionNameCde))
            return false;
          return true;
        })
        parent.showParameters();
      }
    });
  }

  private getActionCde() {
    const parent = this;
    if (parent.attrNameCdeList.includes(CONSTANTS.ATTR_NAME_CDE.CATEGORY) ||
      parent.attrNameCdeList.includes(CONSTANTS.ATTR_NAME_CDE.DOCUMENT_TYPE)) {
      parent.recommendedActionService.getRecommendedAction(parent.document.docId, function (error, data) {
        if (!error && data != null) {
          parent.actionNameCdeSelected = data.actionNameCde;
        } else {
          parent.actionNameCdeSelected = 0;
        }
        parent.getActionMapping();
      });
    }
  }

  // To get attachment attributes
  private fetchAttachmentAttributes(docId: number) {
    const parent = this;
    parent.attributeService.getAttachmentAttributes(docId).then(function (data) {
      if (data) {
        parent.attachmentAttrDataList = data as AttributeData[];
        parent.attachmentAttrDataList.forEach(function (attrData) {
          attrData.attributes = parent.attributeHelper.filteredAttrDataList(attrData.attributes,false);
          parent.attributeHelper.generateList(attrData.attributes, parent.attrNameCdeList, parent.attrNameTxtList, parent.attrValueList);
        });
      }

      if (parent.attrNameCdeList.includes(CONSTANTS.ATTR_NAME_CDE.CATEGORY) ||
        parent.attrNameCdeList.includes(CONSTANTS.ATTR_NAME_CDE.DOCUMENT_TYPE)) {
        parent.getActionCde();
      } else {
        parent.getActionMapping();
      }
    })
      .catch(function (error) {
        parent.attachmentAttrDataList = [];
      });
  }
}
