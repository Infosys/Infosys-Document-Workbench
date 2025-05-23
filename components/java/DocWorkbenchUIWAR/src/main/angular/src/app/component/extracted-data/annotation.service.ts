/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { UtilityService } from '../../service/utility.service';
import { CONSTANTS } from '../../common/constants';
import { ExtractedDataHelper } from './extracted-data-helper';
import { Injectable } from '@angular/core';
import { AttributeData } from '../../data/attribute-data';
import { ToastrService } from 'ngx-toastr';
import { MessageInfo } from '../../utils/message-info';
import { AttributeHelper } from '../../utils/attribute-helper';
import { AttributeValidationData } from '../../data/attribute-validation-data';
import { DataService } from '../../service/data.service';

export class EDData {

    constructor(public attributeDataEditList,
        public deleteAttributeList,
        public isReextractionPendingExist,
        public attributeList,
        public selectedAttrNameCde,
        public selectedAttrNameTxtList,
        public attributeValidationResult
    ) { }
}

@Injectable()
export class AnnotationService {

    constructor(private utilityService: UtilityService, private extractedDataHelper: ExtractedDataHelper,
        private toastrService: ToastrService, private msgInfo: MessageInfo, private attributeHelper: AttributeHelper,
        private dataService: DataService) { }

    deleteAttrForAnnotation(annotationData, model, edDataObj: EDData) {
        if (annotationData.count <= 0) {
            if (annotationData.annotation.attrId === undefined) {
                this.processDelForSavedData(annotationData, false, model, edDataObj);
            } else {
                this.processDelForUnsavedData(annotationData, false, model, edDataObj.attributeList, edDataObj.attributeDataEditList,
                    edDataObj.deleteAttributeList);
            }
        } else if (annotationData.editCount <= 0) {
            if (annotationData.annotation.attrId === undefined) {
                this.processDelForSavedData(annotationData, true, model, edDataObj);
            } else {
                this.processDelForUnsavedData(annotationData, true, model, edDataObj.attributeList, edDataObj.attributeDataEditList,
                    edDataObj.deleteAttributeList);
            }
        } else {
            model.isSaveAllowed = true;
            model.isCancelAllowed = true;
        }
    }

    // To check validation for annotated attributes and perform manipulation for annotated attribute list generation.
    processAnnotatedData(attributeData: AttributeData, model, edDataObj: EDData): void {
        if (attributeData.attrNameTxt.toLowerCase() === CONSTANTS.ATTRIBUTES.ATTR_NAME_TXT.DOCUMENT_TYPE.toLowerCase()) {
            this.extractedDataHelper.publishAttributeOpDataEvent(CONSTANTS.OPERATION_TYPE.DELETE, attributeData, model.isDocTypeFile,
                false);
            this.toastrService.error(this.msgInfo.getMessage(154));
            return;
        }
        const dbDataList = model.attachmentAttrDataList[0].attributes;
        let overAllDataExist;
        const isDbDataExist = this.checkAttributeDataDuplicates(dbDataList, attributeData, model.isDocTypeFile, edDataObj);
        if (!isDbDataExist) {
            if (!edDataObj.attributeValidationResult) {
                this.extractedDataHelper.publishAttributeOpDataEvent(CONSTANTS.OPERATION_TYPE.DELETE, attributeData, model.isDocTypeFile,
                    false);
                this.toastrService.error(this.msgInfo.getMessage(175));
                return;
            } else {
                const isManualAddDataExist = this.checkAttributeDataDuplicates(model.addAttributeDataList, attributeData,
                    model.isDocTypeFile, edDataObj, true);
                const isAnnotateDataExist = this.checkAttributeDataDuplicates(model.annotatedAttributeDataList, attributeData,
                    model.isDocTypeFile, edDataObj);
                overAllDataExist = isDbDataExist || isManualAddDataExist || isAnnotateDataExist;
            }
        } else {
            overAllDataExist = isDbDataExist;
        }
        if (!overAllDataExist) {
            let isDataAdded = false;
            if (!model.isAddAttachmentAttrClicked && model.annotatedAttributeDataList.length <= 0) {
                this.extractedDataHelper.filterNonListedAttributes(dbDataList, model, edDataObj);
            }
            attributeData.isAttrValueChanged = true;
            // Validation for differentiate normal and multi attribute
            if (this.utilityService.isListHasValue(edDataObj.attributeList)) {
                for (let i = 0; i < edDataObj.attributeList.length; i++) {
                    const tempAttrData = edDataObj.attributeList[i];
                    if (tempAttrData.attrNameTxt.toLowerCase() === attributeData.attrNameTxt.toLowerCase()) {
                        attributeData.attrNameCde = tempAttrData.attrNameCde;
                        model.annotatedAttributeDataList.push(attributeData);
                        isDataAdded = true;
                        model.attributeDataValList = model.attributeDataValList.filter(attrData =>
                            attrData.attrNameCde !== attributeData.attrNameCde);
                        break;
                    }
                }
            }
            if (!isDataAdded) {
                const subAttributes = [attributeData];
                const multiAttributeDataList: AttributeData[] = model.annotatedAttributeDataList.filter(
                    attrData => this.attributeHelper.isMultiAttribute(attrData.attrNameCde));
                if (this.utilityService.isListHasValue(multiAttributeDataList)) {
                    multiAttributeDataList[0].attributes = multiAttributeDataList[0].attributes.concat(subAttributes);
                } else {
                    model.annotatedAttributeDataList.push(new AttributeData(CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE, null, null, null, null,
                        null, null, CONSTANTS.ATTRIBUTES.EXTRACT_TYPE_CDE.DIRECT_COPY,
                        null, CONSTANTS.ATTRIBUTES.CONFIDENCE_PCT.UNDEFINED, subAttributes, [], null, null,
                        new AttributeValidationData(null, null), true));
                }
                this.extractedDataHelper.validateAttrNameTxt(attributeData, model, edDataObj.selectedAttrNameTxtList);
            }
        }
        this.extractedDataHelper.isSaveButtonAllowed(model, edDataObj.attributeDataEditList, edDataObj.deleteAttributeList);
        edDataObj.attributeValidationResult = this.extractedDataHelper.checkIfNoValidationErrExists(model, edDataObj.attributeDataEditList);
        model.isCancelAllowed = true;
        if (!model.selectedAttachmentId) {
            model.selectedAttachmentId = this.extractedDataHelper.getMainAttachmentData(
                model.attachmentDataList)[0].attachmentId;
        }
    }

    // To add index to attribute from the annotation.
    addAnnIndexToAttribute(annotationData, model) {
        const savedAnnotations = annotationData.annotations.filter(ann => ann.attrId === undefined);
        const userAddedAnnotations = annotationData.annotations.filter(ann => ann.attrId !== undefined &&
            ann.op === CONSTANTS.OPERATION_TYPE.ADD);
        this.assignIndexToSavedAttrs(annotationData.attachmentId, savedAnnotations, model);
        this.assignIndexToUnsavedAttrs(userAddedAnnotations, model);
    }

    // AnnotateChange event
    moveAnnotateDataToAddAttributeData(attrData: AttributeData, model, edDataObj: EDData) {
        model.isAddAttachmentAttrClicked = true;
        this.extractedDataHelper.filterNonListedAttributes(model.attachmentAttrDataList[0].attributes, model, edDataObj);
        model.isSaveAllowed = false;
        model.addAttributeDataList.push(attrData);
        model.annotatedAttributeDataList = this.attributeHelper.removeAttribute(attrData, model.annotatedAttributeDataList);
    }

    // To send publish event read mode activated/deactivated for file content.
    notifyFCToSwitchReadMode(isDocTypeFile: boolean, mode: number) {
        if (isDocTypeFile) {
            this.dataService.publishExtractedDataCustomEvent(mode);
        }
    }

    private processDelForSavedData(annotationData: any, isEdit: boolean, model, edDataObj: EDData) {
        let isContinueExecution = true;
        model.attachmentAttrDataList.forEach(attachmentAttrData => {
            if (isContinueExecution) {
                if (attachmentAttrData['attachmentId'] === annotationData.attachmentId) {
                    attachmentAttrData.attributes.forEach(attribute => {
                        if (isContinueExecution) {
                            if (attribute.attributes === null) {
                                if (this.callModifyAttribute(attribute, annotationData, isEdit, model, edDataObj)) {
                                    isContinueExecution = false;
                                }
                            } else {
                                attribute.attributes.forEach(attr => {
                                    if (isContinueExecution && this.callModifyAttribute(attr, annotationData, isEdit, model, edDataObj)) {
                                        isContinueExecution = false;
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }
    private callModifyAttribute(attribute, annotationData, isEdit, model, edDataObj: EDData) {
        const attrValues: String[] = this.utilityService.isStringHasValue(attribute.attrValue) ?
            attribute.attrValue.split(CONSTANTS.ATTRIBUTES.DELIMITER) : [];
        const remainingAttrVals: String[] = attrValues.filter(val =>
            !this.utilityService.getIfStringsMatch(val.toLowerCase(), annotationData.annotation.quote.toLowerCase()));
        const filteredValues: String[] = attrValues.filter(val =>
            this.utilityService.getIfStringsMatch(val.toLowerCase(), annotationData.annotation.quote.toLowerCase()));
        if (attribute.attrNameTxt.toLowerCase() === annotationData.annotation.text.toLowerCase() &&
            filteredValues.length > 0) {
            if (isEdit) {
                this.processAttributeForAnnEdit(remainingAttrVals, filteredValues, attribute, model, edDataObj);
            } else {
                if (filteredValues.length < attrValues.length) {
                    this.processAttributeForAnnEdit(remainingAttrVals, filteredValues, attribute, model, edDataObj);
                    if (attribute.attrValue === attribute.attrValueOrg) {
                        attribute['index'] = undefined;
                    }
                } else {
                    this.extractedDataHelper.deleteAttribute(attribute, model, edDataObj, annotationData.annotation);
                }
            }
            return true;
        }
        return false;
    }

    private processAttributeForAnnEdit(attrValues: String[], filteredValues: String[], attribute: any, model, edDataObj: EDData) {
        // attrValues.splice(attrValues.lastIndexOf(filteredValues[0]), 1);
        attribute.attrValue = attrValues.join(CONSTANTS.ATTRIBUTES.DELIMITER);
        if (this.utilityService.getIfDuplicatesExist(attrValues)) {
            attribute.attrNameValidation.attrValueValidationResult = { 'attrValueDuplicate': true };
        } else if (this.utilityService.isStringHasValue(attribute.attrValue)) {
            attribute.attrNameValidation.attrValueValidationResult = undefined;
        }
        edDataObj.attributeDataEditList = edDataObj.attributeDataEditList.filter(attr => attr.id !== attribute.id);
        if (attribute.attrValue !== attribute.attrValueOrg) {
            attribute.isAttrValueChanged = true;
            edDataObj.attributeDataEditList.push(attribute);
        } else {
            attribute.isAttrValueChanged = false;
        }
        this.extractedDataHelper.isSaveButtonAllowed(model, edDataObj.attributeDataEditList, edDataObj.deleteAttributeList);
        model.isCancelAllowed = true;
    }

    private processDelForUnsavedData(annotationData, isEdit: boolean, model, attributeList: AttributeData[],
        attributeDataEditList: AttributeData[], deleteAttributeList: AttributeData[]) {

        model.annotatedAttributeDataList.
            forEach(data => {
                if (data.attributes === null && data.attrId === annotationData.annotation.attrId) {
                    if (!this.getIfAttrValueIsEdited(data, annotationData.annotation, isEdit)) {
                        this.extractedDataHelper.removeAnnotatedAttribute(data, model, attributeDataEditList, deleteAttributeList, false);
                    }
                } else {
                    const dataList = data.attributes.filter(multiAttrData => multiAttrData.attrId === annotationData.annotation.attrId);
                    if (this.utilityService.isListHasValue(dataList)) {
                        if (!this.getIfAttrValueIsEdited(dataList[0], annotationData.annotation, isEdit)) {
                            this.extractedDataHelper.removeAnnotatedAttribute(data, model, attributeDataEditList, deleteAttributeList,
                                false);
                        }
                    }
                }
            });
        model.addAttributeDataList.forEach(attrData => {
            if (attrData.attrId === annotationData.annotation.attrId) {
                if (!this.getIfAttrValueIsEdited(attrData, annotationData.annotation, isEdit)) {
                    this.extractedDataHelper.removeManuallyAddedAttribute(attrData, model, attributeList, attributeDataEditList,
                        deleteAttributeList, false);
                }
            }
        });
    }

    private getIfAttrValueIsEdited(attrData: AttributeData, annotation, isEdit: boolean) {
        let isEditOp = true;
        const attrValues: String[] = attrData.attrValue.split(CONSTANTS.ATTRIBUTES.DELIMITER);
        const filteredValues: String[] = attrValues.filter(val =>
            this.utilityService.getIfStringsMatch(val.toLowerCase(), annotation.quote.toLowerCase()));
        const remainingAttrVals: String[] = attrValues.filter(val =>
            !this.utilityService.getIfStringsMatch(val.toLowerCase(), annotation.quote.toLowerCase()));
        if (isEdit || filteredValues.length < attrValues.length) {
            if (filteredValues.length < attrValues.length) {
                attrData['index'] = undefined;
            }
            attrData.attrValue = remainingAttrVals.join(CONSTANTS.ATTRIBUTES.DELIMITER);
            if (this.utilityService.getIfDuplicatesExist(remainingAttrVals)) {
                attrData.attrNameValidation.attrValueValidationResult = { 'attrValueDuplicate': true };
            } else if (this.utilityService.isStringHasValue(attrData.attrValue)) {
                attrData.attrNameValidation.attrValueValidationResult = undefined;
            }
        } else {
            isEditOp = false;
        }
        return isEditOp;
    }

    // To check for duplicates for annotation content.
    private checkAttributeDataDuplicates(attrDataList: AttributeData[], attributeData: AttributeData, isDocTypeFile: boolean,
        edDataObj: EDData, isManualAddList?: boolean) {
        const attrNameTxtList = this.attributeHelper.getAttrNameTxtList(attrDataList, []);
        const isFound = attrNameTxtList.indexOf(attributeData.attrNameTxt.toLowerCase()) !== -1;
        if (isFound) {
            if (isManualAddList) {
                attrDataList.forEach(attrData => this.appendAnnotateAttrValue(attrData, attributeData,
                    edDataObj.attributeDataEditList));
            } else {
                this.processAttributeDataList(attrDataList, attributeData, isDocTypeFile, edDataObj);
            }
        }
        return isFound;
    }

    // To append attr value if duplicate exist.
    private appendAnnotateAttrValue(attrDbData: AttributeData, attrAnnotateData: AttributeData, attributeDataEditList: AttributeData[]) {
        let skipAttrCount = 0;
        if (attrDbData.attrNameCde !== CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE_TABLE ||
            attrDbData.attrNameCde === CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE) {
            if (attrDbData.attrNameTxt.toLowerCase() === attrAnnotateData.attrNameTxt.toLowerCase()) {
                if (!attrDbData.deleteClicked && this.utilityService.isStringHasValue(attrDbData.attrValue)) {
                    const values = attrDbData.attrValue.toLowerCase().split(CONSTANTS.ATTRIBUTES.DELIMITER);
                    values.push(attrAnnotateData.attrValue.toLowerCase());
                    if (!this.utilityService.getIfDuplicatesExist(values)) {
                        attrDbData.attrValue += CONSTANTS.ATTRIBUTES.DELIMITER + attrAnnotateData.attrValue;
                    }
                } else {
                    attrDbData.attrValue = attrAnnotateData.attrValue;
                    attrDbData.deleteClicked = false;
                }
                if (this.utilityService.isAValidValue(attrDbData.attrNameValidation.attrValueValidationResult)
                    && attrDbData.attrNameValidation.attrValueValidationResult['attrValueDuplicate']) {
                    skipAttrCount++;
                } else {
                    attrDbData.attrNameValidation.attrValueValidationResult = undefined;
                    attrDbData.isAttrValueChanged = true;
                    // TODO Rachana - check below variable is needed or not.
                    attrAnnotateData.isAttrValueChanged = true;
                    if (!this.utilityService.isAValidValue(attrDbData['attrId'])) {
                        attributeDataEditList.push(attrDbData);
                    } else {
                        if (attrDbData['index'] === undefined) {
                            attrDbData['attrId'] = attrAnnotateData['attrId'];
                        }
                    }
                }
            }
        } else {
            skipAttrCount++;
        }
        return skipAttrCount;
    }

    // To call append attr value method either normal or multi attribute using recursive.
    private processAttributeDataList(attributeDbDataList: AttributeData[], attributeData: AttributeData, isDocTypeFile: boolean,
        edDataObj: EDData) {
        let attrSkipCount = 0;
        attributeDbDataList.forEach(attrData => {
            if (!attributeData.isAttrValueChanged) {
                if (this.attributeHelper.isMultiAttribute(attrData.attrNameCde)) {
                    this.processAttributeDataList(attrData.attributes, attributeData, isDocTypeFile, edDataObj);
                } else {
                    attrSkipCount += this.appendAnnotateAttrValue(attrData, attributeData, edDataObj.attributeDataEditList);
                }
            }
        });
        if (attrSkipCount > 0 && !attributeData.isAttrValueChanged) {
            this.extractedDataHelper.publishAttributeOpDataEvent(CONSTANTS.OPERATION_TYPE.DELETE, attributeData, isDocTypeFile, true);
            this.toastrService.error(this.msgInfo.getMessage(edDataObj.attributeValidationResult ? 174 : 175));
        }
    }

    private assignIndexToSavedAttrs(attachmentId, savedAnnotations, model) {
        if (this.utilityService.isListHasValue(savedAnnotations) &&
            this.utilityService.isListHasValue(model.attachmentAttrDataList)) {
            model.attachmentAttrDataList.forEach(attachmentAttrData => {
                if (attachmentAttrData['attachmentId'] === attachmentId) {
                    attachmentAttrData.attributes.forEach(attribute => {
                        if (attribute.attrNameCde !== CONSTANTS.ATTR_NAME_CDE.CATEGORY &&
                            attribute.attrNameCde !== CONSTANTS.ATTR_NAME_CDE.DOCUMENT_TYPE) {
                            if (attribute.attributes === null) {
                                attribute['index'] = undefined;
                                this.assignIndexToAttribute(savedAnnotations, attribute);
                            } else {
                                attribute.attributes.forEach(attr => {
                                    attr['index'] = undefined;
                                    this.assignIndexToAttribute(savedAnnotations, attr);
                                });
                            }
                        }
                    });
                }
            });
        }
    }

    private assignIndexToUnsavedAttrs(userAddedAnnotations, model) {
        // if (this.utilityService.isListHasValue(userAddedAnnotations)) {
        model.annotatedAttributeDataList.
            forEach(data => {
                if (data.attributes === null) {
                    data['index'] = undefined;
                    this.assignIndexToAttribute(userAddedAnnotations, data);
                } else {
                    data.attributes.forEach(attr => {
                        attr['index'] = undefined;
                        this.assignIndexToAttribute(userAddedAnnotations, attr);
                    });
                }
            });
        model.addAttributeDataList.forEach(attrData => {
            attrData['index'] = undefined;
            this.assignIndexToAttribute(userAddedAnnotations, attrData);
        });
        // }
    }

    private assignIndexToAttribute(annotationList: any, attribute: AttributeData) {
        if (this.utilityService.isStringHasValue(attribute.attrNameTxt) &&
            this.utilityService.isStringHasValue(attribute.attrValue)) {
            const attrValues: String[] = this.utilityService.isStringHasValue(attribute.attrValue) ?
                attribute.attrValue.split(CONSTANTS.ATTRIBUTES.DELIMITER) : [];
            const subIndexArray = [];
            annotationList.forEach(ann => {
                if (ann.text.toLowerCase() === attribute.attrNameTxt.toLowerCase() && attrValues.filter(val =>
                    this.utilityService.getIfStringsMatch(val.toLowerCase(), ann.quote.toLowerCase())).length > 0) {
                    const index: string = ann.index + '';
                    const indexArray = index.split('.');
                    const attrIndex = indexArray.length === 2 ? indexArray[0] : ann.index;
                    attribute['index'] = attrIndex;
                    if (indexArray.length === 2) {
                        subIndexArray.push({ 'value': ann.quote, 'id': ann.id, 'index': indexArray[1] });
                    }
                }
            });
            if (this.utilityService.isListHasValue(subIndexArray)) {
                attribute['subIndex'] = subIndexArray;
            }
        }
    }
}
