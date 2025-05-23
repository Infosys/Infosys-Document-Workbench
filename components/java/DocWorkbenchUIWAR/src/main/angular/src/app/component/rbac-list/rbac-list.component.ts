/** =============================================================================================================== *
 * Copyright 2022 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Component, ViewChild, Input, ElementRef , AfterContentInit, HostListener} from '@angular/core';
import { ModalDismissReasons, NgbModal, NgbModalOptions } from '@ng-bootstrap/ng-bootstrap';
import { JsonEditorComponent, JsonEditorOptions } from 'ang-jsoneditor';
import { ToastrService } from 'ngx-toastr';
import { MessageInfo } from '../../utils/message-info';
import { AuditData } from '../../data/audit-data';
import { AppVariableData } from '../../data/app-variable-data';
import { AppVariableService } from '../../service/app.variable.service';
import { CONSTANTS } from '../../common/constants';
import { BaseComponent } from '../../base.component';
import { SessionService } from '../../service/session.service';
import { ConfigDataHelper } from '../../utils/config-data-helper';
import { NiaTelemetryService } from '../../service/nia-telemetry.service';

@Component({
  selector: 'app-rbac-list',
  templateUrl: './rbac-list.component.html',
  styleUrls: ['./rbac-list.component.scss']
})

export class RbacListComponent extends BaseComponent implements AfterContentInit {
  getClassName(): string {
    return "RbacListComponent";
  }
  // For disabling mouse click and keystrokes outside modal window
  ngbModalOptions: NgbModalOptions = {
    backdrop: 'static',
    keyboard: false
  };
  public model: any = {
    wrapperHeightInPx: Number,
    conenteHeightInPx: Number,
    appVarValue:undefined,
    editorOptions: undefined,
    isDataLoaded: false,
    auditData: new AuditData(0, 0, 0, '', '', CONSTANTS.APP_VARIABLE_KEYS.RBAC)
  };

  @Input() maxAllowedHeightInPx: number;
  @Input() maxAllowedWidthInPx: number;

  private closeResult: string;
  private appVarData:AppVariableData;

  @ViewChild(JsonEditorComponent, { static: false }) editor: JsonEditorComponent;

  constructor(private appVariableService: AppVariableService, private modalService: NgbModal,private toastr: ToastrService,
    private msgInfo: MessageInfo, private elementRef: ElementRef, public sessionService: SessionService,
    public configDataHelper: ConfigDataHelper, public niaTelemetryService:NiaTelemetryService) {
    super(sessionService, configDataHelper, niaTelemetryService)
    this.model.editorOptions = new JsonEditorOptions()
    // this.model.editorOptions.modes = ['code', 'text', 'tree', 'view'];
    this.model.editorOptions.modes = ['text', 'tree'];
    this.model.editorOptions.expandAll=true;
  }



  ngOnInit() {
    this.refreshComponent();
  }

  ngAfterContentInit() {
    // This method will be called only once in component lifecycle, NOT called on page resize.
    this.updateProperties();
  }

  @HostListener('window:resize', ['$event'])
  onResize(event) {
    // This method will be called only on page resize and NOT on component load.
    this.updateProperties();
  }

  private updateProperties() {
    this.model.wrapperHeightInPx = this.maxAllowedHeightInPx;
    this.model.conenteHeightInPx = this.maxAllowedHeightInPx - 20; // Calculated using trial-error
    // This is the technique to update variables in SCSS
    this.elementRef.nativeElement.style.setProperty('--content-height', this.model.conenteHeightInPx + 'px')
  }

  public refreshComponent(){
    const parent = this;
    parent.appVariableService.getAppVariableDataPromise(CONSTANTS.APP_VARIABLE_KEYS.RBAC).then(
      (appVarData: AppVariableData) => {
      parent.model.isDataLoaded = true;
      parent.appVarData = appVarData
      parent.model.appVarValue = JSON.parse(appVarData.appVarValue);
    });
  }


  saveModal() {
    const parent = this;
    const changedJson = this.editor.get();
    const postReqData = {
      "prevAppVarId":parent.appVarData.appVarId,
      "appVarKey": parent.appVarData.appVarKey,
	    "appVarValue":JSON.stringify(changedJson)
    }
    parent.appVariableService.editAppVariablePromise(postReqData).then(data => {
      parent.refreshComponent();
      parent.toastr.success(parent.msgInfo.getMessage(178));
    }).catch(function (error) {
      parent.toastr.error(parent.msgInfo.getMessage(177));
    });
  }

  cancelModal() {
    this.refreshComponent();
  }

  open(content) {
    this.modalService.open(content, this.ngbModalOptions).result.then((result) => {
      this.closeResult = `Closed with: ${result}`;
    }, (reason) => {
      this.closeResult = `Dismissed ${this.getDismissReason(reason)}`;
    });
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


}

// ref
// https://github.com/mariohmol/ang-jsoneditor/blob/master/src/app/demo/demo.component.ts#L79
