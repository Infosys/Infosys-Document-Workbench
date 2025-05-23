/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.api;

import java.util.List;

public class ActionResData {

	private long docActionRelId;
	private int actionNameCde;
	private String actionNameTxt;
	private int taskStatusCde;
	private String taskStatusTxt;
	private int taskTypeCde;
	private String actionResult;
	private String snapShot;
	private String createDtm;
	private String createByUserLoginId;
	private String createByUserFullName;
	private int createByUserTypeCde;
	private String createByUserTypeTxt;
	private String createDtmDuration;
	private String lastModDtm;
	private String lastModDtmDuration;
	private List<ParamAttrResData> mappingList;
	private List<ParamResData> paramList;

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

	public int getTaskTypeCde() {
		return taskTypeCde;
	}

	public void setTaskTypeCde(int taskTypeCde) {
		this.taskTypeCde = taskTypeCde;
	}

	public List<ParamAttrResData> getMappingList() {
		return mappingList;
	}

	public void setMappingList(List<ParamAttrResData> mappingList) {
		this.mappingList = mappingList;
	}

	public int getTaskStatusCde() {
		return taskStatusCde;
	}

	public void setTaskStatusCde(int taskStatusCde) {
		this.taskStatusCde = taskStatusCde;
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

	public String getCreateDtm() {
		return createDtm;
	}

	public void setCreateDtm(String createDtm) {
		this.createDtm = createDtm;
	}

	public String getCreateByUserLoginId() {
		return createByUserLoginId;
	}

	public void setCreateByUserLoginId(String createByUserLoginId) {
		this.createByUserLoginId = createByUserLoginId;
	}

	public String getLastModDtm() {
		return lastModDtm;
	}

	public void setLastModDtm(String lastModDtm) {
		this.lastModDtm = lastModDtm;
	}

	public String getCreateDtmDuration() {
		return createDtmDuration;
	}

	public void setCreateDtmDuration(String createDtmDuration) {
		this.createDtmDuration = createDtmDuration;
	}

	public String getLastModDtmDuration() {
		return lastModDtmDuration;
	}

	public void setLastModDtmDuration(String lastModDtmDuration) {
		this.lastModDtmDuration = lastModDtmDuration;
	}

	public String getActionResult() {
		return actionResult;
	}

	public void setActionResult(String actionResult) {
		this.actionResult = actionResult;
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

	public List<ParamResData> getParamList() {
		return paramList;
	}

	public void setParamList(List<ParamResData> paramList) {
		this.paramList = paramList;
	}

}
