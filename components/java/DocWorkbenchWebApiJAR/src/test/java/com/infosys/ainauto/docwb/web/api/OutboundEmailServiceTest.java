/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.web.api;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.infosys.ainauto.docwb.web.data.ActionTempMappingData;
import com.infosys.ainauto.docwb.web.data.AttachmentData;
import com.infosys.ainauto.docwb.web.data.EmailData;
import com.infosys.ainauto.docwb.web.type.EnumFileType;
import com.infosys.ainauto.docwb.web.type.EnumTaskStatus;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class OutboundEmailServiceTest {

	private static DocWbApiFactory docWbFactory = null;
	private static IOutboundEmailService outboundEmailService;
	private static ITemplateService templateService;
	private static IAttachmentService attachmentService;
	private static String tempDownloadPath = "D://TEMP";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		docWbFactory = new DocWbApiFactory("http://localhost:8080/docwbservice", "docwbengine", "docwbengine",
				"ae30c578-8569-4f86-be17-642ebaef2e52", true, tempDownloadPath,5, 5);
		docWbFactory.startServiceThreads();
		outboundEmailService = docWbFactory.getOutboundEmailService();
		templateService = docWbFactory.getTemplateService();
		attachmentService = docWbFactory.getAttachmentService();
	}

	@Test
	public void testGetOutboundEmailList() throws Exception {
		List<EmailData> emailDataList = outboundEmailService.getOutboundEmailList(EnumTaskStatus.YET_TO_START,
				tempDownloadPath);
		assertTrue(emailDataList.size() > 0);
	}

	@Test
	public void testGetAttachmentList() throws Exception {
		long docId = 1000164;
		List<AttachmentData> attachmentDataList = attachmentService.getAttachmentList(1000170, tempDownloadPath, EnumFileType.ALL);

		EmailData draftEmailData = outboundEmailService.getEmailDraft(docId);
		EmailData emailData = new EmailData();

		List<ActionTempMappingData> actionTemplateDataList = templateService.getFlattenedTemplates(docId);
		for (ActionTempMappingData actionTempMappingData : actionTemplateDataList) {
			if (actionTempMappingData.getIsRecommendedTemplate()) {
				emailData.setEmailBodyText(actionTempMappingData.getTemplateText());
				emailData.setEmailBodyHtml(actionTempMappingData.getTemplateHtml());
				emailData.setDocId(docId);
				emailData.setEmailAddressToList(draftEmailData.getEmailAddressToList());
				emailData.setEmailAddressCcList(draftEmailData.getEmailAddressCcList());
				emailData.setEmailAddressBccList(draftEmailData.getEmailAddressBccList());
				emailData.setEmailSubject(draftEmailData.getEmailSubject());
				// Add External attachments here for auto send email if necessary.
				emailData.setAttachmentDataList(attachmentDataList);

				boolean isSuccess = outboundEmailService.addOutboundEmail(emailData);

				assertTrue(isSuccess);
				break;
			}
		}
	}

	@AfterClass
	public static void tearDown() throws Exception {
		docWbFactory.stopServiceThreads();
	}
}
