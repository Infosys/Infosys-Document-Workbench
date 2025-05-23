/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


export class AuditData {

    constructor(
        public docId: number,
        public appUserId: number,
        public queueNameCde: number,
        public auditLoginId: String,
        public auditMessage: String,
        public appVarKey:string=""        
        ) { }

}
