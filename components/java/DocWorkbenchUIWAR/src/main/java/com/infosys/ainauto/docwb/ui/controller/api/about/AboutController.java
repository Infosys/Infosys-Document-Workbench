/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.ui.controller.api.about;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.infosys.ainauto.docwb.ui.controller.api.BaseController;
import com.infosys.ainauto.docwb.ui.model.api.ApiResponseData;
import com.infosys.ainauto.docwb.ui.model.api.about.AboutResData;
import com.infosys.ainauto.docwb.ui.process.about.IAboutProcess;

@Controller
@RequestMapping("/api/v1/about")
@CrossOrigin
public class AboutController extends BaseController {
	private static final Logger logger = LoggerFactory.getLogger(AboutController.class);

	@Autowired
	private IAboutProcess aboutProcess;
	
	@RequestMapping(value = "", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_JSON_VALUE })
	ResponseEntity<String> getAbout() {
		ApiResponseData<AboutResData> apiResponseData;
		try {
			AboutResData aboutResData = aboutProcess.getProductDetails();
			apiResponseData = new ApiResponseData<>();
			apiResponseData.setResponse(aboutResData);
			apiResponseData.setResponseCde(API_RESPONSE_CDE_SUCCESS);
			apiResponseData.setResponseMsg(API_RESPONSE_MSG_SUCCESS);
			return jsonResponseOk(apiResponseData);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return jsonResponseInternalServerError(ex);
		}
	}

}
