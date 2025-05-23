/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.process.user;

import java.util.List;

import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.model.api.UserQueueResData;
import com.infosys.ainauto.docwb.service.model.api.UserResData;
import com.infosys.ainauto.docwb.service.model.api.UserTeammateResData;
import com.infosys.ainauto.docwb.service.model.api.user.InsertUserQueueReqData;
import com.infosys.ainauto.docwb.service.model.api.user.InsertUserReqData;
import com.infosys.ainauto.docwb.service.model.api.user.UpdateUserReqData;
import com.infosys.ainauto.docwb.service.model.db.EntityDbData;
import com.infosys.ainauto.docwb.service.model.process.AppUserData;

public interface IUserProcess {

	public EntityDbData addUser(InsertUserReqData insertUserReqData,String tenantId) throws WorkbenchException;

	public long addUserToQueue(InsertUserQueueReqData insertUserQueueReqData) throws WorkbenchException;

	public List<UserQueueResData> getUserQueueDetails(long appUserId) throws WorkbenchException; 
	
	public long deleteUserFromQueue(long appUserQueueRelId) throws WorkbenchException;

	public UserResData getLoggedInUserDetails() throws WorkbenchException;

	public List<UserResData> getUserListDetails() throws WorkbenchException; 

	public EntityDbData updateUserAccountEnabled(UpdateUserReqData updateUserReqData) throws WorkbenchException;

	public EntityDbData changePassword(String oldPassword, String newPassword) throws WorkbenchException;

	public AppUserData getUserDetailsFromLoginId(String appUserLoginId, String tenantId)throws WorkbenchException;
	
	public List<UserTeammateResData> getTeammateListDetails() throws WorkbenchException;
	
}
