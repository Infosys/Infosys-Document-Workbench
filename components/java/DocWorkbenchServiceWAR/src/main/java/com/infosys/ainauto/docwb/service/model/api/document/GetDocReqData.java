/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.api.document;

import java.util.List;

public class GetDocReqData  {
    
	private int queueNameCde;
	private int taskStatusCde;
	private String taskStatusOperator;
	private int highestEventTypeCde;
	private String highestEventTypeOperator;
	private int latestEventTypeCde;
	private String latestEventTypeOperator;
	private String attrNameCdes;
	private String attachmentAttrNameCdes;
	private int lockStatusCde;
	private long docId;
	private int pageNumber;
	private long appUserId;
	private String sortOrder;
	private String documentName;
	private String assignedTo;
	private String assignedToKey;
	private String fromEventDtm;
	private String toEventDtm;
	private List<DocIdOperation> docIdOperationList;
	private int pageSize;
	private String sortByAttrNameCde;
	private Boolean isPagination;
	private String searchCriteria;
	private String queueNameCdes;
	private String fromCaseCreateDtm;
	private String toCaseCreateDtm;
	
	public static class DocIdOperation {
		private long docId;
		private String operator;
		
		public long getDocId() {
			return docId;
		}
		public void setDocId(long docId) {
			this.docId = docId;
		}
		public String getOperator() {
			return operator;
		}
		public void setOperator(String operator) {
			this.operator = operator;
		}
	}
	
	public int getQueueNameCde() {
		return queueNameCde;
	}
	public void setQueueNameCde(int queueNameCde) {
		this.queueNameCde = queueNameCde;
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
	public int getHighestEventTypeCde() {
		return highestEventTypeCde;
	}
	public void setHighestEventTypeCde(int highestEventTypeCde) {
		this.highestEventTypeCde = highestEventTypeCde;
	}
	public String getHighestEventTypeOperator() {
		return highestEventTypeOperator;
	}
	public void setHighestEventTypeOperator(String highestEventTypeOperator) {
		this.highestEventTypeOperator = highestEventTypeOperator;
	}
	public int getLatestEventTypeCde() {
		return latestEventTypeCde;
	}
	public void setLatestEventTypeCde(int latestEventTypeCde) {
		this.latestEventTypeCde = latestEventTypeCde;
	}
	public String getLatestEventTypeOperator() {
		return latestEventTypeOperator;
	}
	public void setLatestEventTypeOperator(String latestEventTypeOperator) {
		this.latestEventTypeOperator = latestEventTypeOperator;
	}
	public String getAttrNameCdes() {
		return attrNameCdes;
	}
	public void setAttrNameCdes(String attrNameCdes) {
		this.attrNameCdes = attrNameCdes;
	}
	public int getLockStatusCde() {
		return lockStatusCde;
	}
	public void setLockStatusCde(int lockStatusCde) {
		this.lockStatusCde = lockStatusCde;
	}
	public long getDocId() {
		return docId;
	}
	public void setDocId(long docId) {
		this.docId = docId;
	}
	public int getPageNumber() {
		return pageNumber;
	}
	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}
	public long getAppUserId() {
		return appUserId;
	}
	public void setAppUserId(long appUserId) {
		this.appUserId = appUserId;
	}
	public String getSortOrder() {
		return sortOrder;
	}
	public void setSortOrder(String sortOrder) {
		this.sortOrder = sortOrder;
	}
	public String getAttachmentAttrNameCdes() {
		return attachmentAttrNameCdes;
	}
	public void setAttachmentAttrNameCdes(String attachmentAttrNameCdes) {
		this.attachmentAttrNameCdes = attachmentAttrNameCdes;
	}
	
	public String getFromEventDtm() {
		return fromEventDtm ;
	}
	public void setFromEventDtm(String fromEventDtm) {
		this.fromEventDtm=fromEventDtm;
	}
	
	public String getToEventDtm() {
		return toEventDtm ;
	}
	public void setToEventDtm(String toEventDtm) {
		this.toEventDtm=toEventDtm;
	}
	public String getAssignedTo() {
		return assignedTo;
	}
	public void setAssignedTo(String assignedTo) {
		this.assignedTo = assignedTo;
	}
	public String getDocumentName() {
		return documentName;
	}
	public void setDocumentName(String documentName) {
		this.documentName = documentName;
	}
	public String getAssignedToKey() {
		return assignedToKey;
	}
	public void setAssignedToKey(String assignedToKey) {
		this.assignedToKey = assignedToKey;
	}

	public List<DocIdOperation> getDocIdOperationList() {
		return docIdOperationList;
	}
	public void setDocIdOperationList(List<DocIdOperation> docIdOperationList) {
		this.docIdOperationList = docIdOperationList;
	}
	public int getPageSize() {
		return pageSize;
	}
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	public String getSortByAttrNameCde() {
		return sortByAttrNameCde;
	}
	public void setSortByAttrNameCde(String sortByAttrNameCde) {
		this.sortByAttrNameCde = sortByAttrNameCde;
	}
	public Boolean getIsPagination() {
		return isPagination;
	}
	public void setIsPagination(Boolean isPagination) {
		this.isPagination = isPagination;
	}
	public String getFromCaseCreateDtm() {
		return fromCaseCreateDtm;
	}
	public void setFromCaseCreateDtm(String fromCaseCreateDtm) {
		this.fromCaseCreateDtm = fromCaseCreateDtm;
	}
	public String getToCaseCreateDtm() {
		return toCaseCreateDtm;
	}
	public void setToCaseCreateDtm(String toCaseCreateDtm) {
		this.toCaseCreateDtm = toCaseCreateDtm;
	}
	public String getSearchCriteria() {
		return searchCriteria;
	}
	public void setSearchCriteria(String searchCriteria) {
		this.searchCriteria = searchCriteria;
	}
	public String getQueueNameCdes() {
		return queueNameCdes;
	}
	public void setQueueNameCdes(String queueNameCdes) {
		this.queueNameCdes = queueNameCdes;
	}	
}
