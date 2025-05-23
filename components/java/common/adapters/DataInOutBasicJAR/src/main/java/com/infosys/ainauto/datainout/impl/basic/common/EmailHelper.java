/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.datainout.impl.basic.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.infosys.ainauto.commonutils.FileUtility;
import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.commonutils.PatternUtility;
import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.datainout.common.DataInputException;
import com.infosys.ainauto.datainout.config.DataInputConfig;
import com.infosys.ainauto.datainout.model.email.AttachmentRecord;
import com.infosys.ainauto.datainout.model.email.EmailAddress;
import com.infosys.ainauto.datainout.model.email.EmailRecord;

import microsoft.exchange.webservices.data.core.exception.service.local.ServiceLocalException;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.property.complex.Attachment;
import microsoft.exchange.webservices.data.property.complex.AttachmentCollection;
import microsoft.exchange.webservices.data.property.complex.FileAttachment;
import microsoft.exchange.webservices.data.property.complex.ItemAttachment;

public class EmailHelper {

	private final static Logger LOGGER = LoggerFactory.getLogger(EmailHelper.class);

	public EmailHelper() {
		LOGGER.info("Instantiated");
	}

	public static List<EmailAddress> getEmailAddress(Address[] addressArray) {
		List<EmailAddress> emailAddressList = new ArrayList<>();
		if (addressArray != null) {
			for (Address address : addressArray) {
				InternetAddress internetAddress = (InternetAddress) address;
				String emailFromId = internetAddress.getAddress();
				String emailFromName = internetAddress.getPersonal();
				emailAddressList.add(new EmailAddress(emailFromId, emailFromName));
			}
		}
		return emailAddressList;
	}

	public static Address[] getAddress(List<EmailAddress> emailAddressList) throws Exception {
		if (emailAddressList == null || emailAddressList.size() == 0) {
			return null;
		}
		List<Address> addressList = new ArrayList<>();
		for (EmailAddress emailAddress : emailAddressList) {
			Address address = new InternetAddress(emailAddress.getEmailId(), emailAddress.getEmailName());
			addressList.add(address);
		}
		return addressList.toArray(new Address[addressList.size()]);
	}

	public static String convertHtmlToPlainText(String html) {
		if (html == null || html.length() == 0) {
			return html;
		}
		Document doc = Jsoup.parse(html);
		return doc.text().toString();
	}

	public static String convertImageToCid(String html) {
		Pattern p = Pattern.compile(DataInOutConstants.PATTERN_IMG_SRC);
		Matcher m = p.matcher(html);
		String var = "cid:";
		while (m.find()) {
			String match = m.group(1);
			if (!match.toLowerCase(Locale.ENGLISH).startsWith("data:")) {
				html = html.replace(match, var + match);
			}
		}
		return html;
	}

	public static String convertCidToImage(String html) {
		Pattern p = Pattern.compile(DataInOutConstants.PATTERN_IMG_SRC);
		Matcher m = p.matcher(html);
		while (m.find()) {
			String match = m.group(1);
			if (match.toLowerCase(Locale.ENGLISH).startsWith("cid:")) {
				html = html.replace(match, match.substring(4, match.length()));
			}
		}
		return html;
	}

	public static boolean isAllowedByFilterCondition(Object message, DataInputConfig dataInputConfig)
			throws MessagingException {
		// getting list of whitelist filter
		List<String> subjectFilterList = dataInputConfig.getStringMatchForIncludeList();

		String mailSubjectCondtion = dataInputConfig.getStringMatchForIncludeCondition();

		// getting list of blacklist filter
		List<String> subjectBlacklistList = dataInputConfig.getStringMatchForExcludeList();
		String mailSubjectBlacklistCondtion = dataInputConfig.getStringMatchForExcludeCondition();

		boolean subjectMatchesWithRegex = false;
		String emailSubject = null;
		if (message instanceof Message) {
			Message msg = (Message) message;
			emailSubject = msg.getSubject();
		} else if (message instanceof EmailMessage) {
			EmailMessage emailMessage = (EmailMessage) message;
			try {
				emailSubject = emailMessage.getSubject();
			} catch (ServiceLocalException e) {
				LOGGER.error(e.getMessage());
			}
		}
		// Start : this condition to check for whitelist filter in
		// subject, include only
		// mail which will have
		// the matched filter in subject

		if (emailSubject != null && subjectFilterList != null && subjectFilterList.size() != 0) {

			subjectMatchesWithRegex = checkEmailSubjectFilter(emailSubject, subjectFilterList, mailSubjectCondtion);
		}
		// Start : Blacklist filter in subject, excludes mail which will
		// have the
		// matched filter in subject
		if (emailSubject != null && subjectBlacklistList != null && subjectBlacklistList.size() != 0) {
			if (checkEmailSubjectFilter(emailSubject, subjectBlacklistList, mailSubjectBlacklistCondtion)) {
				subjectMatchesWithRegex = false;
			}
		}
		if (subjectMatchesWithRegex || subjectFilterList == null || subjectFilterList.size() == 0) {
			return true;
		}

		return false;
	}

	private static boolean checkEmailSubjectFilter(String emailSubject, List<String> subjectFilterList,
			String mailSubjectCondtion) {
		int k = 0;
		boolean subjectMatchesWithRegex = false;
		while (k < subjectFilterList.size()) {
			String regex = subjectFilterList.get(k).toLowerCase();
			String input = emailSubject.toLowerCase();
			if (mailSubjectCondtion.equalsIgnoreCase("OR")) {
				if (Pattern.compile(regex).matcher(input).matches()) {
					// if (Pattern.matches(subjectFilterList.get(k).toLowerCase(),
					// emailSubject.toLowerCase())) {
					subjectMatchesWithRegex = true;
					break;
				}

			} else if (mailSubjectCondtion.equalsIgnoreCase("AND")) {
				if (Pattern.compile(regex).matcher(input).matches()) {
					// if (Pattern.matches(subjectFilterList.get(k).toLowerCase(),
					// emailSubject.toLowerCase())) {
					subjectMatchesWithRegex = true;
				} else {
					subjectMatchesWithRegex = false;
					break;
				}
			}

			k++;
		}

		return subjectMatchesWithRegex;
	}

	public static void updateEmailAttributes(Object msg, EmailRecord emailRecord) throws MessagingException {

		if (msg instanceof Message) {
			Message message = (Message) msg;
			Date receivedDate = message.getReceivedDate() == null ? message.getSentDate() : message.getReceivedDate();
			emailRecord.setEmailDate(receivedDate);
			emailRecord.setEmailSubject(message.getSubject());
			emailRecord.setEmailAddressFrom(getEmailAddress(message.getFrom()).get(0));

			// Handle TO List
			emailRecord.setEmailAddressToList(getEmailAddress(message.getRecipients(Message.RecipientType.TO)));

			// Handle CC List
			emailRecord.setEmailAddressCcList(getEmailAddress(message.getRecipients(Message.RecipientType.CC)));

			emailRecord.setFlags(message.isSet(Flags.Flag.USER));
			emailRecord.setPriority(message.getHeader("X-Priority"));
		} else if (msg instanceof EmailMessage) {
			EmailMessage message = (EmailMessage) msg;
			try {
				emailRecord.setEmailDate(message.getDateTimeReceived());
				emailRecord.setEmailSubject(message.getSubject());
				emailRecord.setEmailAddressFrom(
						new EmailAddress(message.getSender().getAddress(), message.getSender().getName()));

				// Handle TO List
				List<EmailAddress> emailAddressToList = new ArrayList<>();

				if (ListUtility.hasValue(message.getToRecipients().getItems())) {
					for (int i = 0; i < message.getToRecipients().getItems().size(); i++) {
						emailAddressToList
								.add(new EmailAddress(message.getToRecipients().getItems().get(i).getAddress(),
										message.getToRecipients().getItems().get(i).getName()));
					}
				}
				emailRecord.setEmailAddressToList(emailAddressToList);

				// Handle CC List
				List<EmailAddress> emailAddressCcList = new ArrayList<>();
				if (ListUtility.hasValue(message.getCcRecipients().getItems())) {
					for (int i = 0; i < message.getCcRecipients().getItems().size(); i++) {
						emailAddressCcList
								.add(new EmailAddress(message.getCcRecipients().getItems().get(i).getAddress(),
										message.getCcRecipients().getItems().get(i).getName()));
					}
				}
				emailRecord.setEmailAddressCcList(emailAddressCcList);

				// emailRecord.setFlags();
				// emailRecord.setPriority();

			} catch (ServiceLocalException e) {
				LOGGER.error(e.getMessage());
			}
		}
	}

	public static void updateBodyAndAttachments(Message message, EmailRecord emailRecord, String saveDirectory) {
		String bodyText = "";
		String bodyHtml = "";
		try {
			String messageContentString = message.getContent().toString();
			if (message.isMimeType("text/plain") || message.isMimeType("test/rtf")) {
				bodyText += messageContentString;
				bodyHtml += "";
			} else if (message.isMimeType("text/html")) {
				bodyText += EmailHelper.convertHtmlToPlainText(messageContentString);
				bodyHtml += EmailHelper.convertCidToImage(messageContentString);
			} else if (message.isMimeType("multipart/*")) {
				MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
				int count = mimeMultipart.getCount();
				List<AttachmentRecord> attachmentRecordList = new ArrayList<>();
				List<AttachmentRecord> inlineAttachmentRecordList = new ArrayList<>();
				for (int i = 0; i < count; i++) {
					BodyPart bodyPart = mimeMultipart.getBodyPart(i);
					String bodyPartContentString = bodyPart.getContent().toString();
					if (bodyPart.isMimeType("text/plain") || bodyPart.isMimeType("text/rtf")) {
						bodyHtml += "";
						if (bodyPart.getDisposition() == null) {
							bodyText += bodyPartContentString;
						} else if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
							AttachmentRecord attachmentsReaded = readTxtAttachments(bodyPart, saveDirectory);
							attachmentRecordList.add(attachmentsReaded);
						}
					} else if (bodyPart.isMimeType("text/html")) {
						bodyText += EmailHelper.convertHtmlToPlainText(bodyPartContentString);
						bodyHtml += EmailHelper.convertCidToImage(bodyPartContentString);
					} else if (bodyPart.isMimeType("message/delivery-status")) {
						bodyText += "";
						bodyHtml += "";
					} else if (bodyPart.isMimeType("message/rfc822")) {
						Message msg = (Message) bodyPart.getContent();

						MimeMultipart subMimeMulPart = (MimeMultipart) msg.getContent();

						int noOfParts = subMimeMulPart.getCount();
						for (int j = 0; j < noOfParts; j++) {
							MimeBodyPart subBodyPart = (MimeBodyPart) subMimeMulPart.getBodyPart(j);
							if (Part.ATTACHMENT.equalsIgnoreCase(subBodyPart.getDisposition())) {
								AttachmentRecord attachmentsReaded = readAttachments(subBodyPart, saveDirectory);
								attachmentRecordList.add(attachmentsReaded);
							} else if (Part.INLINE.equalsIgnoreCase(subBodyPart.getDisposition())) {
								AttachmentRecord inLineimagesAttachments = readInlineImages(subBodyPart, saveDirectory);
								inlineAttachmentRecordList.add(inLineimagesAttachments);
							}

						}

					} else if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
						AttachmentRecord attachmentsReaded = readAttachments((MimeBodyPart) bodyPart, saveDirectory);
						attachmentRecordList.add(attachmentsReaded);
					} else if (Part.INLINE.equalsIgnoreCase(bodyPart.getDisposition())) {
						AttachmentRecord inLineimagesAttachments = readInlineImages((MimeBodyPart) bodyPart,
								saveDirectory);
						inlineAttachmentRecordList.add(inLineimagesAttachments);
					} else if (bodyPart.getDisposition() == null && bodyPart.getContent() != null
							&& bodyPart.isMimeType("multipart/*")) {
						Multipart subMultiPart = (Multipart) bodyPart.getContent();
						int numberOfSubParts = subMultiPart.getCount();
						for (int subpartCount = 0; subpartCount < numberOfSubParts; subpartCount++) {
							MimeBodyPart subPart = (MimeBodyPart) subMultiPart.getBodyPart(subpartCount);
							String subPartContentString = subPart.getContent().toString();
							if (subPart.isMimeType("text/plain") || subPart.isMimeType("text/rtf")) {
								bodyText += subPartContentString;
								bodyHtml += "";
							} else if (subPart.isMimeType("text/html")) {
								bodyText += EmailHelper.convertHtmlToPlainText(subPartContentString);
								bodyHtml += EmailHelper.convertCidToImage(subPartContentString);
							} else if (Part.ATTACHMENT.equalsIgnoreCase(subPart.getDisposition())) {
								AttachmentRecord attachmentsReaded = readAttachments(subPart, saveDirectory);
								attachmentRecordList.add(attachmentsReaded);
							} else if (Part.INLINE.equalsIgnoreCase(subPart.getDisposition())) {
								AttachmentRecord inLineimagesAttachments = readInlineImages(subPart, saveDirectory);
								inlineAttachmentRecordList.add(inLineimagesAttachments);
							} else if (subPart.getDisposition() == null && subPart.isMimeType("multipart/*")) {
								Multipart subOfSubMultiPart = (Multipart) subPart.getContent();
								int numberOfSubOfSubParts = subOfSubMultiPart.getCount();
								for (int subOfSubPartCount = 0; subOfSubPartCount < numberOfSubOfSubParts; subOfSubPartCount++) {
									MimeBodyPart subOfSubPart = (MimeBodyPart) subOfSubMultiPart
											.getBodyPart(subOfSubPartCount);
									String subOfSubPartContentString = subOfSubPart.getContent().toString();
									if (subOfSubPart.isMimeType("text/plain") || subOfSubPart.isMimeType("text/rtf")) {
										bodyText += subOfSubPartContentString;
										bodyHtml += "";
									} else if (subOfSubPart.isMimeType("text/html")) {
										bodyText += EmailHelper.convertHtmlToPlainText(subOfSubPartContentString);
										bodyHtml += EmailHelper.convertCidToImage(subOfSubPartContentString);
									} else if (Part.ATTACHMENT.equalsIgnoreCase(subOfSubPart.getDisposition())) {
										AttachmentRecord attachmentsReaded = readAttachments(subOfSubPart,
												saveDirectory);
										attachmentRecordList.add(attachmentsReaded);
									} else if (Part.INLINE.equalsIgnoreCase(subOfSubPart.getDisposition())) {
										AttachmentRecord inLineimagesAttachments = readInlineImages(subOfSubPart,
												saveDirectory);
										inlineAttachmentRecordList.add(inLineimagesAttachments);
									}
								}
							}
						}
					} else {/* if (bodyPart.getContent() instanceof MimeMultipart) */
						throw new Exception("unknown mimetype");
					}
				}

				if (attachmentRecordList != null && attachmentRecordList.size() > 0) {
					emailRecord.setAttachmentRecordList(attachmentRecordList);
				}
				if (inlineAttachmentRecordList != null && inlineAttachmentRecordList.size() > 0) {
					emailRecord.setInlineAttachmentRecordList(inlineAttachmentRecordList);
				}
			}

		} catch (Exception ex) {
			LOGGER.error("Error ", ex);
		}
		// Set as not null ONLY IF there is some non-zero data.
		if (bodyText != null && bodyText.length() > 0) {
			emailRecord.setEmailBodyText(bodyText);
		}
		if (bodyHtml != null && bodyHtml.length() > 0) {
			emailRecord.setEmailBodyHtml(bodyHtml);
		}

	}

	public static AttachmentRecord readTxtAttachments(BodyPart bodyPart, String saveDirectory) throws Exception {
		String actualFileName = bodyPart.getDescription();
		String content = (String) bodyPart.getContent();
		String storedFileName = FileUtility.generateUniqueFileName(actualFileName);
		String storedFileFullPath = FileUtility.getConcatenatedName(saveDirectory, storedFileName);
		FileUtility.createDirsRecursively(saveDirectory);
		FileUtility.saveFile(storedFileFullPath, content);
		AttachmentRecord attachmentRecord = new AttachmentRecord();
		attachmentRecord.setActualFileName(actualFileName);
		attachmentRecord.setStoredFileName(storedFileName);
		attachmentRecord.setStoredFileFullPath(storedFileFullPath);
		return attachmentRecord;
	}

	public static AttachmentRecord readAttachments(MimeBodyPart part, String saveDirectory) throws Exception {
		String actualFileName = part.getFileName();
		String storedFileName = FileUtility.generateUniqueFileName(actualFileName);
		String storedFileFullPath = FileUtility.getConcatenatedName(saveDirectory, storedFileName);
		FileUtility.createDirsRecursively(saveDirectory);
		part.saveFile(storedFileFullPath);
		AttachmentRecord attachmentRecord = new AttachmentRecord();
		attachmentRecord.setActualFileName(actualFileName);
		attachmentRecord.setStoredFileName(storedFileName);
		attachmentRecord.setStoredFileFullPath(storedFileFullPath);
		return attachmentRecord;

	}

	public static AttachmentRecord readInlineImages(MimeBodyPart part, String saveDirectory) throws Exception {
		String actualFileName = part.getFileName();
		String contentId = part.getContentID();
		contentId = contentId.substring(1, contentId.length() - 1);
		LOGGER.debug("Content-Id : " + contentId + " and actual file name of image is " + actualFileName);
		String storedFileName = FileUtility.generateUniqueFileName(actualFileName);
		String storedFileFullPath = FileUtility.getConcatenatedName(saveDirectory, storedFileName);
		FileUtility.createDirsRecursively(saveDirectory);
		part.saveFile(storedFileFullPath);
		AttachmentRecord attachmentRecord = new AttachmentRecord();
		if (actualFileName.equalsIgnoreCase(contentId)) {
			attachmentRecord.setActualFileName(actualFileName);
		} else {
			attachmentRecord.setActualFileName(contentId);
		}
		attachmentRecord.setStoredFileName(storedFileName);
		attachmentRecord.setStoredFileFullPath(storedFileFullPath);
		return attachmentRecord;
	}

	/**
	 * Read provided <b>*.eml</b> file as a Message.
	 * 
	 * @param emlFileFullPath
	 * @return
	 */
	public static Message readEmlFile(String emlFileFullPath) throws DataInputException {

		MimeMessage message = null;

		try (InputStream source = new FileInputStream(emlFileFullPath)) {
			Properties props = System.getProperties();
			props.setProperty("mail.store.protocol", "imap");
			props.setProperty("mail.imap.ssl.enable", "true");
			Session session = Session.getInstance(props, null);
			message = new MimeMessage(session, source);
		} catch (Exception e) {
			LOGGER.error("Error occurred in readEmlFile", e);
			throw new DataInputException("Error occurred in readEmlFile", e);
		}
		return message;
	}

	/**
	 * Save provided email as a <b>*.eml</b> file.
	 * 
	 * @param message
	 * @return
	 */
	public static boolean saveAsEmlFile(Message message, String emlFileFullPath) {

		boolean isSuccess = true;
		FileOutputStream fileOutputStream = null;
		try {
			fileOutputStream = new FileOutputStream(new File(emlFileFullPath));
			message.writeTo(fileOutputStream);
		} catch (Exception e) {
			isSuccess = false;
			LOGGER.error("Error occurred in saveAsEmlFile", e);
		} finally {
			FileUtility.safeCloseOutputStream(fileOutputStream);
		}
		return isSuccess;
	}

	public static void updateBodyAndAttachmentsExchangeOnprem(EmailMessage emailMessage, EmailRecord emailRecord,
			String saveDirectory) {
		String bodyText = "";
		String bodyHtml = "";
		List<AttachmentRecord> attachmentRecordList = null;
		List<AttachmentRecord> inlineAttachmentRecordList = new ArrayList<>();

		try {
			String content = emailMessage.getBody().toString();
			bodyText += EmailHelper.convertHtmlToPlainText(content);
			bodyHtml += EmailHelper.convertCidToImage(content);

			if (emailMessage.getHasAttachments() || emailMessage.getAttachments().getItems().size() > 0) {
				attachmentRecordList = new ArrayList<>();
				inlineAttachmentRecordList = new ArrayList<>();
				// get all the attachments
				AttachmentCollection attachmentsCol = emailMessage.getAttachments();
				LOGGER.debug("File Count: " + attachmentsCol.getCount());

				// loop over the attachments
				for (int i = 0; i < attachmentsCol.getCount(); i++) {
					Attachment attachment = attachmentsCol.getPropertyAtIndex(i);

					if (attachment.getIsInline()) {
						AttachmentRecord attachmentRecord = readExternalAttachments(attachment, saveDirectory);
						if (attachmentRecord != null) {
							inlineAttachmentRecordList.add(attachmentRecord);
						}
					} else if (attachment instanceof FileAttachment) {// FileAttachment - Represents a file that is
																		// attached to an email item
						AttachmentRecord attachmentRecord = readExternalAttachments(attachment, saveDirectory);
						if (attachmentRecord != null) {
							attachmentRecordList.add(attachmentRecord);
						}
					} else if (attachment instanceof ItemAttachment) { // ItemAttachment - Represents an
																		// Exchange item that is
																		// attached to another Exchange
																		// item.

					}
				}
			}
		} catch (Exception ex) {
			LOGGER.error("Error ", ex);
		}

		if (ListUtility.hasValue(inlineAttachmentRecordList)) {
			emailRecord.setInlineAttachmentRecordList(inlineAttachmentRecordList);

			// To avoid inline image src value issue.
			List<String> fileNames = PatternUtility.getHtmlImgSrcValues(bodyHtml);
			for (String fileName : fileNames) {
				for (AttachmentRecord attachmentRecord : inlineAttachmentRecordList) {
					String actualFileName = attachmentRecord.getActualFileName();
					if (fileName.split("\\.")[0].equalsIgnoreCase(actualFileName.split("\\.")[0])) {
						bodyHtml = bodyHtml.replace(fileName, actualFileName);
					}
				}
			}
		}

		// Set as not null ONLY IF there is some non-zero data.
		if (StringUtility.hasValue(bodyText)) {
			emailRecord.setEmailBodyText(bodyText);
		}

		if (StringUtility.hasValue(bodyHtml)) {
			emailRecord.setEmailBodyHtml(bodyHtml);
		}

		if (ListUtility.hasValue(attachmentRecordList)) {
			emailRecord.setAttachmentRecordList(attachmentRecordList);
		}

	}

	private static AttachmentRecord readExternalAttachments(Attachment attachment, String saveDirectory)
			throws Exception {
		AttachmentRecord attachmentRecord = null;
		FileAttachment fileAttachment = (FileAttachment) attachment;
		fileAttachment.load();
		String actualFileName = attachment.getName();
		String storedFileName = FileUtility.generateUniqueFileName(actualFileName);
		String storedFileFullPath = FileUtility.getConcatenatedName(saveDirectory, storedFileName);
		FileUtility.createDirsRecursively(saveDirectory);

		boolean isSuccess = true;
		try (FileOutputStream out = new FileOutputStream(FileUtility.cleanPath(storedFileFullPath))) {
			out.write(fileAttachment.getContent());
		} catch (IOException e) {
			isSuccess = false;
			LOGGER.error("Error occurred in saveFile()", e);
		}

		if (isSuccess) {
			attachmentRecord = new AttachmentRecord();
			attachmentRecord.setActualFileName(actualFileName);
			attachmentRecord.setStoredFileName(storedFileName);
			attachmentRecord.setStoredFileFullPath(storedFileFullPath);
		}
		return attachmentRecord;
	}

}
