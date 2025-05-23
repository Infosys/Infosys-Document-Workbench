/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.common;

import java.util.List;

import com.infosys.ainauto.docwb.web.data.AttributeData;
import com.infosys.ainauto.docwb.web.type.EnumExtractType;

public class AttributeDataHelper {

	public static AttributeData createMultipleAttibuteElement(int attrNameCde, String groupName,
			List<AttributeData> attributes) {
		AttributeData attributeData = new AttributeData();
		attributeData.setAttrNameCde(attrNameCde).setAttrNameTxt(groupName)
				.setConfidencePct(EngineConstants.CONFIDENCE_PCT_DEFAULT)
				.setExtractType(groupName.equals(EngineConstants.GROUP_NAME_ROW) ? EnumExtractType.DIRECT_COPY
						: EnumExtractType.CUSTOM_LOGIC)
				.setAttributeDataList(attributes);
		return attributeData;
	}
}
