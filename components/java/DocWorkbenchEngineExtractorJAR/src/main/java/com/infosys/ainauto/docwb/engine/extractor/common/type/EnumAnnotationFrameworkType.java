/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


package com.infosys.ainauto.docwb.engine.extractor.common.type;

public enum EnumAnnotationFrameworkType {

	ANNOTATOR_JS("annotatorjs");
	private String propertyValue;

	private EnumAnnotationFrameworkType(String s) {
		propertyValue = s;
	}

	public String getValue() {
		return propertyValue;
	}
}
