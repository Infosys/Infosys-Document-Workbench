/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.datasource.impl.emailserver.basic.api;

import java.util.Date;
import java.util.Map;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.datasource.api.IDataSourceWriter;
import com.infosys.ainauto.datasource.common.DataSourceException;
import com.infosys.ainauto.datasource.config.DataSourceConfig;
import com.infosys.ainauto.datasource.config.email.EmailServerDataSourceWriterConfig;
import com.infosys.ainauto.datasource.model.DataSourceRecord;
import com.infosys.ainauto.datasource.model.email.EmailServerDataSourceRecord;

public class EmailServerDataSourceWriter implements IDataSourceWriter {

	private final static Logger logger = LoggerFactory.getLogger(EmailServerDataSourceWriter.class);
	private EmailServerDataSourceWriterConfig emaiServerlDataSourceWriterConfig;
	private Session session;
	private Store store;
	private String name;
	private InternetAddress emailSenderInternetAddress;
	private String CHARSET_HTML_UTF8 = "text/html; charset=utf-8";
	private String PROP_IS_SAVE_MESSAGE_COPY = "isSaveMessageCopy";
	private String PROP_SAVE_MESSAGE_COPY_TO_FOLDER = "saveMessageCopyToFolder";
	// private Transport transport;

	public EmailServerDataSourceWriter(String name, DataSourceConfig dataSourceWriterConfig) {
		this.name = name;
		this.emaiServerlDataSourceWriterConfig = (EmailServerDataSourceWriterConfig) dataSourceWriterConfig;
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * @Description this method is used to connect the mail server
	 * @return Object of Store
	 */
	@Override
	public boolean connect() throws DataSourceException {
		boolean operationResult = false; // Assume operation will fail
		try {

			// set sender address
			this.emailSenderInternetAddress = new InternetAddress(emaiServerlDataSourceWriterConfig.getSenderEmailId(),
					emaiServerlDataSourceWriterConfig.getsenderName());

			if (!"true".equals(emaiServerlDataSourceWriterConfig.getAuthenticateUser())) {
				Properties props = new Properties();
				props.setProperty("mail.smtp.host", emaiServerlDataSourceWriterConfig.getHostName());
				props.setProperty("mail.smtp.port", emaiServerlDataSourceWriterConfig.getPort());
				session = Session.getInstance(props, null);

				// transport = session.getTransport("smtp");
				// transport.connect();
			} else {
				Properties props = new Properties();
				props.put("mail.smtp.host", emaiServerlDataSourceWriterConfig.getHostName());
				props.put("mail.smtp.port", emaiServerlDataSourceWriterConfig.getPort());
				props.put("mail.smtp.auth", emaiServerlDataSourceWriterConfig.getAuthenticateUser());
				// props.put("mail.smtp.starttls.enable", "true");

				Authenticator auth = new Authenticator() {
					// override the getPasswordAuthentication method
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(emaiServerlDataSourceWriterConfig.getSenderEmailId(),
								emaiServerlDataSourceWriterConfig.getSenderPassword());
					}
				};

				session = Session.getInstance(props, auth);

				/*
				 * transport.connect(emailSenderConfig.getHostName(),
				 * emailSenderConfig.getSenderEmailId(), emailSenderConfig.getSenderPassword());
				 */
			}
			store = session.getStore(emaiServerlDataSourceWriterConfig.getStoreProtocol());
			store.connect(emaiServerlDataSourceWriterConfig.getHostName(),
					emaiServerlDataSourceWriterConfig.getStoreProtocolPort(),
					emaiServerlDataSourceWriterConfig.getSenderEmailId(),
					emaiServerlDataSourceWriterConfig.getSenderPassword());
			operationResult = true;
		} catch (Exception e) {
			logger.error("Error occurred in connect method", e);
		}
		return operationResult;
	}

	@Override
	public boolean disconnect() throws DataSourceException {
		boolean operationResult = false; // Assume operation will fail
		try {
			store.close();
			operationResult = true;
		} catch (Exception e) {
			logger.error("Error occurred in disconnect method", e);
		}
		return operationResult;
	}

	@Override
	public boolean writeItem(DataSourceRecord dataSourceRecord, Map<String, String> paramsMap)
			throws DataSourceException {
		boolean operationResult = false; // Assume operation will fail
		try {
			MimeMessage mimeMessage = (MimeMessage) ((EmailServerDataSourceRecord) dataSourceRecord).getMimeMessage();
			mimeMessage.setSentDate(new Date());

			String subject = mimeMessage.getSubject();
			logger.debug("Email is ready to be sent with subject {}", subject);

			Transport.send(mimeMessage);

			if (paramsMap != null) {
				String isSaveMessage = paramsMap.getOrDefault(PROP_IS_SAVE_MESSAGE_COPY, "False");
				if (Boolean.valueOf(isSaveMessage)) {
					String saveMessageCopyToFolder = paramsMap.getOrDefault(PROP_SAVE_MESSAGE_COPY_TO_FOLDER,
							"Sent Items");

					Folder folder = null;
					try {
						// Fix for defect # 122
						if (!isConnected()) {
							connect();
						}
						folder = store.getFolder(saveMessageCopyToFolder);
						// Creates new folder if there is no folder present in mailbox
						// with property name
						if (!folder.exists()) {
							folder.create(Folder.HOLDS_MESSAGES);
						}
						folder.open(Folder.READ_WRITE);
						mimeMessage.setFlag(Flag.SEEN, true);
						folder.appendMessages(new Message[] { mimeMessage });
						logger.debug("Email saved in " + saveMessageCopyToFolder + " folder");
					} catch (Exception e) {
						/*
						 * logger.error(EmailAdapterConstants.ERR_SAVE_EMAIL_FAILED +
						 * "-Error while saving outbound email in " + emailSenderConfig.getSaveFolder()
						 * + " Folder", e); throw new
						 * Exception(EmailAdapterConstants.ERR_SAVE_EMAIL_FAILED +
						 * "-Error while saving outbound email in " + emailSenderConfig.getSaveFolder()
						 * + " folder", e);
						 */

						logger.error(
								"Error while saving outbound email (subj={}) in "
										+ StringUtility.sanitizeReqData(saveMessageCopyToFolder) + " folder",
								subject, e);
						throw new DataSourceException(
								"Error while saving outbound email in " + saveMessageCopyToFolder + " folder", e);

					} finally {
						try {
							if (folder != null && folder.isOpen()) {
								folder.close(true);
							}
						} catch (Exception e) {
							logger.error("Error while closing " + StringUtility.sanitizeReqData(saveMessageCopyToFolder)
									+ " folder", e);
						}
					}
				}
			}
		} catch (Exception ex) {
			logger.error("Error while calling writeItem", ex);
			throw new DataSourceException("Error while calling writeItem", ex);
		}
		operationResult = true;
		return operationResult;

	}

	@Override
	public DataSourceRecord generateNewItem() throws DataSourceException {
		final MimeMessage mimeMessage = new MimeMessage(session);
		try {
			// set message headers
			mimeMessage.addHeader("Content-type", CHARSET_HTML_UTF8);
			mimeMessage.addHeader("format", "flowed");
			mimeMessage.addHeader("Content-Transfer-Encoding", "8bit");
			// set from address
			mimeMessage.setFrom(emailSenderInternetAddress);
		} catch (Exception e) {
			logger.error("Error while creating new DataSourceRecord object", e);
			throw new DataSourceException("Error while creating new DataSourceRecord object", e);
		}
		EmailServerDataSourceRecord emailServerDataSourceRecord = new EmailServerDataSourceRecord("", null,
				mimeMessage);
		return emailServerDataSourceRecord;
	}

	/**
	 * Check if message store is connected
	 * 
	 * @return
	 */
	private boolean isConnected() {
		if (store != null && store.isConnected()) {
			return true;
		}
		return false;
	}
}
