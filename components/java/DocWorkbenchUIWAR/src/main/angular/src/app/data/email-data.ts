/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { AttachmentData } from './attachment-data';

export class EmailData {
    constructor(public docId: number,
        public emailOutboundId: number,
        public docOutgoingEmailRelId: number,
        public emailTo: string,
        public emailBodyText: string,
        public emailBodyHtml: string,
        public emailSubject: string,
        public emailSentDtm: string,
        public emailCC: string,
        public emailBCC: string,
        public createByUserLoginId: string,
        public createByUserFullName: string,
        public createByUserTypeCde: number,
        public createByUserTypeTxt: string,
        public attachmentDataList: AttachmentData[]) { }

}
