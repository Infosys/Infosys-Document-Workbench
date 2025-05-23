/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.extractor.service.ml.email;

import javax.annotation.PostConstruct;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
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
import com.infosys.ainauto.docwb.web.data.EmailData;

@Component
public class EmailClassifyService extends HttpClientBase implements IEmailClassifyService {

	@Autowired
	private Environment environment;

	private static final String MODELID = "modelId";
	private static final String REQUESTNUMBER = "requestNumber";
	private static final String PORTFOLIONAME = "portfolioName";
	private static final String TEXTCONTENT = "textContent";
	private static final String CLASSTOPREDICT = "classToPredict";
	private static final String SUBJECT = "Subject";

	private static final String CATEGORY = "category";
	private static final String CLASSIFYAPIURL = "service.email.classify.api.url";

	private static Logger logger = LoggerFactory.getLogger(EmailClassifyService.class);

	@PostConstruct
	private void init() {
		// To be used if using proxy
		// this.setProxyWithAuth("10.68.248.34", 80,"your-windows-user-id",
		// "your-windows-password");
	}

	public AttributeData getCategory(EmailData emailData) {

		AttributeData attributeData = null;
		try {
			JsonObjectBuilder jsonRequestBuilder = Json.createObjectBuilder();

			jsonRequestBuilder.add(MODELID, Integer.parseInt(environment.getProperty(MODELID)));
			jsonRequestBuilder.add(REQUESTNUMBER, generateCorrelationId());
			jsonRequestBuilder.add(PORTFOLIONAME, environment.getProperty(PORTFOLIONAME));

			// Fix for error->Failed to decode JSON object: 'utf-8' codec can't decode byte
			// 0xa0 in position 57: invalid start byte
			String textContent = StringUtility.findAndReplace(emailData.getEmailBodyText(),
					StringUtility.NON_ASCI_REGEX, "");
			String subject = StringUtility.findAndReplace(emailData.getEmailSubject(), StringUtility.NON_ASCI_REGEX,
					"");
			jsonRequestBuilder.add(TEXTCONTENT, textContent);

			JsonArrayBuilder builder = Json.createArrayBuilder();
			builder.add(CATEGORY);

			jsonRequestBuilder.add(CLASSTOPREDICT, builder.build());
			jsonRequestBuilder.add(SUBJECT, subject);
			JsonObject jsonRequest = jsonRequestBuilder.build();
			JsonObject jsonResponse = (JsonObject) executeHttpCall(HttpCallType.POST,
					environment.getProperty(CLASSIFYAPIURL), jsonRequest);
			float confidence;
			if (jsonResponse != null) {
				int responseCde = jsonResponse.getInt("responseCode");
				if (responseCde == 0) {
					JsonObject obj = jsonResponse.getJsonObject("response");
					JsonArray jsonResponseArray = obj.getJsonArray("classes");
					JsonObject object = jsonResponseArray.getJsonObject(0);
					attributeData = new AttributeData();
					JsonArray values = object.getJsonArray("values");
					JsonObject ValuesObject = values.getJsonObject(0);
					attributeData.setAttrValue(ValuesObject.getString("name"));
					confidence = Float.parseFloat(ValuesObject.get("confidence").toString());
					if (confidence <= 1) {
						confidence = confidence * 100;
					}
					attributeData.setConfidencePct(confidence);
					logger.info("jsonResponse" + jsonResponseArray.toString());
				}

			}
		} catch (Exception ex) {
			logger.error(ex.getMessage());
		}
		return attributeData;
	}
}
