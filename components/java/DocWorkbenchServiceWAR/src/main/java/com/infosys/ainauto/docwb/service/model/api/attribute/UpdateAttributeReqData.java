/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.api.attribute;

import java.util.List;

import com.infosys.ainauto.docwb.service.common.WorkbenchConstants;

public class UpdateAttributeReqData {

	public static class UpdateAttachmentAttrData {
		private long attachmentId;
		private List<UpdateAttributeData> attributes;

		public long getAttachmentId() {
			return attachmentId;
		}

		public void setAttachmentId(long attachmentId) {
			this.attachmentId = attachmentId;
		}

		public List<UpdateAttributeData> getAttributes() {
			return attributes;
		}

		public void setAttributes(List<UpdateAttributeData> attributes) {
			this.attributes = attributes;
		}
	}

	public static class NestedUpdateAttributeData {
		private long id;
		private String attrValue;
		private String attrNameTxt;
		private int extractTypeCde;
		// to allow zero as an explicitly set value by caller
		private float confidencePct = WorkbenchConstants.CONFIDENCE_PCT_UNSET;

		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
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

		public String getAttrNameTxt() {
			return attrNameTxt;
		}

		public void setAttrNameTxt(String attrNameTxt) {
			this.attrNameTxt = attrNameTxt;
		}

	}

	public static class UpdateAttributeData {
		private long id;
		private int attrNameCde;
		private String attrNameTxt;
		private String attrValue;
		private List<NestedUpdateAttributeData> attributes;
		private int extractTypeCde;
		private float confidencePct;

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

		public List<NestedUpdateAttributeData> getAttributes() {
			return attributes;
		}

		public void setAttributes(List<NestedUpdateAttributeData> attributes) {
			this.attributes = attributes;
		}

		public String getAttrNameTxt() {
			return attrNameTxt;
		}

		public void setAttrNameTxt(String attrNameTxt) {
			this.attrNameTxt = attrNameTxt;
		}

	}

	private long docId;
	private long docActionRelId=-1;
	private List<UpdateAttributeData> attributes;
	private List<UpdateAttachmentAttrData> attachments;

	public long getDocId() {
		return docId;
	}

	public void setDocId(long docId) {
		this.docId = docId;
	}

	public List<UpdateAttributeData> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<UpdateAttributeData> attributes) {
		this.attributes = attributes;
	}

	public List<UpdateAttachmentAttrData> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<UpdateAttachmentAttrData> attachments) {
		this.attachments = attachments;
	}

	public long getDocActionRelId() {
		return docActionRelId;
	}

	public void setDocActionRelId(long docActionRelId) {
		this.docActionRelId = docActionRelId;
	}
}
