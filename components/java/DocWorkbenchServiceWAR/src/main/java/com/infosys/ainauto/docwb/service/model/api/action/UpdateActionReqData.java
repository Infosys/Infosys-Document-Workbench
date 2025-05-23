/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.api.action;

import java.util.List;

public class UpdateActionReqData {

	private List<UpdateActionData> actionDataList;

	public List<UpdateActionData> getActionDataList() {
		return actionDataList;
	}

	public void setActionDataList(List<UpdateActionData> actionDataList) {
		this.actionDataList = actionDataList;
	}
}
