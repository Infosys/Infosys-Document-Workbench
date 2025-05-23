/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { ManageAttributeDataList } from './manage-attribute-data-list';
import { ManageAttachmentAttributeDataList } from './manage-attachment-attribute-data-list';


export class ManageAttributeData {

    constructor(
        public docId: number,
        public docActionRelId: number,
        public attribute: ManageAttributeDataList,
        public attachment: ManageAttachmentAttributeDataList
    ) { }


}
