/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.api.attachment;

public class InsertAttachmentResData {
	private long attachmentId;
	private long docAttachmentRelId;
	private String groupName;

	public long getAttachmentId() {
		return attachmentId;
	}

	public void setAttachmentId(long attachmentId) {
		this.attachmentId = attachmentId;
	}

	public long getDocAttachmentRelId() {
		return docAttachmentRelId;
	}

	public void setDocAttachmentRelId(long docAttachmentRelId) {
		this.docAttachmentRelId = docAttachmentRelId;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

}
