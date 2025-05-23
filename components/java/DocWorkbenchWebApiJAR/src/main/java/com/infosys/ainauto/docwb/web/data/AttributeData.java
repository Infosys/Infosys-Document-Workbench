/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.web.data;

import java.util.List;

import com.infosys.ainauto.docwb.web.common.DocwbWebConstants;
import com.infosys.ainauto.docwb.web.type.EnumExtractType;

public class AttributeData {

	private int attrNameCde;
	private long id;
	private String attrNameTxt;
	private String attrValue;
	private EnumExtractType enumExtractType;
	private float confidencePct = DocwbWebConstants.CONFIDENCE_PCT_UNSET;
	private List<AttributeData> attributeDataList;
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getAttrNameCde() {
		return attrNameCde;
	}

	public AttributeData setAttrNameCde(int attrNameCde) {
		this.attrNameCde = attrNameCde;
		return this;
	}

	public String getAttrValue() {
		return attrValue;
	}

	public AttributeData setAttrValue(String attrValue) {
		this.attrValue = attrValue;
		return this;
	}

	public float getConfidencePct() {
		return confidencePct;
	}

	public AttributeData setConfidencePct(float confidencePct) {
		this.confidencePct = confidencePct;
		return this;
	}

	public String getAttrNameTxt() {
		return attrNameTxt;
	}

	public AttributeData setAttrNameTxt(String attrNameTxt) {
		this.attrNameTxt = attrNameTxt;
		return this;
	}

	public EnumExtractType getExtractType() {
		return enumExtractType;
	}

	public AttributeData setExtractType(EnumExtractType enumExtractType) {
		this.enumExtractType = enumExtractType;
		return this;
	}

	public List<AttributeData> getAttributeDataList() {
		return attributeDataList;
	}

	public void setAttributeDataList(List<AttributeData> attributeDataList) {
		this.attributeDataList = attributeDataList;
	}

	@Override
	public String toString() {
		return "AttributeData [attrNameCde=" + attrNameCde + ", id=" + id + ", attrNameTxt=" + attrNameTxt
				+ ", attrValue=" + attrValue + ", attributeDataList=" + attributeDataList + "]";
	}

}
