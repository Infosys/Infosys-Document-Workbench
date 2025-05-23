/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { ParamAttrMappingData } from './param-attr-mapping-data';

export class ActionData {

    constructor(public actionNameCde: number,
        public actionNameTxt: string,
        public actionResult: string,
        public docActionRelId: number,
        public taskTypeCde: number,
        public taskStatusCde: number,
        public createByUserLoginId: string,
        public createByUserFullName: string,
        public createByUserTypeCde: number,
        public createByUserTypeTxt: string,
        public snapShot: string,
        public mappingList: ParamAttrMappingData[],
        public paramList: ParamAttrMappingData[]
    ) { }
}
