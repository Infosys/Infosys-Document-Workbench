/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.process.auth;

import java.util.List;

import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.model.api.UserFeatureAuthResData;

public interface IApiRoleAuthorizationProcess {

	public List<UserFeatureAuthResData> getLoggedInUserRoleFeatureAuthData() throws WorkbenchException;

	public boolean isApiAccessAllowed(String api, String apiMethod, boolean isCheckForExcludeOnly)
			throws WorkbenchException;

	public boolean isFeatureAccessAllowed(String featureId) throws WorkbenchException;

	public List<Long> getFeatureAllowedRoleTypeCde(String featureId) throws WorkbenchException;
}
