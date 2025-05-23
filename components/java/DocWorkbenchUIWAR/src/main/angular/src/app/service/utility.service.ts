/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Injectable } from '@angular/core';
import { SessionService } from './session.service';
import { UserData } from '../data/user-data';
import { DocumentData } from '../data/document-data';
import { EnumTaskStatus } from '../common/task-status.enum';
import { ActionData } from '../data/action-data';
import { CONSTANTS } from '../common/constants';
import { ModalDismissReasons } from '@ng-bootstrap/ng-bootstrap';

@Injectable()
export class UtilityService {

  constructor(private sessionService: SessionService) { }
  userData: UserData = null;

  private isCaseOwner(userId: number) {
    return this.userData != null && this.userData.userId === userId;
  }

  isCaseClosed(taskStatusCde: number) {
    return +taskStatusCde === EnumTaskStatus.COMPLETE;
  }

  isCaseEditable(document: DocumentData) {
    const parent = this;
    let isCaseEditable = false;
    return parent.sessionService.getLoggedInUserDetailsPromise().then(function (value) {
      parent.userData = value as UserData;

      const isCaseOwner: boolean = parent.isCaseOwner(document.appUserId);
      const isCaseClosed: boolean = parent.isCaseClosed(document.taskStatusCde);

      if (isCaseClosed) {
        isCaseEditable = false;
      } else if (isCaseOwner) {
        isCaseEditable = true;
      } else {
        isCaseEditable = false;
      }
      return isCaseEditable;
    }).catch(function () {
      return isCaseEditable;
    });
  }



  getTruncatedFileName(fileName: string, limit: number) {
    let computedName = '';
    let fileNameNoExt = '';
    if (fileName.lastIndexOf('.') >= 0) {
      fileNameNoExt = fileName.substring(0, fileName.lastIndexOf('.'));
    }
    if (fileNameNoExt.length > limit) {
      computedName = fileNameNoExt.substring(0, limit) + '...' + fileName.substring(fileName.lastIndexOf('.'), fileName.length);
    } else {
      computedName = fileName;
    }
    return computedName;
  }

  getDocType(queueNameCde: number): any {
    const parent = this;
    let isDocType = -1;
    return parent.sessionService.getLoggedInUserDetailsPromise().then(function (value) {
      for (const queueData of (value as UserData).queueDataList) {
        if (queueData.queueNameCde === queueNameCde) {
          isDocType = queueData.docTypeCde;
          break;
        }
      }
      return isDocType;
    }).catch(function () {
      return isDocType;
    });
  }

  isListHasValue(objectList: any): boolean {
    return objectList !== undefined && objectList !== null && objectList.length > 0;
  }

  createDuplicateList(objectList: any) {
    const tempObjectList = [];
    objectList.forEach(function (tempAttrData) {
      const attrData = Object.assign({}, tempAttrData);
      tempObjectList.push(attrData);
    });
    return tempObjectList;
  }

  isStringHasValue(str: string): boolean {
    return str !== undefined && str !== null && str.trim().length > 0;
  }

  isReExtractPending(actionDataList: ActionData[]) {
    return this.isListHasValue(actionDataList) && actionDataList.filter(actionData =>
      (actionData.actionNameCde === CONSTANTS.ACTION_NAME_CDE.RE_EXTRACT_DATA &&
        actionData.taskStatusCde !== CONSTANTS.ACTION_TASK_STATUS_CDE.COMPLETED &&
        actionData.taskStatusCde !== CONSTANTS.ACTION_TASK_STATUS_CDE.FAILED)).length > 0;
  }

  isAValidValue(obj) {
    return obj !== null && obj !== undefined;
  }

  getEscapedString(value: string) {
    const regex = new RegExp('[^A-Za-z0-9\\s]', 'g');
    let matches: any = value.match(regex);
    if (matches !== undefined && matches !== null && matches.length > 0) {
      matches = matches.filter((x, i, a) => a.indexOf(x) === i);
      matches.forEach(val => {
        value = value.split(val).join('\\' + val);
      });
    } else {
      value = '\\b' + value + '\\b';
    }
    return value;
  }

  getIfStringsMatch(valueToSearch: string, valueToBeSearched) {
    valueToSearch = this.getEscapedString(valueToSearch).replace(/\s+/g, '\\s+');
    const regex = RegExp(valueToSearch, 'gi');
    let matchAll: any;
    matchAll = Array.from(valueToBeSearched.matchAll(regex));
    return this.isListHasValue(matchAll) && matchAll.filter(match => match[0] === valueToBeSearched).length > 0;
  }

  getIfDuplicatesExist(values) {
    return this.isListHasValue(values) && this.isListHasValue(values.filter((val, i) => values.indexOf(val) !== i));
  }

  testJSON(item: any): boolean {
    let isJson = false;
    try {
      if (item && typeof item === 'string' && typeof JSON.parse(item) === 'object') {
        isJson = true;
      }
    } catch (error) { }
    return isJson;
  }

  getDismissReason(reason: any): string {
    let dismissReason: string;
    if (reason === ModalDismissReasons.ESC) {
      dismissReason = 'by pressing ESC';
    }
    if (reason === ModalDismissReasons.BACKDROP_CLICK) {
      dismissReason = 'by clicking on a backdrop';
    } else {
      dismissReason = `with: ${reason}`;
    }
    return dismissReason;
  }

  getBrowserType(): string {
    // - IE
    // ```
    // mozilla/5.0 (windows nt 10.0; wow64; trident/7.0; .net4.0c; .net4.0e; .net clr 2.0.50727; .net clr 3.0.30729;
    // .net clr 3.5.30729; tablet pc 2.0; wbxapp 1.0.0; rv:11.0) like gecko
    // ```
    // - Chrome
    // ```
    // "mozilla/5.0 (windows nt 10.0; win64; x64) applewebkit/537.36 (khtml, like gecko) chrome/85.0.4183.102 safari/537.36"
    // ```
    // - Firefox
    // ```
    // mozilla/5.0 (windows nt 10.0; win64; x64; rv:80.0) gecko/20100101 firefox/80.0
    // ```

    if ((/trident/.test(navigator.userAgent.toLowerCase()))) {
      return CONSTANTS.BROWSER_TYPE.INTERNET_EXPLORER;
    } else if ((/chrome/.test(navigator.userAgent.toLowerCase()))) {
      return CONSTANTS.BROWSER_TYPE.CHROME;
    } else if ((/firefox/.test(navigator.userAgent.toLowerCase()))) {
      return CONSTANTS.BROWSER_TYPE.FIREFOX;
    } else {
      return CONSTANTS.BROWSER_TYPE.UNKNOWN;
    }
  }

  // Condition to hide/show embed pdf view on IE when opening any popup. Because on IE embed tag used to overlap on popups.
  toggleWhenIEPdfEmbedLayer(skipToggle?: boolean): void {
    if (!skipToggle) {
      const embedPdfDiv = <HTMLFormElement>document.getElementById('embedPdfIEDiv');
      if (embedPdfDiv) {
        if (embedPdfDiv.style.display === 'none') {
          embedPdfDiv.style.display = 'block';
        } else {
          embedPdfDiv.style.display = 'none';
        }
      }
    }
  }

  getTitle(attributeData: any) {
    const createdBy = (attributeData.createByUserTypeCde > 0) ? (attributeData.createByUserTypeCde === 1) ?
      'Added by ' + attributeData.createByUserFullName + ' (' + attributeData.createByUserLoginId + ').'
      : 'Added by Service.' : '';
    const lastModBy = (attributeData.lastModByUserLoginId) ? (attributeData.lastModByUserTypeCde === 1) ?
      'Updated by ' + attributeData.lastModByUserFullName + ' (' + attributeData.lastModByUserLoginId + '). \n'
      : 'Updated by Service.\n' : '';
    return attributeData.attrNameTxt + '.\n' + lastModBy + createdBy;
  }
}
