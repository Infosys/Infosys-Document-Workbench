/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.api.document;

import com.infosys.ainauto.docwb.service.common.WorkbenchConstants;

public class InsertDocData {

	private int attrNameCde;
	private String attrValue;
	// to allow zero as an explicitly set value by caller
	private float confidencePct = WorkbenchConstants.CONFIDENCE_PCT_UNSET; 
	private int extractTypeCde;

	public int getAttrNameCde() {
		return attrNameCde;
	}

	public void setAttrNameCde(int attrNameCde) {
		this.attrNameCde = attrNameCde;
	}

	public String getAttrValue() {
		return attrValue;
	}

	public void setAttrValue(String attrValue) {
		this.attrValue = attrValue;
	}

	public float getConfidencePct() {
		return confidencePct;
	}

	public void setConfidencePct(float confidencePct) {
		this.confidencePct = confidencePct;
	}

	public int getExtractTypeCde() {
		return extractTypeCde;
	}

	public void setExtractTypeCde(int extractTypeCde) {
		this.extractTypeCde = extractTypeCde;
	}

}
