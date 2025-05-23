/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.infosys.ainauto.docwb.service.model.db.AttributeDbData;

public class AttachmentResData {
	private long attachmentId;
	private String fileName;
	private String physicalName;
	private boolean isInlineImage;
	private int extractTypeCde;
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private List<AttributeDbData> attributes;
	private String groupName;
	private int sortOrder;

	public List<AttributeDbData> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<AttributeDbData> attributes) {
		this.attributes = attributes;
	}

	public boolean isInlineImage() {
		return isInlineImage;
	}

	public void setInlineImage(boolean isInlineImage) {
		this.isInlineImage = isInlineImage;
	}

	public long getAttachmentId() {
		return attachmentId;
	}

	public void setAttachmentId(long attachmentId) {
		this.attachmentId = attachmentId;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public int getExtractTypeCde() {
		return extractTypeCde;
	}

	public void setExtractTypeCde(int extractTypeCde) {
		this.extractTypeCde = extractTypeCde;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public int getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}

	public String getPhysicalName() {
		return physicalName;
	}

	public void setPhysicalName(String physicalName) {
		this.physicalName = physicalName;
	}

}
