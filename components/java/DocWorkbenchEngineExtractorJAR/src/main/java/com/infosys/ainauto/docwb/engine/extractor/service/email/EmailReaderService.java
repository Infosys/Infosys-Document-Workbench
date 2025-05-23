/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.extractor.service.email;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.datainout.DataInOutApi;
import com.infosys.ainauto.datainout.api.IDataInputReader;
import com.infosys.ainauto.datainout.config.AbstractDataInOutConfig.ProviderDataSourceConfig;
import com.infosys.ainauto.datainout.config.DataInputConfig;
import com.infosys.ainauto.datainout.model.DataInputRecord;
import com.infosys.ainauto.datainout.spi.IDataInOutProvider;
import com.infosys.ainauto.datasource.config.email.EmailServerDataSourceReaderConfig;
import com.infosys.ainauto.datasource.config.file.FileSystemDataSourceReaderConfig;
import com.infosys.ainauto.datasource.model.email.EmailServerDataSourceFolder;
import com.infosys.ainauto.docwb.engine.extractor.common.EmailDataHelper;
import com.infosys.ainauto.docwb.web.data.EmailData;

@Component
public class EmailReaderService implements IEmailReaderService {
	private static Logger logger = LoggerFactory.getLogger(EmailReaderService.class);

	private IDataInputReader dataInputReader;

	@Autowired
	Environment environment;

	private static final String PROP_NAME_DATA_INPUT_OUTPUT_BASIC_PROVIDER = "dio.reader.basic.provider";
	private static final String PROP_NAME_DATA_SOURCE_EMAIL_SERVER_BASIC_READER_CLIENT_CONNECT = "ds.email-server.basic.reader.client.connect";
	private static final String PROP_NAME_DATA_SOURCE_EMAIL_SERVER_BASIC_READER_PROVIDER = "ds.email-server.basic.reader.provider";
	private static final String PROP_NAME_DATA_SOURCE_EMAIL_SERVER_BASIC_READER_ONCONNERROR_IGNORE = "ds.email-server.basic.reader.on-connection-error.ignore";
	private static final String PROP_NAME_DATA_SOURCE_EMAIL_SERVER_BASIC_READER_STORE_PROTOCOL = "ds.email-server.basic.reader.store.protocol";
	private static final String PROP_NAME_DATA_SOURCE_EMAIL_SERVER_BASIC_READER_HOSTNAME = "ds.email-server.basic.reader.hostname";
	private static final String PROP_NAME_DATA_SOURCE_EMAIL_SERVER_BASIC_READER_PORT = "ds.email-server.basic.reader.port";
	private static final String PROP_NAME_DATA_SOURCE_EMAIL_SERVER_BASIC_READER_DEFAULT_FOLDER = "ds.email-server.basic.reader.default.folder";
	private static final String PROP_NAME_DATA_SOURCE_EMAIL_SERVER_BASIC_READER_LISTEN_FOLDER = "ds.email-server.basic.reader.listen.folder";
	private static final String PROP_NAME_DATA_SOURCE_EMAIL_SERVER_BASIC_READER_USERNAME = "ds.email-server.basic.reader.username";
	private static final String PROP_NAME_DATA_SOURCE_EMAIL_SERVER_BASIC_READER_PASSWRD = "ds.email-server.basic.reader.password";
	private static final String PROP_NAME_DATA_SOURCE_EMAIL_SERVER_BASIC_READER_IMAP_SSL_ENABLED = "ds.email-server.basic.reader.imap.ssl.enable";

	private static final String PROP_NAME_DATA_SOURCE_FILE_SYSTEM_READER_CLIENT_CONNECT = "ds.file-system.reader.client.connect";
	private static final String PROP_NAME_DATA_SOURCE_FILE_SYSTEM_READER_PROVIDER = "ds.file-system.reader.provider";
	private static final String PROP_NAME_DATA_SOURCE_FILE_SYSTEM_READER_ONCONNERROR_IGNORE = "ds.file-system.reader.on-connection-error.ignore";
	private static final String PROP_NAME_DATA_SOURCE_FILE_SYSTEM_READER_DIR_SOURCE = "ds.file-system.reader.dir.source";
	private static final String PROP_NAME_DATA_SOURCE_FILE_SYSTEM_READER_DIR_ARCHIVAL = "ds.file-system.reader.dir.archival";
	private static final String PROP_NAME_TEMP_FOLDER = "dio.reader.temp.path";

	private static final String PROP_NAME_DATA_SOURCE_EMAIL_SERVER_EXCHANGE_READER_CLIENT_CONNECT = "ds.email-server.exchange.reader.client.connect";
	private static final String PROP_NAME_DATA_SOURCE_EMAIL_SERVER_EXCHANGE_READER_PROVIDER = "ds.email-server.exchange.reader.provider";
	private static final String PROP_NAME_DATA_SOURCE_EMAIL_SERVER_EXCHANGE_READER_ONCONNERROR_IGNORE = "ds.email-server.exchange.reader.on-connection-error.ignore";
	private static final String PROP_NAME_DATA_SOURCE_EMAIL_SERVER_EXCHANGE_READER_DOMAIN = "ds.email-server.exchange.reader.domain";
	private static final String PROP_NAME_DATA_SOURCE_EMAIL_SERVER_EXCHANGE_READER_MAILBOX_USERNAME = "ds.email-server.exchange.reader.username";
	private static final String PROP_NAME_DATA_SOURCE_EMAIL_SERVER_EXCHANGE_READER_PASSWRD = "ds.email-server.exchange.reader.password";
	private static final String PROP_NAME_DATA_SOURCE_EMAIL_SERVER_EXCHANGE_READER_SERVICE_URI = "ds.email-server.exchange.reader.service.uri";
	private static final String PROP_NAME_DATA_SOURCE_EMAIL_SERVER_EXCHANGE_READER_LISTEN_FOLDER = "ds.email-server.exchange.reader.listen.folder";

	private static final int NO_OF_TRIES = 2;

	@PostConstruct
	private void init() {

		String dataInputBasicProvider = environment.getProperty(PROP_NAME_DATA_INPUT_OUTPUT_BASIC_PROVIDER);
		if (StringUtility.hasTrimmedValue(dataInputBasicProvider)) {
			DataInputConfig dataInputConfig = new DataInputConfig();
			dataInputConfig.setAttachmentDownloadFolder(environment.getProperty(PROP_NAME_TEMP_FOLDER));

			// Source 1 - Email Server
			boolean isEmailReaderEnabled = Boolean
					.valueOf(environment.getProperty(PROP_NAME_DATA_SOURCE_EMAIL_SERVER_BASIC_READER_CLIENT_CONNECT));

			if (isEmailReaderEnabled) {
				EmailServerDataSourceReaderConfig emailServerDataSourceReaderConfig = new EmailServerDataSourceReaderConfig();
				String onConnectionErrorIgnore = environment
						.getProperty(PROP_NAME_DATA_SOURCE_EMAIL_SERVER_BASIC_READER_ONCONNERROR_IGNORE, "false");
				emailServerDataSourceReaderConfig.setOnConnectionErrorIgnore(Boolean.valueOf(onConnectionErrorIgnore));

				emailServerDataSourceReaderConfig.setStoreProtocol(
						environment.getProperty(PROP_NAME_DATA_SOURCE_EMAIL_SERVER_BASIC_READER_STORE_PROTOCOL));
				emailServerDataSourceReaderConfig
						.setHostName(environment.getProperty(PROP_NAME_DATA_SOURCE_EMAIL_SERVER_BASIC_READER_HOSTNAME));
				emailServerDataSourceReaderConfig.setPortNumber(
						Integer.valueOf(environment.getProperty(PROP_NAME_DATA_SOURCE_EMAIL_SERVER_BASIC_READER_PORT)));
				emailServerDataSourceReaderConfig.setMailboxDefaultFolder(
						environment.getProperty(PROP_NAME_DATA_SOURCE_EMAIL_SERVER_BASIC_READER_DEFAULT_FOLDER));
				emailServerDataSourceReaderConfig.setMailboxUserName(
						environment.getProperty(PROP_NAME_DATA_SOURCE_EMAIL_SERVER_BASIC_READER_USERNAME));
				emailServerDataSourceReaderConfig.setMailboxPassword(
						environment.getProperty(PROP_NAME_DATA_SOURCE_EMAIL_SERVER_BASIC_READER_PASSWRD));
				emailServerDataSourceReaderConfig.setSslEnabled(
						environment.getProperty(PROP_NAME_DATA_SOURCE_EMAIL_SERVER_BASIC_READER_IMAP_SSL_ENABLED));

				ProviderDataSourceConfig providerDataSourceConfig = new ProviderDataSourceConfig(
						environment.getProperty(PROP_NAME_DATA_SOURCE_EMAIL_SERVER_BASIC_READER_PROVIDER),
						emailServerDataSourceReaderConfig);
				dataInputConfig.addToProviderDataSourceConfigList(providerDataSourceConfig);
			}

			// Source 2 - File System
			boolean isFileReaderEnabled = Boolean
					.valueOf(environment.getProperty(PROP_NAME_DATA_SOURCE_FILE_SYSTEM_READER_CLIENT_CONNECT));
			if (isFileReaderEnabled) {
				FileSystemDataSourceReaderConfig fileSystemDataSourceReaderConfig = new FileSystemDataSourceReaderConfig();

				String onConnectionErrorIgnore = environment
						.getProperty(PROP_NAME_DATA_SOURCE_FILE_SYSTEM_READER_ONCONNERROR_IGNORE, "false");
				fileSystemDataSourceReaderConfig.setOnConnectionErrorIgnore(Boolean.valueOf(onConnectionErrorIgnore));

				fileSystemDataSourceReaderConfig
						.setFileSourceDir(environment.getProperty(PROP_NAME_DATA_SOURCE_FILE_SYSTEM_READER_DIR_SOURCE));
				fileSystemDataSourceReaderConfig.setFileArchivalDir(
						environment.getProperty(PROP_NAME_DATA_SOURCE_FILE_SYSTEM_READER_DIR_ARCHIVAL));
				fileSystemDataSourceReaderConfig.setFileTempDir(environment.getProperty(PROP_NAME_TEMP_FOLDER));
				ProviderDataSourceConfig providerDataSourceConfig = new ProviderDataSourceConfig(
						environment.getProperty(PROP_NAME_DATA_SOURCE_FILE_SYSTEM_READER_PROVIDER),
						fileSystemDataSourceReaderConfig);
				dataInputConfig.addToProviderDataSourceConfigList(providerDataSourceConfig);
			}

			// Source 3 - Exchange Onprem
			boolean isExchangeOnpremReaderEnabled = Boolean.valueOf(
					environment.getProperty(PROP_NAME_DATA_SOURCE_EMAIL_SERVER_EXCHANGE_READER_CLIENT_CONNECT));
			if (isExchangeOnpremReaderEnabled) {
				EmailServerDataSourceReaderConfig emailServerDataSourceReaderConfig = new EmailServerDataSourceReaderConfig();

				String onConnectionErrorIgnore = environment
						.getProperty(PROP_NAME_DATA_SOURCE_EMAIL_SERVER_EXCHANGE_READER_ONCONNERROR_IGNORE, "false");
				emailServerDataSourceReaderConfig.setOnConnectionErrorIgnore(Boolean.valueOf(onConnectionErrorIgnore));

				emailServerDataSourceReaderConfig.setMailboxDomain(
						environment.getProperty(PROP_NAME_DATA_SOURCE_EMAIL_SERVER_EXCHANGE_READER_DOMAIN));
				emailServerDataSourceReaderConfig.setMailboxPassword(
						environment.getProperty(PROP_NAME_DATA_SOURCE_EMAIL_SERVER_EXCHANGE_READER_PASSWRD));
				emailServerDataSourceReaderConfig.setMailboxUserName(
						environment.getProperty(PROP_NAME_DATA_SOURCE_EMAIL_SERVER_EXCHANGE_READER_MAILBOX_USERNAME));
				emailServerDataSourceReaderConfig.setServiceUri(
						environment.getProperty(PROP_NAME_DATA_SOURCE_EMAIL_SERVER_EXCHANGE_READER_SERVICE_URI));

				ProviderDataSourceConfig providerDataSourceConfig = new ProviderDataSourceConfig(
						environment.getProperty(PROP_NAME_DATA_SOURCE_EMAIL_SERVER_EXCHANGE_READER_PROVIDER),
						emailServerDataSourceReaderConfig);
				dataInputConfig.addToProviderDataSourceConfigList(providerDataSourceConfig);
			}

			IDataInOutProvider dataInOutProvider = DataInOutApi.getProviderByClassName(dataInputBasicProvider);

			dataInputReader = dataInOutProvider.getDataInputReader(dataInputConfig);

			// Make connection on initialization
			connectToEmailServer();

		} else {
			logger.warn(
					"WARNING!! EmailReaderService could not be instantiated. Reason: missing properties in application.properties file");
		}
	}

	@Override
	public void openMailboxFolder() throws Exception {

		boolean isExchangeOnpremReaderEnabled = Boolean
				.valueOf(environment.getProperty(PROP_NAME_DATA_SOURCE_EMAIL_SERVER_EXCHANGE_READER_CLIENT_CONNECT));
		boolean isEmailReaderEnabled = Boolean
				.valueOf(environment.getProperty(PROP_NAME_DATA_SOURCE_EMAIL_SERVER_BASIC_READER_CLIENT_CONNECT));

		String folderName = null;
		if (isExchangeOnpremReaderEnabled && isEmailReaderEnabled) {
			throw new Exception("Both Email Server is enabled please make one connection as disabled.");
		} else if (isExchangeOnpremReaderEnabled) {
			folderName = environment.getProperty(PROP_NAME_DATA_SOURCE_EMAIL_SERVER_EXCHANGE_READER_LISTEN_FOLDER)
					.trim();
		} else if (isEmailReaderEnabled) {
			folderName = environment.getProperty(PROP_NAME_DATA_SOURCE_EMAIL_SERVER_BASIC_READER_LISTEN_FOLDER).trim();
		}

		EmailServerDataSourceFolder emailServerDataSourceFolder = new EmailServerDataSourceFolder(folderName);

		// Make two attempts to connect to server if not connected
		for (int i = 1; i <= NO_OF_TRIES; i++) {
			try {
				dataInputReader.openFolder(emailServerDataSourceFolder);
				break; // Break from loop if call was successful
			} catch (Exception e) {
				logger.error("Error occured while opening mailbox folder", e);
				// Assume error is due to connection so set status as false
				connectToEmailServer();
			}
		}
	}

	@Override
	public void closeMailboxFolder() throws Exception {
		dataInputReader.closeFolder();
	}

	public List<EmailData> readEmails() {
		List<DataInputRecord> dataInputRecordList = new ArrayList<>();

		// Make two attempts to connect to server if not connected
		for (int i = 1; i <= NO_OF_TRIES; i++) {
			try {
				dataInputRecordList = dataInputReader.getNewItems();
				break; // Break from loop if call was successful
			} catch (Exception e) {
				logger.error("Error occured while reading new emails ", e);
				// Assume error is due to connection so set status as false
				connectToEmailServer();
			}
		}

		EmailData emailData;
		List<EmailData> emailDataList = new ArrayList<EmailData>();
		for (DataInputRecord dataInputRecord : dataInputRecordList) {
			emailData = EmailDataHelper.convertDataInputRecordToEmailData(dataInputRecord);
			if (emailData != null) {
				emailDataList.add(emailData);
			}
		}
		return emailDataList;
	}

	public void updateEmailAsRead(String dataInputRecordId) {
		try {
			dataInputReader.updateItemAsRead(dataInputRecordId);
		} catch (Exception e) {
			logger.error("error occured while marking email as read:- ", e);
		}
	}

	public void deleteSavedAttachements(String dataInputRecordId) {
		try {
			dataInputReader.deleteSavedAttachments(dataInputRecordId);
		} catch (Exception e) {
			logger.error("error occured while marking email as read:- ", e);
		}
	}

	public void connectToEmailServer() {
		try {
			logger.info("Calling method to connect to mail server");
			dataInputReader.connect();
		} catch (Exception e) {
			logger.error("Failed to connect to mailserver", e);
		}

	}

	public void disconnectFromEmailServer() {
		try {
			logger.info("Calling method to disconnect from mail server");
			dataInputReader.disconnect();
		} catch (Exception e) {
			logger.error("Failed To disconnect from mailserver", e);
		}

	}

}
