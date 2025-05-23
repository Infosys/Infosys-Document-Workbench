/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.rules.controller.api.template;

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

import com.infosys.ainauto.docwb.rules.common.DocumentDataHelper;
import com.infosys.ainauto.docwb.rules.controller.BaseController;
import com.infosys.ainauto.docwb.rules.model.api.ApiResponseData;
import com.infosys.ainauto.docwb.rules.model.api.template.FlattenedTemplateReqData;
import com.infosys.ainauto.docwb.rules.model.api.template.FlattenedTemplateResData;
import com.infosys.ainauto.docwb.rules.model.api.template.GetTemplateListResData;
import com.infosys.ainauto.docwb.rules.model.domain.DocumentData;
import com.infosys.ainauto.docwb.rules.model.domain.TemplateData;
import com.infosys.ainauto.docwb.rules.process.template.ITemplateProcess;
import com.infosys.ainauto.docwb.rules.type.EnumApiResponseCde;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/v1/template")
@Tag(name = "template")
public class TemplateController extends BaseController {

	private static final Logger logger = LoggerFactory.getLogger(TemplateController.class);

	@Autowired
	private ITemplateProcess templateProcess;

	@Operation(summary = "Get template list and content to be used for email response", tags = {"template"})
	@GetMapping(path = "/list", produces = "application/json")
	public ResponseEntity<String> getTemplateNames(@RequestHeader("tenantId") String tenantId) {
		long startTime = System.nanoTime();
		try {

			try {
				ApiResponseData<List<GetTemplateListResData>> apiResponseData;
				List<TemplateData> templateDataList = templateProcess.getTemplateList(tenantId);
				List<GetTemplateListResData> getTemplateListResDataList = new ArrayList<>();

				GetTemplateListResData getTemplateListResData = null;
				for (TemplateData templateData : templateDataList) {
					getTemplateListResData = new GetTemplateListResData();
					BeanUtils.copyProperties(templateData, getTemplateListResData);
					getTemplateListResDataList.add(getTemplateListResData);
				}

				apiResponseData = new ApiResponseData<>(getTemplateListResDataList,
						EnumApiResponseCde.SUCCESS.getCdeValue(), EnumApiResponseCde.SUCCESS.getMessageValue());
				apiResponseData.setResponseTimeInSecs((System.nanoTime() - startTime) / 1000000000.0);
				return jsonResponseOk(apiResponseData);
			} catch (Exception ex) {
				ApiResponseData<String> apiResponseData = createErrorStringApiResponseData(ex);
				apiResponseData.setResponseTimeInSecs((System.nanoTime() - startTime) / 1000000000.0);
				return jsonResponseOk(apiResponseData);
			}

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		}
	}

	@Operation(summary = "Get the Flattened Template list to be used for email response", tags = { "template"})
	@PostMapping(path = "/list/flattened", consumes = "application/json", produces = "application/json")
	public ResponseEntity<String> getFlattenedTemplates(@RequestHeader("tenantId") String tenantId,
			@RequestBody FlattenedTemplateReqData flattenedTemplateReqData) {
		long startTime = System.nanoTime();
		try {
			DocumentData documentData = new DocumentData();
			documentData.setDocId(flattenedTemplateReqData.getDocId());
			documentData.setDocTypeCde(flattenedTemplateReqData.getDocTypeCde());
			documentData
					.setActionDataList(DocumentDataHelper.getActionList(flattenedTemplateReqData.getActionDataList()));
			documentData.setAttributes(DocumentDataHelper.getAttributes(flattenedTemplateReqData.getAttributes()));
			documentData.setAttachments(DocumentDataHelper.getAttachments(flattenedTemplateReqData.getAttachments()));

			try {
				ApiResponseData<List<FlattenedTemplateResData>> apiResponseData;

				List<FlattenedTemplateResData> flattenedTemplateResDataList = templateProcess
						.getFlattenedTemplates(tenantId, documentData);

				apiResponseData = new ApiResponseData<>(flattenedTemplateResDataList,
						EnumApiResponseCde.SUCCESS.getCdeValue(), EnumApiResponseCde.SUCCESS.getMessageValue());
				apiResponseData.setResponseTimeInSecs((System.nanoTime() - startTime) / 1000000000.0);
				return jsonResponseOk(apiResponseData);
			} catch (Exception ex) {
				ApiResponseData<String> apiResponseData = createErrorStringApiResponseData(ex);

				apiResponseData.setResponseTimeInSecs((System.nanoTime() - startTime) / 1000000000.0);
				return jsonResponseOk(apiResponseData);
			}

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		}
	}
}
