/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { AttributeData } from './attribute-data';
import { ActionData } from './action-data';
import { AttachmentData } from './attachment-data';

export class DocumentData {
    constructor(
        public docId: number,
        public docTypeCde: number,
        public docLocation: string,
        public taskStatusCde: number,
        public taskStatusTxt: string,
        public eventTypeCde: number,
        public lockStatusCde: number,
        public appUserId: number,
        public assignedTo: string,
        public lockStatusTxt: string,
        public createDtm: String,
        public actionDataList: ActionData[],
        public attributes: AttributeData[],
        public attachments: AttachmentData[]
    ) { }

}
