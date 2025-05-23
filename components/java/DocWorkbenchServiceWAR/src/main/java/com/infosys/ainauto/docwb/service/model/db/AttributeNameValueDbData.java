/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.db;

public class AttributeNameValueDbData {

	private int attrNameCde;
	private String attrNameTxt;
	private String txt;
	private int sequenceNum;

	public String getTxt() {
		return txt;
	}
	public void setTxt(String txt) {
		this.txt = txt;
	}
	public int getSequenceNum() {
		return sequenceNum;
	}
	public void setSequenceNum(int sequenceNum) {
		this.sequenceNum = sequenceNum;
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
}