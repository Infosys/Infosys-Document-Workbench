/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.web.type;

public enum EnumLockStatus {
	UNLOCKED(1),
	LOCKED(2)
    ;
	
    private int propertyValue;

    private EnumLockStatus(int s) {
        propertyValue = s;
    }

    public int getValue() {
        return propertyValue;
    }
}
