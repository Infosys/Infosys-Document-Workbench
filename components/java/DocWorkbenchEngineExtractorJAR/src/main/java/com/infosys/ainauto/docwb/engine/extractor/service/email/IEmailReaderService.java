/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.extractor.service.email;

import java.util.List;

import com.infosys.ainauto.docwb.web.data.EmailData;

public interface IEmailReaderService {

	public void openMailboxFolder() throws Exception;

	public void closeMailboxFolder() throws Exception;

	public List<EmailData> readEmails();

	public void disconnectFromEmailServer();

	public void connectToEmailServer();

	public void updateEmailAsRead(String dataInputRecordId);

	public void deleteSavedAttachements(String dataInputRecordId);

}
