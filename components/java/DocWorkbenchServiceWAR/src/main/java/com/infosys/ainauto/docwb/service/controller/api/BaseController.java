/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.controller.api;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.safety.Whitelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.util.HtmlUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infosys.ainauto.docwb.service.model.api.ApiResponseData;

public class BaseController {

	private static final Logger logger = LoggerFactory.getLogger(BaseController.class);

	protected ApiResponseData<String> getStringApiResponseData(String message, int responseCde, String responseTxt) {
		ApiResponseData<String> apiResponseData = new ApiResponseData<String>();
		apiResponseData.setResponse(message);
		apiResponseData.setResponseCde(responseCde);
		apiResponseData.setResponseMsg(responseTxt);
		return apiResponseData;
	}

	protected ResponseEntity<String> jsonResponseOk(Object apiResponseData) throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		return jsonResponseOk(apiResponseData, objectMapper);
	}

	protected ResponseEntity<String> jsonResponseOk(Object apiResponseData, ObjectMapper objectMapper)
			throws JsonProcessingException {
		String str = objectMapper.writeValueAsString(apiResponseData);
		// Disabling the pretty print to handle input == output format.
		OutputSettings settings = new OutputSettings();
		settings.prettyPrint(false);
		str = HtmlUtils.htmlEscape(str);
		String json = Jsoup
				.clean(str, "http://",
						Whitelist.relaxed().removeAttributes("img", "alt").preserveRelativeLinks(true)
								.addAttributes(":all", "class"), settings)
				.replaceAll("\n", "").replaceAll("\\\"\\\\&quot;", "\\\\\"").replaceAll("\\\\&quot;\\\"", "\\\\\"");
		// Replace &amp; (HTML character) back to & (text character)		
		json = json.replaceAll("&amp;", "&").replaceAll("&gt;", ">").replaceAll("&lt;", "<");
		json = HtmlUtils.htmlUnescape(json);
		logger.debug("API RESPONSE --> " + json);
		return new ResponseEntity<String>(json, HttpStatus.OK);
	}

	protected ResponseEntity<String> jsonResponseInternalServerError(Exception exception) {
		return new ResponseEntity<String>(exception.getMessage(), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	protected ResponseEntity<String> jsonResponseForbidden() {
		String message = "You are not authorized to this content.";
		return new ResponseEntity<String>(message, new HttpHeaders(), HttpStatus.FORBIDDEN);
	}

	/*
	 * Method use for removing Mass Binder: Insecure Configuration reported in
	 * fortify VA. In Our Request model class we have used only for Request mapping
	 * not for database call.
	 */
	@InitBinder
	public void setDisallowedFieldsAsEmpty(WebDataBinder binder) {
		binder.setDisallowedFields(new String[] {});
	}
}
