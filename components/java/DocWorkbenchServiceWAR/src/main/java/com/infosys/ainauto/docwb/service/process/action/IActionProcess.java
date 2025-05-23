/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.process.action;

import java.util.List;

import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.model.api.ActionResData;
import com.infosys.ainauto.docwb.service.model.api.DocumentResData;
import com.infosys.ainauto.docwb.service.model.api.PaginationResData;
import com.infosys.ainauto.docwb.service.model.api.RecommendedActionResData;
import com.infosys.ainauto.docwb.service.model.api.action.GetActionReqData;
import com.infosys.ainauto.docwb.service.model.api.action.InsertActionReqData;
import com.infosys.ainauto.docwb.service.model.api.action.UpdateActionReqData;
import com.infosys.ainauto.docwb.service.model.db.EntityDbData;

public interface IActionProcess {
	public List<ActionResData> getActionMappingList() throws WorkbenchException;
	
	public List<Long> insertActions(List<InsertActionReqData> insertActionReqDataList) throws WorkbenchException;

	public List<DocumentResData> getActionTaskList(GetActionReqData getActionReqData) throws WorkbenchException;
	
	public PaginationResData getPaginationForActions(GetActionReqData getActionReqData) throws WorkbenchException;
	
	public List<EntityDbData> updateActionTaskList(List<UpdateActionReqData> updateActionReqDataList) throws WorkbenchException;
	
	public long deleteAction(long docActionRelId) throws WorkbenchException;

	public List<ActionResData> getActionData(int actionNameCde, long docId) throws WorkbenchException;
	
	public RecommendedActionResData getRecommendedAction(long docId) throws WorkbenchException;

}
