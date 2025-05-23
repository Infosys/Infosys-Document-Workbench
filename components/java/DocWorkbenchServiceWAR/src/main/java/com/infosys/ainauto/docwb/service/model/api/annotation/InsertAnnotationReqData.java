/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.api.annotation;

import java.util.List;

public class InsertAnnotationReqData {

	public static class AnnotationReqData {

		private String value;
		private String label;
		private int occurrenceNum;
		private int page;
		private List<Integer> pageBbox;
		private List<Integer> sourceBbox;

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public int getOccurrenceNum() {
			return occurrenceNum;
		}

		public void setOccurrenceNum(int occurrenceNum) {
			this.occurrenceNum = occurrenceNum;
		}

		public int getPage() {
			return page;
		}

		public void setPage(int page) {
			this.page = page;
		}

		public List<Integer> getPageBbox() {
			return pageBbox;
		}

		public void setPageBbox(List<Integer> pageBbox) {
			this.pageBbox = pageBbox;
		}

		public List<Integer> getSourceBbox() {
			return sourceBbox;
		}

		public void setSourceBbox(List<Integer> sourceBbox) {
			this.sourceBbox = sourceBbox;
		}

	}

	public static class AttachmentAnnotationData {
		private long attachmentId;
		private List<AnnotationReqData> annotations;

		public long getAttachmentId() {
			return attachmentId;
		}

		public void setAttachmentId(long attachmentId) {
			this.attachmentId = attachmentId;
		}

		public List<AnnotationReqData> getAnnotations() {
			return annotations;
		}

		public void setAnnotations(List<AnnotationReqData> annotations) {
			this.annotations = annotations;
		}

	}

	private long docId;
	private List<AttachmentAnnotationData> attachments;
	private List<AnnotationReqData> annotations;

	public long getDocId() {
		return docId;
	}

	public void setDocId(long docId) {
		this.docId = docId;
	}

	public List<AttachmentAnnotationData> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<AttachmentAnnotationData> attachments) {
		this.attachments = attachments;
	}

	public List<AnnotationReqData> getAnnotations() {
		return annotations;
	}

	public void setAnnotations(List<AnnotationReqData> annotations) {
		this.annotations = annotations;
	}

}
