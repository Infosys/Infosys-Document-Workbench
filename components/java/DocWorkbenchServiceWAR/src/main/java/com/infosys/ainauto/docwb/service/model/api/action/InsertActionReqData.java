/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.api.action;

import java.util.List;

public class InsertActionReqData {

	private long docId;
	private List<InsertActionData> actionDataList;

	public List<InsertActionData> getActionDataList() {
		return actionDataList;
	}

	public void setActionDataList(List<InsertActionData> actionDataList) {
		this.actionDataList = actionDataList;
	}

	public long getDocId() {
		return docId;
	}

	public void setDocId(long docId) {
		this.docId = docId;
	}
}