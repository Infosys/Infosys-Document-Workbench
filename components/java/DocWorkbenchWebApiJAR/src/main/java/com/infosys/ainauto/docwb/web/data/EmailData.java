/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.web.data;

import java.util.Date;
import java.util.List;

public class EmailData {

	private long docId;

	private Date emailDate;
	private String emailBodyText;
	private String emailBodyHtml;

	private String emailSubject;

	private EmailAddressData emailAddressFrom;

	private List<EmailAddressData> emailAddressToList;
	private List<EmailAddressData> emailAddressCcList;
	private List<EmailAddressData> emailAddressBccList;

	private long emailOutboundId;

	private List<AttachmentData> attachmentDataList;
	private List<AttachmentData> inlineImageAttachmentDataList;

	private boolean isInlineImageExists;
	
	private String dataInputRecordId;

	public boolean isInlineImageExists() {
		return isInlineImageExists;
	}

	public void setInlineImageExists(boolean isInlineImageExists) {
		this.isInlineImageExists = isInlineImageExists;
	}

	public List<AttachmentData> getInlineImageAttachmentDataList() {
		return inlineImageAttachmentDataList;
	}

	public void setInlineImageAttachmentDataList(List<AttachmentData> inlineImageAttachmentDataList) {
		this.inlineImageAttachmentDataList = inlineImageAttachmentDataList;
	}

	public long getDocId() {
		return docId;
	}

	public void setDocId(long docId) {
		this.docId = docId;
	}

	public long getEmailOutboundId() {
		return emailOutboundId;
	}

	public void setEmailOutboundId(long emailOutboundId) {
		this.emailOutboundId = emailOutboundId;
	}

	public List<AttachmentData> getAttachmentDataList() {
		return attachmentDataList;
	}

	public void setAttachmentDataList(List<AttachmentData> attachmentDataList) {
		this.attachmentDataList = attachmentDataList;
	}

	public String getEmailBodyText() {
		return emailBodyText;
	}

	public void setEmailBodyText(String emailBodyText) {
		this.emailBodyText = emailBodyText;
	}

	public String getEmailSubject() {
		return emailSubject;
	}

	public void setEmailSubject(String emailSubject) {
		this.emailSubject = emailSubject;
	}

	public EmailAddressData getEmailAddressFrom() {
		return emailAddressFrom;
	}

	public void setEmailAddressFrom(EmailAddressData emailAddressFrom) {
		this.emailAddressFrom = emailAddressFrom;
	}

	public List<EmailAddressData> getEmailAddressToList() {
		return emailAddressToList;
	}

	public void setEmailAddressToList(List<EmailAddressData> emailAddressToList) {
		this.emailAddressToList = emailAddressToList;
	}

	public List<EmailAddressData> getEmailAddressCcList() {
		return emailAddressCcList;
	}

	public void setEmailAddressCcList(List<EmailAddressData> emailAddressCcList) {
		this.emailAddressCcList = emailAddressCcList;
	}

	public List<EmailAddressData> getEmailAddressBccList() {
		return emailAddressBccList;
	}

	public void setEmailAddressBccList(List<EmailAddressData> emailAddressBccList) {
		this.emailAddressBccList = emailAddressBccList;
	}

	public Date getEmailDate() {
		return emailDate;
	}

	public void setEmailDate(Date emailDate) {
		this.emailDate = emailDate;
	}

	public String getEmailBodyHtml() {
		return emailBodyHtml;
	}

	public void setEmailBodyHtml(String emailBodyHtml) {
		this.emailBodyHtml = emailBodyHtml;
	}
	
	public String getDataInputRecordId() {
		return dataInputRecordId;
	}

	public void setDataInputRecordId(String dataInputRecordId) {
		this.dataInputRecordId = dataInputRecordId;
	}
}
