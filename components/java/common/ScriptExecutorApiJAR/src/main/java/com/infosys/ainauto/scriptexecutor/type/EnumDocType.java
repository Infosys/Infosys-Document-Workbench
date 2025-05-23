/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.scriptexecutor.type;

public enum EnumDocType {
	EMAIL(1),
	PDF(2),
	WORD(3),
	XML(4)
    ;
	
    private int propertyValue;

    private EnumDocType(int s) {
        propertyValue = s;
    }

    public int getValue() {
        return propertyValue;
    }
}
