/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.ui.controller.api.telemetry;

import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.infosys.ainauto.docwb.ui.controller.api.BaseController;
import com.infosys.ainauto.docwb.ui.model.api.ApiResponseData;
import com.infosys.ainauto.docwb.ui.model.api.query.QueryReqData;
import com.infosys.ainauto.docwb.ui.process.query.IQueryProcess;

@Controller
@CrossOrigin
@RequestMapping("/api/v1/telemetry")
public class TelemetryController extends BaseController {

	private static final Logger logger = LoggerFactory.getLogger(TelemetryController.class);

	@Value("${elasticsearch.post.telemetry.api}")
	private String TELE_ADD_API;
	
	@Autowired
	private IQueryProcess queryProcess;

	@RequestMapping(value = "/add", method = RequestMethod.POST, consumes = {
			MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE }, produces = {
					MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE })
	ResponseEntity<String> addData(@RequestBody Object requestBody) {
		ApiResponseData<String> apiResponseData = new ApiResponseData<>();
		try {
			QueryReqData queryReqData = new QueryReqData();
			queryReqData.setApi(TELE_ADD_API);
			queryReqData.setRequestBody(requestBody);
			JsonObject jsonResponse = queryProcess.executeQuery(queryReqData);
			if (jsonResponse == null) {
				apiResponseData.setResponseCde(API_RESPONSE_CDE_FAILURE);
				apiResponseData.setResponseMsg(API_RESPONSE_MSG_FAILURE);
			} else {
				apiResponseData.setResponse(jsonResponse.toString());
				apiResponseData.setResponseCde(API_RESPONSE_CDE_SUCCESS);
				apiResponseData.setResponseMsg(API_RESPONSE_MSG_SUCCESS);
			}
			return jsonResponseOk(apiResponseData);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		}
	}
}
