/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.extractor.service.ml.sentiment;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.HttpClientBase;
import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.web.data.AttributeData;

@Component
public class SentimentAnalysisService extends HttpClientBase implements ISentimentAnalysisService {

	@Autowired
	private Environment environment;

	private static final String REQUESTNUMBER = "requestNumber";
	private static final String TEXTCONTENT = "textContent";
	private static final String SENTIMENTAPIURL = "service.sentiment.api.url";
	private String sentimentApiUrl;

	private static Logger logger = LoggerFactory.getLogger(SentimentAnalysisService.class);

	public AttributeData getSentimentVal(String text) {
		sentimentApiUrl = environment.getProperty(SENTIMENTAPIURL);
		String requestNumber = generateCorrelationId();
		AttributeData attributeData = null;
		JsonObjectBuilder jsonRequestBuilder = Json.createObjectBuilder();

		jsonRequestBuilder.add(REQUESTNUMBER, requestNumber);

		String textContent = StringUtility.findAndReplace(text, StringUtility.NON_ASCI_REGEX, "");
		jsonRequestBuilder.add(TEXTCONTENT, textContent);

		JsonObject jsonRequest = jsonRequestBuilder.build();
		JsonObject jsonResponse = (JsonObject) executeHttpCall(HttpCallType.POST, sentimentApiUrl, jsonRequest);
		float confidence;
		if (jsonResponse != null) {
			JsonObject obj = jsonResponse.getJsonObject("response");
			JsonObject values = obj.getJsonObject("sentiment");

			attributeData = new AttributeData();
			confidence = Float.parseFloat(values.get("score").toString());
			attributeData.setConfidencePct(confidence);
			logger.info("jsonResponse" + obj.toString());

			String result = values.getString("label");
			attributeData.setAttrValue(result);
		}

		return attributeData;
	}
}
