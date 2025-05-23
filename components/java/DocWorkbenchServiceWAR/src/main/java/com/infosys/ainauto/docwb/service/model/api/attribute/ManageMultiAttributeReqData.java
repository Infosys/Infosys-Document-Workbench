/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.api.attribute;

import java.util.List;

import com.infosys.ainauto.docwb.service.common.WorkbenchConstants;

public class ManageMultiAttributeReqData {
	private long docId;
	private long docActionRelId=-1;
	private MultiAttributeData attribute;
	private AttachmentData attachment;

	public long getDocId() {
		return docId;
	}

	public void setDocId(long docId) {
		this.docId = docId;
	}

	public MultiAttributeData getAttribute() {
		return attribute;
	}

	public void setAttribute(MultiAttributeData attribute) {
		this.attribute = attribute;
	}

	public AttachmentData getAttachment() {
		return attachment;
	}

	public void setAttachment(AttachmentData attachment) {
		this.attachment = attachment;
	}

	public static class MultiAttributeData {
		private long id;
		private int attrNameCde;
		private String attrNameTxt;
		private String attrValue;
		private int extractTypeCde;
		private float confidencePct = WorkbenchConstants.CONFIDENCE_PCT_UNSET;
		private List<AttributeData> addAttributes;
		private List<AttributeData> editAttributes;
		private List<AttributeData> deleteAttributes;

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

		public List<AttributeData> getAddAttributes() {
			return addAttributes;
		}

		public void setAddAttributes(List<AttributeData> addAttributes) {
			this.addAttributes = addAttributes;
		}

		public List<AttributeData> getEditAttributes() {
			return editAttributes;
		}

		public void setEditAttributes(List<AttributeData> editAttributes) {
			this.editAttributes = editAttributes;
		}

		public List<AttributeData> getDeleteAttributes() {
			return deleteAttributes;
		}

		public void setDeleteAttributes(List<AttributeData> deleteAttributes) {
			this.deleteAttributes = deleteAttributes;
		}

	}

	public static class AttributeData {
		private long id;
		private int attrNameCde;
		private String attrNameTxt;
		private String attrValue;
		private int extractTypeCde;
		private float confidencePct = WorkbenchConstants.CONFIDENCE_PCT_UNSET;
		private List<AttributeData> attributes;

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

		public List<AttributeData> getAttributes() {
			return attributes;
		}

		public void setAttributes(List<AttributeData> attributes) {
			this.attributes = attributes;
		}
	}

	public static class AttachmentData {
		private long attachmentId;
		private MultiAttributeData attribute;

		public long getAttachmentId() {
			return attachmentId;
		}

		public void setAttachmentId(long attachmentId) {
			this.attachmentId = attachmentId;
		}

		public MultiAttributeData getAttribute() {
			return attribute;
		}

		public void setAttribute(MultiAttributeData attribute) {
			this.attribute = attribute;
		}
	}

	public long getDocActionRelId() {
		return docActionRelId;
	}

	public void setDocActionRelId(long docActionRelId) {
		this.docActionRelId = docActionRelId;
	}

}
