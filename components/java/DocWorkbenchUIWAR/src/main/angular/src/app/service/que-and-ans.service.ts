/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


import { Injectable } from '@angular/core';
import { CONSTANTS } from '../common/constants';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { ConfigDataHelper } from '../utils/config-data-helper';

@Injectable()
export class QueAndAnsService {

  constructor(private httpClient: HttpClient, private configDataHelper: ConfigDataHelper) { }

  queryLLM(query: String,doc_ids,from_cache:boolean,temperature:number,top_k:number) {
    const parent = this;
    return new Promise(function (fulfilled, rejected) {

      let url: string = parent.configDataHelper.getValue(CONSTANTS.CONFIG.MODEL_SERVICE_URL) + CONSTANTS.APIS.DOCWBSERVICE.GET_QUERY_ANS;
      
      // let url: string = "http://localhost:8003/api/v1/model/embedding/query"
      
      // url += "?question="+query
      // let url: string = "http://127.0.0.1:8003/api/v1/model/inference/qna"
      // var doc_ids=[
      //   "c50cfa4b-f0e8-4ac2-b2c3-63b5e731ad66",
      //   "f116c2d5-2015-4706-865b-9f7074fec036"
      // ]
      //  var doc_ids=[
      //   "c83ab705-e722-418e-a648-c008d30094a2"
      // ]
      
      const input_data = {
        "db_name": doc_ids,
        "question": query,
        "top_k": top_k,
        "from_cache":from_cache,
        "temperature":temperature

      }
      parent.httpClient.post(url,input_data, {
        headers: {
          'Content-Type': 'application/json; charset=utf-8',
          'rejectUnauthorized': 'false',
          'Access-Control-Allow-Origin': 'http://localhost:4200'
        }
      }
      ).subscribe(
        data => {
          console.log("data", data)
          fulfilled(data);

        },
        error => {
          console.log("Service error", error)
          rejected(error);
        });
    });
  }


}
