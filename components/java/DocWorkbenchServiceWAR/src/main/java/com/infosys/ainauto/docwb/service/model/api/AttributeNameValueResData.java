/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.api;

import java.util.List;

public class AttributeNameValueResData {
	
	private int attrNameCde;
    private String attrNameTxt;
    private List<AllowedValueResData> allowedValues;
    
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
	public List<AllowedValueResData> getAllowedValues() {
		return allowedValues;
	}
	public void setAllowedValues(List<AllowedValueResData> allowedValues) {
		this.allowedValues = allowedValues;
	}
}
