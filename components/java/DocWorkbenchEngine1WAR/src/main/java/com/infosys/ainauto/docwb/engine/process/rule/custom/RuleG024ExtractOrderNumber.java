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
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.docwb.engine.common.EngineConstants;
import com.infosys.ainauto.docwb.engine.core.common.DocwbEngineCoreConstants;
import com.infosys.ainauto.docwb.engine.core.rule.AttributeExtractRuleAsyncBase;
import com.infosys.ainauto.docwb.engine.core.rule.IAttributeExtractRuleListener;
import com.infosys.ainauto.docwb.web.common.AttributeHelper;
import com.infosys.ainauto.docwb.web.data.AttributeData;
import com.infosys.ainauto.docwb.web.data.DocumentData;
import com.infosys.ainauto.docwb.web.type.EnumExtractType;

@Component
public class RuleG024ExtractOrderNumber extends AttributeExtractRuleAsyncBase {
	private static final Logger logger = LoggerFactory.getLogger(Rule018ExtractInvoiceNumbers.class);

	@SuppressWarnings("unused")
	private List<String> keywordsList = Arrays.asList("Bestellnummer", "Nummer", "Bestellung?", "Bestellung");
	@SuppressWarnings("unused")
	private List<String> continuationList = Arrays.asList("");

	@PostConstruct
	private void init() {
	}

	@Override
	public void doExtractAsync(List<Object> objList, IAttributeExtractRuleListener attributeExtractRuleListener) {
		try {
			DocumentData documentData = (DocumentData) objList.get(0);
			String ruleType = (objList != null && objList.size() > 1) ? objList.get(1).toString() : "";
			DocumentData responseDocumentData = new DocumentData();
			if (ruleType.isEmpty() || ruleType.equals(DocwbEngineCoreConstants.RULE_TYPE_EMAIL)) {
				String subject = AttributeHelper.getAttributeValue(documentData, 3); // 3=Subject
				String content = AttributeHelper.getAttributeValue(documentData, 9); // 9=Content
				String searchInWhere = subject + " " + content;
				AttributeData attributeData = extractUsingRegex(searchInWhere);
				List<AttributeData> attributeDataList = new ArrayList<>(Arrays.asList(attributeData));
				responseDocumentData.setAttributes(attributeDataList);
			}
			attributeExtractRuleListener.onAttributeExtractionComplete(null, responseDocumentData);
		} catch (Exception e) {
			logger.error("Error occurred in extracting data", e);
			attributeExtractRuleListener.onAttributeExtractionComplete(e, null);
		}

	}

	private AttributeData extractUsingRegex(String contentString) {
		AttributeData attributeData = null;
		List<String> outputList = new ArrayList<String>();
		// Step 1 - Remove all non alphanumeric characters
		contentString = contentString.replaceAll("[^A-Za-z0-9]", " ");
		logger.debug(contentString);

		// Step 2 - Convert all words to token
		StringTokenizer st = new StringTokenizer(contentString);
		String token = "";
		while (st.hasMoreTokens()) {
			token = st.nextToken();
			if (token.toLowerCase(Locale.ENGLISH).startsWith("lc")) {
				outputList.add(token);
			}
		}
		if (outputList.size() > 0) {
			String result = outputList.stream().collect(Collectors.joining(","));
			attributeData = new AttributeData();
			attributeData.setAttrNameCde(EngineConstants.ATTR_NAME_CDE_ORDER_NUMBERS) // 23=Credit card number
					.setAttrValue(result).setExtractType(EnumExtractType.CUSTOM_LOGIC).setConfidencePct(90);
		}
		return attributeData;
	}
}