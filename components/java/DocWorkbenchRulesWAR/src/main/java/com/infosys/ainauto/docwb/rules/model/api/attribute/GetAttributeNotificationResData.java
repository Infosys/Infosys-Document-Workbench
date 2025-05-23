/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


package com.infosys.ainauto.docwb.rules.model.api.attribute;

import java.util.List;

public class GetAttributeNotificationResData {

	public static class AttributeData {

		private long id;
		private List<AttributeData> attributes;
		private String notification;

		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}

		public List<AttributeData> getAttributes() {
			return attributes;
		}

		public void setAttributes(List<AttributeData> attributes) {
			this.attributes = attributes;
		}

		public String getNotification() {
			return notification;
		}

		public void setNotification(String notification) {
			this.notification = notification;
		}

	}

	public static class AttachmentData {

		private long attachmentId;
		private List<AttributeData> attributes;

		public long getAttachmentId() {
			return attachmentId;
		}

		public void setAttachmentId(long attachmentId) {
			this.attachmentId = attachmentId;
		}

		public List<AttributeData> getAttributes() {
			return attributes;
		}

		public void setAttributes(List<AttributeData> attributes) {
			this.attributes = attributes;
		}

	}

	private List<AttributeData> attributes;
	private List<AttachmentData> attachments;

	public List<AttributeData> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<AttributeData> attributes) {
		this.attributes = attributes;
	}

	public List<AttachmentData> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<AttachmentData> attachments) {
		this.attachments = attachments;
	}

}
