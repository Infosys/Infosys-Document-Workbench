/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.datainout.impl.basic.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Date;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.infosys.ainauto.commonutils.FileUtility;
import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.datainout.api.AbstractDataOutputWriter;
import com.infosys.ainauto.datainout.api.IDataOutputWriter;
import com.infosys.ainauto.datainout.common.DataOutputException;
import com.infosys.ainauto.datainout.config.DataOutputConfig;
import com.infosys.ainauto.datainout.impl.basic.common.EmailHelper;
import com.infosys.ainauto.datainout.model.DataOutputRecord;
import com.infosys.ainauto.datainout.model.email.AttachmentRecord;
import com.infosys.ainauto.datainout.model.email.EmailRecord;
import com.infosys.ainauto.datasource.api.IDataSourceWriter;
import com.infosys.ainauto.datasource.config.DataSourceConfig;
import com.infosys.ainauto.datasource.config.email.EmailServerDataSourceWriterConfig;
import com.infosys.ainauto.datasource.config.file.FileSystemDataSourceWriterConfig;
import com.infosys.ainauto.datasource.model.DataSourceRecord;
import com.infosys.ainauto.datasource.model.email.EmailServerDataSourceRecord;
import com.infosys.ainauto.datasource.model.file.FileSystemDataSourceRecord;

public class BasicDataOutputWriter extends AbstractDataOutputWriter implements IDataOutputWriter {

	private final static Logger logger = LoggerFactory.getLogger(BasicDataOutputWriter.class);
	private static final String CHARSET_HTML_UTF8 = "text/html; charset=utf-8";
	private static final String CHARSET_TXT_UTF8 = "text/plain; charset=utf-8";
	private static final String FILE_EXTENSION_EMAIL = ".eml";
	private static final String PATTERN_FILE_NAME_SAFE = "[^.a-zA-Z\\d\\s-]";
	private static final String PATTERN_SINGLE_SPACE = "\\s";
	private static final String PATTERN_DOUBLE_SPACES = "\\s\\s+";
	private static final String TOKEN_SEPARATOR = "_";
	private static final int EMAIL_SUBJECT_MAX_LENGTH = 20;

	public BasicDataOutputWriter(DataOutputConfig dataOutputConfig) {
		super(dataOutputConfig);
		this.dataOutputConfig = (DataOutputConfig) dataOutputConfig;
		logger.info("New instance created");
	}

	@Override
	protected DataSourceRecord parseDataSource(DataOutputRecord dataOutputRecord, DataSourceConfig dataSourceConfig,
			IDataSourceWriter dataSourceWriter) throws DataOutputException {

		DataSourceRecord resultDataSourceRecord = null;
		try {
			if (dataSourceConfig instanceof EmailServerDataSourceWriterConfig) {
				// If data output record contains email record then proceed
				if (dataOutputRecord.getEmailRecord() != null) {
					// Call method to generate a partially populated record item
					EmailServerDataSourceRecord emailServerDataSourceRecord = (EmailServerDataSourceRecord) dataSourceWriter
							.generateNewItem();
					populateMimeMessageObj(dataOutputRecord.getEmailRecord(),
							(MimeMessage) emailServerDataSourceRecord.getMimeMessage());
					return emailServerDataSourceRecord;
				}
			} else if (dataSourceConfig instanceof FileSystemDataSourceWriterConfig) {
				MimeMessage mimeMessage = createNewMimeMessage();
				populateMimeMessageObj(dataOutputRecord.getEmailRecord(), mimeMessage);
				FileSystemDataSourceRecord fileSystemDataSourceRecord = new FileSystemDataSourceRecord();
				ByteArrayOutputStream byteOs = new ByteArrayOutputStream();
				mimeMessage.writeTo(byteOs);
				fileSystemDataSourceRecord.setFileContent(byteOs.toByteArray());
				String emailSubject = dataOutputRecord.getEmailRecord().getEmailSubject();
				if (StringUtility.hasTrimmedValue(emailSubject)) {
					emailSubject = StringUtility.findAndReplace(emailSubject, PATTERN_FILE_NAME_SAFE, "");
					emailSubject = emailSubject.replaceAll(PATTERN_DOUBLE_SPACES, " ");
					emailSubject = emailSubject.replaceAll(PATTERN_SINGLE_SPACE, TOKEN_SEPARATOR);
					emailSubject = emailSubject.length() > EMAIL_SUBJECT_MAX_LENGTH
							? emailSubject.substring(0, EMAIL_SUBJECT_MAX_LENGTH)
							: emailSubject;
				} else {
					emailSubject = "";
				}

				String fileName = StringUtility.getUniqueString() + TOKEN_SEPARATOR + emailSubject.trim()
						+ FILE_EXTENSION_EMAIL;
				fileSystemDataSourceRecord.setFileNameToSave(fileName);
				return fileSystemDataSourceRecord;
			}
		} catch (Exception ex) {
			logger.error("Error occurred in parseDataSource method", ex);
			throw new DataOutputException("Error occurred in parseDataSource method", ex);
		}
		return resultDataSourceRecord;
	}

	private MimeMessage createNewMimeMessage() throws DataOutputException {
		Session session = null;
		MimeMessage mimeMessage = new MimeMessage(session);
		try {
			// set message headers
			mimeMessage.addHeader("Content-type", CHARSET_HTML_UTF8);
			mimeMessage.addHeader("format", "flowed");
			mimeMessage.addHeader("Content-Transfer-Encoding", "8bit");

		} catch (Exception e) {
			logger.error("Error while creating new MimeMessage object", e);
			throw new DataOutputException("Error while creating new MimeMessage object", e);
		}
		return mimeMessage;
	}

	private void populateMimeMessageObj(EmailRecord emailRecord, MimeMessage msg) throws DataOutputException {
		try {
			msg.setSubject(cleanNewLineCharacters(emailRecord.getEmailSubject()), "UTF-8");
			boolean isUseMultiPart = false;
			if (ListUtility.hasValue(emailRecord.getInlineAttachmentRecordList())
					|| ListUtility.hasValue(emailRecord.getAttachmentRecordList())) {
				isUseMultiPart = true;
			}

			if (!isUseMultiPart) {
				if (StringUtility.hasValue(emailRecord.getEmailBodyHtml())) {
					// Use setContent for HTML emails
					msg.setContent(emailRecord.getEmailBodyHtml(), CHARSET_HTML_UTF8);
				} else if (StringUtility.hasValue(emailRecord.getEmailBodyText())) {
					// Use setText for plain text emails
					msg.setText(emailRecord.getEmailBodyText(), "UTF-8");
				} else {
					// Set explicitly as empty string as null causes exception
					msg.setText("", "UTF-8");
				}
			} else {
				// creates message body part
				MimeBodyPart messageBodyPart = new MimeBodyPart();

				// creates multi-part message
				Multipart multipart = new MimeMultipart();
				if (StringUtility.hasValue(emailRecord.getEmailBodyHtml())
						|| StringUtility.hasValue(emailRecord.getEmailBodyText())) {
					if (StringUtility.hasValue(emailRecord.getEmailBodyHtml())) {
						String html = emailRecord.getEmailBodyHtml();
						// adds inline image attachments
						if (ListUtility.hasValue(emailRecord.getInlineAttachmentRecordList())) {
							html = EmailHelper.convertImageToCid(emailRecord.getEmailBodyHtml());
							logger.debug("html after formatting:" + html);
							messageBodyPart.setContent(html, CHARSET_HTML_UTF8);
							multipart.addBodyPart(messageBodyPart);
							for (AttachmentRecord attachmentRecord : emailRecord.getInlineAttachmentRecordList()) {
								String actualFileName = attachmentRecord.getActualFileName();
								String storedFilePath = attachmentRecord.getStoredFileFullPath();
								MimeBodyPart imagePart = new MimeBodyPart();
								imagePart.setHeader("Content-ID", "<" + cleanNewLineCharacters(actualFileName) + ">");
								imagePart.setDisposition(MimeBodyPart.INLINE);
								imagePart.setFileName(actualFileName);
								try {
									imagePart.attachFile(storedFilePath);
								} catch (IOException ex) {
									logger.error("IOException occured while attaching inline image", ex);
								}
								multipart.addBodyPart(imagePart);
							}
						} else {
							messageBodyPart.setContent(html, CHARSET_HTML_UTF8);
							multipart.addBodyPart(messageBodyPart);
						}
					} else {
						messageBodyPart.setContent(emailRecord.getEmailBodyText(), CHARSET_TXT_UTF8);
						multipart.addBodyPart(messageBodyPart);
					}
				} else {
					messageBodyPart.setText("");
					multipart.addBodyPart(messageBodyPart);
				}

				// To send attachments
				if (ListUtility.hasValue(emailRecord.getAttachmentRecordList())) {
					for (AttachmentRecord attachmentRecord : emailRecord.getAttachmentRecordList()) {
						// this part is attachment
						String actualFileName = attachmentRecord.getActualFileName();
						MimeBodyPart messageAttachment = new MimeBodyPart();
						String fileFullPath = FileUtility.cleanPath(attachmentRecord.getStoredFileFullPath());
						fileFullPath = URLDecoder.decode(fileFullPath, "UTF-8");
						logger.debug("file path is :" + fileFullPath);
						DataSource source = new FileDataSource(fileFullPath);
						messageAttachment.setDataHandler(new DataHandler(source));
						messageAttachment.setFileName(actualFileName);
						multipart.addBodyPart(messageAttachment);

					}
				}
				msg.setContent(multipart);
			}

			msg.setSentDate(new Date());
			msg.setRecipients(Message.RecipientType.TO, EmailHelper.getAddress(emailRecord.getEmailAddressToList()));
			msg.setRecipients(Message.RecipientType.CC, EmailHelper.getAddress(emailRecord.getEmailAddressCcList()));
			msg.setRecipients(Message.RecipientType.BCC, EmailHelper.getAddress(emailRecord.getEmailAddressBccList()));
		} catch (Exception e) {
			logger.error("Error in populateMimeMessageObj method for subject=",
					StringUtility.sanitizeReqData(emailRecord.getEmailSubject()), e);
			throw new DataOutputException("Error in populateMimeMessageObj method", e);
		}
	}

	// Fix for Header Manipulation: SMTP to avoid new lines in header.
	private String cleanNewLineCharacters(String str) {
		return StringUtils.normalizeSpace(str);
	}
}
