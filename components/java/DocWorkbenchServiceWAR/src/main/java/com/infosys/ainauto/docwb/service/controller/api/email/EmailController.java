/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.controller.api.email;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infosys.ainauto.docwb.service.common.WorkbenchConstants;
import com.infosys.ainauto.docwb.service.common.type.EnumEntityType;
import com.infosys.ainauto.docwb.service.common.type.EnumExtractType;
import com.infosys.ainauto.docwb.service.common.type.EnumOperationType;
import com.infosys.ainauto.docwb.service.common.type.EnumTaskStatus;
import com.infosys.ainauto.docwb.service.controller.api.BaseController;
import com.infosys.ainauto.docwb.service.model.api.ApiResponseData;
import com.infosys.ainauto.docwb.service.model.api.DocumentResData;
import com.infosys.ainauto.docwb.service.model.api.EmailResData;
import com.infosys.ainauto.docwb.service.model.api.PaginationApiResponseData;
import com.infosys.ainauto.docwb.service.model.api.PaginationResData;
import com.infosys.ainauto.docwb.service.model.api.UserResData;
import com.infosys.ainauto.docwb.service.model.api.attachment.AttachmentReqData;
import com.infosys.ainauto.docwb.service.model.api.email.InsertEmailReqData;
import com.infosys.ainauto.docwb.service.model.api.email.InsertUpdateDraftReqData;
import com.infosys.ainauto.docwb.service.model.api.email.UpdateEmailStatusReqData;
import com.infosys.ainauto.docwb.service.model.db.ActionParamAttrMappingDbData;
import com.infosys.ainauto.docwb.service.model.db.EmailDbData;
import com.infosys.ainauto.docwb.service.model.db.EntityDbData;
import com.infosys.ainauto.docwb.service.process.audit.IAuditProcess;
import com.infosys.ainauto.docwb.service.process.email.IEmailProcess;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;;

@Controller
@CrossOrigin
@RequestMapping("/api/v1/email")
@Api(tags = { "email" })
public class EmailController extends BaseController {

	private static final Logger logger = LoggerFactory.getLogger(EmailController.class);

	@Autowired
	private IAuditProcess auditProcess;

	@Autowired
	private IEmailProcess emailProcess;

	
	@ApiOperation(value = "Add outbound email", tags = "email")
	@RequestMapping(value = "", method = RequestMethod.POST, produces = "application/json")
	ResponseEntity<String> insertOutboundEmail(@RequestPart("emailData") InsertEmailReqData requestData,
			@RequestPart(value = "file1", required = false) MultipartFile file1,
			@RequestPart(value = "file2", required = false) MultipartFile file2,
			@RequestPart(value = "file3", required = false) MultipartFile file3,
			@RequestPart(value = "file4", required = false) MultipartFile file4,
			@RequestPart(value = "file5", required = false) MultipartFile file5) {

		EntityDbData entityDbData = new EntityDbData();
		List<EntityDbData> entityDataList = new ArrayList<EntityDbData>();
		try {

			List<MultipartFile> files = new ArrayList<MultipartFile>();
			files.add(file1);
			files.add(file2);
			files.add(file3);
			files.add(file4);
			files.add(file5);

			AttachmentReqData attachmentRequestData = new AttachmentReqData();

			attachmentRequestData.setMultipartFileList(files);
			attachmentRequestData.setDocId((int) requestData.getDocId());
			List<Integer> extractTypeCdeList = new ArrayList<>();
			extractTypeCdeList.add(EnumExtractType.DIRECT_COPY.getValue());
			attachmentRequestData.setExtractTypeCdeList(extractTypeCdeList);

			entityDbData = emailProcess.sendEmail(requestData, attachmentRequestData);
			String responseMsg = entityDbData.getOutboundEmailMessage();

			ApiResponseData<List<DocumentResData>> apiResponseData = new ApiResponseData<List<DocumentResData>>();
			apiResponseData.setResponseMsg(responseMsg);
			abstract class MixIn {

				@JsonIgnore
				abstract boolean getActionParamAttrMapId(); // we don't need it!

				@JsonIgnore
				abstract long getTaskStatusCde();
			}
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.addMixIn(ActionParamAttrMappingDbData.class, MixIn.class);
			objectMapper.addMixIn(InsertEmailReqData.class, MixIn.class);
			String json = objectMapper.writeValueAsString(apiResponseData);
			return new ResponseEntity<String>(json, HttpStatus.OK);

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return new ResponseEntity<String>(null, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
		} finally {
			EntityDbData data = new EntityDbData();

			data.setEmailIdList(entityDbData.getEmailIdList());
			data.setTaskStatusCde(entityDbData.getTaskStatusCde());

			if (entityDbData.getTaskStatusCde() == EnumTaskStatus.YET_TO_START.getValue()) {
				data.setAttachmentCount(entityDbData.getAttachmentCount());
				entityDataList.add(data);
				auditProcess.addAuditDetails(entityDataList, EnumEntityType.EMAIL, EnumOperationType.INSERT);
			} else {
				if (entityDbData.isDraft()) {
					entityDataList.add(data);
					auditProcess.addAuditDetails(entityDataList, EnumEntityType.EMAIL, EnumOperationType.INSERT);
				} else {
					entityDataList.add(data);
					auditProcess.addAuditDetails(entityDataList, EnumEntityType.EMAIL, EnumOperationType.UPDATE);
				}

			}

		}
	}

	@ApiOperation(value = "Get list of outbound emails", tags = "email")
	@RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<String> getOutboundEmailList(@RequestParam(value = "docId", required = false) Long docId) {
		try {
			List<EmailResData> resultList = emailProcess.getOutboundEmailList(docId);
			ApiResponseData<List<EmailResData>> apiResponseData = new ApiResponseData<List<EmailResData>>();
			apiResponseData.setResponse(resultList);
			return jsonResponseOk(apiResponseData);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		}
	}

	@ApiOperation(value = "Get saved outbound email draft. If none exists, then return the outbound email structure", tags = "email")
	@RequestMapping(value = "/draft", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<String> getOutboundEmailDraft(@RequestParam(value = "docId", required = true) Long docId,
			@RequestParam(value = "appendCaseNumber", required = false, defaultValue = "true") boolean isAppendCaseNumberInSubject,
			@RequestParam(value = "appendString", required = false, defaultValue = WorkbenchConstants.EMAIL_REPLY_SUBJECT_SUFFIX) String appendString) {
		try {
			ApiResponseData<EmailDbData> apiResponseData = new ApiResponseData<EmailDbData>();
			EmailDbData result = emailProcess.getDraftEmailWithEncoding(docId, isAppendCaseNumberInSubject, appendString);
			apiResponseData.setResponse(result);
			return jsonResponseOk(apiResponseData);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		}
	}

	@ApiOperation(value = "Add outbound email draft", tags = "email")
	@RequestMapping(value = "/draft", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE,
					MediaType.APPLICATION_JSON_VALUE })
	ResponseEntity<String> insertOutboundEmailDraft(@RequestBody InsertUpdateDraftReqData insertUpdateDraftReqData) {

		EntityDbData entityDbData = new EntityDbData();
		List<EntityDbData> entityDataList = new ArrayList<EntityDbData>();

		try {

			entityDbData = emailProcess.saveDraftEmail(insertUpdateDraftReqData);

			ApiResponseData<DocumentResData> apiResponseData = new ApiResponseData<DocumentResData>();
			apiResponseData.setResponseMsg(entityDbData.getOutboundEmailMessage());

			abstract class MixIn {
				// @JsonIgnore
				// abstract boolean getActionParamAttrMapId(); // we don't need it!

				@JsonIgnore
				abstract long getTaskStatusCde();
			}
			ObjectMapper objectMapper = new ObjectMapper();
			// objectMapper.addMixIn(ActionParamAttrMappingDbData.class, MixIn.class);
			objectMapper.addMixIn(InsertUpdateDraftReqData.class, MixIn.class);
			// String json = objectMapper.writeValueAsString(apiResponseData);
			// return new ResponseEntity<String>(json, HttpStatus.OK);
			return jsonResponseOk(apiResponseData, objectMapper);

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return new ResponseEntity<String>(null, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
		} finally {
			EntityDbData data = new EntityDbData();
			data.setEmailIdList(entityDbData.getEmailIdList());
			entityDataList.add(data);
			if (entityDbData.isDraft()) {
				auditProcess.addAuditDetails(entityDataList, EnumEntityType.EMAIL, EnumOperationType.UPDATE);
			} else {
				auditProcess.addAuditDetails(entityDataList, EnumEntityType.EMAIL, EnumOperationType.INSERT);
			}
		}
	}

	@ApiOperation(value = "Delete outbound email", tags = "email")
	@RequestMapping(value = "/{emailId}", method = RequestMethod.DELETE, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_JSON_VALUE })
	ResponseEntity<String> deleteOutboundEmail(@PathVariable Long emailId) {
		List<Long> emailIdList = new ArrayList<Long>();
		List<EntityDbData> entityDataList = new ArrayList<EntityDbData>();
		try {
			long emailOutboundId = emailProcess.deleteEmail(emailId);
			if (emailOutboundId > 0) {
				emailIdList.add(emailOutboundId);
				EntityDbData data = new EntityDbData();
				data.setEmailIdList(emailIdList);
				entityDataList.add(data);
			}
			String apiResponse = emailIdList.size() + " record(s) deleted";
			ApiResponseData<String> apiResponseData;

			if (emailIdList.size() > 0) {
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

			auditProcess.addAuditDetails(entityDataList, EnumEntityType.EMAIL, EnumOperationType.DELETE);

		}
	}

	@ApiOperation(value = "Get list of outbound emails by task status", tags = "email")
	@RequestMapping(value = "/taskstatus", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<String> getEmailSentListByTaskStatus(
			@RequestParam(value = "pageNumber", required = false) Integer pageNumber,
			@RequestParam(value = "taskStatusCde", required = true) int taskStatusCde) {
		PaginationApiResponseData<List<EmailResData>> apiResponseData = new PaginationApiResponseData<List<EmailResData>>();
		try {
			PaginationResData paginationResData = null;
			int emailPageNumber = 0;
			if (pageNumber != null)
				emailPageNumber = pageNumber;

			paginationResData = emailProcess.getOutboundEmailCountByTaskStatus(taskStatusCde, emailPageNumber);
			List<EmailResData> resultList = emailProcess.getOutboundEmailListByTaskStatus(taskStatusCde,
					emailPageNumber);
			apiResponseData.setResponse(resultList);
			apiResponseData.setPagination(paginationResData);
			return jsonResponseOk(apiResponseData);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		}
	}

	@ApiOperation(value = "Update outbound email - task status", tags = "email")
	@RequestMapping(value = "", method = RequestMethod.PUT, consumes = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE,
					MediaType.APPLICATION_JSON_VALUE })
	ResponseEntity<String> updateOutboundEmail(@RequestBody UpdateEmailStatusReqData updateEmailStatusReqData) {

		EntityDbData entityDbData = new EntityDbData();
		List<EntityDbData> entityDataList = new ArrayList<EntityDbData>();
		try {
			ApiResponseData<DocumentResData> apiResponseData = new ApiResponseData<>();
			entityDbData = emailProcess.updateEmailTaskStatus(updateEmailStatusReqData);
			entityDataList.add(entityDbData);
			apiResponseData.setResponseCde(WorkbenchConstants.API_RESPONSE_CDE_SUCCESS);
			apiResponseData.setResponseMsg(WorkbenchConstants.API_RESPONSE_MSG_SUCCESS);
			abstract class MixIn {

			}
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.addMixIn(UserResData.class, MixIn.class);
			return jsonResponseOk(apiResponseData, objectMapper);

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		} finally {
			auditProcess.addAuditDetails(entityDataList, EnumEntityType.EMAIL, EnumOperationType.UPDATE);

		}
	}

}
