/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.dao.user;

import java.util.List;

import org.springframework.stereotype.Component;

import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.model.db.AppUserDbData;
import com.infosys.ainauto.docwb.service.model.db.EntityDbData;
import com.infosys.ainauto.docwb.service.model.db.UserQueueDbData;
import com.infosys.ainauto.docwb.service.model.db.UserRoleDbData;
import com.infosys.ainauto.docwb.service.model.db.UserTeammateDbData;

@Component
public interface IUserDataAccess {

	public AppUserDbData getUserData(String tableName,String tenantId) throws WorkbenchException;

	public long insertUser(AppUserDbData appuserDbData) throws WorkbenchException;

	public List<UserQueueDbData> getUserQueueDetails(long appUserId) throws WorkbenchException;

	public long insertUserQueueRel(UserQueueDbData userQueueDbData) throws WorkbenchException;

	public long deleteUserQueueRel(long appUserQueueRelId) throws WorkbenchException;
	
	public List<UserRoleDbData> getUserListDetails() throws WorkbenchException;

	public EntityDbData updateUserAccount(AppUserDbData appuserDbData) throws WorkbenchException;

	public long changePassword(long userId, String newPassword) throws WorkbenchException;
	
	public List<String> getTenants() throws WorkbenchException;

	public AppUserDbData getUserData(String key) throws WorkbenchException;
	
	public AppUserDbData getUserDetailsFromLoginId(String userLoginId, String tenantId) throws WorkbenchException;
	
	public List<UserTeammateDbData> getTeammateUserListDetails() throws WorkbenchException;
}
