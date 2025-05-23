/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.api;

import java.util.List;

public class ParamAttrResData {

	private int paramNameCde;
	private String paramNameTxt;
	private int attrNameCde;
	private String attrNameTxt;
	private List<String> attrValues;
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

	public List<String> getAttrValues() {
		return attrValues;
	}

	public void setAttrValues(List<String> attrValues) {
		this.attrValues = attrValues;
	}

	public String getParamValue() {
		return paramValue;
	}

	public void setParamValue(String paramValue) {
		this.paramValue = paramValue;
	}
}
