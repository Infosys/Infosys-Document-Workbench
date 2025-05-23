/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { APP_INITIALIZER, Injectable } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { ToastrModule } from 'ngx-toastr';
import { AvatarModule } from 'ngx-avatar';

import { AppRoutingModule } from './app-routing.module';

import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { HttpClientBase } from './service/http/http-client-base';

import { AppComponent } from './app.component';
import { DocListComponent } from './component/doc-list/doc-list.component';
import { ExtractedDataComponent } from './component/extracted-data/extracted-data.component';
import { ActionPanelComponent } from './component/action-panel/action-panel.component';
import { MenuComponent } from './component/menu/menu.component';
import { DocumentService } from './service/document.service';
import { ActionService } from './service/action.service';
import { ProductService } from './service/product.service';
import { DataService } from './service/data.service';
import { HttpClientModule } from '@angular/common/http';
import { DatePipe } from '@angular/common';
import { ActionPerformComponent } from './component/action-perform/action-perform.component';
import { ActionListComponent } from './component/action-list/action-list.component';
import { EmailOutboundComposeComponent } from './component/email-outbound-compose/email-outbound-compose.component';
import { EmailOutboundListComponent } from './component/email-outbound-list/email-outbound-list.component';
import { FooterComponent } from './component/footer/footer.component';
import { AttachmentService } from './service/attachment.service';
import { RecommendedActionService } from './service/recommended-action.service';
import { EmailOutboundContentComponent } from './component/email-outbound-content/email-outbound-content.component';
import { ActionRecommendedComponent } from './component/action-recommended/action-recommended.component';
import { GenericComponent } from './page/generic/generic.component';
import { UserListComponent } from './component/user-list/user-list.component';
import { AdminService } from './service/admin.service';
import { CaseAssignComponent } from './component/case-assign/case-assign.component';
import { WorkListComponent } from './page/work-list/work-list.component';
import { WorkDataComponent } from './page/work-data/work-data.component';
import { HeaderComponent } from './component/header/header.component';
import { AuthGuard } from './auth/auth.guard';
import { LoginAuthGuard } from './auth/login-auth.guard';
import { SessionService } from './service/session.service';
import { LocalSessionService } from './service/local-session.service';
import { ErrorPageComponent } from './component/error-page/error-page.component';
import { InvalidPageComponent } from './component/invalid-page/invalid-page.component';
import { NiaTreeViewComponent } from './component/nia-tree-view/nia-tree-view.component';
import { UtilityService } from './service/utility.service';
import { PagerService } from './service/pager.service';
import { MessageInfo } from './utils/message-info';
import { ConfigDataHelper } from './utils/config-data-helper';
import { BreadcrumbComponent } from './component/breadcrumb/breadcrumb.component';
import { NiaFileAttachmentComponent } from './component/nia-file-attachment/nia-file-attachment.component';

import { QuillModule } from 'ngx-quill';
import { EmailService } from './service/email.service';
import { AttributeService } from './service/attribute.service';
import { TemplateService } from './service/template.service';
import { ValService } from './service/val.service';
import { AuditComponent } from './component/audit/audit.component';
import { AuditService } from './service/audit.service';
import { CasePanelComponent } from './component/case-panel/case-panel.component';
import { SessionTimeoutComponent } from './component/session-timeout/session-timeout.component';
import { LoginComponent } from './page/login/login.component';
import { AuthenticationService } from './service/authentication.service';
import { HomeComponent } from './page/home/home.component';
import { LogoutComponent } from './page/logout/logout.component';
import { ProfileComponent } from './page/profile/profile.component';
import { ProfileService } from './service/profile.service';
import { RegistrationComponent } from './page/registration/registration.component';
import { RegistrationService } from './service/registration.service';
import { ManagePasswordComponent } from './component/manage-password/manage-password.component';
import { FileContentComponent } from './component/file-content/file-content.component';
import { EmptyComponent } from './page/empty/empty.component';
import { ReExtractReviewComponent } from './component/re-extract-review/re-extract-review.component';
import { ReExtractConfirmComponent } from './component/re-extract-confirm/re-extract-confirm.component';
import { AttributeHelper } from './utils/attribute-helper';
import { NiaDocumentAnnotatorComponent } from './component/nia-document-annotator/nia-document-annotator.component';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { ExtractedTabularDataComponent } from './component/extracted-tabular-data/extracted-tabular-data.component';
import { JsonTabularDataComponent } from './component/json-tabular-data/json-tabular-data.component';
import { NgxJsonViewerModule } from 'ngx-json-viewer';
import { NgxExtendedPdfViewerModule } from 'ngx-extended-pdf-viewer';
import { NiaAnnotatorUtil } from './component/nia-document-annotator/nia-annotator-util';
import { NiaSortableColumnService } from './component/nia-sortable-column/nia-sortable-column.service';
import { NiaSortableColumnComponent } from './component/nia-sortable-column/nia-sortable-column.component';
import { AnnotationService } from './component/extracted-data/annotation.service';
import { ExtractedDataHelper } from './component/extracted-data/extracted-data-helper';
import { ReExtractionService } from './component/extracted-data/re-extract.service';
import { MatTabsModule } from '@angular/material/tabs';
import { MatCardModule } from "@angular/material/card";
import { MatButtonToggleModule } from '@angular/material/button-toggle';
// REF for material icon - https://www.angularjswiki.com/angular/angular-material-icons-list-mat-icon-list/
// https://fonts.google.com/icons?selected=Material+Icons
import { MatIconModule } from '@angular/material/icon';
import { MatExpansionModule } from '@angular/material/expansion';
import { BaseComponent } from './base.component';
import { RbacListComponent } from './component/rbac-list/rbac-list.component';
import { NgJsonEditorModule } from 'ang-jsoneditor'
import { AppVariableService } from './service/app.variable.service';
import { NiaAuthCheckDirective } from './directive/nia-auth-check.directive';
import { ExtractionPathComponent } from './component/extraction-path/extraction-path.component';
import { ElasticsearchService } from './service/elasticsearch.service';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatInputModule } from '@angular/material/input';
import { MatNativeDateModule } from '@angular/material/core';
import { NativeDateAdapter, DateAdapter, MAT_DATE_FORMATS } from '@angular/material/core';
import { MatRadioModule } from '@angular/material/radio';
import { formatDate } from '@angular/common';
import { NiaTelemetryService } from './service/nia-telemetry.service';

import { TelemetryService } from '@infy-docwb/infy-telemetry-sdk';
import { QueueListComponent } from './component/queue-list/queue-list.component';
import { DashboardComponent } from './component/dashboard/dashboard.component';
import { MatTableModule } from '@angular/material/table';
import { MatSortModule } from '@angular/material/sort';
import {TreeTableModule} from 'primeng/treetable';
import { FilterService } from 'primeng/api';
import { MatPaginatorModule } from '@angular/material/paginator';
import { CaseNavigatorComponent } from './component/case-navigator/case-navigator.component';
import { QueAndAnsComponent } from './component/que-and-ans/que-and-ans.component';
import {QueAndAnsService} from './service/que-and-ans.service';
import { LlmDetailComponent } from './component/llm-detail/llm-detail.component';
import { ClipboardModule } from '@angular/cdk/clipboard';
import { MatSliderModule } from '@angular/material/slider';
import { FeedbackPopoverComponent } from './component/feedback-popover/feedback-popover.component';
import { OverlayModule } from '@angular/cdk/overlay';

export const PICK_FORMATS = {
  parse: {dateInput: {month: 'short', year: 'numeric', day: 'numeric'}},
  display: {
      dateInput: 'input',
      monthYearLabel: {year: 'numeric', month: 'short'},
      dateA11yLabel: {year: 'numeric', month: 'long', day: 'numeric'},
      monthYearA11yLabel: {year: 'numeric', month: 'long'}
  }
};

@Injectable()
export class PickDateAdapter extends NativeDateAdapter {
  format(date: Date, displayFormat: Object): string {
      if (displayFormat === 'input') {
          return formatDate(date,'dd-MMM-yyyy',this.locale);;
      } else {
          return date.toDateString();
      }
  }
}

export function initConfig(configDataHelper: ConfigDataHelper, msgInfo: MessageInfo) {
  return () => {
    return configDataHelper.load().then(function (data) {
      msgInfo.load();
    });
  };
}

@NgModule({
  declarations: [
    AppComponent,
    DocListComponent,
    ExtractedDataComponent,
    ActionPanelComponent,
    MenuComponent,
    ActionPerformComponent,
    ActionListComponent,
    EmailOutboundComposeComponent,
    EmailOutboundListComponent,
    FooterComponent,
    EmailOutboundContentComponent,
    ActionRecommendedComponent,
    GenericComponent,
    UserListComponent,
    CaseAssignComponent,
    WorkListComponent,
    WorkDataComponent,
    HeaderComponent,
    NiaTreeViewComponent,
    ErrorPageComponent,
    InvalidPageComponent,
    BreadcrumbComponent,
    NiaFileAttachmentComponent,
    AuditComponent,
    CasePanelComponent,
    SessionTimeoutComponent,
    LoginComponent,
    HomeComponent,
    LogoutComponent,
    ProfileComponent,
    RegistrationComponent,
    ManagePasswordComponent,
    FileContentComponent,
    EmptyComponent,
    ReExtractReviewComponent,
    ReExtractConfirmComponent,
    ExtractedTabularDataComponent,
    JsonTabularDataComponent,
    NiaDocumentAnnotatorComponent,
    NiaSortableColumnComponent,
    // commenting abstract class based on https://stackoverflow.com/questions/67418127/cannot-assign-an-abstract-constructor-type-to-a-non-abstract-constructor-type
    // BaseComponent,
    RbacListComponent,
    NiaAuthCheckDirective,
    ExtractionPathComponent,
    QueueListComponent,
    DashboardComponent,
    CaseNavigatorComponent,
    QueAndAnsComponent,
    LlmDetailComponent,
    FeedbackPopoverComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    MatDatepickerModule,
    MatInputModule,
    MatNativeDateModule,
    MatRadioModule,
    MatPaginatorModule,
    ToastrModule.forRoot({
      timeOut: 3000,
      positionClass: 'toast-center-center',
      preventDuplicates: true,
    }),
    AvatarModule,
    NgbModule,
    HttpClientModule,
    FormsModule,
    /**
     * TODO : warning thrown in console like ngModel used on the same form field as formControl.
     * it has been deprecated in Angular v6 and will be removed in Angular v7.
     * This error because of both ngModel and formControl used together on Quill Editor.
     * So we have to fix it, and for the time being have add warrning never property to suppress the warning .
     */
    ReactiveFormsModule.withConfig({ warnOnNgModelWithFormControl: 'never' }),
    QuillModule.forRoot(),
    NgxJsonViewerModule,
    NgxExtendedPdfViewerModule,
    MatSlideToggleModule,
    MatTabsModule,
    MatCardModule,
    MatButtonToggleModule,
    MatIconModule,
    MatExpansionModule,
    NgJsonEditorModule,
    MatTableModule,
    MatSortModule,
    TreeTableModule,
    ClipboardModule,
    MatSliderModule,
    OverlayModule,
  ],
  providers: [
    {
      provide: APP_INITIALIZER,
      useFactory: initConfig,
      deps: [ConfigDataHelper, MessageInfo],
      multi: true
    },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: HttpClientBase,
      multi: true
    },
    {provide: DateAdapter, useClass: PickDateAdapter},
    {provide: MAT_DATE_FORMATS, useValue: PICK_FORMATS},
    DatePipe,
    DocumentService,
    ActionService,
    ProductService,
    DataService,
    AttachmentService,
    RecommendedActionService,
    AdminService,
    AuthGuard,
    LoginAuthGuard,
    SessionService,
    NiaTelemetryService,
    TelemetryService,
    LocalSessionService,
    UtilityService,
    PagerService,
    MessageInfo,
    ConfigDataHelper,
    EmailService,
    AttributeService,
    ElasticsearchService,
    TemplateService,
    ValService,
    AuditService,
    AuthenticationService,
    ProfileService,
    RegistrationService,
    AttributeHelper,
    NiaAnnotatorUtil,
    NiaSortableColumnService,
    AnnotationService,
    ExtractedDataHelper,
    ReExtractionService,
    AppVariableService,
    FilterService,
    QueAndAnsService
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
