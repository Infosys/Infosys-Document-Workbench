/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AttachmentData } from '../data/attachment-data';
import { CONSTANTS } from '../common/constants';
import { ConfigDataHelper } from '../utils/config-data-helper';

@Injectable()
export class AttachmentService {

  constructor(private httpClient: HttpClient, private configDataHelper: ConfigDataHelper) { }

  getAttachmentList(docId: number) {
    const parent = this;
    let url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) +
      CONSTANTS.APIS.DOCWBSERVICE.GET_ATTACHMENT_LIST;
    let queryParams = '';

    queryParams += 'docId=' + docId;

    url += '?' + queryParams;
    return new Promise(function (fulfilled, rejected) {
      parent.httpClient.get(url).subscribe(
        (data: { [x: string]: AttachmentData[]; }) => { fulfilled(data['response'] as AttachmentData[]); },
        (error: any) => {
          rejected(error);
        });
    });
  }

  viewAttachment(docId: number, attachmentId: number, fileName: string, callback) {
    const parent = this;
    parent.getFilePath(docId, attachmentId).then(function (filepath) {

      let url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBFILESERVER_BASE_URL) + CONSTANTS.APIS.DOCWBSERVICE.GET_ATTACHMENT_FILE;
      url += "/" + filepath
      parent.httpClient.get(url, {
        responseType: 'blob'
      })
        .subscribe(
          response => {

            const blob = new Blob([response], { type: '' });
            // application/octet-stream
            // const url= window.URL.createObjectURL(blob);
            if (window.navigator && window.navigator.msSaveBlob) {
              // IE browser download
              window.navigator.msSaveBlob(blob, fileName);
            } else {
              const link = document.createElement('a');
              link.href = window.URL.createObjectURL(blob);
              link.download = fileName;
              link.click();
            }
            // window.open(url);
            callback();
          }
          // data => {
          //   let attachmentData: AttachmentData = data['response'];
          //   callback(null, attachmentData);
          // },
          // error => {
          //   callback(error, null);
          // }
        );
    });
    return null;
  }

  openAttachment(docId: number, attachmentId: number, fileName: string, isPopup:boolean) {
    const parent = this;
    return new Promise(function (fulfilled, rejected) {
      parent.getFilePath(docId, attachmentId).then(function (filepath) {
        let url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBFILESERVER_BASE_URL) + CONSTANTS.APIS.DOCWBSERVICE.GET_ATTACHMENT_FILE;
        url += "/" + filepath;
        // CAN'T ADD AUTH HEADER TO WINDOW OPEN, HENCE IT CAN'T OPEN DIRECT URL.
        parent.httpClient.get(url, { responseType: 'blob' }).subscribe(
          response => {
            const fileURL = URL.createObjectURL(response);
            if (isPopup) {
              window.open(fileURL, '_blank', 'popup');
            } else {
              window.open(fileURL, '_blank');
            }
            fulfilled(true);
          }
        );
      });
    });
  }

  getFilePath(docId: number, attachmentId: number) {
    const parent = this;
    let url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) +
      CONSTANTS.APIS.DOCWBSERVICE.GET_ATTACHMENT_FILE_PATH;
    let queryParams = '';
    queryParams += 'docId=' + docId + '&attachmentId=' + attachmentId;
    url += '?' + queryParams;
    return new Promise(function (fulfilled, rejected) {
      parent.httpClient.get(url).subscribe(
        (data: any) => {
          fulfilled(data['response']);
        },
        (error: any) => {
          rejected(error);
        });
    });
  }

  getFileContent(docId: number, attachmentId: number) {
    const parent = this;
    return new Promise(function (fulfilled, rejected) {
      parent.getFilePath(docId, attachmentId).then(function (filepath) {
        let url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBFILESERVER_BASE_URL) + CONSTANTS.APIS.DOCWBSERVICE.GET_ATTACHMENT_FILE;
        url += '/' + filepath;
        parent.httpClient.get(url, {
          responseType: 'blob'
        }).subscribe(data => {
          const resData = { 'data': data, 'fileUrl': url };
          fulfilled(resData);
        },
          error => {
            rejected(null);
          }
        );
      }).catch(function () { rejected(null); })
    });

  }

  getAttachmentListEmail(emailOutboundId: number, callback) {
    const parent = this;
    let url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) +
      CONSTANTS.APIS.DOCWBSERVICE.GET_ATTACHMENT_LIST_EMAIL;
    let queryParams = '';

    queryParams += 'emailOutboundId=' + emailOutboundId;

    url += '?' + queryParams;

    this.httpClient.get(url)
      .subscribe(
        data => {
          const attachmentData: AttachmentData = data['response'];
          callback(null, attachmentData);
        },
        error => {
          callback(error, null);
        }
      );
    return null;
  }

  viewAttachmentEmail(emailOutboundId: number, attachmentId: number, fileName: string, callback) {
    const parent = this;
    let url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) +
      CONSTANTS.APIS.DOCWBSERVICE.GET_ATTACHMENT_FILE_EMAIL;
    let queryParams = '';

    queryParams += 'emailOutboundId=' + emailOutboundId + '&attachmentId=' + attachmentId;

    url += '?' + queryParams;

    this.httpClient.get(url, {
      responseType: 'blob'
    })
      .subscribe(
        response => {

          const blob = new Blob([response], { type: 'application/octet-stream' });
          // application/octet-stream
          // const url= window.URL.createObjectURL(blob);
          const link = document.createElement('a'); // Create a <a> element
          link.href = window.URL.createObjectURL(blob);
          link.download = fileName;
          link.click();
          // window.open(url);
          callback();
        }

      );
    return null;
  }


}
