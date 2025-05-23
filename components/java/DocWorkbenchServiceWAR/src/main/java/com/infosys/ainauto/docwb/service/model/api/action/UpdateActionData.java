/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.api.action;


public class UpdateActionData {
	private String actionResult;
	private String snapShot;
	private long docActionRelId;
	private int taskStatusCde=-1;//to allow zero as an explicitly set value by caller
	
	public String getActionResult() {
		return actionResult;
	}
	public void setActionResult(String actionResult) {
		this.actionResult = actionResult;
	}
	public long getDocActionRelId() {
		return docActionRelId;
	}
	public void setDocActionRelId(long docActionRelId) {
		this.docActionRelId = docActionRelId;
	}
	public int getTaskStatusCde() {
		return taskStatusCde;
	}
	public void setTaskStatusCde(int taskStatusCde) {
		this.taskStatusCde = taskStatusCde;
	}
	public String getSnapShot() {
		return snapShot;
	}
	public void setSnapShot(String snapShot) {
		this.snapShot = snapShot;
	}
}
