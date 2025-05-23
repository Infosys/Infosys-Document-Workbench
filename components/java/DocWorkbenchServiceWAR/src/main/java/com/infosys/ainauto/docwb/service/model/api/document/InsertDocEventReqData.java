/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.api.document;

public class InsertDocEventReqData {
    
    private long docId;
    private int eventTypeCde;
    
 	public long getDocId() {
		return docId;
	}
	public void setDocId(long docId) {
		this.docId = docId;
	}
	public int getEventTypeCde() {
		return eventTypeCde;
	}
	public void setEventTypeCde(int eventTypeCde) {
		this.eventTypeCde = eventTypeCde;
	}

}
