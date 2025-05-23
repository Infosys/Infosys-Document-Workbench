/** =============================================================================================================== *
 * Copyright 2022 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


package com.infosys.ainauto.docwb.ui.service;

import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.HttpClientBase;
import com.infosys.ainauto.commonutils.HttpClientBase.Authentication.BasicAuthenticationConfig;
import com.infosys.ainauto.docwb.ui.common.DocWorkbenchUIException;

@Component
public class ElasticsearchService extends HttpClientBase implements IElasticsearchService {

	private static final Logger logger = LoggerFactory.getLogger(ElasticsearchService.class);

	protected ElasticsearchService(@Value("${elasticsearch.username}") String ELASTICSEARCH_USERNAME,
			@Value("${elasticsearch.drowssap}") String ELASTICSEARCH_DROWSSAP) {
		super(new HttpClientConfig().setDisableSSLValidation(true),
				new BasicAuthenticationConfig(ELASTICSEARCH_USERNAME, ELASTICSEARCH_DROWSSAP, true));
	}

	@Override
	public JsonObject postQuery(String hostUrl, String bodyJsonStr) throws DocWorkbenchUIException {
		logger.info("postQuery Request URL: " + hostUrl);
		JsonObject jsonResponse = (JsonObject) executeHttpCall(HttpCallType.POST, hostUrl, bodyJsonStr);
		return jsonResponse;
	}

}
