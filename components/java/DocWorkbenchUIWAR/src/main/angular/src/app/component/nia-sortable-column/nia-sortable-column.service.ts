/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Injectable } from '@angular/core';
import { NiaSortableColumnData } from './nia-sortable-column-data';
import { BehaviorSubject } from 'rxjs';
import { NIASCCONST } from './nia-sortable-column-constants';

@Injectable()
export class NiaSortableColumnService {
    constructor() { }
    private _customColSortKey = undefined;
    private _nonConfigCustomSortKey = new Array();
    private _isSpecialSerialPreprocessed = false;

    // BehaviorSubject -- Start
    private bsColumnSortedSource: BehaviorSubject<NiaSortableColumnData> = new BehaviorSubject<NiaSortableColumnData>(null);
    public columnSorted = this.bsColumnSortedSource.asObservable();
    public publishColumnSortedEvent(event: NiaSortableColumnData) {
        this.bsColumnSortedSource.next(event);
    }
    // BehaviorSubject -- End

    // Used to sort table without clicking header. For example, Sort table immediate to page load.
    applySort(niaSortableColumnData: NiaSortableColumnData) {
        this.sortTable(niaSortableColumnData);
    }

    // Sort table using bubble sort.
    private sortTable(niaSortableColumnData: NiaSortableColumnData) {
        if (niaSortableColumnData.sortLevel2HeaderClass) {
            this.sortTableBySections(niaSortableColumnData);
        } else {
            const table = <HTMLTableElement>document.getElementById(niaSortableColumnData.sortTableId);
            if (!table) {
                return;
            }
            this.validateAndSortTable(table, niaSortableColumnData, [], 0);
        }
        this.postProcessToRelativeRows(niaSortableColumnData);
        this.publishColumnSortedEvent(niaSortableColumnData);
    }

    // Sort table based on bubble sort.
    private sortTableBySections(niaSortableColumnData: NiaSortableColumnData) {
        const table = <HTMLTableElement>document.getElementById(niaSortableColumnData.sortTableId);
        if (table) {

            const rows = table.rows;
            const headers: any = table.querySelectorAll('tr.' + niaSortableColumnData.sortLevel2HeaderClass);
            // As annotation sort required empty row at last, preprocess to move all empty row to last before doing number sort.
            this.preProcessWhenAnnotationASCSort(niaSortableColumnData, headers, rows);
            // Sort section by section, for example email, document1,2...etc
            let counter = 0;
            for (let h = 0; h < headers.length; h++) {
                this._nonConfigCustomSortKey = new Array();
                // If no custom sort key configured the skip.
                if (this.isCustomSort(niaSortableColumnData) && !this.fetchAndSetCustomKey(rows, headers[h], niaSortableColumnData)) {
                    counter++;
                    continue;
                }
                this.validateAndSortTable(table, niaSortableColumnData, headers, h);
            }

            if (niaSortableColumnData.sortCustomCol && counter === headers.length) {
                this.applyASCTableSort(niaSortableColumnData);
            }
        }
    }

    private applyASCTableSort(niaSortableColumnData: NiaSortableColumnData) {
        niaSortableColumnData.sortDirection = NIASCCONST.SORT.ORDER.ASC;
        niaSortableColumnData.sortCustomCol = undefined;
        niaSortableColumnData.sortForceApply = true;
        this.applySort(niaSortableColumnData);
    }

    private validateAndSortTable(table, event: NiaSortableColumnData, headers, headerIndex) {
        // Until asc /desc loop.
        let switching = true;
        let element1, element2;
        const rows = table.rows;
        const h = headerIndex;
        while (switching) {
            switching = false;
            let i = headers.length > 0 ? headers[h].rowIndex + (+event.sortStartRow) : 1;
            // loop until header section end, for example email, document1,2...etc
            const subRows = (headers.length <= (h + 1)) ? table.rows.length - 1 : headers[h + 1].rowIndex - 1;
            for (i; i < subRows; i++) {
                const currentRow = rows[i];
                const nextRow = rows[i + 1];
                // Get currect row cell value and next row cell value.
                element1 = this.getDOMText(currentRow.getElementsByTagName('td')[+event.sortColumn]);
                element2 = this.getDOMText(nextRow.getElementsByTagName('td')[+event.sortColumn]);
                // Swap current and next row if condition true.
                if (this.validateToSort(event, element1, element2)) {
                    rows[i].parentNode.insertBefore(nextRow, currentRow);
                    // set it true to repeat bubble sort until all row sorted.
                    switching = true;
                    break;
                }
            }
        }
    }

    private isCustomSort(event): boolean {
        return (event.sortDirection === NIASCCONST.SORT.ORDER.CUSTOM && event.sortCustomCol);
    }

    private fetchAndSetCustomKey(rows, headerRow, event) {
        const keyAttributeCde = +rows[headerRow.rowIndex + 1]
            .getElementsByTagName('td')[+event.sortColumn].getAttribute('data-attrNameCde');
        const keyAttributeValue = this.getDOMText(rows[headerRow.rowIndex + 1]
            .getElementsByTagName('td')[+event.sortColumn + 1]).split(':')[1];
        this._customColSortKey = (event.sortableColCustomKey && keyAttributeValue) ?
            event.sortableColCustomKey[keyAttributeCde + '_' + keyAttributeValue.trim()] : undefined;
        return this._customColSortKey;
    }

    private preProcessWhenAnnotationASCSort(event: NiaSortableColumnData, headers, rows) {
        if (event.sortDirection === NIASCCONST.SORT.ORDER.ASC && event.sortIdCol
            && headers.length === 1 && !this._isSpecialSerialPreprocessed) {
            this._isSpecialSerialPreprocessed = true;
            const emptyRows = [];
            for (let r = 3; r < rows.length; r++) {
                if (!this.getDOMText(rows[r].getElementsByTagName('td')[+event.sortColumn])) {
                    emptyRows.push(rows[r]);
                }
            }
            for (let er = 0; er < emptyRows.length; er++) {
                rows[1].parentNode.insertBefore(emptyRows[er], rows[rows.length]);
            }
        }
        if (!event.sortIdCol) {
            this._isSpecialSerialPreprocessed = false;
        }
    }

    private postProcessToRelativeRows(event: NiaSortableColumnData) {
        const placeholderID = NIASCCONST.REPLACE_PLACEHOLDER.ID;
        const relativeToParentRows = document.querySelectorAll(
            NIASCCONST.SELECTOR.RELATIVE_TO_PARENT.replace(placeholderID, event.sortTableId));
        if (relativeToParentRows && relativeToParentRows.length > 0) {
            const relativeParentRows = document.querySelectorAll(
                NIASCCONST.SELECTOR.RELATIVE_PARENT.replace(placeholderID, event.sortTableId));
            relativeToParentRows.forEach((relativeToParentRow: Element) => {
                relativeParentRows.forEach((relativeParentRow: Element) => {
                    if (relativeParentRow.getAttribute(NIASCCONST.ROW_VARIATION.RELATIVE_PARENT) ===
                        relativeToParentRow.getAttribute(NIASCCONST.ROW_VARIATION.RELATIVE_TO_PARENT)) {
                        relativeParentRow.parentNode.insertBefore(relativeToParentRow, relativeParentRow.nextSibling);
                    }
                });
            });
        }
    }

    private validateToSort(event, element1, element2) {
        let shouldSwap = false;
        if (event.sortDirection === NIASCCONST.SORT.ORDER.ASC) {
            shouldSwap = this.validateASCSwap(event, element1, element2);
        }
        if (event.sortDirection === NIASCCONST.SORT.ORDER.DESC) {
            shouldSwap = this.validateDESCSwap(event, element1, element2);
        }
        if (event.sortDirection === NIASCCONST.SORT.ORDER.CUSTOM) {
            shouldSwap = this.validateCustomSwap(element1, element2);
        }
        return shouldSwap;
    }

    private validateASCSwap(event, element1, element2) {
        let isSwap = element1 > element2;
        if (event.sortIdCol) {
            isSwap = false;
            if (element1 && element2) {
                isSwap = +element1.replace(NIASCCONST.SEQ_DECORATOR.DOT, '') >
                    +element2.replace(NIASCCONST.SEQ_DECORATOR.DOT, '');
            }
        }
        return isSwap;
    }

    private validateDESCSwap(event, element1, element2) {
        let isSwap = element1 < element2;
        if (event.sortIdCol) {
            isSwap = +element1.replace(NIASCCONST.SEQ_DECORATOR.DOT, '') < +element2.replace(NIASCCONST.SEQ_DECORATOR.DOT, '');
        }
        return isSwap;
    }

    private validateCustomSwap(element1, element2) {
        let ele1Index, ele2Index = -1;
        if (this._customColSortKey) {
            // Sort based on configured key order.
            const customOrdersReceived = this._customColSortKey.concat(this._nonConfigCustomSortKey);
            ele1Index = customOrdersReceived.indexOf(element1);
            ele2Index = customOrdersReceived.indexOf(element2);
            if (ele1Index === -1) {
                // if non configured value move it to last as asc order
                this._nonConfigCustomSortKey.push(element1);
                this._nonConfigCustomSortKey = this._nonConfigCustomSortKey.sort();
                ele1Index = this._customColSortKey.length + this._nonConfigCustomSortKey.indexOf(element1) + 1;
            }
        }
        return (ele1Index > ele2Index);
    }

    private getDOMText(element: any) {
        let text = '';
        if (element) {
            const children = element.children;
            if (this.isListHasValue(children) && element.tagName !== 'SELECT') {
                for (let i = 0; i < children.length; i++) {
                    if (children[i].className !== 'nia-person person-icon') {
                        text = this.getDOMText(children[i]);
                    }
                }
            } else {
                text = element.value ? element.value : element.outerText;
            }
        }
        return (text ? text.trim().toLowerCase() : text);
    }

    private isListHasValue(elements) {
        return (elements && elements.length > 0);
    }

}
