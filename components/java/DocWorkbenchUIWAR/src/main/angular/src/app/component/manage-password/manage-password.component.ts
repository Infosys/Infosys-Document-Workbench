/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Component, OnInit } from '@angular/core';
import { NgForm } from '@angular/forms';
import { ProfileService } from '../../service/profile.service';
import { ToastrService } from 'ngx-toastr';
import { MessageInfo } from '../../utils/message-info';
import { SessionService } from '../../service/session.service';
import { ConfigDataHelper } from '../../utils/config-data-helper';
import { BaseComponent } from '../../base.component';
import { NiaTelemetryService } from '../../service/nia-telemetry.service';

@Component({
  selector: 'app-manage-password',
  templateUrl: './manage-password.component.html',
  styleUrls: ['./manage-password.component.scss']
})
export class ManagePasswordComponent extends BaseComponent implements OnInit {
  getClassName(): string {
    return "ManagePasswordComponent";
  }
  model: any = {
    isDataLoaded: Boolean,
    oldPasswrd: "",
    confirmNewPasswrd: "",
    newPasswrd: "",
  } // For binding to view

  constructor(private profileService: ProfileService,private msgInfo: MessageInfo,
    private toastr: ToastrService, public sessionService: SessionService, 
    public configDataHelper: ConfigDataHelper, public niaTelemetryService:NiaTelemetryService) {
    super(sessionService, configDataHelper, niaTelemetryService);
  }

ngOnInit() {
  this.model.isDataLoaded = true;
}

changePassword() {
  let parent = this;
  if (parent.model.newPasswrd == parent.model.confirmNewPasswrd) {
    let promise = parent.profileService.changePassword(parent.model.oldPasswrd, parent.model.newPasswrd);
    return promise.then(result => {
      console.log(result);
      if (parent.msgInfo.getMessage(133) == result['response'])
        parent.toastr.error(parent.msgInfo.getMessage(133));
      else if (parent.msgInfo.getMessage(134) == result['response'])
        parent.toastr.error(parent.msgInfo.getMessage(134));
      else if (parent.msgInfo.getMessage(135) == result['response'])
        parent.toastr.error(parent.msgInfo.getMessage(135));
      else if (parent.msgInfo.getMessage(181) == result['response'])
        parent.toastr.error(parent.msgInfo.getMessage(181));
      else
        parent.toastr.success(parent.msgInfo.getMessage(137));
    }).catch(error => {
      parent.toastr.error(error);
    });
  } else {
    parent.toastr.error(parent.msgInfo.getMessage(136));
  }
}
}
