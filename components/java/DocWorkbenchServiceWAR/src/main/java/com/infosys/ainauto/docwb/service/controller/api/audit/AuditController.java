/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.controller.api.audit;

import java.util.List;

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
import com.infosys.ainauto.docwb.service.common.WorkbenchConstants;
import com.infosys.ainauto.docwb.service.controller.api.BaseController;
import com.infosys.ainauto.docwb.service.model.api.ApiResponseData;
import com.infosys.ainauto.docwb.service.model.api.AuditResData;
import com.infosys.ainauto.docwb.service.model.api.PaginationApiResponseData;
import com.infosys.ainauto.docwb.service.model.api.PaginationResData;
import com.infosys.ainauto.docwb.service.model.api.audit.AddDocAuditReqData;
import com.infosys.ainauto.docwb.service.model.api.audit.GetDocAuditReqData;
import com.infosys.ainauto.docwb.service.model.api.audit.GetUserAuditReqData;
import com.infosys.ainauto.docwb.service.model.db.AuditDbData;
import com.infosys.ainauto.docwb.service.process.audit.IAuditProcess;
import com.infosys.ainauto.docwb.service.process.doc.IDocumentProcess;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Controller
@CrossOrigin
@RequestMapping("/api/v1/audit")
@Api(tags = { "audit" })
public class AuditController extends BaseController {
	private static final Logger logger = LoggerFactory.getLogger(AuditController.class);

	@Autowired
	private IAuditProcess auditProcess;

	@Autowired
	private IDocumentProcess documentProcess;

	@ApiOperation(value = "Get audit details", tags = "audit")
	@RequestMapping(value = "/document", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> getDocAuditDetails(@RequestParam(value = "docId", required = true) Long docId,
			@RequestParam(value = "pageNumber", required = true) Integer pageNumber) {
		try {
			PaginationApiResponseData<List<AuditDbData>> apiResponseData = new PaginationApiResponseData<List<AuditDbData>>();

			GetDocAuditReqData getDocAuditReqData = new GetDocAuditReqData();
			if (docId != null)
				getDocAuditReqData.setDocId(docId);
			if (pageNumber != null)
				getDocAuditReqData.setPageNumber(pageNumber);
			AuditResData result = auditProcess.getAuditForDoc(getDocAuditReqData);

			apiResponseData.setResponse(result.getAuditDataList());
			apiResponseData.setPagination(result.getPaginationData());

			abstract class MixIn {
				@JsonIgnore
				abstract boolean getDocId();

				@JsonIgnore
				abstract boolean getAppUserId();

				@JsonIgnore
				abstract boolean getEntityName();

				@JsonIgnore
				abstract boolean getEntityValue();

				@JsonIgnore
				abstract boolean getAuditEventDtm();

				@JsonIgnore
				abstract boolean getUserType();
				
				@JsonIgnore
				abstract boolean getQueueNameCde();

			}
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.addMixIn(AuditDbData.class, MixIn.class);

			return jsonResponseOk(apiResponseData, objectMapper);

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		}
	}

	@ApiOperation(value = "Get audit details", tags = "audit")
	@RequestMapping(value = "/user", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> getUserAuditDetails(
			@RequestParam(value = "appUserId", required = true) Long appUserId,
			@RequestParam(value = "pageNumber", required = true) Integer pageNumber) {
		try {
			PaginationApiResponseData<List<AuditDbData>> apiResponseData = new PaginationApiResponseData<List<AuditDbData>>();

			GetUserAuditReqData getUserAuditReqData = new GetUserAuditReqData();
			if (appUserId != null)
				getUserAuditReqData.setAppUserId(appUserId);
			if (pageNumber != null)
				getUserAuditReqData.setPageNumber(pageNumber);
			AuditResData result = auditProcess.getAuditForUser(getUserAuditReqData);

			apiResponseData.setResponse(result.getAuditDataList());
			apiResponseData.setPagination(result.getPaginationData());

			abstract class MixIn {
				@JsonIgnore
				abstract boolean getDocId();

				@JsonIgnore
				abstract boolean getAppUserId();

				@JsonIgnore
				abstract boolean getEntityName();

				@JsonIgnore
				abstract boolean getEntityValue();

				@JsonIgnore
				abstract boolean getAuditEventDtm();

				@JsonIgnore
				abstract boolean getUserType();
				
				@JsonIgnore
				abstract boolean getQueueNameCde();

			}
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.addMixIn(AuditDbData.class, MixIn.class);

			return jsonResponseOk(apiResponseData, objectMapper);

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		}
	}

	@ApiOperation(value = "Get app variable audit details", tags = "audit")
	@RequestMapping(value = "/variable", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> getUserAuditDetails(
			@RequestParam(value = "appVariableKey", required = true) String appVariableKey,
			@RequestParam(value = "pageNumber", required = true) Integer pageNumber) {
		try {
			PaginationApiResponseData<List<AuditDbData>> apiResponseData = new PaginationApiResponseData<List<AuditDbData>>();
			AuditResData result = auditProcess.getAuditForAppVariableKey(appVariableKey, pageNumber);

			apiResponseData.setResponse(result.getAuditDataList());
			apiResponseData.setPagination(result.getPaginationData());

			abstract class MixIn {
				@JsonIgnore
				abstract boolean getDocId();

				@JsonIgnore
				abstract boolean getAppUserId();

				@JsonIgnore
				abstract boolean getEntityName();

				@JsonIgnore
				abstract boolean getEntityValue();

				@JsonIgnore
				abstract boolean getAuditEventDtm();

				@JsonIgnore
				abstract boolean getUserType();

			}
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.addMixIn(AuditDbData.class, MixIn.class);

			return jsonResponseOk(apiResponseData, objectMapper);

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		}
	}

	@ApiOperation(value = "Add audit details at case level", tags = "audit")
	@RequestMapping(value = "/document", method = RequestMethod.POST, consumes = {
			MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
	ResponseEntity<String> addDocAuditDetails(@RequestBody AddDocAuditReqData requestData) {
		try {
			// Validate docId - will throw error if invalid
			documentProcess.getBasicDocumentDetails(requestData.getDocId());
			int recordsUpdated = auditProcess.addDocAuditDetails(requestData);
			String apiResponse = recordsUpdated + " record(s) added";
			ApiResponseData<String> apiResponseData = getStringApiResponseData(apiResponse,
					WorkbenchConstants.API_RESPONSE_CDE_SUCCESS, WorkbenchConstants.API_RESPONSE_MSG_SUCCESS);

			return jsonResponseOk(apiResponseData);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		}
	}
	
	@ApiOperation(value = "Get case level audit history of currently logged in user", tags = "audit")
	@RequestMapping(value = "/document/currentuser", method = RequestMethod.GET,
					produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> getDocLevelAuditDetailsForCurrentUser(
			@RequestParam(value = "pageNumber", required = true) Integer pageNumber) {
		try {
			PaginationApiResponseData<List<AuditDbData>> apiResponseData = new PaginationApiResponseData<List<AuditDbData>>();

			GetDocAuditReqData getDocAuditReqData = new GetDocAuditReqData();
		
			if (pageNumber != null)
				getDocAuditReqData.setPageNumber(pageNumber);
			AuditResData result = auditProcess.getCaseAuditForUser(getDocAuditReqData);

			apiResponseData.setResponse(result.getAuditDataList());
			apiResponseData.setPagination(result.getPaginationData());

			abstract class MixIn {

				@JsonIgnore
				abstract boolean getAppUserId();

				@JsonIgnore
				abstract boolean getEntityName();

				@JsonIgnore
				abstract boolean getEntityValue();

				@JsonIgnore
				abstract boolean getAuditEventDtm();
				
				@JsonIgnore
				abstract boolean getRbacId();

				@JsonIgnore
				abstract boolean getTotalPageCount();
				
				@JsonIgnore
				abstract boolean getTotalItemCount();

			}
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.addMixIn(AuditDbData.class, MixIn.class);
			objectMapper.addMixIn(PaginationResData.class, MixIn.class);

			return jsonResponseOk(apiResponseData, objectMapper);

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		}
	}

}
