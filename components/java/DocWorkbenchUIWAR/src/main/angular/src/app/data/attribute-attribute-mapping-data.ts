/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { AttributeNameValueData } from './attribute-name-value-data';
import { AttributeData } from './attribute-data';

export class AttributeAttributeMappingData {
    constructor(public attrNameCde: number,
        public attrNameTxt: string,
        public attrNameValues: AttributeData[]
    ) { }
} 


