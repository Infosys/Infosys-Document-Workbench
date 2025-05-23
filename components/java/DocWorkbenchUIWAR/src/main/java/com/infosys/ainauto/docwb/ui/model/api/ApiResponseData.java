/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.ui.model.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.infosys.ainauto.commonutils.DateUtility;
import com.infosys.ainauto.docwb.ui.common.DocWorkbenchUIConstants;
import com.infosys.ainauto.docwb.ui.controller.api.BaseController;



public class ApiResponseData<T> {

	private T response;
	private int responseCde;
	private String responseMsg;
	private String timestamp;
	private long startTimeInMs;
	private double responseTimeInSecs;

	public ApiResponseData() {
		startTimeInMs = System.currentTimeMillis();
	}

	public T getResponse() {
		return response;
	}

	public void setResponse(T response) {
		this.response = response;
		populateResponseCdeMsg(this.response);
		this.timestamp = DateUtility.toString(new Date(), DocWorkbenchUIConstants.API_TIMESTAMP_FORMAT_12HR); // Set current
																										// time stamp
		this.responseTimeInSecs = (System.currentTimeMillis() - startTimeInMs) / 1000.0;
	}

	public int getResponseCde() {
		return responseCde;
	}

	public void setResponseCde(int responseCde) {
		this.responseCde = responseCde;
	}

	public String getResponseMsg() {
		return responseMsg;
	}

	public void setResponseMsg(String responseMsg) {
		this.responseMsg = responseMsg;
	}

	public String getTimestamp() {
		return timestamp;
	}

	private void populateResponseCdeMsg(T response) {

		if (response instanceof List) {
			List<T> data = (ArrayList<T>) response;
			if (data.size() == 0) {
				this.setResponseCde(BaseController.API_RESPONSE_CDE_NO_RECORDS);
				this.setResponseMsg(BaseController.API_RESPONSE_MSG_NO_RECORDS);
			} else {
				this.setResponseCde(BaseController.API_RESPONSE_CDE_SUCCESS);
				this.setResponseMsg(BaseController.API_RESPONSE_MSG_SUCCESS);
			}

		}
	}

	public double getResponseTimeInSecs() {
		return responseTimeInSecs;
	}

}
