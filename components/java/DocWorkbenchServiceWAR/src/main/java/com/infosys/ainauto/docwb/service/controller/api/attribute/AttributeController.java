/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.controller.api.attribute;

import java.util.ArrayList;
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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.docwb.service.common.AttributeValidator;
import com.infosys.ainauto.docwb.service.common.WorkbenchConstants;
import com.infosys.ainauto.docwb.service.common.type.EnumEntityType;
import com.infosys.ainauto.docwb.service.common.type.EnumOperationType;
import com.infosys.ainauto.docwb.service.common.type.EnumSystemAttributeName;
import com.infosys.ainauto.docwb.service.controller.api.BaseController;
import com.infosys.ainauto.docwb.service.model.api.ApiResponseData;
import com.infosys.ainauto.docwb.service.model.api.AttributeNameValueResData;
import com.infosys.ainauto.docwb.service.model.api.attribute.AttributeNameResData;
import com.infosys.ainauto.docwb.service.model.api.attribute.AttributeSourceReqData;
import com.infosys.ainauto.docwb.service.model.api.attribute.DeleteAttributeReqData;
import com.infosys.ainauto.docwb.service.model.api.attribute.ExportAttributeResData;
import com.infosys.ainauto.docwb.service.model.api.attribute.GetAttributeNotificationResData;
import com.infosys.ainauto.docwb.service.model.api.attribute.InsertAttributeReqData;
import com.infosys.ainauto.docwb.service.model.api.attribute.UpdateAttributeReqData;
import com.infosys.ainauto.docwb.service.model.db.AttributeDbData;
import com.infosys.ainauto.docwb.service.model.db.EntityDbData;
import com.infosys.ainauto.docwb.service.process.attribute.IAttributeProcess;
import com.infosys.ainauto.docwb.service.process.audit.IAuditProcess;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;;

@Controller
@CrossOrigin
@RequestMapping("/api/v1/attribute")
@Api(tags = { "attribute" })
public class AttributeController extends BaseController {

	private static final Logger logger = LoggerFactory.getLogger(AttributeController.class);

	@Autowired
	private IAuditProcess auditProcess;

	@Autowired
	private IAttributeProcess attributeProcess;

	@Autowired
	private AttributeValidator attributeValidator;
	
	@ApiOperation(value = "Add one or more attributes", tags = "attribute")
	@RequestMapping(value = "/add", method = RequestMethod.POST, consumes = {
			MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
	ResponseEntity<String> insertAttribute(@RequestBody List<InsertAttributeReqData> requestDataList) {
		List<EntityDbData> prevDocEntityDbDataList = new ArrayList<EntityDbData>();
		List<EntityDbData> latestDocEntityDbDataList = new ArrayList<EntityDbData>();
		ApiResponseData<String> apiResponseData = new ApiResponseData<>();
		String apiResponse = "";
		try {

			ResponseEntity<String> responseEntity = attributeValidator.validateAddAttributeRequest(requestDataList);
			if (responseEntity != null) {
				return responseEntity;
			}
			List<EntityDbData> docEntityDbDataList = attributeProcess.addAttribute(requestDataList);
			if (ListUtility.hasValue(docEntityDbDataList)) {
				prevDocEntityDbDataList.add(docEntityDbDataList.get(1));
				latestDocEntityDbDataList.add(docEntityDbDataList.get(0));

				apiResponse = 0 + " record(s) added";
				if (latestDocEntityDbDataList.get(0).getProcessedCount() > 0) {
					apiResponse = latestDocEntityDbDataList.get(0).getProcessedCount() + " record(s) added";
					apiResponseData = getStringApiResponseData(apiResponse, WorkbenchConstants.API_RESPONSE_CDE_SUCCESS,
							WorkbenchConstants.API_RESPONSE_MSG_SUCCESS);
				} else {
					apiResponseData = getStringApiResponseData(apiResponse,
							WorkbenchConstants.API_RESPONSE_CDE_NO_RECORDS,
							WorkbenchConstants.API_RESPONSE_MSG_NO_RECORDS);
				}
			} else {
				apiResponse = docEntityDbDataList.size() + " record(s) added";
				apiResponseData = getStringApiResponseData(apiResponse, WorkbenchConstants.API_RESPONSE_CDE_NO_RECORDS,
						WorkbenchConstants.API_RESPONSE_MSG_NO_RECORDS);
			}

			return jsonResponseOk(apiResponseData);
		} catch (Exception ex) {
			if (ex.getMessage() != null
					&& ex.getMessage().equalsIgnoreCase(WorkbenchConstants.DB_ERROR_MSG_ATTRIBUTE_ALREADY_EXISTS)) {
				try {
					return jsonResponseOk(
							getStringApiResponseData(WorkbenchConstants.DB_ERROR_MSG_ATTRIBUTE_ALREADY_EXISTS,
									WorkbenchConstants.API_RESPONSE_CDE_MULTI_ATTRIBUTE_ALREADY_EXIST,
									WorkbenchConstants.API_RESPONSE_MSG_MULTI_ATTRIBUTE_ALREADY_EXIST));
				} catch (JsonProcessingException e) {
				}
			}
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		} finally {
			auditProcess.addAuditDetails(prevDocEntityDbDataList, latestDocEntityDbDataList, EnumEntityType.ATTRIBUTE,
					EnumOperationType.INSERT);
		}
	}

	@ApiOperation(value = "Update one or more attributes", tags = "attribute")
	@RequestMapping(value = "/edit", method = RequestMethod.POST, consumes = {
			MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
	ResponseEntity<String> updateAttribute(@RequestBody List<UpdateAttributeReqData> requestDataList) {
		List<EntityDbData> prevdocEntityDbDataList = new ArrayList<EntityDbData>();
		List<EntityDbData> latestdocEntityDbDataList = new ArrayList<EntityDbData>();
		ApiResponseData<String> apiResponseData = new ApiResponseData<>();
		String apiResponse = "";
		try {
			for (UpdateAttributeReqData data : requestDataList) {
				if (data.getDocId() <= 0) {
					String message = "Please provide document Id.";
					return jsonResponseOk(getStringApiResponseData(message,
							WorkbenchConstants.API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED,
							WorkbenchConstants.API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED));
				}
				// To get the attribute list from the request object.
				List<AttributeDbData> dataList = attributeProcess.convertReqDataToAttributeList(data, data.getDocId());
				for (AttributeDbData attrData : dataList) {
					if (attrData.getAttrNameCde() == EnumSystemAttributeName.MULTI_ATTRIBUTE.getCde()) {
						if (attrData.getAttrValue() != null) {
							String validationMessage = "For a multi-attribute, attribute value should not be populated";
							return jsonResponseOk(getStringApiResponseData(validationMessage,
									WorkbenchConstants.API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED,
									WorkbenchConstants.API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED));
						}
						if (!ListUtility.hasValue(attrData.getAttributes())) {
							String validationMessage = "For a multi-attribute, attributes value should be populated";
							return jsonResponseOk(getStringApiResponseData(validationMessage,
									WorkbenchConstants.API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED,
									WorkbenchConstants.API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED));
						}
					}
				}
			}
			List<EntityDbData> docEntityDbDataList = attributeProcess.updateAttribute(requestDataList);
			if (ListUtility.hasValue(docEntityDbDataList)) {
				prevdocEntityDbDataList.add(docEntityDbDataList.get(1));
				latestdocEntityDbDataList.add(docEntityDbDataList.get(0));
				apiResponse = 0 + " record(s) updated";
				if (docEntityDbDataList.get(0).getProcessedCount() > 0) {
					apiResponse = docEntityDbDataList.get(0).getProcessedCount() + " record(s) updated";
					apiResponseData = getStringApiResponseData(apiResponse, WorkbenchConstants.API_RESPONSE_CDE_SUCCESS,
							WorkbenchConstants.API_RESPONSE_MSG_SUCCESS);
				} else {
					apiResponseData = getStringApiResponseData(apiResponse,
							WorkbenchConstants.API_RESPONSE_CDE_NO_RECORDS,
							WorkbenchConstants.API_RESPONSE_MSG_NO_RECORDS);
				}
			} else {
				apiResponse = docEntityDbDataList.size() + " record(s) updated";
				apiResponseData = getStringApiResponseData(apiResponse, WorkbenchConstants.API_RESPONSE_CDE_NO_RECORDS,
						WorkbenchConstants.API_RESPONSE_MSG_NO_RECORDS);
			}

			return jsonResponseOk(apiResponseData);

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		} finally {
			auditProcess.addAuditDetails(prevdocEntityDbDataList, latestdocEntityDbDataList, EnumEntityType.ATTRIBUTE,
					EnumOperationType.UPDATE);
		}
	}

	@ApiOperation(value = "Delete one or more attributes", tags = "attribute")
	@RequestMapping(value = "/delete", method = RequestMethod.POST, consumes = {
			MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
	ResponseEntity<String> deleteAttribute(@RequestBody List<DeleteAttributeReqData> requestDataList) {
		List<EntityDbData> prevdocEntityDbDataList = new ArrayList<EntityDbData>();
		List<EntityDbData> latestdocEntityDbDataList = new ArrayList<EntityDbData>();
		ApiResponseData<String> apiResponseData = new ApiResponseData<>();
		String apiResponse = "";
		try {
			for (DeleteAttributeReqData data : requestDataList) {
				if (data.getDocId() <= 0) {
					String message = "Please provide document Id.";
					return jsonResponseOk(getStringApiResponseData(message,
							WorkbenchConstants.API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED,
							WorkbenchConstants.API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED));
				}
				List<AttributeDbData> dataList = attributeProcess.convertReqDataToAttributeList(data, data.getDocId());
				for (AttributeDbData attrData : dataList) {
					if (attrData.getAttrNameCde() == EnumSystemAttributeName.MULTI_ATTRIBUTE.getCde()) {
						if (!ListUtility.hasValue(attrData.getAttributes())) {
							String validationMessage = "For a multi-attribute, attributes value should be populated";
							return jsonResponseOk(getStringApiResponseData(validationMessage,
									WorkbenchConstants.API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED,
									WorkbenchConstants.API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED));
						}
					}
				}
			}
			List<EntityDbData> entityDbDatas = attributeProcess.deleteAttribute(requestDataList);
			if (ListUtility.hasValue(entityDbDatas)) {
				prevdocEntityDbDataList.add(entityDbDatas.get(1));
				latestdocEntityDbDataList.add(entityDbDatas.get(0));
				apiResponse = 0 + " record(s) deleted";
				if (entityDbDatas.get(0).getProcessedCount() > 0) {
					apiResponse = entityDbDatas.get(0).getProcessedCount() + " record(s) deleted";
					apiResponseData = getStringApiResponseData(apiResponse, WorkbenchConstants.API_RESPONSE_CDE_SUCCESS,
							WorkbenchConstants.API_RESPONSE_MSG_SUCCESS);
				} else {
					apiResponseData = getStringApiResponseData(apiResponse,
							WorkbenchConstants.API_RESPONSE_CDE_NO_RECORDS,
							WorkbenchConstants.API_RESPONSE_MSG_NO_RECORDS);
				}
			} else {
				apiResponse = entityDbDatas.size() + " record(s) deleted";
				apiResponseData = getStringApiResponseData(apiResponse, WorkbenchConstants.API_RESPONSE_CDE_NO_RECORDS,
						WorkbenchConstants.API_RESPONSE_MSG_NO_RECORDS);
			}
			return jsonResponseOk(apiResponseData);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		} finally {
			auditProcess.addAuditDetails(prevdocEntityDbDataList, latestdocEntityDbDataList, EnumEntityType.ATTRIBUTE,
					EnumOperationType.DELETE);
		}
	}

	@ApiOperation(value = "Get list of all attribute name(s), if configured", tags = "attribute")
	@RequestMapping(value = "/names", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<String> getAttributeList() {
		ApiResponseData<List<AttributeDbData>> apiResponseData = new ApiResponseData<List<AttributeDbData>>();
		try {
			List<AttributeDbData> resultList = attributeProcess.getAttributeText();
			apiResponseData.setResponse(resultList);
			abstract class MixIn {
				@JsonIgnore
				abstract AttributeDbData getDocId();

				@JsonIgnore
				abstract AttributeDbData getAttachmentId();

				@JsonIgnore
				abstract AttributeDbData getId();

				@JsonIgnore
				abstract AttributeDbData getExtractTypeCde();

				@JsonIgnore
				abstract AttributeDbData getConfidencePct();

				@JsonIgnore
				abstract AttributeDbData getExtractTypeTxt();

				@JsonIgnore
				abstract AttributeDbData getAttrValue();

				@JsonIgnore
				abstract AttributeDbData getAttributeDbDataList();

				@JsonIgnore
				abstract AttributeDbData getNotification();

				@JsonIgnore
				abstract AttributeDbData getAttributeId();
			}
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.addMixIn(AttributeDbData.class, MixIn.class);

			return jsonResponseOk(apiResponseData, objectMapper);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		}
	}

	@ApiOperation(value = "Add attribute source", tags = "attribute")
	@RequestMapping(value = "/source", method = RequestMethod.POST, consumes = {
			MediaType.APPLICATION_JSON_UTF8_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
	ResponseEntity<String> insertAttributeSource(@RequestBody AttributeSourceReqData attributeSourceReqData) {
		ApiResponseData<String> apiResponseData = new ApiResponseData<>();
		String apiResponse = "";
		List<EntityDbData> docEntityDbDataList = null;
		try {
			if (attributeSourceReqData.getDocId() <= 0) {
				String message = "Please provide valid doc Id.";
				return jsonResponseOk(
						getStringApiResponseData(message, WorkbenchConstants.API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED,
								WorkbenchConstants.API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED));
			}
			if (attributeSourceReqData.getRecord() == null) {
				String message = "Please provide valid doc attribute source data.";
				return jsonResponseOk(
						getStringApiResponseData(message, WorkbenchConstants.API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED,
								WorkbenchConstants.API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED));
			}
			docEntityDbDataList = attributeProcess.addAttributeSource(attributeSourceReqData);
			if (ListUtility.hasValue(docEntityDbDataList)) {
				apiResponse = docEntityDbDataList.size() + " record(s) added";
				apiResponseData = getStringApiResponseData(apiResponse, WorkbenchConstants.API_RESPONSE_CDE_SUCCESS,
						WorkbenchConstants.API_RESPONSE_MSG_SUCCESS);
			}

			return jsonResponseOk(apiResponseData);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		} finally {
			auditProcess.addAuditDetails(null, docEntityDbDataList, EnumEntityType.ATTRIBUTE, EnumOperationType.INSERT);
		}
	}

	@ApiOperation(value = "Get list of all attribute-to-attribute mapping", tags = "attribute")
	@RequestMapping(value = "/attribute/mapping", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<String> getAttributeAttibuteMapping() {
		ApiResponseData<List<AttributeNameResData>> apiResponseData = new ApiResponseData<>();
		try {
			List<AttributeNameResData> resultList = attributeProcess.getAttributeAttributeMapping();
			apiResponseData.setResponse(resultList);
			return jsonResponseOk(apiResponseData);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		}
	}

	@ApiOperation(value = "Get sort key for attributes", tags = "attribute")
	@RequestMapping(value = "/sortkey", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<String> getAttributeSortingKey() {
		ApiResponseData<List<AttributeNameResData>> apiResponseData = new ApiResponseData<>();
		try {
			List<AttributeNameResData> resultList = attributeProcess.getAttributeSortingKey();
			apiResponseData.setResponse(resultList);
			return jsonResponseOk(apiResponseData);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		}
	}

	@ApiOperation(value = "Get list of all (document) attributes", tags = "attribute")
	@RequestMapping(value = "/document", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<String> getAttributes(@RequestParam(value = "docId", required = false) Long docId) {
		ApiResponseData<List<AttributeDbData>> apiResponseData = new ApiResponseData<List<AttributeDbData>>();
		try {
			List<AttributeDbData> resultList = attributeProcess.getDocumentAttributes(docId);
			apiResponseData.setResponse(resultList);
			abstract class MixIn {
				@JsonIgnore
				abstract AttributeDbData getDocId();

				@JsonIgnore
				abstract AttributeDbData getAttachmentId();

				@JsonIgnore
				abstract AttributeDbData getNotification();

				@JsonIgnore
				abstract AttributeDbData getAttributeId();
			}
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.addMixIn(AttributeDbData.class, MixIn.class);

			return jsonResponseOk(apiResponseData, objectMapper);

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		}
	}

	@ApiOperation(value = "Get list of all (attachment) attribute(s) using docId or selected ones using [docId+attachmentId in CSV]", tags = "attribute")
	@RequestMapping(value = "/attachment", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	ResponseEntity<String> getAttributes(@RequestParam(value = "docId", required = true) Long docId,
			@RequestParam(value = "attachmentIds", required = false) String attachmentIds,
			@RequestParam(value = "origValue", required = false) boolean origValue) {
		ApiResponseData<List<AttributeDbData>> apiResponseData = new ApiResponseData<List<AttributeDbData>>();
		try {
			List<AttributeDbData> resultList = attributeProcess.getAttachmentAttributes(docId, attachmentIds,origValue);
			apiResponseData.setResponse(resultList);
			abstract class MixIn {
				@JsonIgnore
				abstract AttributeDbData getDocId();

				@JsonIgnore
				abstract AttributeDbData getNotification();

				@JsonIgnore
				abstract AttributeDbData getAttributeId();
			}
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.addMixIn(AttributeDbData.class, MixIn.class);

			return jsonResponseOk(apiResponseData, objectMapper);

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		}
	}

	@ApiOperation(value = "Export list of all (document and attachment) attributes", tags = "attribute")
	@RequestMapping(value = "/export", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<String> exportAttributes(@RequestParam(value = "docId", required = false) Long docId) {
		ApiResponseData<ExportAttributeResData> apiResponseData = new ApiResponseData<ExportAttributeResData>();
		try {
			ExportAttributeResData resultData = attributeProcess.exportAllAttributes(docId);

			apiResponseData.setResponse(resultData);
			return jsonResponseOk(apiResponseData);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		}
	}

	@ApiOperation(value = "Get list of allowed value(s) for attribute(s), if configured", tags = "attribute")
	@RequestMapping(value = "/values", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<String> getAttributeNameValues(
			@RequestParam(value = "attrNameCdes", required = false) String attrNameCdes) {
		ApiResponseData<List<AttributeNameValueResData>> apiResponseData = new ApiResponseData<List<AttributeNameValueResData>>();
		try {
			List<AttributeNameValueResData> resultList = attributeProcess.getAttributeNameValues(attrNameCdes);
			apiResponseData.setResponse(resultList);

			return jsonResponseOk(apiResponseData);

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		}
	}

	@ApiOperation(value = "Get notifications for attribute(s) based on business rules configured", tags = "attribute")
	@RequestMapping(value = "/notification", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<String> getAttributesNotification(@RequestParam(value = "docId") Long docId) {
		ApiResponseData<GetAttributeNotificationResData> apiResponseData = new ApiResponseData<GetAttributeNotificationResData>();
		try {
			GetAttributeNotificationResData responseData = attributeProcess.getAttributesNotification(docId);
			apiResponseData.setResponse(responseData);
			return jsonResponseOk(apiResponseData);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		}
	}

}
