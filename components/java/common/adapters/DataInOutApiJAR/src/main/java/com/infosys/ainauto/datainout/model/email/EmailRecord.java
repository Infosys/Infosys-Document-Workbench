/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.datainout.model.email;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class EmailRecord {

	private Date emailDate;
	private String[] emailPriority;
	private boolean emailFlags;
	private String emailBodyText;
	private String emailBodyHtml;

	private String emailSubject;

	private EmailAddress emailAddressFrom;

	private List<EmailAddress> emailAddressToList;
	private List<EmailAddress> emailAddressCcList;
	private List<EmailAddress> emailAddressBccList;

	private List<AttachmentRecord> attachmentRecordList;
	private List<AttachmentRecord> inlineAttachmentRecordList;

	public Date getEmailDate() {
		return emailDate;
	}

	public void setEmailDate(Date emailDate) {
		this.emailDate = emailDate;
	}

	public void setFlags(boolean emailFlags) {
		this.emailFlags = emailFlags;
	}

	public boolean getFlags() {
		return this.emailFlags;
	}

	public void setPriority(String[] strings) {
		this.emailPriority = strings;
	}

	public String[] getPriority() {
		return this.emailPriority;
	}

	public String getEmailBodyText() {
		return emailBodyText;
	}

	public void setEmailBodyText(String emailBody) {
		this.emailBodyText = emailBody;
	}

	public String getEmailBodyHtml() {
		return emailBodyHtml;
	}

	public void setEmailBodyHtml(String emailBodyHtml) {
		this.emailBodyHtml = emailBodyHtml;
	}

	public String getEmailSubject() {
		return emailSubject;
	}

	public void setEmailSubject(String emailSubject) {
		this.emailSubject = emailSubject;
	}

	public List<AttachmentRecord> getAttachmentRecordList() {
		return attachmentRecordList;
	}

	public void setAttachmentRecordList(List<AttachmentRecord> attachmentRecordList) {
		this.attachmentRecordList = attachmentRecordList;
	}

	public EmailAddress getEmailAddressFrom() {
		return emailAddressFrom;
	}

	public void setEmailAddressFrom(EmailAddress emailAddressFrom) {
		this.emailAddressFrom = emailAddressFrom;
	}

	public List<EmailAddress> getEmailAddressToList() {
		return emailAddressToList;
	}

	public void setEmailAddressToList(List<EmailAddress> emailAddressToList) {
		this.emailAddressToList = emailAddressToList;
	}

	public List<EmailAddress> getEmailAddressCcList() {
		return emailAddressCcList;
	}

	public void setEmailAddressCcList(List<EmailAddress> emailAddressCcList) {
		this.emailAddressCcList = emailAddressCcList;
	}

	public List<EmailAddress> getEmailAddressBccList() {
		return emailAddressBccList;
	}

	public void setEmailAddressBccList(List<EmailAddress> emailAddressBccList) {
		this.emailAddressBccList = emailAddressBccList;
	}

	public List<AttachmentRecord> getInlineAttachmentRecordList() {
		return inlineAttachmentRecordList;
	}

	public void setInlineAttachmentRecordList(List<AttachmentRecord> inlineAttachmentRecordList) {
		this.inlineAttachmentRecordList = inlineAttachmentRecordList;
	}

	@Override
	public String toString() {
		return "EmailRecord [emailDate=" + emailDate + ", emailPriority=" + Arrays.toString(emailPriority)
				+ ", emailFlags=" + emailFlags + ", emailBodyText=" + emailBodyText + ", emailBodyHtml=" + emailBodyHtml
				+ ", emailSubject=" + emailSubject + ", emailAddressFrom=" + emailAddressFrom + ", emailAddressToList="
				+ emailAddressToList + ", emailAddressCcList=" + emailAddressCcList + ", emailAddressBccList="
				+ emailAddressBccList + ", attachmentRecordList=" + attachmentRecordList
				+ ", inlineAttachmentRecordList=" + inlineAttachmentRecordList + "]";
	}

}
