/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.db;

public class DocAuditDbData {

	private long docId;
	private int taskStatusCde;
	private String attrNameTxt;
	private String actionNameTxt;
	private String emailSubject;
	private String fileName;
	private String attrValue;
	private String userFullName;
	private String taskStatusTxt;
	private String actionResult;
	private String snapShot;

	public String getTaskStatusTxt() {
		return taskStatusTxt;
	}

	public void setTaskStatusTxt(String taskStatusTxt) {
		this.taskStatusTxt = taskStatusTxt;
	}

	public int getTaskStatusCde() {
		return taskStatusCde;
	}

	public void setTaskStatusCde(int taskStatusCde) {
		this.taskStatusCde = taskStatusCde;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getActionNameTxt() {
		return actionNameTxt;
	}

	public void setActionNameTxt(String actionNameTxt) {
		this.actionNameTxt = actionNameTxt;
	}

	public String getEmailSubject() {
		return emailSubject;
	}

	public void setEmailSubject(String emailSubject) {
		this.emailSubject = emailSubject;
	}

	public long getDocId() {
		return docId;
	}

	public void setDocId(long docId) {
		this.docId = docId;
	}

	public String getAttrNameTxt() {
		return attrNameTxt;
	}

	public void setAttrNameTxt(String attrNameTxt) {
		this.attrNameTxt = attrNameTxt;
	}

	public String getUserFullName() {
		return userFullName;
	}

	public void setUserFullName(String userFullName) {
		this.userFullName = userFullName;
	}

	public String getAttrValue() {
		return attrValue;
	}

	public void setAttrValue(String attrValue) {
		this.attrValue = attrValue;
	}

	public String getActionResult() {
		return actionResult;
	}

	public void setActionResult(String actionResult) {
		this.actionResult = actionResult;
	}

	public String getSnapShot() {
		return snapShot;
	}

	public void setSnapShot(String snapShot) {
		this.snapShot = snapShot;
	}

}
