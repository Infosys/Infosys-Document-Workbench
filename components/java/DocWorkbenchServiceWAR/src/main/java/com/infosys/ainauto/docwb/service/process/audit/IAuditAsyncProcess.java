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
import com.infosys.ainauto.docwb.service.model.db.AuditDbData;
import com.infosys.ainauto.docwb.service.model.db.EntityDbData;

public interface IAuditAsyncProcess {

	public void addAuditDetailsAsync(List<EntityDbData> entityDataList, EnumEntityType entityName,
			EnumOperationType operationType, String loggedInUser, String tenantId);

	public void addAuditDetailsAsync(List<EntityDbData> prevEntityDbDataList, List<EntityDbData> latestEntityDbDataList,
			EnumEntityType entityName, EnumOperationType operationType, String loggedInUser, String tenantId);

	public void addAudit(List<AuditDbData> auditDataList, String loggedInUser, String tenantId)
			throws WorkbenchException;

}
