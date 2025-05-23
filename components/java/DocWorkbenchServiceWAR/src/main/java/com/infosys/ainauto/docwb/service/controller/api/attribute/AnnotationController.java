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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.infosys.ainauto.commonutils.FileUtility;
import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.service.common.AttributeValidator;
import com.infosys.ainauto.docwb.service.common.WorkbenchConstants;
import com.infosys.ainauto.docwb.service.common.type.EnumEntityType;
import com.infosys.ainauto.docwb.service.common.type.EnumOperationType;
import com.infosys.ainauto.docwb.service.controller.api.BaseController;
import com.infosys.ainauto.docwb.service.model.api.ApiResponseData;
import com.infosys.ainauto.docwb.service.model.api.annotation.ExportIOBReqData;
import com.infosys.ainauto.docwb.service.model.api.annotation.InsertAnnotationReqData;
import com.infosys.ainauto.docwb.service.model.db.AttachmentDbData;
import com.infosys.ainauto.docwb.service.model.db.EntityDbData;
import com.infosys.ainauto.docwb.service.process.attribute.IAnnotationProcess;
import com.infosys.ainauto.docwb.service.process.audit.IAuditProcess;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;;

@Controller
@CrossOrigin
@RequestMapping("/api/v1/annotation")
@Api(tags = { "annotation" })
public class AnnotationController extends BaseController {

	private static final Logger logger = LoggerFactory.getLogger(AnnotationController.class);

	@Autowired
	private IAuditProcess auditProcess;

	@Autowired
	private IAnnotationProcess annotationProcess;

	@Autowired
	private AttributeValidator attributeValidator;

	@ApiOperation(value = "Add one or more annotations", tags = "annotation")
	@RequestMapping(value = "/add", method = RequestMethod.POST, consumes = {
			MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
	ResponseEntity<String> insertAnnotation(@RequestBody List<InsertAnnotationReqData> requestDataList) {
		List<EntityDbData> prevDocEntityDbDataList = new ArrayList<EntityDbData>();
		List<EntityDbData> latestDocEntityDbDataList = new ArrayList<EntityDbData>();
		ApiResponseData<String> apiResponseData = new ApiResponseData<>();
		String apiResponse = "";
		try {

			ResponseEntity<String> responseEntity = attributeValidator.validateAddAnnotationRequest(requestDataList);
			if (responseEntity != null) {
				return responseEntity;
			}
			List<EntityDbData> docEntityDbDataList = annotationProcess.addAnnotation(requestDataList);
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

	@ApiOperation(value = "Get zip file of conll files from user/system annotation", tags = "annotation")
	@RequestMapping(value = "/export/iob", method = RequestMethod.POST, consumes = {
			MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_OCTET_STREAM_VALUE,
					MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<?> exportIOB(@RequestBody ExportIOBReqData requestData) {
		try {
			String validationMessage = attributeValidator.validateExportIobRequest(requestData);
			if (StringUtility.hasTrimmedValue(validationMessage)) {
				return jsonResponseOk(getStringApiResponseData(validationMessage,
						WorkbenchConstants.API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED,
						WorkbenchConstants.API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED));
			} else {
				AttachmentDbData attachmentDbData = annotationProcess.getAnnotationIOB(requestData);
				byte[] contents = FileUtility.readFile(attachmentDbData.getPhysicalPath());
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
				headers.setContentDispositionFormData("attachment", attachmentDbData.getLogicalName());
				return new ResponseEntity<byte[]>(contents, headers, HttpStatus.OK);
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		}
	}
}
