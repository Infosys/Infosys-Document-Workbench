/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.rules.controller.api.attribute;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.docwb.rules.common.DocumentDataHelper;
import com.infosys.ainauto.docwb.rules.controller.BaseController;
import com.infosys.ainauto.docwb.rules.model.api.ApiResponseData;
import com.infosys.ainauto.docwb.rules.model.api.attribute.GetAttributeNotificationReqData;
import com.infosys.ainauto.docwb.rules.model.api.attribute.GetAttributeNotificationResData;
import com.infosys.ainauto.docwb.rules.model.domain.AttachmentData;
import com.infosys.ainauto.docwb.rules.model.domain.AttributeData;
import com.infosys.ainauto.docwb.rules.model.domain.DocumentData;
import com.infosys.ainauto.docwb.rules.process.attribute.IAttributeProcess;
import com.infosys.ainauto.docwb.rules.type.EnumApiResponseCde;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/v1/attribute")
@Tag(name = "attribute")
public class AttributeController extends BaseController {

	private static final Logger logger = LoggerFactory.getLogger(AttributeController.class);

	@Autowired
	private IAttributeProcess attributeProcess;

	@Operation(summary = "Get notification(s) for attribute(s)", tags = {"attribute"})
	@PostMapping(path = "/notification", consumes = "application/json", produces = "application/json")
	public ResponseEntity<String> getAttributesNotification(@RequestHeader("tenantId") String tenantId,
			@RequestBody GetAttributeNotificationReqData getNotificationAttributeReqData) {
		long startTime = System.nanoTime();
		try {

			DocumentData documentData = new DocumentData();
			documentData.setDocId(getNotificationAttributeReqData.getDocId());
			documentData.setDocTypeCde(getNotificationAttributeReqData.getDocTypeCde());
			documentData.setActionDataList(
					DocumentDataHelper.getActionList(getNotificationAttributeReqData.getActionDataList()));
			documentData
					.setAttributes(DocumentDataHelper.getAttributes(getNotificationAttributeReqData.getAttributes()));
			documentData.setAttachments(
					DocumentDataHelper.getAttachments(getNotificationAttributeReqData.getAttachments()));

			DocumentData responseData = attributeProcess.getAttributesNotification(tenantId, documentData);

			GetAttributeNotificationResData getAttributeNotificationResData = new GetAttributeNotificationResData();

			if (ListUtility.hasValue(responseData.getAttributes())) {
				List<GetAttributeNotificationResData.AttributeData> ganAttributeDataList = populateGanAttributes(
						responseData.getAttributes());
				getAttributeNotificationResData.setAttributes(ganAttributeDataList);
			}
			if (ListUtility.hasValue(responseData.getAttachments())) {
				List<GetAttributeNotificationResData.AttachmentData> attachmentResDataList = new ArrayList<>();
				for (AttachmentData attachmentData : responseData.getAttachments()) {
					GetAttributeNotificationResData.AttachmentData attachmentResData = new GetAttributeNotificationResData.AttachmentData();
					attachmentResData.setAttachmentId(attachmentData.getAttachmentId());
					attachmentResData.setAttributes(populateGanAttributes(attachmentData.getAttributes()));
					attachmentResDataList.add(attachmentResData);
				}
				getAttributeNotificationResData.setAttachments(attachmentResDataList);
			}
			ApiResponseData<GetAttributeNotificationResData> apiResponseData = new ApiResponseData<GetAttributeNotificationResData>();
			apiResponseData.setResponse(getAttributeNotificationResData);
			apiResponseData.setResponseMsg(EnumApiResponseCde.SUCCESS.getMessageValue());

			apiResponseData.setResponseTimeInSecs((System.nanoTime() - startTime) / 1000000000.0);
			return jsonResponseOk(apiResponseData);
		} catch (Exception ex) {
			try {
				ApiResponseData<String> apiResponseData = createErrorStringApiResponseData(ex);
				apiResponseData.setResponseTimeInSecs((System.nanoTime() - startTime) / 1000000000.0);
				return jsonResponseOk(apiResponseData);
			} catch (JsonProcessingException e) {
				logger.error(e.getMessage(), e);
				return jsonResponseInternalServerError(e);
			}
		}

	}

	@Operation(summary = "Get list of all attribute-to-attribute mapping at the tenant id level", tags = {"attribute"})
	@GetMapping(path = "/attribute/mapping", produces = "application/json")
	public ResponseEntity<String> getAttributeAttributeMapping(@RequestHeader("tenantId") String tenantId) {

		long startTime = System.nanoTime();
		try {
			ApiResponseData<Object> apiResponseData;
			Object attrAttrMappingRes = attributeProcess.getAttributeAttributeMapping(tenantId);

			apiResponseData = new ApiResponseData<>(attrAttrMappingRes, EnumApiResponseCde.SUCCESS.getCdeValue(),
					EnumApiResponseCde.SUCCESS.getMessageValue());
			apiResponseData.setResponseTimeInSecs((System.nanoTime() - startTime) / 1000000000.0);
			return jsonResponseOk(apiResponseData);
		} catch (Exception ex) {
			try {
				ApiResponseData<String> apiResponseData = createErrorStringApiResponseData(ex);
				apiResponseData.setResponseTimeInSecs((System.nanoTime() - startTime) / 1000000000.0);
				return jsonResponseOk(apiResponseData);
			} catch (JsonProcessingException e) {
				logger.error(e.getMessage(), e);
				return jsonResponseInternalServerError(e);
			}
		}
	}
	
	@Operation(summary = "Get sort key for attributes at the tenant id level", tags = {"attribute"})
	@GetMapping(path = "/sortkey", produces = "application/json")
	public ResponseEntity<String> getAttributeSortingKey(@RequestHeader("tenantId") String tenantId) {

		long startTime = System.nanoTime();
		try {
			ApiResponseData<Object> apiResponseData;
			Object sortKeys = attributeProcess.getAttributeSortingKey(tenantId);

			apiResponseData = new ApiResponseData<>(sortKeys, EnumApiResponseCde.SUCCESS.getCdeValue(),
					EnumApiResponseCde.SUCCESS.getMessageValue());
			apiResponseData.setResponseTimeInSecs((System.nanoTime() - startTime) / 1000000000.0);
			return jsonResponseOk(apiResponseData);
		} catch (Exception ex) {
			try {
				ApiResponseData<String> apiResponseData = createErrorStringApiResponseData(ex);
				apiResponseData.setResponseTimeInSecs((System.nanoTime() - startTime) / 1000000000.0);
				return jsonResponseOk(apiResponseData);
			} catch (JsonProcessingException e) {
				logger.error(e.getMessage(), e);
				return jsonResponseInternalServerError(e);
			}
		}
	}

	private List<GetAttributeNotificationResData.AttributeData> populateGanAttributes(
			List<AttributeData> attributeDataList) {
		List<GetAttributeNotificationResData.AttributeData> ganAttributeDataList = new ArrayList<>();
		if (ListUtility.hasValue(attributeDataList)) {
			ganAttributeDataList = new ArrayList<>();
			for (AttributeData attributeData : attributeDataList) {
				GetAttributeNotificationResData.AttributeData ganAttributeData = new GetAttributeNotificationResData.AttributeData();
				BeanUtils.copyProperties(attributeData, ganAttributeData);
				// Call recursively if child attributes found
				if (ListUtility.hasValue(attributeData.getAttributes())) {
					ganAttributeData.setAttributes(populateGanAttributes(attributeData.getAttributes()));
				}
				ganAttributeDataList.add(ganAttributeData);
			}
		}
		return ganAttributeDataList;
	}
}
