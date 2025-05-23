/** =============================================================================================================== *
 * Copyright 2022 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


package com.infosys.ainauto.docwb.ui.process.query;

import javax.json.JsonObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infosys.ainauto.commonutils.HttpClientBase;
import com.infosys.ainauto.docwb.ui.common.DocWorkbenchUIException;
import com.infosys.ainauto.docwb.ui.model.api.query.QueryReqData;
import com.infosys.ainauto.docwb.ui.service.IElasticsearchService;

@Component
public class QueryProcess extends HttpClientBase implements IQueryProcess {
	
	@Value("${elasticsearch.base.url}")
	private String ES_BASE_URL;
	
	@Autowired
	private IElasticsearchService elasticsearchService;
	
	@Override
	public JsonObject executeQuery(QueryReqData queryReqData) throws DocWorkbenchUIException, JsonProcessingException {
		String reuestBody =new ObjectMapper().writeValueAsString(queryReqData.getRequestBody());
		return elasticsearchService.postQuery(buildApiURL(queryReqData.getApi()), reuestBody);
	}
	
	private String buildApiURL(String apiParam) {
		String baseURL = ES_BASE_URL;
		if (baseURL.endsWith("/")) {
			baseURL = baseURL.substring(0, baseURL.length()-1);
		}
		String api = apiParam;
		if(api.startsWith("/")) {
			api = api.substring(1,api.length());
		}
		return baseURL+"/"+api;
	}

}
