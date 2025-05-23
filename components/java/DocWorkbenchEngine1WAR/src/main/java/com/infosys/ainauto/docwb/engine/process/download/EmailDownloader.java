/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.process.download;

import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.docwb.engine.core.model.SummaryData;
import com.infosys.ainauto.docwb.engine.core.stereotype.DocumentDownloader;
import com.infosys.ainauto.docwb.engine.core.template.download.DocumentDownloaderBase;
import com.infosys.ainauto.docwb.engine.extractor.service.email.IEmailReaderService;
import com.infosys.ainauto.docwb.web.data.DocumentData;
import com.infosys.ainauto.docwb.web.data.EmailData;
import com.infosys.ainauto.docwb.web.type.EnumDocType;
import com.infosys.ainauto.docwb.web.type.EnumLockStatus;
import com.infosys.ainauto.docwb.web.type.EnumTaskStatus;

@Component
@DocumentDownloader(title = "Download Emails", propertiesFile = "customization.properties")
public class EmailDownloader extends DocumentDownloaderBase<EmailData> {

	private static Logger logger = LoggerFactory.getLogger(EmailDownloader.class);

	@Autowired
	private IEmailReaderService emailReaderService;

	@Override
	protected boolean initialize(Properties properties) throws Exception {
		return true;
	}

	@Override
	protected List<EmailData> downloadData() throws Exception {
		emailReaderService.openMailboxFolder();
		List<EmailData> emailDataList = emailReaderService.readEmails();
		return emailDataList;
	}

	@Override
	protected DocumentData createDocument(EmailData emailData) throws Exception {
		DocumentData documentData = new DocumentData();
		documentData.setDocType(EnumDocType.EMAIL).setDocLocation(emailData.getDataInputRecordId())
				.setLockStatus(EnumLockStatus.UNLOCKED)
				.setTaskStatus(EnumTaskStatus.UNDEFINED);

		if (emailData.getAttachmentDataList() != null && emailData.getAttachmentDataList().size() > 0) {
			documentData.setAttachmentDataList(emailData.getAttachmentDataList());
		}

		if (emailData.getInlineImageAttachmentDataList() != null
				&& emailData.getInlineImageAttachmentDataList().size() > 0) {
			documentData.setInlineAttachmentDataList(emailData.getInlineImageAttachmentDataList());
		}

		return documentData;
	}


	@Override
	protected void uploadDocumentResult(Exception ex, EmailData emailData, DocumentData documentData) throws Exception {
		if (ex != null) {
			logger.error("Error occurred in upload document", ex);
		} else {
			logger.info("Document uploaded with docId=" + documentData.getDocId());
			emailReaderService.updateEmailAsRead(emailData.getDataInputRecordId());
			emailReaderService.deleteSavedAttachements(emailData.getDataInputRecordId());
		}
	}

	@Override
	protected void terminate(SummaryData summaryData) throws Exception {
		emailReaderService.closeMailboxFolder();
		logger.info(summaryData.toString());
	}

}
