/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Component, OnInit, EventEmitter, OnChanges, SimpleChange, Input, Output, ElementRef, HostListener } from '@angular/core';
import { TreeItem } from './tree-item';
import { Router } from '@angular/router';


@Component({
  selector: 'app-nia-tree-view',
  templateUrl: './nia-tree-view.component.html',
  styleUrls: ['./nia-tree-view.component.scss']
})
export class NiaTreeViewComponent implements OnInit, OnChanges {

  /** CLASS VARIABLES */
  private _selectedItemId = '';
  private _selectedUrl = '';

  model: any = {
    treeItems: [] as TreeItem[],
    componentWidthInPx: 0
  }; // For binding to view

  /** I/O VARIABLES */
  @Input()
  set items(items: TreeItem[]) {
    this.model.treeItems = items;
  }

  @Input()
  set selectedId(selectedId: string) {
    console.log('selectedId', selectedId);
    this._selectedItemId = selectedId;
  }

  @Output() itemClicked = new EventEmitter<string>();

  constructor(public router: Router, private elementRef: ElementRef) {
  }

  ngOnInit() {
    this.resizeElements()
  }

  @HostListener('window:resize', ['$event'])
  onResize(event) {
    // This method will be called only on page resize and NOT on component load.
    this.resizeElements()
  }

  onTreeItemClick(itemId: string) {
    // console.log("onTreeItemClick", itemId);
    this._selectedItemId = itemId;
    this.updateTreeview(this._selectedItemId);
    this.itemClicked.emit(itemId);
    if (this._selectedUrl) {
      this.router.navigateByUrl(this._selectedUrl);
    }

  }

  ngOnChanges(changes: { [propKey: string]: SimpleChange }) {
    // console.log("ngOnChanges");
    if (changes['items']) {
      // console.log("ngOnChanges-items");
      const to = changes['items'].currentValue;
      this.model.treeItems = to;
      this.updateTreeview(this._selectedItemId);
    } else if (changes['selectedId']) {
      // console.log("ngOnChanges-selectedId");
      const to = changes['selectedId'].currentValue;
      this._selectedItemId = to;
      if (this._selectedItemId) {
        this.updateTreeview(this._selectedItemId);
      }
    } else {
      // console.log("ngOnChanges-UNKNOWN");
    }
  }

  /** PRIVATE METHODS BELOW  **/

  private resizeElements() {
    this.model.componentWidthInPx = this.elementRef.nativeElement.offsetWidth;
  }

  /**
   * Collapses a given tree item node hiding its children.
   * @param treeItem
   * @param isVisible
   * @param isSelected
   */
  private collapseNode(treeItem: TreeItem, isVisible: boolean, isSelected: boolean) {
    const parent = this;
    treeItem.isVisible = isVisible;
    treeItem.isSelected = isSelected;
    if (treeItem.items != null) {
      for (const itemX of treeItem.items) { // Level X
        parent.collapseNode(itemX, false, false)
      }
    }
  }

  /**
   * Expands a given tree item node showing its children.
   * @param treeItem
   * @param requestedItemId
   */
  private expandNode(treeItem: TreeItem, requestedItemId:string) {
    const parent = this;
    let showChildrenX: boolean;
    if (treeItem.items != null) {
      for (const itemX of treeItem.items) { // Level X
        itemX.isVisible = true;
        showChildrenX = false; // Reset for next iteration
        if (itemX.id === requestedItemId.substring(0, itemX.id.length)) { // correct branch at level X
          itemX.isSelected = true;
          this._selectedUrl = itemX.url;
          if (itemX.items != null && itemX.items.length > 0) {
            showChildrenX = true;
          } else {
            continue; // Continue with next iteration if no children present
          }
        }
        if (showChildrenX) { //show children of level X which is at level X+1
          // Recursive call
          parent.expandNode(itemX, requestedItemId)
        } else {
          parent.collapseNode(itemX, true, false)
        }
      }
    }
  }

  /**
   * Updates all nodes from root to leaf to be visible and selected based
   * on selectedItemId value. All other nodes are deselected and hidden.
   * @param selectedItemId
   * @returns
   */
  private updateTreeview(selectedItemId: string) {
    const parent = this;
    // console.log("updateTreeview->",  "selectedItemId=" + selectedItemId)
    let showChildrenJ: boolean;
    // console.log('this.model.treeItems->',JSON.stringify(this.model.treeItems))
    if (this.model.treeItems == null) {
      return;
    }
    for (const itemI of this.model.treeItems) { // Level I
      if (selectedItemId && itemI.id === selectedItemId.substring(0, itemI.id.length)) { // correct branch at level I
        showChildrenJ = false;
        itemI.isSelected = true;
        parent._selectedUrl = itemI.url;
        if (itemI.items != null && itemI.items.length > 0) {
          showChildrenJ = true;
        }

        if (showChildrenJ) {
          parent.expandNode(itemI, selectedItemId)
        }
      } else { // Incorrect branch at level I so collapse all menus after I
        parent.collapseNode(itemI, true, false)
      }
    }
    // console.log("updateTreeview->", this.treeItems);
  }

}
