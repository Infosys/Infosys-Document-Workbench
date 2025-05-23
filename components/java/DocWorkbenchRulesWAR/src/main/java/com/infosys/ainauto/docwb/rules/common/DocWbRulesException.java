/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.rules.common;

import com.infosys.ainauto.docwb.rules.type.EnumApiResponseCde;

public class DocWbRulesException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private EnumApiResponseCde enumApiResponseCde;
	
	// Only getter method required. For setting, use constructor
	public EnumApiResponseCde getEnumApiResponseCde() {
		return enumApiResponseCde;
	}

	public DocWbRulesException() {
	}

	public DocWbRulesException(String message) {
		super(message);
	}

	public DocWbRulesException(Throwable t) {
		super(t);
	}

	public DocWbRulesException(String message, Throwable t) {
		super(message, t);
	}
	
	public DocWbRulesException(EnumApiResponseCde enumApiResponseCde, String message) {
		super(message);
		this.enumApiResponseCde = enumApiResponseCde;
	}

	public DocWbRulesException(EnumApiResponseCde enumApiResponseCde, String message, Throwable t) {
		super(message, t);
		this.enumApiResponseCde = enumApiResponseCde;
	}
}
