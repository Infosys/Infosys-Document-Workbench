/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Injectable } from '@angular/core';
import { UtilityService } from '../../service/utility.service';
import { AttributeData } from '../../data/attribute-data';
import { DataService } from '../../service/data.service';
import { CONSTANTS } from '../../common/constants';
import { AttributeHelper } from '../../utils/attribute-helper';
import { ToastrService } from 'ngx-toastr';
import { MessageInfo } from '../../utils/message-info';


@Injectable()
export class ReExtractionService {

    constructor(private utilityService: UtilityService, private dataService: DataService, private attributeHelper: AttributeHelper,
        private toastr: ToastrService, private msgInfo: MessageInfo) { }

    // Attribute list generation for Re-Extract after each click on row level checkbox
    onRowChecboxClick(model, headerLevelEditDataList: AttributeData[], attachmentId?: number) {
        model.isReExtractionSliderEnabled = false;
        model.isCancelAllowed = true;
        let attrDataRowSelectedList: AttributeData[];
        if (attachmentId !== undefined && attachmentId > 0) {
            attrDataRowSelectedList = this.getSelectedAttrDataList(model.attachmentAttrDataList.filter(
                attrData => attrData.attachmentId === attachmentId)[0].attributes);
        } else {
            attrDataRowSelectedList = this.getSelectedAttrDataList(model.attributeDataList);
        }
        model.reExtractAttributeList = this.getSelectedAttrDataList(model.reExtractAttributeList);
        if (attrDataRowSelectedList.length > 0) {
            const tempAttributeDataEditList = this.utilityService.createDuplicateList(model.reExtractAttributeList);
            for (let i = 0; i < attrDataRowSelectedList.length; i++) {
                const tempAttrDataList: AttributeData[] = tempAttributeDataEditList.filter(a => a.id === attrDataRowSelectedList[i].id);
                if (this.utilityService.isListHasValue(tempAttrDataList)) {
                    const tempAttrData: AttributeData = tempAttrDataList[0];
                    const index = model.reExtractAttributeList.findIndex(a => a.id === tempAttrData.id);
                    model.reExtractAttributeList.splice(index, 1);
                }
            }
            model.reExtractAttributeList = model.reExtractAttributeList.concat(attrDataRowSelectedList);
        }
        model.areRowsSelected = this.utilityService.isListHasValue(model.reExtractAttributeList);
        if (this.utilityService.isListHasValue(headerLevelEditDataList)) {
            const tempEditDataList = this.utilityService.createDuplicateList(headerLevelEditDataList);
            model.reExtractAttributeList = this.onlyUnique(tempEditDataList.concat(model.reExtractAttributeList));
        }
        model.isBtnReExtractDisabled = this.utilityService.isListHasValue(model.reExtractAttributeList) ? false : true;
    }

    // Attribute list generation for Re-Extract after each click on header level checkbox
    onHeadRowChecboxClick(model, headerLevelEditDataList: AttributeData[], attachmentId?: number): AttributeData[] {
        const parent = this;
        model.isReExtractionSliderEnabled = false;
        let tempHeaderLevelEditDataList = [];
        if (attachmentId > 0) {
            const tempAttachmentAttrDataList = model.attachmentAttrDataList.filter(data => data.attachmentId === attachmentId);
            if (parent.utilityService.isListHasValue(tempAttachmentAttrDataList)) {
                const attachmentAttrData = tempAttachmentAttrDataList[0];
                if (attachmentAttrData['isAttachmentOptionChecked']) {
                    parent.assignAttributeOptionCheckAllowed(attachmentAttrData.attributes, false);
                    tempHeaderLevelEditDataList = this.createHeaderLvlAttrDataListForReExtract(attachmentAttrData.attributes);
                } else {
                    headerLevelEditDataList = parent.removeHeaderLvlAttrDataListForReExtract(attachmentId, model, headerLevelEditDataList);
                    parent.assignAttributeOptionCheckAllowed(attachmentAttrData.attributes, true);
                }
            }
        } else {
            if (model.isEmailAttrSelected) {
                if (parent.utilityService.isListHasValue(model.attributeDataList)) {
                    parent.assignAttributeOptionCheckAllowed(model.attributeDataList, false);
                    tempHeaderLevelEditDataList = this.createHeaderLvlAttrDataListForReExtract(model.attributeDataList);
                }
            } else {
                headerLevelEditDataList = parent.removeHeaderLvlAttrDataListForReExtract(attachmentId, model, headerLevelEditDataList);
                parent.assignAttributeOptionCheckAllowed(model.attributeDataList, true);
            }
        }
        headerLevelEditDataList = parent.utilityService.isListHasValue(headerLevelEditDataList) ?
            headerLevelEditDataList.concat(tempHeaderLevelEditDataList) : tempHeaderLevelEditDataList;
        if (parent.utilityService.isListHasValue(headerLevelEditDataList)) {
            model.reExtractAttributeList = parent.utilityService.isListHasValue(model.reExtractAttributeList) ?
                model.reExtractAttributeList.concat(tempHeaderLevelEditDataList) : tempHeaderLevelEditDataList;
        }
        model.areRowsSelected = parent.utilityService.isListHasValue(model.reExtractAttributeList);
        model.isBtnReExtractDisabled = model.areRowsSelected ? false : true;
        model.isCancelAllowed = true;
        return headerLevelEditDataList;
    }

    // Validation after clicking on Re-Extract button in UI
    validateToReextract(model) {
        const parent = this;
        let isValid = true;
        model.baseAttrDataList = [];
        let attrDataList = parent.utilityService.createDuplicateList(model.reExtractAttributeList);
        attrDataList =
            // attrDataList.filter((x, i, a) => a.indexOf(x) === i);
            attrDataList.filter((v, i, a) => a.findIndex(t => (t.id === v.id)) === i);
        attrDataList.forEach(attrData => {
            if (parent.attributeHelper.isKeyAttribute(attrData.attrNameCde)) {
                attrData.optionChecked = true;
                model.baseAttrDataList.push(attrData);
            }
        });
        // To avoid Re extract without selecting document type or category which has value as unknown.
        const tempDataList: AttributeData[] = parent.utilityService.createDuplicateList(model.reExtractAttributeList);
        const tempEmailAttrDataList = tempDataList.filter(attrData => attrData.attachmentId === undefined);
        const tempAttachmentAttrDataList = tempDataList.filter(attrData => attrData.attachmentId !== undefined);
        if (parent.utilityService.isListHasValue(model.attachmentDataList) &&
            parent.utilityService.isListHasValue(model.attachmentAttrDataList) &&
            parent.utilityService.isListHasValue(tempAttachmentAttrDataList)) {
            for (let i = 0; i < model.attachmentAttrDataList.length; i++) {
                let tempAttrDataList = tempAttachmentAttrDataList.filter(attrData =>
                    attrData.attachmentId === model.attachmentAttrDataList[i].attachmentId);
                if (parent.validateIsKeyAttributeUnknown(model.attachmentAttrDataList[i].attributes, tempAttrDataList,
                    CONSTANTS.ATTR_NAME_CDE.DOCUMENT_TYPE)) {
                    parent.toastr.error(parent.msgInfo.getMessage(150));
                    isValid = false;
                    break;
                }
                tempAttrDataList = [];
            }
        }
        if (isValid && parent.utilityService.isListHasValue(model.attributeDataList) &&
            parent.validateIsKeyAttributeUnknown(model.attributeDataList, tempEmailAttrDataList, CONSTANTS.ATTR_NAME_CDE.CATEGORY)) {
            parent.toastr.error(parent.msgInfo.getMessage(150));
            isValid = false;
        }
        return isValid;
    }

    // To reset back to normal for Re-Extract flow changes.
    cancelReExtract(model) {
        this.unCheckOption(model.attributeDataList);
        model.attachmentAttrDataList.forEach(
            attachmentAttrData => {
                this.unCheckOption(attachmentAttrData.attributes);
                attachmentAttrData['isAttachmentOptionChecked'] = false;
            }
        );
        model.reExtractAttributeList = [];
        model.areRowsSelected = model.isEmailAttrSelected = model.isCancelAllowed = false;
        model.isBtnReExtractDisabled = model.isReExtractionSliderEnabled = true;
    }

    // To get selected attribute from checkbox.
    private getSelectedAttrDataList(attributeDataList: AttributeData[]) {
        let attributeDataEditList: AttributeData[] = [];
        let tempDataList: AttributeData[];
        const tempAttrDataList = attributeDataList.filter(attrData => attrData.attributes !== null);
        const attrDataList = this.utilityService.createDuplicateList(tempAttrDataList);
        attrDataList.forEach(function (attrData) {
            attrData.attributes = attrData.attributes.filter(opt => opt.optionChecked);
        });
        attributeDataEditList = attrDataList.filter(attrData => attrData.attributes.length > 0);

        tempDataList = attributeDataList
            .filter(opt => opt.optionChecked)
            .map(opt => opt);
        if (attributeDataEditList.length > 0) {
            attributeDataEditList = attributeDataEditList.concat(tempDataList);
        } else {
            attributeDataEditList = tempDataList;
        }
        return attributeDataEditList;
    }

    // To remove repeated data using id.
    private onlyUnique(array: AttributeData[]) {
        const flags = [], output = [], l = array.length, attachmentFlag = [];
        for (let i = 0; i < l; i++) {
            const id = array[i].id;
            if (id === 0) {
                const attachmentId = array[i].attachmentId;
                if (attachmentFlag[attachmentId]) {
                    continue;
                }
                attachmentFlag[attachmentId] = true;
            } else {
                if (flags[id]) {
                    continue;
                }
                flags[id] = true;
            }
            output.push(array[i]);
        }
        return output;
    }

    // To assign optionCheckAllowed parameter for each attribute based on header row selection.
    private assignAttributeOptionCheckAllowed(attrDataList: AttributeData[], isAllowed: boolean) {
        attrDataList.forEach(attributeData => {
            if (this.attributeHelper.isMultiAttribute(attributeData.attrNameCde)) {
                attributeData.attributes.forEach(attrData => attrData['isOptionCheckAllowed'] = isAllowed);
            } else {
                attributeData['isOptionCheckAllowed'] = isAllowed;
            }
        });
    }

    // To create attributelist while click on the header check box for re extraction.
    private createHeaderLvlAttrDataListForReExtract(attributeDataList: AttributeData[]) {
        const tempHeaderLevelEditDataList = [];
        attributeDataList.forEach(attributeData => {
            const data = Object.assign({}, attributeData);
            if (this.attributeHelper.isMultiAttribute(data.attrNameCde)) {
                data.attributes = this.utilityService.createDuplicateList(data.attributes);
                data.attributes.forEach(tempData => tempData['headerSelection'] = true);
            } else {
                data['headerSelection'] = true;
            }
            tempHeaderLevelEditDataList.push(data);
        });
        return tempHeaderLevelEditDataList;
    }

    // To remove header level selected attributes for re extraction.
    private removeHeaderLvlAttrDataListForReExtract(attachmentId, model, headerLevelEditDataList: AttributeData[]) {
        const duplicateList = this.utilityService.createDuplicateList(model.reExtractAttributeList);
        duplicateList.forEach(attributeData => {
            if (attributeData.attachmentId === attachmentId) {
                if (this.attributeHelper.isMultiAttribute(attributeData.attrNameCde)) {
                    attributeData.attributes.forEach(attrData => {
                        if (attrData['headerSelection']) {
                            const tempDataList = model.reExtractAttributeList.filter(data => data.id === attributeData.id);
                            let removeData;
                            tempDataList.forEach(tempData => {
                                tempData.attributes.forEach(data => {
                                    if (data.id === attrData.id && data['headerSelection']) {
                                        removeData = tempData;
                                    }
                                });
                            });
                            if (removeData !== undefined) {
                                model.reExtractAttributeList = this.attributeHelper.removeAttribute(removeData,
                                    model.reExtractAttributeList);
                            }
                        }
                    });
                } else if (attributeData['headerSelection']) {
                    const tempData = model.reExtractAttributeList.filter(data =>
                        data.id === attributeData.id && data['headerSelection'])[0];
                    model.reExtractAttributeList = this.attributeHelper.removeAttribute(tempData, model.reExtractAttributeList);
                }
            }
        });
        headerLevelEditDataList = headerLevelEditDataList.filter(data => data.attachmentId !== attachmentId);
        return headerLevelEditDataList;
    }

    // To check whether key attribute value is Unknown or not.
    private validateIsKeyAttributeUnknown(attributeDataList: AttributeData[], selectedAttributeDataList: AttributeData[],
        attrNameCde: number) {
        const keyAttributeList = attributeDataList.filter(attrData =>
            attrData.attrNameCde === attrNameCde);
        let flag = false;
        if (this.utilityService.isListHasValue(selectedAttributeDataList) && !this.utilityService.isListHasValue(
            selectedAttributeDataList.filter(attrData => attrData.attrNameCde === attrNameCde)) &&
            this.utilityService.isListHasValue(keyAttributeList)) {
            const attrValue = keyAttributeList[0].attrValue;
            if (attrValue === CONSTANTS.ATTRIBUTES.UNKNOWN_ATTR_VALUE) {
                flag = true;
            }
        }
        return flag;
    }

    // To uncheck row level checkbox after clicking on cancel.
    private unCheckOption(attrDataList: AttributeData[]) {
        attrDataList.forEach(attrData => {
            if (this.attributeHelper.isMultiAttribute(attrData.attrNameCde)) {
                this.unCheckOption(attrData.attributes);
            } else {
                attrData.optionChecked = false;
                attrData['isOptionCheckAllowed'] = true;
            }
        });
    }
}
