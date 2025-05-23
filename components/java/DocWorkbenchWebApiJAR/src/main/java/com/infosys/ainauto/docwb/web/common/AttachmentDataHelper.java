/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


package com.infosys.ainauto.docwb.web.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.infosys.ainauto.commonutils.FileUtility;
import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.docwb.web.data.AttachmentData;
import com.infosys.ainauto.docwb.web.type.EnumExtractType;
import com.infosys.ainauto.docwb.web.type.EnumFileMetadata;
import com.infosys.ainauto.docwb.web.type.EnumFileSupported;
import com.infosys.ainauto.docwb.web.type.EnumFileType;
import com.infosys.ainauto.docwb.web.type.EnumFileUnsupported;

public class AttachmentDataHelper {

	public static List<AttachmentData> getFilteredAttachmentList(List<AttachmentData> attachmentDataList,
			EnumFileType enumFileType) {
		List<AttachmentData> filteredAttachmentDataList = new ArrayList<>();
		if (enumFileType.equals(EnumFileType.ALL)) {
			filteredAttachmentDataList = attachmentDataList;
		} else {
			List<String> valueList = new ArrayList<>();
			if (enumFileType.equals(EnumFileType.SUPPORTED)) {
				Arrays.asList(EnumFileSupported.values())
						.forEach(fileType -> valueList.add(fileType.getValue().toLowerCase()));
			} else {
				Arrays.asList(EnumFileUnsupported.values())
						.forEach(fileType -> valueList.add(fileType.getValue().toLowerCase()));
			}
			for (AttachmentData attachmentData : attachmentDataList) {
				if (valueList.contains(FileUtility.getFileExtension(attachmentData.getLogicalName()).toLowerCase())) {
					filteredAttachmentDataList.add(attachmentData);
				}
			}
		}
		return filteredAttachmentDataList;
	}

	/**
	 * This method is called to get the main attachment based on extract type cde
	 * and file meta data. </br>
	 * <b>Main Attachment </b>The attachment which is shown in UI and used for
	 * processing.
	 * 
	 * @param attachmentDataList
	 */
	public static AttachmentData getMainAttachmentData(List<AttachmentData> attachmentDataList) {
		AttachmentData attachmentData = null;
		// Zip Customization
		{
			attachmentDataList = sortAttachmentDataBasedOnLogicalNames(attachmentDataList);
			AttachmentData firstAttachmentData = attachmentDataList.get(0);
			attachmentDataList = groupAttachment(attachmentDataList).get(firstAttachmentData.getGroupName());
		}

		Map<Integer, List<AttachmentData>> attachmentDataExtractTypeMap = attachmentDataList.stream()
				.collect(Collectors.groupingBy(AttachmentData::getExtractTypeCde, Collectors.toList()));
		if (attachmentDataExtractTypeMap.containsKey(EnumExtractType.CUSTOM_LOGIC.getValue())) {
			attachmentData = attachmentDataExtractTypeMap.get(EnumExtractType.CUSTOM_LOGIC.getValue()).stream().filter(
					attachment -> !(ListUtility.hasValue(attachment.getAttributes()) && attachment.getAttributes()
							.get(0).getAttrValue().equalsIgnoreCase(EnumFileMetadata.PLAIN_TEXT.getValue())))
					.findFirst().orElse(null);
		}
		return attachmentData != null ? attachmentData
				: attachmentDataExtractTypeMap.get(EnumExtractType.DIRECT_COPY.getValue()).get(0);
	}

	// private static <T extends Enum<T>> List<String> getEnumValues(Class<T> key) {
	// List<String> valueList = new ArrayList<>();
	// Arrays.asList(key.values()).forEach(fileType ->
	// valueList.add(fileType.getValue()));
	// return valueList;
	// }

	/**
	 * This method is called to sort attachment based on logical name. </br>
	 * 
	 * @param attachmentDataList
	 */
	public static List<AttachmentData> sortAttachmentDataBasedOnLogicalNames(List<AttachmentData> attachmentDataList) {
		attachmentDataList.sort(Comparator.comparing(AttachmentData::getSortOrder));
		return attachmentDataList;
	}

	/**
	 * This method is called to group attachment based on group name. </br>
	 * 
	 * @param attachmentDataList
	 */
	public static Map<String, List<AttachmentData>> groupAttachment(List<AttachmentData> attachmentDataList) {
		Map<String, List<AttachmentData>> attachmentGroup = new HashMap<>();
		for (AttachmentData attachmentData : attachmentDataList) {
			String groupName = attachmentData.getGroupName();
			List<AttachmentData> existingList = attachmentGroup.get(groupName);
			if (!ListUtility.hasValue(existingList)) {
				existingList = new ArrayList<>();
			}
			existingList.add(attachmentData);
			attachmentGroup.put(groupName, existingList);
		}
		return attachmentGroup;
	}
}
