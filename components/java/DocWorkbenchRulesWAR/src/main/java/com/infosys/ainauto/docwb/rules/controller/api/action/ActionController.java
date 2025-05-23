/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.rules.controller.api.action;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.infosys.ainauto.docwb.rules.common.DocumentDataHelper;
import com.infosys.ainauto.docwb.rules.controller.BaseController;
import com.infosys.ainauto.docwb.rules.model.api.ApiResponseData;
import com.infosys.ainauto.docwb.rules.model.api.action.GetRecommendedActionReqData;
import com.infosys.ainauto.docwb.rules.model.api.action.GetRecommendedActionResData;
import com.infosys.ainauto.docwb.rules.model.domain.DocumentData;
import com.infosys.ainauto.docwb.rules.process.action.IActionProcess;
import com.infosys.ainauto.docwb.rules.type.EnumApiResponseCde;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/v1/action")
@Tag(name = "action")
public class ActionController extends BaseController {

	private static final Logger logger = LoggerFactory.getLogger(ActionController.class);

	@Autowired
	private IActionProcess actionProcess;

	@Operation(summary = "Get recommended action(s) for document", tags = {"action"})
	@PostMapping(path = "/recommendation", consumes = "application/json", produces = "application/json")
	public ResponseEntity<String> getRecommendedActions(@RequestHeader("tenantId") String tenantId,
			@RequestBody GetRecommendedActionReqData getRecommendedActionReqData) {

		long startTime = System.nanoTime();
		try {

			DocumentData documentData = new DocumentData();
			documentData.setDocId(getRecommendedActionReqData.getDocId());
			documentData.setDocTypeCde(getRecommendedActionReqData.getDocTypeCde());
			documentData.setActionDataList(
					DocumentDataHelper.getActionList(getRecommendedActionReqData.getActionDataList()));
			documentData.setAttributes(DocumentDataHelper.getAttributes(getRecommendedActionReqData.getAttributes()));
			documentData
					.setAttachments(DocumentDataHelper.getAttachments(getRecommendedActionReqData.getAttachments()));

			try {
				ApiResponseData<List<GetRecommendedActionResData>> apiResponseData;
				List<GetRecommendedActionResData> getRecommendedActionResDataList = actionProcess
						.getRecommendedAction(tenantId, documentData);

				apiResponseData = new ApiResponseData<>(getRecommendedActionResDataList,
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
