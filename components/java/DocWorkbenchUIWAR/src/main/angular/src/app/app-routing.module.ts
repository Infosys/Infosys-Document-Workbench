/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { GenericComponent } from './page/generic/generic.component';
import { WorkListComponent } from './page/work-list/work-list.component';
import { WorkDataComponent } from './page/work-data/work-data.component';
import { AuthGuard } from './auth/auth.guard';
import { LoginAuthGuard } from './auth/login-auth.guard';
import { ErrorPageComponent } from './component/error-page/error-page.component';
import { InvalidPageComponent } from './component/invalid-page/invalid-page.component';
import { SessionTimeoutComponent } from './component/session-timeout/session-timeout.component';
import { LoginComponent } from './page/login/login.component';
import { HomeComponent } from './page/home/home.component';
import { LogoutComponent } from './page/logout/logout.component';
import { ProfileComponent } from './page/profile/profile.component';
import { RegistrationComponent } from './page/registration/registration.component';
import { EmptyComponent } from './page/empty/empty.component';
import { DashboardComponent } from './component/dashboard/dashboard.component';


const routes: Routes = [
  { path: 'login', component: LoginComponent, canActivate: [LoginAuthGuard] },
  { path: 'login/:tenantId', component: LoginComponent, canActivate: [LoginAuthGuard] },
  { path: 'logout', component: LogoutComponent },
  { path: 'registration', component: RegistrationComponent },
  { path: 'registration/:tenantId', component: RegistrationComponent },
  {
    path: 'home', component: HomeComponent, canActivate: [AuthGuard],
    children: [
      {path: 'dashboard',component: DashboardComponent, canActivate: [AuthGuard]},
      {
        path: 'configuration', canActivate: [AuthGuard],
        children: [
          { path: '', component: EmptyComponent, canActivate: [AuthGuard] },
          { path: 'userlist', component: GenericComponent, canActivate: [AuthGuard] },
          { path: 'rbac', component: GenericComponent, canActivate: [AuthGuard] },
          { path: 'queuelist', component: GenericComponent, canActivate: [AuthGuard] },
        ]
      },
      { path: 'myqueues', component: EmptyComponent, canActivate: [AuthGuard] },
      { path: 'worklist/:queueNameCde', component: EmptyComponent, canActivate: [AuthGuard] },
      { path: 'worklist/:queueNameCde/:docStatusCde', component: WorkListComponent, canActivate: [AuthGuard] },
      { path: 'workdata/:queueNameCde/:documentId', component: WorkDataComponent, canActivate: [AuthGuard] },

      { path: 'my/profile', component: ProfileComponent, canActivate: [AuthGuard] },
      { path: 'workdata/:queueNameCde/:documentId/attachment/:attachmentId', component: WorkDataComponent, canActivate: [AuthGuard] },
      { path: 'workdata/:queueNameCde/:documentId/extractionpath/:attachmentId', component: WorkDataComponent, canActivate: [AuthGuard] },

      { path: 'my/managequeue', component: GenericComponent, canActivate: [AuthGuard] },

      { path: 'myclosedqueues', component: EmptyComponent, canActivate: [AuthGuard] },

    ]
  },
  { path: '', redirectTo: 'home/dashboard', pathMatch: 'full' },
  { path: 'sessiontimeout', component: SessionTimeoutComponent },
  { path: 'error/:reason', component: ErrorPageComponent },
  { path: '**', component: InvalidPageComponent }
];


@NgModule({
  imports: [RouterModule.forRoot(routes, { useHash: true })],
  exports: [RouterModule]
})
export class AppRoutingModule { }
