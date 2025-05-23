/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


export class ParamAttrMappingData {

    constructor(public paramNameCde: number,
        public paramNameTxt: string,
        public paramValue: string,
        public attrNameCde: number,
        public attrNameTxt: string) { }

}
