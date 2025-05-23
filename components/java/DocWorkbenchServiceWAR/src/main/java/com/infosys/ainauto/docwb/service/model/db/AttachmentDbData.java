/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.db;

public class AttachmentDbData {
	private Long attachmentId;
	private String logicalName;
	private String physicalName;
	private String physicalPath;
	private boolean isInlineImage;
	private int extractTypeCde;
	private String groupName;
	private boolean isPrimary;
	private Long sequenceNum;
	
	public boolean isInlineImage() {
		return isInlineImage;
	}

	public void setInlineImage(boolean isInlineImage) {
		this.isInlineImage = isInlineImage;
	}

	public Long getAttachmentId() {
		return attachmentId;
	}

	public void setAttachmentId(Long attachmentId) {
		this.attachmentId = attachmentId;
	}

	public String getLogicalName() {
		return logicalName;
	}

	public void setLogicalName(String logicalName) {
		this.logicalName = logicalName;
	}

	public String getPhysicalName() {
		return physicalName;
	}

	public void setPhysicalName(String physicalName) {
		this.physicalName = physicalName;
	}

	public String getPhysicalPath() {
		return physicalPath;
	}

	public void setPhysicalPath(String physicalPath) {
		this.physicalPath = physicalPath;
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

	public Long getSequenceNum() {
		return sequenceNum;
	}

	public void setSequenceNum(Long sequenceNum) {
		this.sequenceNum = sequenceNum;
	}

	public boolean isPrimary() {
		return isPrimary;
	}

	public void setPrimary(boolean isPrimary) {
		this.isPrimary = isPrimary;
	}

}
