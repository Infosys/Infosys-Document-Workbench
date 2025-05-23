/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { AttributeData } from './attribute-data';

export class AttachmentData {

    constructor(
        public attachmentId: number,
        public fileName: string,
        public extractTypeCde: number,
        public groupName: string,
        public attributes: AttributeData[],
        public sortOrder: number,
        public displayNumber: number =0) { }

}
