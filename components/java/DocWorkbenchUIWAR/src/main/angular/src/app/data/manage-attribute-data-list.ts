/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { AddEditAttributeData } from './add-edit-attribute-data';
import { DeleteAttributeData } from './delete-attribute-data';


export class ManageAttributeDataList {

    constructor(
        public id: number,
        public attrNameCde: number,
        public attrNameTxt: string,
        public attrValue: string,
        public extractTypeCde: number,
        public extractTypeTxt: string,
        public confidencePct: number,
        public addAttributes: AddEditAttributeData[],
        public editAttributes: AddEditAttributeData[],
        public deleteAttributes: DeleteAttributeData[]) { }

}
