/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.core.common.type;

public enum EnumStatusType {
	QUEUED(1,"QUEUED"),
	SUCCESS(2,"SUCCESS"),
	FAILED(3,"FAILED")
	
    ;
	
	private int cde;
    private String text;

    private EnumStatusType(int cde, String text) {
        this.cde = cde;
        this.text = text;
    }

    public int getCdeValue() {
        return cde;
    }
    
    public String getTextValue() {
		return this.text;
	}
}
