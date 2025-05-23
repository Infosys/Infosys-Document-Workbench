/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.api;

import java.util.List;

import com.infosys.ainauto.docwb.service.model.db.AuditDbData;

public class AuditResData {

	private List<AuditDbData> auditDataList;
	private int pageNumber;
	private long totalCount;
	private PaginationResData paginationData;
	private long docId;
	private long appUserId;

	public List<AuditDbData> getAuditDataList() {
		return auditDataList;
	}

	public void setAuditDataList(List<AuditDbData> auditDataList) {
		this.auditDataList = auditDataList;
	}

	public int getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}

	public PaginationResData getPaginationData() {
		return paginationData;
	}

	public void setPaginationData(PaginationResData paginationData) {
		this.paginationData = paginationData;
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

	public long getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(long totalCount) {
		this.totalCount = totalCount;
	}

}