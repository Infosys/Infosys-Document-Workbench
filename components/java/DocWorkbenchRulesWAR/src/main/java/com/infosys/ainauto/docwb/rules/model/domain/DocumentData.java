/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.rules.model.domain;

import java.util.List;

public class DocumentData {

	private long docId;
	private int docTypeCde;
	private List<ActionData> actionDataList;
	private List<AttributeData> attributes;
	private List<AttachmentData> attachments;

	public long getDocId() {
		return docId;
	}

	public void setDocId(long docId) {
		this.docId = docId;
	}

	public List<ActionData> getActionDataList() {
		return actionDataList;
	}

	public void setActionDataList(List<ActionData> actionDataList) {
		this.actionDataList = actionDataList;
	}

	public List<AttributeData> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<AttributeData> attributes) {
		this.attributes = attributes;
	}

	public int getDocTypeCde() {
		return docTypeCde;
	}

	public void setDocTypeCde(int docTypeCde) {
		this.docTypeCde = docTypeCde;
	}

	public List<AttachmentData> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<AttachmentData> attachments) {
		this.attachments = attachments;
	}

}
