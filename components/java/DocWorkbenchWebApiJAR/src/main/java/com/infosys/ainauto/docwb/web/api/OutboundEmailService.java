/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.web.api;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.infosys.ainauto.commonutils.FileUtility;
import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.docwb.web.common.DocwbWebConstants;
import com.infosys.ainauto.commonutils.HttpClientBase;
import com.infosys.ainauto.docwb.web.common.PropertyManager;
import com.infosys.ainauto.docwb.web.data.AttachmentData;
import com.infosys.ainauto.docwb.web.data.EmailAddressData;
import com.infosys.ainauto.docwb.web.data.EmailData;
import com.infosys.ainauto.docwb.web.exception.DocwbWebException;
import com.infosys.ainauto.docwb.web.type.EnumTaskStatus;

public class OutboundEmailService extends HttpClientBase implements IOutboundEmailService {
	private static Logger logger = LoggerFactory.getLogger(OutboundEmailService.class);

	private static final String PROP_NAME_API_GET_EMAIL = "docwb.api.email.url";
	private static final String PROP_NAME_API_UPDATE_EMAIL_STATUS = "docwb.api.email.status.url";

	private static final String TASK_STATUS_CDE = "taskStatusCde";
	private static final String DOC_ID = "docId";
	private static final String EMAIL_OUTBOUND_ID = "emailOutboundId";

	private static final String EMAIL_BODY_TEXT = "emailBodyText";
	private static final String EMAIL_BODY_HTML = "emailBodyHtml";
	private static final String EMAIL_SUBJECT = "emailSubject";
	private static final String EMAIL_TO = "emailTo";
	private static final String EMAIL_CC = "emailCC";
	private static final String EMAIL_BCC = "emailBCC";
	
	private static final String FILE_CONTENT_ID_EMAIL_DATA = "emailData";
	private static final String FILE_CONTENT_ID_FILE = "file";

	private static final String PROP_NAME_API_SEND_EMAIL_URL = "docwb.api.send.email.url";
	private String emailApiUrl = "";
	private String updateEmailStatusApiUrl = "";
	private String draftEmailUrl = "";
	private String outboundAttachmentUrl = "";

	private static final String PROP_NAME_API_DRAFT_EMAIL_URL = "docwb.api.email.draft.url";
	private static final String PROP_NAME_GET_OUTBOUND_ATTACHMENT_FILE_URL = "docwb.api.attachment.email.file.url";

	private static final String PROP_NAME_ATTACHMENT_PATH = "docwb.temp.path";
	private String attachmentPath = "";

	public OutboundEmailService(HttpClientBase.Authentication.BearerAuthenticationConfig bearerAuthConfig) {
		super(null, bearerAuthConfig);
		emailApiUrl = PropertyManager.getInstance().getProperty(PROP_NAME_API_GET_EMAIL);
		updateEmailStatusApiUrl = PropertyManager.getInstance().getProperty(PROP_NAME_API_UPDATE_EMAIL_STATUS);
		draftEmailUrl = PropertyManager.getInstance().getProperty(PROP_NAME_API_DRAFT_EMAIL_URL);
		outboundAttachmentUrl = PropertyManager.getInstance().getProperty(PROP_NAME_GET_OUTBOUND_ATTACHMENT_FILE_URL);
		attachmentPath = PropertyManager.getInstance().getProperty(PROP_NAME_ATTACHMENT_PATH);
	}

	public List<EmailData> getOutboundEmailList(EnumTaskStatus taskStatusCde, String attachmentSaveFolder)
			throws DocwbWebException {
		String url = "";
		try {

			URIBuilder uriBuilder = new URIBuilder(emailApiUrl);
			uriBuilder.addParameter(TASK_STATUS_CDE, String.valueOf(taskStatusCde.getValue()));
			url = uriBuilder.build().toURL().toString();
		} catch (Exception e) {
			logger.error("Error occurred in getCategoryConfidence", e);
		}

		JsonObject jsonResponse = (JsonObject) executeHttpCall(HttpCallType.GET, url);
		List<EmailData> emailDataList = new ArrayList<EmailData>();
		if (jsonResponse != null) {
			JsonArray emailDataJson = jsonResponse.getJsonArray("response");
			for (int i = 0; i < emailDataJson.size(); i++) {
				try {
					JsonObject valuesObject = emailDataJson.getJsonObject(i);
					EmailData emailData = new EmailData();
					emailData.setDocId(valuesObject.getInt("docId"));
					emailData.setEmailBodyHtml(valuesObject.getString("emailBodyHtml", null));
					emailData.setEmailBodyText(valuesObject.getString("emailBodyText", null));
					if (valuesObject.getString("emailTo") != null && valuesObject.getString("emailTo").length() > 0) {
						List<EmailAddressData> emailAddressDataList = new ArrayList<>();
						String[] to = valuesObject.getString("emailTo").split(";");
						for (String toAddress : to) {
							EmailAddressData emailAddressData = new EmailAddressData(toAddress, "");
							emailAddressDataList.add(emailAddressData);

						}
						emailData.setEmailAddressToList(emailAddressDataList);

					}
					emailData.setEmailOutboundId(valuesObject.getInt("emailOutboundId"));
					emailData.setEmailSubject(valuesObject.getString("emailSubject"));
					if (valuesObject.getString("emailCC") != null && valuesObject.getString("emailCC").length() > 0) {
						String[] cC = valuesObject.getString("emailCC").split(";");
						List<EmailAddressData> emailAddressCcDataList = new ArrayList<>();
						for (String ccId : cC) {
							EmailAddressData emailCcAddressData = new EmailAddressData(ccId, "");
							emailAddressCcDataList.add(emailCcAddressData);
						}
						emailData.setEmailAddressCcList(emailAddressCcDataList);

					}
					if (valuesObject.getString("emailBCC") != null && valuesObject.getString("emailBCC").length() > 0) {
						String[] cC = valuesObject.getString("emailBCC").split(";");
						List<EmailAddressData> emailAddressBccDataList = new ArrayList<>();
						for (String ccId : cC) {
							EmailAddressData emailCcAddressData = new EmailAddressData(ccId, "");
							emailAddressBccDataList.add(emailCcAddressData);
						}
						emailData.setEmailAddressBccList(emailAddressBccDataList);

					}
					List<AttachmentData> attachmentDatList = new ArrayList<AttachmentData>();
					JsonArray attachmentDataJsonArray = valuesObject.getJsonArray("attachmentDataList");
					if (attachmentDataJsonArray != null && attachmentDataJsonArray.size() > 0) {
						for (int j = 0; j < attachmentDataJsonArray.size(); j++) {
							JsonObject attachmentDataJsonObject = attachmentDataJsonArray.getJsonObject(j);
							int attachmentId = attachmentDataJsonObject.getInt("attachmentId");
							String outboundFileUrl = outboundAttachmentUrl + "?emailOutboundId="
									+ emailData.getEmailOutboundId() + "&attachmentId=" + attachmentId;
							HttpClientBase.HttpFileResponseData httpFileResData = executeGetAttachmentCall(outboundFileUrl,
									attachmentSaveFolder);
							if (httpFileResData!=null) {
								AttachmentData attachmentData = new AttachmentData();
								attachmentData.setLogicalName(httpFileResData.getFileName());
								attachmentData.setPhysicalName(httpFileResData.getFilePhysicalName());
								attachmentData.setPhysicalPath(httpFileResData.getFilePhysicalPath());
								
								attachmentDatList.add(attachmentData);
							}
						}

					}

					emailData.setAttachmentDataList(attachmentDatList);

					List<AttachmentData> inLineattachmentDatList = new ArrayList<AttachmentData>();
					JsonArray inLineattachmentDataJsonArray = valuesObject
							.getJsonArray("inlineImageAttachmentDataList");
					if (inLineattachmentDataJsonArray != null && inLineattachmentDataJsonArray.size() > 0) {
						for (int j = 0; j < inLineattachmentDataJsonArray.size(); j++) {
							JsonObject inLineattachmentDataJsonObject = inLineattachmentDataJsonArray.getJsonObject(j);
							int attachmentId = inLineattachmentDataJsonObject.getInt("attachmentId");
							String outboundFileUrl = outboundAttachmentUrl + "?emailOutboundId="
									+ emailData.getEmailOutboundId() + "&attachmentId=" + attachmentId;
							HttpClientBase.HttpFileResponseData httpFileResData = executeGetAttachmentCall(outboundFileUrl,
									attachmentSaveFolder);
							if (httpFileResData!=null) {
								AttachmentData attachmentData = new AttachmentData();
								attachmentData.setLogicalName(httpFileResData.getFileName());
								attachmentData.setPhysicalName(httpFileResData.getFilePhysicalName());
								attachmentData.setPhysicalPath(httpFileResData.getFilePhysicalPath());
								
								inLineattachmentDatList.add(attachmentData);
							}
						}

					}
					emailData.setInlineImageAttachmentDataList(inLineattachmentDatList);
					emailDataList.add(emailData);
				} catch (Exception e) {
					logger.error("Error occurred while iterating through email list", e);
				}
			}
		}

		return emailDataList;
	}

	public boolean addOutboundEmail(EmailData emailData) {
		JsonObjectBuilder jsonRequestBuilder = Json.createObjectBuilder();
		jsonRequestBuilder.add(DOC_ID, emailData.getDocId());
		jsonRequestBuilder.add(EMAIL_BODY_TEXT, emailData.getEmailBodyText());
		jsonRequestBuilder.add(EMAIL_BODY_HTML, emailData.getEmailBodyHtml());
		jsonRequestBuilder.add(EMAIL_SUBJECT, emailData.getEmailSubject());

		String emailToId = "";
		List<EmailAddressData> emailToAddressList = emailData.getEmailAddressToList();
		if (emailToAddressList != null && emailToAddressList.size() > 0) {
			for (EmailAddressData emailAddressData : emailToAddressList) {
				emailToId += emailAddressData.getEmailId() + ";";
			}
			emailToId = emailToId.substring(0, emailToId.length() - 1);
		}
		jsonRequestBuilder.add(EMAIL_TO, emailToId);

		String emailCcId = "";
		List<EmailAddressData> emailCcAddressList = emailData.getEmailAddressCcList();
		if (emailCcAddressList != null && emailCcAddressList.size() > 0) {
			for (EmailAddressData emailAddressData : emailCcAddressList) {
				emailCcId += emailAddressData.getEmailId() + ";";
			}
			emailCcId = emailCcId.substring(0, emailCcId.length() - 1);
		}
		jsonRequestBuilder.add(EMAIL_CC, emailCcId);

		String emailBccId = "";
		List<EmailAddressData> emailBccAddressList = emailData.getEmailAddressBccList();
		if (emailBccAddressList != null && emailBccAddressList.size() > 0) {
			for (EmailAddressData emailAddressData : emailBccAddressList) {
				emailBccId += emailAddressData.getEmailId() + ";";
			}
			emailBccId = emailBccId.substring(0, emailBccId.length() - 1);
		}
		jsonRequestBuilder.add(EMAIL_BCC, emailBccId);

		JsonObject jsonRequest = jsonRequestBuilder.build();
		String tempFileName = UUID.randomUUID().toString() + DocwbWebConstants.OUTBOUND_EMAIL_JSON_FILE_EXTENSION;
		String tempFilePath = FileUtility.getConcatenatedName(attachmentPath, tempFileName);
		boolean isSuccess = FileUtility.saveFile(tempFilePath, jsonRequest.toString());
		if (!isSuccess) {
			logger.error("Error occured in creating JSON File");
			return false;
		}

		List<HttpFileRequestData> httpFileDataList = new ArrayList<>();
		httpFileDataList.add(new HttpFileRequestData(tempFileName, tempFilePath,
				FILE_CONTENT_ID_EMAIL_DATA, "application/json"));

		List<AttachmentData> attachmentDataList = emailData.getAttachmentDataList();
		if (ListUtility.hasValue(attachmentDataList)) {
			int fileCount = attachmentDataList.size();
			if (fileCount <= DocwbWebConstants.OUTBOUND_EMAIL_ATTACHMENT_FILE_COUNT) {
				int i = 1;
				for (AttachmentData attachmentData1 : attachmentDataList) {
					httpFileDataList
							.add(new HttpFileRequestData(attachmentData1.getPhysicalName(), attachmentData1.getPhysicalPath(),
									FILE_CONTENT_ID_FILE + i++, "application/json"));
				}
			} else {
				logger.error("Attachment count exceeds 5. Reduce the count and try again");
				return false;
			}
		}
		executePostAttachmentWithAuthCall(PropertyManager.getInstance().getProperty(PROP_NAME_API_SEND_EMAIL_URL),
				httpFileDataList);
		if (FileUtility.doesFileExist(tempFilePath))
			FileUtility.deleteFile(tempFilePath);
		return true;
	}

	public void updateOutboundEmailStatus(long emailOutboundId, EnumTaskStatus taskStatus) {
		JsonObjectBuilder jsonRequestBuilder = Json.createObjectBuilder();
		jsonRequestBuilder.add(EMAIL_OUTBOUND_ID, emailOutboundId);
		jsonRequestBuilder.add(TASK_STATUS_CDE, taskStatus.getValue());

		JsonObject jsonRequest = jsonRequestBuilder.build();
		executeHttpCall(HttpCallType.PUT, updateEmailStatusApiUrl, jsonRequest);

	}

	public EmailData getEmailDraft(long docId) throws DocwbWebException {
		String url = "";
		try {

			URIBuilder uriBuilder = new URIBuilder(draftEmailUrl);
			uriBuilder.addParameter(DOC_ID, String.valueOf(docId));
			url = uriBuilder.build().toURL().toString();
		} catch (Exception e) {
			logger.error("Error occurred in getCategoryConfidence", e);
		}
		EmailData emailData = new EmailData();
		JsonObject jsonResponse = (JsonObject) executeHttpCall(HttpCallType.GET, url);
		if (jsonResponse != null) {
			JsonObject emailDataJson = jsonResponse.getJsonObject("response");
			emailData.setDocId(emailDataJson.getInt("docId"));
			if (emailDataJson.getString("emailTo") != null && emailDataJson.getString("emailTo").length() > 0) {
				List<EmailAddressData> emailAddressDataList = new ArrayList<>();
				String[] to = emailDataJson.getString("emailTo").split(";");
				for (String toAddress : to) {
					EmailAddressData emailAddressData = new EmailAddressData(toAddress, "");
					emailAddressDataList.add(emailAddressData);
				}
				emailData.setEmailAddressToList(emailAddressDataList);
			}
			emailData.setEmailSubject(emailDataJson.getString("emailSubject"));
			if (emailDataJson.getString("emailCC") != null && emailDataJson.getString("emailCC").length() > 0) {
				String[] cC = emailDataJson.getString("emailCC").split(";");
				List<EmailAddressData> emailAddressCcDataList = new ArrayList<>();
				for (String ccId : cC) {
					EmailAddressData emailCcAddressData = new EmailAddressData(ccId, "");
					emailAddressCcDataList.add(emailCcAddressData);
				}
				emailData.setEmailAddressCcList(emailAddressCcDataList);
			}
			if (emailDataJson.getString("emailBCC") != null && emailDataJson.getString("emailBCC").length() > 0) {
				String[] cC = emailDataJson.getString("emailBCC").split(";");
				List<EmailAddressData> emailAddressBccDataList = new ArrayList<>();
				for (String ccId : cC) {
					EmailAddressData emailCcAddressData = new EmailAddressData(ccId, "");
					emailAddressBccDataList.add(emailCcAddressData);
				}
				emailData.setEmailAddressBccList(emailAddressBccDataList);
			}
		}
		return emailData;
	}

}
