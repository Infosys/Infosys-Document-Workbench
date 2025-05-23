/** =============================================================================================================== *
 * Copyright 2022 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


package com.infosys.ainauto.docwb.service.service.idms;

import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.model.security.IdmsConfigData.TenantConfigData;
import com.infosys.ainauto.docwb.service.model.service.IdmsUserReqData;
import com.infosys.ainauto.docwb.service.model.service.IdmsUserResData;

public interface IIdentityManagementSystemService {

	public IdmsUserResData getLdapAuthData(IdmsUserReqData idmsUserReqData) throws WorkbenchException;

	public boolean isLdapAuthEnabled();

	public TenantConfigData getTenantConfigData(String tenantId);

}
