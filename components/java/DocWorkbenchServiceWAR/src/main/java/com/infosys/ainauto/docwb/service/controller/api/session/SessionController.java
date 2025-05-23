/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.controller.api.session;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

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
import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.service.common.WorkbenchConstants;
import com.infosys.ainauto.docwb.service.common.type.EnumEntityType;
import com.infosys.ainauto.docwb.service.common.type.EnumOperationType;
import com.infosys.ainauto.docwb.service.controller.api.BaseController;
import com.infosys.ainauto.docwb.service.model.api.ApiResponseData;
import com.infosys.ainauto.docwb.service.model.api.UserQueueResData;
import com.infosys.ainauto.docwb.service.model.api.UserResData;
import com.infosys.ainauto.docwb.service.model.api.session.UpdatePasswordReqData;
import com.infosys.ainauto.docwb.service.model.db.EntityDbData;
import com.infosys.ainauto.docwb.service.process.audit.IAuditProcess;
import com.infosys.ainauto.docwb.service.process.auth.IApiRoleAuthorizationProcess;
import com.infosys.ainauto.docwb.service.process.user.IUserProcess;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Controller
@CrossOrigin
@RequestMapping("/api/v1/session")
@Api(tags = { "session" })
public class SessionController extends BaseController {

	private static final Logger logger = LoggerFactory.getLogger(SessionController.class);

	@Autowired
	private IUserProcess userProcess;

	@Autowired
	private IAuditProcess auditProcess;

	@Autowired
	private IApiRoleAuthorizationProcess apiRoleAuthorizationProcess;

	@ApiOperation(value = "Get logged in user details", tags = "session")
	@RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	ResponseEntity<String> getLoggedInUserDetails() {
		try {
			ApiResponseData<UserResData> apiResponseData = new ApiResponseData<>();
			UserResData userResData = userProcess.getLoggedInUserDetails();
			userResData.setFeatureAuthDataList(apiRoleAuthorizationProcess.getLoggedInUserRoleFeatureAuthData());
			apiResponseData.setResponse(userResData);
			apiResponseData.setResponseCde(WorkbenchConstants.API_RESPONSE_CDE_SUCCESS);
			apiResponseData.setResponseMsg(WorkbenchConstants.API_RESPONSE_MSG_SUCCESS);
			abstract class MixIn {
				@JsonIgnore
				abstract long getUserPassword();

				@JsonIgnore
				abstract boolean isAccountEnabled();

				@JsonIgnore
				abstract long getAppUserId();

			}
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.addMixIn(UserResData.class, MixIn.class);
			objectMapper.addMixIn(UserQueueResData.class, MixIn.class);

			return jsonResponseOk(apiResponseData, objectMapper);

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		}
	}

	@ApiOperation(value = "Change existing user password", tags = "session")
	@RequestMapping(value = "/password", method = RequestMethod.PUT, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_JSON_VALUE })

	ResponseEntity<String> changePassword(@RequestBody UpdatePasswordReqData updatePasswordReqData) {
		long appUserId = 0;
		String newPassword = updatePasswordReqData.getNewPassword();
		String oldPassword = updatePasswordReqData.getOldPassword();
		String errorMessage = "";
		try {
			// TO-DO Uncomment the code for deployment
			// if (!PatternUtility.isValidPassword(newPassword)) { //if new pwd is not valid
			// errorMessage = "Password must be of 6 characters & should contains a
			// combination of uppercase, lowercase letter, special character & number.";
			// return jsonResponseOk(getStringApiResponseData(errorMessage,
			// WorkbenchConstants.API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED,
			// WorkbenchConstants.API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED));
			// }
			ApiResponseData<String> apiResponseData = new ApiResponseData<String>();
			if (!StringUtility.hasValue(errorMessage)) {
				EntityDbData entityDbData = new EntityDbData();

				entityDbData = userProcess.changePassword(oldPassword, newPassword);
				appUserId = entityDbData.getAppUserId();
				String apiResponse = entityDbData.getApiResponseData();

				if (appUserId > 0) // when pwd is successfully changed
					apiResponseData = getStringApiResponseData(apiResponse, WorkbenchConstants.API_RESPONSE_CDE_SUCCESS,
							WorkbenchConstants.API_RESPONSE_MSG_SUCCESS);
				else if (appUserId == WorkbenchConstants.PASSWORD_MISMATCH_EXISTING
						|| appUserId == WorkbenchConstants.PASSWORD_SAME_AS_EXISTING 
						||  appUserId == WorkbenchConstants.PASSWORD_MANAGED_BY_LDAP) /*
																						 * when current pwd entered
																						 * doesn't match with the DB OR
																						 * new pwd and current pwd
																						 * entered are same OR 
																						 * pwd is managed by LDAP
																						 */
					apiResponseData = getStringApiResponseData(apiResponse, WorkbenchConstants.API_RESPONSE_CDE_FAILURE,
							WorkbenchConstants.API_RESPONSE_MSG_FAILURE);
			}
			return jsonResponseOk(apiResponseData);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		} finally {
			if (appUserId > 0) {
				List<EntityDbData> entityDbDataList = new ArrayList<EntityDbData>();
				EntityDbData entityDbData = new EntityDbData();
				entityDbData.setAppUserId(appUserId);
				entityDbData.setUserPasswordChanged(true);
				entityDbDataList.add(entityDbData);
				auditProcess.addAuditDetails(entityDbDataList, EnumEntityType.USER, EnumOperationType.UPDATE);
			}
		}
	}
	
	@ApiOperation(value = "Terminate session (logout)", tags = "session")
	@RequestMapping(value = "/terminate", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_JSON_VALUE })
	ResponseEntity<String> terminate(HttpServletRequest request) {
		ApiResponseData<String> apiResponseData = new ApiResponseData<String>();
		try {
			request.logout();
			apiResponseData.setResponse("Session terminated successfully.");
			apiResponseData.setResponseCde(WorkbenchConstants.API_RESPONSE_CDE_SUCCESS);
			apiResponseData.setResponseMsg(WorkbenchConstants.API_RESPONSE_MSG_SUCCESS);
			return jsonResponseOk(apiResponseData);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		}
	}

}
