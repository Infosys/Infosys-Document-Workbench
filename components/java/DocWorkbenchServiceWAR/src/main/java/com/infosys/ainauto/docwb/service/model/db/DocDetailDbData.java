/** =============================================================================================================== *
 * Copyright 2023 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.db;

import java.util.List;

import com.infosys.ainauto.docwb.service.common.WorkbenchConstants;

public class DocDetailDbData {
	
	public class AttributeData{
		private long attributeId;
		private int attrNameCde;
		private String attrNameTxt;
		private String attrValue;
		private int extractTypeCde;
		private float confidencePct = WorkbenchConstants.CONFIDENCE_PCT_UNSET;
		public long getAttributeId() {
			return attributeId;
		}
		public void setAttributeId(long attributeId) {
			this.attributeId = attributeId;
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
	}

	private long docId;
	private int docTypeCde;
	private String docLocation;
	private int taskStatusCde;
	private String taskStatusTxt;
	private int queueNameCde;
	private String createDtm;
	private List<AttributeData> attributeData;
	public long getDocId() {
		return docId;
	}
	public void setDocId(long docId) {
		this.docId = docId;
	}
	public int getDocTypeCde() {
		return docTypeCde;
	}
	public void setDocTypeCde(int docTypeCde) {
		this.docTypeCde = docTypeCde;
	}
	public String getDocLocation() {
		return docLocation;
	}
	public void setDocLocation(String docLocation) {
		this.docLocation = docLocation;
	}
	public int getTaskStatusCde() {
		return taskStatusCde;
	}
	public void setTaskStatusCde(int taskStatusCde) {
		this.taskStatusCde = taskStatusCde;
	}
	public String getTaskStatusTxt() {
		return taskStatusTxt;
	}
	public void setTaskStatusTxt(String taskStatusTxt) {
		this.taskStatusTxt = taskStatusTxt;
	}
	public int getQueueNameCde() {
		return queueNameCde;
	}
	public void setQueueNameCde(int queueNameCde) {
		this.queueNameCde = queueNameCde;
	}
	public String getCreateDtm() {
		return createDtm;
	}
	public void setCreateDtm(String createDtm) {
		this.createDtm = createDtm;
	}
	public List<AttributeData> getAttributeData() {
		return attributeData;
	}
	public void setAttributeData(List<AttributeData> attributeData) {
		this.attributeData = attributeData;
	}
	
}
