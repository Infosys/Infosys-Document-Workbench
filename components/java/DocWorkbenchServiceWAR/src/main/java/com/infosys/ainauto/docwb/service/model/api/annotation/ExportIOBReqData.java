/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.api.annotation;

import java.util.List;

public class ExportIOBReqData {

	public static class ExportIOBAttachmentData {

		private List<ExportIOBAttributeData> attributes;

		public List<ExportIOBAttributeData> getAttributes() {
			return attributes;
		}

		public void setAttributes(List<ExportIOBAttributeData> attributes) {
			this.attributes = attributes;
		}

	}

	public static class ExportIOBAttributeData {
		private int attrNameCde;
		private String attrValue;

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

	}

	public static class ExportIOBCreateDtmData {
		private String start;
		private String end;

		public String getStart() {
			return start;
		}

		public void setStart(String start) {
			this.start = start;
		}

		public String getEnd() {
			return end;
		}

		public void setEnd(String end) {
			this.end = end;
		}

	}

	private List<ExportIOBAttachmentData> attachments;
	private List<ExportIOBAttributeData> attributes;
	private ExportIOBCreateDtmData createDtm;

	public List<ExportIOBAttachmentData> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<ExportIOBAttachmentData> attachments) {
		this.attachments = attachments;
	}

	public List<ExportIOBAttributeData> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<ExportIOBAttributeData> attributes) {
		this.attributes = attributes;
	}

	public ExportIOBCreateDtmData getCreateDtm() {
		return createDtm;
	}

	public void setCreateDtm(ExportIOBCreateDtmData createDtm) {
		this.createDtm = createDtm;
	}

}
