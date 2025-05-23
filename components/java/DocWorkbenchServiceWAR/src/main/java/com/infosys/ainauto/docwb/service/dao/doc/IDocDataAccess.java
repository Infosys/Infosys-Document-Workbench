/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.dao.doc;

import java.util.List;

import org.springframework.stereotype.Component;

import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.model.db.DocAppUserDbData;
import com.infosys.ainauto.docwb.service.model.db.DocAttrWrapperDbData;
import com.infosys.ainauto.docwb.service.model.db.DocDetailDbData;
import com.infosys.ainauto.docwb.service.model.db.DocUserDbData;
import com.infosys.ainauto.docwb.service.model.db.DocumentDbData;
import com.infosys.ainauto.docwb.service.model.db.EntityDbData;

@Component
public interface IDocDataAccess {

	public long addDocument(DocumentDbData documentDbData) throws WorkbenchException;

	public long deleteDocument(long docId) throws WorkbenchException;
	
	public long getTotalDocCount(DocumentDbData documentDbDataIn) throws WorkbenchException;

	public List<DocAttrWrapperDbData> getDocumentList(DocumentDbData documentDbDataIn) throws WorkbenchException;
	
	public DocumentDbData getDocumentDetails(long docId) throws WorkbenchException;

	public long insertUserDocRel(long appUserId, long docId, long docRoleTypeCde) throws WorkbenchException;

	public List<DocAppUserDbData> getUserDocDetails(long docId, long docRoleTypeCde) throws WorkbenchException;

	public long updateDocAppUser(DocAppUserDbData docUser) throws WorkbenchException;

	public List<EntityDbData> updateDocActionStatus(DocumentDbData documentDbData) throws WorkbenchException;

	public int insertDocEventType(DocumentDbData documentDbData) throws WorkbenchException;
	
	public List<DocAttrWrapperDbData> getSearchCriteriaDetails(String docIdList, DocumentDbData documentDbDataIn) throws WorkbenchException;
	
	public List<DocDetailDbData> getDocumentListByAttribute(DocumentDbData documentDbDataIn, String searchCriteria, String queueNameCdes) throws WorkbenchException;
	
	public long getTotalCountOnSearchCriteria(String docIdListStr,DocumentDbData documentDbDataIn) throws WorkbenchException;

	public List<DocUserDbData> getDocUserRel(long docId) throws WorkbenchException;
}
