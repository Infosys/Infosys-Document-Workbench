/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


export class ResponseData {

    constructor(public response: any,
        public responseCde: number,
        public responseMsg: string,
        public responseTimeInSecs: number,
        public timestamp: string){}
}
