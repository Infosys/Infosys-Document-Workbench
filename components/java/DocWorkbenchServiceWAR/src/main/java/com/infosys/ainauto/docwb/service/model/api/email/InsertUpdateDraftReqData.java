/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.api.email;

public class InsertUpdateDraftReqData {

	private long docId;
	private String emailBCC;
	private String emailTo;
	private String emailBodyText;
	private String emailBodyHtml;
	private String emailSubject;
	private String emailCC;
/*	private long emailOutboundId;

	public long getEmailOutboundId() {
		return emailOutboundId;
	}

	public void setEmailOutboundId(long emailOutboundId) {
		this.emailOutboundId = emailOutboundId;
	}*/

	public long getDocId() {
		return docId;
	}

	public void setDocId(long docId) {
		this.docId = docId;
	}

	public String getEmailBCC() {
		return emailBCC;
	}

	public void setEmailBCC(String emailBCC) {
		this.emailBCC = emailBCC;
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
		return emailCC;
	}

	public void setEmailCC(String emailCC) {
		this.emailCC = emailCC;
	}

}
