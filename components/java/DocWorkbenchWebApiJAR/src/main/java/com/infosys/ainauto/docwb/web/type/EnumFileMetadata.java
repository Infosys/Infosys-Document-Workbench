/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


package com.infosys.ainauto.docwb.web.type;

public enum EnumFileMetadata {

	PDF_SCANNED("PDF Scanned"), PDF_NATIVE("PDF Native"), MS_WORD("MS Word"), PLAIN_TEXT("Plain Text"), EMAIL("Email");

	private String propertyValue;

	private EnumFileMetadata(String s) {
		propertyValue = s;
	}

	public String getValue() {
		return propertyValue;
	}
}
