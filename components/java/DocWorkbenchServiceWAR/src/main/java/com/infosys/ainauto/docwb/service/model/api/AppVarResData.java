/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.api;

public class AppVarResData {
	private long appVarId;
	private String appVarKey;
	private String appVarValue;
	public long getAppVarId() {
		return appVarId;
	}
	public void setAppVarId(long appVarId) {
		this.appVarId = appVarId;
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
