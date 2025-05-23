/** =============================================================================================================== *
 * Copyright 2022 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


package com.infosys.ainauto.docwb.service.process.auth;

import com.infosys.ainauto.docwb.service.common.WorkbenchException;

public interface IAuthenticationProcess {

	public boolean authenticateUser(String userName, String rawPassword, String tenantId) throws WorkbenchException;
}
