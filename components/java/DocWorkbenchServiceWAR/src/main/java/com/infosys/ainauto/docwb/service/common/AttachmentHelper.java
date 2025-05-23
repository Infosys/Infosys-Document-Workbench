/** =============================================================================================================== *
 * Copyright 2023 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.common;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.docwb.service.model.db.AttachmentDbData;

public class AttachmentHelper {

	public static AttachmentDbData getEmailAttachment(List<AttachmentDbData> attachmentDbDataList) {
		// Create a list of first item of each group if its name is "EmailBody.html" OR
		// "EmailBody.txt"
		// From this list, get the latest record using latest attachmentId
		String[] emailFileTypes = { WorkbenchConstants.EMAIL_BODY_HTML_STANDARD_FILE_NAME,
				WorkbenchConstants.EMAIL_BODY_TXT_STANDARD_FILE_NAME };
		Map<String, List<AttachmentDbData>> groupAttachmentDbDataMap = attachmentDbDataList.stream()
				.collect(Collectors.groupingBy(AttachmentDbData::getGroupName));

		AttachmentDbData attachmentDbData = null;
		List<Long> emailBodyAttachmentIds = new ArrayList<>();
		for (String emailFileType : emailFileTypes) {
			for (Map.Entry<String, List<AttachmentDbData>> entry : groupAttachmentDbDataMap.entrySet()) {
				List<AttachmentDbData> groupAttachmentDbDataList = entry.getValue();
				AttachmentDbData firstInGroupAttachmentDbData = groupAttachmentDbDataList.stream()
						.sorted(Comparator.comparingLong(AttachmentDbData::getAttachmentId))
						.collect(Collectors.toList()).get(0);
				if (firstInGroupAttachmentDbData.getLogicalName().equalsIgnoreCase(emailFileType)) {
					emailBodyAttachmentIds.add(firstInGroupAttachmentDbData.getAttachmentId());
				}
			}
		}

		List<Long> emailAttachList= emailBodyAttachmentIds.stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
		if(ListUtility.hasValue(emailAttachList)) {
			long latestAttachmentId = emailAttachList.get(0);
			attachmentDbData = attachmentDbDataList.stream().filter(x -> x.getAttachmentId() == latestAttachmentId)
					.collect(Collectors.toList()).get(0);
		}
		return attachmentDbData;
	}

	public static String getEmailAttachmentFileName(AttachmentDbData attachmentDbData) {
		String fileName = "";
		if (attachmentDbData.getLogicalName().equalsIgnoreCase(WorkbenchConstants.EMAIL_BODY_HTML_STANDARD_FILE_NAME)) {
			fileName = WorkbenchConstants.EMAIL_BODY_HTML_STANDARD_FILE_NAME;
		} else if (attachmentDbData.getLogicalName()
				.equalsIgnoreCase(WorkbenchConstants.EMAIL_BODY_TXT_STANDARD_FILE_NAME)) {
			fileName = WorkbenchConstants.EMAIL_BODY_TXT_STANDARD_FILE_NAME;
		}
		return fileName;
	}
}