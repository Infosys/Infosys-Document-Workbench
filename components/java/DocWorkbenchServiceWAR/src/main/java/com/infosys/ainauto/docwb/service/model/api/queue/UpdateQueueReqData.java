/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.api.queue;

public class UpdateQueueReqData {
	private int queueNameCde;
	private String endDtm;
	private String queueHideAfterDtm;
	public int getQueueNameCde() {
		return queueNameCde;
	}
	public void setQueueNameCde(int queueNameCde) {
		this.queueNameCde = queueNameCde;
	}
	
	public String getEndDtm() {
		return endDtm;
	}
	public void setEndDtm(String endDtm) {
		this.endDtm = endDtm;
	}
	public String getQueueHideAfterDtm() {
		return queueHideAfterDtm;
	}
	public void setQueueHideAfterDtm(String queueHideAfterDtm) {
		this.queueHideAfterDtm = queueHideAfterDtm;
	}
	
}
