/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Injectable } from '@angular/core';
import { AttributeData } from '../data/attribute-data';
import { CONSTANTS } from '../common/constants';
import { UtilityService } from '../service/utility.service';
import { DocumentData } from '../data/document-data';
import { AttachmentData } from '../data/attachment-data';

@Injectable()
export class AttributeHelper {

    constructor(private utilityService: UtilityService) { }

    // To get attribute data list without Direct copy attributes.
    filteredAttrDataList(attributeDataList: AttributeData[],getAttrUpstreamDocId:boolean) {
        const attrDataList: AttributeData[] = [];
        attributeDataList = attributeDataList.filter(data => data.attrNameCde !== CONSTANTS.ATTR_NAME_CDE.SENTIMENT &&
            data.attrNameCde !== CONSTANTS.ATTR_NAME_CDE.CONTENT_ANNOTATION);
        attributeDataList.forEach(function (attrData) {
            if (attrData.attrNameCde === CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE_TABLE) {
                attrData['isOptionCheckAllowed'] = true;
            }
            
            if (attrData.attributes !== null && attrData.attributes !== undefined) {
                const multiAttrDataList: AttributeData[] = [];
                attrData.attributes.forEach(function (multiAttrData) {
                    if ((multiAttrData.attributes != null && multiAttrData.attributes.length > 0)
                        || multiAttrData.extractTypeCde !== CONSTANTS.ATTRIBUTES.EXTRACT_TYPE_CDE.DIRECT_COPY) {
                        multiAttrData['attrValueOrg'] = multiAttrData.attrValue;
                        if (attrData.attrNameCde === CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE) {
                            multiAttrData['isOptionCheckAllowed'] = true;
                        }
                        multiAttrDataList.push(multiAttrData);
                    }
                });
                attrData.attributes = multiAttrDataList;
                attrDataList.push(attrData);
            } else if (getAttrUpstreamDocId && attrData.attrNameCde === CONSTANTS.ATTR_NAME_CDE.UPSTREAM_DOCID) {
                attrDataList.push(attrData);
            }
            else if (attrData.extractTypeCde !== CONSTANTS.ATTRIBUTES.EXTRACT_TYPE_CDE.DIRECT_COPY) {
                attrData['attrValueOrg'] = attrData.attrValue;
                attrData['isOptionCheckAllowed'] = true;
                attrDataList.push(attrData);
            }
        });
        return attrDataList;
    }

    // To fetch attibutes entities in seperate list.
    generateList(attributeDataList: AttributeData[], attrNameCdeList: number[], attrNameTxtList: string[],
        attrValueList: string[], attrConfidencePctList?: number[]) {
        if (attrConfidencePctList === undefined) {
            attrConfidencePctList = [];
        }
        attributeDataList.forEach(function (attrData) {
            if (attrData.attributes != null) {
                attrData.attributes.forEach(function (multiAttrData) {
                    const attrNameTxt = multiAttrData.attrNameTxt;
                    if (attrNameTxtList.includes(attrNameTxt)) {
                        const index = attrNameTxtList.indexOf(attrNameTxt);
                        if (attrValueList[index] !== null &&
                            attrValueList[index].split(CONSTANTS.ATTRIBUTES.ATTR_VALUE_DELIMITER).indexOf(multiAttrData.attrValue) < 0) {
                            attrValueList[index] += CONSTANTS.ATTRIBUTES.ATTR_VALUE_DELIMITER + multiAttrData.attrValue;
                        }
                    } else {
                        if(!attrNameTxt.endsWith('::list')){
                            attrNameCdeList.push(attrData.attrNameCde);
                            attrNameTxtList.push(attrNameTxt);
                            attrValueList.push(multiAttrData.attrValue);
                            attrConfidencePctList.push(multiAttrData.confidencePct);
                        }
                    }
                });
            } else {
                const attrNameTxt = attrData.attrNameTxt;
                if (attrNameTxtList.includes(attrNameTxt)) {
                    const index = attrNameTxtList.indexOf(attrNameTxt);
                    if (attrValueList[index].split(CONSTANTS.ATTRIBUTES.ATTR_VALUE_DELIMITER).indexOf(attrData.attrValue) < 0) {
                        attrValueList[index] += CONSTANTS.ATTRIBUTES.ATTR_VALUE_DELIMITER + attrData.attrValue;
                    }
                } else {
                    if(!attrNameTxt.endsWith('::list')){
                        attrNameCdeList.push(attrData.attrNameCde);
                        attrNameTxtList.push(attrNameTxt);
                        attrValueList.push(attrData.attrValue);
                        attrConfidencePctList.push(attrData.confidencePct);
                    }
                }
            }
        });
    }

    removeAttributeDataOptionFields(attributeData) {
        const deleteKey = ['isAdded', 'isUpdated', 'isDeleted', 'remark', 'newExtractTypeCde',
            'newAttrValue', 'attrValueTemp', 'newConfidencePct', 'newAttrNameCde', 'attachmentName', 'attachmentSequence'];
        const parent = this;
        for (const key in attributeData) {
            if (attributeData.hasOwnProperty(key)) {
                if (deleteKey.indexOf(key) > -1) {
                    delete attributeData[key];
                } else {
                    if (Array.isArray(attributeData[key]) && attributeData[key].length > 0) {
                        attributeData[key].forEach(innerKey => {
                            parent.removeAttributeDataOptionFields(innerKey);
                        });
                    }
                    if (attributeData[key] instanceof Object) {
                        parent.removeAttributeDataOptionFields(attributeData[key]);
                    }
                }
            }
        }
    }

    // To get attrNameTxt alone for given attributeList
    getAttrNameTxtList(attrDataList: AttributeData[], attrNameTxtList: string[]): string[] {
        if (this.utilityService.isListHasValue(attrDataList)) {
            attrDataList.forEach(attrData => {
                const attrNameTxt = attrData.attrNameTxt;
                if (attrData.attrNameCde === CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE_TABLE
                    && this.utilityService.isStringHasValue(attrNameTxt)) {
                    attrNameTxtList.push(attrNameTxt.toLowerCase().trim());
                } else if (this.utilityService.isListHasValue(attrData.attributes)) {
                    attrNameTxtList = this.getAttrNameTxtList(attrData.attributes, attrNameTxtList);
                } else if (this.utilityService.isStringHasValue(attrNameTxt)) {
                    attrNameTxtList.push(attrNameTxt.toLowerCase().trim());
                }
            });
        }
        return attrNameTxtList;
    }

    // To convert tree to flat structure for Annotation add data list for validation.
    convertFlatStructure(attrDataList: AttributeData[]) {
        let attributeDataList = [];
        attrDataList.forEach(attrData => {
            if (this.utilityService.isListHasValue(attrData.attributes)) {
                attributeDataList = attributeDataList.concat(this.convertFlatStructure(attrData.attributes));
            } else {
                attributeDataList.push(attrData);
            }
        });
        return attributeDataList;
    }

    // To remove attribute from given attribute list.
    removeAttribute(attrData: AttributeData, attrDataList: AttributeData[]): AttributeData[] {

        if (this.utilityService.isListHasValue(attrDataList.filter(attributeData => attributeData === attrData))) {
            attrDataList = attrDataList.filter(attributeData => attributeData !== attrData);
        } else {
            let isSubAttrEmpty = false;
            attrDataList.forEach(attributeData => {
                if (this.utilityService.isListHasValue(attributeData.attributes)) {
                    attributeData.attributes = attributeData.attributes.filter(data => data !== attrData);
                    // To remove parent attribute if there is no sub attributes.
                    if (!this.utilityService.isListHasValue(attributeData.attributes)) {
                        isSubAttrEmpty = true;
                    }
                }
            });
            if (isSubAttrEmpty) {
                attrDataList = attrDataList.filter(data => data.attrNameCde !== CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE ||
                    this.utilityService.isListHasValue(data.attributes));
            }
        }
        return attrDataList;
    }

    // To check whether given attrNameCde is Multi attribute.
    isMultiAttribute(attrNameCde: number): boolean {
        return +attrNameCde === CONSTANTS.ATTR_NAME_CDE.MULTI_ATTRIBUTE;
    }

    isKeyAttribute(attrNameCde: number): boolean {
        return +attrNameCde === CONSTANTS.ATTR_NAME_CDE.CATEGORY || +attrNameCde === CONSTANTS.ATTR_NAME_CDE.DOCUMENT_TYPE;
    }
    //To Check whether given attrribute is WorkflowDocId and attrNameCde is 11 
    isUpstreamDocId(attrNameCde: number): boolean {
        return +attrNameCde === CONSTANTS.ATTR_NAME_CDE.UPSTREAM_DOCID;
    }
    getTelemetryEventActionParams(document:any,attachmentAttrDataList:any, docUserDataList:any) {
        const cdata = [];
        cdata.push({"type":"queueNameCde","id":document["queueNameCde"]});
        cdata.push({"type":"docId","id":document["docId"]});
        if (attachmentAttrDataList.length>0){
          const attachData = attachmentAttrDataList[0].attributes.filter(x=>x.attrNameCde==31)[0];
          cdata.push({"type":"attachmentId","id":attachData.attachmentId});
          cdata.push({"type":"documentType","id":attachData.attrValue});
        }
        for (let docUserData of docUserDataList) {
          if(docUserData.docRoleTypeCde==CONSTANTS.DOC_ROLE_TYPE.CASE_OWNER){
            cdata.push({"type":"maker","id":"["+docUserData.roleTypeTxt+"]-"+docUserData.userLoginId});
          }else if(docUserData.docRoleTypeCde==CONSTANTS.DOC_ROLE_TYPE.CASE_REVIEWER){
            cdata.push({"type":"checker","id":"["+docUserData.roleTypeTxt+"]-"+docUserData.userLoginId});
          }
        }
        return cdata;
      }
}
