/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Component, OnInit, Input, SimpleChange, OnChanges, OnDestroy } from '@angular/core';
import { ToastrService } from 'ngx-toastr';
import { DocumentService } from '../../service/document.service';
import { GetDocListReqData } from '../../service/document.service';
import { DataService } from '../../service/data.service';
import { PagerService } from '../../service/pager.service';
import { SessionService } from '../../service/session.service';
import { LocalSessionService } from '../../service/local-session.service';
import { NgbModal, ModalDismissReasons, NgbModalOptions } from '@ng-bootstrap/ng-bootstrap';
import { DocumentData } from '../../data/document-data';
import { UserData } from '../../data/user-data';
import { AttributeData } from '../../data/attribute-data';
import { MessageInfo } from '../../utils/message-info';
import { UtilityService } from '../../service/utility.service';
import { EnumTaskStatus } from '../../common/task-status.enum';
import { QueueData } from '../../data/queue-data';
import { AttachmentData } from '../../data/attachment-data';
import { CONSTANTS } from '../../common/constants';
import { LocalSessionData } from '../../data/local-session-data';
import { BaseComponent } from '../../base.component';
import { formatDate } from '@angular/common';
import { ConfigDataHelper } from '../../utils/config-data-helper';
// import { FormControl } from '@angular/forms';
import { NiaTelemetryService } from '../../service/nia-telemetry.service';
import { Router, ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-doc-list',
  templateUrl: './doc-list.component.html',
  styleUrls: ['./doc-list.component.scss']
})
export class DocListComponent extends BaseComponent implements OnInit, OnChanges, OnDestroy {
  getClassName(): string {
    return "DocListComponent";
  }
  readonly EVENT_OPERATOR_GREATER_THAN_EQUAL = '>=';
  constructor(private toastr: ToastrService, private modalService: NgbModal, private documentService: DocumentService,
    private dataService: DataService, public sessionService: SessionService, private pagerService: PagerService,
    private msgInfo: MessageInfo, private utilityService: UtilityService, private localSessionService: LocalSessionService,
    public configDataHelper: ConfigDataHelper, public niaTelemetryService:NiaTelemetryService, private route: ActivatedRoute, private router: Router) {
    super(sessionService, configDataHelper, niaTelemetryService);
  }

  @Input() tableBodyHeightInPx: number;
  @Input() isRequired: boolean;
  @Input() isDocTypeFile: boolean;

  model: any = {
    selTabNum: 0,
    tooltip: "",
    isDataLoaded: false,
    fromDate: '',
    toDate: '',
    documentDataList: [],
    priDocumentDataList: [],
    documentData: DocumentData,
    queueNameCdeSelected: 0,
    apiResponseTimeInSecs: -1,
    apiMessage: '',
    pager: {},
    isReassignAllowed: false,
    selectedRow: 0,
    searchText: '',
    searchTextUsed: false,
    fromDateUsed: false,
    toDateUsed: false,
    columnSortOrderList: [],
    isSortAcrossPage: false,
    CONSTANT: {
      EMAILWB: {
        COL_CASE: 2,
        COL_FROM: 3,
        COL_SUBJECT:4,
        COL_CATEGORY:5,
        COL_RECEVIED_DATE:6
      },
      DOCWB: {
        COL_CASE: 0,
        COL_DOC_NAME: 1,
        COL_DOC_TYPE:2,
        COL_RECEVIED_DATE:3
      }

    }

  };

  private docHttpSubscription: any;
  private selectedQueueSubscription: any;
  private currentPage = 0;
  private totalPages = 0;
  private taskStatusCdeSelected: number;
  private taskStatusCde = 0;
  private taskStatusOperator = '=';
  private lockStatusCdeSelected: number;
  private subQueueNameCdeSelected = 0;
  private userSelected = -2;
  private userData: UserData = null;
  private attrNameCdes = '';
  private attachmentAttrNameCdes = '';
  private closeResult: string;
  private sortOrder: string;
  private sortByAttrNameCde: string;
  // For disabling mouse click and keystrokes outside modal window
  private ngbModalOptions: NgbModalOptions = {
    backdrop: 'static',
    keyboard: false
  };
  private COL_ATTR_NAME_CDE_MAP={
    DOCWB: {},
    EMAILWB: {}
  }


  ngOnInit() {
    let TOOLTIP = 'Format is key1:value1;key2:value2\n';
    TOOLTIP += 'e.g.\n';
    TOOLTIP += 'assignedto:john\n';
    TOOLTIP += 'filename:xyz.pdf\n';
    TOOLTIP += 'case:1001234';
    this.model.tooltip = TOOLTIP;
    // Mapping of column no. to attribute name code
    this.COL_ATTR_NAME_CDE_MAP.DOCWB[this.model.CONSTANT.DOCWB.COL_CASE]='';
    this.COL_ATTR_NAME_CDE_MAP.DOCWB[this.model.CONSTANT.DOCWB.COL_DOC_NAME]='30';
    this.COL_ATTR_NAME_CDE_MAP.DOCWB[this.model.CONSTANT.DOCWB.COL_DOC_TYPE]='31';
    this.COL_ATTR_NAME_CDE_MAP.DOCWB[this.model.CONSTANT.DOCWB.COL_RECEVIED_DATE]='2';
    this.COL_ATTR_NAME_CDE_MAP.EMAILWB[this.model.CONSTANT.EMAILWB.COL_CASE]='';
    this.COL_ATTR_NAME_CDE_MAP.EMAILWB[this.model.CONSTANT.EMAILWB.COL_FROM]='1';
    this.COL_ATTR_NAME_CDE_MAP.EMAILWB[this.model.CONSTANT.EMAILWB.COL_SUBJECT]='3';
    this.COL_ATTR_NAME_CDE_MAP.EMAILWB[this.model.CONSTANT.EMAILWB.COL_CATEGORY]='19';
    this.COL_ATTR_NAME_CDE_MAP.EMAILWB[this.model.CONSTANT.EMAILWB.COL_RECEVIED_DATE]='2';
    // Note: No direct service calls in init method here because all calls are made
    // via subscriptions to publications

    this.selectedQueueSubscription = this.dataService.selectedQueueNameCde.subscribe(data => {
      this.model.isDataLoaded = false;
      this.model.priDocumentDataList = [];
      this.model.documentDataList = [];
      if (data) {
        this.model.isDataLoaded = false;
        this.model.priDocumentDataList = [];
        this.model.documentDataList = [];
        this.model.queueNameCdeSelected = data['queueNameCde'];
        this.subQueueNameCdeSelected = data['subQueueNameCde'];

        this.taskStatusCde = this.subQueueNameCdeSelected;
        this.model.isReassignAllowed = !this.utilityService.isCaseClosed(this.taskStatusCde);
        this.sortByAttrNameCde = '';
        this.sortOrder = "desc";
        this.model.columnSortOrderList[0] = "desc";
        this.model.isSortAcrossPage = false;

        this.setPage(1);
      } else {
        this.model.isDataLoaded = true;
      }

    });
    this.lockStatusCdeSelected = 0; // Set to 0 for all types as part of release 1
    this.taskStatusCdeSelected = 0; // Initial value on load. Can change as per user action
    this.model.documentDataList = [];
    this.model.searchText = '';

  }

  ngOnDestroy() {
    this.selectedQueueSubscription.unsubscribe();
  }

  ngOnChanges(changes: { [propKey: string]: SimpleChange }) {
    // Note: No direct service calls in ngOnChanges method here because all calls are made
    // via subscriptions to publications
    const parent = this;
    if (changes['isDocTypeFile']) {
      this.attrNameCdes = '';
      this.attachmentAttrNameCdes = '';
      if (this.isDocTypeFile) {
        const FILE_ATTR_NAME_CDES = [CONSTANTS.ATTR_NAME_CDE.RECEIVED_DATE, CONSTANTS.ATTR_NAME_CDE.FILE_NAME,
          CONSTANTS.ATTR_NAME_CDE.DOCUMENT_TYPE]
        this.attrNameCdes = FILE_ATTR_NAME_CDES.join(",")
        this.attachmentAttrNameCdes = FILE_ATTR_NAME_CDES.join(",")
      } else {
        const EMAIL_ATTR_NAME_CDES = [CONSTANTS.ATTR_NAME_CDE.FROM, CONSTANTS.ATTR_NAME_CDE.RECEIVED_DATE,
          CONSTANTS.ATTR_NAME_CDE.SUBJECT, CONSTANTS.ATTR_NAME_CDE.FROM_ID, CONSTANTS.ATTR_NAME_CDE.SENTIMENT]
        const CASE_ATTR_NAME_CDES = [CONSTANTS.ATTR_NAME_CDE.CATEGORY]
        this.attrNameCdes = CASE_ATTR_NAME_CDES.concat(EMAIL_ATTR_NAME_CDES).join(",")
        this.attachmentAttrNameCdes = EMAIL_ATTR_NAME_CDES.join(",")
      }
    }
  }

  private getCaseListForQueue() {
    const parent = this;
    parent.sessionService.getLoggedInUserDetailsPromise()
      .then(function (value) {
        const userData: UserData = value as UserData;
        parent.userData = userData;
        parent.userSelected = -2;
        parent.model.selTabNum = 0;
        const localSessionData: LocalSessionData = parent.localSessionService.getLocalSessionData();

        parent.updateParamsForViewAndServiceCall(localSessionData.caseListSelectedTab);

        if (parent.taskStatusCde !== EnumTaskStatus.UNDEFINED && parent.taskStatusCde !== EnumTaskStatus.RETRY_LATER &&
          parent.taskStatusCde !== EnumTaskStatus.FAILED) {
          if (parent.taskStatusCde === EnumTaskStatus.ON_HOLD) {
            parent.taskStatusCdeSelected = EnumTaskStatus.FOR_YOUR_REVIEW;
          } else {
            parent.taskStatusCdeSelected = parent.taskStatusCde;
          }
          parent.getCaseList(parent.taskStatusCdeSelected, parent.taskStatusOperator,
            parent.lockStatusCdeSelected, parent.model.queueNameCdeSelected,
            parent.attrNameCdes, parent.attachmentAttrNameCdes);
        } else if (parent.taskStatusCde === EnumTaskStatus.UNDEFINED) {
          parent.taskStatusCdeSelected = EnumTaskStatus.YET_TO_START;
          parent.getCaseList(parent.taskStatusCdeSelected, parent.EVENT_OPERATOR_GREATER_THAN_EQUAL, parent.lockStatusCdeSelected,
            parent.model.queueNameCdeSelected, parent.attrNameCdes, parent.attachmentAttrNameCdes);
        } else {
          parent.model.isDataLoaded = true;
          parent.model.documentDataList = [];
        }

      });
  }

  onRowClick(rowNum: number) {
    this.model.selectedRow = rowNum;
    this.model.documentData = this.model.documentDataList[this.model.selectedRow];
    this.taskStatusCdeSelected = this.model.documentData.taskStatusCde;
    // this.appUserId = this.model.documentData.appUserId;
  }

  selectListType(selectedTabIndex: number) {
    console.log(selectedTabIndex);
    const parent = this;
    parent.currentPage = 0;
    parent.updateParamsForViewAndServiceCall(selectedTabIndex);
    const localSessionData: LocalSessionData = parent.localSessionService.getLocalSessionData();
    localSessionData.caseListSelectedTab = selectedTabIndex;
    parent.localSessionService.updateLocalSessionData(localSessionData);

    parent.refreshComponent();
  }


  assignCase(documentData: DocumentData) {
    const parent = this;
    const appUserId: number = parent.userData.userId;
    console.log(appUserId);
    this.documentService.assignUserToDoc(documentData.appUserId, appUserId, documentData.docId, function (error, data) {
      if (!error && data['responseCde'] as number === 0) {
        parent.toastr.success(parent.msgInfo.getMessage(103));

      } else if (data['responseCde'] as number === 105) {
        parent.toastr.error(parent.msgInfo.getMessage(104));
        console.log(error);
      } else {
        parent.toastr.error(parent.msgInfo.getMessage(102));
        console.log(error);
      }
      parent.refreshComponent();
      // parent.documentData = parent.documentDataList[parent.selectedRow];
    });
  }

  closeCase() {
    const parent = this;
    const document: DocumentData = parent.model.documentDataList[parent.model.selectedRow];
    this.documentService.closeDocCase(document.docId, parent.userData.userId, parent.model.queueNameCdeSelected, function (error, data) {
      if (data['responseCde'] as number === 0 && !error) {
        parent.toastr.success(parent.msgInfo.getMessage(105));
      } else if (data['responseCde'] as number === 107 && !error) {
        parent.toastr.error(parent.msgInfo.getMessage(138));
      } else if (error) {
        parent.toastr.error(parent.msgInfo.getMessage(102));
        console.log(error);
      } else {
        parent.toastr.error(parent.msgInfo.getMessage(119));
      }
      parent.refreshComponent();
    });
  }


  open(content) {
    this.modalService.open(content, this.ngbModalOptions).result.then((result) => {
      this.closeResult = `Closed with: ${result}`;
      if (result !== 'cancel') {
        this.refreshComponent();
      }
    }, (reason) => {
      this.closeResult = `Dismissed ${this.getDismissReason(reason)}`;
    });

  }

  refreshComponent() {
    // this.getDocumentList(this.taskStatusCdeSelected, this.lockStatusCdeSelected, this.EVENT_CDE_ATTRIBUTES_EXTRACTED,
    // this.EVENT_OPERATOR_GREATER_THAN, this.queueNameCdeSelected);
    this.taskStatusCdeSelected = this.taskStatusCde;
    this.lockStatusCdeSelected = 0; // Set to 0 for all types as part of release 1
    this.taskStatusOperator = '=';
    this.model.isDataLoaded = false;
    this.getCaseListForQueue();
  }

  clearFilter() {
    const parent = this;
    parent.model.searchText = ""
    parent.model.fromDate = ""
    parent.model.toDate = ""
    parent.applyFilter()
  }

  applyFilter() {
    const parent = this;

    if (parent.model.searchText.length > 0 && parent.model.searchText.indexOf(":") == -1) {
      if (isNaN(parent.model.searchText)) {
        parent.toastr.error(parent.msgInfo.getMessage(183));
        parent.model.documentDataList = [];
        parent.model.apiResponseTimeInSecs = -1;
        parent.model.apiMessage = "No records found";
        parent.model.isDataLoaded = true;
        parent.model.searchTextUsed = true
        return
      }
      parent.model.searchText = "case:" + parent.model.searchText
    }


    this.setPage(1);
    // const attributesToCheckFor: number[] = [CONSTANTS.ATTR_NAME_CDE.FROM, CONSTANTS.ATTR_NAME_CDE.RECEIVED_DATE,
    // CONSTANTS.ATTR_NAME_CDE.SUBJECT, CONSTANTS.ATTR_NAME_CDE.FILE_NAME];

    // let includeItem: boolean;
    // this.documentDataList = this.priDocumentDataList.filter(function (d) {
    //   includeItem = false;
    //   if (isNaN(+parent.searchText)) {
    //     d.attributes.forEach(function (a) {
    //       if (attributesToCheckFor.indexOf(a.attrNameCde) > -1 &&
    //         a.attrValue.toLowerCase().indexOf(parent.searchText.toLowerCase()) > -1) {
    //         includeItem = true;
    //         return;
    //       }
    //     });
    //   }
    //   return includeItem; // Apply no filter. Return all values
    // });
    // }
    // parent.documentData = parent.documentDataList[parent.selectedRow];
  }

  setPage(page: number) {
    const parent = this;
    if (parent.currentPage === 0 && parent.totalPages === 0) {
      parent.currentPage = page;
      parent.refreshComponent();
    } else if (!(page > parent.totalPages || page <= 0)) {
      parent.currentPage = page;
      parent.refreshComponent();
    }
  }

  getValueForAttrCde(docAttributeList: AttributeData[], code: number) {
    if (docAttributeList == null) { return ''; }
    const attributeDataList: AttributeData[] = docAttributeList.filter(function (p) {
      if (p.attrNameCde === code) {
        return p.attrValue;
      }
      return null;
    });
    if ((attributeDataList == null) || (attributeDataList.length === 0)) { return ''; }
    return attributeDataList[0].attrValue;
  }

  private updateParamsForViewAndServiceCall(selectedTabIndex) {
    const parent = this;
    switch (selectedTabIndex) {
      case 0://Unassigned Tab
        {
          parent.userSelected = -2; // Unassigned cases
          parent.model.selTabNum = selectedTabIndex;
          break;
        }
      case 1://Assigned Tab
        {
          parent.userSelected = -1; // Assigned cases to other users (i.e. excluding current user)
          parent.model.selTabNum = selectedTabIndex;
          break;
        }
      case 2://My Cases Tab
        {
          parent.userSelected = (parent.userData) ? parent.userData.userId : -2;
          parent.model.selTabNum = selectedTabIndex;
          break;
        }
      case 3://MyReview Tab
          {
            parent.userSelected = -3; // MyReview Cases
            parent.model.selTabNum = selectedTabIndex;
            break;
          }
      default:
        {
          parent.userSelected = -2; // Unassigned cases
          parent.model.selTabNum = 0; // Unassigned Tab
        }
    }

  }

  private getDismissReason(reason: any): string {
    if (reason === ModalDismissReasons.ESC) {
      return 'by pressing ESC';
    } else if (reason === ModalDismissReasons.BACKDROP_CLICK) {
      return 'by clicking on a backdrop';
    } else {
      return `with: ${reason}`;
    }
  }

  private getCaseList(taskStatusCde: number, taskStatusOperator: string, lockStatusCde: number, queueNameCde: number,
    attrNameCdes: string, attachmentAttrNameCdes: string) {

    const parent = this;
    let getDocListReqData: GetDocListReqData = new GetDocListReqData();
    getDocListReqData.queueNameCde = queueNameCde;
    getDocListReqData.taskStatusCde = taskStatusCde;
    getDocListReqData.taskStatusOperator = taskStatusOperator;
    getDocListReqData.lockStatusCde = lockStatusCde;
    getDocListReqData.pageNumber = parent.currentPage;
    getDocListReqData.appUserId = parent.userSelected;
    getDocListReqData.attrNameCdes = attrNameCdes;
    getDocListReqData.attachmentAttrNameCdes = attachmentAttrNameCdes;
    getDocListReqData.searchText = parent.model.searchText;
    getDocListReqData.sortByAttrNameCde = parent.sortByAttrNameCde;
    getDocListReqData.sortOrder = parent.sortOrder;
    parent.model.searchTextUsed = false;
    if (parent.model.searchText) {
      parent.model.searchTextUsed = true;
      let arrOfMultiSearchCriteria = [];
      arrOfMultiSearchCriteria = parent.model.searchText.split(";", -2);

      for (let i = 0; i < arrOfMultiSearchCriteria.length; i++) {
        let arrOfSearchCriteria: string[];
        arrOfSearchCriteria = arrOfMultiSearchCriteria[i].split(":", 2);
        let searchKey: string = arrOfSearchCriteria[0].toLowerCase();
        let searchVal: string = arrOfSearchCriteria[1];
        if (searchKey.includes("filename")) {
          getDocListReqData.fileNameVal = searchVal;
          getDocListReqData.fileNameKey = searchKey;
        }
        else if (searchKey.includes("assignedto")) {
          getDocListReqData.assignedToVal = searchVal;
          getDocListReqData.assignedToKey = searchKey;
        }
        else if (searchKey.includes("case")) {
          getDocListReqData.caseVal = searchVal;
          getDocListReqData.caseKey = searchKey;
        }

      }

      if (!getDocListReqData.caseKey) {// For non case searches
        if (!parent.model.fromDate) {
          let myDate = new Date()
          myDate.setDate(myDate.getDate() - 60)
          myDate.setHours(0, 0, 0, 0)
          parent.model.fromDate = myDate
        }
        if (!parent.model.toDate) {
          let myDate = new Date()
          myDate.setDate(myDate.getDate())
          myDate.setHours(23, 59, 59, 999)
          parent.model.toDate = myDate
        }

      }

    }


    parent.model.fromDateUsed = false;
    parent.model.toDateUsed = false;

    if (parent.model.fromDate) {
      getDocListReqData.fromDate = formatDate(new Date(parent.model.fromDate), 'yyyy-MM-dd HH:mm:ss', 'en');
      parent.model.fromDateUsed = true;
    }

    if (parent.model.toDate) {
      getDocListReqData.toDate = formatDate(new Date(parent.model.toDate), 'yyyy-MM-dd HH:mm:ss', 'en');
      parent.model.toDateUsed = true;
    }

    if (parent.docHttpSubscription != null) {
      parent.docHttpSubscription.unsubscribe();
    }
    parent.docHttpSubscription = this.documentService.getDocumentList(getDocListReqData, function (error, data) {
      if (!error) {

        if (data['responseCde'] != 0) {
          if (parent.msgInfo.getMessage(182) == data['response']) {
            parent.toastr.error(parent.msgInfo.getMessage(182));
          }
          else if (parent.msgInfo.getMessage(183) == data['response']) {
            parent.toastr.error(parent.msgInfo.getMessage(183));
          }
          else if (parent.msgInfo.getMessage(184) == data['response']) {
            parent.toastr.error(parent.msgInfo.getMessage(184));
          }
          else {
            parent.toastr.error(data['response']);
          }


          parent.model.priDocumentDataList = [];
          parent.model.documentDataList = parent.model.priDocumentDataList;
          parent.model.apiResponseTimeInSecs = -1;
          parent.model.apiMessage = "No records found";
          parent.model.documentData = parent.model.documentDataList[parent.model.selectedRow];

        } else {
          const tempDocumentDataList: DocumentData[] = data['response'];
          if (tempDocumentDataList.length > 0) {
            // Sort the attribute list within each document so that Subject(3) comes before Received Date(2)
            tempDocumentDataList.forEach(function (d) {
              if (d.appUserId <= 0 || d.appUserId == null) {
                d.assignedTo = 'Assign To Me';
              }
              d.attributes.sort((a, b) => {
                if (a.attrNameCde === 2 && b.attrNameCde === 3) {
                  return 1;
                }
                return 0;
              }
              );
            });
          }
          parent.model.apiMessage = ""
          parent.model.priDocumentDataList = tempDocumentDataList;
          parent.model.documentDataList = parent.model.priDocumentDataList;
          parent.model.apiResponseTimeInSecs = data['responseTimeInSecs']
          if (data['pagination'].totalItemCount) {
            parent.model.apiMessage = "Total Records: " + data['pagination'].totalItemCount;
          }

          if (parent.model.documentDataList.length == 0) {
            parent.model.apiMessage = "No records found";
          }
          parent.model.documentData = parent.model.documentDataList[parent.model.selectedRow];


        }

        if (data['pagination'] != null) {
          parent.currentPage = data['pagination'].currentPageNumber;
          parent.totalPages = data['pagination'].totalPageCount;
        }
        else {
          //if (getDocListReqData.caseVal) {
          parent.currentPage = 1;
          parent.totalPages = 1;
        }


        parent.model.pager = parent.pagerService.getPager(parent.totalPages, parent.currentPage);
      }
      else if (error) {
        parent.toastr.error("Invalid search request");
        parent.model.priDocumentDataList = [];
        parent.model.documentDataList = parent.model.priDocumentDataList;
        parent.model.apiResponseTimeInSecs = -1;
        parent.model.apiMessage = "No records found";
        parent.model.documentData = parent.model.documentDataList[parent.model.selectedRow];
      }
      parent.model.isDataLoaded = true;



    });
  }

  // To fix table header width alignment issue.
  setTableHeaderWidth(): void {
    const tableElement: HTMLElement = document.getElementById('doc-list');
    // To check table generated in UI or not
    if (tableElement != null) {
      const componentWidth = tableElement.parentElement.parentElement.parentElement.clientWidth - 1;
      tableElement['style']['width'] = componentWidth + 'px';
      const tableChildElements: HTMLCollection = tableElement.children;
      if (tableChildElements != null && tableChildElements.length === 2) {
        const theadTrElements: HTMLCollection = tableChildElements[0].children;
        if (theadTrElements != null && theadTrElements.length > 0) {
          const theadTrTdElements: HTMLCollection = theadTrElements[0].children;
          const tbodyTrElements: HTMLCollection = tableChildElements[1].children;
          // To check tbody generated in UI or not. If generated set td width to th. Else equal width shared by each th.
          if (tbodyTrElements != null && tbodyTrElements.length > 0) {
            const tbodyTrTdElements: HTMLCollection = tbodyTrElements[0].children;
            for (let i = 0; i < tbodyTrTdElements.length; i++) {
              theadTrTdElements[i]['style']['width'] = tbodyTrTdElements[i].clientWidth + 'px';
            }
          } else {
            const totalElement: number = theadTrTdElements.length;
            const elementWidth = componentWidth / totalElement;
            for (let i = 0; i < totalElement; i++) {
              theadTrTdElements[i]['style']['width'] = elementWidth + 'px';
            }
          }
        }
      }
    }
  }

  onColumnClick(event) {
    if (!this.model.isSortAcrossPage) {
      return
    }
    console.log("Inside onColumnClick func", event)
    let sortOrderList = this.model.columnSortOrderList
    let requestedSortOrder = event.sortDirection

    for (let i = 0; i < sortOrderList.length; i++) {
      sortOrderList[i] = ""
    }
    sortOrderList[event.sortColumn] = requestedSortOrder
    if (this.isDocTypeFile) {
      this.sortByAttrNameCde = this.COL_ATTR_NAME_CDE_MAP.DOCWB[event.sortColumn]
    }
    else {
      this.sortByAttrNameCde = this.COL_ATTR_NAME_CDE_MAP.EMAILWB[event.sortColumn]
    }
    this.sortOrder = sortOrderList[event.sortColumn]
    this.applyFilter()
  }

}
