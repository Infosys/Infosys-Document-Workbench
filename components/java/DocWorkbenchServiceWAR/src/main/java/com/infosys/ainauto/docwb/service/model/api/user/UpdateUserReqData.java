/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.api.user;

public class UpdateUserReqData {
	private boolean accountEnabled;
	private long userId;
	
	public boolean isAccountEnabled() {
		return accountEnabled;
	}
	public void setAccountEnabled(boolean accountEnabled) {
		this.accountEnabled = accountEnabled;
	}
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
}
