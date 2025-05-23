/** =============================================================================================================== *
 * Copyright 2022 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


package com.infosys.ainauto.docwb.ui.process.auth;

import java.util.HashMap;

import javax.json.JsonObject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.HttpClientBase;
import com.infosys.ainauto.docwb.ui.common.DocWorkbenchUIConstants;
import com.infosys.ainauto.docwb.ui.common.DocWorkbenchUIException;

@Component
public class AuthorizationProcess extends HttpClientBase implements IAuthorizationProcess{
	@Value("${docwb.service.api.auth.validate}")
	private String DOCWB_SERVICE_API_AUTH_VALIDATE;
	/**
     * THIS METHOD USED TO CALL DOCWBSERVICE /API/V1/AUTH/VALIDATE API TO VALIDATE DOCWBUI LOGIN USER AUTHTOKEN.
     * @return api json response.
     */
	public JsonObject validate(String authHeader) throws DocWorkbenchUIException {
		HashMap<String, String> headerPropertiesMap = new HashMap<>();
		headerPropertiesMap.put(DocWorkbenchUIConstants.AUTH_HEADER_PROP_NAME, authHeader);
		JsonObject jsonResponse = (JsonObject) executeHttpCall(HttpCallType.GET, DOCWB_SERVICE_API_AUTH_VALIDATE, headerPropertiesMap);
		return jsonResponse;
	}

}
