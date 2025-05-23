/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.rules.model.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.infosys.ainauto.commonutils.DateUtility;
import com.infosys.ainauto.docwb.rules.common.DocWbConstants;
import com.infosys.ainauto.docwb.rules.type.EnumApiResponseCde;

public class ApiResponseData<T> {

    private T response;
    private int responseCde;
    private String responseMsg;
    private String timestamp;
    private double responseTimeInSecs;
    
    public ApiResponseData(T response, int responseCde, String responseMsg) {
    	this.setResponse(response);
    	this.setResponseCde(responseCde);
    	this.setResponseMsg(responseMsg);
	}
    
    public ApiResponseData(){
    	
    }

    public T getResponse() { 
        return response;
    }

	public void setResponse(T response) {
		this.response = response;
		populateResponseCdeMsg(this.response);
		this.timestamp = DateUtility.toString(new Date(), DocWbConstants.API_TIMESTAMP_FORMAT_12HR); // Set current
																										// time stamp
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

    private void populateResponseCdeMsg(T response){
        
        if (response instanceof List){
            @SuppressWarnings("unchecked")
			List<T> data = (ArrayList<T>) response;
			if (data.size() == 0) {
				this.setResponseCde(EnumApiResponseCde.NO_RECORDS.getCdeValue());
				this.setResponseMsg(EnumApiResponseCde.NO_RECORDS.getMessageValue());
			} else {
				this.setResponseCde(EnumApiResponseCde.SUCCESS.getCdeValue());
				this.setResponseMsg(EnumApiResponseCde.SUCCESS.getMessageValue());
			}
		}
	}

	public double getResponseTimeInSecs() {
		return responseTimeInSecs;
	}

	public void setResponseTimeInSecs(double responseTimeInSecs) {
		this.responseTimeInSecs = responseTimeInSecs;
	}


    
}
