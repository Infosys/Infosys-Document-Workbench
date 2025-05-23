/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.common.type;

public enum EnumOperationType {
	INSERT("insert"),
	UPDATE("update"),
	DELETE("delete")
	
    ;
	
    private String propertyValue;

    private EnumOperationType(String s) {
        propertyValue = s;
    }

    public String getValue() {
        return propertyValue;
    }
}
