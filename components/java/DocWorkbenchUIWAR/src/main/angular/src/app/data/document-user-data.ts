/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

export class DocumentUserData {
    constructor(
        public userId: number,
        public userLoginId: string,
        public userEmail: string,
        public userFullName: string,
        public roleTypeCde: number,
        public roleTypeTxt: string,
        public docRoleTypeCde: number,
        public docRoleTypeTxt: string,
        public userTypeCde: number,
        public userTypeTxt: string
    ) { }
}
