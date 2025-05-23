/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.controller.api.auth;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infosys.ainauto.docwb.service.common.WorkbenchConstants;
import com.infosys.ainauto.docwb.service.controller.api.BaseController;
import com.infosys.ainauto.docwb.service.model.api.ApiResponseData;
import com.infosys.ainauto.docwb.service.model.api.AuthResData;
import com.infosys.ainauto.docwb.service.model.api.auth.AuthorizationData;
import com.infosys.ainauto.docwb.service.process.auth.IAuthorizationProcess;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Controller
@CrossOrigin
@RequestMapping("/api/v1/auth")
@Api(tags = { "auth" })
public class AuthorizationController extends BaseController {

	private static final Logger logger = LoggerFactory.getLogger(AuthorizationController.class);

	@Autowired
	private IAuthorizationProcess authorizationProcess;

	@ApiOperation(value = "Get authorization token", tags = "auth")
	@RequestMapping(value = "", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	ResponseEntity<String> getAuthToken(@RequestBody AuthorizationData authorizationData) {
		try {
			ApiResponseData<AuthResData> apiResponseData = new ApiResponseData<>();
			AuthResData authResData = new AuthResData();
			if (authorizationData.getAuthorization() != null
					&& authorizationData.getAuthorization().toLowerCase(Locale.ENGLISH).startsWith("basic")) {
				// Authorization: Basic base64credentials
				String base64Credentials = authorizationData.getAuthorization().substring("Basic".length()).trim();
				byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
				String credentials = new String(credDecoded, StandardCharsets.UTF_8);
				// credentials = username:password
				String[] values = new String[2];
				values = credentials.split(":", 2);
				String userName = values[0];
				String rawPassword = values[1];
				authResData = authorizationProcess.getAuthToken(userName, rawPassword, authorizationData.getTenantId());
			} else {
				apiResponseData.setResponseCde(WorkbenchConstants.API_RESPONSE_CDE_BASIC_AUTH_MISSING);
				apiResponseData.setResponseMsg(WorkbenchConstants.API_RESPONSE_MSG_BASIC_AUTH_MISSING);
			}
			apiResponseData.setResponse(authResData);
			if (authResData.getErrorCode() == WorkbenchConstants.AUTH_TOKEN_ERROR_CDE_NOT_SERVICE_ACCOUNT) {
				apiResponseData.setResponseCde(WorkbenchConstants.API_RESPONSE_CDE_NOT_SERVICE_ACCOUNT);
				apiResponseData.setResponseMsg(WorkbenchConstants.API_RESPONSE_MSG_NOT_SERVICE_ACCOUNT);
			} else if (authResData.getErrorCode() == WorkbenchConstants.AUTH_TOKEN_ERROR_CDE_BAD_CREDENTIALS) {
				apiResponseData.setResponseCde(WorkbenchConstants.API_RESPONSE_CDE_BAD_CREDENTIALS);
				apiResponseData.setResponseMsg(WorkbenchConstants.API_RESPONSE_MSG_BAD_CREDENTIALS);
			} else if (authResData.getErrorCode() == WorkbenchConstants.AUTH_TOKEN_ERROR_CDE_INVALID_TENANT_ID) {
				apiResponseData.setResponseCde(WorkbenchConstants.API_RESPONSE_CDE_INVALID_TENANT_ID);
				apiResponseData.setResponseMsg(WorkbenchConstants.API_RESPONSE_MSG_INVALID_TENANT_ID);
			} else if (authResData
					.getErrorCode() == WorkbenchConstants.AUTH_TOKEN_ERROR_CDE_ACCOUNT_DISABLED_OR_INACTIVE) {
				apiResponseData.setResponseCde(WorkbenchConstants.API_RESPONSE_CDE_ACCOUNT_DISABLED_OR_INACTIVE);
				apiResponseData.setResponseMsg(WorkbenchConstants.API_RESPONSE_MSG_ACCOUNT_DISABLED_OR_INACTIVE);
			} else if (authResData
					.getErrorCode() == WorkbenchConstants.AUTH_TOKEN_ERROR_CDE_ACCOUNT_ROLE_NOT_ASSIGNED) {
				apiResponseData.setResponseCde(WorkbenchConstants.API_RESPONSE_CDE_ACCOUNT_ROLE_NOT_ASSIGNED);
				apiResponseData.setResponseMsg(WorkbenchConstants.API_RESPONSE_MSG_ACCOUNT_ROLE_NOT_ASSIGNED);
			} else if (authResData.getErrorCode() == WorkbenchConstants.AUTH_TOKEN_ERROR_CDE_USER_UNAUTHORIZED) {
				apiResponseData.setResponseCde(WorkbenchConstants.API_RESPONSE_CDE_NOT_AUTHORIZED);
				apiResponseData.setResponseMsg(WorkbenchConstants.API_RESPONSE_MSG_NOT_AUTHORIZED);
			} else {
				apiResponseData.setResponseCde(WorkbenchConstants.API_RESPONSE_CDE_SUCCESS);
				apiResponseData.setResponseMsg(WorkbenchConstants.API_RESPONSE_MSG_SUCCESS);
			}

			abstract class MixIn {
				@JsonIgnore
				abstract String getErrorCode();
			}
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.addMixIn(AuthResData.class, MixIn.class);
			return jsonResponseOk(apiResponseData, objectMapper);

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		}
	}

	@ApiOperation(value = "Validate Auth Token", tags = "auth")
	@RequestMapping(value = "/validate", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	ResponseEntity<String> validateAuthToken() {
		try {
			ApiResponseData<AuthResData> apiResponseData = new ApiResponseData<>();
			apiResponseData.setResponseCde(WorkbenchConstants.API_RESPONSE_CDE_SUCCESS);
			apiResponseData.setResponseMsg(WorkbenchConstants.API_RESPONSE_MSG_SUCCESS);
			abstract class MixIn {
				@JsonIgnore
				abstract String getErrorCode();
			}
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.addMixIn(AuthResData.class, MixIn.class);
			return jsonResponseOk(apiResponseData, objectMapper);

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		}
	}

}
