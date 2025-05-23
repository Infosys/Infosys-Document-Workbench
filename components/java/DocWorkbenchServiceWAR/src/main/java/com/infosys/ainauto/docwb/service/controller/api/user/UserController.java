/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.controller.api.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infosys.ainauto.commonutils.PatternUtility;
import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.service.common.InvalidTenantIdException;
import com.infosys.ainauto.docwb.service.common.SessionHelper;
import com.infosys.ainauto.docwb.service.common.WorkbenchConstants;
import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.common.type.EnumEntityType;
import com.infosys.ainauto.docwb.service.common.type.EnumOperationType;
import com.infosys.ainauto.docwb.service.controller.api.BaseController;
import com.infosys.ainauto.docwb.service.model.api.ApiResponseData;
import com.infosys.ainauto.docwb.service.model.api.InsertUserResData;
import com.infosys.ainauto.docwb.service.model.api.UserQueueResData;
import com.infosys.ainauto.docwb.service.model.api.UserResData;
import com.infosys.ainauto.docwb.service.model.api.UserTeammateResData;
import com.infosys.ainauto.docwb.service.model.api.user.InsertUserQueueReqData;
import com.infosys.ainauto.docwb.service.model.api.user.InsertUserReqData;
import com.infosys.ainauto.docwb.service.model.api.user.UpdateUserReqData;
import com.infosys.ainauto.docwb.service.model.db.EntityDbData;
import com.infosys.ainauto.docwb.service.model.process.AppUserData;
import com.infosys.ainauto.docwb.service.process.audit.IAuditProcess;
import com.infosys.ainauto.docwb.service.process.user.IUserProcess;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Controller
@CrossOrigin
@RequestMapping("/api/v1/user")
@Api(tags = { "user" })
public class UserController extends BaseController {

	private static final Logger logger = LoggerFactory.getLogger(UserController.class);

	@Autowired
	private IUserProcess userProcess;
	@Autowired
	private IAuditProcess auditProcess;

	@ApiOperation(value = "Add a new user along with associated data", tags = "user")
	@RequestMapping(value = "/register", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE,
					MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> addNewUser(@RequestBody InsertUserReqData insertUserRequestData) {
		ApiResponseData<InsertUserResData> apiResponseData;
		long userId = -1;

		InsertUserResData insertUserResData = null;
		List<EntityDbData> entityDbDataList = new ArrayList<>();
		EntityDbData entityDbData = new EntityDbData();
		try {
			insertUserResData = validateUserCreationData(insertUserRequestData);

			if (insertUserResData.getErrCde() > 0) {
				return jsonResponseOk(populateApiResponseData(insertUserResData,
						WorkbenchConstants.API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED, WorkbenchConstants.API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED));
			}

			insertUserResData = new InsertUserResData();
			try {
				entityDbData = userProcess.addUser(insertUserRequestData,insertUserRequestData.getTenantId());
				userId = entityDbData.getAppUserId();
			} catch (InvalidTenantIdException ite) {
				insertUserResData.setErrTxt(ite.getMessage());
				insertUserResData.setErrCde(19);
				return jsonResponseOk(populateApiResponseData(insertUserResData, WorkbenchConstants.API_RESPONSE_CDE_INVALID_TENANT_ID,
						WorkbenchConstants.API_RESPONSE_MSG_INVALID_TENANT_ID));
			} catch (WorkbenchException wex) {
				if (wex.getMessage().equals(WorkbenchConstants.DB_ERROR_MSG_USERNAME_ALREADY_EXISTS)) {
					insertUserResData.setErrTxt("Username already exists: " + insertUserRequestData.getUserName());
					insertUserResData.setErrCde(18);
					return jsonResponseOk(populateApiResponseData(insertUserResData,
							WorkbenchConstants.API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED, WorkbenchConstants.API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED));
				} else {
					throw new WorkbenchException("", wex);
				}
			}
			
			insertUserResData.setUserId(userId);
			insertUserResData.setUserName(insertUserRequestData.getUserName());
			apiResponseData = new ApiResponseData<>();
			apiResponseData.setResponse(insertUserResData);
			apiResponseData.setResponseCde(WorkbenchConstants.API_RESPONSE_CDE_SUCCESS);
			apiResponseData.setResponseMsg(WorkbenchConstants.API_RESPONSE_MSG_SUCCESS);
			return jsonResponseOk(apiResponseData);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		} finally {
			if (userId > 0) {
				entityDbDataList.add(entityDbData);
				auditProcess.addAuditDetails(entityDbDataList, EnumEntityType.USER, EnumOperationType.INSERT);
			}
		}
	}

	@ApiOperation(value = "Add user to a queue", tags = "user")
	@RequestMapping(value = "/queue", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE,
					MediaType.APPLICATION_JSON_VALUE })
	ResponseEntity<String> addUserQueueRelationship(@RequestBody InsertUserQueueReqData insertUserQueueReqData) {
		long appUserQueueRelId = 0;
		List<Long> appUserQueueRelIdList = new ArrayList<Long>();

		try {
			String tenantId = "";

			if (!SessionHelper.getTenantId().isEmpty()) {
				tenantId = SessionHelper.getTenantId();
			}
			if (insertUserQueueReqData.getAppUserId()<=0)
			{ 
				AppUserData appUserData =userProcess.getUserDetailsFromLoginId(insertUserQueueReqData.getAppUserLoginId(),tenantId);
				insertUserQueueReqData.setAppUserId(appUserData.getAppUserId());
			}
			
			String validationMessage = validateUserQueueRelCreationData(insertUserQueueReqData);

			if (StringUtility.hasValue(validationMessage)) {
				return jsonResponseOk(getStringApiResponseData(validationMessage,
						WorkbenchConstants.API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED, WorkbenchConstants.API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED));
			}

			appUserQueueRelId = userProcess.addUserToQueue(insertUserQueueReqData);
			if (appUserQueueRelId > 0)
				appUserQueueRelIdList.add(appUserQueueRelId);

			ApiResponseData<String> apiResponseData;
			if (appUserQueueRelId > 0) {
				String apiResponse = "User added to queue";
				apiResponseData = getStringApiResponseData(apiResponse, WorkbenchConstants.API_RESPONSE_CDE_SUCCESS,
						WorkbenchConstants.API_RESPONSE_MSG_SUCCESS);
			} else {
				String apiResponse = "User is not added to queue";
				apiResponseData = getStringApiResponseData(apiResponse, WorkbenchConstants.API_RESPONSE_CDE_NO_RECORDS,
						WorkbenchConstants.API_RESPONSE_MSG_NO_RECORDS);
			}

			return jsonResponseOk(apiResponseData);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		} finally {
			List<EntityDbData> entityDbDataList = new ArrayList<EntityDbData>();
			EntityDbData entityDbData = new EntityDbData();
			entityDbData.setAppUserQueueRelIdList(appUserQueueRelIdList);
			entityDbDataList.add(entityDbData);
			auditProcess.addAuditDetails(entityDbDataList, EnumEntityType.QUEUE_ASSIGNMENT, EnumOperationType.INSERT);

		}
	}

	@ApiOperation(value = "Remove user from a queue", tags = "user")
	@RequestMapping(value = "/queue/{appUserQueueRelId}", method = RequestMethod.DELETE, produces = {
			MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE })
	ResponseEntity<String> deleteUserQueueRelationship(@PathVariable Long appUserQueueRelId) {
		long sqlResponseId = 0;
		List<Long> sqlResponseIdList = new ArrayList<Long>();

		try {
			sqlResponseId = userProcess.deleteUserFromQueue(appUserQueueRelId);
			String apiResponse = "";
			ApiResponseData<String> apiResponseData;

			if (sqlResponseId > 0) {
				sqlResponseIdList.add(sqlResponseId);
				apiResponse = sqlResponseId + " record was deleted";
				apiResponseData = getStringApiResponseData(apiResponse, WorkbenchConstants.API_RESPONSE_CDE_SUCCESS,
						WorkbenchConstants.API_RESPONSE_MSG_SUCCESS);
			} else {
				apiResponse = sqlResponseId + "is an invalid input";
				apiResponseData = getStringApiResponseData(apiResponse, WorkbenchConstants.API_RESPONSE_CDE_NO_RECORDS,
						WorkbenchConstants.API_RESPONSE_MSG_NO_RECORDS);
			}

			return jsonResponseOk(apiResponseData);

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		} finally {
			List<EntityDbData> entityDbDataList = new ArrayList<EntityDbData>();
			EntityDbData entityDbData = new EntityDbData();
			entityDbData.setAppUserQueueRelIdList(sqlResponseIdList);
			entityDbDataList.add(entityDbData);
			auditProcess.addAuditDetails(entityDbDataList, EnumEntityType.QUEUE_ASSIGNMENT, EnumOperationType.DELETE);

		}
	}

	@ApiOperation(value = "Get list of queues for user", tags = "user")
	@RequestMapping(value = "/queue", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	ResponseEntity<String> getQueueListForUser(@RequestParam(value = "appUserId", required = true) Integer appUserId) {
		ApiResponseData<List<UserQueueResData>> apiResponseData = new ApiResponseData<List<UserQueueResData>>();
		try {

			if (appUserId != null && appUserId == 0) {
				String validationMessage = "appUserId is mandatory";
				return jsonResponseOk(getStringApiResponseData(validationMessage,
						WorkbenchConstants.API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED, WorkbenchConstants.API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED));
			}
			List<UserQueueResData> userQueueResDataList = userProcess.getUserQueueDetails(appUserId);
			apiResponseData.setResponse(userQueueResDataList);
			apiResponseData.setResponseCde(WorkbenchConstants.API_RESPONSE_CDE_SUCCESS);
			apiResponseData.setResponseMsg(WorkbenchConstants.API_RESPONSE_MSG_SUCCESS);

			abstract class MixIn {
				@JsonIgnore
				abstract long getAppUserId();
			}
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.addMixIn(UserQueueResData.class, MixIn.class);
			return jsonResponseOk(apiResponseData, objectMapper);

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		}
	}

	private InsertUserResData validateUserCreationData(InsertUserReqData addUserRequestData) {
		String errorMessage = "";
		int errorCde = 0;
		if (!StringUtility.hasValue(addUserRequestData.getUserName())) {
			errorMessage = "Username is mandatory";
			errorCde = 11;
		} else if (!StringUtility.hasValue(addUserRequestData.getUserPassword())) {
			errorMessage = "Password is mandatory";
			errorCde = 12;
		} else if (!StringUtility.hasValue(addUserRequestData.getUserFullName())) {
			errorMessage = "User full name is mandatory";
			errorCde = 13;
		} else if (!StringUtility.hasValue(addUserRequestData.getUserEmail())) {
			errorMessage = "User email is mandatory";
			errorCde = 14;
		} else {
			if (!PatternUtility.isValidUserName(addUserRequestData.getUserName())) {
				errorMessage = "Username has special characters. Only [a-zA-Z0-9] allowed";
				errorCde = 15;
			}
			if (!StringUtility.hasValue(errorMessage)) {
//				TO-DO Uncomment the code for deployment
//				if (!PatternUtility.isValidPassword(addUserRequestData.getUserPassword())) {
//					errorMessage = "Password must be of 6 characters & should contains a combination of uppercase, lowercase letter, special character & number.";
//					errorCde = 16;
//				}
			}
			if (!StringUtility.hasValue(errorMessage)) {
				boolean isEmaild = PatternUtility.isValidEmailId(addUserRequestData.getUserEmail());
				if (!isEmaild) {
					errorMessage = "Invalid E-mail ID";
					errorCde = 17;
				}
			}
		}
		InsertUserResData insertUserResData = new InsertUserResData();
		insertUserResData.setErrCde(errorCde);
		insertUserResData.setErrTxt(errorMessage);
		return insertUserResData;
	}

	private String validateUserQueueRelCreationData(InsertUserQueueReqData insertUserQueueReqData)
			throws WorkbenchException {
		String errorMessage = "";
		List<UserQueueResData> userQueueResDataList = userProcess
				.getUserQueueDetails(insertUserQueueReqData.getAppUserId());

		Optional<UserQueueResData> userQueueDbDataOptional = userQueueResDataList.stream()
				.filter(a -> a.getQueueNameCde() == insertUserQueueReqData.getQueueNameCde()).findFirst();
		if (userQueueDbDataOptional.isPresent()) {
			errorMessage = "User is already added to queue";
		}
		return errorMessage;
	}

	@ApiOperation(value = "Get list of users", tags = "user")
	@RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	ResponseEntity<String> getListOfUsers() {
		ApiResponseData<List<UserResData>> apiResponseData = new ApiResponseData<List<UserResData>>();
		try {

			List<UserResData> userResDataList = userProcess.getUserListDetails();
			apiResponseData.setResponse(userResDataList);
			apiResponseData.setResponseCde(WorkbenchConstants.API_RESPONSE_CDE_SUCCESS);
			apiResponseData.setResponseMsg(WorkbenchConstants.API_RESPONSE_MSG_SUCCESS);

			abstract class MixIn {

				@JsonIgnore
				abstract long getAppUserId();

				@JsonIgnore
				abstract String getUserPassword();

				@JsonIgnore
				abstract long getListAppUserId();

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

	@ApiOperation(value = "Update account enabled status", tags = "user")
	@RequestMapping(value = "", method = RequestMethod.PUT, consumes = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE,
					MediaType.APPLICATION_JSON_VALUE })
	ResponseEntity<String> updateUserAccountEnabler(@RequestBody UpdateUserReqData updateUserReqData) {
		EntityDbData entityDbData = new EntityDbData();
		try {
			if (updateUserReqData.getUserId() <= 0) {
				String validationMessage = "Please enter a valid User id";
				return jsonResponseOk(getStringApiResponseData(validationMessage,
						WorkbenchConstants.API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED, WorkbenchConstants.API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED));

			}

			entityDbData = userProcess.updateUserAccountEnabled(updateUserReqData);

			String apiResponse = entityDbData.getAppUserId() + " appUserId was updated";
			ApiResponseData<String> apiResponseData;

			if (entityDbData.getAppUserId() > 0) {
				apiResponseData = getStringApiResponseData(apiResponse, WorkbenchConstants.API_RESPONSE_CDE_SUCCESS,
						WorkbenchConstants.API_RESPONSE_MSG_SUCCESS);
			} else {
				apiResponseData = getStringApiResponseData(apiResponse, WorkbenchConstants.API_RESPONSE_CDE_NO_RECORDS,
						WorkbenchConstants.API_RESPONSE_MSG_NO_RECORDS);
			}

			return jsonResponseOk(apiResponseData);

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		} finally {
			List<EntityDbData> entityDbDataList = new ArrayList<EntityDbData>();
			entityDbDataList.add(entityDbData);
			auditProcess.addAuditDetails(entityDbDataList, EnumEntityType.USER, EnumOperationType.UPDATE);

		}
	}
	
	private ApiResponseData<InsertUserResData> populateApiResponseData(InsertUserResData insertUserResData, int responseCde, String responseTxt) {
		ApiResponseData<InsertUserResData> apiResponseData = new ApiResponseData<InsertUserResData>();
		apiResponseData.setResponse(insertUserResData);
		apiResponseData.setResponseCde(responseCde);
		apiResponseData.setResponseMsg(responseTxt);
		return apiResponseData;
	}

	@ApiOperation(value = "Get list of teammates of currently logged in user.", tags = "user")
	@RequestMapping(value = "/team/currentuser", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	ResponseEntity<String> getListOfTeammates() {
		ApiResponseData<List<UserTeammateResData>> apiResponseData = new ApiResponseData<List<UserTeammateResData>>();
		try {

			List<UserTeammateResData> userTeammateDataList = userProcess.getTeammateListDetails();
			apiResponseData.setResponse(userTeammateDataList);
			apiResponseData.setResponseCde(WorkbenchConstants.API_RESPONSE_CDE_SUCCESS);
			apiResponseData.setResponseMsg(WorkbenchConstants.API_RESPONSE_MSG_SUCCESS);

			abstract class MixIn {

				@JsonIgnore
				abstract long getAppUserId();

				@JsonIgnore
				abstract String getUserPassword();

				@JsonIgnore
				abstract long getListAppUserId();
				
				@JsonIgnore
				abstract long getAppUserQueueRelId(); 
				
				@JsonIgnore
				abstract String getQueue_closed_dtm();
				
				@JsonIgnore
				abstract String getQueue_status();
				
				@JsonIgnore
				abstract String getQueue_hide_after_dtm();
				
				@JsonIgnore
				abstract String getUser_queue_hide_after_dtm();

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
}
