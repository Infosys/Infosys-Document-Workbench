/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.core.common.type;

public enum EnumExecutorType {
	DOCUMENT_DOWNLOADER(1,"Document Downloader"),
	ATTRIBUTE_EXTRACTOR(2,"Attribute Extractor"),
	CASE_OPENER(3,"Case Opener"),
	ACTION_EXECUTOR(4,"Action Executor"),
	OUTBOUND_EMAIL_SENDER(5,"Outbound Email Sender"),
	ACTION_SCRIPT_EXECUTOR(6,"Action Script Executor"),
	ACTION_SCRIPT_RESULT_UPDATER(7,"Action Script Result Updater"),
	EXTRACT_ATTRIBUTE_ACTION_EXECUTOR(8,"Re Extract Data Action Executor")
	
    ;
	
    private int cde;
    private String text;

    private EnumExecutorType(int cde, String text) {
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
