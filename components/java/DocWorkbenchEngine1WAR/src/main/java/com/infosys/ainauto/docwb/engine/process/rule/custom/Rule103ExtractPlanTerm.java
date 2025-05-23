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
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.docwb.engine.common.EngineConstants;
import com.infosys.ainauto.docwb.engine.core.rule.AttributeExtractRuleAsyncBase;
import com.infosys.ainauto.docwb.engine.core.rule.IAttributeExtractRuleListener;
import com.infosys.ainauto.docwb.engine.service.ner.IExtractService;
import com.infosys.ainauto.docwb.web.common.AttributeHelper;
import com.infosys.ainauto.docwb.web.data.AttributeData;
import com.infosys.ainauto.docwb.web.data.DocumentData;
import com.infosys.ainauto.docwb.web.type.EnumExtractType;

@Component
public class Rule103ExtractPlanTerm extends AttributeExtractRuleAsyncBase {

	private static final Logger logger = LoggerFactory.getLogger(Rule103ExtractPlanTerm.class);

	@Autowired
	private IExtractService extractService;

	private List<String> entityList = Arrays.asList("PlanTerm");

	@PostConstruct
	private void init() {
	}

	public void doExtractAsync(List<Object> objList, IAttributeExtractRuleListener attributeExtractRuleListener) {

		DocumentData documentData = (DocumentData) objList.get(0);
		AttributeData attributeData = new AttributeData();
		String attrValue = null;
		String subject = AttributeHelper.getAttributeValue(documentData, 3); // 3=Subject
		String content = AttributeHelper.getAttributeValue(documentData, 9); // 9=Content

		String textToProcess = subject + " " + content;
		List<String> outputList = new ArrayList<String>();

		AttributeData planTermAttributeData = extractService.getExtract(textToProcess, entityList);
		if (planTermAttributeData != null && planTermAttributeData.getAttrValue() != null) {
			attrValue = planTermAttributeData.getAttrValue();
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
			float confidencePct = (planTermAttributeData != null & planTermAttributeData.getAttrValue() != null)
					? planTermAttributeData.getConfidencePct()
					: 80f;
			attributeData = new AttributeData();
			attributeData.setAttrNameCde(EngineConstants.ATTR_NAME_CDE_PLAN_TERM) // 103=Plan Term
					.setAttrValue(result).setExtractType(EnumExtractType.CUSTOM_LOGIC).setConfidencePct(confidencePct);
		}
		List<AttributeData> attributeDataList = new ArrayList<>(Arrays.asList(attributeData));
		DocumentData responseDocumentData = new DocumentData();
		responseDocumentData.setAttributes(attributeDataList);
		attributeExtractRuleListener.onAttributeExtractionComplete(null, responseDocumentData);
	}
}
