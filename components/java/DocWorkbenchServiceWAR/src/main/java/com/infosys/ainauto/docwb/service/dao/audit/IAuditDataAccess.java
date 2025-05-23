/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.dao.audit;

import java.util.List;

import org.springframework.stereotype.Component;

import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.common.type.EnumEntityType;
import com.infosys.ainauto.docwb.service.model.db.AuditDbData;
import com.infosys.ainauto.docwb.service.model.db.DocAuditDbData;
import com.infosys.ainauto.docwb.service.model.db.UserAuditDbData;

@Component
public interface IAuditDataAccess {

	public List<AuditDbData> addAudit(List<AuditDbData> auditDbDataList, String loggedInUser, String tenantId)
			throws WorkbenchException;

	public void addDocAuditRel(AuditDbData auditDbData, String loggedInUser, String tenantId) throws WorkbenchException;

	public void addUserAuditRel(AuditDbData auditDbData, String loggedInUser, String tenantId)
			throws WorkbenchException;

	public List<DocAuditDbData> getDocValues(EnumEntityType entityName, String columnName, long entityValue,
			String tenantId) throws WorkbenchException;

	public List<UserAuditDbData> getUserValues(EnumEntityType entityName, String columnName, Long entityValue,
			String tenantId) throws WorkbenchException;

	public long getAuditCount(long docId, long appUserId, String appVariableKey) throws WorkbenchException;

	List<AuditDbData> getAudit(long docId, long appUserId, String appVariableKey, int pageNumber)
			throws WorkbenchException;
}
