/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.datasource.impl.emailserver.basic.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.infosys.ainauto.datasource.DataSourceApi;
import com.infosys.ainauto.datasource.api.IDataSourceReader;
import com.infosys.ainauto.datasource.api.IDataSourceWriter;
import com.infosys.ainauto.datasource.config.DataSourceConfig;
import com.infosys.ainauto.datasource.config.email.EmailServerDataSourceReaderConfig;
import com.infosys.ainauto.datasource.config.email.EmailServerDataSourceWriterConfig;
import com.infosys.ainauto.datasource.impl.emailserver.basic.api.EmailServerDataSourceReader;
import com.infosys.ainauto.datasource.impl.emailserver.basic.api.EmailServerDataSourceWriter;
import com.infosys.ainauto.datasource.model.email.EmailServerDataSourceRecord;
import com.infosys.ainauto.datasource.spi.IDataSourceProvider;
import com.infosys.ainauto.testutils.FileTestUtility;

public class TestHelper {

	private final static Logger logger = LoggerFactory.getLogger(TestHelper.class);
	private static final String PROVIDER_CLASS_NAME_DATA_SOURCE_EMAIL_SERVER = "com.infosys.ainauto.datasource.impl.emailserver.basic.api.EmailServerDataSourceProvider";

	public static EmailServerDataSourceReaderConfig getEmailServerDataSourceReaderConfig(String propertiesFileName)
			throws Exception {

		Properties properties = FileTestUtility.readPropertiesFileFromClassPath(propertiesFileName);

		EmailServerDataSourceReaderConfig emailServerDataSourceReaderConfig = new EmailServerDataSourceReaderConfig();
		emailServerDataSourceReaderConfig.setHostName(properties.getProperty("ds.email-server.basic.reader.hostname"));
		emailServerDataSourceReaderConfig
				.setPortNumber(Integer.valueOf(properties.getProperty("ds.email-server.basic.reader.port")));
		emailServerDataSourceReaderConfig
				.setMailboxDefaultFolder(properties.getProperty("ds.email-server.basic.reader.default.folder"));
		emailServerDataSourceReaderConfig
				.setMailboxUserName(properties.getProperty("ds.email-server.basic.reader.username"));
		emailServerDataSourceReaderConfig
				.setMailboxPassword(properties.getProperty("ds.email-server.basic.reader.password"));
		emailServerDataSourceReaderConfig
				.setSslEnabled(properties.getProperty("ds.email-server.basic.reader.ssl.enable"));

		return emailServerDataSourceReaderConfig;
	}

	public static EmailServerDataSourceWriterConfig getEmailServerDataSourceWriterConfig(String propertiesFileName)
			throws Exception {

		Properties properties = FileTestUtility.readPropertiesFileFromClassPath(propertiesFileName);

		EmailServerDataSourceWriterConfig emailServerDataSourceWriterConfig = new EmailServerDataSourceWriterConfig();
		emailServerDataSourceWriterConfig.setHostName(properties.getProperty("ds.email-server.basic.writer.hostname"));
		emailServerDataSourceWriterConfig.setPort(properties.getProperty("ds.email-server.basic.writer.port"));
		emailServerDataSourceWriterConfig
				.setSenderName(properties.getProperty("ds.email-server.basic.writer.from.name"));
		emailServerDataSourceWriterConfig
				.setSenderEmailId(properties.getProperty("ds.email-server.basic.writer.from.id"));
		emailServerDataSourceWriterConfig
				.setSenderPassword(properties.getProperty("ds.email-server.basic.writer.from.password"));
		emailServerDataSourceWriterConfig
				.setAuthenticateUser(properties.getProperty("ds.email-server.basic.writer.auth"));

		return emailServerDataSourceWriterConfig;
	}

	public static IDataSourceReader getDataSourceReader(DataSourceConfig dataSourceReaderConfig) {
		IDataSourceProvider dataSourceProvider = DataSourceApi
				.getProviderByClassName(PROVIDER_CLASS_NAME_DATA_SOURCE_EMAIL_SERVER);
		IDataSourceReader dataSourceReader = (EmailServerDataSourceReader) dataSourceProvider
				.getDataSourceReader(PROVIDER_CLASS_NAME_DATA_SOURCE_EMAIL_SERVER, dataSourceReaderConfig);
		return dataSourceReader;
	}

	public static IDataSourceWriter getDataSourceWriter(DataSourceConfig dataSourceWriterConfig) {
		IDataSourceProvider dataSourceProvider = DataSourceApi
				.getProviderByClassName(PROVIDER_CLASS_NAME_DATA_SOURCE_EMAIL_SERVER);
		IDataSourceWriter dataSourceWriter = (EmailServerDataSourceWriter) dataSourceProvider
				.getDataSourceWriter(PROVIDER_CLASS_NAME_DATA_SOURCE_EMAIL_SERVER, dataSourceWriterConfig);
		return dataSourceWriter;
	}

	public static class EmailSenderHelper {

		public static final String MAIL_FIELD_SUBJECT = "subject";
		public static final String MAIL_FIELD_BODY = "body";
		public static final String MAIL_FIELD_TO_ID = "mailToId";
		public static final String MAIL_FIELD_TO_NAME = "mailToName";

		public static void sendEmails(List<Map<String, String>> mailFieldsMapList) throws Exception {
			try {
				EmailServerDataSourceWriterConfig emailServerDataSourceWriterConfig = TestHelper
						.getEmailServerDataSourceWriterConfig("emailServerDataSourceTest.properties");

				Map<String, String> paramsMap = new HashMap<>();
				paramsMap.put("isSaveMessageCopy", "True");

				IDataSourceWriter dataSourceWriter = TestHelper.getDataSourceWriter(emailServerDataSourceWriterConfig);
				dataSourceWriter.connect();

				for (Map<String, String> mailFieldsMap : mailFieldsMapList) {
					EmailServerDataSourceRecord emailServerDataSourceRecord = (EmailServerDataSourceRecord) dataSourceWriter
							.generateNewItem();

					MimeMessage mimeMessage = (MimeMessage) emailServerDataSourceRecord.getMimeMessage();

					mimeMessage.setSubject(mailFieldsMap.getOrDefault(MAIL_FIELD_SUBJECT, ""), "UTF-8");
					mimeMessage.setText(mailFieldsMap.getOrDefault(MAIL_FIELD_BODY, "UTF-8"));
					mimeMessage.setRecipients(Message.RecipientType.TO, convertToInternetAddress(
							mailFieldsMap.get(MAIL_FIELD_TO_NAME), mailFieldsMap.get(MAIL_FIELD_TO_ID)));

					dataSourceWriter.writeItem(emailServerDataSourceRecord, paramsMap);
				}

				dataSourceWriter.disconnect();
			} catch (Exception e) {
				logger.error("Error while sending emails", e);
			}
		}

		public static Address[] convertToInternetAddress(String name, String emailAddress) throws Exception {
			Address internetAddress = new InternetAddress(emailAddress, name);
			List<Address> addressList = new ArrayList<>();
			addressList.add(internetAddress);
			Address[] addressArray = addressList.toArray(new Address[addressList.size()]);
			return addressArray;
		}
	}

}
