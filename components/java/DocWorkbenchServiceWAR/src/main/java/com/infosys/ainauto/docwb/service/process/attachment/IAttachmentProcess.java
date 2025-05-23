/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.process.attachment;

import java.util.List;

import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.model.api.AttachmentResData;
import com.infosys.ainauto.docwb.service.model.api.attachment.AttaAttaRelReqData;
import com.infosys.ainauto.docwb.service.model.api.attachment.AttachmentReqData;
import com.infosys.ainauto.docwb.service.model.api.attachment.InsertAttaAttaRelResData;
import com.infosys.ainauto.docwb.service.model.api.attachment.InsertAttachmentResData;
import com.infosys.ainauto.docwb.service.model.db.AttachmentDbData;

public interface IAttachmentProcess {

	public List<InsertAttachmentResData> addAttachmentToDoc(AttachmentReqData attachmentRequestData, long docId) throws WorkbenchException;

	public int addAttachmentToEmail(AttachmentReqData attachmentRequestData, long outboundEmailId)
			throws WorkbenchException;

	public List<AttachmentResData> getDocAttachmentList(long docId) throws WorkbenchException;

	public AttachmentDbData getDocAttachmentFile(long docId, long attachmentId) throws WorkbenchException;

	public List<AttachmentResData> getAttachmentListEmail(long emailOutboundId) throws WorkbenchException;
	
	public AttachmentDbData getOutboudAttachmentFile(long emailOutboundId, long attachmentId) throws WorkbenchException;
	
	public List<InsertAttaAttaRelResData> addAttaAttaRel(AttaAttaRelReqData attachmentRelReqData)throws WorkbenchException;
	
	public long countAttaAttaDocExist(AttaAttaRelReqData attachmentRelReqData)throws WorkbenchException;
	
	public long countAttaAttaRelRecord(AttaAttaRelReqData attachmentRelReqData)throws WorkbenchException;
	
}
