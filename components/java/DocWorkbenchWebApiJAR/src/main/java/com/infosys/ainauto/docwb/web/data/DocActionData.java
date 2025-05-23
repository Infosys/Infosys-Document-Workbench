/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.web.data;

import java.util.List;

import com.infosys.ainauto.docwb.web.type.EnumTaskStatus;
import com.infosys.ainauto.docwb.web.type.EnumTaskType;

public class DocActionData {
	private long docId;
	private List<ActionParamAttrMappingData> actionParamAttrMappingDataList;
	private List<ActionData> actionDataList;
	private EnumTaskStatus taskStatus;
	private EnumTaskType taskType;

	public long getDocId() {
		return docId;
	}

	public void setDocId(long docId2) {
		this.docId = docId2;
	}

	public void setActionParamAttrMappingDataList(List<ActionParamAttrMappingData> actionParamAttrMappingDataList) {
		this.actionParamAttrMappingDataList = actionParamAttrMappingDataList;
	}

	public List<ActionParamAttrMappingData> getActionParamAttrMappingDataList() {
		return actionParamAttrMappingDataList;
	}

	public void setActionDataList(List<ActionData> actionDataList) {
		this.actionDataList = actionDataList;
	}

	public List<ActionData> getActionDataList() {
		return actionDataList;
	}

	public EnumTaskType getTaskType() {
		return taskType;
	}

	public void setTaskType(EnumTaskType taskType) {
		this.taskType = taskType;
	}

	public EnumTaskStatus getTaskStatus() {
		return taskStatus;
	}

	public void setTaskStatus(EnumTaskStatus taskStatus) {
		this.taskStatus = taskStatus;
	}
}
