/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.db;

public class AppVariableDbData {

	private long appVarId;
	private String appVarKey;
	private String appVarValue;
	private String tenantId;
	private String createBy;
	private String createDtm;
	private String lastModBy;
	private String lastModDtm;

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

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public String getCreateBy() {
		return createBy;
	}

	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}

	public String getCreateDtm() {
		return createDtm;
	}

	public void setCreateDtm(String createDtm) {
		this.createDtm = createDtm;
	}

	public String getLastModBy() {
		return lastModBy;
	}

	public void setLastModBy(String lastModBy) {
		this.lastModBy = lastModBy;
	}

	public String getLastModDtm() {
		return lastModDtm;
	}

	public void setLastModDtm(String lastModDtm) {
		this.lastModDtm = lastModDtm;
	}

}
