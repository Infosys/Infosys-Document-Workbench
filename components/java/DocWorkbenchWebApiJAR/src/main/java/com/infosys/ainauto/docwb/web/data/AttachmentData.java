/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.web.data;

import java.util.List;

public class AttachmentData {

	private long docId;
	private long attachmentId;
	private String logicalName;
	private String physicalName;
	private String physicalPath;
	private boolean isInlineImage;
	private int extractTypeCde;
	private String groupName;
	private List<AttributeData> attributes;
	private List<AnnotationData> annotations;
	private int sortOrder;

	public boolean isInlineImage() {
		return isInlineImage;
	}

	public void setInlineImage(boolean isInlineImage) {
		this.isInlineImage = isInlineImage;
	}

	public long getDocId() {
		return docId;
	}

	public void setDocId(long docId) {
		this.docId = docId;
	}

	public long getAttachmentId() {
		return attachmentId;
	}

	public void setAttachmentId(long attachmentId) {
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

	public List<AttributeData> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<AttributeData> attributes) {
		this.attributes = attributes;
	}

	public List<AnnotationData> getAnnotations() {
		return annotations;
	}

	public void setAnnotations(List<AnnotationData> annotations) {
		this.annotations = annotations;
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

}
