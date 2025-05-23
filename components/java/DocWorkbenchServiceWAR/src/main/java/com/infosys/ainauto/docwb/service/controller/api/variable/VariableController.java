/** =============================================================================================================== *
 * Copyright 2022 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


package com.infosys.ainauto.docwb.service.controller.api.variable;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.service.common.WorkbenchConstants;
import com.infosys.ainauto.docwb.service.common.type.EnumEntityType;
import com.infosys.ainauto.docwb.service.common.type.EnumOperationType;
import com.infosys.ainauto.docwb.service.controller.api.BaseController;
import com.infosys.ainauto.docwb.service.controller.api.action.ActionController;
import com.infosys.ainauto.docwb.service.model.api.ApiResponseData;
import com.infosys.ainauto.docwb.service.model.api.AppVarReqData;
import com.infosys.ainauto.docwb.service.model.api.AppVarResData;
import com.infosys.ainauto.docwb.service.model.db.EntityDbData;
import com.infosys.ainauto.docwb.service.process.audit.IAuditProcess;
import com.infosys.ainauto.docwb.service.process.variable.IVariableProcess;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Controller
@CrossOrigin
@RequestMapping("/api/v1/variable")
@Api(tags = { "variable" })
public class VariableController extends BaseController {

	private static final Logger logger = LoggerFactory.getLogger(ActionController.class);

	@Autowired
	private IVariableProcess variableProcess;

	@Autowired
	private IAuditProcess auditProcess;

	@ApiOperation(value = "Update app variable value", tags = "variable")
	@RequestMapping(value = "", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE,
					MediaType.APPLICATION_JSON_VALUE })
	ResponseEntity<String> updateAppVariable(@RequestBody AppVarReqData appVarReqData) {
		ApiResponseData<Long> apiResponseData;
		List<EntityDbData> entityDbDataList = null;
		try {
			if (appVarReqData.getPrevAppVarId() <= 0) {
				String message = "Please provide valid app variable Id.";
				return jsonResponseOk(
						getStringApiResponseData(message, WorkbenchConstants.API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED,
								WorkbenchConstants.API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED));
			}
			if (!StringUtility.hasValue(appVarReqData.getAppVarKey())) {
				String message = "Please provide valid app variable key.";
				return jsonResponseOk(
						getStringApiResponseData(message, WorkbenchConstants.API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED,
								WorkbenchConstants.API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED));				
			}
			entityDbDataList = variableProcess.updateAppVariableValue(appVarReqData);
			apiResponseData = new ApiResponseData<>();
			apiResponseData.setResponse(entityDbDataList.get(0).getAppVariableId());
			apiResponseData.setResponseCde(WorkbenchConstants.API_RESPONSE_CDE_SUCCESS);
			apiResponseData.setResponseMsg(WorkbenchConstants.API_RESPONSE_MSG_SUCCESS);
			return jsonResponseOk(apiResponseData);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		} finally {
			auditProcess.addAuditDetails(entityDbDataList, EnumEntityType.APP_VARIABLE, EnumOperationType.UPDATE);
		}
	}

	@ApiOperation(value = "Get app variable value for given variable key", tags = "variable")
	@RequestMapping(value = "/{appVariableKey}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	ResponseEntity<String> getAppVariable(@PathVariable String appVariableKey) {

		ApiResponseData<AppVarResData> apiResponseData = new ApiResponseData<AppVarResData>();
		try {
			apiResponseData.setResponse(variableProcess.getAppVariableData(appVariableKey));
			apiResponseData.setResponseCde(WorkbenchConstants.API_RESPONSE_CDE_SUCCESS);
			apiResponseData.setResponseMsg(WorkbenchConstants.API_RESPONSE_MSG_SUCCESS);
			return jsonResponseOk(apiResponseData);

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		}
	}

}
