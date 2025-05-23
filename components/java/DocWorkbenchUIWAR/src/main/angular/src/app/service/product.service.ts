/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ProductData } from '../data/product-data';
import { CONSTANTS } from '../common/constants';
import { ConfigDataHelper } from '../utils/config-data-helper';

@Injectable()
export class ProductService {

  constructor(private httpClient: HttpClient, private configDataHelper: ConfigDataHelper) { }


  getApiProductData(callback) {
    const parent = this;
    let url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBSERVICE_BASE_URL) + CONSTANTS.APIS.DOCWBSERVICE.GET_PRODUCT_DATA;
    this.httpClient.get(url)
      .subscribe(
        data => {
          let productData: ProductData = data['response'];
          callback(null, productData);
        },
        error => {
          callback(error, null);
        }
      );
    return null;
  }

  getWebProductData() {
    const parent = this;
    return new Promise(function (fulfilled, rejected) {
      let url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.DOCWBWEB_BASE_URL) + CONSTANTS.APIS.DOCWBWEB.GET_PRODUCT_DATA;
      parent.httpClient.get(url).subscribe(
          data => {
            let productData: ProductData = data['response'];
            fulfilled(productData);
          },
          error => {
            rejected(error);
          }
        );
    });
  }


}
