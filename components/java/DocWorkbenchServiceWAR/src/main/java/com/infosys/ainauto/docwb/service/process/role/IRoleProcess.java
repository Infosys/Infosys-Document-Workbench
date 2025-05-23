/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.process.role;

import java.util.List;

import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.model.api.role.AddRoleReqData;
import com.infosys.ainauto.docwb.service.model.api.role.DeleteRoleReqData;
import com.infosys.ainauto.docwb.service.model.db.EntityDbData;
import com.infosys.ainauto.docwb.service.model.db.UserRoleDbData;


public interface IRoleProcess {

	public List<EntityDbData> addNewRole(AddRoleReqData addRoleReqData) throws WorkbenchException;

	public List<Long> deleteRole(List<DeleteRoleReqData> deleteRoleReqDataList) throws WorkbenchException;
	
	public List<UserRoleDbData> getRole(int appUserId) throws WorkbenchException;
}

