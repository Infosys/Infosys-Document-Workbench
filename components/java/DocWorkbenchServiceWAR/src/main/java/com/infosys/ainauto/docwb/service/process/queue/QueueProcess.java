/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.process.queue;

import java.util.ArrayList;
//import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.docwb.service.common.SessionHelper;
import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.dao.queue.IQueueDataAccess;
import com.infosys.ainauto.docwb.service.model.api.QueueCountResData;
import com.infosys.ainauto.docwb.service.model.api.UserQueueResData;
import com.infosys.ainauto.docwb.service.model.api.UserResData;
import com.infosys.ainauto.docwb.service.model.api.queue.AddQueueReqData;
import com.infosys.ainauto.docwb.service.model.api.queue.AddQueueResData;
import com.infosys.ainauto.docwb.service.model.api.queue.UpdateQueueReqData;
import com.infosys.ainauto.docwb.service.model.api.queue.UpdateUserQueueReqData;
import com.infosys.ainauto.docwb.service.model.db.EntityDbData;
import com.infosys.ainauto.docwb.service.model.db.QueueDbData;
import com.infosys.ainauto.docwb.service.model.db.UserQueueDbData;
import com.infosys.ainauto.docwb.service.model.db.UserRoleDbData;
import com.infosys.ainauto.docwb.service.model.db.ValTableDbData;
import com.infosys.ainauto.docwb.service.model.security.UserDetailsData;

@Component
public class QueueProcess implements IQueueProcess {

	@Autowired
	private IQueueDataAccess queueDataAccess;

	@PostConstruct
	private void init() {
	}

	public List<QueueCountResData> getDocCount(long queueNameCde,boolean assignmentCount) throws WorkbenchException {
		List<QueueCountResData> queueCount = queueDataAccess.getDocCount(queueNameCde,assignmentCount);
		return queueCount;
	}

	public List<QueueCountResData> getDocCountForUser(long appUserId,boolean assignmentCount,List<Long> assignedToList) throws WorkbenchException {
		
		List<QueueCountResData> queueCount = queueDataAccess.getDocCountForUser(appUserId,assignmentCount,assignedToList);
		return queueCount;
	}

	@Override
	public List<UserResData> getQueueUsersList(long queueNameCde,List<Long> allowedUserRoleList) throws WorkbenchException {
		List<UserResData> userResDataList = new ArrayList<>();
		List<UserRoleDbData> userRoleDbDataList = queueDataAccess.getQueueUsers(queueNameCde);

		for (UserRoleDbData userRoleDbData : userRoleDbDataList) {
			if (allowedUserRoleList!=null && !allowedUserRoleList.contains(userRoleDbData.getUserRoleTypeCde())) {
				continue;
			}
			UserResData userResData = new UserResData();
			userResData.setUserId(userRoleDbData.getAppUserId());
			userResData.setUserLoginId(userRoleDbData.getUserLoginId());
			userResData.setUserFullName((userRoleDbData.getUserFullName()));
			userResData.setRoleTypeCde(userRoleDbData.getUserRoleTypeCde());
			userResData.setRoleTypeTxt(userRoleDbData.getUserRoleTypeTxt());
			userResDataList.add(userResData);
		}
		return userResDataList;

	}

	@Override
	public List<ValTableDbData> getQueues() throws WorkbenchException {
		List<ValTableDbData> queueValDbDataList = queueDataAccess.getQueues();
		return queueValDbDataList;
	}

	@Override
	public AddQueueResData addQueue(AddQueueReqData addQueueReqData) throws WorkbenchException {
		AddQueueResData ResDbData = new AddQueueResData();
		QueueDbData queueDbData = new QueueDbData();
		queueDbData.setQueueNameCde(addQueueReqData.getQueueNameCde());
		queueDbData.setQueueNameTxt(addQueueReqData.getQueueNameTxt());
		queueDbData.setDocTypeCde(addQueueReqData.getDocTypeCde());
		queueDbData = queueDataAccess.addQueue(queueDbData);
		ResDbData.setQueueNameCde(queueDbData.getQueueNameCde());
		ResDbData.setQueueNameTxt(addQueueReqData.getQueueNameTxt());

		return ResDbData;
	}

	public List<UserQueueResData> getQueueListForCurrentUser(String queueStatus) throws WorkbenchException {
		UserDetailsData userDetailsData=SessionHelper.getLoginUserData();
		long appUserId=userDetailsData.getUserId();
		List<UserQueueResData> userQueueResDataList =new ArrayList<>();
		List<UserQueueDbData> userQueueDbDataList=queueDataAccess.getQueueListOfUser(appUserId,queueStatus);
		for (UserQueueDbData userQueueDbData : userQueueDbDataList) {
			UserQueueResData userQueueResData = new UserQueueResData();
			userQueueResData.setQueueNameCde(userQueueDbData.getQueueNameCde());
			userQueueResData.setQueueNameTxt(userQueueDbData.getQueueNameTxt());
			userQueueResData.setQueueClosedDtm(userQueueDbData.getQueueClosedDtm());
			userQueueResData.setQueueStatus(userQueueDbData.getQueueStatus());
			userQueueResData.setQueueHideAfterDtm(userQueueDbData.getQueueHideAfterDtm());
			userQueueResData.setUserQueueHideAfterDtm(userQueueDbData.getUserQueueHideAfterDtm());
			userQueueResDataList.add(userQueueResData);
		}
		
		return userQueueResDataList;

	}
	
	public EntityDbData updatePersonalQueueVisibility(List<UpdateUserQueueReqData> updateUserQueueReqDataList) throws WorkbenchException {	
		EntityDbData entityDbData = new EntityDbData();
		long updatedRowCount=0;
		String apiResponse = "";
		updatedRowCount=queueDataAccess.updatePersonalQueueVisibility(updateUserQueueReqDataList);
		if(updatedRowCount>0) {
			apiResponse="Visibility date of "+updatedRowCount+" no. of queue updated.";
		}
		else {
			apiResponse="No date updated.";
		}
		entityDbData.setUpdatedRowCount(updatedRowCount);
		entityDbData.setApiResponseData(apiResponse);
		return entityDbData;
	}
	
	public EntityDbData updateQueueDetails (List<UpdateQueueReqData> updateQueueReqDataList) throws WorkbenchException {	
		EntityDbData entityDbData = new EntityDbData();
		long updatedRowCount=0;
		String apiResponse = "";
		updatedRowCount=queueDataAccess.updateQueueDetails(updateQueueReqDataList);
		if(updatedRowCount>0) {
			apiResponse="Closure date of "+updatedRowCount+" no. of queue updated.";
		}
		else {
			apiResponse="No date updated.";
		}
		entityDbData.setUpdatedRowCount(updatedRowCount);
		entityDbData.setApiResponseData(apiResponse);
		return entityDbData;
	}
}
