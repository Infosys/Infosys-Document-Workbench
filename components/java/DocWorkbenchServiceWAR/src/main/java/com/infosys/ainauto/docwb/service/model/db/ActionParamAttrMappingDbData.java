/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.db;

import java.sql.Timestamp;

public class ActionParamAttrMappingDbData {

    private long actionParamAttrMapId;
    private long docId;
    private long docActionRelId;
    private long actionParamAttrRelId;
    private int actionNameCde;
    private String actionNameTxt;
    private int paramNameCde;
    private String paramNameTxt;
    private String paramValue;
    private int attrNameCde;
    private String attrNameTxt;
    private String attrValue;
    private int taskStatusCde;
    private String taskStatusTxt;
    private int taskTypeCde;
    private int pageNumber;
    private String actionResult;
    private String snapShot;
    private Timestamp createDtm;
    private String createByUserLoginId;
    private String createByUserFullName;
    private int createByUserTypeCde;
    private String createByUserTypeTxt;
    private Timestamp lastModDtm;
    private int queueNameCde;
    
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
    public int getAttrNameCde() {
        return attrNameCde;
    }
    public void setAttrNameCde(int attrNameCde) {
        this.attrNameCde = attrNameCde;
    }
    public String getAttrNameTxt() {
        return attrNameTxt;
    }
    public void setAttrNameTxt(String attrNameTxt) {
        this.attrNameTxt = attrNameTxt;
    }
    public int getParamNameCde() {
        return paramNameCde;
    }
    public void setParamNameCde(int paramNameCde) {
        this.paramNameCde = paramNameCde;
    }
    public String getParamNameTxt() {
        return paramNameTxt;
    }
    public void setParamNameTxt(String paramNameTxt) {
        this.paramNameTxt = paramNameTxt;
    }
    public long getDocId() {
        return docId;
    }
    public void setDocId(long docId) {
        this.docId = docId;
    }
    public int getTaskStatusCde() {
        return taskStatusCde;
    }
    public void setTaskStatusCde(int taskStatusCde) {
        this.taskStatusCde = taskStatusCde;
    }
    public int getPageNumber() {
		return pageNumber;
	}
	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}
    public String getTaskStatusTxt() {
        return taskStatusTxt;
    }
    public void setTaskStatusTxt(String taskStatusTxt) {
        this.taskStatusTxt = taskStatusTxt;
    }
    public int getTaskTypeCde() {
        return taskTypeCde;
    }
    public void setTaskTypeCde(int taskTypeCde) {
        this.taskTypeCde = taskTypeCde;
    }
    public long getDocActionRelId() {
        return docActionRelId;
    }
    public void setDocActionRelId(long docActionRelId) {
        this.docActionRelId = docActionRelId;
    }
    public String getAttrValue() {
        return attrValue;
    }
    public void setAttrValue(String attrValue) {
        this.attrValue = attrValue;
    }
    public long getActionParamAttrRelId() {
        return actionParamAttrRelId;
    }
    public void setActionParamAttrRelId(long actionParamAttrRelId) {
        this.actionParamAttrRelId = actionParamAttrRelId;
    }
    public Timestamp getCreateDtm() {
        return createDtm;
    }
    public void setCreateDtm(Timestamp createDtm) {
        this.createDtm = createDtm;
    }
    public String getCreateByUserLoginId() {
        return createByUserLoginId;
    }
    public void setCreateByUserLoginId(String createByUserLoginId) {
        this.createByUserLoginId = createByUserLoginId;
    }
    public Timestamp getLastModDtm() {
        return lastModDtm;
    }
    public void setLastModDtm(Timestamp lastModDtm) {
        this.lastModDtm = lastModDtm;
    }
	public String getActionResult() {
		return actionResult;
	}
	public void setActionResult(String actionResult) {
		this.actionResult = actionResult;
	}
	public String getParamValue() {
		return paramValue;
	}
	public void setParamValue(String paramValue) {
		this.paramValue = paramValue;
	}
	public String getSnapShot() {
		return snapShot;
	}
	public void setSnapShot(String snapShot) {
		this.snapShot = snapShot;
	}
	public int getCreateByUserTypeCde() {
		return createByUserTypeCde;
	}
	public void setCreateByUserTypeCde(int createByUserTypeCde) {
		this.createByUserTypeCde = createByUserTypeCde;
	}
	public String getCreateByUserTypeTxt() {
		return createByUserTypeTxt;
	}
	public void setCreateByUserTypeTxt(String createByUserTypeTxt) {
		this.createByUserTypeTxt = createByUserTypeTxt;
	}
	public String getCreateByUserFullName() {
		return createByUserFullName;
	}
	public void setCreateByUserFullName(String createByUserFullName) {
		this.createByUserFullName = createByUserFullName;
	}
	public int getQueueNameCde() {
		return queueNameCde;
	}
	public void setQueueNameCde(int queueNameCde) {
		this.queueNameCde = queueNameCde;
	}
}
