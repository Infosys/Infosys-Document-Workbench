/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.rules.model.domain;

import java.util.List;

public class ActionData {

	private int actionNameCde;
	private String actionNameTxt;
	private String actionResult;
	private int actionStatusCde;
	private String actionStatusTxt;
	private List<ParamData> paramList;

	public int getActionStatusCde() {
		return actionStatusCde;
	}

	public void setActionStatusCde(int actionStatusCde) {
		this.actionStatusCde = actionStatusCde;
	}

	public String getActionStatusTxt() {
		return actionStatusTxt;
	}

	public void setActionStatusTxt(String actionStatusTxt) {
		this.actionStatusTxt = actionStatusTxt;
	}

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

	public String getActionResult() {
		return actionResult;
	}

	public void setActionResult(String actionResult) {
		this.actionResult = actionResult;
	}

	public List<ParamData> getParamList() {
		return paramList;
	}

	public void setParamList(List<ParamData> paramList) {
		this.paramList = paramList;
	}

}
