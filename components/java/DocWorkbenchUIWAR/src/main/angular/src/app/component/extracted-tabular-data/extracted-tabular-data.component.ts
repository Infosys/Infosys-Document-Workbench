/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { AttachmentAttributeData } from '../../data/attachment-attribute-data';
import { ManageAttributeData } from '../../data/manage-attribute-data';
import { AttributeService } from '../../service/attribute.service';
import { ToastrService } from 'ngx-toastr';
import { MessageInfo } from '../../utils/message-info';

import { DocumentData } from '../../data/document-data';
import { ManageAttributeDataList } from '../../data/manage-attribute-data-list';
import { ManageAttachmentAttributeDataList } from '../../data/manage-attachment-attribute-data-list';
import { CONSTANTS } from '../../common/constants';
import { AttributeHelper } from '../../utils/attribute-helper';
import { UtilityService } from '../../service/utility.service';
import { BaseComponent } from '../../base.component';
import { SessionService } from '../../service/session.service';
import { ConfigDataHelper } from '../../utils/config-data-helper';
import { NiaTelemetryService } from '../../service/nia-telemetry.service';

@Component({
  selector: 'app-extracted-tabular-data',
  templateUrl: './extracted-tabular-data.component.html',
  styleUrls: ['./extracted-tabular-data.component.scss']
})
export class ExtractedTabularDataComponent extends BaseComponent implements OnInit {
  getClassName(): string {
    return "ExtractedTabularDataComponent";
  }
  constructor(private attributeService: AttributeService, private toastr: ToastrService, private msgInfo: MessageInfo,
    private attributeHelper: AttributeHelper, private utilityService: UtilityService, public sessionService: SessionService,
    public configDataHelper: ConfigDataHelper, public niaTelemetryService:NiaTelemetryService) {
      super(sessionService, configDataHelper, niaTelemetryService)
     }
  model: any = {
    isDataSaved: true,
    minheight: Number,
    isSaveClicked: false,
    popupHeader: String,
    closeButton: Boolean,
    isHeaderInEditMode: false,
    isReadOnlyTabularView: false,
    readOnlyTabularTxtValue: String
  }; // For binding to view
  private documentData: DocumentData;
  @Output() close = new EventEmitter<string>();
  @Input() popupButton = true;
  @Input() attachmentAttrDataList: AttachmentAttributeData[];
  @Input()
  set isReadOnlyTabularView(isReadOnlyTabularView: boolean) {
    this.model.isReadOnlyTabularView = isReadOnlyTabularView;
  }
  @Input()
  set readOnlyTabularTxtCde(readOnlyTabularTxtCde: number) {
    this.model.readOnlyTabularTxtValue = this.msgInfo.getMessage(readOnlyTabularTxtCde);
  }
  @Input() isRowSelectAllowed: boolean;
  @Input()
  set document(document: DocumentData) {
    this.documentData = document;
  }
  @Input()
  set minheight(minheight: number) {
    this.model.minheight = minheight;
  }
  @Input()
  set closeButton(closeButton: number) {
    this.model.closeButton = closeButton;
  }
  @Input() currentTableColOrderMap: [{ 'colOrderRegExp': [], 'colNames': [] }];

  private tabularAttrData: AttachmentAttributeData[] = [];
  private tabularAttrDataTmp: AttachmentAttributeData[] = [];
  private tabularAttrDataRefresh: AttachmentAttributeData[] = [];
  private tableId = 'tabular-data';
  private appendTableTo = 'tabular-data-div';
  private headerCols = [];
  private jsonData: any;
  private isAllRowDeleted: boolean;
  private isResetRequired = false;

  ngOnInit() {
    const parent = this;
    parent.model.isSaveClicked = !parent.isRowSelectAllowed;
    parent.model.isHeaderInEditMode = false;
    parent.tabularAttrData = parent.attachmentAttrDataList;
    parent.sortAttributesToCustomOrder().then(() => {
      parent.tabularAttrDataTmp = JSON.parse(JSON.stringify(parent.tabularAttrData));
      parent.tabularAttrDataRefresh = JSON.parse(JSON.stringify(parent.tabularAttrData));
      parent.model.popupHeader = parent.tabularAttrDataRefresh[0].attrNameTxt;
      parent.prepareAndCreateTable();
    });
  }

  closeWindow(msg?: string) {
    this.close.emit(msg ? msg : CONSTANTS.EVENT.CANCEL);
  }

  /** This method used for saving the modified data from popup to DB */
  saveEventCall() {
    const parent = this;
    parent.model.isDataSaved = false;
    parent.model.isSaveClicked = true;
    let updatedAttrDataList: any;
    let addedAttrDataList: any;
    let deletedAttrDataList: any;
    parent.tabularAttrData.forEach(attributes =>
      updatedAttrDataList = attributes.attributes.filter(rowAttributes => rowAttributes.isUpdated === true));
    parent.tabularAttrData.forEach(attributes =>
      addedAttrDataList = attributes.attributes.filter(rowAttributes => rowAttributes.isAdded === true));
    parent.tabularAttrData.forEach(attributes =>
      deletedAttrDataList = attributes.attributes.filter(rowAttributes => rowAttributes.isDeleted === true));
    if (parent.isSaveAllowed(updatedAttrDataList, addedAttrDataList, deletedAttrDataList)) {
      const attrData = parent.tabularAttrData[0];
      const consolidatedAttrDataList = new ManageAttributeDataList(attrData.id, attrData.attrNameCde, attrData.attrNameTxt,
        attrData.attrValue, attrData.extractTypeCde, attrData.extractTypeTxt, attrData.confidencePct, addedAttrDataList,
        updatedAttrDataList, deletedAttrDataList);
      const consolidatedAttachAttrDataList = new ManageAttachmentAttributeDataList(attrData.attachmentId, consolidatedAttrDataList);
      const consolidateAttrData = this.convertToManageAttributeData(new ManageAttributeData(parent.documentData.docId,
        -1, null, consolidatedAttachAttrDataList));
      parent.saveDataToDataBase(consolidateAttrData);
    }
  }

  private isSaveAllowed(updatedAttrDataList, addedAttrDataList, deletedAttrDataList) {
    const parent = this;
    let isAllowed = true;
    if (parent.checkAnyEditedRow() || parent.checkIsLastRowEmpty() || parent.checkIsHeaderInEditMode()) {
      parent.model.isDataSaved = true;
      parent.model.isSaveClicked = false;
      parent.toastr.error(parent.msgInfo.getMessage(161));
      isAllowed = false;
    } else if (updatedAttrDataList.length === 0 && addedAttrDataList.length === 0 && deletedAttrDataList.length === 0) {
      parent.model.isDataSaved = true;
      parent.model.isSaveClicked = false;
      parent.toastr.error(parent.msgInfo.getMessage(152));
      isAllowed = false;
    }
    return isAllowed;
  }

  private sortAttributesToCustomOrder() {
    const parent = this;
    return new Promise<void>((fulfilled, rejected) => {
      const headerNameOrder = parent.getCustomOrderToTableName();
      if (headerNameOrder.length > 0) {
        parent.tabularAttrData[0].attributes.forEach(attr => parent.sortAttributes(attr, headerNameOrder));
      }
      fulfilled();
    });
  }

  private getCustomOrderToTableName() {
    let headerNameOrder = [];
    if (this.currentTableColOrderMap) {
      this.currentTableColOrderMap.some(colOrderData => {
        return colOrderData.colOrderRegExp.some((regExp: RegExp) => {
          const tableName = this.attachmentAttrDataList[0].attrNameTxt;
          if (regExp.test(tableName)) {
            headerNameOrder = colOrderData.colNames;
            return true;
          }
        });
      });
    }
    return headerNameOrder;
  }

  private getPositiveIndex(elements: any[], element: any): number {
    let index = elements.indexOf(element);
    if (index === -1) {
      elements.push(element);
      index = elements.indexOf(element);
    }
    return index;
  }

  private sortAttributes(attr, sortOrder) {
    const matchTxt = 'attrNameTxt';
    attr.attributes.sort((current, next) => {
      if (this.getPositiveIndex(sortOrder, current[matchTxt].trim()) > this.getPositiveIndex(sortOrder, next[matchTxt].trim())) {
        return 1;
      } else {
        return -1;
      }
    });
  }

  private prepareAndCreateTable() {
    const parent = this;
    parent.headerCols = [];
    parent.isAllRowDeleted = false;
    let data: any = [];
    if (parent.isResetRequired) {
      parent.tabularAttrData = JSON.parse(JSON.stringify(parent.tabularAttrDataRefresh)) as AttachmentAttributeData[];
      parent.tabularAttrDataTmp = JSON.parse(JSON.stringify(parent.tabularAttrDataRefresh)) as AttachmentAttributeData[];
      parent.isResetRequired = false;
    }
    let temp: any = [];
    parent.tabularAttrData.forEach(attributes =>
      temp = attributes.attributes.filter(innerAttr => innerAttr.isDeleted === false)
    );
    if (temp.length > 0) {
      parent.jsonData = parent.generateJsonFromDBData(parent.tabularAttrData);
    } else {
      parent.jsonData = [];
    }
    parent.jsonData = parent.generateJsonFromDBData(parent.tabularAttrData);
    if (parent.jsonData.length === 0) {
      parent.isAllRowDeleted = true;
      data = parent.generateJsonFromDBData(parent.tabularAttrDataTmp);
    } else {
      data = parent.jsonData;
    }
    parent.headerCols = parent.generateHeadersFromJsonData(data);
    parent.createTable(parent.jsonData, parent.headerCols);
    parent.model.isHeaderInEditMode = false;
  }

  /** This method used for generating the rows from DB data*/
  private generateJsonFromDBData(attrDataList: AttachmentAttributeData[]): any[] {
    const rows = [];
    attrDataList.forEach(element => {
      element.attributes.forEach(innerElement => {
        const columns = {};
        columns['ID'] = innerElement.id as number;
        innerElement.attributes.forEach(attribute => {
          columns[attribute.id + '_' + attribute.attrNameTxt] = attribute.attrValue;
        });
        if (!innerElement.isDeleted) {
          rows.push(columns);
        }
      });
    });
    // console.log('JSON rows');
    // console.log(rows);
    return rows;
  }

  /** This method used for generating the headers from DB data*/
  private generateHeadersFromJsonData(data: any[]) {
    const cols = [];
    for (let i = 0; i < data.length; i++) {
      for (const key in data[i]) {
        if (cols.indexOf(key) === -1) {
          cols.push(key);
        }
      }
    }
    return cols;
  }

  private convertToManageAttributeData(manageAttributeData: ManageAttributeData): ManageAttributeData {
    this.attributeHelper.removeAttributeDataOptionFields(manageAttributeData);
    return JSON.parse(JSON.stringify(manageAttributeData)) as ManageAttributeData;
  }

  private checkAnyEditedRow(): boolean {
    let isEdited = false;
    for (let row = 1; row <= this.jsonData.length; row++) {
      const tab = (<HTMLTableElement>document.getElementById(this.tableId)).rows[row];
      const cellCount = this.headerCols.length - 1;
      for (let i = 0; i < cellCount; i++) {
        const cell: any = tab.getElementsByTagName('td')[i];
        if (cell.children.length > 0) {
          isEdited = true;
          break;
        }
      }
      if (isEdited) {
        break;
      }
    }
    return isEdited;
  }

  private checkIsHeaderInEditMode(): boolean {
    let isEdited = false;
    const tab = (<HTMLTableElement>document.getElementById(this.tableId)).rows[0];
    const cell: any = tab.getElementsByTagName('th')[this.headerCols.length - 2];
    if (cell.children.length > 0) {
      isEdited = true;
    }
    return isEdited;
  }

  private checkIsLastRowEmpty(): boolean {
    const tab = (<HTMLTableElement>document.getElementById(this.tableId)).rows[this.jsonData.length + 1];
    const cellCount = this.headerCols.length - 1;
    let isNotEmpty = false;
    for (let i = 0; i < cellCount; i++) {
      const cell: any = tab.getElementsByTagName('td')[i];
      if (cell.childNodes[0].value.trim() !== '') {
        isNotEmpty = true;
        break;
      }
    }
    return isNotEmpty;
  }

  /**Service call to DB save on Update attributes */
  private saveDataToDataBase(manageAttributeData: any) {
    const parent = this;
    parent.attributeService.manageAttributeData(manageAttributeData).then(function (_data) {
      parent.toastr.success(parent.msgInfo.getMessage(112));
      parent.model.isDataSaved = true;
      parent.closeWindow(CONSTANTS.EVENT.TABULAR_DATA_SAVE);
    }).catch(function (error) {
      parent.toastr.error(parent.msgInfo.getMessage(113));
      parent.model.isDataSaved = true;
      parent.model.isSaveClicked = false;
    });
  }

  /** This method used for adding the new row to the temp attribute object. later this object pushed to DB*/
  private addRowToTabularAttrData(attrData: AttachmentAttributeData[]) {
    const parent = this;
    parent.tabularAttrData.forEach(element => {
      element.attributes.push(new AttachmentAttributeData(
        element.attributes.length + 1, 0, 0, 'Row', null, CONSTANTS.ATTRIBUTES.EXTRACT_TYPE_CDE.MANUALLY_CORRECTED,
        CONSTANTS.ATTRIBUTES.EXTRACT_TYPE_TXT.MANUALLY_CORRECTED, CONSTANTS.ATTRIBUTES.CONFIDENCE_PCT.MAX, false,
        false, true, attrData));
    });
  }

  /** This method used for deleting the row(s) from temp attribute object. later this object pushed to DB*/
  private removeRowFromTabularAttrData(removeId: number) {
    const parent = this;
    parent.tabularAttrData.forEach(element => {
      const nonDeletedAttrs = element.attributes.filter(attribute => +attribute.id !== +removeId);
      const deletedAttrs = element.attributes.filter(attribute => +attribute.id === +removeId);
      deletedAttrs[0].isDeleted = true;
      element.attributes = nonDeletedAttrs.concat(deletedAttrs);
    });
    // console.log('Removed');
    // console.log(parent.tabularAttrData);
  }

  /** This method used for adding the new header column to the temp attribute object. later this object pushed to DB*/
  private addHeaderToTabularAttrData(elements: AttachmentAttributeData[]) {
    const parent = this;
    let tempId = 0;
    elements.forEach(element => {
      element.attributes.forEach(innerElement => {
        if (innerElement.attributes.length + 1 > tempId) {
          tempId = innerElement.attributes.length + 1;
        }
        const id = tempId;
        innerElement.attributes.push(new AttachmentAttributeData(id, 0, 0, '', '', CONSTANTS.ATTRIBUTES.EXTRACT_TYPE_CDE.MANUALLY_CORRECTED,
          CONSTANTS.ATTRIBUTES.EXTRACT_TYPE_TXT.MANUALLY_CORRECTED, CONSTANTS.ATTRIBUTES.CONFIDENCE_PCT.MAX, false, false, true, null));
      });
    });
  }

  /** This method used for removing the empty header column from temp attribute object. later this object pushed to DB*/
  private removeEmptyHeaderFromTabularAttrData(elements: AttachmentAttributeData[]) {
    const parent = this;
    elements.forEach(element => {
      element.attributes.forEach(innerElement => {
        innerElement.attributes = innerElement.attributes.filter(attribute => attribute.attrNameTxt !== '');
      });
    });
  }

  /** This method used for modifying the header column value to the temp attribute object. later this object pushed to DB*/
  private updateHeaderTxtInTabularAttrData(elements: AttachmentAttributeData[], updateId: number, updateTo: string): boolean {
    const parent = this;
    let isUpdateSuccess = false;
    elements.forEach(element => {
      element.attributes.forEach(rowElements => {
        let isUpdated = false;
        rowElements.attributes.some(attribute => {
          if (attribute.attrNameTxt === updateTo) {
            //TODO : Configure this msg when refactoring.
            parent.toastr.error('Header already exist.');
            return true;
          }
          if (+attribute.id === +updateId) {
            attribute.attrNameTxt = updateTo;
            attribute.confidencePct = CONSTANTS.ATTRIBUTES.CONFIDENCE_PCT.MAX;
            attribute.extractTypeCde = CONSTANTS.ATTRIBUTES.EXTRACT_TYPE_CDE.MANUALLY_CORRECTED;
            attribute.extractTypeTxt = CONSTANTS.ATTRIBUTES.EXTRACT_TYPE_TXT.MANUALLY_CORRECTED;
            isUpdated = true;
            return true;
          }
        });
        if (isUpdated) {
          rowElements.extractTypeCde = CONSTANTS.ATTRIBUTES.EXTRACT_TYPE_CDE.MANUALLY_CORRECTED;
          rowElements.extractTypeTxt = CONSTANTS.ATTRIBUTES.EXTRACT_TYPE_TXT.MANUALLY_CORRECTED;
          rowElements.confidencePct = CONSTANTS.ATTRIBUTES.CONFIDENCE_PCT.MAX;
          rowElements.isUpdated = isUpdated;
          isUpdateSuccess = true;
        }
      });
    });

    return isUpdateSuccess;
  }

  /** This method used for modifying the row column value to the temp attribute object. later this object pushed to DB*/
  private updateValueInTabularAttrData(updateToRow: number, updateToCellId: number, updateToValue: string) {
    const parent = this;
    parent.tabularAttrData.forEach(element => {
      element.attributes.forEach(rowElements => {
        let isUpdated = false;
        if (+rowElements.id === +updateToRow) {
          rowElements.attributes.forEach(attribute => {
            if (+attribute.id === +updateToCellId) {
              attribute.attrValue = updateToValue;
              attribute.confidencePct = CONSTANTS.ATTRIBUTES.CONFIDENCE_PCT.MAX;
              attribute.extractTypeCde = CONSTANTS.ATTRIBUTES.EXTRACT_TYPE_CDE.MANUALLY_CORRECTED;
              attribute.extractTypeTxt = CONSTANTS.ATTRIBUTES.EXTRACT_TYPE_TXT.MANUALLY_CORRECTED;
              isUpdated = true;
            }
          });
        }
        if (isUpdated) {
          rowElements.confidencePct = CONSTANTS.ATTRIBUTES.CONFIDENCE_PCT.MAX;
          rowElements.extractTypeCde = CONSTANTS.ATTRIBUTES.EXTRACT_TYPE_CDE.MANUALLY_CORRECTED;
          rowElements.extractTypeTxt = CONSTANTS.ATTRIBUTES.EXTRACT_TYPE_TXT.MANUALLY_CORRECTED;
          rowElements.isUpdated = isUpdated;
        }
      });
    });
  }

  /** Following methods are related to Table and its events  */

  /** On click of Table Edit button this method will be called to make cel vales as editable */
  private editEventCall(index: number, id: string, cellTag: string) {
    const parent = this;
    if (!this.isRowSelectAllowed || parent.checkIsHeaderInEditMode()) {
      return;
    }
    const activeRow = index;
    const tab = (<HTMLTableElement>document.getElementById(parent.tableId)).rows[activeRow];
    for (let i = 0; i < this.headerCols.length - 1; i++) {
      const cell: any = tab.getElementsByTagName(cellTag)[i];
      const ele = document.createElement('input');      // TEXTBOX.
      ele.setAttribute('type', 'text');
      ele.setAttribute('id', cell.getAttribute('id'));
      ele.setAttribute('value', cell.innerText);
      ele.setAttribute('style', 'line-height: 0vh');
      cell.innerText = '';
      cell.appendChild(ele);
    }

    const lblCancel = document.getElementById('lbl' + (activeRow));
    lblCancel.setAttribute('style', 'cursor:pointer; display: inline-block; float:left; padding-right:6px;');

    const btSave = document.getElementById('Save' + (activeRow));
    btSave.setAttribute('style', 'display:block; float:left; padding: 0px 5px 0px 5px;');

    if (cellTag === 'th') {
      const btNew = document.getElementById('New' + (activeRow));
      btNew.setAttribute('style', 'display:none;');
    }

    document.getElementById(id).setAttribute('style', 'display:none;');
  }

  /** On click of Table cancel button this method will be called to cancel the cel modified vales */
  private cancelEventCall(i: number, cellTag: string) {
    const parent = this;
    if (parent.isAllRowDeleted) {
      parent.removeEmptyHeaderFromTabularAttrData(parent.tabularAttrDataTmp);
    } else {
      parent.removeEmptyHeaderFromTabularAttrData(parent.tabularAttrData);
    }
    parent.isAllRowDeleted = false;
    parent.model.isHeaderInEditMode = false;
    parent.prepareAndCreateTable();
    return;
  }

  /** On click of Table delete button this method will be delete the table row from attribute object */
  private deleteEventCall(index: number) {
    if (!this.isRowSelectAllowed) {
      return;
    }
    this.removeEmptyHeaderFromTabularAttrData(this.tabularAttrData);
    this.removeRowFromTabularAttrData(index);
    this.prepareAndCreateTable();
  }

  /** On click of Table update button this method will be update cel modified values to attribute object */
  private updateEventCall(index: number, dataRowIndex: number, cellTag: string) {
    const parent = this;
    const activeRow = index;
    const tab = (<HTMLTableElement>document.getElementById(this.tableId)).rows[activeRow];
    const cellCount = this.headerCols.length - 1;
    parent.model.isHeaderInEditMode = false;

    let emptyCellCount = 0;
    if (cellTag === 'td') {
      for (let i = 0; i < cellCount; i++) {
        const cell: any = tab.getElementsByTagName(cellTag)[i];
        if (cell.childNodes[0].value.trim() === '') {
          emptyCellCount = emptyCellCount + 1;
        }
      }
      if (cellCount === emptyCellCount) {
        return;
      }
    }

    for (let i = 0; i < cellCount; i++) {
      const cell: any = tab.getElementsByTagName(cellTag)[i];
      const cellId = cell.getAttribute('id');
      try {
        if (cellTag !== 'td' && cell.childNodes[0].value.trim() === '') {
          return;
        }
        if (cell.childNodes[0].getAttribute('type') === 'text' || cell.childNodes[0].tagName === 'SELECT') {
          if (cellTag === 'th') {
            // && cellCount === i
            let isUpdated = false;
            if (parent.isAllRowDeleted) {
              isUpdated = parent.updateHeaderTxtInTabularAttrData(parent.tabularAttrDataTmp, cellId, cell.childNodes[0].value);
              parent.isAllRowDeleted = false;
            } else {
              isUpdated = parent.updateHeaderTxtInTabularAttrData(parent.tabularAttrData, cellId, cell.childNodes[0].value);
            }
            if (!isUpdated) {
              return;
            }
          } else if (cellTag === 'td') {
            parent.updateValueInTabularAttrData(dataRowIndex, cellId, cell.childNodes[0].value);
          }
        }
      } catch (e) { }
    }
    this.prepareAndCreateTable();
  }

  /** On click of Table add button this method will be to add new row value to attribute object */
  private createNewRowEventCall(index: number) {
    if (!this.isRowSelectAllowed || this.checkIsHeaderInEditMode() || !this.checkIsLastRowEmpty()) {
      return;
    }
    const activeRow = index;
    // Take last row
    const tab = (<HTMLTableElement>document.getElementById(this.tableId)).rows[activeRow];
    const attibutes = [];
    let emptyCellCount = 0;
    for (let i = 0; i < this.headerCols.length - 1; i++) {
      const cell: any = tab.getElementsByTagName('td')[i];
      const cellIndex = this.headerCols[i + 1].split('_')[0];
      const cellHeader = this.headerCols[i + 1].split('_')[1];
      // Check is last row all cell is empty then dont created new row else create
      if (cell.childNodes[0].getAttribute('type') === 'text' || cell.childNodes[0].tagName === 'SELECT') {
        const txtVal = cell.childNodes[0].value.trim();
        if (txtVal === '') {
          emptyCellCount = emptyCellCount + 1;
        }
        // const cellHeader = this.headerCols[cellIndex].replace(this.headerCols[cellIndex].split('_')[0] + '_', '');
        attibutes.push(new AttachmentAttributeData(cellIndex, 0, 0, cellHeader, txtVal,
          CONSTANTS.ATTRIBUTES.EXTRACT_TYPE_CDE.MANUALLY_CORRECTED, CONSTANTS.ATTRIBUTES.EXTRACT_TYPE_TXT.MANUALLY_CORRECTED,
          CONSTANTS.ATTRIBUTES.CONFIDENCE_PCT.MAX, false, false, true, null));

      }
    }
    if (emptyCellCount !== this.headerCols.length - 1 && attibutes.length > 0) {
      this.addRowToTabularAttrData(attibutes);
      this.prepareAndCreateTable();
      document.getElementById('containerScroll').scrollTop += 100;
    }
  }

  /** On click of Table add button at header this method will be to add new column value to attribute object */
  private createNewHeaderEventCall(i: number) {
    const parent = this;
    if (!this.isRowSelectAllowed) {
      return;
    }
    if (parent.isAllRowDeleted) {
      parent.addHeaderToTabularAttrData(parent.tabularAttrDataTmp);
    } else {
      parent.addHeaderToTabularAttrData(parent.tabularAttrData);
    }
    parent.model.isHeaderInEditMode = true;
    parent.prepareAndCreateTable();
    document.getElementById(parent.appendTableTo).scrollLeft += 150;
    const tab = (<HTMLTableElement>document.getElementById(parent.tableId)).rows[0];
    // console.log('Columns');
    // console.log(this.headerCols);
    const cell: any = tab.getElementsByTagName('th')[this.headerCols.length - 2];
    const ele = document.createElement('input');      // TEXTBOX.
    ele.setAttribute('type', 'text');
    ele.setAttribute('value', cell.innerText);
    ele.setAttribute('maxlength', '20');
    ele.focus();
    cell.innerText = '';
    cell.appendChild(ele);
    document.getElementById('Save' + i).setAttribute('style', 'display:inline-block;padding: 0px 10px 0px 8px;');
    document.getElementById('lbl' + i).setAttribute('style', 'display:inline-block; float:left;padding-right:6px');

    document.getElementById('New' + i).setAttribute('style', 'display:none;');
  }

  /** This method used for loading the original values */
  resetTable() {
    this.isResetRequired = true;
    this.prepareAndCreateTable();
  }

  /** This method used for creating the editable table with the event calls */
  private createTable(data: any[], headerColumns: any[]) {
    const parent = this;

    // CREATE A TABLE.
    const table = <HTMLTableElement>document.createElement('table');
    table.setAttribute('id', parent.tableId);
    table.setAttribute('class', 'table table-fixed table-hover table-sm ui-table');

    // CREATE TABLE HEAD .
    const tHead = document.createElement('thead');
    // CREATE ROW FOR TABLE HEAD .
    const hRow = document.createElement('tr');

    // ADD COLUMN HEADER TO ROW OF TABLE HEAD.
    for (let i = 1; i < headerColumns.length; i++) {
      const thTag = document.createElement('th');
      const cellId = headerColumns[i].split('_')[0];
      const cellValue = headerColumns[i].replace(headerColumns[i].split('_')[0] + '_', '');
      thTag.innerHTML = cellValue;
      thTag.setAttribute('id', cellId);
      thTag.setAttribute('class', 'static');

      hRow.appendChild(thTag);
    }
    const th = document.createElement('th');
    th.setAttribute('class', 'static ui-sticky-col');
    th.appendChild(parent.createCancelBtn(0, 0, 'th'));
    th.appendChild(parent.createUpdateBtn(0, 0, 'th'));
    th.appendChild(parent.createNewBtn(0, 'th'));
    hRow.appendChild(th);
    tHead.appendChild(hRow);
    table.appendChild(tHead);

    // CREATE TABLE BODY .
    const tBody = document.createElement('tbody');
    // ADD COLUMN HEADER TO ROW OF TABLE HEAD.
    for (let i = 0; i < data.length; i++) {
      const rowTag = document.createElement('tr'); // CREATE ROW FOR EACH RECORD .
      for (let j = 1; j < headerColumns.length; j++) {
        const tdTag = document.createElement('td');
        const cellId = headerColumns[j].split('_')[0];
        tdTag.setAttribute('id', cellId);
        tdTag.innerHTML = data[i][headerColumns[j]] ? data[i][headerColumns[j]] : '';
        rowTag.appendChild(tdTag);
      }

      const td0 = document.createElement('td');
      const dataRowIndex = data[i][headerColumns[0]];
      const rowId = i + 1;
      td0.setAttribute('class', 'static ui-sticky-col');
      td0.appendChild(parent.createCancelBtn(rowId, dataRowIndex, 'td'));
      td0.appendChild(parent.createUpdateBtn(rowId, dataRowIndex, 'td'));
      td0.appendChild(parent.createEditBtn(rowId, dataRowIndex, 'td'));
      td0.appendChild(parent.createDeleteBtn(rowId, dataRowIndex));
      rowTag.appendChild(td0);
      tBody.appendChild(rowTag);
    }
    // Last Row - Editable
    if (!parent.model.isReadOnlyTabularView) {
      const bRow = document.createElement('tr');
      for (let j = 1; j < headerColumns.length; j++) {
        const tdd = document.createElement('td');
        const cellId = headerColumns[j].split('_')[0];
        tdd.setAttribute('id', cellId);
        const tBox = document.createElement('input');          // CREATE AND ADD A TEXTBOX.
        tBox.setAttribute('type', 'text');
        const headerName = headerColumns[j].replace(headerColumns[j].split('_')[0] + '_', '');
        tBox.setAttribute('placeholder', headerName);
        tBox.setAttribute('value', '');
        tdd.appendChild(tBox);
        bRow.appendChild(tdd);
      }
      const td = document.createElement('td');
      td.setAttribute('class', 'static ui-sticky-col');
      // td.appendChild(parent.createNewBtn(data.length + 1, 'td'));
      td.appendChild(parent.createCancelBtn(data.length + 1, data.length + 1, 'td', true));
      td.appendChild(parent.createUpdateBtn(data.length + 1, data.length + 1, 'td', true));
      bRow.appendChild(td);
      bRow.setAttribute('style', 'line-height: 0vh');
      tBody.appendChild(bRow);
    }
    table.appendChild(tBody);

    const div = document.getElementById(parent.appendTableTo);
    div.innerHTML = '';
    div.appendChild(table);
  }

  private createCancelBtn(i: number, dataRowIndex: number, cellTag: string, isNewRow?: boolean) {
    const parent = this;
    const id = 'lbl' + i;
    const lblCancel = document.createElement('span');
    lblCancel.setAttribute('id', id);
    if (isNewRow) {
      lblCancel.setAttribute('style', 'display: inline-block; float:left; padding-right:6px;');
    } else {
      lblCancel.setAttribute('style', 'display:none; padding-right:6px');
    }
    if (isNewRow && parent.model.isHeaderInEditMode || parent.model.isReadOnlyTabularView) {
      lblCancel.setAttribute('class', 'nia-cancel-red-button-not-allowed');
      lblCancel.removeEventListener('click', function (event) { parent.cancelEventCall(i, cellTag); });
    } else {
      lblCancel.setAttribute('class', 'nia-cancel-red-button');
      lblCancel.addEventListener('click', function (event) { parent.cancelEventCall(i, cellTag); });
    }
    lblCancel.setAttribute('title', 'Cancel');
    return lblCancel;
  }

  private createUpdateBtn(i: number, dataRowIndex: number, cellTag: string, isNewRow?: boolean) {
    const parent = this;
    const btSave = document.createElement('span');
    btSave.setAttribute('title', 'Save');
    btSave.setAttribute('id', 'Save' + i);
    if (isNewRow) {
      btSave.setAttribute('style', 'display:block; float:left; padding: 0px 5px 0px 5px;');
    } else {
      btSave.setAttribute('style', 'display:none;');
    }
    if (isNewRow && parent.model.isHeaderInEditMode || parent.model.isReadOnlyTabularView) {
      btSave.setAttribute('class', 'nia-check-green-button-not-allowed');
      btSave.removeEventListener('click', function (event) {
        if (isNewRow) { parent.createNewRowEventCall(i); } else { parent.updateEventCall(i, dataRowIndex, cellTag); }
      });
    } else {
      btSave.setAttribute('class', 'nia-check-green-button');
      btSave.addEventListener('click', function (event) {
        if (isNewRow) { parent.createNewRowEventCall(i); } else { parent.updateEventCall(i, dataRowIndex, cellTag); }
      });
    }
    return btSave;
  }

  private createEditBtn(i: number, dataRowIndex: number, cellTag: string) {
    const parent = this;
    const id = 'Edit' + i;
    const btUpdate = document.createElement('span');
    btUpdate.setAttribute('title', 'Edit');
    btUpdate.setAttribute('id', 'Edit' + i);
    if (parent.model.isHeaderInEditMode || parent.model.isReadOnlyTabularView) {
      btUpdate.setAttribute('class', 'nia-edit-button-not-allowed');
      btUpdate.removeEventListener('click', function (event) { parent.editEventCall(i, id, cellTag); });
    } else {
      btUpdate.setAttribute('class', 'nia-edit-button');
      btUpdate.addEventListener('click', function (event) { parent.editEventCall(i, id, cellTag); });
    }
    return btUpdate;
  }

  private createDeleteBtn(i: number, dataRowIndex: number) {
    const parent = this;
    const btDelete = document.createElement('span');
    btDelete.setAttribute('title', 'Delete');
    btDelete.setAttribute('style', 'padding-left: 6px');
    if (parent.model.isHeaderInEditMode || parent.model.isReadOnlyTabularView) {
      btDelete.setAttribute('class', 'nia-delete-button-not-allowed');
      btDelete.removeEventListener('click', function (event) { parent.deleteEventCall(dataRowIndex); });
    } else {
      btDelete.setAttribute('class', 'nia-delete-button');
      btDelete.addEventListener('click', function (event) { parent.deleteEventCall(dataRowIndex); });
    }

    return btDelete;
  }

  private createNewBtn(i: number, cellTag: string) {
    const parent = this;
    const btNew = document.createElement('span');
    if (parent.model.isReadOnlyTabularView) {
      btNew.setAttribute('class', 'nia-plus-button-not-allowed');
    } else {
      btNew.setAttribute('class', 'nia-plus-button');
      btNew.setAttribute('id', 'New' + i);
      btNew.setAttribute('title', 'Add');
      btNew.addEventListener('click', function (event) {
        if (cellTag === 'th') { parent.createNewHeaderEventCall(i); } else { parent.createNewRowEventCall(i); }
      });
    }
    return btNew;
  }

}
