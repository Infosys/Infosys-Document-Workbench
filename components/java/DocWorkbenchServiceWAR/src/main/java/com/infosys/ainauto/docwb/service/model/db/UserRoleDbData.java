/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.db;

public class UserRoleDbData {
	private int appUserId;
	private long userRoleTypeCde;
	private String userFullName;
	private String userEmail;
	private String userLoginId;
	private String userTypeTxt;
	private long userTypeCde;
	private boolean accountEnabled;
	private String userRoleTypeTxt;
	private long appUserRoleRelId;
	
	public long getAppUserRoleRelId() {
		return appUserRoleRelId;
	}
	public void setAppUserRoleRelId(long appUserRoleRelId) {
		this.appUserRoleRelId = appUserRoleRelId;
	}
	
	public String getUserFullName() {
		return userFullName;
	}
	public void setUserFullName(String userFullName) {
		this.userFullName = userFullName;
	}
	public String getUserEmail() {
		return userEmail;
	}
	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}
	public String getUserLoginId() {
		return userLoginId;
	}
	public void setUserLoginId(String userLoginId) {
		this.userLoginId = userLoginId;
	}
	public String getUserTypeTxt() {
		return userTypeTxt;
	}
	public void setUserTypeTxt(String userTypeTxt) {
		this.userTypeTxt = userTypeTxt;
	}
	public long getUserTypeCde() {
		return userTypeCde;
	}
	public void setUserTypeCde(long userTypeCde) {
		this.userTypeCde = userTypeCde;
	}
	public boolean getAccountEnabled() {
		return accountEnabled;
	}
	public void setAccountEnabled(boolean accountEnabled) {
		this.accountEnabled = accountEnabled;
	}
	public String getUserRoleTypeTxt() {
		return userRoleTypeTxt;
	}
	public void setUserRoleTypeTxt(String useRoleTypeTxt) {
		this.userRoleTypeTxt = useRoleTypeTxt;
	}
	public int getAppUserId() {
		return appUserId;
	}
	public void setAppUserId(int appUserId) {
		this.appUserId = appUserId;
	}
	public long getUserRoleTypeCde() {
		return userRoleTypeCde;
	}
	public void setUserRoleTypeCde(int userRoleTypeCde) {
		this.userRoleTypeCde = userRoleTypeCde;
	}

}
