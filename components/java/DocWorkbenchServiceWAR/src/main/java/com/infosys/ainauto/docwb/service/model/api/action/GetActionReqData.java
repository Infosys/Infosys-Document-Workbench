/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.api.action;

public class GetActionReqData {
    
	private int queueNameCde;
    private long docId;
    private int actionNameCde;
    private int pageNumber;
    private int taskStatusCde;
    private String taskStatusOperator;
    
	public int getQueueNameCde() {
		return queueNameCde;
	}
	public void setQueueNameCde(int queueNameCde) {
		this.queueNameCde = queueNameCde;
	}
	public long getDocId() {
		return docId;
	}
	public void setDocId(long docId) {
		this.docId = docId;
	}
	public int getActionNameCde() {
		return actionNameCde;
	}
	public void setActionNameCde(int actionNameCde) {
		this.actionNameCde = actionNameCde;
	}
	public int getPageNumber() {
		return pageNumber;
	}
	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}
	public int getTaskStatusCde() {
		return taskStatusCde;
	}
	public void setTaskStatusCde(int taskStatusCde) {
		this.taskStatusCde = taskStatusCde;
	}
	public String getTaskStatusOperator() {
		return taskStatusOperator;
	}
	public void setTaskStatusOperator(String taskStatusOperator) {
		this.taskStatusOperator = taskStatusOperator;
	}
}
