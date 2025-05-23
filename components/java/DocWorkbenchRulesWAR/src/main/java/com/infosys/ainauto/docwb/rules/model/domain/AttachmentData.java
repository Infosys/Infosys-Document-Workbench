/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.rules.model.domain;

import java.util.List;

public class AttachmentData {
	private long attachmentId;
	private List<AttributeData> attributes;

	public List<AttributeData> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<AttributeData> attributes) {
		this.attributes = attributes;
	}

	public long getAttachmentId() {
		return attachmentId;
	}

	public void setAttachmentId(long attachmentId) {
		this.attachmentId = attachmentId;
	}
}
