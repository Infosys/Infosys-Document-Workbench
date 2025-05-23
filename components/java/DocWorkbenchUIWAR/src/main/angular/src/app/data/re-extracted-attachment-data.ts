/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { ReExtractedAttributeData } from './re-extracted-attribute-data';

export class ReExtractedAttachmentData {
    constructor(
        public attachmentId: number,
        public attachmentName: String,
        public attachmentSequence: number,
        public attributes: ReExtractedAttributeData[],
        public annotations: {
            value: string,
            label: string,
            occurrenceNum: number
        }[]) { }
}
