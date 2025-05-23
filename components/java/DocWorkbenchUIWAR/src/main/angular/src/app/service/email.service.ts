/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { EmailData } from '../data/email-data';
import { CONSTANTS } from '../common/constants';
import { EnumTaskStatus } from '../common/task-status.enum';
import { ConfigDataHelper } from '../utils/config-data-helper';
import { SessionService } from './session.service';


@Injectable()
export class EmailService {

    constructor(private httpClient: HttpClient, private configDataHelper: ConfigDataHelper,
        private sessionService: SessionService) { }

    getSentEmailList(docId: number, callback) {
        const parent = this;
        if (!parent.sessionService.getFeatureAccessModeDataFor(CONSTANTS.FEATURE_ID_CONFIG.OUTBOUND_EMAIL_VIEW).isVisible) {
            callback(true, null);
        } else {
            let url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) +
                CONSTANTS.APIS.DOCWBSERVICE.GET_SENT_EMAIL_LIST;
            let queryParams: string = "";
            if (docId > 0) {
                queryParams += "docId=" + docId;
            }
            if (queryParams.length > 0) {
                url += "?" + queryParams;
            }

            this.httpClient.get(url)
                .subscribe(
                    data => {
                        let emailDataList: EmailData[] = data['response'];
                        callback(null, emailDataList);
                    },
                    error => {
                        callback(error, null);
                    }
                );
        }
        return null;
    }

    getEmailDraftData(docId: number, callback) {
        const parent = this;
        let url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) +
            CONSTANTS.APIS.DOCWBSERVICE.GET_DRAFT_EMAIL;
        let queryParams: string = "";
        if (docId > 0) {
            queryParams += "docId=" + docId;
        }
        if (queryParams.length > 0) {
            url += "?" + queryParams;
        }
        this.httpClient.get(url)
            .subscribe(
                data => {
                    let draftData: EmailData = data['response'];
                    callback(null, draftData);
                },
                error => {
                    callback(error, null);
                }
            );
        return null;
    }

    sendMail(docId: number, to, ccId, bcc, subject, templateText, templateName, formData: FormData, callback) {
        const parent = this;
        let url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) + CONSTANTS.APIS.DOCWBSERVICE.SEND_EMAIL;
        console.log("postData()");
        const data = {
            "docId": docId,
            "emailTo": to,
            "emailCC": ccId,
            "emailBCC": bcc,
            "emailSubject": subject,
            "emailBodyHtml": templateText,
            "templateName": templateName
        };

        formData.append('emailData', new Blob([JSON.stringify(data)], {
            type: "application/json"
        }));

        this.httpClient.post(url,
            formData
        ).subscribe(
            data => {
                callback(null, data);
            },
            error => {
                callback(error, null);
            }
        );
    }

    addDraft(docId: number, to, emailCc, bcc, templateText, emailSubject, callback) {
        const parent = this;
        let url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) +
            CONSTANTS.APIS.DOCWBSERVICE.SAVE_EMAIL_DRAFT;
        console.log("postData()");
        const data =
        {
            "docId": docId,
            "docOutgoingEmailRelId": 0,
            "emailBCC": bcc,
            "emailBodyHtml": templateText,
            "emailCC": emailCc,
            "emailData": "",
            "emailFrom": "",
            "emailFromId": "",
            "emailSentDtm": "",
            "emailSubject": emailSubject,
            "emailTo": to,
            "taskStatusCde": EnumTaskStatus.UNDEFINED
        };
        this.httpClient.post(url,
            data
        ).subscribe(

            data => {
                callback(null, data);
            },
            error => {
                callback(error, null);
            }
        );
    }

    updateDraft(docId: number, to, cC, bcc, templateText, emailSubject, docOutgoingEmailRelId, callback) {
        const parent = this;
        let url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) +
            CONSTANTS.APIS.DOCWBSERVICE.SAVE_EMAIL_DRAFT;
        console.log("postData()");
        console.log("updatedTest" + templateText);
        const data =
        {
            "docId": docId,
            "docOutgoingEmailRelId": docOutgoingEmailRelId,
            "emailBCC": bcc,
            "emailBodyHtml": templateText,
            "emailCC": cC,
            "emailData": "",
            "emailFrom": "",
            "emailFromId": "",
            "emailSentDtm": "",
            "emailSubject": emailSubject,
            "emailTo": to,
            "taskStatusCde": EnumTaskStatus.UNDEFINED
        };
        this.httpClient.post(url,
            data
        ).subscribe(
            data => {
                callback(null, data);
            },
            error => {
                callback(error, null);
            }
        );
    }
}
