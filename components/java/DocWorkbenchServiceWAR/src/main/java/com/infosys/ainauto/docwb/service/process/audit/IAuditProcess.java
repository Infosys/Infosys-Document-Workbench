/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.process.audit;

import java.util.List;

import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.common.type.EnumEntityType;
import com.infosys.ainauto.docwb.service.common.type.EnumOperationType;
import com.infosys.ainauto.docwb.service.model.api.AuditResData;
import com.infosys.ainauto.docwb.service.model.api.audit.AddDocAuditReqData;
import com.infosys.ainauto.docwb.service.model.api.audit.GetDocAuditReqData;
import com.infosys.ainauto.docwb.service.model.api.audit.GetUserAuditReqData;
import com.infosys.ainauto.docwb.service.model.db.EntityDbData;

public interface IAuditProcess {

	public void addAuditDetails(List<EntityDbData> inputDataList, EnumEntityType entityName,
			EnumOperationType operationType);

	public void addAuditDetails(List<EntityDbData> prevEntityDbDataList, List<EntityDbData> latestEntityDbDataList,
			EnumEntityType entityName, EnumOperationType operationType);

	public int addDocAuditDetails(AddDocAuditReqData addDocAuditReqData) throws WorkbenchException;

	public AuditResData getAuditForDoc(GetDocAuditReqData getDocAuditReqData) throws WorkbenchException;

	public AuditResData getAuditForUser(GetUserAuditReqData getUserAuditReqData) throws WorkbenchException;

	public AuditResData getAuditForAppVariableKey(String appVariableKey, int pageNumber) throws WorkbenchException;
	
	public AuditResData getCaseAuditForUser(GetDocAuditReqData getDocAuditReqData) throws WorkbenchException;

}
