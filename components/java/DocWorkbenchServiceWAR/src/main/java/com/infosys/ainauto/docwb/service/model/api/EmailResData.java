/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.api;

import java.util.List;

import com.infosys.ainauto.docwb.service.model.db.EmailDbData;

public class EmailResData extends EmailDbData {

	private List<AttachmentResData> inlineImageAttachmentDataList;

	public List<AttachmentResData> getInlineImageAttachmentDataList() {
		return inlineImageAttachmentDataList;
	}

	public void setInlineImageAttachmentDataList(List<AttachmentResData> inlineImageAttachmentDataList) {
		this.inlineImageAttachmentDataList = inlineImageAttachmentDataList;
	}

	private List<AttachmentResData> attachmentDataList;

	public List<AttachmentResData> getAttachmentDataList() {
		return attachmentDataList;
	}

	public void setAttachmentDataList(List<AttachmentResData> attachmentDataList) {
		this.attachmentDataList = attachmentDataList;
	}

}
