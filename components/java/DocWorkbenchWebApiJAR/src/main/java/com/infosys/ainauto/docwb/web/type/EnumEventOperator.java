/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.web.type;

public enum EnumEventOperator {
	EQUALS("="),
	NOT_EQUALS("<>"),
	GREATER_THAN(">"),
	LESS_THAN("<"),
	GREATER_THAN_OR_EQUALS(">="),
	LESS_THAN_OR_EQUALS("<=")
	
    ;
	
    private String propertyValue;

    private EnumEventOperator(String s) {
        propertyValue = s;
    }

    public String getValue() {
        return propertyValue;
    }
}
