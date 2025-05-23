/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


export class AttachmentAttributeData {

    constructor(
        public id: number,
        public attachmentId: number,
        public attrNameCde: number,
        public attrNameTxt: string,
        public attrValue: string,
        public extractTypeCde: number,
        public extractTypeTxt: string,
        public confidencePct: number,
        public isUpdated: boolean = false,
        public isDeleted: boolean = false,
        public isAdded: boolean = false,
        public attributes: AttachmentAttributeData[]) { }

}
