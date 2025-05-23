/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.db;

import java.sql.Timestamp;

public class AuditDbData {

	private long auditId;
	private long docId;
	private long queueNameCde;
	private long appUserId;
	private long rbacId;
	private String entityName;
	private String entityValue;
	private String auditLoginId;
	private String auditMessage;
	private String currentValue;
	private String previousValue;
	private Timestamp auditEventDtm;
	private String createDtm;
	private int userType;

	public String getPreviousValue() {
		return previousValue;
	}

	public void setPreviousValue(String previousValue) {
		this.previousValue = previousValue;
	}

	public String getCurrentValue() {
		return currentValue;
	}

	public void setCurrentValue(String currentValue) {
		this.currentValue = currentValue;
	}

	public long getDocId() {
		return docId;
	}

	public void setDocId(long docId) {
		this.docId = docId;
	}

	public long getAppUserId() {
		return appUserId;
	}

	public void setAppUserId(long appUserId) {
		this.appUserId = appUserId;
	}

	public long getAuditId() {
		return auditId;
	}

	public void setAuditId(long auditId) {
		this.auditId = auditId;
	}

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

	public String getAuditLoginId() {
		return auditLoginId;
	}

	public void setAuditLoginId(String auditLoginId) {
		this.auditLoginId = auditLoginId;
	}

	public String getAuditMessage() {
		return auditMessage;
	}

	public void setAuditMessage(String auditMessage) {
		this.auditMessage = auditMessage;
	}

	public Timestamp getAuditEventDtm() {
		return auditEventDtm;
	}

	public void setAuditEventDtm(Timestamp auditEventDtm) {
		this.auditEventDtm = auditEventDtm;
	}

	public String getCreateDtm() {
		return createDtm;
	}

	public void setCreateDtm(String createDtm) {
		this.createDtm = createDtm;
	}

	public int getUserType() {
		return userType;
	}

	public void setUserType(int userType) {
		this.userType = userType;
	}

	public long getRbacId() {
		return rbacId;
	}

	public void setRbacId(long rbacId) {
		this.rbacId = rbacId;
	}

	public long getQueueNameCde() {
		return queueNameCde;
	}

	public void setQueueNameCde(long queueNameCde) {
		this.queueNameCde = queueNameCde;
	}
}
