/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { AttributeNameValueData } from './attribute-name-value-data';
import { AttributeValidationData } from './attribute-validation-data';

export class AttributeData {

    constructor(public attrNameCde: number,
        public docId: number,
        public attachmentId: number,
        public optionChecked: boolean,
        public id: number,
        public attrNameTxt: string,
        public attrValue: string,
        public extractTypeCde: number,
        public extractTypeTxt: string,
        public confidencePct: number,
        public attributes: AttributeData[],
        public allowedValues: AttributeNameValueData[],
        public notification: string,
        public deleteClicked: boolean,
        public attrNameValidation: AttributeValidationData,
        public isAttrValueChanged = false
    ) { }

}
