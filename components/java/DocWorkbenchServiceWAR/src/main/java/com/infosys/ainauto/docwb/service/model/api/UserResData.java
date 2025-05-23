/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.api;

import java.util.List;

public class UserResData {

	private String userName;
	private String userPassword;
	private String userFullName;
	private String userEmail;
	private long userTypeCde;
	private String userTypeTxt;
	private int userSourceCde;
	private long userId;
	private String userLoginId;
	private boolean accountEnabled;
	private String roleTypeTxt;
	private long roleTypeCde;
	private String tenantId;
	private List<UserQueueResData> queueDataList;
	private List<UserFeatureAuthResData> featureAuthDataList;
	

	public int getUserSourceCde() {
		return userSourceCde;
	}
	public void setUserSourceCde(int userSourceCde) {
		this.userSourceCde=userSourceCde;
	}
		
	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public List<UserQueueResData> getQueueDataList() {
		return queueDataList;
	}

	public void setQueueDataList(List<UserQueueResData> list) {
		this.queueDataList = list;
	}

	public boolean isAccountEnabled() {
		return accountEnabled;
	}

	public void setAccountEnabled(boolean accountEnabled) {
		this.accountEnabled = accountEnabled;
	}

	public String getRoleTypeTxt() {
		return roleTypeTxt;
	}

	public void setRoleTypeTxt(String roleTypeTxt) {
		this.roleTypeTxt = roleTypeTxt;
	}

	public long getRoleTypeCde() {
		return roleTypeCde;
	}

	public void setRoleTypeCde(long l) {
		this.roleTypeCde = l;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserPassword() {
		return userPassword;
	}

	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public String getUserFullName() {
		return userFullName;
	}

	public void setUserFullName(String userFullName) {
		this.userFullName = userFullName;
	}

	public long getUserTypeCde() {
		return userTypeCde;
	}

	public void setUserTypeCde(long userTypeCde) {
		this.userTypeCde = userTypeCde;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public String getUserTypeTxt() {
		return userTypeTxt;
	}

	public void setUserTypeTxt(String userTypeTxt) {
		this.userTypeTxt = userTypeTxt;
	}

	public List<UserFeatureAuthResData> getFeatureAuthDataList() {
		return featureAuthDataList;
	}

	public void setFeatureAuthDataList(List<UserFeatureAuthResData> featureAuthDataList) {
		this.featureAuthDataList = featureAuthDataList;
	}
	public String getUserLoginId() {
		return userLoginId;
	}
	public void setUserLoginId(String userLoginId) {
		this.userLoginId = userLoginId;
	}

}
