/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.common;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.service.common.type.EnumSystemAttributeName;
import com.infosys.ainauto.docwb.service.model.api.attribute.ExportAttributeResData;
import com.infosys.ainauto.docwb.service.model.db.AttributeDbData;

public class ExportAttributeHelper {

	public static List<ExportAttributeResData.AttributeData> convertAttrDataListToExportData(
			List<AttributeDbData> attrDbDataList) {
		List<ExportAttributeResData.AttributeData> attributeDataList = new ArrayList<>();
		if (ListUtility.hasValue(attrDbDataList)) {
			for (AttributeDbData attrDbData : attrDbDataList) {
				if (attrDbData.getAttrNameCde() != EnumSystemAttributeName.CONTENT_ANNOTATION_ANNOTATOR.getCde()) {
					ExportAttributeResData.AttributeData attributeData = new ExportAttributeResData.AttributeData();
					BeanUtils.copyProperties(attrDbData, attributeData);
					if (!StringUtility.hasValue(attributeData.getAttrValue())) {
						attributeData.setAttrValue("");
					}
					attributeData.setAttributes(convertAttrDataListToExportData(attrDbData.getAttributes()));
					attributeDataList.add(attributeData);
				}
			}
		}
		return attributeDataList;
	}

	public static List<ExportAttributeResData.AttachmentData> convertAttachDataListToExportData(
			List<AttributeDbData> attrDbDataList) {
		List<ExportAttributeResData.AttachmentData> attributeDataList = new ArrayList<>();
		if (ListUtility.hasValue(attrDbDataList)) {
			for (AttributeDbData attrDbData : attrDbDataList) {
				ExportAttributeResData.AttachmentData attachmentData = new ExportAttributeResData.AttachmentData();
				attachmentData.setAttachmentId(attrDbData.getAttachmentId());
				attachmentData.setAttributes(convertAttrDataListToExportData(attrDbData.getAttributes()));
				attributeDataList.add(attachmentData);
			}
		}
		return attributeDataList;
	}
}