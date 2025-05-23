/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.api.action;

import java.util.List;

public class InsertActionData {

	private int actionNameCde;
	private List<ParamAttrReqData> mappingList;
	private int taskTypeCde;

	public int getActionNameCde() {
		return actionNameCde;
	}

	public void setActionNameCde(int actionNameCde) {
		this.actionNameCde = actionNameCde;
	}

	public List<ParamAttrReqData> getMappingList() {
		return mappingList;
	}

	public void setMappingList(List<ParamAttrReqData> mappingList) {
		this.mappingList = mappingList;
	}

	public int getTaskTypeCde() {
		return taskTypeCde;
	}

	public void setTaskTypeCde(int taskTypeCde) {
		this.taskTypeCde = taskTypeCde;
	}
}
