/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.core.service.email;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.datainout.DataInOutApi;
import com.infosys.ainauto.datainout.api.IDataOutputWriter;
import com.infosys.ainauto.datainout.config.AbstractDataInOutConfig.ProviderDataSourceConfig;
import com.infosys.ainauto.datainout.config.DataOutputConfig;
import com.infosys.ainauto.datainout.model.DataOutputRecord;
import com.infosys.ainauto.datainout.model.email.AttachmentRecord;
import com.infosys.ainauto.datainout.model.email.EmailAddress;
import com.infosys.ainauto.datainout.model.email.EmailRecord;
import com.infosys.ainauto.datainout.spi.IDataInOutProvider;
import com.infosys.ainauto.datasource.config.email.EmailServerDataSourceWriterConfig;
import com.infosys.ainauto.docwb.web.data.AttachmentData;
import com.infosys.ainauto.docwb.web.data.EmailAddressData;
import com.infosys.ainauto.docwb.web.data.EmailData;

@Component
public class EmailSenderCoreService implements IEmailSenderCoreService {
	private static Logger logger = LoggerFactory.getLogger(EmailSenderCoreService.class);

	private static IDataOutputWriter dataOutputWriter;

	@Autowired
	Environment environment;

	private static final String PROP_NAME_DATA_IN_OUT_PROVIDER = "dio.writer.basic.provider";
	private static final String PROP_NAME_DATA_SOURCE_EMAIL_SERVER_WRITER_PROVDER = "ds.email-server.writer.provider";
	private static final String PROP_NAME_EMAIL_SENDER_FEATURE_ENABLED = "email.sender.enabled";
	private static final String PROP_NAME_DATA_SOURCE_EMAIL_SERVER_WRITER_HOSTNAME = "ds.email-server.writer.hostname";
	private static final String PROP_NAME_DATA_SOURCE_EMAIL_SERVER_WRITER_PORT = "ds.email-server.writer.port";
	private static final String PROP_NAME_DATA_SOURCE_EMAIL_SERVER_WRITER_FROM_ID = "ds.email-server.writer.from.id";
	private static final String PROP_NAME_DATA_SOURCE_EMAIL_SERVER_WRITER_FROM_NAME = "ds.email-server.writer.from.name";
	private static final String PROP_NAME_DATA_SOURCE_EMAIL_SERVER_WRITER_AUTH = "ds.email-server.writer.auth";
	private static final String PROP_NAME_DATA_SOURCE_EMAIL_SERVER_WRITER_FROM_PASSWRD = "ds.email-server.writer.from.password";
	private static final String PROP_NAME_DATA_SOURCE_EMAIL_SERVER_WRITER_SAVE_FOLDER = "ds.email-server.writer.save.folder";

	private String PROP_IS_SAVE_MESSAGE_COPY = "isSaveMessageCopy";
	private String PROP_SAVE_MESSAGE_COPY_TO_FOLDER = "saveMessageCopyToFolder";

	@PostConstruct
	private void init() {
		boolean isSendEmailEnabled = Boolean.valueOf(environment.getProperty(PROP_NAME_EMAIL_SENDER_FEATURE_ENABLED));

		if (isSendEmailEnabled) {

			DataOutputConfig dataOutputConfig = new DataOutputConfig();

			// Source 1 - Email Server
			{
				EmailServerDataSourceWriterConfig emailServerDataSourceWriterConfig = new EmailServerDataSourceWriterConfig();
				emailServerDataSourceWriterConfig
						.setHostName(environment.getProperty(PROP_NAME_DATA_SOURCE_EMAIL_SERVER_WRITER_HOSTNAME));
				emailServerDataSourceWriterConfig
						.setSenderEmailId(environment.getProperty(PROP_NAME_DATA_SOURCE_EMAIL_SERVER_WRITER_FROM_ID));
				emailServerDataSourceWriterConfig
						.setSenderName(environment.getProperty(PROP_NAME_DATA_SOURCE_EMAIL_SERVER_WRITER_FROM_NAME));
				emailServerDataSourceWriterConfig
						.setAuthenticateUser(environment.getProperty(PROP_NAME_DATA_SOURCE_EMAIL_SERVER_WRITER_AUTH));
				emailServerDataSourceWriterConfig
						.setPort(environment.getProperty(PROP_NAME_DATA_SOURCE_EMAIL_SERVER_WRITER_PORT));
				emailServerDataSourceWriterConfig.setSenderPassword(
						environment.getProperty(PROP_NAME_DATA_SOURCE_EMAIL_SERVER_WRITER_FROM_PASSWRD));
				ProviderDataSourceConfig providerDataSourceConfig = new ProviderDataSourceConfig(
						environment.getProperty(PROP_NAME_DATA_SOURCE_EMAIL_SERVER_WRITER_PROVDER),
						emailServerDataSourceWriterConfig);
				dataOutputConfig.addToProviderDataSourceConfigList(providerDataSourceConfig);
			}

			IDataInOutProvider dataInOutProvider = DataInOutApi
					.getProviderByClassName(environment.getProperty(PROP_NAME_DATA_IN_OUT_PROVIDER));
			dataOutputWriter = dataInOutProvider.getDataOutputWriter(dataOutputConfig);

			// Make connection on initialization
			connectToEmailServer();
		}

	}

	public void sendEmail(EmailData emailData, boolean isSaveEmail) throws Exception {
		try {
			logger.info("Calling method to send email");
			EmailRecord emailRecord = new EmailRecord();
			emailRecord.setEmailSubject(emailData.getEmailSubject());
			emailRecord.setEmailBodyText(emailData.getEmailBodyText());
			emailRecord.setEmailBodyHtml(emailData.getEmailBodyHtml());

			List<EmailAddress> emailAddressList = new ArrayList<>();
			for (EmailAddressData emailAddressData : emailData.getEmailAddressToList()) {
				EmailAddress emailAddress = new EmailAddress(emailAddressData.getEmailId(),
						emailAddressData.getEmailName());
				emailAddressList.add(emailAddress);
			}
			emailRecord.setEmailAddressToList(emailAddressList);

			if (emailData.getEmailAddressCcList() != null) {
				emailAddressList = new ArrayList<>();
				for (EmailAddressData emailAddressData : emailData.getEmailAddressCcList()) {
					EmailAddress emailAddress = new EmailAddress(emailAddressData.getEmailId(),
							emailAddressData.getEmailName());
					emailAddressList.add(emailAddress);

				}
				emailRecord.setEmailAddressCcList(emailAddressList);
			}

			if (emailData.getEmailAddressBccList() != null) {
				emailAddressList = new ArrayList<>();
				for (EmailAddressData emailAddressData : emailData.getEmailAddressBccList()) {
					EmailAddress emailAddress = new EmailAddress(emailAddressData.getEmailId(),
							emailAddressData.getEmailName());
					emailAddressList.add(emailAddress);
				}
				emailRecord.setEmailAddressBccList(emailAddressList);
			}
			if (emailData.getInlineImageAttachmentDataList() != null) {
				List<AttachmentRecord> inlineAttachmentRecordList = new ArrayList<AttachmentRecord>();
				for (AttachmentData attachmentData : emailData.getInlineImageAttachmentDataList()) {
					AttachmentRecord attachmentRecord = new AttachmentRecord();
					attachmentRecord.setActualFileName(attachmentData.getLogicalName());
					attachmentRecord.setStoredFileFullPath(attachmentData.getPhysicalPath());
					inlineAttachmentRecordList.add(attachmentRecord);
				}
				emailRecord.setInlineAttachmentRecordList(inlineAttachmentRecordList);
			}
			if (emailData.getAttachmentDataList() != null) {
				List<AttachmentRecord> attachmentRecordList = new ArrayList<AttachmentRecord>();
				for (AttachmentData attachmentData : emailData.getAttachmentDataList()) {
					AttachmentRecord attachmentRecord = new AttachmentRecord();
					attachmentRecord.setActualFileName(attachmentData.getLogicalName());
					attachmentRecord.setStoredFileFullPath(attachmentData.getPhysicalPath());
					attachmentRecordList.add(attachmentRecord);
				}
				emailRecord.setAttachmentRecordList(attachmentRecordList);
			}

			DataOutputRecord dataOutputRecord = new DataOutputRecord();
			dataOutputRecord.setEmailRecord(emailRecord);
			Map<String, String> paramsMap = new HashMap<>();
			paramsMap.put(PROP_IS_SAVE_MESSAGE_COPY, String.valueOf(isSaveEmail));
			paramsMap.put(PROP_SAVE_MESSAGE_COPY_TO_FOLDER,
					environment.getProperty(PROP_NAME_DATA_SOURCE_EMAIL_SERVER_WRITER_SAVE_FOLDER));
			dataOutputWriter.writeItem(dataOutputRecord, paramsMap);
		} catch (Exception e) {
			logger.error("Error occured while sending email", e);
			throw e;
		}
	}

	public void connectToEmailServer() {
		try {
			logger.info("Calling method to connect to mail server");
			dataOutputWriter.connect();
		} catch (Exception e) {
			logger.error("Failed to connect to mailserver", e);
		}
	}

	public void disconnectFromEmailServer() {
		try {
			logger.info("Calling method to disconnect from mail server");
			dataOutputWriter.disconnect();
		} catch (Exception e) {
			logger.error("Failed To disconnect from mailserver", e);
		}
	}
}
