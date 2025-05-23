/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.dao.attachment;

import java.util.List;

import org.springframework.stereotype.Component;

import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.model.api.attachment.AttaAttaRelReqData;
import com.infosys.ainauto.docwb.service.model.db.AttachmentDbData;

@Component
public interface IAttachmentDataAccess {

	public long addAttachment(AttachmentDbData attachmentDbData) throws WorkbenchException;

	public List<AttachmentDbData> getDocAttachmentList(long docId) throws WorkbenchException;

	public List<Long> addDocAttachmentRel(List<Long> attachmentIdList, long docId) throws WorkbenchException;

	public List<AttachmentDbData> getAttachmentListEmail(long emailOutboundId) throws WorkbenchException;

	public long addEmailOutboundAttachmentRel(List<Long> attachmentDbDataList, long emailOutboundId)
			throws WorkbenchException;
	public long addAttaAttaRel(AttaAttaRelReqData attachmentRelReqData) throws WorkbenchException;
	
	public long countAttaAttaDocExist(AttaAttaRelReqData attachmentRelReqData) throws WorkbenchException;
	
	public long countAttaAttaRelRecord(AttaAttaRelReqData attachmentRelReqData) throws WorkbenchException;
	

}
