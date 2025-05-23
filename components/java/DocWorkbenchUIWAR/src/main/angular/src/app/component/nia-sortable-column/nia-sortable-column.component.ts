/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Component, OnInit, Input, EventEmitter, OnDestroy, HostListener, SimpleChange, OnChanges, Output } from '@angular/core';
import { Subscription } from 'rxjs';
import { NiaSortableColumnService } from './nia-sortable-column.service';
import { NiaSortableColumnData } from './nia-sortable-column-data';
import { NIASCCONST } from './nia-sortable-column-constants';


@Component({
  // tslint:disable-next-line: component-selector
  selector: '[sortable-column]',
  templateUrl: './nia-sortable-column.component.html'
})
export class NiaSortableColumnComponent implements OnInit, OnDestroy {

  constructor(private niaSortTableService: NiaSortableColumnService) { }

  // Mandatory attribute to sort table.
  // tslint:disable-next-line: no-input-rename
  @Input('sortable-column')
  columnName: string;

  // Mandatory attribute to differentiate if multi table sorted.
  // tslint:disable-next-line: no-input-rename
  @Input('sort-table-name')
  sortTableName: string;

  // Mandatory if sort-direction set default to custom, else optional.
  // tslint:disable-next-line: no-input-rename
  @Input('sort-custom-col')
  sortCustomCol: string;

  // Mandatory if sort-custom-col set default to custom, else optional.
  // tslint:disable-next-line: no-input-rename
  @Input('sortable-col-custom-key')
  sortableColCustomKey: {};

  // Mandatory if numberic col needs sorting. For example serial number column.
  // tslint:disable-next-line: no-input-rename
  @Input('sort-id-col')
  sortIdCol: string;

  // While initial loading to set anyone column in defaul direction. For Example asc/ desc/ custom.
  sortDirection: string;
  @Input('sort-direction')
  set sortDirection1(value: string) {
    this.sortDirection = value;
    this.model.sortMsg = this.getSortedMsg(value);
  }

  // Set if any second level header.
  // tslint:disable-next-line: no-input-rename
  @Input('sort-level-2-header-class')
  sortLevel2HeaderClass: string;

  // Set if any row top row needs freeze.
  // tslint:disable-next-line: no-input-rename
  @Input('sort-start-row')
  sortStartRow: string;

  sortTableDisabled: boolean;
  @Input('sort-table-disabled')
  set sortTableDisabled1(value: string) {
    this.sortTableDisabled = (value == "true") ? true : false
  }

  @Output() columnClicked = new EventEmitter<{}>();

  // Used inside class
  private _columnSortedSubscription: Subscription;

  // Used in HTML
  model: any = {
    sortMsg: NIASCCONST.SORT.MESSAGE.DEFAULT
  };

  // Called while clicking - 'sortable-column'
  @HostListener('click', ['$event'])
  sort(event: any) {

    let el = event.target;
    while ((el = el.parentElement) && el.nodeName.toUpperCase() !== 'TABLE') { }
    if (this.sortCustomCol) {
      this.sortDirection = this.sortDirection === NIASCCONST.SORT.ORDER.ASC ?
        NIASCCONST.SORT.ORDER.DESC : this.sortDirection === NIASCCONST.SORT.ORDER.DESC ?
          NIASCCONST.SORT.ORDER.CUSTOM : NIASCCONST.SORT.ORDER.ASC;
    } else {
      this.sortDirection = this.sortDirection === NIASCCONST.SORT.ORDER.ASC ?
        NIASCCONST.SORT.ORDER.DESC : NIASCCONST.SORT.ORDER.ASC;
    }

    this.model.sortMsg = this.getSortedMsg(this.sortDirection);
    const niaSortableColumnData = new NiaSortableColumnData(this.columnName, this.sortDirection, el.id, this.sortCustomCol,
      this.sortableColCustomKey, this.sortIdCol, this.sortTableName, this.model.sortMsg, this.sortLevel2HeaderClass,
      this.sortStartRow, false);
    if (!this.sortTableDisabled) {
      this.niaSortTableService.applySort(niaSortableColumnData);
    }
    this.columnClicked.emit(niaSortableColumnData);
  }

  ngOnInit() {
    // subscribe to sort changes so we can react when other columns are sorted
    this._columnSortedSubscription = this.niaSortTableService.columnSorted.subscribe(event => {
      // reset this column's sort direction to hide previous sort highlight
      if (event && this.sortTableName === event.sortTableName) {
        if (this.columnName !== event.sortColumn) {
          this.sortDirection = '';
          this.model.sortMsg = NIASCCONST.SORT.MESSAGE.DEFAULT;
          this.niaSortTableService.publishColumnSortedEvent(undefined);
        }
        if (this.columnName === event.sortColumn && event.sortForceApply) {
          this.sortDirection = event.sortDirection;
          this.model.sortMsg = this.getSortedMsg(event.sortDirection);
          this.niaSortTableService.publishColumnSortedEvent(undefined);
        }
      }
    });
  }

  ngOnDestroy() {
    this._columnSortedSubscription.unsubscribe();
  }

  private titleCase(str) {
    return str.toLowerCase().split(' ').map(function (word) {
      return (word.charAt(0).toUpperCase() + word.slice(1));
    }).join(' ');
  }

  private getSortedMsg(direction) {
    return this.titleCase(direction) + ' Sorted';
  }
}
