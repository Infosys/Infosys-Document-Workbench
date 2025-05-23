/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.db;

import java.util.List;

public class EntityDbData {

	private List<Long> docAttrRelIdList;
	private List<Long> attachAttrRelIdList;
	private List<Long> docActionRelIdList;
	private List<Long> docAttachmentRelIdList;
	private List<Long> emailIdList;
	private List<Long> docAppUserRelIdList;
	private List<Long> appUserRoleRelIdList;
	private List<Long> appUserQueueRelIdList;
	private long docId;
	private long attachmentId;
	private int attrNameCde;
	private long appUserId;
	private long taskStatusCde;
	private String taskStatusTxt;
	private String actionResult;
	private String snapShot;
	private String outboundEmailMessage;
	private long processedCount;
	private long addProcessedCount;
	private long updateProcessedCount;
	private long deleteProcessedCount;
	private long attachmentCount;
	private boolean isDraft;
	private boolean accountEnabled;
	private boolean isUserPasswordChanged;
	private boolean isTaskStatusUpdate;
	private boolean isActionResultUpdated;
	private boolean isSnapShotUpdated;
	private String apiResponseData;
	private String tenantId;
	private String appVariableKey;
	private long appVariableId;
	private long attrSourceId;
	private long queueNameCde;
	private long updatedRowCount;
	private long parentAttachmentId;
	private long childAttachmentId;
	private long attaAttaRelId;
	

	public long getQueueNameCde() {
		return queueNameCde;
	}

	public void setQueueNameCde(long queueNameCde) {
		this.queueNameCde = queueNameCde;
	}

	public List<Long> getAttachAttrRelIdList() {
		return attachAttrRelIdList;
	}

	public void setAttachAttrRelIdList(List<Long> attachAttrRelIdList) {
		this.attachAttrRelIdList = attachAttrRelIdList;
	}

	public long getAttachmentId() {
		return attachmentId;
	}

	public void setAttachmentId(long attachmentId) {
		this.attachmentId = attachmentId;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public String getTaskStatusTxt() {
		return taskStatusTxt;
	}

	public void setTaskStatusTxt(String taskStatusTxt) {
		this.taskStatusTxt = taskStatusTxt;
	}

	public boolean isAccountEnabled() {
		return accountEnabled;
	}

	public void setAccountEnabled(boolean accountEnabled) {
		this.accountEnabled = accountEnabled;
	}

	public String getApiResponseData() {
		return apiResponseData;
	}

	public void setApiResponseData(String apiResponseData) {
		this.apiResponseData = apiResponseData;
	}

	public boolean isUserPasswordChanged() {
		return isUserPasswordChanged;
	}

	public void setUserPasswordChanged(boolean isUserPasswordChanged) {
		this.isUserPasswordChanged = isUserPasswordChanged;
	}

	public boolean isDraft() {
		return isDraft;
	}

	public void setDraft(boolean isDraft) {
		this.isDraft = isDraft;
	}

	public long getTaskStatusCde() {
		return taskStatusCde;
	}

	public void setTaskStatusCde(long taskStatusCde) {
		this.taskStatusCde = taskStatusCde;
	}

	public long getAttachmentCount() {
		return attachmentCount;
	}

	public void setAttachmentCount(long attachmentCount) {
		this.attachmentCount = attachmentCount;
	}

	public String getOutboundEmailMessage() {
		return outboundEmailMessage;
	}

	public void setOutboundEmailMessage(String outboundEmailMessage) {
		this.outboundEmailMessage = outboundEmailMessage;
	}

	public List<Long> getDocAttrRelIdList() {
		return docAttrRelIdList;
	}

	public void setDocAttrRelIdList(List<Long> docAttrRelIdList) {
		this.docAttrRelIdList = docAttrRelIdList;
	}

	public long getDocId() {
		return docId;
	}

	public void setDocId(long docId) {
		this.docId = docId;
	}

	public List<Long> getDocActionRelIdList() {
		return docActionRelIdList;
	}

	public void setDocActionRelIdList(List<Long> docActionRelIdList) {
		this.docActionRelIdList = docActionRelIdList;
	}

	public List<Long> getDocAttachmentRelIdList() {
		return docAttachmentRelIdList;
	}

	public void setDocAttachmentRelIdList(List<Long> docAttachmentRelIdList) {
		this.docAttachmentRelIdList = docAttachmentRelIdList;
	}

	public List<Long> getEmailIdList() {
		return emailIdList;
	}

	public void setEmailIdList(List<Long> emailIdList) {
		this.emailIdList = emailIdList;
	}

	public List<Long> getDocAppUserRelIdList() {
		return docAppUserRelIdList;
	}

	public void setDocAppUserRelIdList(List<Long> docAppUserRelIdList) {
		this.docAppUserRelIdList = docAppUserRelIdList;
	}

	public List<Long> getAppUserRoleRelIdList() {
		return appUserRoleRelIdList;
	}

	public void setAppUserRoleRelIdList(List<Long> appUserRoleRelIdList) {
		this.appUserRoleRelIdList = appUserRoleRelIdList;
	}

	public List<Long> getAppUserQueueRelIdList() {
		return appUserQueueRelIdList;
	}

	public void setAppUserQueueRelIdList(List<Long> appUserQueueRelIdList) {
		this.appUserQueueRelIdList = appUserQueueRelIdList;
	}

	public long getAppUserId() {
		return appUserId;
	}

	public void setAppUserId(long appUserId) {
		this.appUserId = appUserId;
	}

	public String getActionResult() {
		return actionResult;
	}

	public void setActionResult(String actionResult) {
		this.actionResult = actionResult;
	}

	public boolean isTaskStatusUpdate() {
		return isTaskStatusUpdate;
	}

	public void setTaskStatusUpdate(boolean isTaskStatusUpdate) {
		this.isTaskStatusUpdate = isTaskStatusUpdate;
	}

	public boolean isActionResultUpdated() {
		return isActionResultUpdated;
	}

	public void setActionResultUpdated(boolean isActionResultUpdated) {
		this.isActionResultUpdated = isActionResultUpdated;
	}

	public long getProcessedCount() {
		return processedCount;
	}

	public void setProcessedCount(long processedCount) {
		this.processedCount = processedCount;
	}

	public String getSnapShot() {
		return snapShot;
	}

	public void setSnapShot(String snapShot) {
		this.snapShot = snapShot;
	}

	public boolean isSnapShotUpdated() {
		return isSnapShotUpdated;
	}

	public void setSnapShotUpdated(boolean isSnapShotUpdated) {
		this.isSnapShotUpdated = isSnapShotUpdated;
	}

	public long getAddProcessedCount() {
		return addProcessedCount;
	}

	public void setAddProcessedCount(long addProcessedCount) {
		this.addProcessedCount = addProcessedCount;
	}

	public long getUpdateProcessedCount() {
		return updateProcessedCount;
	}

	public void setUpdateProcessedCount(long updateProcessedCount) {
		this.updateProcessedCount = updateProcessedCount;
	}

	public long getDeleteProcessedCount() {
		return deleteProcessedCount;
	}

	public void setDeleteProcessedCount(long deleteProcessedCount) {
		this.deleteProcessedCount = deleteProcessedCount;
	}

	public int getAttrNameCde() {
		return attrNameCde;
	}

	public void setAttrNameCde(int attrNameCde) {
		this.attrNameCde = attrNameCde;
	}

	public String getAppVariableKey() {
		return appVariableKey;
	}

	public void setAppVariableKey(String appVariableKey) {
		this.appVariableKey = appVariableKey;
	}

	public long getAppVariableId() {
		return appVariableId;
	}

	public void setAppVariableId(long appVariableId) {
		this.appVariableId = appVariableId;
	}

	public long getAttrSourceId() {
		return attrSourceId;
	}

	public void setAttrSourceId(long attrSourceId) {
		this.attrSourceId = attrSourceId;
	}

	public long getUpdatedRowCount() {
		return updatedRowCount;
	}

	public void setUpdatedRowCount(long updatedRowCount) {
		this.updatedRowCount = updatedRowCount;
	}
	public long getParentAttachmentId() {
		return parentAttachmentId;
	}

	public void setParentAttachmentId(long parentAttachmentId) {
		this.parentAttachmentId = parentAttachmentId;
	}

	public long getChildAttachmentId() {
		return childAttachmentId;
	}

	public void setChildAttachmentId(long childAttachmentId) {
		this.childAttachmentId = childAttachmentId;
	}

	public long getAttaAttaRelId() {
		return attaAttaRelId;
	}

	public void setAttaAttaRelId(long attaAttaRelId) {
		this.attaAttaRelId = attaAttaRelId;
	}

}