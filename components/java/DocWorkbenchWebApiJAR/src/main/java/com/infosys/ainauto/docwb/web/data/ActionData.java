/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.web.data;

import java.util.List;

public class ActionData {
	private int actionNameCde;
	private String actionNameTxt;
	private int taskStatusCde;
	private String createBy;
	private List<ParamAttrData> mappingList;

	public int getActionNameCde() {
		return actionNameCde;
	}

	public void setActionNameCde(int actionNameCde) {
		this.actionNameCde = actionNameCde;
	}

	public String getActionNameTxt() {
		return actionNameTxt;
	}

	public void setActionNameTxt(String actionNameTxt) {
		this.actionNameTxt = actionNameTxt;
	}

	public int getTaskStatusCde() {
		return taskStatusCde;
	}

	public void setTaskStatusCde(int taskStatusCde) {
		this.taskStatusCde = taskStatusCde;
	}
	
	public List<ParamAttrData> getMappingList() {
		return mappingList;
	}

	public void setMappingList(List<ParamAttrData> mappingList) {
		this.mappingList = mappingList;
	}

	public String getCreateBy() {
		return createBy;
	}

	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}
}
