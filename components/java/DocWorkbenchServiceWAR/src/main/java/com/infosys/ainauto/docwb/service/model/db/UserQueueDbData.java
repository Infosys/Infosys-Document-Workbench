/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.db;

public class UserQueueDbData {
	private long appUserId;
	private int queueNameCde;
	private String queueNameTxt;
	private long appUserQueueRelId;
	private long docTypeCde;
	private String docTypeTxt;
	private String queueClosedDtm;
	private String queueStatus;
	private String queueHideAfterDtm;
	private String userQueueHideAfterDtm;

	public long getDocTypeCde() {
		return docTypeCde;
	}

	public void setDocTypeCde(long docTypeCde) {
		this.docTypeCde = docTypeCde;
	}

	public String getDocTypeTxt() {
		return docTypeTxt;
	}

	public void setDocTypeTxt(String docTypeTxt) {
		this.docTypeTxt = docTypeTxt;
	}

	public long getAppUserId() {
		return appUserId;
	}

	public void setAppUserId(long appUserId) {
		this.appUserId = appUserId;
	}

	public int getQueueNameCde() {
		return queueNameCde;
	}

	public void setQueueNameCde(int queueNameCde) {
		this.queueNameCde = queueNameCde;
	}

	public String getQueueNameTxt() {
		return queueNameTxt;
	}

	public void setQueueNameTxt(String queueNameTxt) {
		this.queueNameTxt = queueNameTxt;
	}
	
	public long getAppUserQueueRelId() {
		return appUserQueueRelId;
	}

	public void setAppUserQueueRelId(long appUserQueueRelId) {
		this.appUserQueueRelId = appUserQueueRelId;
	}

	public String getUserQueueHideAfterDtm() {
		return userQueueHideAfterDtm;
	}

	public void setUserQueueHideAfterDtm(String userQueueHideAfterDtm) {
		this.userQueueHideAfterDtm = userQueueHideAfterDtm;
	}

	public String getQueueClosedDtm() {
		return queueClosedDtm;
	}

	public void setQueueClosedDtm(String queueClosedDtm) {
		this.queueClosedDtm = queueClosedDtm;
	}

	public String getQueueStatus() {
		return queueStatus;
	}

	public void setQueueStatus(String queueStatus) {
		this.queueStatus = queueStatus;
	}

	public String getQueueHideAfterDtm() {
		return queueHideAfterDtm;
	}

	public void setQueueHideAfterDtm(String queueHideAfterDtm) {
		this.queueHideAfterDtm = queueHideAfterDtm;
	}

}
