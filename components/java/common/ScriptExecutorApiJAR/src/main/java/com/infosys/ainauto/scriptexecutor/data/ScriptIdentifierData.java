/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.scriptexecutor.data;

import java.util.List;

public class ScriptIdentifierData {

	private long categoryId;
	private long companyId;
	private String domain;
	private int executionMode;
	private int iapNodeTransport;
	private List<ParameterData> parameterDataList;
	private String path;
	private String referenceKey;
	private String remoteServerNames;
	private String responseNotificationCallbackURL;
	private long scriptId;
	private String scriptName;
	private String userName;
	private String password;
	
	public long getCategoryId() {
		return categoryId;
	}
	public void setCategoryId(long categoryId) {
		this.categoryId = categoryId;
	}
	public long getCompanyId() {
		return companyId;
	}
	public void setCompanyId(long companyId) {
		this.companyId = companyId;
	}
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public int getExecutionMode() {
		return executionMode;
	}
	public void setExecutionMode(int executionMode) {
		this.executionMode = executionMode;
	}
	public int getIapNodeTransport() {
		return iapNodeTransport;
	}
	public void setIapNodeTransport(int iapNodeTransport) {
		this.iapNodeTransport = iapNodeTransport;
	}
	public List<ParameterData> getParameterDataList() {
		return parameterDataList;
	}
	public void setParameterDataList(List<ParameterData> parameterDataList) {
		this.parameterDataList = parameterDataList;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getReferenceKey() {
		return referenceKey;
	}
	public void setReferenceKey(String referenceKey) {
		this.referenceKey = referenceKey;
	}
	public String getRemoteServerNames() {
		return remoteServerNames;
	}
	public void setRemoteServerNames(String remoteServerNames) {
		this.remoteServerNames = remoteServerNames;
	}
	public String getResponseNotificationCallbackURL() {
		return responseNotificationCallbackURL;
	}
	public void setResponseNotificationCallbackURL(String responseNotificationCallbackURL) {
		this.responseNotificationCallbackURL = responseNotificationCallbackURL;
	}
	public long getScriptId() {
		return scriptId;
	}
	public void setScriptId(long scriptId) {
		this.scriptId = scriptId;
	}
	public String getScriptName() {
		return scriptName;
	}
	public void setScriptName(String scriptName) {
		this.scriptName = scriptName;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}

}
