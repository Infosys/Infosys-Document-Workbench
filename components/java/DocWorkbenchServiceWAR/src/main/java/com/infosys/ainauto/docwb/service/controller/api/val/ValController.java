/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.controller.api.val;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.infosys.ainauto.docwb.service.model.db.ValTableDbData;
import com.infosys.ainauto.docwb.service.process.val.IValProcess;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Controller
@CrossOrigin
@RequestMapping("/api/v1/val")
@Api(tags = { "val" })
public class ValController extends BaseController {

	private static final Logger logger = LoggerFactory.getLogger(ValController.class);

	@Autowired
	private IValProcess valProcess;

	@ApiOperation(value = "Get static values list for a given entity", tags = "val")
	@RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	ResponseEntity<String> getValTableData(@RequestParam("entity") String entity) {
		try {
			List<ValTableDbData> valTableDbDataList = valProcess.getTaskStatusVal(entity);

			ApiResponseData<List<ValTableDbData>> apiResponseData = new ApiResponseData<List<ValTableDbData>>();
			apiResponseData.setResponse(valTableDbDataList);

			abstract class MixIn {
				@JsonIgnore
				abstract boolean getCreateBy(); // we don't need it!

				@JsonIgnore
				abstract boolean getCreateDtm();

				@JsonIgnore
				abstract boolean getLastModBy();

				@JsonIgnore
				abstract boolean getLastModDtm();
			}
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.addMixIn(ValTableDbData.class, MixIn.class);

			return jsonResponseOk(apiResponseData, objectMapper);

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		}
	}
}
