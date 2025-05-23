/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.core.common.type;

public enum EnumExecutionEventType {
	WORK_STARTED(100,"Work Started"),
	ONLINE_TASKS_TRIGGERED(400,"Online Jobs Triggered"),
	ONLINE_TASKS_COMPLETED(500,"Online Jobs Completed"),
	BATCH_TASKS_TRIGGERED(600,"Batch Jobs Triggered"),
	BATCH_TASKS_COMPLETED(700,"Batch Jobs Completed"),
	WORK_COMPLETED(800,"Work Completed")
	
    ;
	
	private int cde;
    private String text;

    private EnumExecutionEventType(int cde, String text) {
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
