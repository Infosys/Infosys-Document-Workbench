/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


export class AttributeSortKeyData {
    constructor(public attrNameCde: number,
        public attrNameTxt: string,
        public attrNameValues: AttributeSKData[]
    ) { }
}

export class AttributeSKData {
    constructor(public attrValue: string,
        public attributes: AttributeSKFormData[]
    ) { }
}

export class AttributeSKFormData {
    constructor(public nonTabular: AttributeSKFormValueData[],
        public tabular: AttributeTabularSKFormValueData[]
    ) { }
}

export class AttributeTabularSKFormValueData {
    constructor(public orderColumnUsingAnyOfRegExp: AttributeTabularRegExpData[],
        public attributes: AttributeSKFormValueData[]
    ) { }
}

export class AttributeTabularRegExpData {
    constructor(public pattern: string,
        public flag: string
    ) { }
}


export class AttributeSKFormValueData {
    constructor(public attrNameCde: number,
        public attrNameTxt: string
    ) { }
}
