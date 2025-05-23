/** =============================================================================================================== *
 * Copyright 2022 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.api.queue;

public class AddQueueReqData {

	private long queueNameCde;
	private String queueNameTxt;
	private int docTypeCde;

	public long getQueueNameCde() {
		return queueNameCde;
	}

	public void setQueueNameCde(long queueNameCde) {
		this.queueNameCde = queueNameCde;
	}

	public String getQueueNameTxt() {
		return queueNameTxt;
	}

	public void setQueueNameTxt(String queueNameTxt) {
		this.queueNameTxt = queueNameTxt;
	}

	public int getDocTypeCde() {
		return docTypeCde;
	}

	public void setDocTypeCde(int docTypeCde) {
		this.docTypeCde = docTypeCde;
	}

}
