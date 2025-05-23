/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.db;

public class DocAppUserDbData {

	private long docId;
	private long appUserId;
	private long docAppUserRelId;
	private long docRoleTypeCde;
	
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
	
	public long getDocAppUserRelId() {
		return docAppUserRelId;
	}

	public void setDocAppUserRelId(long docAppUserRelId) {
		this.docAppUserRelId = docAppUserRelId;
	}

	public long getDocRoleTypeCde() {
		return docRoleTypeCde;
	}

	public void setDocRoleTypeCde(long docRoleTypeCde) {
		this.docRoleTypeCde = docRoleTypeCde;
	}
}
