/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { FeatureAuthData } from './feature-auth-data';
import { CurrentUserQueueData } from './user-queue-data';

export class UserData {

    constructor(
        public userFullName: string,
        public userEmail: string,
        public userTypeTxt: string,
        public roleTypeTxt: string,
        public roleTypeCde: number,
        public queueDataList: CurrentUserQueueData[],
        public optionChecked: boolean,
        public userId: number,
        public userTypeCde: number,
        public userName: string,
        public accountEnabled: boolean,
        public userType: string,
        public isQueueAssigned: boolean,
        public uiShowQueueList: boolean, //For controlling queue list visibility per user
        public userSourceCde:number,
        public featureAuthDataList:FeatureAuthData[]) { }
}
