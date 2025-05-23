/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.api;

public class AppVarReqData {
	private long prevAppVarId;
	private String appVarKey;
	private String appVarValue;
	public long getPrevAppVarId() {
		return prevAppVarId;
	}
	public void setPrevAppVarId(long prevAppVarId) {
		this.prevAppVarId = prevAppVarId;
	}
	public String getAppVarKey() {
		return appVarKey;
	}
	public void setAppVarKey(String appVarKey) {
		this.appVarKey = appVarKey;
	}
	public String getAppVarValue() {
		return appVarValue;
	}
	public void setAppVarValue(String appVarValue) {
		this.appVarValue = appVarValue;
	}
	
}
