/** =============================================================================================================== *
 * Copyright 2022 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


package com.infosys.ainauto.docwb.service.model.api;

import java.util.List;

public class UserTeammateResData {


	private String userName;
	private String userFullName;
	private String userEmail;
	private long userTypeCde;
	private String userTypeTxt;
	private int userSourceCde;
	private long userId;
	private String userLoginId;
	private String roleTypeTxt;
	private long roleTypeCde;
	private List<UserQueueResData> commonQueueList;
	
	public int getUserSourceCde() {
		return userSourceCde;
	}
	public void setUserSourceCde(int userSourceCde) {
		this.userSourceCde=userSourceCde;
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

	public String getUserLoginId() {
		return userLoginId;
	}
	public void setUserLoginId(String userLoginId) {
		this.userLoginId = userLoginId;
	}
	public List<UserQueueResData> getCommonQueueList() {
		return commonQueueList;
	}
	public void setCommonQueueList(List<UserQueueResData> commonQueueList) {
		this.commonQueueList = commonQueueList;
	}
	


}
