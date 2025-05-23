/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.core.handler.controller.api.callback;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.infosys.ainauto.docwb.engine.core.handler.controller.api.BaseController;
import com.infosys.ainauto.docwb.engine.core.model.ApiResponseData;
import com.infosys.ainauto.docwb.engine.core.model.api.ScriptResponseWrapperData;
import com.infosys.ainauto.docwb.engine.core.process.action.IAutomationExecutionProcess;
import com.infosys.ainauto.docwb.engine.core.service.script.ScriptExecutorProxy;
import com.infosys.ainauto.scriptexecutor.api.IScriptExecutorService;
import com.infosys.ainauto.scriptexecutor.data.ScriptResponseData;
import com.infosys.ainauto.scriptexecutor.data.ScriptResponseItemData;

@RestController
@RequestMapping("/api/v1/callback/scriptexecutor")
public class ScriptExecutorCallbackController extends BaseController {
	private static final Logger logger = LoggerFactory.getLogger(ScriptExecutorCallbackController.class);

	@Autowired
	private IAutomationExecutionProcess automationExecutionProcess;

	private IScriptExecutorService scriptExecutorService;
	
	@Autowired
	private ScriptExecutorProxy scriptExecutorProxy;
	
	@PostConstruct
	private void init() {
		scriptExecutorService = scriptExecutorProxy.getScriptExecutorService();
	}
	

	@RequestMapping(value = "/status", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE,
					MediaType.APPLICATION_JSON_VALUE })
	ResponseEntity<String> getResults(@RequestBody ScriptResponseWrapperData responseData) {
		ScriptResponseItemData scriptResponseItemData;
		try {
			int rowsImpacted = automationExecutionProcess.updateExternalTransaction(responseData.getTransactionId(),
					responseData.getStatus());
			String apiResponse = rowsImpacted + " Records(s) updated";
			ApiResponseData<String> apiResponseData;
			if (rowsImpacted > 0) {
				
				ScriptResponseData scriptResponseData = scriptExecutorService.getTransactionStatusAndResult(responseData.getTransactionId());
				scriptResponseItemData = scriptResponseData.getScriptResponseItemDataList().get(0);
				int rowsUpdated = automationExecutionProcess.updateResultsForAction(scriptResponseItemData);
				if (rowsUpdated > 0) {
					rowsImpacted += rowsUpdated;
					apiResponse = rowsImpacted + " Records(s) updated";
					apiResponseData = getStringApiResponseData(apiResponse, API_RESPONSE_CDE_SUCCESS,
							API_RESPONSE_MSG_SUCCESS);
				} else {
					apiResponse = "Could not update transaction successfully";
					apiResponseData = getStringApiResponseData(apiResponse, API_RESPONSE_CDE_NO_RECORDS,
							API_RESPONSE_MSG_NO_RECORDS);
				}

			} else {
				apiResponse = "Could not update external transaction successfully";
				apiResponseData = getStringApiResponseData(apiResponse, API_RESPONSE_CDE_NO_RECORDS,
						API_RESPONSE_MSG_NO_RECORDS);
			}

			return jsonResponseOk(apiResponseData);

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		}

	}
}