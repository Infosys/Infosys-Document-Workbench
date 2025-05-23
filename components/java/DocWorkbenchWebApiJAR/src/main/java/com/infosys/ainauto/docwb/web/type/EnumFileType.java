/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


package com.infosys.ainauto.docwb.web.type;

public enum EnumFileType {

	ALL("all"), SUPPORTED("supported"), UNSUPPORTED("unsupported");
	private String propertyValue;

	private EnumFileType(String s) {
		propertyValue = s;
	}

	public String getValue() {
		return propertyValue;
	}
}
