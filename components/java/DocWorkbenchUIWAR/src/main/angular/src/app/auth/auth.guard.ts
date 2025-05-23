/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { SessionService } from '../service/session.service';
import { AuthenticationService } from '../service/authentication.service';
import { UserData } from '../data/user-data';
import { UtilityService } from '../service/utility.service';
import { CONSTANTS } from '../common/constants';

@Injectable()
export class AuthGuard implements CanActivate {
  constructor(private sessionService: SessionService,
    private authenticationService: AuthenticationService,
    private router: Router) { }


  private static NOT_VALID_USER_ERROR = 'error/user';
  private static NOT_VALID_PAGE_ERROR = 'error/page';
  private isAdmin: boolean;
  canActivate(
    next: ActivatedRouteSnapshot,
    state: RouterStateSnapshot): Observable<boolean> | Promise<boolean> | boolean {
    const parent = this;
    if (!this.authenticationService.isUserLoggedIn()) {
      // not logged in so redirect to login page with the return url
      this.router.navigate(['/login'], {
        queryParams: {
          returnUrl: state.url
        }
      });
      // location.reload(); //This is needed to reload sidebar after login
      return false;
    }
    const promise = parent.isUserValidPromise();
    return promise.then(function (value) {
      const isUserValid: boolean = value as boolean;
      if (isUserValid) {
        let nextUrl = '';
        for (let i = 0; i < next.url.length; i++) {
          nextUrl += next.url[i].path;
        }
        let queueNameCde: number;

        // To handle empty paths such as '' added in app-routing-module for admin page
        if (next.url.length === 0) {
          return true;
        }

        switch (next.url[0].path) {
          case 'admin':
            return parent.checkUserRole();

          case 'worklist':
          case 'workdata':
            const nextUrlPathVal = next.url[1].path;
            queueNameCde = +nextUrlPathVal;
            return parent.checkUserQueue(queueNameCde);
          case 'epworkdata':
            queueNameCde = +next.url[1].path;
            return parent.checkUserQueue(queueNameCde);
          case 'queue':
            queueNameCde = +next.url[1].path;
            return parent.checkUserQueue(queueNameCde);

          default:
            return true;
        }
      } else {
        parent.router.navigate([AuthGuard.NOT_VALID_USER_ERROR]);
        return false;
      }
    }).catch(function (error) {
      parent.router.navigate([AuthGuard.NOT_VALID_USER_ERROR]);
      return false;
    });

  }

  private checkUserRole() {
    const parent = this;

    const promise = parent.sessionService.getLoggedInUserDetailsPromise();
    return promise.then(function (value) {
      const userData: UserData = value as UserData;
      let isAdmin: boolean;

      isAdmin = (parent.sessionService.getFeatureAccessModeDataFor(CONSTANTS.FEATURE_ID_CONFIG.USER_LIST).isVisible ||
                 parent.sessionService.getFeatureAccessModeDataFor(CONSTANTS.FEATURE_ID_CONFIG.USER_VIEW).isVisible ||
                 parent.sessionService.getFeatureAccessModeDataFor(CONSTANTS.FEATURE_ID_CONFIG.USER_EDIT).isVisible ||
                 parent.sessionService.getFeatureAccessModeDataFor(CONSTANTS.FEATURE_ID_CONFIG.USER_DELETE).isVisible)

      if (isAdmin) {
        parent.isAdmin = true;
        return true;
      } else {
        parent.isAdmin = false;
        parent.router.navigate([AuthGuard.NOT_VALID_PAGE_ERROR]);
        return false;
      }

    }).catch(function (error) {
      parent.router.navigate([AuthGuard.NOT_VALID_PAGE_ERROR]);
      return false;
    });
  }

  private checkUserQueue(queueNameCde: number) {
    const parent = this;

    const promise = parent.sessionService.getLoggedInUserDetailsPromise();
    return promise.then(function (value) {
      const userData: UserData = value as UserData;
      let isQueueAssigned = false;
      for (let i = 0; i < userData.queueDataList.length; i++) {
        if (queueNameCde === userData.queueDataList[i].queueNameCde) {
          isQueueAssigned = true;
          break;
        }
      }
      if (isQueueAssigned) {
        console.log('queue is assigned to the user');
        return true;
      } else {
        console.log('queue is not assigned to the user');
        parent.router.navigate([AuthGuard.NOT_VALID_PAGE_ERROR]);
        return false;
      }

    }).catch(function (error) {
      parent.router.navigate([AuthGuard.NOT_VALID_PAGE_ERROR]);
      return false;
    });
  }

  private isUserValidPromise() {
    const parent = this;
    return new Promise(function (fulfilled, rejected) {
      const promise = parent.sessionService.getLoggedInUserDetailsPromise();
      return promise.then(function (value) {
        const userData: UserData = value as UserData;
        if (userData.userTypeCde !== 1) {
          fulfilled(false);
        } else {
          fulfilled(true);
        }
      }).catch(function (error) {
        parent.router.navigate([AuthGuard.NOT_VALID_USER_ERROR]);
        fulfilled(false);
      });
    });
  }

}
