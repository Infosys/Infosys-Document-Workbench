/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.controller.api.template;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infosys.ainauto.docwb.service.controller.api.BaseController;
import com.infosys.ainauto.docwb.service.model.api.ApiResponseData;
import com.infosys.ainauto.docwb.service.model.db.ActionTempMappingDbData;
import com.infosys.ainauto.docwb.service.process.template.ITemplateProcess;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Controller
@CrossOrigin
@RequestMapping("/api/v1/template")
@Api(tags = { "template" })
public class TemplateController extends BaseController {

	private static final Logger logger = LoggerFactory.getLogger(TemplateController.class);

	@Autowired
	private ITemplateProcess templateProcess;

	@ApiOperation(value = "Get a list of templates", tags = "template")
	@RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	ResponseEntity<String> getTemplateList() {
		try {
			List<ActionTempMappingDbData> resultList = templateProcess.getTemplates();

			ApiResponseData<List<ActionTempMappingDbData>> apiResponseData = new ApiResponseData<List<ActionTempMappingDbData>>();
			apiResponseData.setResponse(resultList);

			abstract class MixIn {
				@JsonIgnore
				abstract long getIsRecommendedTemplate();
			}

			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.addMixIn(ActionTempMappingDbData.class, MixIn.class);
			return jsonResponseOk(apiResponseData, objectMapper);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return new ResponseEntity<String>(null, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ApiOperation(value = "Returns templates flattened using extracted data", tags = "template")
	@RequestMapping(value = "/flattened", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	ResponseEntity<String> getTemplateList(@RequestParam(value = "docId", required = false) Long docId) {
		try {

			ApiResponseData<List<ActionTempMappingDbData>> apiResponseData = new ApiResponseData<List<ActionTempMappingDbData>>();

			List<ActionTempMappingDbData> resultList = templateProcess.getTemplatesWithData(docId);
			apiResponseData.setResponse(resultList);
			ObjectMapper objectMapper = new ObjectMapper();
			return jsonResponseOk(apiResponseData, objectMapper);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return new ResponseEntity<String>(null, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
