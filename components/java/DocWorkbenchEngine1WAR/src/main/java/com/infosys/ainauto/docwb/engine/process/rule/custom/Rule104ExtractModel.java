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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.docwb.engine.common.EngineConstants;
import com.infosys.ainauto.docwb.engine.core.rule.AttributeExtractRuleAsyncBase;
import com.infosys.ainauto.docwb.engine.core.rule.IAttributeExtractRuleListener;
import com.infosys.ainauto.docwb.web.common.AttributeHelper;
import com.infosys.ainauto.docwb.web.data.AttributeData;
import com.infosys.ainauto.docwb.web.data.DocumentData;
import com.infosys.ainauto.docwb.web.type.EnumExtractType;

@Component
public class Rule104ExtractModel extends AttributeExtractRuleAsyncBase {

	private static final Logger logger = LoggerFactory.getLogger(Rule104ExtractModel.class);

	@PostConstruct
	private void init() {
	}

	public void doExtractAsync(List<Object> objList, IAttributeExtractRuleListener attributeExtractRuleListener) {

		DocumentData documentData = (DocumentData) objList.get(0);
		AttributeData attributeData = new AttributeData();
		String subject = AttributeHelper.getAttributeValue(documentData, 3); // 3=Subject
		String content = AttributeHelper.getAttributeValue(documentData, 9); // 9=Content

		String textToProcess = subject + " " + content;
		List<String> outputList = new ArrayList<String>();

		// AttributeData modelAttributeData = extractService.getExtract(textToProcess,
		// entityList);
		// if (modelAttributeData != null && modelAttributeData.getAttrValue() != null)
		// {
		// attrValue = modelAttributeData.getAttrValue();
		// if (attrValue.contains(",")) {
		// String[] b = attrValue.split(",");
		// for (int i = 0; i < b.length; i++) {
		// outputList.add(b[i]);
		// }
		// } else {
		// outputList.add(attrValue);
		// }
		// }

		textToProcess = textToProcess.replaceAll("[^A-Za-z0-9]", " ");
		logger.debug(textToProcess);
		Pattern p = Pattern.compile(EngineConstants.MODEL_EXTRACTION_REGEX, Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(textToProcess);
		while (m.find()) {
			if (m.group(0).length() > 0) {
				outputList.add(m.group(0));
			}
		}

		if (outputList.size() > 0) {
			String result = outputList.stream().collect(Collectors.joining(","));
			float confidencePct = 80f;
			attributeData = new AttributeData();
			attributeData.setAttrNameCde(EngineConstants.ATTR_NAME_CDE_MODEL) // 104=Model
					.setAttrValue(result).setExtractType(EnumExtractType.CUSTOM_LOGIC).setConfidencePct(confidencePct);
		}
		List<AttributeData> attributeDataList = new ArrayList<>(Arrays.asList(attributeData));
		DocumentData responseDocumentData = new DocumentData();
		responseDocumentData.setAttributes(attributeDataList);
		attributeExtractRuleListener.onAttributeExtractionComplete(null, responseDocumentData);
	}
}