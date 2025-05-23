/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.process.doc;

import java.util.List;

import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.model.api.DocAndAttrResData;
import com.infosys.ainauto.docwb.service.model.api.DocUserResData;
import com.infosys.ainauto.docwb.service.model.api.PaginationApiResponseData;
import com.infosys.ainauto.docwb.service.model.api.PaginationResData;
import com.infosys.ainauto.docwb.service.model.api.document.CloseCaseReqData;
import com.infosys.ainauto.docwb.service.model.api.document.GetDocReqData;
import com.infosys.ainauto.docwb.service.model.api.document.InsertDocEventReqData;
import com.infosys.ainauto.docwb.service.model.api.document.InsertDocReqData;
import com.infosys.ainauto.docwb.service.model.api.document.UpdateDocStatusReqData;
import com.infosys.ainauto.docwb.service.model.db.DocDetailDbData;
import com.infosys.ainauto.docwb.service.model.db.DocumentDbData;
import com.infosys.ainauto.docwb.service.model.db.EntityDbData;

public interface IDocumentProcess {

	public List<EntityDbData> addDocumentAndAttributes(InsertDocReqData insertDocReqData) throws WorkbenchException;

	public EntityDbData deleteDocument(long docId) throws WorkbenchException;

	public PaginationResData getPaginationDetails(GetDocReqData getDocReqData) throws WorkbenchException;

	public List<DocAndAttrResData> getDocumentAndAttributes(GetDocReqData getDocReqData)
			throws WorkbenchException;
	
	public void getDocumentDetails(PaginationApiResponseData<List<DocAndAttrResData>> apiResponseData,
			GetDocReqData getDocReqData) throws WorkbenchException;
	
	public void getDocumentDetailsByAttribute(PaginationApiResponseData<List<DocDetailDbData>> apiResponseData,
			GetDocReqData getDocReqData) throws WorkbenchException;
	
	public DocumentDbData getBasicDocumentDetails(long docId) throws WorkbenchException;
	
	public List<EntityDbData> updateDocActionStatus(UpdateDocStatusReqData documentRequestData) throws WorkbenchException;

	public EntityDbData closeCaseForDocument(int queueNameCde, 
			List<CloseCaseReqData> closeCaseReqDataList) throws WorkbenchException;

	public List<EntityDbData> addUserToDoc(long prevAppUserId, long appUserId, long docId, long docRoleTypeCde) throws WorkbenchException;

	public void insertDocEventType(InsertDocEventReqData docEventRequestData) throws WorkbenchException;

	public List<DocUserResData> getDocUserDetails(long docId) throws WorkbenchException;
}
