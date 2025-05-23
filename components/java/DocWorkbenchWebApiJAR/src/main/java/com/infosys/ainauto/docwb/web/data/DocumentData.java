/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.web.data;

import java.util.List;

import com.infosys.ainauto.docwb.web.type.EnumDocType;
import com.infosys.ainauto.docwb.web.type.EnumLockStatus;
import com.infosys.ainauto.docwb.web.type.EnumTaskStatus;

public class DocumentData {

	private long docId;
	private EnumDocType docType;
	private String docLocation;
	private EnumTaskStatus taskStatus;
	private EnumLockStatus lockStatus;
	private int queueNameCde;
	private int eventTypeCde;
	private List<AttributeData> attributes;
	private List<AttachmentData> attachmentDataList;
	private List<AttachmentData> inlineAttachmentDataList;
	private List<AnnotationData> annotations;
	private int docTaskStatus;

	public EnumDocType getDocType() {
		return docType;
	}

	public DocumentData setDocType(EnumDocType docType) {
		this.docType = docType;
		return this;
	}

	public String getDocLocation() {
		return docLocation;
	}

	public DocumentData setDocLocation(String docLocation) {
		this.docLocation = docLocation;
		return this;
	}

	public List<AttributeData> getAttributes() {
		return attributes;
	}

	public DocumentData setAttributes(List<AttributeData> attributes) {
		this.attributes = attributes;
		return this;
	}

	public EnumTaskStatus getTaskStatus() {
		return taskStatus;
	}

	public DocumentData setTaskStatus(EnumTaskStatus taskStatus) {
		this.taskStatus = taskStatus;
		return this;
	}

	public int getDocTaskStatus() {
		return docTaskStatus;
	}

	public void setDocTaskStatus(int docTaskStatus) {
		this.docTaskStatus = docTaskStatus;
	}

	public int getEventTypeCde() {
		return eventTypeCde;
	}

	public void setEventTypeCde(int eventTypeCde) {
		this.eventTypeCde = eventTypeCde;
	}

	public EnumLockStatus getLockStatus() {
		return lockStatus;
	}

	public DocumentData setLockStatus(EnumLockStatus lockStatus) {
		this.lockStatus = lockStatus;
		return this;
	}

	public int getQueueNameCde() {
		return queueNameCde;
	}

	public DocumentData setQueueNameCde(int queueNameCde) {
		this.queueNameCde = queueNameCde;
		return this;
	}

	public long getDocId() {
		return docId;
	}

	public void setDocId(long docId) {
		this.docId = docId;
	}

	public List<AttachmentData> getAttachmentDataList() {
		return attachmentDataList;
	}

	public void setAttachmentDataList(List<AttachmentData> attachmentDataList) {
		this.attachmentDataList = attachmentDataList;
	}

	public List<AttachmentData> getInlineAttachmentDataList() {
		return inlineAttachmentDataList;
	}

	public void setInlineAttachmentDataList(List<AttachmentData> inlineAttachmentDataList) {
		this.inlineAttachmentDataList = inlineAttachmentDataList;
	}

	public List<AnnotationData> getAnnotations() {
		return annotations;
	}

	public void setAnnotations(List<AnnotationData> annotations) {
		this.annotations = annotations;
	}

}
