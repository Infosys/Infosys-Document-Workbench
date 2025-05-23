/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


export enum EnumTaskStatus {
    UNDEFINED = 50,
    YET_TO_START = 100,
    IN_PROGRESS = 200,
    ON_HOLD = 300,
    FOR_YOUR_REVIEW = 400,
    RETRY_LATER = 500,
    COMPLETE = 900,
    FAILED
}
