/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


export class ReExtractedAttributeData {

    constructor(public attrNameCde: number,
        public docId: number,
        public isAdded: boolean,
        public isUpdated: boolean,
        public isDeleted: boolean,
        public isHiddenInUI: boolean,
        public remark: string,
        public id: number,
        public attrNameTxt: string,
        public attrValue: string,
        public attrValueTemp: string,
        public confidencePct: number,
        public extractTypeCde: number,
        public newExtractTypeCde: number,
        public newAttrValue: string,
        public newConfidencePct: number,
        public newAttrNameCde: number,
        public extractTypeTxt: string,
        public groupingNameTxtInUI: string,
        public createByUserTypeCde: number,
        public createByUserFullName: string,
        public createByUserLoginId: string,
        public lastModByUserTypeCde: number,
        public lastModByUserFullName: string,
        public lastModByUserLoginId: string,
        public attributes: ReExtractedAttributeData[]) { }
}
