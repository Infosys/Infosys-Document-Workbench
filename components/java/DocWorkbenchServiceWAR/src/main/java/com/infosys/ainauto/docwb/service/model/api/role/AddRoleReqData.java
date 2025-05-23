/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.api.role;

public class AddRoleReqData {
	private int appUserId;
	private int userRoleType;

	public int getAppUserId() {
		return appUserId;
	}

	public void setAppUserId(int appUserId) {
		this.appUserId = appUserId;
	}

	public int getUserRoleType() {
		return userRoleType;
	}

	public void setUserRoleType(int userRoleType) {
		this.userRoleType = userRoleType;
	}

}
