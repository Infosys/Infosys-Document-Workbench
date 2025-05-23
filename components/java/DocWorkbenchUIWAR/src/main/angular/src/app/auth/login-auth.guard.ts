/** =============================================================================================================== *
 * Copyright 2022 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { SessionService } from '../service/session.service';
import { AuthenticationService } from '../service/authentication.service';

@Injectable()
export class LoginAuthGuard implements CanActivate {
  constructor(private sessionService: SessionService,
    private authenticationService: AuthenticationService,
    private router: Router) { }

  canActivate(
    next: ActivatedRouteSnapshot,
    state: RouterStateSnapshot): Observable<boolean> | Promise<boolean> | boolean {
    const parent = this;

    if (parent.authenticationService.isUserLoggedIn()) {
      // already logged in so redirect to home page
      parent.router.navigate(['/home/dashboard']);
    }
    return true;
  }
}
