/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.db;

public class EmailDbData {
	private long docId;
	private String emailBCC;
	private String emailTo;
	private String emailFrom;
	private String emailFromId;
	private String emailBodyText;
	private String emailBodyHtml;
	private String emailSubject;
	private String emailCC;
	private String emailSentDtm;
    private long taskStatusCde;
	private long emailOutboundId;
    private String createByUserLoginId;
    private String createByUserFullName;
    private int createByUserTypeCde;
    private String createByUserTypeTxt;

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

	public String getEmailBCC() {
		return emailBCC;
	}

	public void setEmailBCC(String emailBCC) {
		this.emailBCC = emailBCC;
	}

	public String getEmailFrom() {
		return emailFrom;
	}

	public void setEmailFrom(String emailFrom) {
		this.emailFrom = emailFrom;
	}
	public String getEmailFromId() {
		return emailFromId;
	}

	public void setEmailFromId(String emailFromId) {
		this.emailFromId = emailFromId;
	}
	
	public String getEmailTo() {
		return emailTo;
	}

	public void setEmailTo(String emailTo) {
		this.emailTo = emailTo;
	}

	public String getEmailBodyText() {
		return emailBodyText;
	}

	public void setEmailBodyText(String emailBodyText) {
		this.emailBodyText = emailBodyText;
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

	public String getEmailCC() {
		return this.emailCC;
	}

	public void setEmailCC(String emailCC) {
		this.emailCC = emailCC;
	}

	public void setEmailSentDtm(String emailSentDtm) {
		this.emailSentDtm = emailSentDtm;
	}

	public String getEmailSentDtm() {
		return this.emailSentDtm;
	}
	
    public long getTaskStatusCde() {
        return taskStatusCde;
    }

    public void setTaskStatusCde(long taskStatusCde) {
        this.taskStatusCde = taskStatusCde;
    }

	public String getCreateByUserLoginId() {
		return createByUserLoginId;
	}

	public void setCreateByUserLoginId(String createByUserLoginId) {
		this.createByUserLoginId = createByUserLoginId;
	}

	public int getCreateByUserTypeCde() {
		return createByUserTypeCde;
	}

	public void setCreateByUserTypeCde(int createByUserTypeCde) {
		this.createByUserTypeCde = createByUserTypeCde;
	}

	public String getCreateByUserTypeTxt() {
		return createByUserTypeTxt;
	}

	public void setCreateByUserTypeTxt(String createByUserTypeTxt) {
		this.createByUserTypeTxt = createByUserTypeTxt;
	}

	public String getCreateByUserFullName() {
		return createByUserFullName;
	}

	public void setCreateByUserFullName(String createByUserFullName) {
		this.createByUserFullName = createByUserFullName;
	}
}
