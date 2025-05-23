/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.api.document;

public class CloseCaseReqData {

	private long appUserId;
	private String appUserLoginId;
	private long docId;	
	
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
	public String getAppUserLoginId() {
		return appUserLoginId;
	}
	public void setAppUserLoginId(String appUserLoginId) {
		this.appUserLoginId = appUserLoginId;
	}
}
