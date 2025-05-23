/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.api.action;

public class ParamAttrReqData {

	private int paramNameCde;
	private int attrNameCde;
	private String attrNameTxt;
	private String paramValue;

	public int getParamNameCde() {
		return paramNameCde;
	}

	public void setParamNameCde(int paramNameCde) {
		this.paramNameCde = paramNameCde;
	}

	public int getAttrNameCde() {
		return attrNameCde;
	}

	public void setAttrNameCde(int attrNameCde) {
		this.attrNameCde = attrNameCde;
	}

	public String getAttrNameTxt() {
		return attrNameTxt;
	}

	public void setAttrNameTxt(String attrNameTxt) {
		this.attrNameTxt = attrNameTxt;
	}

	public String getParamValue() {
		return paramValue;
	}

	public void setParamValue(String paramValue) {
		this.paramValue = paramValue;
	}

}
