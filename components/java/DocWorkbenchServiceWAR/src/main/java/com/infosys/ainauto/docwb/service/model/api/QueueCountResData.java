/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.api;

public class QueueCountResData {
		
	private long queueNameCde;
	private long taskStatusCde;
	private long docCount;
//	private long unassignedCount;
//	private long assignedCount;
	private long myCasesCount;
	
	public long getTaskStatusCde() {
        return taskStatusCde;
    }
    public void setTaskStatusCde(long taskStatusCde) {
        this.taskStatusCde = taskStatusCde;
    }
    
    public long getQueueNameCde() {
        return queueNameCde;
    }
    public void setQueueNameCde(long queueNameCde) {
        this.queueNameCde = queueNameCde;
    }

    public long getDocCount() {
        return docCount;
    }
    public void setDocCount(long docCount) {
        this.docCount = docCount;
    }
	public long getMyCasesCount() {
		return myCasesCount;
	}
	public void setMyCasesCount(long myCasesCount) {
		this.myCasesCount = myCasesCount;
	}
//	public long getAssignedCount() {
//		return assignedCount;
//	}
//	public void setAssignedCount(long assignedCount) {
//		this.assignedCount = assignedCount;
//	}
//	public long getUnassignedCount() {
//		return unassignedCount;
//	}
//	public void setUnassignedCount(long unassignedCount) {
//		this.unassignedCount = unassignedCount;
//	}
    
}
