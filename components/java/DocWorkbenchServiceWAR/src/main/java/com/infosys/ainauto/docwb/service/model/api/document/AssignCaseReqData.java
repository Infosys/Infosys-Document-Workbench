/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.api.document;

public class AssignCaseReqData {

	private long appUserId;
	private long docId;
	private long prevAppUserId;
	private String appUserLoginId;
	private String prevAppUserLoginId;
	private long docRoleTypeCde = 1;

	public long getAppUserId() {
		return appUserId;
	}

	public void setAppUserId(long appUserId) {
		this.appUserId = appUserId;
	}

	public long getDocId() {
		return docId;
	}

	public void setDocId(long docId) {
		this.docId = docId;
	}

	public long getPrevAppUserId() {
		return prevAppUserId;
	}

	public void setPrevAppUserId(long prevAppUserId) {
		this.prevAppUserId = prevAppUserId;
	}

	public String getAppUserLoginId() {
		return appUserLoginId;
	}

	public void setAppUserLoginId(String appUserLoginId) {
		this.appUserLoginId = appUserLoginId;
	}

	public String getPrevAppUserLoginId() {
		return prevAppUserLoginId;
	}

	public void setPrevAppUserLoginId(String prevAppUserLoginId) {
		this.prevAppUserLoginId = prevAppUserLoginId;
	}

	public long getDocRoleTypeCde() {
		return (docRoleTypeCde<1)?1:docRoleTypeCde;
	}

	public void setDocRoleTypeCde(long docRoleTypeCde) {
		this.docRoleTypeCde = docRoleTypeCde;
	}
}
