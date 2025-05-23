/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.ui.controller.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infosys.ainauto.docwb.ui.model.api.ApiResponseData;


public class BaseController {

	private static final Logger logger = LoggerFactory.getLogger(BaseController.class);
	
	// API Response Code and Message
	public static final int API_RESPONSE_CDE_SUCCESS = 0;
	public static final String API_RESPONSE_MSG_SUCCESS = "Success";
	public static final int API_RESPONSE_CDE_FAILURE = 999;
	public static final String API_RESPONSE_MSG_FAILURE = "Failure";
	public static final int API_RESPONSE_CDE_PARTIAL_SUCCESS = 50;
	public static final String API_RESPONSE_MSG_PARTIAL_SUCCESS = "PartialSuccess";
	public static final int API_RESPONSE_CDE_NO_RECORDS = 100;
	public static final String API_RESPONSE_MSG_NO_RECORDS = "No records";

	
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
	
	protected ResponseEntity<String> jsonResponseOk(Object apiResponseData, ObjectMapper objectMapper) throws JsonProcessingException {
		String json = objectMapper.writeValueAsString(apiResponseData);
		logger.debug("API RESPONSE --> " + json);
		return new ResponseEntity<String>(json, HttpStatus.OK);
	}
	
	protected ResponseEntity<String> jsonResponseInternalServerError(Exception exception) {
		return new ResponseEntity<String>(exception.getMessage(), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	
}
