/** =============================================================================================================== *
 * Copyright 2023 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Component, OnInit } from '@angular/core';
import { DocumentService } from '../../service/document.service';
import { DocumentData } from '../../data/document-data';
import { ToastrService } from 'ngx-toastr';
import { ActivatedRoute, Router } from '@angular/router';
import { MessageInfo } from '../../utils/message-info';
import { UtilityService } from '../../service/utility.service';
import { DataService } from '../../service/data.service';
import { SessionService } from '../../service/session.service';
import { ConfigDataHelper } from '../../utils/config-data-helper';
import { NiaTelemetryService } from '../../service/nia-telemetry.service';
import { BaseComponent } from '../../base.component';

@Component({
  selector: 'app-case-navigator',
  templateUrl: './case-navigator.component.html',
  styleUrls: ['./case-navigator.component.scss']
})
export class CaseNavigatorComponent extends BaseComponent implements OnInit {
  getClassName(): string {
    return "CaseNavigatorComponent";
  }
  model: any = {
    queueNameCde: Number,
    selTabNum: Number
  };

  private documentData: DocumentData;
  private docId: number;
  private splittedURL: string[];

  constructor(private dataService: DataService,
    private documentService: DocumentService, private utilityService: UtilityService,
    private router: Router, private route: ActivatedRoute, public sessionService: SessionService,
    public configDataHelper: ConfigDataHelper, public niaTelemetryService:NiaTelemetryService,
    private msgInfo: MessageInfo,private toastr: ToastrService) {
    super(sessionService, configDataHelper, niaTelemetryService);
    const parent = this;
    parent.route.url.subscribe(activeUrl => { // for catching any URL changes and extracting it
      const url: string = window.location.hash;
      parent.splittedURL = url.split('/');
      parent.route.queryParams
          .subscribe(params => {
            parent.model.selTabNum = params.view;
            console.log(params.view);
            if(parent.model.selTabNum == undefined){
              if(parent.splittedURL[2] == 'workdata'){
                parent.model.selTabNum = 0
              }

            }
          }
        );
    });
  }



  ngOnInit(){
  }


  navigateCase(nextOrPrevCase){
    const parent = this;
    const urlSplitList = parent.splittedURL
    if(urlSplitList[2] == 'workdata'){
      var queueNameCde = +urlSplitList[3]
      var docId = +urlSplitList[4].split('?')[0]
    }

    if(nextOrPrevCase == 'Next'){
      var sortOrder = 'DESC';
      var searchCriteria = 'case:<' + docId;
    }
    else{
      var sortOrder = 'ASC';
      var searchCriteria = 'case:>' + docId;
    }
    if (docId > 0 && queueNameCde > 0) {
      const promise = parent.documentService.getDocumentData(queueNameCde, docId);
      promise.then(function (data) {
        if (parent.utilityService.isListHasValue(data)) {
          parent.documentData = data[0];
        }
        let taskStatusCde = parent.documentData['taskStatusCde'];
        let taskStatusOperator = '=';
        let appUserId;
        let nextId = [];
        if(parent.model.selTabNum == 0)
          appUserId = -2
        else if(parent.model.selTabNum == 1)
          appUserId = -1
        else
          appUserId = parent.documentData.appUserId

        parent.documentService.getNextOrPrevId(appUserId, queueNameCde, taskStatusCde,
          taskStatusOperator,sortOrder,searchCriteria, function (error, data) {
          if(data){
            if(!error && data[0] !== undefined){
              const nextId: DocumentData[] = data['response']= data;
              data=nextId;
            }
            if (data[0] !== undefined){
              nextId = data[0].docId;
              parent.router.navigate(["/home/workdata/" + queueNameCde + "/" + nextId],{
                  queryParams: {
                    view: parent.model.selTabNum
                  },
                  //queryParamsHandling: 'merge',
                });
              }
            else{
              if(nextOrPrevCase == 'Next')
                parent.toastr.error(parent.msgInfo.getMessage(185));
              else
                parent.toastr.error(parent.msgInfo.getMessage(186));
            }
            return nextId;
          }
        });
      }).catch(function (error) {
        console.log(error);
      });

    }
  }

}
