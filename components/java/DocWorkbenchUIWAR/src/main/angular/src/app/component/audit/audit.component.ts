/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { UtilityService } from './../../service/utility.service';
import { Component, OnInit, Output, EventEmitter, Input } from '@angular/core';
import { AuditService } from '../../service/audit.service';
import { AuditData } from '../../data/audit-data';
import { PagerService } from '../../service/pager.service';
import { NgbModal, ModalDismissReasons, NgbModalOptions } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-audit',
  templateUrl: './audit.component.html',
  styleUrls: ['./audit.component.scss']
})
export class AuditComponent implements OnInit {

  constructor(private auditService: AuditService, private pagerService: PagerService, private modalService: NgbModal,
    private utilityService: UtilityService) { }

  @Output() close = new EventEmitter<string>();
  @Input() title: string;
  @Input() auditData: AuditData;

  // For disabling mouse click and keystrokes outside modal window
  ngbModalOptions: NgbModalOptions = {
    backdrop: 'static',
    keyboard: false
  };
  closeResult: string;
  currentJsonValue: string;
  previousJsonValue: string;
  auditDataList: AuditData[] = [];
  isDataLoaded = false;
  currentPage = 0;
  totalPages = 0;
  pager: any = {};

  ngOnInit() {
    this.setPage(1);
  }

  closeWindow() {
    this.close.emit('window closed');
  }

  setPage(page: number) {
    const parent = this;
    parent.currentPage = page;
    parent.getData();
  }

  getData() {
    const parent = this;
    if (parent.auditData.docId != null && parent.auditData.docId > 0) {
      parent.auditService.getAuditForDocument(parent.auditData.docId, parent.currentPage, function (error, data) {
        if (!error) {
          const auditDataList = data['response'];
          if (auditDataList != null) {
            for (let i = 0; i < auditDataList.length; i++) {
              const message: String = auditDataList[i].auditMessage.replace(new RegExp('\\[', 'g'), '<b>').
                replace(new RegExp('\\]', 'g'), '</b>');
              auditDataList[i].auditMessage = message;
            }
          }

          parent.auditDataList = auditDataList;
        } else {
          parent.auditDataList = [];
        }
        if (data['pagination'] != null) {
          parent.currentPage = data['pagination'].currentPageNumber;
          parent.totalPages = data['pagination'].totalPageCount;
        }
        parent.pager = parent.pagerService.getPager(parent.totalPages, parent.currentPage);
        parent.isDataLoaded = true;
      });
    } else if (parent.auditData.appUserId != null && parent.auditData.appUserId > 0) {
      parent.auditService.getAuditForUser(parent.auditData.appUserId, parent.currentPage, function (error, data) {
        if (!error) {
          const auditDataList = data['response'];
          if (auditDataList != null) {
            for (let i = 0; i < auditDataList.length; i++) {
              const message: String = auditDataList[i].auditMessage.replace(new RegExp('\\[', 'g'), '<b>').
                replace(new RegExp('\\]', 'g'), '</b>');
              auditDataList[i].auditMessage = message;
            }
          }
          parent.auditDataList = auditDataList;

        } else {
          parent.auditDataList = [];
        }
        if (data['pagination'] != null) {
          parent.currentPage = data['pagination'].currentPageNumber;
          parent.totalPages = data['pagination'].totalPageCount;
        }
        parent.pager = parent.pagerService.getPager(parent.totalPages, parent.currentPage);
        parent.isDataLoaded = true;
      });
    }else{
      parent.auditService.getAuditForAppVariable(parent.auditData.appVarKey, parent.currentPage, function (error, data) {
        if (!error) {
          const auditDataList = data['response'];
          if (auditDataList != null) {
            for (let i = 0; i < auditDataList.length; i++) {
              const message: String = auditDataList[i].auditMessage.replace(new RegExp('\\[', 'g'), '<b>').
                replace(new RegExp('\\]', 'g'), '</b>');
              auditDataList[i].auditMessage = message;
            }
          }
          parent.auditDataList = auditDataList;

        } else {
          parent.auditDataList = [];
        }
        if (data['pagination'] != null) {
          parent.currentPage = data['pagination'].currentPageNumber;
          parent.totalPages = data['pagination'].totalPageCount;
        }
        parent.pager = parent.pagerService.getPager(parent.totalPages, parent.currentPage);
        parent.isDataLoaded = true;
      });
    }

  }

  open(content, currentJsonValue: string, previousJsonValue: string) {
    this.currentJsonValue = JSON.parse(JSON.stringify(currentJsonValue));
    this.previousJsonValue = JSON.parse(JSON.stringify(previousJsonValue));
    this.modalService.open(content, this.ngbModalOptions).result.then((result) => {
      this.closeResult = `Closed with: ${result}`;
    }, (reason) => {
      this.closeResult = `Dismissed ${this.getDismissReason(reason)}`;
    });

  }

  testJSON(item: any): boolean {
    return this.utilityService.testJSON(item);
  }

  private getDismissReason(reason: any): string {
    if (reason == ModalDismissReasons.ESC) {
      return 'by pressing ESC';
    } else if (reason == ModalDismissReasons.BACKDROP_CLICK) {
      return 'by clicking on a backdrop';
    } else {
      return `with: ${reason}`;
    }
  }



}
