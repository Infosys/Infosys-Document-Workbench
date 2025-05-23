/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.dao.queue;

import java.util.List;

import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.model.api.QueueCountResData;
import com.infosys.ainauto.docwb.service.model.api.queue.UpdateQueueReqData;
import com.infosys.ainauto.docwb.service.model.api.queue.UpdateUserQueueReqData;
import com.infosys.ainauto.docwb.service.model.db.QueueDbData;
import com.infosys.ainauto.docwb.service.model.db.UserQueueDbData;
import com.infosys.ainauto.docwb.service.model.db.UserRoleDbData;
import com.infosys.ainauto.docwb.service.model.db.ValTableDbData;

public interface IQueueDataAccess {

	public List<QueueCountResData> getDocCount(long queueNameCde,boolean assignmentCount) throws WorkbenchException;

	public List<QueueCountResData> getDocCountForUser(long appUserId,boolean assignmentCount,List<Long> assignedToList) throws WorkbenchException;

	public List<UserRoleDbData> getQueueUsers(long queueNameCde) throws WorkbenchException;
	
	public List<ValTableDbData> getQueues() throws WorkbenchException; 
	
	public QueueDbData addQueue(QueueDbData queueDBData)throws WorkbenchException;
	
	public List<UserQueueDbData> getQueueListOfUser(long appUserId,String queueStatus)throws WorkbenchException;
	
	public long updatePersonalQueueVisibility(List<UpdateUserQueueReqData> updateUserQueueReqDataList) throws WorkbenchException;
	
	public long updateQueueDetails (List<UpdateQueueReqData> updateQueueReqDataList) throws WorkbenchException;
}
