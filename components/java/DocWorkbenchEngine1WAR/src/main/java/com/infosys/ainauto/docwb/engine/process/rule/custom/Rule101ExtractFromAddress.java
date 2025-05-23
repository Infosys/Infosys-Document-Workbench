/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.process.rule.custom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.docwb.engine.common.EngineConstants;
import com.infosys.ainauto.docwb.engine.core.rule.AttributeExtractRuleAsyncBase;
import com.infosys.ainauto.docwb.engine.core.rule.IAttributeExtractRuleListener;
import com.infosys.ainauto.docwb.web.data.AttributeData;
import com.infosys.ainauto.docwb.web.data.DocumentData;
import com.infosys.ainauto.docwb.web.type.EnumExtractType;
import com.infosys.ainauto.docwb.web.type.EnumSystemAttributeName;

@Component
public class Rule101ExtractFromAddress extends AttributeExtractRuleAsyncBase {

	@PostConstruct
	private void init() {
	}

	public void doExtractAsync(List<Object> objList, IAttributeExtractRuleListener attributeExtractRuleListener) {

		DocumentData documentData = (DocumentData) objList.get(0);
		AttributeData attributeData = null;
		List<AttributeData> subAttributeDataList = new ArrayList<>();
		for (int i = 0; i < documentData.getAttributes().size(); i++) {
			if (documentData.getAttributes().get(i).getAttrNameCde() == EnumSystemAttributeName.FROM_ID.getCde()) {
				attributeData = new AttributeData();
				subAttributeDataList.add(new AttributeData()
						.setAttrValue(documentData.getAttributes().get(i).getAttrValue().toLowerCase())
						.setAttrNameTxt(EngineConstants.ATTR_NAME_TXT_EMAIL_ADDRESS)
						.setExtractType(EnumExtractType.CUSTOM_LOGIC).setConfidencePct(100));
			}
		}
		if (ListUtility.hasValue(subAttributeDataList) && attributeData != null) {
			attributeData.setAttrNameCde(EnumSystemAttributeName.MULTI_ATTRIBUTE.getCde()) // 44=Multi-Attribute
					.setAttrNameTxt(EngineConstants.GROUP_NAME_INVOICE_BILL_COPY)
					.setConfidencePct(EngineConstants.CONFIDENCE_PCT_UNDEFINED)
					.setExtractType(EnumExtractType.DIRECT_COPY).setAttributeDataList(subAttributeDataList);

		}
		List<AttributeData> attributeDataList = new ArrayList<>(Arrays.asList(attributeData));
		DocumentData responseDocumentData = new DocumentData();
		responseDocumentData.setAttributes(attributeDataList);
		attributeExtractRuleListener.onAttributeExtractionComplete(null, responseDocumentData);
	}
}
