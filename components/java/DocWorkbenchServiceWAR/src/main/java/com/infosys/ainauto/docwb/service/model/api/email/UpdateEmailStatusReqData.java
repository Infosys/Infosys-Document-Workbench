/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.api.email;

public class UpdateEmailStatusReqData {

	private long emailOutboundId;
	private long taskStatusCde;

	public long getEmailOutboundId() {
		return emailOutboundId;
	}

	public void setEmailOutboundId(long emailOutboundId) {
		this.emailOutboundId = emailOutboundId;
	}

	public long getTaskStatusCde() {
		return taskStatusCde;
	}

	public void setTaskStatusCde(long taskStatusCde) {
		this.taskStatusCde = taskStatusCde;
	}

}
