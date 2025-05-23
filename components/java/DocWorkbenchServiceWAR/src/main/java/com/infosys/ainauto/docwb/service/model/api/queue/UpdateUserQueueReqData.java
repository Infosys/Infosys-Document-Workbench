/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.api.queue;

public class UpdateUserQueueReqData {
	private int queueNameCde;
	private String userQueueHideAfterDtm;
	public int getQueueNameCde() {
		return queueNameCde;
	}
	public void setQueueNameCde(int queueNameCde) {
		this.queueNameCde = queueNameCde;
	}
	public String getUserQueueHideAfterDtm() {
		return userQueueHideAfterDtm;
	}
	public void setUserQueueHideAfterDtm(String userQueueHideAfterDtm) {
		this.userQueueHideAfterDtm = userQueueHideAfterDtm;
	}
	
}
