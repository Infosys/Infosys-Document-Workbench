/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.process.email;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.FileUtility;
import com.infosys.ainauto.commonutils.PatternUtility;
import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.service.common.AttachmentHelper;
import com.infosys.ainauto.docwb.service.common.FileUtil;
import com.infosys.ainauto.docwb.service.common.WorkbenchConstants;
import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.common.type.EnumExtractType;
import com.infosys.ainauto.docwb.service.common.type.EnumSystemAttributeName;
import com.infosys.ainauto.docwb.service.common.type.EnumTaskStatus;
import com.infosys.ainauto.docwb.service.dao.attachment.IAttachmentDataAccess;
import com.infosys.ainauto.docwb.service.dao.attribute.IAttributeDataAccess;
import com.infosys.ainauto.docwb.service.dao.email.IEmailDataAccess;
import com.infosys.ainauto.docwb.service.model.api.AttachmentResData;
import com.infosys.ainauto.docwb.service.model.api.EmailResData;
import com.infosys.ainauto.docwb.service.model.api.PaginationResData;
import com.infosys.ainauto.docwb.service.model.api.attachment.AttachmentReqData;
import com.infosys.ainauto.docwb.service.model.api.email.InsertEmailReqData;
import com.infosys.ainauto.docwb.service.model.api.email.InsertUpdateDraftReqData;
import com.infosys.ainauto.docwb.service.model.api.email.UpdateEmailStatusReqData;
import com.infosys.ainauto.docwb.service.model.db.AttachmentDbData;
import com.infosys.ainauto.docwb.service.model.db.AttributeDbData;
import com.infosys.ainauto.docwb.service.model.db.EmailDbData;
import com.infosys.ainauto.docwb.service.model.db.EntityDbData;
import com.infosys.ainauto.docwb.service.process.attachment.IAttachmentProcess;

@Component
@PropertySource("classpath:application.properties")
public class EmailProcess implements IEmailProcess {

	@Autowired
	private IEmailDataAccess emailDataAccess;

	@Autowired
	private IAttributeDataAccess attributeDataAccess;

	@Autowired
	private IAttachmentProcess attachProcess;

	@Autowired
	private IAttachmentDataAccess attachmentDataAccess;

	@Value("${mail.downloader.id}")
	private String mailDownloaderId;

	@Value("${pageSize}")
	private int pageSize;

	@Value("${attachmentFilePath}")
	private String attachmentFilePath;

	private static final Logger logger = LoggerFactory.getLogger(EmailProcess.class);

	@PostConstruct
	private void init() {
	}

	public EntityDbData sendEmail(InsertEmailReqData insertEmailReqData, AttachmentReqData attachmentRequestData)
			throws WorkbenchException {
		EntityDbData outboundEmailEntityData = new EntityDbData();
		List<Long> emailIdList = new ArrayList<Long>();
		int attachmentCount = 0;
		try {
			long emailOutboundId = 0;
			String errorMessage = "Invalid E-mail Id.";
			outboundEmailEntityData.setOutboundEmailMessage(errorMessage);
			outboundEmailEntityData.setEmailIdList(emailIdList);
			outboundEmailEntityData.setAttachmentCount(attachmentCount);

			EmailDbData draftEmail = getDraftEmail(insertEmailReqData.getDocId());
			InsertEmailReqData insertEmailData = new InsertEmailReqData();
			if (insertEmailReqData.getEmailTo().length() > 0) {
				String formatEmailIdResult = PatternUtility.formatEmailId(insertEmailReqData.getEmailTo());
				if (formatEmailIdResult != errorMessage) {
					insertEmailData.setEmailTo(formatEmailIdResult);
				} else {
					errorMessage = "Incorrect email id in To address. Please enter valid To address.";
					outboundEmailEntityData.setOutboundEmailMessage(errorMessage);

					return outboundEmailEntityData;
				}
			} else {
				errorMessage = "Please enter To address.";
				outboundEmailEntityData.setOutboundEmailMessage(errorMessage);
				return outboundEmailEntityData;
			}
			if (insertEmailReqData.getEmailCC().length() > 0) {
				String formatEmailCcIdResult = PatternUtility.formatEmailId(insertEmailReqData.getEmailCC());

				if (formatEmailCcIdResult != errorMessage) {
					insertEmailData.setEmailCC(formatEmailCcIdResult);
				} else {
					errorMessage = "Incorrect email id in Cc address. Please enter valid Cc address.";
					outboundEmailEntityData.setOutboundEmailMessage(errorMessage);
					return outboundEmailEntityData;
				}
			} else {
				insertEmailData.setEmailCC(insertEmailReqData.getEmailCC());
			}

			insertEmailData.setEmailSubject(insertEmailReqData.getEmailSubject());

			// Converting Base64 String to Image
			String imageBase64 = insertEmailReqData.getEmailBodyHtml();
			if (imageBase64 != null) {
				insertEmailData.setEmailBodyHtml(decodeBase64Images(imageBase64));
			} else {
				insertEmailData.setEmailBodyHtml(imageBase64);
			}

			insertEmailData.setEmailBCC(insertEmailReqData.getEmailBCC());
			insertEmailData.setDocId(insertEmailReqData.getDocId());
			// Set task status code as YTS
			insertEmailData.setTaskStatusCde(EnumTaskStatus.YET_TO_START.getValue());
			// emailDbData.setTaskStatusCde(emailData.getTaskStatusCde());
			if (draftEmail != null && draftEmail.getEmailOutboundId() != 0) {
				// To remove duplicate (or) old draft Image files.
				List<String> draftFilesList = encodeImagesToBase64(draftEmail);
				for (int i = 0; i < draftFilesList.size(); i++) {
					String filePath = FileUtility.getConcatenatedName(attachmentFilePath, draftFilesList.get(i));
					FileUtility.deleteFile(filePath);
				}
				insertEmailData.setEmailOutboundId(draftEmail.getEmailOutboundId());
				EmailDbData emailDbData = new EmailDbData();
				BeanUtils.copyProperties(insertEmailData, emailDbData);
				emailDataAccess.updateDraftEmail(emailDbData);

				emailOutboundId = draftEmail.getEmailOutboundId();
				if (emailOutboundId > 0) {
					emailIdList.add(emailOutboundId);
				}
				outboundEmailEntityData.setEmailIdList(emailIdList);
				outboundEmailEntityData.setTaskStatusCde(insertEmailData.getTaskStatusCde());
				outboundEmailEntityData.setDraft(true);

			} else {
				emailOutboundId = emailDataAccess.insertOutboundEmail(insertEmailData);
				if (emailOutboundId > 0) {
					emailIdList.add(emailOutboundId);
				}

				outboundEmailEntityData.setEmailIdList(emailIdList);
				outboundEmailEntityData.setTaskStatusCde(insertEmailData.getTaskStatusCde());
				outboundEmailEntityData.setDraft(false);
			}

			if (attachmentRequestData != null) {
				// attachmentRequestData.getMultipartFileList().isEmpty()
				if (attachmentRequestData.getMultipartFileList() != null
						&& attachmentRequestData.getMultipartFileList().size() > 0 && emailOutboundId != 0) {
					attachmentCount = attachProcess.addAttachmentToEmail(attachmentRequestData, emailOutboundId);
					outboundEmailEntityData.setAttachmentCount(attachmentCount);
				}
			}
			if (insertEmailData.getEmailBodyHtml() != null) {
				storeInlineAttachment(insertEmailData.getEmailBodyHtml(), emailOutboundId);
			}

		} catch (Exception e) {
			logger.error("Error occured while sending email", e);
			throw new WorkbenchException("Error occured while sending email", e);
		}
		outboundEmailEntityData.setOutboundEmailMessage("Email saved and will be sent shortly.");
		outboundEmailEntityData.setAttachmentCount(attachmentCount);
		return outboundEmailEntityData;
	}

	public List<EmailResData> getOutboundEmailList(Long docId) throws WorkbenchException {
		List<EmailDbData> emailDbDataList = emailDataAccess.getOutboundEmailList(docId);
		List<EmailResData> emailResDataList = new ArrayList<>();
		for (EmailDbData emailDbData : emailDbDataList) {
			encodeImagesToBase64(emailDbData);
		}
		for (EmailDbData source : emailDbDataList) {
			EmailResData target = new EmailResData();
			BeanUtils.copyProperties(source, target);
			emailResDataList.add(target);
		}
		for (EmailResData emailResData : emailResDataList) {
			long emailOutboundId = emailResData.getEmailOutboundId();
			List<AttachmentResData> attachmentList = attachProcess.getAttachmentListEmail(emailOutboundId);
			List<AttachmentResData> attachmentDataList = new ArrayList<>();
			List<AttachmentResData> inlineImageList = new ArrayList<AttachmentResData>();
			for (AttachmentResData attachments : attachmentList) {
				if (!attachments.isInlineImage()) {
					attachmentDataList.add(attachments);
				} else {
					inlineImageList.add(attachments);
				}

			}

			emailResData.setAttachmentDataList(attachmentDataList);
			emailResData.setInlineImageAttachmentDataList(inlineImageList);
		}
		return emailResDataList;
	}

	public EmailDbData getDraftEmailWithEncoding(Long docId, boolean isAppendCaseNumberInSubject, 
			String appendString) 
			throws WorkbenchException {
		EmailDbData emailDbData = getDraftEmail(docId);
		if (isAppendCaseNumberInSubject) {
			String emailSubject = emailDbData.getEmailSubject();
			String caseNumberToAppend = appendString + String.valueOf(docId);
			// Append case # to subject if not already on it (same case number)
			emailSubject = emailSubject.toUpperCase().indexOf(caseNumberToAppend) > 0 ? emailSubject
					: emailSubject + caseNumberToAppend;
			emailDbData.setEmailSubject(emailSubject);
		}
		encodeImagesToBase64(emailDbData);
		return emailDbData;
	}

	
	//TODO  sendEmail and saveDraftEmail have >50% code in common so can it be refactored
	public EntityDbData saveDraftEmail(InsertUpdateDraftReqData insertUpdateDraftReqData) throws WorkbenchException {
		EntityDbData outboundEmailEntityData = new EntityDbData();
		List<Long> emailIdList = new ArrayList<Long>();
		long emailOutboundId = 0;
		String errorMessage = "Invalid E-mail Id.";
		outboundEmailEntityData.setOutboundEmailMessage(errorMessage);
		EmailDbData draftEmail = getDraftEmail(insertUpdateDraftReqData.getDocId());

		EmailDbData emailDbData = new EmailDbData();
		if (insertUpdateDraftReqData.getEmailTo().length() > 0) {
			String formatEmailIdResult = PatternUtility.formatEmailId(insertUpdateDraftReqData.getEmailTo());
			if (formatEmailIdResult != errorMessage) {
				emailDbData.setEmailTo(formatEmailIdResult);
			} else {
				errorMessage = "Incorrect email id in To address. Please enter valid To address.";
				outboundEmailEntityData.setOutboundEmailMessage(errorMessage);
				return outboundEmailEntityData;
			}
		} else {
			errorMessage = "Please enter To address.";
			outboundEmailEntityData.setOutboundEmailMessage(errorMessage);
			return outboundEmailEntityData;
		}
		if (insertUpdateDraftReqData.getEmailCC().length() > 0) {
			String formatEmailCcIdResult = PatternUtility.formatEmailId(insertUpdateDraftReqData.getEmailCC());
			if (formatEmailCcIdResult != errorMessage) {
				emailDbData.setEmailCC(formatEmailCcIdResult);
			} else {
				errorMessage = "Incorrect email id in Cc address. Please enter valid Cc address.";
				outboundEmailEntityData.setOutboundEmailMessage(errorMessage);
				return outboundEmailEntityData;
			}
		} else {
			emailDbData.setEmailCC(insertUpdateDraftReqData.getEmailCC());
		}
		emailDbData.setEmailSubject(insertUpdateDraftReqData.getEmailSubject());
		emailDbData.setEmailBCC(insertUpdateDraftReqData.getEmailBCC());

		// Converting Base64 String to Image
		String imageBase64 = insertUpdateDraftReqData.getEmailBodyHtml();
		if (imageBase64 != null) {
			emailDbData.setEmailBodyHtml(decodeBase64Images(imageBase64));
		} else {
			emailDbData.setEmailBodyHtml(imageBase64);
		}

		emailDbData.setDocId(insertUpdateDraftReqData.getDocId());
		emailDbData.setTaskStatusCde(EnumTaskStatus.UNDEFINED.getValue());
		// Check if draft email is not pseudo
		if (draftEmail != null && draftEmail.getEmailOutboundId() != 0) {
			emailDbData.setEmailOutboundId(draftEmail.getEmailOutboundId());
			emailDbData.setTaskStatusCde(draftEmail.getTaskStatusCde());
		}

		// If draft email is pseudo then insert new
		if (emailDbData.getEmailOutboundId() == 0) {
			emailOutboundId = emailDataAccess.insertDraftEmail(emailDbData);
			outboundEmailEntityData.setDraft(false);

		} else {
			List<String> draftFilesList = encodeImagesToBase64(draftEmail);
			// To remove duplicate (or) old draft Image files.
			for (int i = 0; i < draftFilesList.size(); i++) {
				String filePath = FileUtility.getConcatenatedName(attachmentFilePath, draftFilesList.get(i));
				FileUtility.deleteFile(filePath);
			}
			emailOutboundId = emailDataAccess.updateDraftEmail(emailDbData);
			outboundEmailEntityData.setDraft(true);
		}
		if (emailOutboundId > 0) {
			emailIdList.add(emailOutboundId);
		}
		outboundEmailEntityData.setEmailIdList(emailIdList);
		outboundEmailEntityData.setOutboundEmailMessage("Draft saved successfully.");
		return outboundEmailEntityData;
	}

	public long deleteEmail(long emailId) throws WorkbenchException {
		return emailDataAccess.deleteEmail(emailId);
	}

	public PaginationResData getOutboundEmailCountByTaskStatus(int taskStatusCde, int pageNumber)
			throws WorkbenchException {
		long totalCount = 0;
		{
			// Make the call to Data Layer with inputs provided by caller
			totalCount = emailDataAccess.getTotalEmailCount(taskStatusCde);
		}
		int totalPages = 0;
		int currentPage = 0;
		if (totalCount <= 0) {
			currentPage = 0;
			totalPages = 0;
		} else {
			double total = (totalCount * 1.0) / pageSize;
			totalPages = (int) Math.ceil(total);
			currentPage = pageNumber;
			if (totalCount <= pageSize || currentPage < 1) {
				if (currentPage <= totalPages) {
					currentPage = 1;
				}
			}
		}
		PaginationResData paginationResData = new PaginationResData();
		paginationResData.setCurrentPageNumber(currentPage);
		paginationResData.setTotalPageCount(totalPages);
		return paginationResData;
	}

	public List<EmailResData> getOutboundEmailListByTaskStatus(int taskStatusCde, int pageNumber)
			throws WorkbenchException {
		List<EmailDbData> emailDbDataList = emailDataAccess.getOutboundEmailListByTaskStatus(taskStatusCde, pageNumber);
		List<EmailResData> emailResDataList = new ArrayList<EmailResData>();
		for (EmailDbData source : emailDbDataList) {
			EmailResData target = new EmailResData();
			BeanUtils.copyProperties(source, target);
			emailResDataList.add(target);
		}
		for (EmailResData emailResData : emailResDataList) {
			long emailOutboundId = emailResData.getEmailOutboundId();
			List<AttachmentResData> attachmentDbDataList = attachProcess.getAttachmentListEmail(emailOutboundId);
			List<AttachmentResData> inlineImageList = new ArrayList<AttachmentResData>();
			List<AttachmentResData> attachmentDataList = new ArrayList<AttachmentResData>();
			for (AttachmentResData attachmentResponseData : attachmentDbDataList) {
				if (attachmentResponseData.isInlineImage()) {
					inlineImageList.add(attachmentResponseData);
				} else {
					attachmentDataList.add(attachmentResponseData);
				}
			}
			emailResData.setAttachmentDataList(attachmentDataList);
			emailResData.setInlineImageAttachmentDataList(inlineImageList);
		}
		return emailResDataList;
	}

	public EntityDbData updateEmailTaskStatus(UpdateEmailStatusReqData updateEmailStatusReqData)
			throws WorkbenchException {
		EntityDbData emailEntityData = new EntityDbData();
		int attachmentCount = 0;
		emailEntityData = emailDataAccess.updateEmailTaskStatus(updateEmailStatusReqData.getEmailOutboundId(),
				updateEmailStatusReqData.getTaskStatusCde());
		List<AttachmentResData> attachmentList = attachProcess
				.getAttachmentListEmail(updateEmailStatusReqData.getEmailOutboundId());

		for (AttachmentResData attachments : attachmentList) {
			if (!attachments.isInlineImage()) {
				attachmentCount += 1;
			}
		}
		emailEntityData.setDraft(true);
		emailEntityData.setAttachmentCount(attachmentCount);
		return emailEntityData;
	}

	private String decodeBase64Images(String htmlContent) {
		List<String> uniqueList = new ArrayList<>();
		List<String> base64StringList = PatternUtility.getHtmlImgSrcValues(htmlContent);
		for (String base64String : base64StringList) {
			if (!uniqueList.isEmpty() && uniqueList.contains(base64String))
				continue;
			String[] strings = base64String.split(",");
			String fileExtension = StringUtility.getStringExtension1(strings[0]);
			String fileName = UUID.randomUUID().toString() + "." + fileExtension;
			String filePath = FileUtility.getConcatenatedName(attachmentFilePath, fileName);
			if (FileUtil.createImageFile(filePath, strings[1]))
				htmlContent = htmlContent.replace(base64String, fileName);
			uniqueList.add(base64String);

		}
		return htmlContent;
	}

	// To Store Inline attachment for send mail methods.
	private void storeInlineAttachment(String emailBodyHtml, long emailOutboundId) throws WorkbenchException {
		List<String> fileList = PatternUtility.getHtmlImgSrcValues(emailBodyHtml);
		List<Long> attachmentIdList = new ArrayList<>();
		for (String fileName : fileList) {
			AttachmentDbData attachmentDbData = new AttachmentDbData();
			attachmentDbData.setLogicalName(fileName);
			attachmentDbData.setPhysicalName(fileName);
			attachmentDbData.setInlineImage(true);
			attachmentDbData.setGroupName(StringUtility.getUniqueString());
			attachmentDbData.setExtractTypeCde(EnumExtractType.DIRECT_COPY.getValue());
			attachmentDbData.setSequenceNum(WorkbenchConstants.SEQUENCE_NUM);
			attachmentIdList.add(attachmentDataAccess.addAttachment(attachmentDbData));
		}
		if (attachmentIdList != null && attachmentIdList.size() > 0) {
			attachmentDataAccess.addEmailOutboundAttachmentRel(attachmentIdList, emailOutboundId);
		}
	}

	private EmailDbData getDraftEmail(Long docId) throws WorkbenchException {
		EmailDbData emailDbData = emailDataAccess.getDraftEmail(docId);
		if (emailDbData == null) {
			// Generate a pseudo template email with all known fields populated
			List<AttributeDbData> attributeDbDataList = attributeDataAccess.getDocumentAttributes(docId);
			attributeDbDataList.addAll(attributeDataAccess.getAttachmentAttributes(docId, ""));
			String toId = "";
			String subject = "";
			String ccId = "";
			String fromId = "";
			String bccId = "";
			String emailBodyHtml = "";
			String toAddressId="";
			boolean isCCExist = false;
			boolean isDbContentHtmlExist = false;
			boolean isDbContentExist = false;
			boolean isFileSystemContentExpected = false; 
			String trailingEmailText = "From: <<FromId>>\r\nSent: <<ReceivedDate>>\r\nTo: <<To Address Id>>\r\n";

			for (AttributeDbData docAttributeData : attributeDbDataList) {
				if (docAttributeData.getAttrNameCde() == EnumSystemAttributeName.FROM_ID.getCde()) { // 20=FromId
					toId += docAttributeData.getAttrValue() + ";";
				}
				if (docAttributeData.getAttrNameCde() == EnumSystemAttributeName.TO_ADDRESS_ID.getCde()) { // 5=To
																											// AddressId
					toAddressId=docAttributeData.getAttrValue();																						// Id
					toId += docAttributeData.getAttrValue() + ";";
				}
				if (docAttributeData.getAttrNameCde() == EnumSystemAttributeName.SUBJECT.getCde()) {// 3=subject
					subject = docAttributeData.getAttrValue();
					// Prefix with Reply indicator if not already present
					if (StringUtility.hasValue(subject)) {
						subject = subject.toUpperCase().indexOf(WorkbenchConstants.EMAIL_REPLY_SUBJECT_PREFIX) == 0
								? subject
								: WorkbenchConstants.EMAIL_REPLY_SUBJECT_PREFIX + subject;
					} else {
						subject = WorkbenchConstants.EMAIL_REPLY_SUBJECT_PREFIX;
					}
				}
				if (docAttributeData.getAttrNameCde() == EnumSystemAttributeName.CC_ADDRESS_ID.getCde()) { // 7=CC
																											// Addres Id
					ccId += docAttributeData.getAttrValue();
				}
				if (docAttributeData.getAttrNameCde() == EnumSystemAttributeName.FROM_ID.getCde()) { // 20=FromId
					fromId += docAttributeData.getAttrValue();
				}
				/*
				 * if (docAttributeData.getAttrNameCde() == 28) { bccId +=
				 * docAttributeData.getAttrValue(); }
				 */
				if (docAttributeData.getAttrNameCde() == EnumSystemAttributeName.CC_ADDRESS_ID.getCde()
						&& docAttributeData.getAttrValue().length() > 0 && docAttributeData.getAttrValue() != null) {
					isCCExist = true;
				}
				if (docAttributeData.getAttrNameCde() == EnumSystemAttributeName.CONTENT_HTML.getCde()
						&& docAttributeData.getAttrValue().length() > 0 && docAttributeData.getAttrValue() != null) {
					isDbContentHtmlExist = true;
				}
				if (docAttributeData.getAttrNameCde() == EnumSystemAttributeName.CONTENT.getCde()
						&& docAttributeData.getAttrValue().length() > 0 && docAttributeData.getAttrValue() != null) {
					isDbContentExist = true;
				}
			}
			if (isCCExist) {
				trailingEmailText += "Cc: <<CC Address Id>>\r\nSubject: <<Subject>>\r\n";
			} else {
				trailingEmailText += "Subject: <<Subject>>\r\n";
			}
			if (isDbContentHtmlExist) {
				trailingEmailText += "\n" + WorkbenchConstants.PLACEHOLDER_CONTENT_HTML;
			} else if (isDbContentExist) {
				trailingEmailText += WorkbenchConstants.PLACEHOLDER_CONTENT_TXT;
			} else {
				isFileSystemContentExpected = true;
				trailingEmailText += WorkbenchConstants.CONTENT_FROM_FILE_SYSTEM;
			}
			emailBodyHtml = "\n\n" + "-------------------------------------------------" + "\n" + trailingEmailText;
			for (AttributeDbData docAttributeData : attributeDbDataList) {
				if ((emailBodyHtml.toLowerCase()).contains(docAttributeData.getAttrNameTxt().toLowerCase())) {

					emailBodyHtml = emailBodyHtml.replace(("<<" + docAttributeData.getAttrNameTxt() + ">>"),
							docAttributeData.getAttrValue().toString());
				}

			}

			List<AttachmentDbData> attachmentDbDataList = attachmentDataAccess.getDocAttachmentList(docId);
			
			if (isFileSystemContentExpected) {
				AttachmentDbData attachmentDbData = AttachmentHelper.getEmailAttachment(attachmentDbDataList);
				String fileSystemContent = "";
				if (attachmentDbData != null) {
					attachmentDbData = attachProcess.getDocAttachmentFile(docId, attachmentDbData.getAttachmentId());
					byte[] contents = FileUtility.readFile(attachmentDbData.getPhysicalPath());
					fileSystemContent = new String(contents, StandardCharsets.UTF_8); // for UTF-8 encoding
				}
				emailBodyHtml = emailBodyHtml.replace(WorkbenchConstants.CONTENT_FROM_FILE_SYSTEM, fileSystemContent);
			}
			
			// Converting Inline image logical name to physical name
			List<String> fileList = PatternUtility.getHtmlImgSrcValues(emailBodyHtml);
			for (String fileName : fileList) {
				logger.debug("file name " + fileName);
				for (AttachmentDbData attachmentDbData : attachmentDbDataList) {
					if (attachmentDbData.isInlineImage()) {
						if (fileName.equalsIgnoreCase(attachmentDbData.getLogicalName())) {
							emailBodyHtml = emailBodyHtml.replace(fileName, attachmentDbData.getPhysicalName());
						}
					}
				}
			}

			toId = toId.substring(0, toId.length() - 1);
			String[] toIdList = toId.split(";");
			String updatedTo = "";
			for (String to : toIdList) {
				if (!to.equals(mailDownloaderId) && !to.equals(toAddressId)) {
					updatedTo += to + ";";
				}
			}
			if (updatedTo.length() == 0) {
				updatedTo += fromId + ";";
			}
			updatedTo = updatedTo.substring(0, updatedTo.length() - 1);
			emailDbData = new EmailDbData();
			emailDbData.setDocId(docId);
			emailDbData.setEmailSubject(subject);
			emailDbData.setEmailTo(updatedTo);
			emailDbData.setEmailCC(ccId);
			emailDbData.setEmailBCC(bccId);

			// Converting normal text to HTML text.
			emailBodyHtml = StringUtility.replaceNewLineCharacters(emailBodyHtml);
			// To avoid <html> tags in between the emailBodyHtml due to ContentHtml
			// Attribute.
			emailBodyHtml = emailBodyHtml.replace("<html>", "").replace("</html>", "");
			emailBodyHtml = "<html>" + emailBodyHtml + "</html>";
			emailDbData.setEmailBodyHtml(emailBodyHtml);
		}
		return emailDbData;
	}

	private List<String> encodeImagesToBase64(EmailDbData emailDbData) {
		List<String> draftFilesList = new ArrayList<>();
		String imageBase64 = emailDbData.getEmailBodyHtml();
		if (imageBase64 != null) {
			List<String> fileList = PatternUtility.getHtmlImgSrcValues(imageBase64);
			String filePath = null;
			for (String fileName : fileList) {
				draftFilesList.add(fileName);
				String[] strings = fileName.split("\\.");
				String fileExtension = StringUtility.getBase64Extension1(strings[1]);
				String base64String = "";
				try {
					// Reading a Image file from file system and convert it into Base64 String
					filePath = FileUtility.getConcatenatedName(attachmentFilePath, fileName);
					byte[] imageData = FileUtility.readFile(filePath);
					base64String = fileExtension + "," + Base64.getEncoder().encodeToString(imageData);
					imageBase64 = imageBase64.replace(fileName, base64String);
				} catch (Exception e) {
					logger.error("Exception while reading the Image " + e);
				}
			}
			emailDbData.setEmailBodyHtml(imageBase64);
		}
		return draftFilesList;
	}

}
