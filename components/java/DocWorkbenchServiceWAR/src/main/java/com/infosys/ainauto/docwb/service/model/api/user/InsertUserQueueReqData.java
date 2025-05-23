/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.api.user;

public class InsertUserQueueReqData {
	private long appUserId;
	private String appUserLoginId;
	private int queueNameCde;

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

	public String getAppUserLoginId() {
		return appUserLoginId;
	}

	public void setAppUserLoginId(String appUserLoginId) {
		this.appUserLoginId = appUserLoginId;
	}
}
