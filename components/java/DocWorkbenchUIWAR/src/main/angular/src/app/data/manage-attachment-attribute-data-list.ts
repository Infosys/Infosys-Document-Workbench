/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { ManageAttributeDataList } from './manage-attribute-data-list';


export class ManageAttachmentAttributeDataList {

    constructor(
        public attachmentId: number,
        public attribute: ManageAttributeDataList) { }

}
