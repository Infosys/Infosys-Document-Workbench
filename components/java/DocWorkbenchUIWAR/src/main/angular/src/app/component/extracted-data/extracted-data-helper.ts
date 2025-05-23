/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Injectable } from '@angular/core';
import { UtilityService } from '../../service/utility.service';
import { DataService } from '../../service/data.service';
import { AttributeData } from '../../data/attribute-data';
import { CONSTANTS } from '../../common/constants';
import { EDData } from './annotation.service';
import { ToastrService } from 'ngx-toastr';
import { MessageInfo } from '../../utils/message-info';
import { AttributeHelper } from '../../utils/attribute-helper';
import { AttributeAttributeMappingData } from '../../data/attribute-attribute-mapping-data';
import { AttributeValidationData } from '../../data/attribute-validation-data';
import { AttachmentData } from '../../data/attachment-data';

@Injectable()
export class ExtractedDataHelper {

    constructor(private utilityService: UtilityService, private toastrService: ToastrService, private msgInfo: MessageInfo,
        private dataService: DataService, private attributeHelper: AttributeHelper) { }

    // To check save button is allowed or not for add attributes.
    isSaveButtonAllowed(model, attributeDataEditList, deleteAttributeList): void {
        model.isSaveAllowed = (this.isUnsavedDataExist(model, attributeDataEditList, deleteAttributeList))
            && this.checkIfNoValidationErrExists(model, attributeDataEditList);
    }

    // To check any unsave data irrespective of operation.
    isUnsavedDataExist(model, attributeDataEditList: AttributeData[], deleteAttributeList: AttributeData[]) {
        return this.utilityService.isListHasValue(model.annotatedAttributeDataList) ||
            this.utilityService.isListHasValue(deleteAttributeList) ||
            this.utilityService.isListHasValue(attributeDataEditList) ||
            this.utilityService.isListHasValue(model.addAttributeDataList);
    }

    // To check whether attribute name and attribute value has error for given attribute list
    checkAttributeValidData(attrDataList: AttributeData[], isManualAdd?: boolean): boolean {
        const isAttrNameTxtValidationExist = this.utilityService.isListHasValue(attrDataList.filter(attrData =>
            this.attributeHelper.isMultiAttribute(attrData.attrNameCde) && this.checkAttributeValidation(attrData, isManualAdd, true)));
        const isAttrValValidationResult = this.utilityService.isListHasValue(attrDataList.filter(
            attrData => this.checkAttributeValidation(attrData, isManualAdd)));
        return !(isAttrNameTxtValidationExist || isAttrValValidationResult);
    }

    // To change attribute row icon from delete to undo in UI.
    deleteAttribute(attributeData: AttributeData, model, edDataObj: EDData, annotation?) {
        if (this.isCUDOperationAllowed(model)) {
            if (edDataObj.isReextractionPendingExist) {
                this.toastrService.error(this.msgInfo.getMessage(149));
                return;
            }
            attributeData['index'] = undefined;
            edDataObj.attributeDataEditList = this.attributeHelper.removeAttribute(attributeData, edDataObj.attributeDataEditList);
            attributeData.deleteClicked = true;
            if (annotation === undefined) {
                this.publishAttributeOpDataEvent(CONSTANTS.OPERATION_TYPE.DELETE, attributeData, model.isDocTypeFile, true);
            }
            attributeData.attrValue = attributeData['attrValueOrg'];
            edDataObj.deleteAttributeList.push(attributeData);
            this.isSaveButtonAllowed(model, edDataObj.attributeDataEditList, edDataObj.deleteAttributeList);
            edDataObj.attributeValidationResult = this.checkIfNoValidationErrExists(model, edDataObj.attributeDataEditList);
            model.isCancelAllowed = true;
            attributeData.isAttrValueChanged = false;
            model.isReExtractionSliderEnabled = false;
        }
    }

    // To check Header is clicked for add/delete operation
    isCUDOperationAllowed(model) {
        let isAllowed = model.isCaseEditAllowed;
        if (model.isDocTypeFile) {
            isAllowed = isAllowed && model.isEDReadyToModify;
        }
        return isAllowed;
    }

    // To publish attribute operation data event.
    publishAttributeOpDataEvent(operationType: string, attrData, isDocTypeFile: boolean, status?: boolean) {
        if (isDocTypeFile) {
            this.dataService.publishAttributeOpData({
                'operation': operationType,
                'status': status,
                'attribute': attrData
            });
        }
    }

    // To remove selected attribute from annotated list in UI using cancel button.
    removeAnnotatedAttribute(attrData: AttributeData, model, attributeDataEditList: AttributeData[], deleteAttributeList: AttributeData[],
        isEvent?): void {
        this.removeValidation(attrData, model);
        model.annotatedAttributeDataList = this.attributeHelper.removeAttribute(attrData, model.annotatedAttributeDataList);
        if (isEvent === undefined) {
            this.publishAttributeOpDataEvent(CONSTANTS.OPERATION_TYPE.DELETE, attrData, model.isDocTypeFile, false);
        }
        this.isSaveButtonAllowed(model, attributeDataEditList, deleteAttributeList);
    }

    // To remove selected attribute from newly added list in UI using cancel button.
    removeManuallyAddedAttribute(attrData: AttributeData, model, attributeList: AttributeData[], attributeDataEditList: AttributeData[],
        deleteAttributeList: AttributeData[], isEvent?): void {
        this.removeValidation(attrData, model);
        model.addAttributeDataList = this.attributeHelper.removeAttribute(attrData, model.addAttributeDataList);
        if (isEvent === undefined) {
            this.publishAttributeOpDataEvent(CONSTANTS.OPERATION_TYPE.DELETE, attrData, model.isDocTypeFile, false);
        }
        let attrNameTxt = attrData.attrNameTxt;
        if (!attrData['isLastAddedAttribute'] && !this.attributeHelper.isMultiAttribute(attrData.attrNameCde)) {
            const tempAttrData = attributeList.filter(data => data.attrNameCde === +attrData.attrNameCde)[0];
            model.attributeDataValList = this.restoreData(model.attributeDataValList, tempAttrData);
            attrNameTxt = tempAttrData.attrNameTxt;
        }
        this.isSaveButtonAllowed(model, attributeDataEditList, deleteAttributeList);
        if (!this.utilityService.isListHasValue(model.addAttributeDataList)) {
            model.isAddAttachmentAllowed = model.isAddAllowed = true;
            model.isAddClicked = model.isAddAttachmentAttrClicked = false;
            model.selectedAttachmentId = undefined;
        }
    }

    // To get new attributes for adding attributes in attachment or document level.
    filterNonListedAttributes(attrDataList: AttributeData[], model, edDataObj: EDData, isManualAdd?: boolean,
        attributeNameValuesList = []) {
        const attributeValList: AttributeData[] = [];
        this.resetModelValues(model, edDataObj);
        let attrNameTxtList = [];
        edDataObj.attributeList = this.filterConfiguredAttrByKeyAttribute(attrDataList, model.attributeAttributeMapping);
        // Don't add it in single line because 2 list is acting as reference if we add it in single line.
        edDataObj.selectedAttrNameTxtList = this.attributeHelper.getAttrNameTxtList(attrDataList, []);
        if (edDataObj.attributeList.length > 0) {
            attrNameTxtList = this.attributeHelper.getAttrNameTxtList(attrDataList, []);
            attrNameTxtList = this.attributeHelper.getAttrNameTxtList(model.annotatedAttributeDataList, attrNameTxtList);
            for (let j = 0; j < edDataObj.attributeList.length; j++) {
                if (attrNameTxtList.filter(a => a === edDataObj.attributeList[j].attrNameTxt.toLowerCase()).length <= 0) {
                    attributeValList.push(edDataObj.attributeList[j]);
                }
            }
            model.attributeDataValList = attributeValList;
            if (this.utilityService.isListHasValue(model.attributeDataValList)) {
                edDataObj.selectedAttrNameCde = attributeValList[0].attrNameCde;
            }
            const tempAttributeData: AttributeData = new AttributeData(CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE, 0, 0,
                false, 0, 'Other', '', 0, '', 0, [], [], '', false, new AttributeValidationData(null, null), true);
            edDataObj.attributeValidationResult = false;
            model.attributeDataValList.push(tempAttributeData);
            model.addAttributeValList = model.attributeDataValList;
        }

        if (isManualAdd) {
            const attributeData = new AttributeData(null, null, null, null, null, null, '',
                CONSTANTS.ATTRIBUTES.EXTRACT_TYPE_CDE.MANUALLY_CORRECTED, null, CONSTANTS.ATTRIBUTES.CONFIDENCE_PCT.MAX, null, null, null,
                null, new AttributeValidationData(null, null), true);
            attributeData['attrId'] = this.counter();
            if (edDataObj.selectedAttrNameCde > 0) {
                attributeData.attrNameCde = edDataObj.selectedAttrNameCde;
                // When no attribute listed, while clicking add btn category dropdown values should be displayed
                this.getAllowedValues(edDataObj.selectedAttrNameCde, attributeData, model, attributeNameValuesList);
            } else {
                attributeData.attrNameCde = CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE;
            }
            attributeData['isLastAddedAttribute'] = true;
            edDataObj.attributeValidationResult = false;
            model.addAttributeDataList.push(attributeData);
        }
    }

    counter() {
        return Math.floor(1000000 + new Date().getMilliseconds() * 9000000);
    }

    getAllowedValues(selectedAttrNameCde, attrData: AttributeData, model, attributeNameValuesList) {
        attrData.attrValue = '';
        attrData.allowedValues = [];
        model.isSaveAllowed = false;
        if (!this.attributeHelper.isMultiAttribute(selectedAttrNameCde)) {
            for (let i = 0; i < attributeNameValuesList.length; i++) {
                if (+selectedAttrNameCde === attributeNameValuesList[i].attrNameCde) {
                    attrData.attrNameTxt = attributeNameValuesList[i].attrNameTxt;
                    for (let j = 0; j < attributeNameValuesList[i].allowedValues.length; j++) {
                        attrData.allowedValues.push(attributeNameValuesList[i].allowedValues[j].txt);
                    }
                }
            }
        } else {
            attrData.attrNameTxt = '';
            attrData.attrNameValidation.attrNameTxtValidationResult = null;
        }
        // Don't set as undefined then it will fail attibute value validation.
        attrData.attrNameValidation.attrValueValidationResult = null;
        if (attrData !== undefined) {
            this.notifyAttributeAddEvent(attrData, model);
        }
    }

    // To send the attribute add event based on condition to the file-content for annotations
    notifyAttributeAddEvent(attrData: AttributeData, model) {
        this.notifyIsKeyAttributeChanged(attrData, model.attributeAttributeMapping);
        if (!this.utilityService.isAValidValue(attrData.attrNameValidation.attrNameTxtValidationResult)) {
            if (model.isDocTypeFile && attrData.attrNameCde !== CONSTANTS.ATTR_NAME_CDE.CATEGORY &&
                attrData.attrNameCde !== CONSTANTS.ATTR_NAME_CDE.DOCUMENT_TYPE) {
                if (!this.utilityService.isStringHasValue(attrData.attrNameTxt)) {
                    if (this.utilityService.isListHasValue(model.addAttributeValList)) {
                        model.addAttributeValList.filter(attr => {
                            if (attr.attrNameCde === attrData.attrNameCde) {
                                attrData.attrNameTxt = attr.attrNameTxt;
                            }
                        });
                    }
                }
                this.trimValue(attrData);
                if (this.utilityService.isStringHasValue(attrData.attrNameTxt) &&
                    (!this.utilityService.isAValidValue(attrData.attrNameValidation.attrValueValidationResult) ||
                        !attrData.attrNameValidation.attrValueValidationResult['attrValueDuplicate'])) {
                    this.publishAttributeOpDataEvent(attrData['attrId'] === undefined ?
                        CONSTANTS.OPERATION_TYPE.EDIT : CONSTANTS.OPERATION_TYPE.ADD, attrData, model.isDocTypeFile);
                }
            }
        }
    }

    // Only for Document flow publish attribute names to annotator DropDown
    postToFileContentComponent(attributeData: AttributeData[], attributeAttributeMapping) {
        const postToFileContentComponent = [];
        this.filterConfiguredAttrByKeyAttribute(attributeData, attributeAttributeMapping).forEach(attr => {
            postToFileContentComponent.push({ 'name': attr.attrNameTxt, 'value': attr.attrNameCde });
        });
        this.dataService.publishAttributeOpData(postToFileContentComponent);
    }

    // To validate attrNameTxt is already exist from either db or newly added.
    validateAttrNameTxt(attributeData: AttributeData, model, selectedAttrNameTxtList) {
        let error;
        // To check db data validation
        const currentValue = attributeData.attrNameTxt.toLowerCase().trim();
        if (currentValue.length === attributeData.attrNameTxt.length) {
            const attrNameTxtList = selectedAttrNameTxtList;
            attrNameTxtList.forEach(attrNameTxt => {
                if (attrNameTxt.trim() === currentValue) {
                    error = { 'attrNameTxtInvalid': true };
                }
            });
            const object = this.generateAddListAndCurrentIndex(attributeData, model.addAttributeDataList, model.annotatedAttributeDataList);
            const addAttributeDataList = object['addAttributeDataList'];
            const currentIndex = object['currentIndex'];
            let isAttrValid = true;
            let duplicateIndex;
            delete attributeData['duplicateOfIndex'];
            if (error === undefined) {
                for (let i = 0; i < addAttributeDataList.length; i++) {
                    /* Comparision
                      1. To find duplicates from the list.
                      2. To ignore duplicate id of next attribute is current attribute id.
                    */
                    const isAttrNameTxtExist = this.utilityService.isStringHasValue(addAttributeDataList[i].attrNameTxt)
                        && addAttributeDataList[i].attrNameTxt.toLowerCase().trim() === attributeData.attrNameTxt.toLowerCase();
                    const isCurntIndxAndDupIndxSame = addAttributeDataList[i]['duplicateOfIndex'] === currentIndex;
                    // Skip active row check.
                    if (currentIndex !== i) {
                        // Check text match.
                        if (isAttrNameTxtExist && addAttributeDataList[i]['duplicateOfIndex'] === undefined) {
                            // Set duplicate reference
                            attributeData['duplicateOfIndex'] = i;
                            error = { 'attrNameTxtInvalid': true };
                        }
                        // Update Index reference when original text changed.
                        if (isCurntIndxAndDupIndxSame && !isAttrNameTxtExist) {
                            const attrId = addAttributeDataList[i]['attrId'];
                            const result = this.reassignDuplicateOfIndex(i, isAttrValid, duplicateIndex, attrId, model);
                            duplicateIndex = result[0];
                            isAttrValid = result[1];
                        }
                    }
                }
            } else {
                // If current row attribute name has duplicate with saved attribute name and if there is any duplicate id reference with
                // current attribute remove that reference id for other attribute and re assign the duplicate index.
                this.removeValidation(attributeData, model);
            }
        } else {
            error = { 'attrNameTxtExtraSpaces': true };
        }
        attributeData.attrNameValidation.attrNameTxtValidationResult = error;
    }

    // To check valid data based on attribute validation for the given attributeData List
    checkValidData(isValid: boolean, attrDataList: AttributeData[]) {
        return isValid && (!this.utilityService.isListHasValue(attrDataList) || this.checkAttributeValidData(attrDataList));
    }

    enableDisableFields(model, attributeValidationResult: boolean, attributeDataEditList: AttributeData[],
        deleteAttributeList: AttributeData[]) {
        model.isAddAllowed = model.isAddAttachmentAllowed = model.isSaveAllowed = false;
        model.isCancelAllowed = true;
        if (!model.isAddClicked &&
            !model.isAddAttachmentAttrClicked) {
            model.isAddAllowed = model.isAddAttachmentAllowed = true;
            model.isCancelAllowed = false;
        }
        if (this.utilityService.isListHasValue(attributeDataEditList) || this.utilityService.isListHasValue(deleteAttributeList)) {
            model.isSaveAllowed = true;
        }
        if (model.isSaveAllowed && (!attributeValidationResult ||
            !this.checkAttributeValidData(model.addAttributeDataList, true))) {
            model.isSaveAllowed = false;
        }
    }

    /**
     * This method is called to get the main attachment based on extract type cde and file meta data.
     * Main Attachment: The attachment which is shown in UI and used for processing.
     * @param attachmentDataList
     */
    getMainAttachmentData(attachmentDataList: AttachmentData[]) {

        // Zip Customization
        {
            attachmentDataList = attachmentDataList.sort((attachData1, attachData2) => attachData1.sortOrder - attachData2.sortOrder);
            const firstAttachmentData: AttachmentData = attachmentDataList[0];
            attachmentDataList = this.groupAttachment(attachmentDataList).get(firstAttachmentData.groupName);
        }

        let attachmentList = attachmentDataList.filter(attachData =>
            attachData.extractTypeCde === CONSTANTS.ATTRIBUTES.EXTRACT_TYPE_CDE.CUSTOM_LOGIC &&
            !(this.utilityService.isListHasValue(attachData.attributes)
                && attachData.attributes[0].attrValue === CONSTANTS.FILE_METADATA.PLAIN_TXT));
        if (!this.utilityService.isListHasValue(attachmentList)) {
            attachmentList = attachmentDataList.filter(attachData =>
                attachData.extractTypeCde === CONSTANTS.ATTRIBUTES.EXTRACT_TYPE_CDE.DIRECT_COPY);
        }
        return attachmentList;
    }

    checkIfNoValidationErrExists(model, attributeDataEditList) {
        let isValid = true;
        if (this.utilityService.isListHasValue(model.addAttributeDataList)) {
            isValid = this.checkAttributeValidData(model.addAttributeDataList, true);
        }
        if (isValid && this.utilityService.isListHasValue(model.annotatedAttributeDataList)) {
            isValid = this.checkAttributeValidData(model.annotatedAttributeDataList);
        }
        if (isValid && this.utilityService.isListHasValue(attributeDataEditList)) {
            isValid = this.checkAttributeValidData(attributeDataEditList);
        }
        return isValid;
    }

    trimValue(attrData: AttributeData) {
        attrData.attrValue = attrData.attrValue.startsWith(CONSTANTS.ATTRIBUTES.DELIMITER) ?
            attrData.attrValue.substring(CONSTANTS.ATTRIBUTES.DELIMITER.length) : attrData.attrValue;
        attrData.attrValue = attrData.attrValue.endsWith(CONSTANTS.ATTRIBUTES.DELIMITER) ?
            attrData.attrValue.substring(0, attrData.attrValue.lastIndexOf(CONSTANTS.ATTRIBUTES.DELIMITER)) : attrData.attrValue;
        // attrData.attrValue = attrData.attrValue.trim();
    }

    // To perform validation based on manual add or normal attribute.
    private checkAttributeValidation(attrData: AttributeData, isManualAdd: boolean, isAttrNameTxt?: boolean) {
        let isInvalid = false;
        if (isManualAdd || !this.attributeHelper.isMultiAttribute(attrData.attrNameCde)) {
            isInvalid = this.checkAttributeTypeValidation(attrData, isAttrNameTxt);
        } else {
            attrData.attributes.forEach(subAttrData => {
                if (!isInvalid) {
                    isInvalid = this.checkAttributeTypeValidation(subAttrData, isAttrNameTxt);
                }
            });
        }
        return isInvalid;
    }

    // To perform necessary type of validation.
    private checkAttributeTypeValidation(attrData: AttributeData, isAttrNameTxt: boolean): boolean {
        let isInvalid = false;
        if (isAttrNameTxt) {
            isInvalid = attrData.attrNameValidation.attrNameTxtValidationResult !== undefined;
        } else {
            isInvalid = attrData.attrNameValidation.attrValueValidationResult !== undefined;
        }
        return isInvalid;
    }

    // To remove Validation if current data removed from manual added list.
    private removeValidation(attributeData: AttributeData, model) {
        const object = this.generateAddListAndCurrentIndex(attributeData, model.addAttributeDataList, model.annotatedAttributeDataList);
        const addAttributeDataList = object['addAttributeDataList'];
        const currentIndex = object['currentIndex'];
        let isAttrValid = true;
        let duplicateIndex;
        for (let i = 0; i < addAttributeDataList.length; i++) {
            if (addAttributeDataList[i]['duplicateOfIndex'] !== undefined) {
                const isCurntIndxAndDupIndxSame = addAttributeDataList[i]['duplicateOfIndex'] === currentIndex;
                // Skip active row check.
                if (currentIndex !== i) {
                    const attrId = addAttributeDataList[i]['attrId'];
                    // To check removed unsaved added attribute occurs before invalid attributes.
                    const isBeforeAttribute = currentIndex < i;
                    // Update Index reference when original text deleted.
                    if (isCurntIndxAndDupIndxSame) {
                        const result = this.reassignDuplicateOfIndex(i, isAttrValid, duplicateIndex, attrId, model);
                        if (duplicateIndex === undefined && isBeforeAttribute) {
                            result[0] = result[0] - 1;
                        }
                        duplicateIndex = result[0];
                        isAttrValid = result[1];
                    } else if (isBeforeAttribute) {
                        const attrData = this.getAttributeUsingAttrId(attrId, model);
                        attrData['duplicateOfIndex'] -= 1;
                    }
                }
            }
        }
    }

    // To reassign Duplicate Of Index for attrNameTxt validation.
    private reassignDuplicateOfIndex(index, isAttrValid, duplicateIndex, attrId, model) {
        const attrData = this.getAttributeUsingAttrId(attrId, model);
        if (isAttrValid) {
            delete attrData['duplicateOfIndex'];
            attrData.attrNameValidation.attrNameTxtValidationResult = undefined;
            duplicateIndex = index;
            isAttrValid = false;
        } else {
            attrData['duplicateOfIndex'] = duplicateIndex;
        }
        return [duplicateIndex, isAttrValid];
    }

    // To get attribute from newly added attribute using attrId
    private getAttributeUsingAttrId(attrId, model) {
        let attributeData;
        attributeData = model.addAttributeDataList.filter(attrData => attrData['attrId'] === attrId)[0];
        if (!attributeData) {
            model.annotatedAttributeDataList.forEach(attrData => {
                if (this.utilityService.isListHasValue(attrData.attributes)) {
                    attrData.attributes.forEach(subAttrData => {
                        if (subAttrData['attrId'] === attrId) {
                            attributeData = subAttrData;
                        }
                    });
                }
                if (attrData['attrId'] === attrId) {
                    attributeData = attrData;
                }
            });
        }
        return attributeData;
    }

    // Add new attribute to list and sort it based on attrNameTxt.
    private restoreData(attrDataList: AttributeData[], attrData: AttributeData): AttributeData[] {
        const tempData = attrDataList.filter(data => this.attributeHelper.isMultiAttribute(data.attrNameCde))[0];
        attrDataList.pop();
        attrDataList.push(attrData);
        attrDataList.sort((attrData1, attrData2) => (attrData1.attrNameTxt > attrData2.attrNameTxt) ? 1 :
            (attrData1.attrNameTxt < attrData2.attrNameTxt) ? -1 : 0);
        attrDataList.push(tempData);
        return attrDataList;
    }

    // To generate add list by combining normal add and annotation add. Find current index of the attribute in overall add list.
    private generateAddListAndCurrentIndex(attributeData: AttributeData, addAttributeDataList: AttributeData[],
        annotatedAttributeDataList: AttributeData[]) {
        let tempAddAttributeDataList: AttributeData[];
        if (this.utilityService.isListHasValue(addAttributeDataList)) {
            tempAddAttributeDataList = this.utilityService.createDuplicateList(addAttributeDataList);
        }
        if (this.utilityService.isListHasValue(annotatedAttributeDataList)) {
            const tempAnnotatedAttributeDataList = this.attributeHelper.convertFlatStructure(
                this.utilityService.createDuplicateList(annotatedAttributeDataList));
            if (this.utilityService.isListHasValue(tempAddAttributeDataList)) {
                tempAddAttributeDataList = tempAnnotatedAttributeDataList.concat(tempAddAttributeDataList);
            } else {
                tempAddAttributeDataList = tempAnnotatedAttributeDataList;
            }
        }
        const currentIndex = tempAddAttributeDataList.indexOf(
            tempAddAttributeDataList.filter(attrData => attrData['attrId'] === attributeData['attrId'])[0]);
        const object = { 'currentIndex': currentIndex, 'addAttributeDataList': tempAddAttributeDataList };
        return object;
    }

    private resetModelValues(model, edDataObj: EDData) {
        model.attributeDataValList = [];
        edDataObj.selectedAttrNameCde = 0;
    }

    // Attr Attr Mapping data filter - To show Drop Down attr name while add click
    private filterConfiguredAttrByKeyAttribute(attrDataList: AttributeData[], attributeAttributeMapping): AttributeData[] {
        const keyAttribute = attrDataList.filter(attr => attr.attrNameCde === CONSTANTS.ATTR_NAME_CDE.CATEGORY ||
            attr.attrNameCde === CONSTANTS.ATTR_NAME_CDE.DOCUMENT_TYPE)[0];
        let matchedAttr = [];
        if (keyAttribute !== undefined && attributeAttributeMapping) {
            const ruleMappedAttr: AttributeAttributeMappingData[] = attributeAttributeMapping.filter(attr =>
                attr.attrNameCde === keyAttribute.attrNameCde);
            matchedAttr = ruleMappedAttr.length > 0 ?
                ruleMappedAttr[0].attrNameValues.filter(attrNameValue =>
                    attrNameValue.attrValue === keyAttribute.attrValue) : [];
        }
        return matchedAttr.length > 0 ? matchedAttr[0].attributes as AttributeData[] : [];
    }

    private notifyIsKeyAttributeChanged(attrData: AttributeData, attributeAttributeMapping) {
        if (this.attributeHelper.isKeyAttribute(attrData.attrNameCde)) {
            this.postToFileContentComponent([attrData], attributeAttributeMapping);
        }
    }

    /**
     * This method is called to group attachment based on group name. </br>
     *
     * @param attachmentDataList
     */
    private groupAttachment(attachmentDataList: AttachmentData[]): Map<String, AttachmentData[]> {
        const attachmentGroup: Map<String, AttachmentData[]> = new Map();
        attachmentDataList.forEach(attachmentData => {
            let groupName = attachmentData.groupName;
            let existingList: AttachmentData[] = attachmentGroup.get(groupName);
            if (!this.utilityService.isListHasValue(existingList)) {
                existingList = [];
            }
            existingList.push(attachmentData);
            attachmentGroup.set(groupName, existingList);
        });
        return attachmentGroup;
    }
}
