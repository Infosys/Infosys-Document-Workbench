/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { ActionData } from './action-data';

export class RecommendedActionData {

    constructor(
        public taskTypeCde: number,
        public recommendedPct: number,
        public confidencePct: number,
        public actionNameCde: number,
        public actionDataList: ActionData[]) { }

}
