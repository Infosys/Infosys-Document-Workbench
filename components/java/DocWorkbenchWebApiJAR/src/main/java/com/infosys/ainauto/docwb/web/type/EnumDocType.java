/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.web.type;

import java.util.HashMap;
import java.util.Map;

public enum EnumDocType {
	EMAIL(1),
	FILE(2)
	;
	
    private int propertyValue;

    private EnumDocType(int s) {
        propertyValue = s;
    }

    public int getValue() {
        return propertyValue;
    }
    
 // Reverse Lookup
 	public static EnumDocType get(int cde) {
 		return lookup.get(cde);
 	}

 	private static final Map<Integer, EnumDocType> lookup = new HashMap<>();

 	static {
 		for (EnumDocType enumType : EnumDocType.values()) {
 			lookup.put(enumType.getValue(), enumType);
 		}
 	}
}
