/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Component, OnInit, Output, EventEmitter, Input } from '@angular/core';

@Component({
  selector: 'app-json-tabular-data',
  templateUrl: './json-tabular-data.component.html',
  styleUrls: ['./json-tabular-data.component.scss']
})
export class JsonTabularDataComponent implements OnInit {
  constructor() { }
  model: any = {
    closeButton: Boolean,
    currentJsonValue: String,
    previousJsonValue: String,
    minheight: Number,
    isTableView: Boolean
  };

  @Output() close = new EventEmitter<string>();
  @Input()
  set currentJsonValue(currentJsonValue: string) {
    try {
      this.model.currentJsonValue = JSON.parse(currentJsonValue);
    } catch (e) {
      this.model.currentJsonValue = JSON.parse(JSON.stringify(currentJsonValue));
    }
  }
  @Input()
  set previousJsonValue(previousJsonValue: string) {
    try {
      this.model.previousJsonValue = JSON.parse(previousJsonValue);
    } catch (e) {
      this.model.previousJsonValue = JSON.parse(JSON.stringify(previousJsonValue));
    }
  }
  @Input()
  set minheight(minheight: number) {
    this.model.minheight = minheight;
  }
  @Input()
  set isTableView(isTableView: boolean) {
    this.model.isTableView = isTableView;
  }

  @Input()
  set closeButton(closeButton: number) {
    this.model.closeButton = closeButton;
  }

  ngOnInit() { }
  closeWindow() {
    this.close.emit('window closed');
  }

}
