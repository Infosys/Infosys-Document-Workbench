/** =============================================================================================================== *
 * Copyright 2022 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { UserData } from '../../data/user-data';
import { CONSTANTS } from '../../common/constants';
import { SessionService } from '../../service/session.service';
import { TreeItem } from '../nia-tree-view/tree-item';
import { DocumentService } from '../../service/document.service';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { LocalSessionData } from '../../data/local-session-data';
import { LocalSessionService } from '../../service/local-session.service';
import { TreeNode } from 'primeng/api';
import { AuditService } from '../../service/audit.service';
import { Router, Event, NavigationStart, NavigationEnd, NavigationError } from '@angular/router';
import { AdminService } from '../../service/admin.service';
import { UtilityService } from '../../service/utility.service';
import { BaseComponent } from '../../base.component';
import { ConfigDataHelper } from '../../utils/config-data-helper';
import { NiaTelemetryService } from '../../service/nia-telemetry.service';
import { DataService } from '../../service/data.service';
import { TreeTable } from 'primeng/treetable';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})

export class DashboardComponent extends BaseComponent implements OnInit {
  getClassName(): string {
    return "DashboardComponent";
  }
  model: any = {
    userData: UserData,
    treeItems: [] as TreeItem[],
    tableHeaders: [],
    selectedItemId: '',
    selectedQueueModeCde: 0,
    queueModeFlatEnabled: true,
    queueModeFlatVisible: true,
    queueModeHierarchyEnabled: true,
    queueModeHierarchyVisible: false,
    queueButtonsVisible: true,
    isDataLoaded: false,
    isQLDataLoaded: true,
    pending: 0,
    new: 0,
    reviewOrReworkTxt: 'Rework',
    reviewOrReworkTitleTxt: '',
    reviewOrReworkCnt: 0,
    assignedCasesCount: 0,
    completed: 0,
    queuesPending: 0,
    inProgress: 0,
    forYourReview: 0,
    forYourRework: 0,
    actionable: 0,
    actionableTitleTxt : '',
    showFooter: true,
    lastQueueWorkedOn: [''],
    lastCaseWorkedOn: [''],
    caseOwnerDict: {},
    selTabNum:0,
  };
  teammatesDetails: any = null;
  dataFinalList: DataFinalList[] = [];
  dataSource = new MatTableDataSource(this.dataFinalList);
  filter: string;
  treefilter: string;
  QueueDashboardProcess = class {
    static QUEUE_MODE_CDE_FLAT = 0;
    static QUEUE_MODE_CDE_HIERARCHY = 1;
  }
  private treeFinalDict: TreeDict[] = [];
  private tabSelected = 2;
  private selectedUserId = '';
  cols: any[];
  ftr: any[];
  treeStructured: TreeNode[];
  isAssignmentCntRequired: boolean = true;

  constructor(public sessionService: SessionService, private documentService: DocumentService,
    private localSessionService: LocalSessionService, private auditService: AuditService,
    private router: Router, private adminService: AdminService, private utilityService: UtilityService,
    private dataService: DataService, public configDataHelper: ConfigDataHelper, public niaTelemetryService: NiaTelemetryService,
  ) {
    super(sessionService, configDataHelper, niaTelemetryService);

    // for detecting the url change and changing the sidebar
    this.router.events.subscribe((event: Event) => {
      if (event instanceof NavigationEnd) {
        this.dataService.publishDocActionAddedEvent(true);
      }

      if (event instanceof NavigationError) {
        // Present error to user
        console.log(event.error);
      }
    });
  };

  ngOnInit() {
    this.triggerTelemetryEvents(CONSTANTS.TELEMETRY_EVENT.START);
    this.model.queueModeFlatEnabled = this.configDataHelper.getValue('sidebar.queueModeFlatEnabled');
    this.model.queueModeFlatVisible = this.configDataHelper.getValue('sidebar.queueModeFlatVisible');
    this.model.queueModeHierarchyEnabled = this.configDataHelper.getValue('sidebar.queueModeHierarchyEnabled');
    this.model.queueModeHierarchyVisible = this.configDataHelper.getValue('sidebar.queueModeHierarchyVisible');

    this.model.isDataLoaded = false;
    this.getLastWorkedQueue();
    this.getLastCaseWorked();

    this.getCaseOwnerDetails();//to get team member's name and their details in dropdown

    // Get user's choices stored in browser local storage
    const localSessionData: LocalSessionData = this.localSessionService.getLocalSessionData();

    // Handle tab selection
    this.model.selTabNum =0; // set as default which is 'My Cases' tab
    if (localSessionData.dashboardSelectedTab != LocalSessionService.UNDEFINED_NUMBER ) {
      // If user has access to second tab, then only go for preference check
      if (this.getFeature(this.bmodel.FID.DASHBOARD_CASE_ALL_VIEW).isEnabled) {
        // Get user's previously stored choices for tab selection from browser
        this.model.selTabNum = localSessionData.dashboardSelectedTab
      }
    }

    this.isAssignmentCntRequired = true; // Default for 'My Cases' tab
    this.tabSelected = 2;
    if (this.model.selTabNum === 1 ) {
      this.isAssignmentCntRequired = false
      this.tabSelected = 1;
    }

    // Handle queue mode selection
    this.model.selectedQueueModeCde =0 // set as default which is 'Flat Structure' tab
    if (localSessionData.dashboardSelectedMenuStyle != LocalSessionService.UNDEFINED_NUMBER ) {
      if (this.model.queueModeHierarchyEnabled) {
        this.model.selectedQueueModeCde = localSessionData.dashboardSelectedMenuStyle
      }
    }

    //below method called to populate data into table
    this.populateDataIntoTable(null, null);

  }


  ngAfterViewInit() {
    this.triggerTelemetryEvents(CONSTANTS.TELEMETRY_EVENT.IMPRESSION);
  }

  ngOnDestroy() {
    this.triggerTelemetryEvents(CONSTANTS.TELEMETRY_EVENT.END);
  }
  private sort: MatSort;
  @ViewChild(MatSort, { static: false }) set matSort(ms: MatSort) {
    this.sort = ms;
    this.dataSource.sort = this.sort;
  }


  @ViewChild('tt', {static: false}) table: TreeTable;


  onQueueModeClick(modeType: string) {
    const parent = this;
    parent.model.selectedQueueModeCde = modeType === 'flat' ? parent.QueueDashboardProcess.QUEUE_MODE_CDE_FLAT
      : parent.QueueDashboardProcess.QUEUE_MODE_CDE_HIERARCHY;

    const localSessionData: LocalSessionData = parent.localSessionService.getLocalSessionData();
    localSessionData.dashboardSelectedMenuStyle = parent.model.selectedQueueModeCde;
    parent.localSessionService.updateLocalSessionData(localSessionData);

  }

  //Calculating footer of table i.e. Total
  private getTotalNewCase() {
    return this.dataFinalList.map(t => t.newCase).reduce((acc, value) => acc + value, 0);
  }

  private getTotalInProgress() {
    return this.dataFinalList.map(t => t.inProgress).reduce((acc, value) => acc + value, 0);
  }

  private getTotalForYourReview() {
    return this.dataFinalList.map(t => t.forYourReview).reduce((acc, value) => acc + value, 0);
  }

  private getTotalForYourRework() {
    return this.dataFinalList.map(t => t.forYourRework).reduce((acc, value) => acc + value, 0);
  }

  private getTotalCompleted() {
    return this.dataFinalList.map(t => t.complete).reduce((acc, value) => acc + value, 0);
  }

  private getTotalActionable() {
    return this.dataFinalList.map(t => t.actionable).reduce((acc, value) => acc + value, 0);
  }

  applyFilter(filterValue: string) {
    this.model.showFooter = false;
    const tableFilters = [];
    tableFilters.push({
      id: "queue",
      value: filterValue
    });

    this.dataSource.filter = JSON.stringify(tableFilters);
    if (filterValue === '') {
      this.model.showFooter = true;
    }

    this.dataSource.filterPredicate =
      (data: DataFinalList, filtersJson: string) => {
        const matchFilter = [];
        const filters = JSON.parse(filtersJson);

        filters.forEach(filter => {
          const val = data[filter.id] === null ? '' : data[filter.id];
          matchFilter.push(val.toLowerCase().includes(filter.value.toLowerCase()));
        });
        return matchFilter.every(Boolean);
      };
  }

  clearFilter() {
    this.dataSource.filter = '';
    this.filter = '';
    this.model.showFooter = true;
  }


  clearTreeFilter() {
    this.treefilter = '';
    this.table.filter('', 'queue', 'contains');
  }
  private reloadTable(userId: string, teammatesDetails: any) {
    this.model.queuesPending = 0;
    this.model.assignedCasesCount = 0;
    this.model.pending = 0;
    this.model.new = 0;
    this.model.reviewOrReworkCnt = 0;
    this.model.completed = 0;
    this.dataFinalList = [];
    this.treeFinalDict = [];
    this.model.isDataLoaded = false;
    this.populateDataIntoTable(userId, teammatesDetails);
    this.dataSource = new MatTableDataSource(this.dataFinalList);
    if (this.dataSource.filter === '')
      this.model.showFooter = true;
  }


  public refresh() {
    if(this.selectedUserId === '' || this.selectedUserId === 'All')
      this.reloadTable(null, null);
    else
      this.reloadTable(this.selectedUserId, this.teammatesDetails);
  }
  public onSelected(userId: string) {
    this.selectedUserId = userId;
    if (userId == 'All') {
      this.reloadTable(null, null);
      this.isAssignmentCntRequired = false;
    }
    else{
      this.reloadTable(userId, this.teammatesDetails);
    }
  }

  private fetchUserQueueDetails(userId: string, teammatesDetails: any) {
    const parent = this;
    //After case owner is selected in drop down,case owner's queue details which is present in teammatesDetails
    return new Promise(function (fulfilled, rejected) {
      if (userId !== null && teammatesDetails !== null) {
        const teammate = teammatesDetails.filter(x => x.userId == userId);
        if (parent.utilityService.isListHasValue(teammate)) {
          fulfilled(teammate[0]);
          parent.isAssignmentCntRequired = true;
        }
        else {
          rejected();
        }

      }
      else {
        parent.sessionService.getLoggedInUserDetailsPromise()
          .then(function (data: UserData) {
            fulfilled(data);
          })
          .catch(function (error) {
            rejected();
          });
      }
    });
  }

  private fetchQueueStatsDetails(userId: string) {
    const parent = this;

    return new Promise(function (fulfilled, rejected) {
      parent.documentService.getDocCount(parent.isAssignmentCntRequired, null, userId, function (error, data) {
        if (!error) {
          fulfilled(data);
        } else {
          rejected();
        }
      });

    });

  }

  public getLastCaseWorked() {
    const parent = this;
    parent.model.isQLDataLoaded = false;

    parent.auditService.getDocLevelAuditForCurrentUser(1).then(function (data: any) {
      parent.model.lastCaseWorkedOn = '';
      if (data.length > 0) {
        parent.model.lastCaseWorkedOn = ['/home/workdata', data[0]['queueNameCde'], data[0]['docId']];
      }
      parent.model.isQLDataLoaded = true;
    });
  }

  private createStructure(nodes) {
    var objects = [];
    for (var i = 0; i < nodes.length; i++) {
      objects.push({
        data: nodes[i],
        children: []
      });
    }
    return objects
  }

  private getParent(child, nodes) {
    var parent = null;

    for (var i = 0; i < nodes.length; i++) {
      if (nodes[i].data.queueId === child.data.parentId) {
        return nodes[i];
      }
    }

    return parent;
  }

  private nodeMapBuild(hierarchy, nodeMap) {
    for (let i = 0; i < hierarchy.length; i++) {
      if (i == 0) {
        var tmp = hierarchy[i]
        if (!nodeMap.hasOwnProperty(tmp)) {
          nodeMap[tmp] = Object.keys(nodeMap).length + 1;
        }
      }
      else {
        tmp = hierarchy[i] + ":" + tmp
        if (!nodeMap.hasOwnProperty(tmp)) {
          nodeMap[tmp] = Object.keys(nodeMap).length + 1;
        }
      }
    }
    return nodeMap
  }

  private treeBuild(hierarchy, nodeMap, treeDict, colHeaders, item) {
    for (let i = hierarchy.length - 1; i > 0; i--) {
      var row = {}
      row['queue'] = hierarchy[i]
      var tmp = ''
      for (let j = i - 1; j > 0; j--) {
        tmp += hierarchy[j] + ":"
      }
      tmp += hierarchy[0]
      row['parentId'] = nodeMap[tmp]
      tmp = hierarchy[i] + ":" + tmp
      row['queueId'] = nodeMap[tmp]
      if (i == hierarchy.length - 1)
        row['queueCde'] = item['queueCde'];
      if (treeDict.hasOwnProperty(tmp)) {
        for (let header of colHeaders) {
          row[header] = treeDict[tmp][header] + item[header]
        }
      }
      else {
        for (let header of colHeaders) {
          row[header] = item[header]
        }
      }
      treeDict[tmp] = row
    }
    row = {}
    tmp = hierarchy[0]
    row['queue'] = hierarchy[0]
    row['queueId'] = nodeMap[hierarchy[0]]
    row['parentId'] = 0
    if (hierarchy.length == 1)
      row['queueCde'] = item['queueCde'];
    if (treeDict.hasOwnProperty(tmp)) {
      for (let header of colHeaders)
        row[header] = treeDict[tmp][header] + item[header]
    }
    else {
      for (let header of colHeaders)
        row[header] = item[header]
    }
    treeDict[tmp] = row
    return treeDict
  }



  public populateDataIntoTable(userId: string, teammatesDetails: any) {
    const parent = this;
    parent.model.isDataLoaded = false;
    const fetchUserQueueDetailsPromise = parent.fetchUserQueueDetails(userId, teammatesDetails);

    fetchUserQueueDetailsPromise.then(function (data0) {
      const fetchQueueStatsDetailsPromise = parent.fetchQueueStatsDetails(userId);
      fetchQueueStatsDetailsPromise.then(function (data1) {
        //After case owner is selected in drop downtable should have the common queues data
        // among current user and selected case owner.
        var data = [data0, data1];
        let queueDataList: any;
        if (userId !== null) {
          queueDataList = data[0]['commonQueueList'];
        }
        else {
          queueDataList = data[0]['queueDataList'];
        }

        var queueNameMapping = {}
        // creating key,value pair of queueNameCde: queueNameTxt -- For example-> 11: Customer Care (U)
        for (let i of queueDataList) {
          queueNameMapping[i['queueNameCde']] = i['queueNameTxt'];
        }

        let dataFinal = {};
        // Initializing dataFinal with all the rows of the table with zeros
        for (let i of queueDataList) {
          dataFinal[i['queueNameTxt']] = {};
          for (let header of CONSTANTS.DASHBOARD_ATTRIBUTES.TABLE_HEADERS.slice(1,)) {
            dataFinal[i['queueNameTxt']][header] = 0;
          };
          dataFinal[i['queueNameTxt']]['queueCde'] = i['queueNameCde'];
        };

        // Creating key:value pairs containing key as the queueName and related attributes(new, inProgress,..) as value
        if (!parent.isAssignmentCntRequired) {
          for (let [key, value] of Object.entries(data[1])) {
            var temp = queueNameMapping[value['queueNameCde']];
            dataFinal[temp][CONSTANTS.DASHBOARD_ATTRIBUTES.TASK_STATUS_CDE_MAP[value['taskStatusCde']]] = value['docCount'];
            // dataFinal[temp]['queueCde'] = value['queueNameCde']
            parent.model.assignedCasesCount += value['docCount'];
          }
        }
        else {
          for (let [key, value] of Object.entries(data[1])) {
            var temp = queueNameMapping[value['queueNameCde']];
            if(temp === undefined){ // if value['queueNameCde'] not in queueNameMapping then continue
              continue;
            }
            dataFinal[temp][CONSTANTS.DASHBOARD_ATTRIBUTES.TASK_STATUS_CDE_MAP[value['taskStatusCde']]] = value['myCasesCount'];
            dataFinal[temp]['queueCde'] = value['queueNameCde']
            parent.model.assignedCasesCount += value['myCasesCount'];
          }
        }

        //Calculation of Actionable based on the logged user and adding that to dataFinal
        for (let i of queueDataList) {
          if (parent.model.selTabNum === 1) { //For All Cases Tab
            dataFinal[i['queueNameTxt']]['actionable'] = dataFinal[i['queueNameTxt']][CONSTANTS.DASHBOARD_ATTRIBUTES.TASK_STATUS_CDE_MAP[400]];
          }
          else {
            dataFinal[i['queueNameTxt']]['actionable'] = dataFinal[i['queueNameTxt']][CONSTANTS.DASHBOARD_ATTRIBUTES.TASK_STATUS_CDE_MAP[100]] + dataFinal[i['queueNameTxt']][CONSTANTS.DASHBOARD_ATTRIBUTES.TASK_STATUS_CDE_MAP[450]];
          }
        }

        // Flattening the results of dataFinal Ex-{queueName:{new:1,..}} pair with {queue:queueName, new:1,..}
        for (var [key, val] of Object.entries(dataFinal)) {
          val["queue"] = key;
          parent.dataFinalList.push(val as unknown as DataFinalList);
        }


        // Total calculation
        parent.model.new = parent.getTotalNewCase();
        parent.model.inProgress = parent.getTotalInProgress();
        parent.model.forYourReview = parent.getTotalForYourReview();
        parent.model.forYourRework = parent.getTotalForYourRework();
        parent.model.actionable = parent.getTotalActionable();
        parent.model.completed = parent.getTotalCompleted();
        parent.model.pending = parent.getTotalActionable();

        if (parent.model.selTabNum === 1) { // All Cases tab
          parent.model.reviewOrReworkTxt = "Review"
          parent.model.reviewOrReworkTitleTxt = "Total number of cases waiting for user review"
          parent.model.actionableTitleTxt = "Total number of cases waiting for user response (= For Your Review)"
          parent.model.reviewOrReworkCnt = parent.getTotalForYourReview();

        }
        else {
          parent.model.reviewOrReworkTxt = "Rework"
          parent.model.reviewOrReworkTitleTxt = "Total number of cases waiting for user rework"
          parent.model.actionableTitleTxt = "Total number of cases waiting for user response (= New + For Your Rework)"
          parent.model.reviewOrReworkCnt = parent.getTotalForYourRework();
        }

        for (let [key, value] of Object.entries(dataFinal)) {
          if (value['actionable'] > 0) {
            parent.model.queuesPending += 1;
          }
        }

        // TREE HIERARCHY STARTS HERE
        var nodeMap = {};
        var colHeaders = ['newCase', 'inProgress', 'forYourReview', 'forYourRework', 'complete', 'actionable'];
        var queueNameSplitDelimiter = parent.configDataHelper.getValue('sidebar.queueModeHierarchyDelimiter');
        for (let item of parent.dataFinalList) {
          var hierarchy = item['queue'].split(queueNameSplitDelimiter);
          nodeMap = parent.nodeMapBuild(hierarchy, nodeMap)
        }

        var treeDict = {}
        for (let item of parent.dataFinalList) {
          var hierarchy = item['queue'].split(':');
          treeDict = parent.treeBuild(hierarchy, nodeMap, treeDict, colHeaders, item)
        }

        for (let [key, val] of Object.entries(treeDict)) {
          parent.treeFinalDict.push(val as unknown as TreeDict);
        }

        // sorting treeFinalDict based on the queueId
        parent.treeFinalDict.sort(function (a, b) {
          return a.queueId - b.queueId;
        });

        // Converting flat into tree Structure
        var nodeObjects = parent.createStructure(parent.treeFinalDict);

        for (let i = nodeObjects.length - 1; i >= 0; i--) {
          var currentNode = nodeObjects[i];
          if (currentNode.data.parentId === 0) {
            continue;
          }
          var prnt = parent.getParent(currentNode, nodeObjects);

          if (prnt === null) {
            continue;
          }

          prnt.children.push(currentNode);
          nodeObjects.splice(i, 1);
        }

        parent.treeStructured = nodeObjects;

        parent.cols = [
          { field: 'queue', header: 'Queue', width: "23vw", paddingLeft: "32px"},
          { field: 'newCase', header: 'New', width: "", paddingLeft: "" },
          { field: 'inProgress', header: 'In Progress', width: "", paddingLeft: "" },
          { field: 'forYourReview', header: 'For Your Review', width: "", paddingLeft: "" },
          { field: 'forYourRework', header: 'For Your Rework', width: "", paddingLeft: "" },
          { field: 'complete', header: 'Complete', width: "", paddingLeft: "" },
          { field: 'actionable', header: 'Actionable', width: "", paddingLeft: "" },
        ];

        parent.ftr = [
          { field: "Total", width: "23vw", paddingLeft: "32px" },
          { field: parent.model.new, width: "", paddingLeft: "0px" },
          { field: parent.model.inProgress, width: "", paddingLeft: "0px" },
          { field: parent.model.forYourReview, width: "", paddingLeft: "0px" },
          { field: parent.model.forYourRework, width: "", paddingLeft: "0px" },
          { field: parent.model.completed, width: "", paddingLeft: "0px" },
          { field: parent.model.actionable, width: "", paddingLeft: "0px" },
        ]
        parent.model.isDataLoaded = true;
      });


    })

    parent.model.tableHeaders = CONSTANTS.DASHBOARD_ATTRIBUTES.TABLE_HEADERS;

  }

  public navigate(queueNameCde, taskStatusCde) {
    const localSessionData: LocalSessionData = this.localSessionService.getLocalSessionData();
    localSessionData.caseListSelectedTab = this.tabSelected;
    this.localSessionService.updateLocalSessionData(localSessionData);
    return ['/home/worklist', queueNameCde, taskStatusCde];
  }

  public navigateTo(queueNameCde, index) {
    var indexMap = {
      1: 100, 2: 200, 3: 400, 4: 450, 5: 900
    }
    const localSessionData: LocalSessionData = this.localSessionService.getLocalSessionData();
    localSessionData.caseListSelectedTab = this.tabSelected;
    this.localSessionService.updateLocalSessionData(localSessionData);
    return ['/home/worklist', queueNameCde, indexMap[index]];

  }

  public getLastWorkedQueue() {
    const localSessionData: LocalSessionData = this.localSessionService.getLocalSessionData();
    if (localSessionData.lastWorkedQueueCde > 0) {
      this.model.lastQueueWorkedOn = ['/home/worklist', localSessionData.lastWorkedQueueCde, (localSessionData.lastDocStatusCde) ? localSessionData.lastDocStatusCde : 100];
    }

  }

  public getCaseOwnerDetails() {
    if(!this.getFeature(this.bmodel.FID.DASHBOARD_CASE_TEAM_VIEW).isEnabled){
      return
    }
    const parent = this;
    let teammateDataDict = {};
    parent.adminService.getListOfTeammates().then(function (data: any) {
      if (data.length > 0) {
        for (let i = 0; i < data.length; i++) {
          teammateDataDict[ data[i]['userName'] + " (" + data[i]['roleTypeTxt'] + ") " ] = data[i]['userId'];
        }
        parent.model.caseOwnerDict = teammateDataDict;
        // Storing team members data ,so that can be use later in populateDataIntoTable(),after dropdown selection
        parent.teammatesDetails = data;
      }
    });
  }

  public onTabClick(tabName: string){
    if(!this.getFeature(this.bmodel.FID.DASHBOARD_CASE_ALL_VIEW).isEnabled){
      return
    }
    this.model.queuesPending = 0;
    this.model.assignedCasesCount = 0;
    this.model.pending = 0;
    this.model.new = 0;
    this.model.reviewOrReworkCnt = 0;
    this.model.completed = 0;
    this.dataFinalList = [];
    this.treeFinalDict = [];
    this.model.isDataLoaded = false;
    this.dataSource.filter = '';
    this.filter = '';
    this.model.showFooter = true;
    if(tabName == 'allCases'){
      this.tabSelected = 1;
      this.model.selTabNum = 1;
      this.isAssignmentCntRequired = false;
      this.populateDataIntoTable(null, null);
      this.dataSource = new MatTableDataSource(this.dataFinalList);
      if (this.dataSource.filter === '')
      this.model.showFooter = true;
    }
    else{
      this.tabSelected = 2;
      this.model.selTabNum = 0;
      this.isAssignmentCntRequired = true;
      this.populateDataIntoTable(null,null);
      this.dataSource = new MatTableDataSource(this.dataFinalList);
      if (this.dataSource.filter === '')
      this.model.showFooter = true;
    }
    // Update user preference to user's browser
    const localSessionData: LocalSessionData = this.localSessionService.getLocalSessionData();
    localSessionData.dashboardSelectedTab = this.model.selTabNum;
    this.localSessionService.updateLocalSessionData(localSessionData);

    this.dataSource.filterPredicate =
      (data: DataFinalList, filtersJson: string) => {
        const matchFilter = [];
        const filters = JSON.parse(filtersJson);

        filters.forEach(filter => {
          const val = data[filter.id] === null ? '' : data[filter.id];
          matchFilter.push(val.toLowerCase().includes(filter.value.toLowerCase()));
        });
        return matchFilter.every(Boolean);
      };

  }

}


export interface DataFinalList {
  queue: string;
  queueCde: number;
  newCase: number;
  inProgress: number;
  forYourReview: number;
  forYourRework: number;
  complete: number;
  actionable: number;
}

export interface TreeDict {
  queueId: number;
  parentId: number;
  queue: string;
  newCase: number;
  inProgress: number;
  forYourReview: number;
  forYourRework: number;
  complete: number;
  actionable: number;
}




