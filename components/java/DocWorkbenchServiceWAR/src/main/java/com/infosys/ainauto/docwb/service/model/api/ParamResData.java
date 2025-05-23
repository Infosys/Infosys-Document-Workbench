/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.api;

public class ParamResData {

	private int paramNameCde;
	private String paramNameTxt;
	private String paramValue;

	public int getParamNameCde() {
		return paramNameCde;
	}

	public void setParamNameCde(int paramNameCde) {
		this.paramNameCde = paramNameCde;
	}

	public String getParamNameTxt() {
		return paramNameTxt;
	}

	public void setParamNameTxt(String paramNameTxt) {
		this.paramNameTxt = paramNameTxt;
	}

	public String getParamValue() {
		return paramValue;
	}

	public void setParamValue(String paramValue) {
		this.paramValue = paramValue;
	}

}
