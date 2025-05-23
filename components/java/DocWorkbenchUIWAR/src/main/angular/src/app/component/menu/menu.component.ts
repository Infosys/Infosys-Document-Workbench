/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { DocumentService } from '../../service/document.service';
import { DataService } from '../../service/data.service';
import { ValData } from '../../data/val-data';
import { DocCountData } from '../../data/doccount-data';
import { TreeItem } from '../nia-tree-view/tree-item';
import { UserData } from '../../data/user-data';
import { SessionService } from '../../service/session.service';
import { ValService } from '../../service/val.service';
import { EnumTaskStatus } from '../../common/task-status.enum';
import { BaseComponent } from '../../base.component';
import { ConfigDataHelper } from '../../utils/config-data-helper';
import { LocalSessionData } from '../../data/local-session-data';
import { LocalSessionService } from '../../service/local-session.service';
import { NiaTelemetryService } from '../../service/nia-telemetry.service';
import { QueueData } from '../../data/queue-data';

@Component({
  selector: 'app-menu',
  templateUrl: './menu.component.html',
  styleUrls: ['./menu.component.scss']
})
export class MenuComponent extends BaseComponent implements OnInit, OnDestroy {

  getClassName(): string {
    return "MenuComponent";
  }
  constructor(private route: ActivatedRoute,
    private documentService: DocumentService,
    private valService: ValService,
    private dataService: DataService,
    public sessionService: SessionService,
    public configDataHelper: ConfigDataHelper,
    private localSessionService: LocalSessionService, public niaTelemetryService: NiaTelemetryService) {
    super(sessionService, configDataHelper, niaTelemetryService)
  }

  // Constants
  private EVENT_TYPE_ON_INIT = 1;
  private EVENT_TYPE_DOC_SUBSCRIPTION = 2;
  private UNSET_INTEGER_VALUE = -1;
  // Variables
  private routeEventsSubscription: any;
  private selectedQueueSubscription: any;
  private docActionSubscription: any;
  private docCountSubscription: any;
  private valServiceSubscription: any;
  private selectedSubQueueNameCde = this.UNSET_INTEGER_VALUE;
  private selectedQueueNameCde = this.UNSET_INTEGER_VALUE;
  private currentUrl = '';
  private taskStatusCdeList: number[] = [];
  private queueDocStatusMap = new Map();
  private subQueuesList: string[] = [];
  private userData: UserData;
  private navigationTriggeredFromSidebar = false;

  model: any = {
    treeItems: [] as TreeItem[],
    selectedItemId: '',
    selectedQueueModeCde: -1,
    queueModeFlatEnabled: false,
    queueModeFlatVisible: false,
    queueModeHierarchyEnabled: false,
    queueModeHierarchyVisible: false,
    queueButtonsVisible: false
  }; // For binding to view

  TreeViewUtil = class {
    static searchById(treeItemsX: TreeItem[], searchId?: string) {
      return this.searchInTreeItems(treeItemsX, searchId)
    }
    static searchByCde(treeItemsX: TreeItem[], searchCde?: string) {
      return this.searchInTreeItems(treeItemsX, null, searchCde)
    }
    static searchByLabel(treeItemsX: TreeItem[], searchLabel?: string) {
      return this.searchInTreeItems(treeItemsX, null, null, searchLabel)
    }
    static searchByUrl(treeItemsX: TreeItem[], searchUrl?: string) {
      return this.searchInTreeItems(treeItemsX, null, null, null, searchUrl)
    }
    private static searchInTreeItems(treeItemsX: TreeItem[], searchId?: string,
      searchCde?: string, searchLabel?: string, searchUrl?: string) {
      // Input validation
      const parent = this;
      const searchParamCount = (searchId ? 1 : 0) + (searchCde ? 1 : 0)
        + (searchLabel ? 1 : 0) + (searchUrl ? 1 : 0)
      if (searchParamCount != 1) {
        throw Error("Only one search parameter can be populated")
      }
      for (const treeItemX of treeItemsX) {
        if (searchId && treeItemX.id === searchId) {
          return treeItemX;
        } else if (searchCde && treeItemX.cde === searchCde) {
          return treeItemX
        } else if (searchLabel && treeItemX.label === searchLabel) {
          return treeItemX
        } else if (searchUrl && treeItemX.url === searchUrl) {
          return treeItemX
        }
        if (treeItemX.items) {
          const result = parent.searchInTreeItems(treeItemX.items, searchId, searchCde,
            searchLabel, searchUrl)
          if (result)
            return result
        }
      }
      return null
    }
  }

  QueueMenuProcess = class {

    static QUEUE_MODE_CDE_FLAT = 0;
    static QUEUE_MODE_CDE_HIERARCHY = 1;
    static QUEUE_NAME_SPLIT_DELIMITER_INVALID = "#$%@#"; //Garbage value to block split
    static QUEUE_TYPE_OPEN = 'OPEN';
    static QUEUE_TYPE_CLOSED = 'CLOSED';
    static MENU_LABEL_MY_QUEUES = 'My Queues';
    static MENU_LABEL_MY_QUEUES_CLOSED = 'My Queues - Closed';

    static addLeafNodesForQueue(treeItemX: TreeItem, queueNameCdeStr: string,
      queueDocStatusMap: Map<any, any>, taskStatusCdeList: number[],
      subQueuesList: string[]) {
      const parent = this;
      let treeItemY: TreeItem;
      let docCount = queueDocStatusMap.get(queueNameCdeStr);
      // If map doesn't have value yet, then create zero values for count
      if (!docCount) {
        docCount = Array(taskStatusCdeList.length).fill(0)
      }
      for (let k = 0; k < taskStatusCdeList.length; k++) {
        const taskStatusCdeStr = String(taskStatusCdeList[k]);
        // const idK: string = treeItemX.id + '.' + String(k + 1);
        const idK: string = treeItemX.id + '.' + String(k + 1).padStart(2, '0');
        const urlK: string = `home/worklist/${queueNameCdeStr}/${taskStatusCdeStr}`;
        treeItemY = {
          id: idK,
          label: subQueuesList[k] + ' (' + docCount[k] + ')',
          cde: taskStatusCdeStr,
          url: urlK
        };
        treeItemX.items.push(treeItemY);
      }
    }

    static getNestedQueueDataMap(userQueueDataList: QueueData[], delimiter: string) {
      const parent = this;
      const queueDataList = userQueueDataList.map(function (p) {
        return [String(p.queueNameCde), p.queueNameTxt]
      })
      let queueDataMap = new Map<string, any>();
      let queueNameCdeList = []
      for (let i = 0; i < queueDataList.length; i++) {
        var queueData = queueDataList[i]
        var queueNameCde = queueData[0]
        var queueNameTxt = queueData[1]
        const tokens = queueNameTxt.split(delimiter)
        let currentMap = queueDataMap
        tokens.forEach((token, index, arr) => {
          if (!currentMap.has(token)) {
            currentMap.set(token, new Map<string, any>())
          }
          currentMap = currentMap.get(token)
          // If last item
          if (index == arr.length - 1) {
            // currentMap.set('queueNameCde', queueNameCde)
            currentMap.set(queueNameCde, null)
            queueNameCdeList.push(queueNameCde)
          }
        });
      }
      return [queueDataMap, queueNameCdeList]
    }

    /**
     * Compacts nested map by merging parent and single child as one
     * @param dataMap
     */
    static compactNestedMap(dataMap: Map<string, any>, delimiter: string) {
      const parent = this;
      if (!dataMap) {
        return
      }
      // traverse horizontally
      let localDataMap = dataMap
      for (let entry of localDataMap) {
        const key = entry[0]
        const valueMap: Map<string, any> = entry[1]
        if (!valueMap) {
          continue
        }
        // Assign current as self
        let currentValueMap = valueMap;
        // Shift child node upwards only if child present with both key and value
        if (currentValueMap.size == 1) {
          const childKey = valueMap.keys().next().value
          const childValueMap = valueMap.values().next().value
          if (!(childKey && childValueMap)) {
            continue
          }
          const newKey = key + delimiter + childKey
          localDataMap.set(newKey, childValueMap)
          localDataMap.delete(key)
          // Assign current as child who has now moved up one level
          currentValueMap = childValueMap
        }
        if (currentValueMap) {
          parent.compactNestedMap(currentValueMap, delimiter)
          // traverse vertically
          // currentValueMap.forEach((value1Map: Map<string, any>, key1: string) => {
          //   parent.compactNestedMap(value1Map, delimiter)
          // });
        }
      }
    }

    static populateQueueDataInTreeView(queueDataMap, idPrefix, treeItemX: TreeItem,
      queueDocStatusMap: Map<any, any>, taskStatusCdeList: number[],
      subQueuesList: string[], level = 0) {
      const parent = this;
      let row = 0
      let treeItemY: TreeItem;
      for (let [key, value] of queueDataMap) {
        row += 1
        let leafNode = true
        if ((value instanceof Map)) {
          leafNode = false
        }
        const idStr = idPrefix + '.' + String(row).padStart(2, '0');
        const str1 = leafNode ? "EOL" : ""
        // console.log("-".repeat(level) , idStr,  key , str1);
        const queueNameCdeStr = leafNode ? key : ''
        if (leafNode) {
          treeItemX.cde = queueNameCdeStr
          parent.addLeafNodesForQueue(treeItemX, queueNameCdeStr, queueDocStatusMap, taskStatusCdeList,
            subQueuesList);
        } else {
          treeItemY = {
            id: idStr,
            label: key,
            cde: queueNameCdeStr,
            items: [],
            // url: 'home/worklist/' + queueNameCdeStr
            url: ''
          };
          treeItemX.items.push(treeItemY);
        }


        if (!leafNode) {
          parent.populateQueueDataInTreeView(value, idStr, treeItemY, queueDocStatusMap, taskStatusCdeList,
            subQueuesList, level + 1)
        }// } else {
        //   console.log(" ".repeat(level), "EOL")
        // }
      }
    }

    static updateModelForVisibility(model: any) {
      // If only one button is available to user, then don't show it
      const buttonsVisibleCount = (model.queueModeFlatVisible ? 1 : 0) + (model.queueModeHierarchyVisible ? 1 : 0)
      model.queueButtonsVisible = buttonsVisibleCount > 1 ? true : false
    }

    static getQueueMode(model: any, localSessionData: LocalSessionData) {
      // If both modes are available
      if (model.queueModeFlatEnabled && model.queueModeHierarchyEnabled) {
        let selectedQueueModeCde = this.QUEUE_MODE_CDE_FLAT; //Default mode is Flat
        if (localSessionData.sidebarSelectedMenuStyle != LocalSessionService.UNDEFINED_NUMBER) {
          selectedQueueModeCde = localSessionData.sidebarSelectedMenuStyle
        }
        return selectedQueueModeCde
      } else if (model.queueModeFlatEnabled) {
        return this.QUEUE_MODE_CDE_FLAT;
      } else if (model.queueModeHierarchyEnabled) {
        return this.QUEUE_MODE_CDE_HIERARCHY;
      }

    }


    static createQueueTreeItem(userQueueDataList: QueueData[], queueType: string,
      queueNameSplitDelimiter: string, queueDocStatusMap: Map<any, any>,
      taskStatusCdeList: number[], subQueuesList: string[]) {
      let treeItemI: TreeItem;
      if (queueType == this.QUEUE_TYPE_OPEN) {
        treeItemI = {
          id: '03 ',
          label: this.MENU_LABEL_MY_QUEUES,
          isVisible: true,
          items: [],
          url: 'home/myqueues'
        };
      } else if (queueType == this.QUEUE_TYPE_CLOSED) {
        treeItemI = {
          id: '04 ',
          label: this.MENU_LABEL_MY_QUEUES_CLOSED,
          isVisible: true,
          items: [],
          url: 'home/myclosedqueues'
        };
      }
      // Create nested queue data map

      let [queueDataMap, _] = this.getNestedQueueDataMap(userQueueDataList,
        queueNameSplitDelimiter)
      // console.log('queueDataMap', queueDataMap)

      // Compact the nested map to reduce unwanted clicks
      this.compactNestedMap(queueDataMap as Map<string, any>,
        queueNameSplitDelimiter)

      this.populateQueueDataInTreeView(queueDataMap, treeItemI.id, treeItemI,
        queueDocStatusMap, taskStatusCdeList, subQueuesList)

      return treeItemI;
    }

  }

  ngOnInit() {
    // console.log("ngOnInit");
    const parent = this;
    parent.model.queueModeFlatEnabled = parent.configDataHelper.getValue('sidebar.queueModeFlatEnabled');
    parent.model.queueModeFlatVisible = parent.configDataHelper.getValue('sidebar.queueModeFlatVisible');
    parent.model.queueModeHierarchyEnabled = parent.configDataHelper.getValue('sidebar.queueModeHierarchyEnabled');
    parent.model.queueModeHierarchyVisible = parent.configDataHelper.getValue('sidebar.queueModeHierarchyVisible');
    this.QueueMenuProcess.updateModelForVisibility(parent.model)
    this.handleNewPageRequest(this.EVENT_TYPE_ON_INIT);


    this.docActionSubscription = this.dataService.docActionAddedEvent.subscribe(message => {
      if (message) {
        // parent.getDocCount(parent.selectedQueueNameCde);
        parent.fetchAndUpdateIndividualTaskStatusCount().then(function () {
          parent.reloadTreeview();
        });
      }
    });
    this.selectedQueueSubscription = this.dataService.selectedQueueNameCde.subscribe(data => {
      if (data) {
        if (!parent.navigationTriggeredFromSidebar) {
          parent.selectedQueueNameCde = data['queueNameCde'];
          parent.selectedSubQueueNameCde = data['subQueueNameCde'];
          console.log('From subscription: Q=' + parent.selectedQueueNameCde + '|SQ=' + parent.selectedSubQueueNameCde);
          // parent.getQueueDataList();
          parent.handleNewPageRequest(parent.EVENT_TYPE_DOC_SUBSCRIPTION);
        }
      }
    });
    // Reset queuenamecde and subqueuenamecde if URL changes. Assumption is that
    // this.dataService.selectedQueueNameCde will provide new values and happens
    // AFTER route change event is detected
    parent.routeEventsSubscription = this.route.url.subscribe(activeUrl => {
      const newUrl: string = window.location.hash.split('?')[0];
      if (parent.currentUrl !== newUrl) {
        parent.currentUrl = newUrl;
        parent.selectedQueueNameCde = parent.UNSET_INTEGER_VALUE;
        parent.selectedSubQueueNameCde = parent.UNSET_INTEGER_VALUE;
      }
    });
  }

  ngOnDestroy() {
    this.routeEventsSubscription.unsubscribe();
    this.selectedQueueSubscription.unsubscribe();
    this.docActionSubscription.unsubscribe();
    if (this.valServiceSubscription) {
      this.valServiceSubscription.unsubscribe();
    }
  }

  // Method called when user clicks on menu item
  onTreeItemClick(itemId: string) {
    // console.log("onTreeItemClick", itemId)
    // This variable is to differentiate between navigation happening as a result of side bar click
    // vs navigation happening because of URL entered in browser's addressbar
    this.navigationTriggeredFromSidebar = true;
    this.updateQueueCount(itemId);
  }

  onQueueModeClick(name: string) {
    const parent = this;
    console.log(name)
    if (name === 'flat' && !parent.model.queueModeFlatEnabled) {
      return
    }
    if (name === 'hierarchy' && !parent.model.queueModeHierarchyEnabled) {
      return
    }
    const selectedQueueModeCde = name === 'flat' ? parent.QueueMenuProcess.QUEUE_MODE_CDE_FLAT
      : parent.QueueMenuProcess.QUEUE_MODE_CDE_HIERARCHY;
    // Update local session
    const localSessionData: LocalSessionData = parent.localSessionService.getLocalSessionData();
    localSessionData.sidebarSelectedMenuStyle = selectedQueueModeCde
    parent.localSessionService.updateLocalSessionData(localSessionData);
    // Reload data
    parent.reloadTreeview()
  }

  private handleNewPageRequest(eventType: number) {
    const parent = this;
    const promiseForTaskStatusValList = this.fetchAndUpdateTaskStatusValList();
    const promiseForUserAndQueueDetails = this.fetchAndUpdateUserAndQueueDetails();

    Promise.all([promiseForTaskStatusValList, promiseForUserAndQueueDetails]).then(function () {
      if (+eventType === parent.EVENT_TYPE_ON_INIT) {
        parent.updateNewQueueCdeFromUrl();
      } else if (+eventType === parent.EVENT_TYPE_DOC_SUBSCRIPTION) {
        // Do nothing
      }
      // Do below steps for all scenarioes
      parent.validateNewQueueCde();
      parent.fetchAndUpdateIndividualTaskStatusCount().then(function () {
        parent.reloadTreeview();
      });
    });
  }


  private updateQueueCount(selectedItemId: string) {
    const parent = this;

    const treeItemResult: TreeItem = parent.TreeViewUtil.searchById(this.model.treeItems, selectedItemId)
    console.log('searchById', treeItemResult)
    // Queue name code is present only at the parent of a leaf node
    if (treeItemResult && treeItemResult.cde != '' && treeItemResult.items != undefined) {
      parent.selectedQueueNameCde = Number(treeItemResult.cde)
    }

    // console.log("updateQueueCount", this.modelTreeItems)
    // console.log("this.selectedQueueNameCde = ", this.selectedQueueNameCde);
    if (this.selectedQueueNameCde > 0) {
      parent.fetchAndUpdateIndividualTaskStatusCount().then(function () {
        parent.reloadTreeview();
      });
    }
  }

  private reloadTreeview() {
    // console.log("reloadTreeview", "entering")
    const parent = this;
    let queueNameSplitDelimiter = parent.QueueMenuProcess.QUEUE_NAME_SPLIT_DELIMITER_INVALID
    if (parent.model.selectedQueueModeCde === parent.QueueMenuProcess.QUEUE_MODE_CDE_HIERARCHY) {
      queueNameSplitDelimiter = parent.configDataHelper.getValue('sidebar.queueModeHierarchyDelimiter');
    }

    parent.updateNewQueueCdeFromUrl();
    const promiseForTaskStatusValList = this.fetchAndUpdateTaskStatusValList();
    const promiseForUserAndQueueDetails = this.fetchAndUpdateUserAndQueueDetails();

    const localSessionData: LocalSessionData = parent.localSessionService.getLocalSessionData();
    parent.model.selectedQueueModeCde = this.QueueMenuProcess.getQueueMode(
      parent.model, localSessionData)

    Promise.all([promiseForTaskStatusValList, promiseForUserAndQueueDetails]).then(function () {
      let hasConfigAccess: boolean;
      let hasQueueAccess: boolean;
      let hasClosedQueueAccess: boolean;
      const treeItemsNew: TreeItem[] = [];
      let treeItemI: TreeItem;
      let treeItemJ: TreeItem;
      let treeItemK: TreeItem;

      hasQueueAccess = parent.getFeature(parent.bmodel.FID.QUEUE_LIST).isEnabled;
      hasClosedQueueAccess = parent.getFeature(parent.bmodel.FID.QUEUE_USER_VIEW).isEnabled;
      // Check only for visibility here. Enabled check will be done inside the page
      hasConfigAccess = (parent.getFeature(parent.bmodel.FID.USER_LIST).isVisible ||
        parent.getFeature(parent.bmodel.FID.USER_VIEW).isVisible ||
        parent.getFeature(parent.bmodel.FID.USER_EDIT).isVisible ||
        parent.getFeature(parent.bmodel.FID.USER_DELETE).isVisible ||
        parent.getFeature(parent.bmodel.FID.RBAC_VIEW).isVisible ||
        parent.getFeature(parent.bmodel.FID.RBAC_EDIT).isVisible
      );
      /** First Row **/
      treeItemK = { id: '01', label: 'Dashboard', isVisible: true, url: 'home/dashboard' };
      treeItemsNew.push(treeItemK);
      /** First Row **/
      if (hasConfigAccess) {
        /** Second Row **/
        treeItemI = { id: '02', label: 'Configuration', isVisible: true, items: [], url: 'home/configuration' };
        treeItemJ = { id: '02.01', label: 'Manage Users', url: 'home/configuration/userlist' };
        treeItemI.items.push(treeItemJ);
        treeItemJ = { id: '02.02', label: 'Manage RBAC', url: 'home/configuration/rbac' };
        treeItemI.items.push(treeItemJ);
        treeItemJ = { id: '02.03', label: 'Manage All Queues', url: 'home/configuration/queuelist' };
        treeItemI.items.push(treeItemJ);
        treeItemsNew.push(treeItemI);
        /** Second Row **/
      }
      if (hasQueueAccess) {
        /** Third Row **/
        if (parent.userData.queueDataList != null && parent.userData.queueDataList.length > 0) {
          let userQueueDataList: QueueData[] = parent.userData.queueDataList.filter(x =>
            x.queueStatus == 'OPEN' || x.queueStatus == 'SCHEDULED')

          // Uncomment for testing queue in different order
          // parent.userData.queueDataList.sort((a,b)=> {
          //   return b.queueNameTxt.length - a.queueNameTxt.length});
          treeItemI = parent.QueueMenuProcess.createQueueTreeItem(userQueueDataList,
            parent.QueueMenuProcess.QUEUE_TYPE_OPEN, queueNameSplitDelimiter,
            parent.queueDocStatusMap, parent.taskStatusCdeList, parent.subQueuesList);

          treeItemsNew.push(treeItemI);
        }
        /** Third Row **/
      }
      if (hasClosedQueueAccess) {
        /** Fourth Row **/
        if (parent.userData.queueDataList != null && parent.userData.queueDataList.length > 0) {
          let userClosedQueueDataList: QueueData[] = parent.userData.queueDataList.filter(x =>
            x.queueStatus == 'CLOSED')
          treeItemI = parent.QueueMenuProcess.createQueueTreeItem(userClosedQueueDataList,
            parent.QueueMenuProcess.QUEUE_TYPE_CLOSED, queueNameSplitDelimiter,
            parent.queueDocStatusMap, parent.taskStatusCdeList, parent.subQueuesList);

          treeItemsNew.push(treeItemI);
        }
        /** Fourth Row **/
      }

      if (!parent.navigationTriggeredFromSidebar) {
        // If this method is not called on side bar item click by user, then we need to find out the
        // selected item id programmatically and send this information to the nia-tree-view component
        // by updating the model property which is bound to nia-tree-view component's input property
        // Get selected item id
        const selectedItemId = parent.getTreeItemId(treeItemsNew);

        // Update model if selectedItemId is different to propogate to treeview component
        if (selectedItemId !== parent.model.selectedItemId) {
          parent.model.selectedItemId = selectedItemId;
        }
      }

      // Reset treeview ONLY after selectedItemId has been determined first
      parent.model.treeItems = treeItemsNew;
      // console.log("reloadTreeview", "exiting", parent.modelTreeItems)

      // Reset value to false;
      parent.navigationTriggeredFromSidebar = false;
    });
  }

  private updateNewQueueCdeFromUrl() {
    const parent = this;
    let pageUrl = window.location.hash.split('?')[0];
    if (pageUrl) {
      pageUrl = pageUrl.replace('#/', '');
    }
    if (pageUrl.startsWith('home/worklist/')) {
      const tokens = pageUrl.split('/');
      // pattern is worklist/:queueNameCde
      if (tokens.length === 3) {
        parent.selectedQueueNameCde = +tokens[2];
        parent.selectedSubQueueNameCde = parent.UNSET_INTEGER_VALUE;
      } else if (tokens.length === 4) { // pattern is worklist/:queueNameCde/:docStatusCde
        parent.selectedQueueNameCde = +tokens[2];
        parent.selectedSubQueueNameCde = +tokens[3];
      }
    } else if (pageUrl.startsWith('home/myqueues')) {
      parent.selectedQueueNameCde = parent.UNSET_INTEGER_VALUE;
      parent.selectedSubQueueNameCde = parent.UNSET_INTEGER_VALUE;
    }
    else if (pageUrl.startsWith('home/myclosedqueues')) {
      parent.selectedQueueNameCde = parent.UNSET_INTEGER_VALUE;
      parent.selectedSubQueueNameCde = parent.UNSET_INTEGER_VALUE;
    }
  }

  private getTreeItemId(treeItems: TreeItem[]) {
    const parent = this;
    // The core logic in this method is to get the current URL (after navigation has completed)
    // and decide what item on treeview should be shown as selected
    // From the URL, it should be possible to do this in all scenarios
    // except workdata/:queueNameCde/:documentId because taskStatusCde is not part of URL
    let pageUrl = window.location.hash.split('?')[0];
    if (pageUrl) {
      pageUrl = pageUrl.replace('#/', '');
    }
    let isQueueNavigation = false;
    if (pageUrl.startsWith('home/worklist/') || pageUrl.startsWith('home/workdata/')
      || pageUrl.startsWith('home/epworkdata/')
      || pageUrl.startsWith('home/myqueues')
      || pageUrl.startsWith('home/myclosedqueues')) {
      isQueueNavigation = true;
    }
    let treeItemResult: TreeItem = null;

    if (isQueueNavigation) {
      if (treeItems) {
        let bestMatchId: string = '';
        let bestMatchId2: string = '';
        treeItemResult = parent.TreeViewUtil.searchByLabel(treeItems,
          parent.QueueMenuProcess.MENU_LABEL_MY_QUEUES)

        if (treeItemResult) {
          bestMatchId = treeItemResult.id
          treeItemResult = parent.TreeViewUtil.searchByCde(treeItemResult.items,
            String(parent.selectedQueueNameCde))
          if (treeItemResult) {
            bestMatchId = treeItemResult.id
            treeItemResult = parent.TreeViewUtil.searchByCde(treeItemResult.items,
              String(parent.selectedSubQueueNameCde))
            if (treeItemResult) {
              bestMatchId = treeItemResult.id
            }
          }
        }
        treeItemResult = parent.TreeViewUtil.searchByLabel(treeItems,
          parent.QueueMenuProcess.MENU_LABEL_MY_QUEUES_CLOSED)

        if (treeItemResult) {
          bestMatchId2 = treeItemResult.id
          treeItemResult = parent.TreeViewUtil.searchByCde(treeItemResult.items,
            String(parent.selectedQueueNameCde))
          if (treeItemResult) {
            bestMatchId2 = treeItemResult.id
            treeItemResult = parent.TreeViewUtil.searchByCde(treeItemResult.items,
              String(parent.selectedSubQueueNameCde))
            if (treeItemResult) {
              bestMatchId2 = treeItemResult.id
            }
          }
        }
        return bestMatchId2.length > bestMatchId.length ? bestMatchId2 : bestMatchId

      }
    } else {
      if (treeItems) {
        treeItemResult = parent.TreeViewUtil.searchByUrl(treeItems, pageUrl)
        if (treeItemResult) {
          return treeItemResult.id
        }
      }
    }
    return '';
  }

  private validateNewQueueCde() {
    const parent = this;
    let isValid = false;
    // First, validate selectedQueueNameCde
    if (+parent.selectedQueueNameCde !== parent.UNSET_INTEGER_VALUE) {
      for (let i = 0; i < parent.userData.queueDataList.length; i++) {
        if (parent.userData.queueDataList[i].queueNameCde === +parent.selectedQueueNameCde) {
          isValid = true;
          break;
        }
      }
      if (!isValid) {
        parent.selectedQueueNameCde = parent.UNSET_INTEGER_VALUE;
      }
    }
    // Second, validate subQueueNameCde
    if (+parent.selectedSubQueueNameCde !== parent.UNSET_INTEGER_VALUE) {
      if (parent.taskStatusCdeList.filter(a => a === +parent.selectedSubQueueNameCde).length === 0) {
        parent.selectedSubQueueNameCde = parent.UNSET_INTEGER_VALUE;
      }
    }
  }

  private fetchAndUpdateTaskStatusValList() {
    const parent = this;
    return new Promise(function (fulfilled, rejected) {
      if (parent.taskStatusCdeList.length > 0) {
        fulfilled(null);
        return;
      }
      // If any pending calls, then unsubscribe first
      if (parent.valServiceSubscription) {
        parent.valServiceSubscription.unsubscribe();
      }
      parent.valServiceSubscription = parent.valService.getValList('task-status', function (error, data) {
        if (!error) {
          parent.taskStatusCdeList = []; // reset variable
          let taskStatusList = data as ValData[];
          taskStatusList = taskStatusList.filter(a => a.cde > EnumTaskStatus.UNDEFINED && a.cde < EnumTaskStatus.FAILED);

          for (let i = 0; i < taskStatusList.length; i++) {
            if (taskStatusList[i].cde === EnumTaskStatus.YET_TO_START) {
              parent.taskStatusCdeList.push(taskStatusList[i].cde);
              parent.subQueuesList.push('New');
            } else if (taskStatusList[i].cde !== EnumTaskStatus.ON_HOLD && taskStatusList[i].cde !== EnumTaskStatus.RETRY_LATER) {
              parent.taskStatusCdeList.push(taskStatusList[i].cde);
              parent.subQueuesList.push(taskStatusList[i].txt);
            }
          }
          fulfilled(null);
        } else {
          rejected();
        }
      });

    });
  }

  private fetchAndUpdateUserAndQueueDetails() {
    const parent = this;
    return new Promise(function (fulfilled, rejected) {

      parent.sessionService.getLoggedInUserDetailsPromise()
        .then(function (data) {
          parent.userData = data as UserData;
          fulfilled(null);
        })
        .catch(function (error) {
          rejected();
        });
    });
  }

  private fetchAndUpdateIndividualTaskStatusCount() {
    const parent = this;
    // As this method has async call, capture value locally
    const selectedQueueNameCde = parent.selectedQueueNameCde;
    return new Promise(function (fulfilled, rejected) {
      if (+selectedQueueNameCde === parent.UNSET_INTEGER_VALUE) {
        fulfilled(null);
        return;
      }
      // If any pending calls, then unsubscribe first
      if (parent.docCountSubscription) {
        parent.docCountSubscription.unsubscribe();
      }

      parent.docCountSubscription = parent.documentService.getDocCount(null, selectedQueueNameCde, null, function (error, data) {
        if (!error) {
          const docCountList: DocCountData[] = data;
          const docCount = Array(parent.taskStatusCdeList.length).fill(0);
          if (docCountList) {
            for (let i = 0; i < parent.taskStatusCdeList.length; i++) {
              docCountList.forEach(function (a) {
                if (a.taskStatusCde === parent.taskStatusCdeList[i]) {
                  docCount[i] = a.docCount;
                }
              });
            }
          }
          // Store docstatuscount into a map per queue so that old values are retained
          // when user clicks on a previous queue instead of current queue count values
          // The retained old values will get updated as new count is fetched from DB
          parent.queueDocStatusMap.set(String(selectedQueueNameCde), docCount);
          fulfilled(null);
          // console.log("getDocCount", "Exiting", docCount);
        } else {
          rejected();
        }
      });
    });
  }

}

