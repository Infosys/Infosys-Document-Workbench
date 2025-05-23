/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.process.queue;


import java.util.List;

import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.model.api.QueueCountResData;
import com.infosys.ainauto.docwb.service.model.api.UserQueueResData;
import com.infosys.ainauto.docwb.service.model.api.UserResData;
import com.infosys.ainauto.docwb.service.model.api.queue.AddQueueReqData;
import com.infosys.ainauto.docwb.service.model.api.queue.AddQueueResData;
import com.infosys.ainauto.docwb.service.model.api.queue.UpdateQueueReqData;
import com.infosys.ainauto.docwb.service.model.api.queue.UpdateUserQueueReqData;
import com.infosys.ainauto.docwb.service.model.db.EntityDbData;
import com.infosys.ainauto.docwb.service.model.db.ValTableDbData;

public interface IQueueProcess {

	public List<QueueCountResData> getDocCount(long queueNameCde,boolean assignmentCount) throws WorkbenchException;

	public List<QueueCountResData> getDocCountForUser(long appUserId,boolean assignmentCount,List<Long> assignedToList) throws WorkbenchException;

	public List<UserResData> getQueueUsersList(long queueNameCde, List<Long> allowedUserRoleList) throws WorkbenchException;
	
	public List<ValTableDbData> getQueues() throws WorkbenchException;
	
	public AddQueueResData addQueue(AddQueueReqData addQueueReqData) throws WorkbenchException;

	public List<UserQueueResData> getQueueListForCurrentUser(String queueStatus)throws WorkbenchException;

	public EntityDbData updatePersonalQueueVisibility(List<UpdateUserQueueReqData> updateUserQueueReqDataList) throws WorkbenchException;

	public EntityDbData updateQueueDetails (List<UpdateQueueReqData> updateQueueReqDataList) throws WorkbenchException;
}
