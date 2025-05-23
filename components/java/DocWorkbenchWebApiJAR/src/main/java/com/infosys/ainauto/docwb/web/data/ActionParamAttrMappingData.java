/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.web.data;

import java.sql.Timestamp;
import java.util.List;

import com.infosys.ainauto.docwb.web.type.EnumCallType;
import com.infosys.ainauto.docwb.web.type.EnumDataFlowType;
import com.infosys.ainauto.docwb.web.type.EnumTaskStatus;
import com.infosys.ainauto.docwb.web.type.EnumTaskType;

public class ActionParamAttrMappingData {

    private long actionParamAttrMapId;
    private long docId;
    private long docActionRelId;
    private long actionParamAttrRelId;
    private int actionNameCde;
    private String actionNameTxt;
    private EnumDataFlowType dataFlowType;
    private EnumCallType callType;
    private EnumTaskStatus taskStatus;
    private String taskStatusTxt;
    private EnumTaskType taskType;

    private String createDtm;
    private Timestamp lastModDtm;
	private List<ParamAttrData> paramAttrDataList;
	private String taskActionResult;
    
    public long getActionParamAttrMapId() {
        return actionParamAttrMapId;
    }
    public void setActionParamAttrMapId(long actionParamAttrMapId) {
        this.actionParamAttrMapId = actionParamAttrMapId;
    }
    public int getActionNameCde() {
        return actionNameCde;
    }
    public void setActionNameCde(int actionNameCde) {
        this.actionNameCde = actionNameCde;
    }
    public String getActionNameTxt() {
        return actionNameTxt;
    }
    public void setActionNameTxt(String actionNameTxt) {
        this.actionNameTxt = actionNameTxt;
    }
    public long getDocId() {
        return docId;
    }
    public void setDocId(long docId) {
        this.docId = docId;
    }
    public String getTaskActionResult() {
        return taskActionResult;
    }
    public void setTaskActionResult(String result) {
        this.taskActionResult = result;
    }
    public String getTaskStatusTxt() {
        return taskStatusTxt;
    }
    public void setTaskStatusTxt(String taskStatusTxt) {
        this.taskStatusTxt = taskStatusTxt;
    }
    public long getDocActionRelId() {
        return docActionRelId;
    }
    public void setDocActionRelId(long docActionRelId) {
        this.docActionRelId = docActionRelId;
    }
    public long getActionParamAttrRelId() {
        return actionParamAttrRelId;
    }
    public void setActionParamAttrRelId(long actionParamAttrRelId) {
        this.actionParamAttrRelId = actionParamAttrRelId;
    }
    public String getCreateDtm() {
        return createDtm;
    }
    public void setCreateDtm(String createDtm) {
        this.createDtm = createDtm;
    }
    public Timestamp getLastModDtm() {
        return lastModDtm;
    }
    public void setLastModDtm(Timestamp lastModDtm) {
        this.lastModDtm = lastModDtm;
    }
	public void setParamAttrDataList(List<ParamAttrData> paramAttrDataList) {
		this.paramAttrDataList=paramAttrDataList;
	}
	public List<ParamAttrData> getParamAttrDataList(){
		return paramAttrDataList;
	}
	public EnumCallType getCallType() {
		return callType;
	}
	public void setCallType(EnumCallType callType) {
		this.callType = callType;
	}
	public EnumDataFlowType getDataFlowType() {
		return dataFlowType;
	}
	public void setDataFlowType(EnumDataFlowType dataFlowType) {
		this.dataFlowType = dataFlowType;
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
