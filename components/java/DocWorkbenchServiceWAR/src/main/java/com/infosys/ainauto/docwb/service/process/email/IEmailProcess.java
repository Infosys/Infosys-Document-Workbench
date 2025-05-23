/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.process.email;

import java.util.List;

import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.model.api.attachment.AttachmentReqData;
import com.infosys.ainauto.docwb.service.model.api.EmailResData;
import com.infosys.ainauto.docwb.service.model.api.PaginationResData;
import com.infosys.ainauto.docwb.service.model.api.email.InsertEmailReqData;
import com.infosys.ainauto.docwb.service.model.api.email.InsertUpdateDraftReqData;
import com.infosys.ainauto.docwb.service.model.api.email.UpdateEmailStatusReqData;
import com.infosys.ainauto.docwb.service.model.db.EmailDbData;
import com.infosys.ainauto.docwb.service.model.db.EntityDbData;

public interface IEmailProcess {

	public EntityDbData sendEmail(InsertEmailReqData requestData, AttachmentReqData attachmentReqData)
			throws WorkbenchException;

	public List<EmailResData> getOutboundEmailList(Long docId) throws WorkbenchException;

	public EmailDbData getDraftEmailWithEncoding(Long docId, boolean isAppendCaseNumberInSubject, 
			String appendString) throws WorkbenchException;

	public EntityDbData saveDraftEmail(InsertUpdateDraftReqData insertUpdateDraftReqData) throws WorkbenchException;

	public long deleteEmail(long emailId) throws WorkbenchException;

	public PaginationResData getOutboundEmailCountByTaskStatus(int taskStatusCde, int pageNumber) throws WorkbenchException;

	public List<EmailResData> getOutboundEmailListByTaskStatus(int taskStatusCde, int pageNumber)
			throws WorkbenchException;

	public EntityDbData updateEmailTaskStatus(UpdateEmailStatusReqData updateEmailStatusReqData) throws WorkbenchException;
}
