/** =============================================================================================================== *
 * Copyright 2023 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.db;

public class DocUserDbData {
	private long docId;
	private long userId;
	private String userLoginId;
	private String userEmail;
	private String userFullName;
	private long roleTypeCde;
	private String roleTypeTxt;
	private long docRoleTypeCde;
	private String docRoleTypeTxt;
	private long userTypeCde;
	private String userTypeTxt;
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public String getUserLoginId() {
		return userLoginId;
	}
	public void setUserLoginId(String userLoginId) {
		this.userLoginId = userLoginId;
	}
	public String getUserEmail() {
		return userEmail;
	}
	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}
	public String getUserFullName() {
		return userFullName;
	}
	public void setUserFullName(String userFullName) {
		this.userFullName = userFullName;
	}
	public long getRoleTypeCde() {
		return roleTypeCde;
	}
	public void setRoleTypeCde(long roleTypeCde) {
		this.roleTypeCde = roleTypeCde;
	}
	public String getRoleTypeTxt() {
		return roleTypeTxt;
	}
	public void setRoleTypeTxt(String roleTypeTxt) {
		this.roleTypeTxt = roleTypeTxt;
	}
	public long getDocRoleTypeCde() {
		return docRoleTypeCde;
	}
	public void setDocRoleTypeCde(long docRoleTypeCde) {
		this.docRoleTypeCde = docRoleTypeCde;
	}
	public String getDocRoleTypeTxt() {
		return docRoleTypeTxt;
	}
	public void setDocRoleTypeTxt(String docRoleTypeTxt) {
		this.docRoleTypeTxt = docRoleTypeTxt;
	}
	public long getUserTypeCde() {
		return userTypeCde;
	}
	public void setUserTypeCde(long userTypeCde) {
		this.userTypeCde = userTypeCde;
	}
	public String getUserTypeTxt() {
		return userTypeTxt;
	}
	public void setUserTypeTxt(String userTypeTxt) {
		this.userTypeTxt = userTypeTxt;
	}
	public long getDocId() {
		return docId;
	}
	public void setDocId(long docId) {
		this.docId = docId;
	}
}
