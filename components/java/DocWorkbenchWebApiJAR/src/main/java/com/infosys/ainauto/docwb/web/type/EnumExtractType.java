/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.web.type;

import java.util.HashMap;
import java.util.Map;

public enum EnumExtractType {
	DIRECT_COPY(1),
	CUSTOM_LOGIC(2),
	MANUALLY_CORRECTED(3)
    ;
	
    private int propertyValue;

    private EnumExtractType(int s) {
        propertyValue = s;
    }

    public int getValue() {
        return propertyValue;
    }
    
	// Reverse Lookup
	public static EnumExtractType get(int cde) {
		return lookup.get(cde);
	}

	private static final Map<Integer, EnumExtractType> lookup = new HashMap<>();

	static {
		for (EnumExtractType enumType : EnumExtractType.values()) {
			lookup.put(enumType.getValue(), enumType);
		}
	}
    
}
