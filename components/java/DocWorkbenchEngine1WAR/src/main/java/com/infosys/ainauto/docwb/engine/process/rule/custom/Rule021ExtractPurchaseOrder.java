/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.process.rule.custom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.docwb.web.data.AttributeData;
import com.infosys.ainauto.docwb.web.data.DocumentData;
import com.infosys.ainauto.docwb.engine.common.EngineConstants;
import com.infosys.ainauto.docwb.engine.core.rule.AttributeExtractRuleAsyncBase;
import com.infosys.ainauto.docwb.engine.core.rule.IAttributeExtractRuleListener;
import com.infosys.ainauto.docwb.engine.service.ner.IExtractService;
import com.infosys.ainauto.docwb.web.common.AttributeHelper;
import com.infosys.ainauto.docwb.web.type.EnumExtractType;

@Component
public class Rule021ExtractPurchaseOrder extends AttributeExtractRuleAsyncBase {

	@Autowired
	private IExtractService extractService;
	
	private List<String> entityList = Arrays.asList("PurchaseOrder"); 

	@Override
	public void doExtractAsync(List<Object> objList, IAttributeExtractRuleListener attributeExtractRuleListener) {
		AttributeData attributeData = null;
		DocumentData documentData = (DocumentData) objList.get(0);
		String attrValue = null;

		String subject = AttributeHelper.getAttributeValue(documentData, 3); // 3=Subject
		String content = AttributeHelper.getAttributeValue(documentData, 9); // 9=Content
		
		
		String textToProcess = subject + " " + content;
		List<String> outputList = new ArrayList<String>();
	
		AttributeData planTypeAttributeData = extractService.getExtract(textToProcess, entityList);
		if (planTypeAttributeData != null && planTypeAttributeData.getAttrValue() != null) {
			attrValue = planTypeAttributeData.getAttrValue();
			if (attrValue.contains(",")) {
				String[] b = attrValue.split(",");
				for (int i = 0; i < b.length; i++) {
					outputList.add(b[i]);
				}	
			} else {
				outputList.add(attrValue);
			}
		}
		
		if (outputList.size() > 0) {
			String result = outputList.stream().collect(Collectors.joining(","));
			float confidencePct = (planTypeAttributeData != null & planTypeAttributeData.getAttrValue() != null)
					? planTypeAttributeData.getConfidencePct()
					: 80f;
			attributeData = new AttributeData();
			attributeData.setAttrNameCde(EngineConstants.ATTR_NAME_CDE_PURCHASE_ORDER) // 21=Purchase order
					.setAttrValue(result).setExtractType(EnumExtractType.CUSTOM_LOGIC).setConfidencePct(confidencePct);
		}
		List<AttributeData> attributeDataList = new ArrayList<>(Arrays.asList(attributeData));
		DocumentData responseDocumentData = new DocumentData();
		responseDocumentData.setAttributes(attributeDataList);
		attributeExtractRuleListener.onAttributeExtractionComplete(null, responseDocumentData);
	}
}
