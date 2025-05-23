/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.rules.model.api.action;

import java.util.List;

public class GetRecommendedActionReqData {

	public static class ActionData {

		private int actionNameCde;
		private String actionNameTxt;
		private String actionResult;
		private int actionStatusCde;
		private String actionStatusTxt;
		private List<ParamData> paramList;

		public int getActionStatusCde() {
			return actionStatusCde;
		}

		public void setActionStatusCde(int actionStatusCde) {
			this.actionStatusCde = actionStatusCde;
		}

		public String getActionStatusTxt() {
			return actionStatusTxt;
		}

		public void setActionStatusTxt(String actionStatusTxt) {
			this.actionStatusTxt = actionStatusTxt;
		}

		public int getActionNameCde() {
			return actionNameCde;
		}

		public void setActionNameCde(int actionNameCde) {
			this.actionNameCde = actionNameCde;
		}

		public String getActionNameTxt() {
			return actionNameTxt;
		}

		public void setActionNameTxt(String actionNameTxt) {
			this.actionNameTxt = actionNameTxt;
		}

		public String getActionResult() {
			return actionResult;
		}

		public void setActionResult(String actionResult) {
			this.actionResult = actionResult;
		}

		public List<ParamData> getParamList() {
			return paramList;
		}

		public void setParamList(List<ParamData> paramList) {
			this.paramList = paramList;
		}

	}

	public static class ParamData {

		private int paramNameCde;
		private String paramNameTxt;
		private String paramValue;

		public int getParamNameCde() {
			return paramNameCde;
		}

		public void setParamNameCde(int paramNameCde) {
			this.paramNameCde = paramNameCde;
		}

		public String getParamNameTxt() {
			return paramNameTxt;
		}

		public void setParamNameTxt(String paramNameTxt) {
			this.paramNameTxt = paramNameTxt;
		}

		public String getParamValue() {
			return paramValue;
		}

		public void setParamValue(String paramValue) {
			this.paramValue = paramValue;
		}

	}

	public static class AttachmentData {
		private long attachmentId;
		private List<AttributeData> attributes;

		public List<AttributeData> getAttributes() {
			return attributes;
		}

		public void setAttributes(List<AttributeData> attributes) {
			this.attributes = attributes;
		}

		public long getAttachmentId() {
			return attachmentId;
		}

		public void setAttachmentId(long attachmentId) {
			this.attachmentId = attachmentId;
		}
	}

	public static class AttributeData {

		private long id;
		private int attrNameCde;
		private String attrNameTxt;
		private String attrValue;
		private int extractTypeCde;
		private String extractTypeTxt;
		private float confidencePct;
		private List<AttributeData> attributes;

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

		public String getExtractTypeTxt() {
			return extractTypeTxt;
		}

		public void setExtractTypeTxt(String extractTypeTxt) {
			this.extractTypeTxt = extractTypeTxt;
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

		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}

	}

	private long docId;
	private int docTypeCde;
	private List<ActionData> actionDataList;
	private List<AttributeData> attributes;
	private List<AttachmentData> attachments;

	public long getDocId() {
		return docId;
	}

	public void setDocId(long docId) {
		this.docId = docId;
	}

	public List<ActionData> getActionDataList() {
		return actionDataList;
	}

	public void setActionDataList(List<ActionData> actionDataList) {
		this.actionDataList = actionDataList;
	}

	public List<AttributeData> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<AttributeData> attributes) {
		this.attributes = attributes;
	}

	public int getDocTypeCde() {
		return docTypeCde;
	}

	public void setDocTypeCde(int docTypeCde) {
		this.docTypeCde = docTypeCde;
	}

	public List<AttachmentData> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<AttachmentData> attachments) {
		this.attachments = attachments;
	}

}
