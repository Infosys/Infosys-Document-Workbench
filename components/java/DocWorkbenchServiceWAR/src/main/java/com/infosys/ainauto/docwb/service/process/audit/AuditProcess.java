/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.process.audit;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.DateUtility;
import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.docwb.service.common.SessionHelper;
import com.infosys.ainauto.docwb.service.common.WorkbenchConstants;
import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.common.type.EnumEntityType;
import com.infosys.ainauto.docwb.service.common.type.EnumOperationType;
import com.infosys.ainauto.docwb.service.dao.audit.IAuditDataAccess;
import com.infosys.ainauto.docwb.service.model.api.AuditResData;
import com.infosys.ainauto.docwb.service.model.api.PaginationResData;
import com.infosys.ainauto.docwb.service.model.api.audit.AddDocAuditReqData;
import com.infosys.ainauto.docwb.service.model.api.audit.GetDocAuditReqData;
import com.infosys.ainauto.docwb.service.model.api.audit.GetUserAuditReqData;
import com.infosys.ainauto.docwb.service.model.db.AuditDbData;
import com.infosys.ainauto.docwb.service.model.db.EntityDbData;

@Component
public class AuditProcess implements IAuditProcess {

	@Autowired
	private IAuditDataAccess auditDataAccess;

	@Autowired
	private IAuditAsyncProcess auditAsyncProcess;

	private static final int pageSize = 15;

	@Override
	public void addAuditDetails(List<EntityDbData> entityDataList, EnumEntityType entityName,
			EnumOperationType operationType) {
		String user = WorkbenchConstants.USER_TYPE_EXTERNAL;
		String tenantId = "";
		if (ListUtility.hasValue(entityDataList))
			tenantId = entityDataList.get(0).getTenantId();
		if (!SessionHelper.getLoginUsername().isEmpty()) {
			user = SessionHelper.getLoginUsername();
		}
		if (!SessionHelper.getTenantId().isEmpty()) {
			tenantId = SessionHelper.getTenantId();
		}

		auditAsyncProcess.addAuditDetailsAsync(entityDataList, entityName, operationType, user, tenantId);
	}

	@Override
	public void addAuditDetails(List<EntityDbData> prevEntityDbDataList, List<EntityDbData> latestEntityDbDataList,
			EnumEntityType entityName, EnumOperationType operationType) {
		String user = WorkbenchConstants.USER_TYPE_EXTERNAL;
		String tenantId = "";
		if (!SessionHelper.getLoginUsername().isEmpty()) {
			user = SessionHelper.getLoginUsername();
		}
		if (!SessionHelper.getTenantId().isEmpty()) {
			tenantId = SessionHelper.getTenantId();
		}

		auditAsyncProcess.addAuditDetailsAsync(prevEntityDbDataList, latestEntityDbDataList, entityName, operationType,
				user, tenantId);
	}

	@Override
	public int addDocAuditDetails(AddDocAuditReqData addDocAuditReqData) throws WorkbenchException {

		String user = WorkbenchConstants.USER_TYPE_EXTERNAL;
		String tenantId = "";
		if (!SessionHelper.getLoginUsername().isEmpty()) {
			user = SessionHelper.getLoginUsername();
		}
		if (!SessionHelper.getTenantId().isEmpty()) {
			tenantId = SessionHelper.getTenantId();
		}

		List<AuditDbData> auditDataList = new ArrayList<AuditDbData>();

		for (AddDocAuditReqData.AuditData auditData : addDocAuditReqData.getAuditDataList()) {
			AuditDbData auditDbData = new AuditDbData();
			auditDbData.setEntityName(auditData.getEntityName());
			auditDbData.setEntityValue(auditData.getEntityValue());
			auditDbData.setAuditMessage(auditData.getAuditMessage());
			auditDbData.setPreviousValue(auditData.getPreviousValue());
			auditDbData.setCurrentValue(auditData.getCurrentValue());
			auditDbData.setDocId(addDocAuditReqData.getDocId());
			auditDataList.add(auditDbData);
		}
		auditAsyncProcess.addAudit(auditDataList, user, tenantId);
		return auditDataList.size();
	}

	public AuditResData getAuditForDoc(GetDocAuditReqData getDocAuditReqData) throws WorkbenchException {
		List<AuditDbData> auditDbDataList = new ArrayList<AuditDbData>();
		AuditResData resultData = new AuditResData();
		List<AuditDbData> auditDataList = auditDataAccess.getAudit(getDocAuditReqData.getDocId(), 0, "",
				getDocAuditReqData.getPageNumber());
		resultData = getPaginationForAudit(getDocAuditReqData.getDocId(), 0, "", getDocAuditReqData.getPageNumber());
		PaginationResData paginationResData = resultData.getPaginationData();
		if (ListUtility.hasValue(auditDataList) && paginationResData != null) {
			for (int i = 0; i < auditDataList.size(); i++) {
				AuditDbData auditData = auditDataList.get(i);
				long auditId = (resultData.getTotalCount() - i)
						- (pageSize * (paginationResData.getCurrentPageNumber() - 1));
				auditData.setAuditId(auditId);
				if (auditData.getUserType() > 1) {
					auditData.setAuditLoginId(WorkbenchConstants.USER_TYPE_SERVICE);
				}
				auditData.setCreateDtm(DateUtility.toString(auditData.getAuditEventDtm(),
						WorkbenchConstants.API_TIMESTAMP_FORMAT_12HR));
				auditDbDataList.add(auditData);
			}
		}
		resultData.setAuditDataList(auditDataList);
		return resultData;
	}

	public AuditResData getAuditForUser(GetUserAuditReqData getUserAuditReqData) throws WorkbenchException {
		List<AuditDbData> auditDbDataList = new ArrayList<AuditDbData>();
		AuditResData resultData = new AuditResData();
		List<AuditDbData> auditDataList = auditDataAccess.getAudit(0, getUserAuditReqData.getAppUserId(), "",
				getUserAuditReqData.getPageNumber());
		resultData = getPaginationForAudit(0, getUserAuditReqData.getAppUserId(), "",
				getUserAuditReqData.getPageNumber());
		PaginationResData paginationResData = resultData.getPaginationData();
		if (ListUtility.hasValue(auditDataList) && paginationResData != null) {
			for (int i = 0; i < auditDataList.size(); i++) {
				AuditDbData auditData = auditDataList.get(i);
				long auditId = (resultData.getTotalCount() - i)
						- (pageSize * (paginationResData.getCurrentPageNumber() - 1));
				auditData.setAuditId(auditId);
				auditData.setCreateDtm(DateUtility.toString(auditData.getAuditEventDtm(),
						WorkbenchConstants.API_TIMESTAMP_FORMAT_12HR));
				auditDbDataList.add(auditData);
			}
		}
		resultData.setAuditDataList(auditDataList);
		return resultData;
	}

	@Override
	public AuditResData getAuditForAppVariableKey(String appVariableKey, int pageNumber) throws WorkbenchException {
		List<AuditDbData> auditDbDataList = new ArrayList<AuditDbData>();
		AuditResData resultData = new AuditResData();
		List<AuditDbData> auditDataList = auditDataAccess.getAudit(0, 0, appVariableKey, pageNumber);
		resultData = getPaginationForAudit(0, 0, appVariableKey, pageNumber);
		PaginationResData paginationResData = resultData.getPaginationData();
		if (ListUtility.hasValue(auditDataList) && paginationResData != null) {
			for (int i = 0; i < auditDataList.size(); i++) {
				AuditDbData auditData = auditDataList.get(i);
				long auditId = (resultData.getTotalCount() - i)
						- (pageSize * (paginationResData.getCurrentPageNumber() - 1));
				auditData.setAuditId(auditId);
				auditData.setCreateDtm(DateUtility.toString(auditData.getAuditEventDtm(),
						WorkbenchConstants.API_TIMESTAMP_FORMAT_12HR));
				auditDbDataList.add(auditData);
			}
		}
		resultData.setAuditDataList(auditDataList);
		return resultData;
	}

	private AuditResData getPaginationForAudit(long docId, long appUserId, String appVariableKey, int pageNumber)
			throws WorkbenchException {
		int totalPages = 0;
		int currentPage = 0;
		long totalCount = 0;
		totalCount = auditDataAccess.getAuditCount(docId, appUserId, appVariableKey);
		if (totalCount <= 0) {
			currentPage = 0;
			totalPages = 0;
		} else {
			double total = (totalCount * 1.0) / pageSize;
			totalPages = (int) Math.ceil(total);
			currentPage = pageNumber;
			if (totalCount <= pageSize || currentPage < 1) {
				if (currentPage <= totalPages) {
					currentPage = 1;
				}
			}
		}
		AuditResData auditResData = new AuditResData();
		PaginationResData paginationResData = new PaginationResData();
		paginationResData.setCurrentPageNumber(currentPage);
		paginationResData.setTotalPageCount(totalPages);
		paginationResData.setTotalItemCount(totalCount);
		paginationResData.setPageSize(pageSize);
		auditResData.setPaginationData(paginationResData);
		auditResData.setTotalCount(totalCount);
		return auditResData;

	}
	
	public AuditResData getCaseAuditForUser(GetDocAuditReqData getDocAuditReqData) throws WorkbenchException {
		List<AuditDbData> auditDbDataList = new ArrayList<AuditDbData>();
		AuditResData auditResData = new AuditResData();
		List<AuditDbData> auditDataList = auditDataAccess.getAudit(getDocAuditReqData.getDocId(), 0, "",
				getDocAuditReqData.getPageNumber());
		
		PaginationResData paginationResData = new PaginationResData();
		paginationResData.setPageSize(pageSize);
		
		if (ListUtility.hasValue(auditDataList)) {
			paginationResData.setCurrentPageNumber(getDocAuditReqData.getPageNumber());
			for (int i = 0; i < auditDataList.size(); i++) {
				AuditDbData auditData = auditDataList.get(i);
				// Set record serial number based on page number and page size
				long auditId = pageSize * (paginationResData.getCurrentPageNumber() - 1) + i+1;
				auditData.setAuditId(auditId);
				if (auditData.getUserType() > 1) {
					auditData.setAuditLoginId(WorkbenchConstants.USER_TYPE_SERVICE);
				}
				auditData.setCreateDtm(DateUtility.toString(auditData.getAuditEventDtm(),
						WorkbenchConstants.API_TIMESTAMP_FORMAT_12HR));
				auditDbDataList.add(auditData);
			}
		}
		auditResData.setAuditDataList(auditDataList);
		auditResData.setPaginationData(paginationResData);
		return auditResData;
	}
}
