/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.dao.action;

import java.util.List;

import org.springframework.stereotype.Component;

import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.model.db.ActionParamAttrMappingDbData;
import com.infosys.ainauto.docwb.service.model.db.EntityDbData;

@Component
public interface IDocActionDataAccess {

    public List<ActionParamAttrMappingDbData> getActionMappingList() throws WorkbenchException;

	public List<ActionParamAttrMappingDbData> getActionTaskList(ActionParamAttrMappingDbData filterData,
			int queueNameCde, String taskStatusOp) throws WorkbenchException;

	public long getTotalActionCount(ActionParamAttrMappingDbData filterData,
			int queueNameCde, String taskStatusOp) throws WorkbenchException;
	
    public List<EntityDbData> updateActionTask(List<ActionParamAttrMappingDbData> actionParamAttrMappingDbDataList) throws WorkbenchException;

    public long addActionToDocument(ActionParamAttrMappingDbData actionParamAttrMappingDbData) throws WorkbenchException;

    public int addActionParamAttrRel(List<ActionParamAttrMappingDbData> actionParamAttrMappingDbDataList) throws WorkbenchException;
    public long deleteActionFromDoc(long docActionRelId) throws WorkbenchException;
    public int deleteActionParamAttrRel(long docActionRelId) throws WorkbenchException;

	public List<ActionParamAttrMappingDbData> getActionData(int actionNameCde, long docId) throws WorkbenchException;
	
	public String getActionResult(long docActionRelId) throws WorkbenchException;

}
