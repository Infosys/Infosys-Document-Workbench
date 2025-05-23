/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.extractor.rule.email;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.docwb.engine.core.rule.AttributeExtractRuleAsyncBase;
import com.infosys.ainauto.docwb.engine.core.rule.IAttributeExtractRuleListener;
import com.infosys.ainauto.docwb.web.data.AttributeData;
import com.infosys.ainauto.docwb.web.data.DocumentData;
import com.infosys.ainauto.docwb.web.data.EmailData;
import com.infosys.ainauto.docwb.web.type.EnumExtractType;
import com.infosys.ainauto.docwb.web.type.EnumSystemAttributeName;

@Component
public class Rule002ExtractReceivedDate extends AttributeExtractRuleAsyncBase {
	private static Logger logger = LoggerFactory.getLogger(Rule002ExtractReceivedDate.class);

	public void doExtractAsync(List<Object> objList, IAttributeExtractRuleListener attributeExtractRuleListener) {

		EmailData emailData = (EmailData) objList.get(0);
		Date formatReceivedDate = emailData.getEmailDate();
		SimpleDateFormat dateFormatter = new SimpleDateFormat("E MMM dd yyyy HH:mm:ss z");
		String date = dateFormatter.format(formatReceivedDate);
		logger.info("Date format :" + date);

		AttributeData attributeData = new AttributeData();
		attributeData.setAttrNameCde(EnumSystemAttributeName.RECEIVED_DATE.getCde()) // 2=ReceivedDate
				.setAttrValue(date).setExtractType(EnumExtractType.DIRECT_COPY).setConfidencePct(100);

		List<AttributeData> attributeDataList = new ArrayList<>(Arrays.asList(attributeData));
		DocumentData responseDocumentData = new DocumentData();
		responseDocumentData.setAttributes(attributeDataList);
		attributeExtractRuleListener.onAttributeExtractionComplete(null, responseDocumentData);
	}

}
