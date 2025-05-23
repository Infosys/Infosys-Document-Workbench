/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.ui.model.api.query;

public class QueryReqData {
	
	private String api;
	private Object requestBody;
	public String getApi() {
		return api;
	}
	public void setApi(String api) {
		this.api = api;
	}
	public Object getRequestBody() {
		return requestBody;
	}
	public void setRequestBody(Object requestBody) {
		this.requestBody = requestBody;
	}
	
}
