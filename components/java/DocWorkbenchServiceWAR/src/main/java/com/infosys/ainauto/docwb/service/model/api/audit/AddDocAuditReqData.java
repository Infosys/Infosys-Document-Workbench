/** =============================================================================================================== *
 * Copyright 2021 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.api.audit;

import java.util.List;

public class AddDocAuditReqData {

	public static class AuditData {
		private String entityName;
		private String entityValue;
		private String auditMessage;
		private String currentValue;
		private String previousValue;

		public String getEntityName() {
			return entityName;
		}

		public void setEntityName(String entityName) {
			this.entityName = entityName;
		}

		public String getEntityValue() {
			return entityValue;
		}

		public void setEntityValue(String entityValue) {
			this.entityValue = entityValue;
		}

		public String getAuditMessage() {
			return auditMessage;
		}

		public void setAuditMessage(String auditMessage) {
			this.auditMessage = auditMessage;
		}

		public String getCurrentValue() {
			return currentValue;
		}

		public void setCurrentValue(String currentValue) {
			this.currentValue = currentValue;
		}

		public String getPreviousValue() {
			return previousValue;
		}

		public void setPreviousValue(String previousValue) {
			this.previousValue = previousValue;
		}
	}

	private long docId;
	private List<AuditData> auditDataList;

	public long getDocId() {
		return docId;
	}

	public void setDocId(long docId) {
		this.docId = docId;
	}

	public List<AuditData> getAuditDataList() {
		return auditDataList;
	}

	public void setAuditDataList(List<AuditData> auditDataList) {
		this.auditDataList = auditDataList;
	}

}