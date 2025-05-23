/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.controller.api.doc;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import com.infosys.ainauto.commonutils.DateUtility;
import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.service.common.SessionHelper;
import com.infosys.ainauto.docwb.service.common.WorkbenchConstants;
import com.infosys.ainauto.docwb.service.common.type.EnumEntityType;
import com.infosys.ainauto.docwb.service.common.type.EnumOperationType;
import com.infosys.ainauto.docwb.service.common.type.EnumTaskStatus;
import com.infosys.ainauto.docwb.service.controller.api.BaseController;
import com.infosys.ainauto.docwb.service.model.api.ApiResponseData;
import com.infosys.ainauto.docwb.service.model.api.DocAndAttrResData;
import com.infosys.ainauto.docwb.service.model.api.DocUserResData;
import com.infosys.ainauto.docwb.service.model.api.DocumentResData;
import com.infosys.ainauto.docwb.service.model.api.PaginationApiResponseData;
import com.infosys.ainauto.docwb.service.model.api.PaginationResData;
import com.infosys.ainauto.docwb.service.model.api.UserResData;
import com.infosys.ainauto.docwb.service.model.api.document.AssignCaseReqData;
import com.infosys.ainauto.docwb.service.model.api.document.CloseCaseReqData;
import com.infosys.ainauto.docwb.service.model.api.document.GetDocReqData;
import com.infosys.ainauto.docwb.service.model.api.document.GetDocReqData.DocIdOperation;
import com.infosys.ainauto.docwb.service.model.api.document.InsertDocEventReqData;
import com.infosys.ainauto.docwb.service.model.api.document.InsertDocReqData;
import com.infosys.ainauto.docwb.service.model.api.document.UpdateDocStatusReqData;
import com.infosys.ainauto.docwb.service.model.db.AttributeDbData;
import com.infosys.ainauto.docwb.service.model.db.DocDetailDbData;
import com.infosys.ainauto.docwb.service.model.db.EntityDbData;
import com.infosys.ainauto.docwb.service.model.process.AppUserData;
import com.infosys.ainauto.docwb.service.process.audit.IAuditProcess;
import com.infosys.ainauto.docwb.service.process.auth.IApiRoleAuthorizationProcess;
import com.infosys.ainauto.docwb.service.process.doc.IDocumentProcess;
import com.infosys.ainauto.docwb.service.process.user.IUserProcess;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Controller
@CrossOrigin
@RequestMapping("/api/v1/document")
@Api(tags = { "document" })
public class DocumentController extends BaseController {

	@Value("${pageSizeMax}")
	private int pageSizeMax;

	private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);

	@Autowired
	private IDocumentProcess documentProcess;

	@Autowired
	private IAuditProcess auditProcess;

	@Autowired
	IApiRoleAuthorizationProcess apiRoleAuthorizationProcess;

	@Autowired
	private IUserProcess userProcess;

	private static final String SEARCH_KEY_FILE_NAME = "filename";
	private static final String SEARCH_KEY_ASSIGNED_TO = "assignedto";
	private static final String SEARCH_KEY_CASE = "case";

	@ApiOperation(value = "Add a new document along with associated data", tags = "document")
	@RequestMapping(value = "", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE,
					MediaType.APPLICATION_JSON_VALUE })
	ResponseEntity<String> addDocument(@RequestBody InsertDocReqData addDocumentRequestData) {
		List<EntityDbData> prevDocEntityDbDataList = new ArrayList<EntityDbData>();
		List<EntityDbData> latestDocEntityDbDataList = new ArrayList<EntityDbData>();
		ApiResponseData<DocumentResData> apiResponseData = new ApiResponseData<>();
		try {

			if (addDocumentRequestData != null && addDocumentRequestData.getQueueNameCde() == 0) {
				String validationMessage = "queueNameCde is mandatory";
				return jsonResponseOk(getStringApiResponseData(validationMessage,
						WorkbenchConstants.API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED,
						WorkbenchConstants.API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED));

			}
			List<EntityDbData> docEntityDbDataList = documentProcess.addDocumentAndAttributes(addDocumentRequestData);
			if (ListUtility.hasValue(docEntityDbDataList)) {
				prevDocEntityDbDataList.add(docEntityDbDataList.get(1));
				latestDocEntityDbDataList.add(docEntityDbDataList.get(0));
				DocumentResData documentResData = new DocumentResData();
				documentResData.setDocId(docEntityDbDataList.get(0).getDocId());
				apiResponseData.setResponse(documentResData);

				apiResponseData.setResponseCde(WorkbenchConstants.API_RESPONSE_CDE_SUCCESS);
				apiResponseData.setResponseMsg(WorkbenchConstants.API_RESPONSE_MSG_SUCCESS);
			}
			abstract class MixIn {
				@JsonIgnore
				abstract long getDocAttributeList();
			}
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.addMixIn(UserResData.class, MixIn.class);
			return jsonResponseOk(apiResponseData, objectMapper);

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		} finally {
			auditProcess.addAuditDetails(latestDocEntityDbDataList, EnumEntityType.DOCUMENT, EnumOperationType.INSERT);
			// Adding this update to show the insertion of task status undefined. For
			// understanding the update from undefined to yet to start.
			auditProcess.addAuditDetails(latestDocEntityDbDataList, EnumEntityType.DOCUMENT, EnumOperationType.UPDATE);
			auditProcess.addAuditDetails(prevDocEntityDbDataList, latestDocEntityDbDataList, EnumEntityType.ATTRIBUTE,
					EnumOperationType.INSERT);

		}
	}

	@ApiOperation(value = "Get a list of documents with or without filter", tags = "document")
	@RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	ResponseEntity<String> getDocumentList(@RequestParam(value = "queueNameCde", required = true) Integer queueNameCde,
			@RequestParam(value = "taskStatusCde", required = false) Integer taskStatusCde,
			@RequestParam(value = "taskStatusOperator", required = false) String taskStatusOperator,
			@RequestParam(value = "highestEventTypeCde", required = false) Integer highestEventTypeCde,
			@RequestParam(value = "highestEventTypeOperator", required = false) String highestEventTypeOperator,
			@RequestParam(value = "latestEventTypeCde", required = false) Integer latestEventTypeCde,
			@RequestParam(value = "latestEventTypeOperator", required = false) String latestEventTypeOperator,
			@RequestParam(value = "attrNameCdes", required = false) String attrNameCdes,
			@RequestParam(value = "searchCriteria", required = false) String searchCriteria,
			@RequestParam(value = "attachmentAttrNameCdes", required = false) String attachmentAttrNameCdes,
			@RequestParam(value = "lockStatusCde", required = false) Integer lockStatusCde,
			@RequestParam(value = "pageNumber", required = false) Integer pageNumber,
			@RequestParam(value = "pageSize", required = false) Integer pageSize,
			@RequestParam(value = "appUserId", required = false) Integer appUserId,
			@RequestParam(value = "docId", required = false) Integer docId,
			@RequestParam(value = "sortOrder", required = false, defaultValue = "DESC") String sortOrder,
			@RequestParam(value = "fromEventDtm", required = false) String fromEventDtm,
			@RequestParam(value = "toEventDtm", required = false) String toEventDtm,
			@RequestParam(value = "sortByAttrNameCde", required = false) String sortByAttrNameCde,
			@RequestParam(value = "isPagination ", required = false) Boolean isPagination) {
		PaginationApiResponseData<List<DocAndAttrResData>> apiResponseData = new PaginationApiResponseData<List<DocAndAttrResData>>();
		try {

			// Do validation
			boolean isDocIdDataPopulated = true;
			if (docId == null || docId == 0) {
				isDocIdDataPopulated = false;
			}
			boolean isHighestEventTypeDataPopulated = true;
			if (highestEventTypeCde == null || highestEventTypeCde == 0
					|| !StringUtility.hasValue(highestEventTypeOperator)) {
				isHighestEventTypeDataPopulated = false;
			}
			boolean isLatestEventTypeDataPopulated = true;
			if (latestEventTypeCde == null || latestEventTypeCde == 0
					|| !StringUtility.hasValue(latestEventTypeOperator)) {
				isLatestEventTypeDataPopulated = false;
			}

			if (!(appUserId == null)) {
				// If appUserId is INVALID i.e appUserId Not > 0 AND appUserId NOT IN (-1,-2 AND -3)
				if ((appUserId != WorkbenchConstants.CASE_IS_UNASSIGNED)
						&& (appUserId != WorkbenchConstants.CASE_IS_ASSIGNED) 
						&& (appUserId != WorkbenchConstants.CASE_FOR_MY_REVIEW)
						&& !(appUserId > 0)) {
					String validationMessage = "Enter a valid User Id. For filters, use -1 for assigned (to others) and -2 for unassigned and -3 for My Review";
					return jsonResponseOk(getStringApiResponseData(validationMessage,
							WorkbenchConstants.API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED,
							WorkbenchConstants.API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED));
				}
			}
			if (taskStatusCde == null || taskStatusCde <= EnumTaskStatus.UNDEFINED.getValue()
					|| !StringUtility.hasValue(taskStatusOperator)) {
				String validationMessage = "EITHER [highestEventTypeCde,highestEventTypeOperator] OR [latestEventTypeCde,latestEventTypeOperator] is mandatory";
				if (!isHighestEventTypeDataPopulated && !isLatestEventTypeDataPopulated && !isDocIdDataPopulated) {
					return jsonResponseOk(getStringApiResponseData(validationMessage,
							WorkbenchConstants.API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED,
							WorkbenchConstants.API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED));

				}
			} else {
				String validationMessage = "EventTypeCde & EventTypeOperator are not Applicable with taskStatusCde";
				if (isHighestEventTypeDataPopulated || isLatestEventTypeDataPopulated && !isDocIdDataPopulated) {
					return jsonResponseOk(getStringApiResponseData(validationMessage,
							WorkbenchConstants.API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED,
							WorkbenchConstants.API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED));
				}

			}

			if (!(sortOrder.equalsIgnoreCase("desc") || sortOrder.equalsIgnoreCase("asc"))) {
				String validationMessage = "Enter a valid sort order. For filters, use ASC for ascending order and DESC for descending order, default value is DESC";
				return jsonResponseOk(getStringApiResponseData(validationMessage,
						WorkbenchConstants.API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED,
						WorkbenchConstants.API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED));
			}

			if (StringUtility.hasTrimmedValue(fromEventDtm)) {
				Date date = DateUtility.toTimestamp(fromEventDtm, WorkbenchConstants.TIMESTAMP_FORMAT_24HR);
				String validationMessage = "Date should be in one of the following formats-'yyyy-MM-dd hh:mm:ss' OR 'yyyy-MM-dd'";
				if (date == null) {
					date = DateUtility.toTimestamp(fromEventDtm, WorkbenchConstants.API_DATE_FORMAT);
					if (date == null)
						return jsonResponseOk(getStringApiResponseData("fromEventDtm:" + validationMessage,
								WorkbenchConstants.API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED,
								WorkbenchConstants.API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED));
				}
			}
			if (StringUtility.hasTrimmedValue(toEventDtm)) {
				Date date = DateUtility.toTimestamp(toEventDtm, WorkbenchConstants.TIMESTAMP_FORMAT_24HR);
				String validationMessage = "Date should be in one of the following formats-'yyyy-MM-dd hh:mm:ss' OR 'yyyy-MM-dd'";
				if (date == null) {
					date = DateUtility.toTimestamp(toEventDtm, WorkbenchConstants.API_DATE_FORMAT);
					if (date == null)
						return jsonResponseOk(getStringApiResponseData("toEventDtm:" + validationMessage,
								WorkbenchConstants.API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED,
								WorkbenchConstants.API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED));
				}
			}

			if (StringUtility.hasTrimmedValue(searchCriteria) && !StringUtility.hasTrimmedValue(toEventDtm)
					&& !StringUtility.hasTrimmedValue(fromEventDtm)
					&& !searchCriteria.toLowerCase().startsWith(SEARCH_KEY_CASE)) {
				String validationMessage = "[toEventDtm] and [fromEventDtm] is mandatory";
				return jsonResponseOk(getStringApiResponseData(validationMessage,
						WorkbenchConstants.API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED,
						WorkbenchConstants.API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED));
			}
			if (pageSize != null && (pageSize > pageSizeMax || pageSize < 1)) {
				String validationMessage = "Page size can range from 1-100.";
				return jsonResponseOk(getStringApiResponseData(validationMessage,
						WorkbenchConstants.API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED,
						WorkbenchConstants.API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED));
			}
			GetDocReqData getDocReqData = new GetDocReqData();
			getDocReqData.setQueueNameCde(queueNameCde);

			if (taskStatusCde != null)
				getDocReqData.setTaskStatusCde(taskStatusCde);
			if (taskStatusOperator != null)
				getDocReqData.setTaskStatusOperator(taskStatusOperator);
			if (highestEventTypeCde != null)
				getDocReqData.setHighestEventTypeCde(highestEventTypeCde);
			if (highestEventTypeOperator != null)
				getDocReqData.setHighestEventTypeOperator(highestEventTypeOperator);
			if (latestEventTypeCde != null)
				getDocReqData.setLatestEventTypeCde(latestEventTypeCde);
			if (latestEventTypeOperator != null)
				getDocReqData.setLatestEventTypeOperator(latestEventTypeOperator);
			if (attrNameCdes != null)
				getDocReqData.setAttrNameCdes(attrNameCdes);
			if (attachmentAttrNameCdes != null)
				getDocReqData.setAttachmentAttrNameCdes(attachmentAttrNameCdes);
			if (lockStatusCde != null)
				getDocReqData.setLockStatusCde(lockStatusCde);
			if (docId != null)
				getDocReqData.setDocId(docId);
			if (pageNumber != null)
				getDocReqData.setPageNumber(pageNumber);
			if (appUserId != null)
				getDocReqData.setAppUserId(appUserId);
			if (sortOrder != null)
				getDocReqData.setSortOrder(sortOrder);
			if (pageSize != null)
				getDocReqData.setPageSize(pageSize);
			if (sortByAttrNameCde != null)
				getDocReqData.setSortByAttrNameCde(sortByAttrNameCde);
			if (isPagination == null)
				getDocReqData.setIsPagination(true);
			else
				getDocReqData.setIsPagination(isPagination);
			if (StringUtility.hasTrimmedValue(searchCriteria)) {
				try {
					List<DocIdOperation> docIdOperationList = new ArrayList<DocIdOperation>();

					// AssignedTo:docwbadmin;FileName:BTRF-DIEXT-MEK-2107211214194.pdf
					String[] arrOfMultiSearchCriteria = searchCriteria.split(";", -2);
					for (String searchCriteriaVal : arrOfMultiSearchCriteria) {
						if (!StringUtility.hasTrimmedValue(searchCriteriaVal)) {
							continue;
						}
						String[] arrOfSearchCriteria = searchCriteriaVal.split(":", 2);
						String searchKey = arrOfSearchCriteria[0].toLowerCase();
						String searchVal = arrOfSearchCriteria[1];
						if (searchKey.contentEquals(SEARCH_KEY_FILE_NAME)) {
							if (!StringUtility.hasTrimmedValue(searchVal)) {
								String validationMessage = "Please provide attached file name";
								return jsonResponseOk(getStringApiResponseData(validationMessage,
										WorkbenchConstants.API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED,
										WorkbenchConstants.API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED));
							}
							getDocReqData.setDocumentName(searchVal);
						} else if (searchKey.contentEquals(SEARCH_KEY_ASSIGNED_TO)) {
							getDocReqData.setAssignedTo(searchVal);
							getDocReqData.setAssignedToKey(searchKey);
						} else if (searchKey.contentEquals(SEARCH_KEY_CASE)) {
							docIdOperationList.add(getDocIdOperation(searchVal));
						}

						else {
							String validationMessage = "Invalid Search Key.Format expected is key1:value1;key2:value2";
							return jsonResponseOk(getStringApiResponseData(validationMessage,
									WorkbenchConstants.API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED,
									WorkbenchConstants.API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED));
						}
					}

					getDocReqData.setDocIdOperationList(docIdOperationList);
				} catch (Exception err) {
					String validationMessage = err.getMessage();
					if (validationMessage.contains("Array index out of range")) {
						validationMessage = "Invalid Search Key.Format expected is key1:value1;key2:value2";
					}
					return jsonResponseOk(getStringApiResponseData(validationMessage,
							WorkbenchConstants.API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED,
							WorkbenchConstants.API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED));

				}
			}
			if (StringUtility.hasTrimmedValue(fromEventDtm))
				getDocReqData.setFromEventDtm(fromEventDtm);
			if (StringUtility.hasTrimmedValue(toEventDtm))
				getDocReqData.setToEventDtm(toEventDtm);

			documentProcess.getDocumentDetails(apiResponseData, getDocReqData);
			abstract class MixIn {
				@JsonIgnore
				abstract long getDocId();

				@JsonIgnore
				abstract String getAttrNameCdes();

				@JsonIgnore
				abstract String getAttachmentAttrNameCdes();

				@JsonIgnore
				abstract AttributeDbData getAttributeId();

				@JsonIgnore
				abstract AttributeDbData getNotification();

				@JsonIgnore
				abstract String getSortOrder();

			}
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.addMixIn(AttributeDbData.class, MixIn.class);

			return jsonResponseOk(apiResponseData, objectMapper);

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		}
	}

	@ApiOperation(value = "Get list of document(s) based on attribute(s) search criteria", tags = "document")
	@RequestMapping(value = "/attribute", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	ResponseEntity<String> getDocumentListByAttribute(
			@RequestParam(value = "searchCriteria", required = true) String searchCriteria,
			@RequestParam(value = "queueNameCdes", required = false) String queueNameCdes,
			@RequestParam(value = "taskStatusCde", required = false) Integer taskStatusCde,
			@RequestParam(value = "taskStatusOperator", required = false) String taskStatusOperator,
			@RequestParam(value = "pageNumber", required = false) Integer pageNumber,
			@RequestParam(value = "sortOrder", required = false, defaultValue = "DESC") String sortOrder,
			@RequestParam(value = "fromCaseCreateDtm", required = false) String fromCaseCreateDtm,
			@RequestParam(value = "toCaseCreateDtm", required = false) String toCaseCreateDtm) {
		PaginationApiResponseData<List<DocDetailDbData>> apiResponseData = new PaginationApiResponseData<>();
		try {
			
			if (!StringUtility.hasTrimmedValue(searchCriteria)) {
				String validationMessage = "[searchCriteria] is mandatory";
				return jsonResponseOk(getStringApiResponseData(validationMessage,
						WorkbenchConstants.API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED,
						WorkbenchConstants.API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED));
			}

			if (!(sortOrder.equalsIgnoreCase("desc") || sortOrder.equalsIgnoreCase("asc"))) {
				String validationMessage = "Enter a valid sort order. For filters, use ASC for ascending order and DESC for descending order, default value is DESC";
				return jsonResponseOk(getStringApiResponseData(validationMessage,
						WorkbenchConstants.API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED,
						WorkbenchConstants.API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED));
			}

			if (StringUtility.hasTrimmedValue(fromCaseCreateDtm)) {
				Date date = DateUtility.toTimestamp(fromCaseCreateDtm, WorkbenchConstants.TIMESTAMP_FORMAT_24HR);
				String validationMessage = "Date should be in one of the following formats-'yyyy-MM-dd hh:mm:ss' OR 'yyyy-MM-dd'";
				if (date == null) {
					date = DateUtility.toTimestamp(fromCaseCreateDtm, WorkbenchConstants.API_DATE_FORMAT);
					if (date == null)
						return jsonResponseOk(getStringApiResponseData("fromCaseCreateDtm:" + validationMessage,
								WorkbenchConstants.API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED,
								WorkbenchConstants.API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED));
				}
			}
			if (StringUtility.hasTrimmedValue(toCaseCreateDtm)) {
				Date date = DateUtility.toTimestamp(toCaseCreateDtm, WorkbenchConstants.TIMESTAMP_FORMAT_24HR);
				String validationMessage = "Date should be in one of the following formats-'yyyy-MM-dd hh:mm:ss' OR 'yyyy-MM-dd'";
				if (date == null) {
					date = DateUtility.toTimestamp(toCaseCreateDtm, WorkbenchConstants.API_DATE_FORMAT);
					if (date == null)
						return jsonResponseOk(getStringApiResponseData("toCaseCreateDtm:" + validationMessage,
								WorkbenchConstants.API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED,
								WorkbenchConstants.API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED));
				}
			}
			
			if ((taskStatusCde != null && !StringUtility.hasValue(taskStatusOperator)) 
					|| (taskStatusCde == null && StringUtility.hasValue(taskStatusOperator))) {
				String validationMessage = "Needed both [taskStatusCde, taskStatusOperator]";
				return jsonResponseOk(getStringApiResponseData(validationMessage,
							WorkbenchConstants.API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED,
							WorkbenchConstants.API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED));
			} 

			GetDocReqData getDocReqData = new GetDocReqData();
			if (taskStatusCde != null)
				getDocReqData.setTaskStatusCde(taskStatusCde);
			if (taskStatusOperator != null)
				getDocReqData.setTaskStatusOperator(taskStatusOperator);
			if (pageNumber != null)
				getDocReqData.setPageNumber(pageNumber);
			if (sortOrder != null)
				getDocReqData.setSortOrder(sortOrder);
			if (StringUtility.hasTrimmedValue(fromCaseCreateDtm))
				getDocReqData.setFromCaseCreateDtm(fromCaseCreateDtm);
			if (StringUtility.hasTrimmedValue(toCaseCreateDtm))
				getDocReqData.setToCaseCreateDtm(toCaseCreateDtm);
			if (StringUtility.hasTrimmedValue(searchCriteria))
				getDocReqData.setSearchCriteria(searchCriteria);
			if (StringUtility.hasTrimmedValue(queueNameCdes))
				getDocReqData.setQueueNameCdes(queueNameCdes);
			documentProcess.getDocumentDetailsByAttribute(apiResponseData, getDocReqData);
			abstract class MixIn {
				@JsonIgnore
				abstract boolean getTotalPageCount();
				@JsonIgnore
				abstract boolean getTotalItemCount();
			}
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.addMixIn(DocDetailDbData.class, MixIn.class);
			objectMapper.addMixIn(PaginationResData.class, MixIn.class);
			return jsonResponseOk(apiResponseData, objectMapper);

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		}
	}
	
	public DocIdOperation getDocIdOperation(String splitText) throws Exception {
		List<String> opertors = new ArrayList<String>();
		opertors.add(">=");
		opertors.add("<=");
		opertors.add("<");
		opertors.add(">");
		opertors.add("=");
		DocIdOperation docIdOperation = new DocIdOperation();
		try {
			for (String opertor : opertors) {
				if (StringUtility.hasTrimmedValue(splitText) && splitText.startsWith(opertor)) {
					docIdOperation.setDocId(Long.valueOf(splitText.split(opertor, 2)[1]));
					docIdOperation.setOperator(opertor);
					break;
				}

			}
			if (!StringUtility.hasTrimmedValue(docIdOperation.getOperator())) {
				docIdOperation.setDocId(Long.valueOf(splitText));
				docIdOperation.setOperator("=");
			}

		} catch (Exception ex) {
			throw new Exception("Invaild operator. Expected operators are \">=\", \"<=\", \"=\", \">\", \"<\" ");
		}
		return docIdOperation;
	}

	@ApiOperation(value = "Assign/Reassign user to a document", tags = "document")
	@RequestMapping(value = "/user", method = RequestMethod.POST, consumes = {
			MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
	ResponseEntity<String> addUserDocRelationship(@RequestBody List<AssignCaseReqData> documentRequestData) {
		List<EntityDbData> docEntityDbDataList = new ArrayList<EntityDbData>();

		try {
			boolean isFeatureAllowed = false;
			if (documentRequestData.get(0).getPrevAppUserId() > 0 && apiRoleAuthorizationProcess.isFeatureAccessAllowed(
					(documentRequestData.get(0).getDocRoleTypeCde() == 1) ? WorkbenchConstants.FEATURE_ID_CASE_REASSIGN
							: WorkbenchConstants.FEATURE_ID_CASE_REVIEW_USER_REASSIGN)) {
				isFeatureAllowed = true;
			} else if (documentRequestData.get(0).getPrevAppUserId() <= 0 && apiRoleAuthorizationProcess
					.isFeatureAccessAllowed((documentRequestData.get(0).getDocRoleTypeCde() == 1)
							? WorkbenchConstants.FEATURE_ID_CASE_ASSIGN
							: WorkbenchConstants.FEATURE_ID_CASE_REVIEW_USER_ASSIGN)) {
				isFeatureAllowed = true;
				if (StringUtility.hasTrimmedValue(documentRequestData.get(0).getPrevAppUserLoginId())) {
					AppUserData appUserData = userProcess.getUserDetailsFromLoginId(
							documentRequestData.get(0).getPrevAppUserLoginId(), SessionHelper.getTenantId());
					documentRequestData.get(0).setPrevAppUserId(appUserData.getAppUserId());
				}
			}

			if (!isFeatureAllowed) {
				return jsonResponseForbidden();
			}
			if (documentRequestData.get(0).getAppUserId() <= 0) {
				AppUserData appUserData = userProcess.getUserDetailsFromLoginId(
						documentRequestData.get(0).getAppUserLoginId(), SessionHelper.getTenantId());
				documentRequestData.get(0).setAppUserId(appUserData.getAppUserId());
			}

			if (documentRequestData.get(0).getAppUserId() > 0) {
				docEntityDbDataList = documentProcess.addUserToDoc(documentRequestData.get(0).getPrevAppUserId(),
						documentRequestData.get(0).getAppUserId(), documentRequestData.get(0).getDocId(),
						documentRequestData.get(0).getDocRoleTypeCde());

			}
			String apiResponse = "User assigned to document";
			ApiResponseData<String> apiResponseData = new ApiResponseData<>();
			if (ListUtility.hasValue(docEntityDbDataList)) {
				if (docEntityDbDataList.get(0).getDocAppUserRelIdList().get(0) > 0) {
					apiResponseData = getStringApiResponseData(apiResponse, WorkbenchConstants.API_RESPONSE_CDE_SUCCESS,
							WorkbenchConstants.API_RESPONSE_MSG_SUCCESS);
				} else if (docEntityDbDataList.get(0).getDocAppUserRelIdList()
						.get(0) == WorkbenchConstants.PREV_USER_DETAILS_OUTDATED) {
					apiResponse = "Previous User data sent is not matching";
					apiResponseData = getStringApiResponseData(apiResponse,
							WorkbenchConstants.API_RESPONSE_CDE_CONCURRENT,
							WorkbenchConstants.API_RESPONSE_MSG_CONCURRENT);
				}
			} else {
				apiResponse = "User not assigned to document";
				apiResponseData = getStringApiResponseData(apiResponse, WorkbenchConstants.API_RESPONSE_CDE_NO_RECORDS,
						WorkbenchConstants.API_RESPONSE_MSG_NO_RECORDS);
			}

			return jsonResponseOk(apiResponseData);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		} finally {
			List<EntityDbData> prevdocEntityDbDataList = new ArrayList<EntityDbData>();
			List<EntityDbData> latestdocEntityDbDataList = new ArrayList<EntityDbData>();
			if (ListUtility.hasValue(docEntityDbDataList)) {
				if (docEntityDbDataList.get(0).getDocAppUserRelIdList().get(0) > 0) {
					if (ListUtility.hasValue(docEntityDbDataList.get(1).getDocAppUserRelIdList())) {
						prevdocEntityDbDataList.add(docEntityDbDataList.get(1));
						latestdocEntityDbDataList.add(docEntityDbDataList.get(0));
						auditProcess.addAuditDetails(prevdocEntityDbDataList, latestdocEntityDbDataList,
								(documentRequestData.get(0).getDocRoleTypeCde() == 1)
										? EnumEntityType.CASE_OWNER_ASSIGNMENT
										: EnumEntityType.CASE_REVIEWER_ASSIGNMENT,
								EnumOperationType.UPDATE);
					} else {
						auditProcess.addAuditDetails(docEntityDbDataList,
								(documentRequestData.get(0).getDocRoleTypeCde() == 1)
										? EnumEntityType.CASE_OWNER_ASSIGNMENT
										: EnumEntityType.CASE_REVIEWER_ASSIGNMENT,
								EnumOperationType.INSERT);
					}
				}
			}
		}
	}
	
	@ApiOperation(value = "Get document user details", tags = "document")
	@RequestMapping(value = "/user", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	ResponseEntity<String> getUserDocRelationship(@RequestParam(value = "docId", required = true) int docId) {
		try {
			ApiResponseData<List<DocUserResData>> apiResponseData = new ApiResponseData<>();
			List<DocUserResData> docUserResDataList = documentProcess.getDocUserDetails(docId);
			apiResponseData.setResponse(docUserResDataList);
			return jsonResponseOk(apiResponseData);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		}
	}
	
	@ApiOperation(value = "Delete a document", tags = "document")
	@RequestMapping(value = "/{docId}", method = RequestMethod.DELETE, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_JSON_VALUE })
	ResponseEntity<String> deleteDocument(@PathVariable Long docId) {
		List<EntityDbData> entityDataList = new ArrayList<EntityDbData>();
		try {
			EntityDbData data = documentProcess.deleteDocument(docId);
			ApiResponseData<String> apiResponseData;

			if (data.getDocId() > 0) {
				entityDataList.add(data);
				String apiResponse = entityDataList.size() + " record(s) deleted";
				apiResponseData = getStringApiResponseData(apiResponse, WorkbenchConstants.API_RESPONSE_CDE_SUCCESS,
						WorkbenchConstants.API_RESPONSE_MSG_SUCCESS);
			} else {
				String apiResponse = entityDataList.size() + " record(s) deleted";
				apiResponseData = getStringApiResponseData(apiResponse, WorkbenchConstants.API_RESPONSE_CDE_NO_RECORDS,
						WorkbenchConstants.API_RESPONSE_MSG_NO_RECORDS);
			}

			return jsonResponseOk(apiResponseData);

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		} finally {
			auditProcess.addAuditDetails(entityDataList, EnumEntityType.DOCUMENT, EnumOperationType.DELETE);
		}
	}

	@ApiOperation(value = "Close case of document", tags = "document")
	@RequestMapping(value = "/case", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE,
					MediaType.APPLICATION_JSON_VALUE })
	ResponseEntity<String> closeCaseForDoc(@RequestBody List<CloseCaseReqData> closeCaseReqDataList,
			@RequestParam(value = "queueNameCde", required = true) Integer queueNameCde) {
		List<EntityDbData> entityDbData = new ArrayList<EntityDbData>();
		EntityDbData docEntityDbData = new EntityDbData();
		try {

			ApiResponseData<String> apiResponseData = new ApiResponseData<>();

			if (closeCaseReqDataList.size() > 1) {
				String message = "Only single item is supported.";
				return jsonResponseOk(
						getStringApiResponseData(message, WorkbenchConstants.API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED,
								WorkbenchConstants.API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED));
			}

			// Update appUserId if missing
			for (CloseCaseReqData closeCaseReqData : closeCaseReqDataList) {
				if (closeCaseReqData.getAppUserId() <= 0) {
					AppUserData appUserData = userProcess.getUserDetailsFromLoginId(
							closeCaseReqData.getAppUserLoginId(), SessionHelper.getTenantId());
					if (!(appUserData.getAppUserId() > 0)) {
						String message = "User doesn't exist : " + closeCaseReqData.getAppUserLoginId();
						return jsonResponseOk(getStringApiResponseData(message,
								WorkbenchConstants.API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED,
								WorkbenchConstants.API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED));
					}
					closeCaseReqData.setAppUserId(appUserData.getAppUserId());
				}
			}

			docEntityDbData = documentProcess.closeCaseForDocument(queueNameCde, closeCaseReqDataList);

			if (docEntityDbData.getDocId() > 0) {
				entityDbData.add(docEntityDbData);
				apiResponseData.setResponse("Case closed successfully");
				apiResponseData.setResponseCde(WorkbenchConstants.API_RESPONSE_CDE_SUCCESS);
				apiResponseData.setResponseMsg(WorkbenchConstants.API_RESPONSE_MSG_SUCCESS);
			} else if (docEntityDbData.getDocId() == WorkbenchConstants.CASE_OWNER_NOT_SAME_AS_USER) {
				apiResponseData.setResponse("Case can be closed only by the case owner");
				apiResponseData.setResponseCde(WorkbenchConstants.API_RESPONSE_CDE_NO_RECORDS);
				apiResponseData.setResponseMsg(WorkbenchConstants.API_RESPONSE_MSG_NO_RECORDS);
			} else if (docEntityDbData.getDocId() == WorkbenchConstants.CASE_HAS_PENDING_ACTIONS) {
				apiResponseData.setResponse("Case cannot be closed due to pending actions");
				apiResponseData.setResponseCde(WorkbenchConstants.API_RESPONSE_CDE_CASE_CANT_BE_CLOSED);
				apiResponseData.setResponseMsg(WorkbenchConstants.API_RESPONSE_MSG_CASE_CANT_BE_CLOSED);
			} else if (docEntityDbData.getDocId() == WorkbenchConstants.CASE_STATUS_DIFFERENT) {
				apiResponseData.setResponse("Case is already closed or not valid.");
				apiResponseData.setResponseCde(WorkbenchConstants.API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED);
				apiResponseData.setResponseMsg(WorkbenchConstants.API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED);
			} else {
				apiResponseData.setResponse("Case could not be closed due to unknown error");
				apiResponseData.setResponseCde(WorkbenchConstants.API_RESPONSE_CDE_NO_RECORDS);
				apiResponseData.setResponseMsg(WorkbenchConstants.API_RESPONSE_MSG_NO_RECORDS);
			}

			return jsonResponseOk(apiResponseData);

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		} finally {
			if (entityDbData.size() > 0) {
				auditProcess.addAuditDetails(entityDbData, EnumEntityType.DOCUMENT, EnumOperationType.UPDATE);
			}
		}
	}

	@ApiOperation(value = "Update action Status to document", tags = "document")
	@RequestMapping(value = "/taskstatus", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE,
					MediaType.APPLICATION_JSON_VALUE })
	ResponseEntity<String> updateDocActionStatus(@RequestBody UpdateDocStatusReqData documentRequestData) {
		List<EntityDbData> entityDataList = new ArrayList<EntityDbData>();
		try {
			ApiResponseData<UpdateDocStatusReqData> apiResponseData = new ApiResponseData<>();
			entityDataList = documentProcess.updateDocActionStatus(documentRequestData);
			apiResponseData.setResponseCde(WorkbenchConstants.API_RESPONSE_CDE_SUCCESS);
			apiResponseData.setResponseMsg(WorkbenchConstants.API_RESPONSE_MSG_SUCCESS);
			abstract class MixIn {
				@JsonIgnore
				abstract long getDocAttributeList();
			}
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.addMixIn(UserResData.class, MixIn.class);
			return jsonResponseOk(apiResponseData, objectMapper);

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		} finally {
			auditProcess.addAuditDetails(entityDataList, EnumEntityType.DOCUMENT, EnumOperationType.UPDATE);
		}
	}

	@ApiOperation(value = "insert action Status to document", tags = "document")
	@RequestMapping(value = "/eventType", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE,
					MediaType.APPLICATION_JSON_VALUE })
	ResponseEntity<String> insertDocEventType(@RequestBody InsertDocEventReqData documentRequestData) {
		try {

			ApiResponseData<InsertDocEventReqData> apiResponseData = new ApiResponseData<>();
			documentProcess.insertDocEventType(documentRequestData);

			apiResponseData.setResponse(null);
			apiResponseData.setResponseCde(WorkbenchConstants.API_RESPONSE_CDE_SUCCESS);
			apiResponseData.setResponseMsg(WorkbenchConstants.API_RESPONSE_MSG_SUCCESS);
			abstract class MixIn {
				@JsonIgnore
				abstract long getDocAttributeList();
			}
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.addMixIn(UserResData.class, MixIn.class);
			return jsonResponseOk(apiResponseData, objectMapper);

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		}
	}
}
