/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.rules.model.domain;

import java.util.List;

public class AttributeData {

	private long id;
	private int attrNameCde;
	private String attrNameTxt;
	private String attrValue;
	private int extractTypeCde;
	private String extractTypeTxt;
	private float confidencePct;
	private String notification;
	private List<AttributeData> attributes;

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

	public String getAttrValue() {
		return attrValue;
	}

	public void setAttrValue(String attrValue) {
		this.attrValue = attrValue;
	}

	public int getExtractTypeCde() {
		return extractTypeCde;
	}

	public void setExtractTypeCde(int extractTypeCde) {
		this.extractTypeCde = extractTypeCde;
	}

	public String getExtractTypeTxt() {
		return extractTypeTxt;
	}

	public void setExtractTypeTxt(String extractTypeTxt) {
		this.extractTypeTxt = extractTypeTxt;
	}

	public float getConfidencePct() {
		return confidencePct;
	}

	public void setConfidencePct(float confidencePct) {
		this.confidencePct = confidencePct;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public List<AttributeData> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<AttributeData> attributes) {
		this.attributes = attributes;
	}

	public String getNotification() {
		return notification;
	}

	public void setNotification(String notification) {
		this.notification = notification;
	}

}
