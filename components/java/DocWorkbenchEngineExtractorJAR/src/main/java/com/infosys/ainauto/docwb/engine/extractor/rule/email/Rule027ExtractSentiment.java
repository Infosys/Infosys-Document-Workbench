/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.extractor.rule.email;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.engine.core.rule.AttributeExtractRuleAsyncBase;
import com.infosys.ainauto.docwb.engine.core.rule.IAttributeExtractRuleListener;
import com.infosys.ainauto.docwb.engine.extractor.service.ml.sentiment.ISentimentAnalysisService;
import com.infosys.ainauto.docwb.web.data.AttributeData;
import com.infosys.ainauto.docwb.web.data.DocumentData;
import com.infosys.ainauto.docwb.web.data.EmailData;
import com.infosys.ainauto.docwb.web.type.EnumExtractType;
import com.infosys.ainauto.docwb.web.type.EnumSystemAttributeName;

@Component
public class Rule027ExtractSentiment extends AttributeExtractRuleAsyncBase {

	private static final Logger logger = LoggerFactory.getLogger(Rule027ExtractSentiment.class);

	@Autowired
	private ISentimentAnalysisService sentimentAnalysisService;

	public void doExtractAsync(List<Object> objList, IAttributeExtractRuleListener attributeExtractRuleListener) {

		EmailData emailData = (EmailData) objList.get(0);
		String text = "";
		if (StringUtility.hasTrimmedValue(emailData.getEmailBodyHtml())) {
			text = emailData.getEmailBodyHtml();
		} else {
			text = emailData.getEmailBodyText();
		}
		AttributeData sentiAttributeData = null;
		if (StringUtility.hasTrimmedValue(text)) {
			sentiAttributeData = sentimentAnalysisService.getSentimentVal(text);
		}

		if (sentiAttributeData == null) {
			logger.error("Sentiment data could not be fetched");
			Exception ex = new Exception("Sentiment data could not be fetched");
			attributeExtractRuleListener.onAttributeExtractionComplete(ex, null);
		} else {
			AttributeData attributeData = new AttributeData();
			attributeData.setAttrNameCde(EnumSystemAttributeName.SENTIMENT.getCde()) // 107=Sentiment
					.setAttrValue(sentiAttributeData.getAttrValue()).setExtractType(EnumExtractType.CUSTOM_LOGIC)
					.setConfidencePct(sentiAttributeData.getConfidencePct());

			List<AttributeData> attributeDataList = new ArrayList<>(Arrays.asList(attributeData));
			DocumentData responseDocumentData = new DocumentData();
			responseDocumentData.setAttributes(attributeDataList);
			attributeExtractRuleListener.onAttributeExtractionComplete(null, responseDocumentData);
		}
	}
}
