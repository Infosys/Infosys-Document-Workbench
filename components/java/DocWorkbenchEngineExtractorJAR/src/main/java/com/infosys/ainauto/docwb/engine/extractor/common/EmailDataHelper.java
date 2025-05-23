/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.extractor.common;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.infosys.ainauto.datainout.model.DataInputRecord;
import com.infosys.ainauto.datainout.model.email.AttachmentRecord;
import com.infosys.ainauto.datainout.model.email.EmailAddress;
import com.infosys.ainauto.datainout.model.email.EmailRecord;
import com.infosys.ainauto.docwb.web.data.AttachmentData;
import com.infosys.ainauto.docwb.web.data.EmailAddressData;
import com.infosys.ainauto.docwb.web.data.EmailData;
import com.infosys.ainauto.docwb.web.type.EnumExtractType;

public class EmailDataHelper {

	public static EmailData convertDataInputRecordToEmailData(DataInputRecord dataInputRecord) {
		EmailData emailData = null;
		AttachmentData attachmentData;
		EmailRecord emailRecord = dataInputRecord.getEmailRecord();
		if (emailRecord != null) {
			emailData = new EmailData();
			emailData.setDataInputRecordId(dataInputRecord.getDataInputRecordId());
			// Copy all email data to return object
			BeanUtils.copyProperties(emailRecord, emailData);
			emailData.setEmailAddressFrom(new EmailAddressData("", ""));
			BeanUtils.copyProperties(emailRecord.getEmailAddressFrom(), emailData.getEmailAddressFrom());
			if (emailRecord.getEmailAddressToList() != null) {
				emailData.setEmailAddressToList(new ArrayList<EmailAddressData>());
				for (EmailAddress source : emailRecord.getEmailAddressToList()) {
					EmailAddressData target = new EmailAddressData("", "");
					BeanUtils.copyProperties(source, target);
					emailData.getEmailAddressToList().add(target);
				}
			}
			if (emailRecord.getEmailAddressCcList() != null) {
				emailData.setEmailAddressCcList(new ArrayList<EmailAddressData>());
				for (EmailAddress source : emailRecord.getEmailAddressCcList()) {
					EmailAddressData target = new EmailAddressData("", "");
					BeanUtils.copyProperties(source, target);
					emailData.getEmailAddressCcList().add(target);
				}

			}
			if (emailRecord.getEmailAddressBccList() != null) {
				emailData.setEmailAddressBccList(new ArrayList<EmailAddressData>());
				for (EmailAddress source : emailRecord.getEmailAddressBccList()) {
					EmailAddressData target = new EmailAddressData("", "");
					BeanUtils.copyProperties(source, target);
					emailData.getEmailAddressBccList().add(target);
				}

			}

			// Copy all attachment data to return object
			if (emailRecord.getAttachmentRecordList() != null
					&& emailRecord.getAttachmentRecordList().size() > 0) {
				List<AttachmentData> attachmentDataList = new ArrayList<>();
				for (AttachmentRecord attachmentRecord : emailRecord.getAttachmentRecordList()) {
					attachmentData = new AttachmentData();
					attachmentData.setLogicalName(attachmentRecord.getActualFileName());
					attachmentData.setPhysicalName(attachmentRecord.getStoredFileName());
					attachmentData.setPhysicalPath(attachmentRecord.getStoredFileFullPath());
					attachmentData.setExtractTypeCde(EnumExtractType.DIRECT_COPY.getValue());
					attachmentDataList.add(attachmentData);
				}
				emailData.setAttachmentDataList(attachmentDataList);
			}

			// Copy inline image attachements
			if (emailRecord.getInlineAttachmentRecordList() != null
					&& emailRecord.getInlineAttachmentRecordList().size() > 0) {
				List<AttachmentData> attachmentDataList = new ArrayList<>();
				for (AttachmentRecord attachmentRecord : emailRecord.getInlineAttachmentRecordList()) {
					attachmentData = new AttachmentData();
					attachmentData.setLogicalName(attachmentRecord.getActualFileName());
					attachmentData.setPhysicalName(attachmentRecord.getStoredFileName());
					attachmentData.setPhysicalPath(attachmentRecord.getStoredFileFullPath());
					attachmentData.setExtractTypeCde(EnumExtractType.DIRECT_COPY.getValue());
					attachmentDataList.add(attachmentData);
				}
				emailData.setInlineImageAttachmentDataList(attachmentDataList);
			}
		}
		return emailData;
	}
}
