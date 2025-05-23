/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.db;

public class UserAuditDbData {

	private long appUserId;
	private String userFullName;
	private String roleTypeTxt;
	private String queueNameTxt;
	private boolean accountEnabled;

	public boolean isAccountEnabled() {
		return accountEnabled;
	}

	public void setAccountEnabled(boolean accountEnabled) {
		this.accountEnabled = accountEnabled;
	}

	public long getAppUserId() {
		return appUserId;
	}

	public void setAppUserId(long appUserId) {
		this.appUserId = appUserId;
	}

	public String getRoleTypeTxt() {
		return roleTypeTxt;
	}

	public void setRoleTypeTxt(String roleTypeTxt) {
		this.roleTypeTxt = roleTypeTxt;
	}

	public String getQueueNameTxt() {
		return queueNameTxt;
	}

	public void setQueueNameTxt(String queueNameTxt) {
		this.queueNameTxt = queueNameTxt;
	}

	public String getUserFullName() {
		return userFullName;
	}

	public void setUserFullName(String userFullName) {
		this.userFullName = userFullName;
	}

}
