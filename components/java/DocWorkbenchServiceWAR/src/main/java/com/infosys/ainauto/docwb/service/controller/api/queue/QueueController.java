/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.controller.api.queue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infosys.ainauto.commonutils.DateUtility;
import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.service.common.WorkbenchConstants;
import com.infosys.ainauto.docwb.service.common.type.EnumEntityType;
import com.infosys.ainauto.docwb.service.common.type.EnumOperationType;
import com.infosys.ainauto.docwb.service.controller.api.BaseController;
import com.infosys.ainauto.docwb.service.model.api.ApiResponseData;
import com.infosys.ainauto.docwb.service.model.api.QueueCountResData;
import com.infosys.ainauto.docwb.service.model.api.UserQueueResData;
import com.infosys.ainauto.docwb.service.model.api.UserResData;
import com.infosys.ainauto.docwb.service.model.api.queue.AddQueueReqData;
import com.infosys.ainauto.docwb.service.model.api.queue.AddQueueResData;
import com.infosys.ainauto.docwb.service.model.api.queue.UpdateQueueReqData;
import com.infosys.ainauto.docwb.service.model.api.queue.UpdateUserQueueReqData;
import com.infosys.ainauto.docwb.service.model.db.EntityDbData;
import com.infosys.ainauto.docwb.service.model.db.ValTableDbData;
import com.infosys.ainauto.docwb.service.process.audit.IAuditProcess;
import com.infosys.ainauto.docwb.service.process.auth.IApiRoleAuthorizationProcess;
import com.infosys.ainauto.docwb.service.process.queue.IQueueProcess;
import com.infosys.ainauto.docwb.service.process.user.UserProcess;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Controller
@CrossOrigin
@RequestMapping("/api/v1/queue")
@Api(tags = { "queue" })

public class QueueController extends BaseController {

	private static final Logger logger = LoggerFactory.getLogger(QueueController.class);

	@Autowired
	private IQueueProcess queueProcess;

	@Autowired
	private UserProcess userProcess;

	@Autowired
	private IAuditProcess auditProcess;

	@Autowired
	private IApiRoleAuthorizationProcess apiRoleAuthorizationProcess;

	@ApiOperation(value = "Get count of documents in queue under different status", tags = "queue")
	@RequestMapping(value = "/stats", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	ResponseEntity<String> getDocumentCount(
			@RequestParam(value = "queueNameCde", required = false) Integer queueNameCde,
			@RequestParam(value = "assignmentCount", required = false) boolean assignmentCount,
			@RequestParam(value = "assignedTo", required = false) String assignedTo) {
		try {
			UserResData loggedInUserDetails = userProcess.getLoggedInUserDetails();

			String validationMessage = "";
			ApiResponseData<List<QueueCountResData>> apiResponseData;

			if (queueNameCde != null) {
				List<Integer> queueNameCdeList = new ArrayList<Integer>();
				List<UserQueueResData> queueDataList = loggedInUserDetails.getQueueDataList();
				for (int i = 0; i < queueDataList.size(); i++) {
					queueNameCdeList.add(queueDataList.get(i).getQueueNameCde());
				}
				if (queueNameCdeList.contains(queueNameCde)) {
					apiResponseData = new ApiResponseData<List<QueueCountResData>>();
					List<QueueCountResData> docCount = queueProcess.getDocCount(queueNameCde, assignmentCount);
					apiResponseData.setResponse(docCount);
					apiResponseData.setResponseCde(WorkbenchConstants.API_RESPONSE_CDE_SUCCESS);
					apiResponseData.setResponseMsg(WorkbenchConstants.API_RESPONSE_MSG_SUCCESS);

				} else {
					apiResponseData = new ApiResponseData<List<QueueCountResData>>();
					validationMessage = "You are not authorized to access this.";

					return jsonResponseOk(getStringApiResponseData(validationMessage,
							WorkbenchConstants.API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED,
							WorkbenchConstants.API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED));
				}

			} else {
				long appUserId = loggedInUserDetails.getUserId();
				List<Long> assignedToList = null;
				if (StringUtility.hasValue(assignedTo)) {
					assignedToList = Arrays.asList(assignedTo.split(",")).stream().map(x -> Long.valueOf(x))
							.collect(Collectors.toList());
				}
				apiResponseData = new ApiResponseData<List<QueueCountResData>>();
				List<QueueCountResData> docCount = queueProcess.getDocCountForUser(appUserId, assignmentCount,
						assignedToList);
				if (docCount.isEmpty()) {
					apiResponseData.setResponse(docCount);
					apiResponseData.setResponseMsg(WorkbenchConstants.API_RESPONSE_MSG_NO_RECORDS);
					apiResponseData.setResponseCde(WorkbenchConstants.API_RESPONSE_CDE_SUCCESS);
					return jsonResponseOk(apiResponseData);

				} else {
					apiResponseData.setResponse(docCount);
					apiResponseData.setResponseMsg(WorkbenchConstants.API_RESPONSE_MSG_SUCCESS);
					apiResponseData.setResponseCde(WorkbenchConstants.API_RESPONSE_CDE_SUCCESS);
				}
			}
			if (assignmentCount == false) {
				abstract class MixIn {
//					@JsonIgnore
//					abstract long getAssignedCount();
//					@JsonIgnore
//					abstract long getUnassignedCount();
					@JsonIgnore
					abstract long getMyCasesCount();
				}
				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.addMixIn(QueueCountResData.class, MixIn.class);
				return jsonResponseOk(apiResponseData, objectMapper);
			} else {
				abstract class MixIn {
					@JsonIgnore
					abstract long getDocCount();
				}
				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.addMixIn(QueueCountResData.class, MixIn.class);
				return jsonResponseOk(apiResponseData, objectMapper);
			}

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		}
	}

	@ApiOperation(value = "Get queue users list", tags = "queue")
	@RequestMapping(value = "/users", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	ResponseEntity<String> getListOfQueueUsers(
			@RequestParam(value = "queueNameCde", required = true) Integer queueNameCde,
			@RequestParam(value = "docRoleTypeCde", required = false) Integer docRoleTypeCde) {
//		docRoleTypeCde = 1(caseowner)/ 2(casereviewer)
		try {
			List<Long> allowedUserRoleList = null;
			if (docRoleTypeCde!=null) {
				String featureId = "";
				if (docRoleTypeCde == 1) {
					featureId = WorkbenchConstants.FEATURE_ID_CASE_OWN_USER_ALLOW;
				} else if (docRoleTypeCde == 2) {
					featureId = WorkbenchConstants.FEATURE_ID_CASE_REVIEW_USER_ALLOW;
				}
				allowedUserRoleList = apiRoleAuthorizationProcess.getFeatureAllowedRoleTypeCde(featureId);
			}
			ApiResponseData<List<UserResData>> apiResponseData = new ApiResponseData<List<UserResData>>();
			List<UserResData> userResDataList = queueProcess.getQueueUsersList(queueNameCde, allowedUserRoleList);
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

				@JsonIgnore
				abstract List<UserQueueResData> getQueueDataList();

				@JsonIgnore
				abstract String getUserEmail();

				@JsonIgnore
				abstract String getUserName();

				@JsonIgnore
				abstract String getUserTypeTxt();

				@JsonIgnore
				abstract String getUserTypeCde();

				@JsonIgnore
				abstract boolean isAccountEnabled();

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

	@ApiOperation(value = "Get list of queues", tags = "queue")
	@RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> getAttachmentListEmail() {
		List<ValTableDbData> resultList = new ArrayList<ValTableDbData>();
		try {
			ApiResponseData<List<ValTableDbData>> apiResponseData = new ApiResponseData<List<ValTableDbData>>();
			resultList = queueProcess.getQueues();
			apiResponseData.setResponse(resultList);
			abstract class MixIn {
				@JsonIgnore
				abstract boolean getCreateBy(); // we don't need it!

				@JsonIgnore
				abstract boolean getCreateDtm();

				@JsonIgnore
				abstract boolean getLastModBy();

				@JsonIgnore
				abstract boolean getLastModDtm();
			}
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.addMixIn(ValTableDbData.class, MixIn.class);
			return jsonResponseOk(apiResponseData, objectMapper);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		}
	}

	@ApiOperation(value = "Add a new queue", tags = "queue")
	@RequestMapping(value = "", method = RequestMethod.POST, consumes = {
			MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
	ResponseEntity<String> addQueue(@RequestBody AddQueueReqData requestData) {
		EntityDbData entityDbData = new EntityDbData();
		try {
			ApiResponseData<AddQueueResData> apiResponseData = new ApiResponseData<>();
			AddQueueResData addQueueResData = queueProcess.addQueue(requestData);

			if (addQueueResData.getQueueNameCde() == -1) {
				String validationMessage = "queueNameCde already exists";
				return jsonResponseOk(getStringApiResponseData(validationMessage,
						WorkbenchConstants.API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED,
						WorkbenchConstants.API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED));
			}

			apiResponseData.setResponse(addQueueResData);
			apiResponseData.setResponseCde(WorkbenchConstants.API_RESPONSE_CDE_SUCCESS);
			apiResponseData.setResponseMsg(WorkbenchConstants.API_RESPONSE_MSG_SUCCESS);

			entityDbData.setQueueNameCde(addQueueResData.getQueueNameCde());

			return jsonResponseOk(apiResponseData);

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		} finally {
			List<EntityDbData> entityDbDataList = new ArrayList<EntityDbData>();
			entityDbDataList.add(entityDbData);
			auditProcess.addAuditDetails(entityDbDataList, EnumEntityType.QUEUE, EnumOperationType.INSERT);

		}
	}

	@ApiOperation(value = "Get personal queue visibility details", tags = "queue")
	@RequestMapping(value = "/currentuser", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<String> getQueueListForCurrentUser(
			@RequestParam(value = "queueStatus", required = true) String queueStatus) {
		ApiResponseData<List<UserQueueResData>> apiResponseData = new ApiResponseData<List<UserQueueResData>>();
		try {
			if (!StringUtility.hasTrimmedValue(queueStatus)) {
				queueStatus = "OPEN";
			}
			if (!(queueStatus.equalsIgnoreCase("OPEN") || queueStatus.equalsIgnoreCase("CLOSED"))) {
				String validationMessage = "queueStatus can only be either OPEN or CLOSED ";
				return jsonResponseOk(getStringApiResponseData(validationMessage,
						WorkbenchConstants.API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED,
						WorkbenchConstants.API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED));
			}

			List<UserQueueResData> queueListCurrentUser = queueProcess.getQueueListForCurrentUser(queueStatus);
			apiResponseData.setResponse(queueListCurrentUser);
			apiResponseData.setResponseCde(WorkbenchConstants.API_RESPONSE_CDE_SUCCESS);
			apiResponseData.setResponseMsg(WorkbenchConstants.API_RESPONSE_MSG_SUCCESS);
			abstract class MixIn {
				@JsonIgnore
				abstract long getAppUserId();

				@JsonIgnore
				abstract long getAppUserQueueRelId();

				@JsonIgnore
				abstract long getDocTypeCde();

				@JsonIgnore
				abstract long getDocTypeTxt();

			}
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.addMixIn(UserQueueResData.class, MixIn.class);
			return jsonResponseOk(apiResponseData, objectMapper);

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		}
	}

	@ApiOperation(value = "Save personal queue visibility details", tags = "queue")
	@RequestMapping(value = "/currentuser", method = RequestMethod.PUT, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_JSON_VALUE })
	ResponseEntity<String> updatePersonalQueueVisibility(
			@RequestBody List<UpdateUserQueueReqData> updateUserQueueReqDataList) {
		ApiResponseData<String> apiResponseData = new ApiResponseData<String>();
		try {

			EntityDbData entityDbData = new EntityDbData();
			Date date = null;
			long updatedRowCount = 0;
			for (UpdateUserQueueReqData updateUserQueueReqDataData : updateUserQueueReqDataList) {
				String queue_hide_after_dtm = updateUserQueueReqDataData.getUserQueueHideAfterDtm();
				if (StringUtility.hasTrimmedValue(queue_hide_after_dtm)) {
					date = DateUtility.toTimestamp(queue_hide_after_dtm, WorkbenchConstants.TIMESTAMP_FORMAT_24HR);
					String validationMessage = "Date should be in one of the following formats-'yyyy-MM-dd hh:mm:ss' OR 'yyyy-MM-dd'";
					if (date == null) {
						date = DateUtility.toTimestamp(queue_hide_after_dtm, WorkbenchConstants.API_DATE_FORMAT);
						if (date == null)
							return jsonResponseOk(getStringApiResponseData("queue_hide_after_dtm:" + validationMessage,
									WorkbenchConstants.API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED,
									WorkbenchConstants.API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED));
					}
				}
			}
			entityDbData = queueProcess.updatePersonalQueueVisibility(updateUserQueueReqDataList);
			updatedRowCount = entityDbData.getUpdatedRowCount();
			String apiResponse = entityDbData.getApiResponseData();

			if (updatedRowCount > 0) {
				apiResponseData.setResponse(apiResponse);
				apiResponseData.setResponseCde(WorkbenchConstants.API_RESPONSE_CDE_SUCCESS);
				apiResponseData.setResponseMsg(WorkbenchConstants.API_RESPONSE_MSG_SUCCESS);
			} else {
				apiResponseData.setResponse(apiResponse);
				apiResponseData.setResponseCde(WorkbenchConstants.API_RESPONSE_CDE_FAILURE);
				apiResponseData.setResponseMsg(WorkbenchConstants.API_RESPONSE_MSG_FAILURE);
			}

			return jsonResponseOk(apiResponseData);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		}

	}

	@ApiOperation(value = "update queue details", tags = "queue")
	@RequestMapping(value = "", method = RequestMethod.PUT, produces = { MediaType.APPLICATION_JSON_VALUE })
	ResponseEntity<String> updateQueueDetails(@RequestBody List<UpdateQueueReqData> updateQueueReqDataList) {
		ApiResponseData<String> apiResponseData = new ApiResponseData<String>();
		try {

			EntityDbData entityDbData = new EntityDbData();
			Date date = null;
			Date queueHideAfterDate = null;
			long updatedRowCount = 0;
			for (UpdateQueueReqData updateQueueReqData : updateQueueReqDataList) {
				String end_dtm = updateQueueReqData.getEndDtm();
				String queueHideAfterDtm = updateQueueReqData.getQueueHideAfterDtm();

				if (StringUtility.hasTrimmedValue(end_dtm)) {
					date = DateUtility.toTimestamp(end_dtm, WorkbenchConstants.TIMESTAMP_FORMAT_24HR);
					String validationMessage = "end_dtm should be in one of the following formats-'yyyy-MM-dd hh:mm:ss' OR 'yyyy-MM-dd'";
					if (date == null) {
						date = DateUtility.toTimestamp(end_dtm, WorkbenchConstants.API_DATE_FORMAT);
						if (date == null)
							return jsonResponseOk(getStringApiResponseData("queue_hide_after_dtm:" + validationMessage,
									WorkbenchConstants.API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED,
									WorkbenchConstants.API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED));
					}
				}
				if (StringUtility.hasTrimmedValue(queueHideAfterDtm)) {
					queueHideAfterDate = DateUtility.toTimestamp(queueHideAfterDtm,
							WorkbenchConstants.TIMESTAMP_FORMAT_24HR);
					String validationMessage = "queueHideAfterDtm should be in one of the following formats-'yyyy-MM-dd hh:mm:ss' OR 'yyyy-MM-dd'";
					if (queueHideAfterDate == null) {
						queueHideAfterDate = DateUtility.toTimestamp(queueHideAfterDtm,
								WorkbenchConstants.API_DATE_FORMAT);
						if (queueHideAfterDate == null)
							return jsonResponseOk(getStringApiResponseData("queue_hide_after_dtm:" + validationMessage,
									WorkbenchConstants.API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED,
									WorkbenchConstants.API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED));
					}
				}
			}
			entityDbData = queueProcess.updateQueueDetails(updateQueueReqDataList);
			updatedRowCount = entityDbData.getUpdatedRowCount();
			String apiResponse = entityDbData.getApiResponseData();

			if (updatedRowCount > 0) {
				apiResponseData.setResponse(apiResponse);
				apiResponseData.setResponseCde(WorkbenchConstants.API_RESPONSE_CDE_SUCCESS);
				apiResponseData.setResponseMsg(WorkbenchConstants.API_RESPONSE_MSG_SUCCESS);
			} else {
				apiResponseData.setResponse(apiResponse);
				apiResponseData.setResponseCde(WorkbenchConstants.API_RESPONSE_CDE_FAILURE);
				apiResponseData.setResponseMsg(WorkbenchConstants.API_RESPONSE_MSG_FAILURE);
			}

			return jsonResponseOk(apiResponseData);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		}
	}

}
