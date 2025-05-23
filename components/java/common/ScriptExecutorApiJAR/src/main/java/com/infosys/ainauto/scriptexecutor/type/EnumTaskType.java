/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.scriptexecutor.type;

import java.util.HashMap;
import java.util.Map;

public enum EnumTaskType {
	SYSTEM(1), USER(2);

	private int propertyValue;

	private EnumTaskType(int s) {
		propertyValue = s;
	}

	public int getValue() {
		return propertyValue;
	}

	// Reverse Lookup
	public static EnumTaskType get(int cde) {
		return lookup.get(cde);
	}

	private static final Map<Integer, EnumTaskType> lookup = new HashMap<>();

	static {
		for (EnumTaskType enumType : EnumTaskType.values()) {
			lookup.put(enumType.getValue(), enumType);
		}
	}
}
