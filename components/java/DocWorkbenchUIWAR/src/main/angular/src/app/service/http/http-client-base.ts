/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */



import { tap } from 'rxjs/operators';
import { Injectable, isDevMode } from '@angular/core';
import { HttpRequest, HttpResponse, HttpHandler, HttpEvent, HttpInterceptor, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Router } from '@angular/router';
import { AuthenticationService } from '../authentication.service';

import { ConfigDataHelper } from '../../utils/config-data-helper';
import { CONSTANTS } from '../../common/constants';

class HttpRequestData {
  url: string;
  body: string;
  constructor(url: string, body: string) {
    this.url = url;
    this.body = body;
  }
  toString() {
    return JSON.stringify(this);
  }
}

class HttpResponseData {
  body: string;
  constructor(body: string) {
    this.body = body;
  }
  toString() {
    return JSON.stringify(this);
  }
}

@Injectable()
export class HttpClientBase implements HttpInterceptor {
  constructor(private router: Router, private authenticationService: AuthenticationService,
    private configDataHelper: ConfigDataHelper) { }

  private httpRequestData: HttpRequestData;
  private httpResponseData: HttpResponseData;

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const parent = this;
    this.httpRequestData = new HttpRequestData(request.url, request.body);
    if (isDevMode) {
      // console.log("### HTTP REQUEST ### " + new Date().toLocaleTimeString() + " >>> " + this.httpRequestData);
      // console.log(JSON.stringify(request.headers));
    }
    let isAuthTokenExpired = false
    // Do this only if calling DOCWB APIs
    if (request.url.startsWith(parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL)) ||
      request.url.startsWith(parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBFILESERVER_BASE_URL))) {
      let requestHeaders = request.headers;
      requestHeaders = requestHeaders.append('X-Client', 'EmailWorkbench');
      // Set Authorization if not already set
      if (!requestHeaders.get('Authorization')) {
        let authToken: String = this.authenticationService.getAuthToken();
        if (authToken) {
          requestHeaders = requestHeaders.append('Authorization', 'Bearer ' + authToken);
        } else {
          isAuthTokenExpired = true
        }
      }
      const clonedRequestObj: any = request.clone({
        headers: requestHeaders
      });

      const clonedRequestConst = clonedRequestObj;

      return next.handle(clonedRequestConst).pipe(tap((event: HttpEvent<any>) => {
        if (event instanceof HttpResponse) {
          this.httpResponseData = new HttpResponseData(event.body);
          if (isDevMode) {
            // console.log("### HTTP RESPONSE ### " + new Date().toLocaleTimeString() + " >>> " + this.httpResponseData);
          }
        }
      }, (err: any) => {
        if (err instanceof HttpErrorResponse) {
          if (isDevMode) {
            // console.log("### ERROR HTTP RESPONSE ### " + new Date().toLocaleTimeString() + " >>> " + JSON.stringify(err));
            // console.log(err.status);
          }
          if (isAuthTokenExpired) {
            if (err.status === 401) {
              parent.router.navigate(['sessiontimeout']);
            }
            // In some cases, actual httpStatusCode=401 in API response but we get it as 0
            if (err.status === 0) {
              parent.router.navigate(['sessiontimeout']);
            }
            // Clear all session data in browser
            parent.authenticationService.deleteAuthToken();
          }
          else {
            let message = "An error occurred while communicating with the server."
            message += "\nIf error persists, please refresh this page OR relogin."
            alert(message)
          }

        }
      }));
    } else { // Do this for all other API calls
      return next.handle(request).pipe(tap((event: HttpEvent<any>) => {
        if (event instanceof HttpResponse) {
          this.httpResponseData = new HttpResponseData(event.body);
          if (isDevMode) {
            // console.log("### HTTP RESPONSE ### " + new Date().toLocaleTimeString() + " >>> " + this.httpResponseData);
          }
        }
      }, (err: any) => {
        if (err instanceof HttpErrorResponse) {
          if (isDevMode) {
            console.log('### ERROR HTTP RESPONSE ### ' + new Date().toLocaleTimeString() + ' >>> ' + JSON.stringify(err));
            console.log(err.status);
          }
        }
      }));
    }
  }
}

// Reference https://ryanchenkie.com/angular-authentication-using-the-http-client-and-http-interceptors
