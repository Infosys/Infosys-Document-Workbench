/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.common.type;

import java.util.HashMap;
import java.util.Map;

public enum EnumTaskStatus {
	UNDEFINED(50),
	YET_TO_START(100),
	IN_PROGRESS(200),
	ON_HOLD(300),
	FOR_YOUR_REVIEW(400),
	RETRY_LATER(500),
	COMPLETE(900),
	FAILED(901)
    ;
	
    private int propertyValue;

    private EnumTaskStatus(int s) {
        propertyValue = s;
    }

    public int getValue() {
        return propertyValue;
    }
    
    // Reverse Lookup
 	public static EnumTaskStatus get(int cde) {
 		return lookup.get(cde);
 	}

 	private static final Map<Integer, EnumTaskStatus> lookup = new HashMap<>();

 	static {
 		for (EnumTaskStatus enumType : EnumTaskStatus.values()) {
 			lookup.put(enumType.getValue(), enumType);
 		}
 	}
}
