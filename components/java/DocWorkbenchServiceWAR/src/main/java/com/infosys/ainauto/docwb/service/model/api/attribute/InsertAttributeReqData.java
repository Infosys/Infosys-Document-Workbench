/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.api.attribute;

import java.util.List;

import com.infosys.ainauto.docwb.service.common.WorkbenchConstants;

public class InsertAttributeReqData {
	public static class InsertAttributeData {

		private long id;
		private int attrNameCde;
		private String attrNameTxt;
		private String attrValue;
		private int extractTypeCde;
		// to allow zero as an explicitly set value by caller
		private float confidencePct = WorkbenchConstants.CONFIDENCE_PCT_UNSET; 
		private List<InsertAttributeData> attributes;

		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}

		public int getAttrNameCde() {
			return attrNameCde;
		}

		public void setAttrNameCde(int attrNameCde) {
			this.attrNameCde = attrNameCde;
		}

		public String getAttrNameTxt() {
			return attrNameTxt;
		}

		public void setAttrNameTxt(String attrNameTxt) {
			this.attrNameTxt = attrNameTxt;
		}

		public String getAttrValue() {
			return attrValue;
		}

		public void setAttrValue(String attrValue) {
			this.attrValue = attrValue;
		}

		public int getExtractTypeCde() {
			return extractTypeCde;
		}

		public void setExtractTypeCde(int extractTypeCde) {
			this.extractTypeCde = extractTypeCde;
		}

		public float getConfidencePct() {
			return confidencePct;
		}

		public void setConfidencePct(float confidencePct) {
			this.confidencePct = confidencePct;
		}

		public List<InsertAttributeData> getAttributes() {
			return attributes;
		}

		public void setAttributes(List<InsertAttributeData> attributes) {
			this.attributes = attributes;
		}

	}

	public static class InsertAttachmentAttrData {
		private long attachmentId;
		private List<InsertAttributeData> attributes;

		public long getAttachmentId() {
			return attachmentId;
		}

		public void setAttachmentId(long attachmentId) {
			this.attachmentId = attachmentId;
		}

		public List<InsertAttributeData> getAttributes() {
			return attributes;
		}

		public void setAttributes(List<InsertAttributeData> attributes) {
			this.attributes = attributes;
		}
	}

	private long docId;
	private long docActionRelId=-1;
	private List<InsertAttributeData> attributes;
	private List<InsertAttachmentAttrData> attachments;

	public long getDocId() {
		return docId;
	}

	public void setDocId(long docId) {
		this.docId = docId;
	}

	public List<InsertAttributeData> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<InsertAttributeData> attributes) {
		this.attributes = attributes;
	}

	public List<InsertAttachmentAttrData> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<InsertAttachmentAttrData> attachments) {
		this.attachments = attachments;
	}

	public long getDocActionRelId() {
		return docActionRelId;
	}

	public void setDocActionRelId(long docActionRelId) {
		this.docActionRelId = docActionRelId;
	}
}
