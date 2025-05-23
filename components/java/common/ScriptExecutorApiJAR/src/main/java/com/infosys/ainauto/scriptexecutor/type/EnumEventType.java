/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.scriptexecutor.type;

public enum EnumEventType {
	DOCUMENT_CREATED(100),
	ATTRIBUTES_EXTRACTED_PENDING(150),
	ATTRIBUTES_EXTRACTED(200),
	CASE_OPENED(300),
	CASE_ASSIGNED(400),
	ACTION_CREATED(500),
	ACTION_COMPLETED(600),
	EMAIL_SENT(700),
	CASE_CLOSED(800)
	
    ;
	
    private int propertyValue;

    private EnumEventType(int s) {
        propertyValue = s;
    }

    public int getValue() {
        return propertyValue;
    }
}
