/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


export class NiaSortableColumnData {

    constructor(public sortColumn: string,
        public sortDirection: string,
        public sortTableId: string,
        public sortCustomCol: string,
        public sortableColCustomKey: {},
        public sortIdCol: string,
        public sortTableName: string,
        public sortMsg: string,
        public sortLevel2HeaderClass: string,
        public sortStartRow: string,
        public sortForceApply: boolean
    ) { }
}
