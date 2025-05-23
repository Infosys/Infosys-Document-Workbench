/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Component, OnInit, ViewChild, ElementRef, HostListener, AfterViewInit } from '@angular/core';
import { CONSTANTS } from '../../common/constants';
import { BaseComponent } from '../../base.component';
import { SessionService } from '../../service/session.service';
import { ConfigDataHelper } from '../../utils/config-data-helper';
import { NiaTelemetryService } from '../../service/nia-telemetry.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent extends BaseComponent {
  getClassName(): string {
    return "HomeComponent";
  }
  private static WIDTH_CORRECTION_IN_PX = 8; // Calculated using trial-error

  constructor(public sessionService: SessionService, public configDataHelper: ConfigDataHelper, 
    public niaTelemetryService:NiaTelemetryService) {
    super(sessionService, configDataHelper, niaTelemetryService);
  }

  @ViewChild('mainContentArea', { static: false })
  mainContentArea: ElementRef;

  public model: any = {
    menuState: "block",
    cssSidebarClass: "col-md-2 no padding",
    cssMainContentClass: "col-md-10 no padding",
    isShowHeader: true,
    isShowFooter: true,
    isShowSidebar: true
  };

  ngOnInit() {
    // this.triggerTelemetryEvents(CONSTANTS.TELEMETRY_EVENT.START);
  }

  ngAfterViewInit(): void {
    // console.log("HomeComponent->ngAfterViewInit()")
    window.dispatchEvent(new Event('resize'));
    // this.triggerTelemetryEvents(CONSTANTS.TELEMETRY_EVENT.IMPRESSION);
  }

  // ngOnDestroy() {
  //   this.triggerTelemetryEvents(CONSTANTS.TELEMETRY_EVENT.END);
  // }

  showSidebarHandler(isShowSidebar: boolean) {
    // console.log("HomeComponent->showSidebarHandler()");
    if (isShowSidebar) {
      this.model.cssSidebarClass = "col-md-2 no padding";
      this.model.cssMainContentClass = "col-md-10 no padding";
      this.model.menuState = 'block';
    } else {
      this.model.cssSidebarClass = "";
      this.model.cssMainContentClass = "col-md-12 no padding";
      this.model.menuState = 'none';
    }

    // Wait for component to render before reading the changed properties values
    setTimeout(() => {
      console.log('Manually raising a resize event')
      window.dispatchEvent(new Event('resize'));
    }, 100);

  }

  @HostListener('window:resize', ['$event'])
  onResize(event) {
    // console.log('HomeComponent->OnResize()')
    this.captureScreenPropsInDom()
  }

  private captureScreenPropsInDom() {
    let element: HTMLElement = document.querySelector(':root') as HTMLElement;
    let mainContentWidth = this.mainContentArea.nativeElement.offsetWidth;
    const mainContentHeight = this.mainContentArea.nativeElement.offsetHeight;
    mainContentWidth-= HomeComponent.WIDTH_CORRECTION_IN_PX
    // console.log('[',mainContentWidth, ',' , mainContentHeight,']')
    // Store <div #mainContentArea> 's width and height as DOM property
    // Note: <div #mainContentArea> stores the main content of page excluding header, footer and sidebar
    element.style.setProperty('--docwb-main-content-width', String(mainContentWidth))
    element.style.setProperty('--docwb-main-content-height', String(mainContentHeight))

  }
}
