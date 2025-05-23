/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.controller.api.action;

import java.util.ArrayList;
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
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.docwb.service.common.WorkbenchConstants;
import com.infosys.ainauto.docwb.service.common.type.EnumEntityType;
import com.infosys.ainauto.docwb.service.common.type.EnumOperationType;
import com.infosys.ainauto.docwb.service.common.type.EnumTaskStatus;
import com.infosys.ainauto.docwb.service.controller.api.BaseController;
import com.infosys.ainauto.docwb.service.model.api.ActionResData;
import com.infosys.ainauto.docwb.service.model.api.ApiResponseData;
import com.infosys.ainauto.docwb.service.model.api.DocumentResData;
import com.infosys.ainauto.docwb.service.model.api.PaginationApiResponseData;
import com.infosys.ainauto.docwb.service.model.api.PaginationResData;
import com.infosys.ainauto.docwb.service.model.api.ParamAttrResData;
import com.infosys.ainauto.docwb.service.model.api.ParamResData;
import com.infosys.ainauto.docwb.service.model.api.RecommendedActionResData;
import com.infosys.ainauto.docwb.service.model.api.action.GetActionReqData;
import com.infosys.ainauto.docwb.service.model.api.action.InsertActionReqData;
import com.infosys.ainauto.docwb.service.model.api.action.UpdateActionReqData;
import com.infosys.ainauto.docwb.service.model.api.document.InsertDocEventReqData;
import com.infosys.ainauto.docwb.service.model.api.document.UpdateDocStatusReqData;
import com.infosys.ainauto.docwb.service.model.db.ActionParamAttrMappingDbData;
import com.infosys.ainauto.docwb.service.model.db.EntityDbData;
import com.infosys.ainauto.docwb.service.process.action.IActionProcess;
import com.infosys.ainauto.docwb.service.process.audit.IAuditProcess;
import com.infosys.ainauto.docwb.service.process.auth.IApiRoleAuthorizationProcess;
import com.infosys.ainauto.docwb.service.process.doc.IDocumentProcess;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Controller
@CrossOrigin
@RequestMapping("/api/v1/action")
@Api(tags = { "action" })
public class ActionController extends BaseController {

	private static final Logger logger = LoggerFactory.getLogger(ActionController.class);

	@Autowired
	private IActionProcess actionProcess;
	@Autowired
	private IAuditProcess auditProcess;
	@Autowired
	private IDocumentProcess docProcess;
	@Autowired
	private IApiRoleAuthorizationProcess apiRoleAuthorizationProcess;

	@ApiOperation(value = "Add new action to document", tags = "action")
	@RequestMapping(value = "", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE,
					MediaType.APPLICATION_JSON_VALUE })
	ResponseEntity<String> insertAction(@RequestBody List<InsertActionReqData> requestDataList) {
		ApiResponseData<List<InsertActionReqData>> apiResponseData = new ApiResponseData<List<InsertActionReqData>>();
		List<Long> docActionRelIdList = new ArrayList<Long>();
		List<EntityDbData> actionEntityDataList = new ArrayList<EntityDbData>();
		List<EntityDbData> docEntityDataList = new ArrayList<EntityDbData>();
		try {
			docActionRelIdList = actionProcess.insertActions(requestDataList);
			// Adding the task status and doc event while adding action to the document
			// while working on issue #30 so that it reflects under the right queue.

			InsertDocEventReqData docEventReqData = new InsertDocEventReqData();
			UpdateDocStatusReqData docStatusReqData = new UpdateDocStatusReqData();

			if (ListUtility.hasValue(docActionRelIdList)) {
				EntityDbData entityData = new EntityDbData();
				entityData.setDocActionRelIdList(docActionRelIdList);
				actionEntityDataList.add(entityData);
				for (int i = 0; i < requestDataList.size(); i++) {
					docEventReqData.setEventTypeCde(WorkbenchConstants.EVENT_TYPE_CDE_ACTION_CREATED);
					docEventReqData.setDocId(requestDataList.get(i).getDocId());

					docStatusReqData.setDocId(requestDataList.get(i).getDocId());
					docStatusReqData.setTaskStatusCde(EnumTaskStatus.IN_PROGRESS.getValue());

					docProcess.insertDocEventType(docEventReqData);
					List<EntityDbData> data = docProcess.updateDocActionStatus(docStatusReqData);
					if (ListUtility.hasValue(data))
						docEntityDataList.add(data.get(0));
				}
			}

			apiResponseData.setResponse(null);
			return jsonResponseOk(apiResponseData);

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		} finally {

			auditProcess.addAuditDetails(actionEntityDataList, EnumEntityType.ACTION, EnumOperationType.INSERT);
			auditProcess.addAuditDetails(docEntityDataList, EnumEntityType.DOCUMENT, EnumOperationType.UPDATE);

		}
	}

	@ApiOperation(value = "Returns mapping details of action's parameters with document's attributes", tags = "action")
	@RequestMapping(value = "/mapping", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	ResponseEntity<String> getActionMapping() {
		ApiResponseData<List<ActionResData>> apiResponseData = new ApiResponseData<List<ActionResData>>();
		try {
			List<ActionResData> resultList = actionProcess.getActionMappingList();
			apiResponseData.setResponse(resultList);

			abstract class MixIn {
				@JsonIgnore
				abstract boolean getDocId();

				@JsonIgnore
				abstract boolean getAttrValue();

				@JsonIgnore
				abstract String getParamValue();

				@JsonIgnore
				abstract boolean getDocActionRelId();

				@JsonIgnore
				abstract boolean getTaskStatusCde();

				@JsonIgnore
				abstract boolean getTaskStatusTxt();

				@JsonIgnore
				abstract List<ParamResData> getParamList();
			}
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.addMixIn(DocumentResData.class, MixIn.class);
			objectMapper.addMixIn(ActionResData.class, MixIn.class);
			objectMapper.addMixIn(ParamAttrResData.class, MixIn.class);

			return jsonResponseOk(apiResponseData, objectMapper);

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		}
	}

	@ApiOperation(value = "Returns data for mapping details of action's parameters with document's attributes", tags = "action")
	@RequestMapping(value = "/param/attr/recommendation", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	ResponseEntity<String> getAction(@RequestParam(value = "actionNameCde", required = true) int actionNameCde,
			@RequestParam(value = "docId", required = true) long docId) {
		ApiResponseData<List<ActionResData>> apiResponseData = new ApiResponseData<List<ActionResData>>();
		try {
			List<ActionResData> resultList = actionProcess.getActionData(actionNameCde, docId);
			apiResponseData.setResponse(resultList);

			abstract class MixIn {
				@JsonIgnore
				abstract boolean getDocId();

				@JsonIgnore
				abstract boolean getDocActionRelId();

				@JsonIgnore
				abstract boolean getTaskStatusCde();

				@JsonIgnore
				abstract boolean getTaskStatusTxt();

				@JsonIgnore
				abstract String getParamValue();

				@JsonIgnore
				abstract List<ParamResData> getParamList();
			}
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.addMixIn(DocumentResData.class, MixIn.class);
			objectMapper.addMixIn(ActionResData.class, MixIn.class);
			objectMapper.addMixIn(ParamAttrResData.class, MixIn.class);

			return jsonResponseOk(apiResponseData, objectMapper);

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		}
	}

	@ApiOperation(value = "Update action status", tags = "action")
	@RequestMapping(value = "", method = RequestMethod.PUT, consumes = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE,
					MediaType.APPLICATION_JSON_VALUE })
	ResponseEntity<String> updateTaskList(@RequestBody List<UpdateActionReqData> requestDataList) {
		List<EntityDbData> entityDataList = new ArrayList<EntityDbData>();
		ApiResponseData<List<UpdateActionReqData>> apiResponseData = new ApiResponseData<List<UpdateActionReqData>>();
		try {
			entityDataList = actionProcess.updateActionTaskList(requestDataList);
			apiResponseData.setResponse(null);
			abstract class MixIn {
				@JsonIgnore
				abstract boolean getActionParamAttrMapId(); // we don't need it!
			}
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.addMixIn(ActionParamAttrMappingDbData.class, MixIn.class);

			return jsonResponseOk(apiResponseData, objectMapper);

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		} finally {

			auditProcess.addAuditDetails(entityDataList, entityDataList, EnumEntityType.ACTION,
					EnumOperationType.UPDATE);

		}

	}

	@ApiOperation(value = "Get list of actions along with details", tags = "action")
	@RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	ResponseEntity<String> getTaskList(@RequestParam(value = "queueNameCde", required = false) Integer queueNameCde,
			@RequestParam(value = "docId", required = false) Long docId,
			@RequestParam(value = "actionNameCde", required = false) Integer actionNameCde,
			@RequestParam(value = "pageNumber", required = false) Integer pageNumber,
			@RequestParam(value = "taskStatusCde", required = false) Integer taskStatusCde,
			@RequestParam(value = "taskStatusOperator", required = false) String taskStatusOperator) {
		PaginationApiResponseData<List<DocumentResData>> apiResponseData = new PaginationApiResponseData<List<DocumentResData>>();
		try {
			boolean isActionViewReq = false;
			GetActionReqData getActionReqData = new GetActionReqData();
			// Set default values
			getActionReqData.setQueueNameCde(0);
			getActionReqData.setTaskStatusOperator("=");

			if (queueNameCde != null) {
				getActionReqData.setQueueNameCde(queueNameCde);
			}
			// "action-list" means the response can contain data spanning multiple cases 
			// "action-view" means the response can contain data only for a particular case
			if (docId != null) {
				getActionReqData.setDocId(docId);
				isActionViewReq=true;
			}
			if (pageNumber != null) {
				getActionReqData.setPageNumber(pageNumber);
			}
			if (actionNameCde != null) {
				getActionReqData.setActionNameCde(actionNameCde);
			}
			if (taskStatusCde != null) {
				getActionReqData.setTaskStatusCde(taskStatusCde);
			}
			if (taskStatusOperator != null) {
				getActionReqData.setTaskStatusOperator(taskStatusOperator);
			}
			
			boolean isFeatureAllowed = false;
			if (isActionViewReq && apiRoleAuthorizationProcess
					.isFeatureAccessAllowed(WorkbenchConstants.FEATURE_ID_ACTION_VIEW)) {
				isFeatureAllowed = true;
			} else if (!isActionViewReq
					&& apiRoleAuthorizationProcess.isFeatureAccessAllowed(WorkbenchConstants.FEATURE_ID_ACTION_LIST)) {
				isFeatureAllowed = true;
			}
			
			if (!isFeatureAllowed) {
				return jsonResponseForbidden();
			}
			

			PaginationResData paginationResData = actionProcess.getPaginationForActions(getActionReqData);

			List<DocumentResData> resultList = actionProcess.getActionTaskList(getActionReqData);

			apiResponseData.setResponse(resultList);
			apiResponseData.setPagination(paginationResData);

			abstract class MixIn {
				@JsonIgnore
				abstract List<ParamAttrResData> getMappingList();
			}
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.addMixIn(ActionResData.class, MixIn.class);

			return jsonResponseOk(apiResponseData, objectMapper);

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		}

	}

	@ApiOperation(value = "Delete an action", tags = "action")
	@RequestMapping(value = "/{docActionRelId}", method = RequestMethod.DELETE, produces = {
			MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE })
	ResponseEntity<String> deleteAction(@PathVariable Long docActionRelId) {
		List<Long> docActionRelIdList = new ArrayList<Long>();
		List<EntityDbData> entityData = new ArrayList<EntityDbData>();
		try {
			Long docActionRelIdOut = actionProcess.deleteAction(docActionRelId);
			if (docActionRelIdOut > 0) {
				docActionRelIdList.add(docActionRelIdOut);
			}
			String apiResponse = docActionRelIdList.size() + " Records(s) deleted";
			ApiResponseData<String> apiResponseData;

			if (docActionRelIdList.size() > 0) {
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
			EntityDbData entityDbData = new EntityDbData();
			entityDbData.setDocActionRelIdList(docActionRelIdList);
			entityData.add(entityDbData);
			auditProcess.addAuditDetails(entityData, EnumEntityType.ACTION, EnumOperationType.DELETE);

		}
	}

	@ApiOperation(value = "Get recommended action based on business rules configured", tags = "action")
	@RequestMapping(value = "/recommendation", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	ResponseEntity<String> getRecommendedActionByDocID(@RequestParam(value = "docId", required = true) long docId) {
		try {
			ApiResponseData<RecommendedActionResData> apiResponseData = new ApiResponseData<RecommendedActionResData>();
			RecommendedActionResData result = actionProcess.getRecommendedAction(docId);
			apiResponseData.setResponse(result);
			return jsonResponseOk(apiResponseData);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		}
	}

}