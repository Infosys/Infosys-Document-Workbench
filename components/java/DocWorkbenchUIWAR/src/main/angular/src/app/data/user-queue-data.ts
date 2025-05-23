/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { QueueData } from './queue-data';

export class CurrentUserQueueData extends QueueData{

    constructor(
        public queueNameCde: number,
        public queueNameTxt: string,
        public userQueueHideAfterDtm: string,
        public queueHideAfterDtm: string,
        public queueClosedDtm: string,
        public queueStatus: string,
        public isDateValueChanged: boolean,
        public isClosureDateValueChanged: boolean,        
        public isVisible: boolean
        ) {
            super(queueNameCde,queueNameTxt,null,null)
        }   
}
