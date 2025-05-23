/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-session-timeout',
  templateUrl: './session-timeout.component.html',
  styleUrls: ['./session-timeout.component.scss']
})
export class SessionTimeoutComponent implements OnInit {

  constructor(private router: Router) { }

  returnUrl: string;

  ngOnInit() {
    let tokens: string[] = this.router.url.split("returnUrl=");
    if (tokens.length == 2) {
      this.returnUrl = decodeURIComponent(tokens[1]);
    }
  }
}
