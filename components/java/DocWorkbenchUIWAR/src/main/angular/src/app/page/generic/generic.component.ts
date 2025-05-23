/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Component, OnInit, HostListener } from '@angular/core';
import { Router, ActivatedRoute } from "@angular/router";
import { AdminService } from '../../service/admin.service';

@Component({
  selector: 'app-generic',
  templateUrl: './generic.component.html',
  styleUrls: ['./generic.component.scss']
})
// This is a generic component to be used as the default page component for containing all child component(s).
// It will calculate the max allowed height and pass that value to the child component(s) so that
// child component(s) can adjust their content to not exceed this value.
// Also, when the page resizes, the ajusted new value of max allowed height is calculated and passed to child
// component(s)
export class GenericComponent implements OnInit {

  private static HEIGHT_OFFSET = 185; // Calculated using trial-error
  private static WIDTH_OFFSET = 36; // Calculated using trial-error

  model: any = {
    isUserList: false,
    isRbacList: false,
    isPersonalQueueList: false,
    isAdminQueueList: false,
    childComponentMaxAllowedHeightInPx: Number,
    childComponentMaxAllowedWidthInPx: Number
  };

  constructor(private router: Router, private activatedRoute: ActivatedRoute,
    private adminService: AdminService) {
    let parent = this;
    const route = this.router.url; // E.g. /home/configuration/userlist
    const tokens: string[] = route.split("/")

    if (tokens.length >= 2) {
      const requestedPage = tokens[tokens.length - 1]
      if (requestedPage == 'userlist') {
        this.model.isUserList = true
      } else if (requestedPage == 'rbac') {
        this.model.isRbacList = true
      } else if (requestedPage == 'managequeue') {
        this.model.isPersonalQueueList = true
      }  
      else if (requestedPage == 'queuelist') {
           this.model.isAdminQueueList = true
         }
    }
    this.activatedRoute.params.subscribe(params => {
      console.log('GenericComponent -> ' + params)
    });

  }

  ngOnInit() {
    // console.log("Sized");
    this.resizeChildComponents();
  }

  @HostListener('window:resize', ['$event'])
  onResize(event) {
    // console.log("Resized", event.target.innerHeight );
    this.resizeChildComponents();
  }

  private resizeChildComponents() {
    this.model.childComponentMaxAllowedHeightInPx = window.innerHeight - GenericComponent.HEIGHT_OFFSET
    this.model.childComponentMaxAllowedWidthInPx = window.innerWidth - GenericComponent.WIDTH_OFFSET
    console.log('Generic Component: onResize',
      'w=', this.model.childComponentMaxAllowedWidthInPx,
      'h=', this.model.childComponentMaxAllowedHeightInPx
      )
  }

}
