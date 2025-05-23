/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.web.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.json.JsonArray;
import javax.json.JsonObject;

import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.docwb.web.data.AttributeData;
import com.infosys.ainauto.docwb.web.data.DocumentData;
import com.infosys.ainauto.docwb.web.type.EnumExtractType;

public class AttributeHelper {

	public static String getAttributeValue(DocumentData documentData, int attributeNameCde) {
		return getAttributeValue(documentData.getAttributes(), attributeNameCde);
	}

	public static String getAttributeValue(List<AttributeData> attributeDataList, int attributeNameCde) {
		String attributeValue = null;
		if (attributeDataList != null && attributeDataList.size() > 0) {
			Optional<AttributeData> attributeData = attributeDataList.stream()
					.filter(a -> a.getAttrNameCde() == attributeNameCde).findFirst();
			if (attributeData.isPresent()) {
				attributeValue = attributeData.get().getAttrValue();
			}
		}
		return attributeValue;
	}

	/**
	 * @param attributeDataList
	 * @param jsonResponseArray
	 */
	public static List<AttributeData> getAttributes(JsonArray jsonResponseArray) {
		List<AttributeData> attributeDataList = new ArrayList<AttributeData>();

		for (int i = 0; i < jsonResponseArray.size(); i++) {
			JsonObject object = jsonResponseArray.getJsonObject(i);
			AttributeData attributeData = new AttributeData();
			attributeData.setAttrNameCde(object.getInt(DocwbWebConstants.ATTR_NAME_CDE));
			attributeData.setAttrNameTxt(object.getString(DocwbWebConstants.ATTR_NAME_TXT));
			attributeData.setAttrValue(object.getString(DocwbWebConstants.ATTR_VALUE, ""));
			attributeData.setConfidencePct(object.getInt(DocwbWebConstants.CONFIDENCE_PCT));
			if (!object.isNull(DocwbWebConstants.ATTRIBUTES)) {
				JsonArray multiAttrArray = object.getJsonArray(DocwbWebConstants.ATTRIBUTES);
				if (ListUtility.hasValue(multiAttrArray)) {
					List<AttributeData> multiAttrDataList = new ArrayList<>();
					for (int j = 0; j < multiAttrArray.size(); j++) {
						JsonObject multiAttrobject = multiAttrArray.getJsonObject(j);
						AttributeData multiAttrData = new AttributeData();
						multiAttrData.setAttrNameTxt(multiAttrobject.getString(DocwbWebConstants.ATTR_NAME_TXT));
						multiAttrData.setAttrValue(multiAttrobject.getString(DocwbWebConstants.ATTR_VALUE, ""));
						multiAttrData.setConfidencePct(multiAttrobject.getInt(DocwbWebConstants.CONFIDENCE_PCT));
						multiAttrData.setExtractType(
								EnumExtractType.get(multiAttrobject.getInt(DocwbWebConstants.EXTRACT_TYPE_CDE)));
						if (!multiAttrobject.isNull(DocwbWebConstants.ATTRIBUTES)) {
							multiAttrData.setAttributeDataList(
									getAttributes(multiAttrobject.getJsonArray(DocwbWebConstants.ATTRIBUTES)));
						}

						multiAttrDataList.add(multiAttrData);
					}
					attributeData.setAttributeDataList(multiAttrDataList);
				}
			}
			attributeData.setExtractType(EnumExtractType.get(object.getInt(DocwbWebConstants.EXTRACT_TYPE_CDE)));
			attributeDataList.add(attributeData);
		}
		return attributeDataList;
	}

}