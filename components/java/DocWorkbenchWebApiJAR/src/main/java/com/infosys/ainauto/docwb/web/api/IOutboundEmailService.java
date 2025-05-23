/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.web.api;

import java.util.List;

import com.infosys.ainauto.docwb.web.data.EmailData;
import com.infosys.ainauto.docwb.web.exception.DocwbWebException;
import com.infosys.ainauto.docwb.web.type.EnumTaskStatus;

public interface IOutboundEmailService {

	public List<EmailData> getOutboundEmailList(EnumTaskStatus taskStatusCde, String attachmentSaveFolder)
			throws DocwbWebException;

	public boolean addOutboundEmail(EmailData emailDatad);

	public void updateOutboundEmailStatus(long emailOutboundId, EnumTaskStatus taskStatusCde);

	public EmailData getEmailDraft(long docId) throws DocwbWebException;

}
