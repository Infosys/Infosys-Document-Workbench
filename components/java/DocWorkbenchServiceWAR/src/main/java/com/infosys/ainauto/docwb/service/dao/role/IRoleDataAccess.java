/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.dao.role;

import java.util.List;

import org.springframework.stereotype.Component;

import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.model.db.UserRoleDbData;

@Component
public interface IRoleDataAccess {
	public long insertUserRole(UserRoleDbData userRoleDbData) throws WorkbenchException;

	public long deleteUserRole(UserRoleDbData userRoleDbData) throws WorkbenchException;
	
	public List<UserRoleDbData> getRole(int appUserId) throws WorkbenchException;
}
