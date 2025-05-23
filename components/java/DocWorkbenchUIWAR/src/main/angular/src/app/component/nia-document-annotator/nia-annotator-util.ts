/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import * as annotator from 'annotator';
import { Injectable } from '@angular/core';

export class RangeData {

    constructor(public start: string,
        public startOffset: number,
        public end: string,
        public endOffset: number
    ) { }
}
@Injectable()
export class NiaAnnotatorUtil {
    constructor() { }
    FILE_VIEWER_TXT = 'text';
    FILE_VIEWER_HTML = 'html';
    FILE_VIEWER_PDF = 'pdf';
    FILE_VIEWER_EMBED = 'embed';
    SELECTOR = '#textContent';
    HTML_SELECTOR = '#htmlContent';
    PDF_SELECTOR = 'div#viewer.pdfViewer.textlayer-on';
    FILE_TYPE = {
        TXT: 'text/plain',
        PDF: 'application/pdf',
        JPG: 'image/jpg',
        JPEG: 'image/jpeg',
        PNG: 'image/png',
        HTML: 'text/html'
    };
    DUPLICATE_ANN_ID_PREFIX = 'dup-';

    findTextRangeFromTextlayer(text: string, selector: string, occurrenceNum?: number): RangeData[] {
        const ranges = [];
        const $ = annotator.util.$;
        const node = $(selector)[0];
        if (node !== undefined) {
            text = this.getEscapedString(text).replace(/\s+/g, '\\s+');
            const regex = RegExp(text, 'gi');
            let matchAll: any;
            matchAll = Array.from(this.replaceNbsps(node.textContent).matchAll(regex));
            if (matchAll !== null && matchAll !== undefined && matchAll.length > 0) {
                const path = this.simpleXPathJQuery(selector, node);
                this.getRanges(matchAll, path, ranges, occurrenceNum);
            }
        }
        return ranges;
    }

    getDuplicateList(dataList) {
        const tempObjectList = [];
        if (dataList !== undefined && dataList !== null) {
            dataList.forEach(function (tempData) {
                const data = Object.assign({}, tempData);
                tempObjectList.push(data);
            });
        }
        return tempObjectList;
    }

    cleanHtml(fileContent) {
        const parent = this;
        const regex = new RegExp(/([^\s])(<[a-zA-Z]+(>|.*?[^?]>))/, 'gi');
        fileContent = fileContent.replace(regex, '$1\n$2');
        return fileContent;
    }

    getMappedAnnotations(valueList, valueToMap) {
        const valueMap = new Map<string, any[]>();
        for (let i = 0; i < valueList.length; i++) {
            if (valueMap.has(valueList[i][valueToMap].toLowerCase())) {
                const values = valueMap.get(valueList[i][valueToMap].toLowerCase());
                values.push(valueList[i]);
                valueMap.set(valueList[i][valueToMap].toLowerCase(), values);
            } else {
                valueMap.set(valueList[i][valueToMap].toLowerCase(), [valueList[i]]);
            }
        }
        return valueMap;
    }

    getSelectorBasedOnViewer(viewer) {
        let selector = this.SELECTOR;
        if (viewer === this.FILE_VIEWER_PDF) {
            selector = this.PDF_SELECTOR;
        } else if (viewer === this.FILE_VIEWER_HTML) {
            selector = this.HTML_SELECTOR;
        }
        return selector;
    }

    getIfListPopulated(list) {
        return list !== undefined && list !== null && list.length > 0;
    }

    getIfStringsMatch(valueToSearch: string, valueToBeSearched) {
        valueToSearch = this.getEscapedString(valueToSearch).replace(/\s+/g, '\\s+');
        const regex = RegExp(valueToSearch, 'gi');
        let matchAll: any;
        matchAll = Array.from(valueToBeSearched.matchAll(regex));
        return this.getIfListPopulated(matchAll) && matchAll.filter(match => match[0] === valueToBeSearched).length > 0;
    }

    private getEscapedString(value: string) {
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

    private simpleXPathJQuery(root: string, node) {
        const $ = annotator.util.$;
        let elem, idx, path, tagName;
        path = '';
        elem = node;
        const relativeRoot: string = $(root)[0];
        while (elem !== null && elem !== relativeRoot) {
            try {
                if ((elem !== null ? elem.nodeType : void 0) === 1 &&
                    (elem.classList !== undefined && !elem.classList.contains('annotator-hl'))) {
                    tagName = elem.tagName.replace(':', '\\:');
                    idx = $(elem.parentNode).children(tagName).index(elem) + 1;
                    idx = '[' + idx + ']';
                    path = '/' + elem.tagName.toLowerCase() + idx + path;
                }
                elem = elem.parentNode;
            } catch (exception) {
                console.log(elem, exception);
                elem = null;
            }
        }
        return path;
    }

    private replaceNbsps(str) {
        const re = new RegExp(String.fromCharCode(160), 'g');
        return str.replace(re, ' ');
    }

    private getRanges(matchAll: any[], path: string, ranges: any[], occurrenceNum?: number) {
        if (this.getIfListPopulated(matchAll)) {
            if (occurrenceNum > 0 && occurrenceNum <= matchAll.length) {
                const matchListBasedOnOccNum = matchAll[occurrenceNum - 1];
                this.buildRangeArray(path, matchListBasedOnOccNum, ranges);
            } else {
                matchAll.forEach(match => {
                    this.buildRangeArray(path, match, ranges);
                });
            }
        }
    }

    private buildRangeArray(path: string, match: any, ranges: any[]) {
        const xPath = path;
        const startIndex = match.index;
        const endIndex = match.index + match[0].length;
        const rangeData = new RangeData(xPath, startIndex, xPath, endIndex);
        rangeData['quote'] = match[0];
        ranges.push(rangeData);
    }

}
