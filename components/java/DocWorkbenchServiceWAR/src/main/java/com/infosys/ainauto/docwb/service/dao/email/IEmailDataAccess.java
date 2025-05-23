/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.dao.email;

import java.util.List;

import org.springframework.stereotype.Component;

import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.model.api.email.InsertEmailReqData;
import com.infosys.ainauto.docwb.service.model.db.EmailDbData;
import com.infosys.ainauto.docwb.service.model.db.EntityDbData;

@Component
public interface IEmailDataAccess {

	long insertOutboundEmail(InsertEmailReqData insertEmailReqData) throws WorkbenchException;

	public List<EmailDbData> getOutboundEmailList(Long docId) throws WorkbenchException;

	public EmailDbData getDraftEmail(Long docId) throws WorkbenchException;

	public long insertDraftEmail(EmailDbData emailDbData) throws WorkbenchException;

	public long updateDraftEmail(EmailDbData emailDbData) throws WorkbenchException;

	public long deleteEmail(long emailId) throws WorkbenchException;

	public Long getTotalEmailCount(int taskStatusCde) throws WorkbenchException;

	public List<EmailDbData> getOutboundEmailListByTaskStatus(int taskStatusCde, int pageNumber) throws WorkbenchException;

	public EntityDbData updateEmailTaskStatus(long emailOutboundId, long taskStatusCde) throws WorkbenchException;

}
