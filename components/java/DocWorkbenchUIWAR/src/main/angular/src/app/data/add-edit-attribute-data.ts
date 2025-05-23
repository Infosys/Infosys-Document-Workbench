/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


export class AddEditAttributeData {

    constructor(public attrNameCde: number,
        public id: number,
        public attrNameTxt: string,
        public attrValue: string,
        public extractTypeCde: number,
        public extractTypeTxt: string,
        public confidencePct: number,
        public attributes: AddEditAttributeData[]) { }

}
