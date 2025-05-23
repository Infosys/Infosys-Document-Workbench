/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.api.attribute;

import java.util.List;

public class DeleteAttributeReqData {

	public static class DeleteAttachmentAttrData {
		private long attachmentId;
		private List<DeleteAttributeData> attributes;

		public long getAttachmentId() {
			return attachmentId;
		}

		public void setAttachmentId(long attachmentId) {
			this.attachmentId = attachmentId;
		}

		public List<DeleteAttributeData> getAttributes() {
			return attributes;
		}

		public void setAttributes(List<DeleteAttributeData> attributes) {
			this.attributes = attributes;
		}
	}

	public static class NestedDeleteAttributeData {
		private long id;

		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}
	}

	public static class DeleteAttributeData {
		private long id;
		private int attrNameCde;
		private String attrNameTxt;
		private List<NestedDeleteAttributeData> attributes;

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

		public List<NestedDeleteAttributeData> getAttributes() {
			return attributes;
		}

		public void setAttributes(List<NestedDeleteAttributeData> attributes) {
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
	private List<DeleteAttributeData> attributes;
	private List<DeleteAttachmentAttrData> attachments;

	public long getDocId() {
		return docId;
	}

	public void setDocId(long docId) {
		this.docId = docId;
	}

	public List<DeleteAttributeData> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<DeleteAttributeData> attributes) {
		this.attributes = attributes;
	}

	public List<DeleteAttachmentAttrData> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<DeleteAttachmentAttrData> attachments) {
		this.attachments = attachments;
	}

	public long getDocActionRelId() {
		return docActionRelId;
	}

	public void setDocActionRelId(long docActionRelId) {
		this.docActionRelId = docActionRelId;
	}

}
