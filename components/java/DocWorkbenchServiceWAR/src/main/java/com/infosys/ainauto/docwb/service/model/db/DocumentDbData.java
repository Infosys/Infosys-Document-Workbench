/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.db;
import java.util.List;

public class DocumentDbData {

	private long docId;
	private int docTypeCde;
	private String docLocation;
	private int taskStatusCde;
	private String taskStatusOperator;
	private String taskStatusTxt;
	private int lockStatusCde;
	private String lockStatusTxt;
	private int attachmentCount;
	private int queueNameCde;
	private long appUserId;
	private String attrNameCdes;
	private String attachmentAttrNameCdes;
	private String queueNameTxt;
	private int eventTypeCde;
	private int pageNumber;
	private int highestEventTypeCde;
	private String highestEventTypeOperator;
	private int latestEventTypeCde;
	private String latestEventTypeOperator;
	private String createDtm;
	private String sortOrder;
	private String fromEventDtm;
	private String toEventDtm;
	private String documentName;
	private String assignedTo;
	private String assignedToKey;
	private int pageSize;
	private String sortByAttrNameCde;
	private List<DocIdOperation> docIdOperationList;
	private String fromCaseCreateDtm;
	private String toCaseCreateDtm;
	private long caseReviewerId;
	private String caseReviewer;
	
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

	public long getDocId() {
		return docId;
	}

	public void setDocId(long docId) {
		this.docId = docId;
	}

	public int getDocTypeCde() {
		return docTypeCde;
	}

	public void setDocTypeCde(int docTypeCde) {
		this.docTypeCde = docTypeCde;
	}

	public int getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}

	public String getDocLocation() {
		return docLocation;
	}

	public void setDocLocation(String docLocation) {
		this.docLocation = docLocation;
	}

	public int getLockStatusCde() {
		return lockStatusCde;
	}

	public void setLockStatusCde(int lockStatusCde) {
		this.lockStatusCde = lockStatusCde;
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

	public String getTaskStatusTxt() {
		return taskStatusTxt;
	}

	public void setTaskStatusTxt(String taskStatusTxt) {
		this.taskStatusTxt = taskStatusTxt;
	}

	public String getLockStatusTxt() {
		return lockStatusTxt;
	}

	public void setLockStatusTxt(String lockStatusTxt) {
		this.lockStatusTxt = lockStatusTxt;
	}

	public int getQueueNameCde() {
		return queueNameCde;
	}

	public void setQueueNameCde(int queueNameCde) {
		this.queueNameCde = queueNameCde;
	}

	public String getQueueNameTxt() {
		return queueNameTxt;
	}

	public void setQueueNameTxt(String queueNameTxt) {
		this.queueNameTxt = queueNameTxt;
	}

	public int getEventTypeCde() {
		return eventTypeCde;
	}

	public void setEventTypeCde(int eventTypeCde) {
		this.eventTypeCde = eventTypeCde;
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

	public int getAttachmentCount() {
		return attachmentCount;
	}

	public void setAttachmentCount(int attachmentCount) {
		this.attachmentCount = attachmentCount;
	}

	public long getAppUserId() {
		return appUserId;
	}

	public void setAppUserId(long appUserId) {
		this.appUserId = appUserId;
	}

	public String getAssignedTo() {
		return assignedTo;
	}

	public void setAssignedTo(String assignedTo) {
		this.assignedTo = assignedTo;
	}

	public String getCreateDtm() {
		return createDtm;
	}

	public void setCreateDtm(String createDtm) {
		this.createDtm = createDtm;
	}

	public String getAttrNameCdes() {
		return attrNameCdes;
	}

	public void setAttrNameCdes(String attrNameCdes) {
		this.attrNameCdes = attrNameCdes;
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

	public long getCaseReviewerId() {
		return caseReviewerId;
	}

	public void setCaseReviewerId(long caseReviewerId) {
		this.caseReviewerId = caseReviewerId;
	}

	public String getCaseReviewer() {
		return caseReviewer;
	}

	public void setCaseReviewer(String caseReviewer) {
		this.caseReviewer = caseReviewer;
	}
	
}
