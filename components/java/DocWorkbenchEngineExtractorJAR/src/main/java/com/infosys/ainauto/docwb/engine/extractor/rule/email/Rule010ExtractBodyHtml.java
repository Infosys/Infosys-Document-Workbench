/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.extractor.rule.email;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import com.infosys.ainauto.docwb.engine.core.rule.AttributeExtractRuleAsyncBase;
import com.infosys.ainauto.docwb.engine.core.rule.IAttributeExtractRuleListener;
import com.infosys.ainauto.docwb.web.data.AttributeData;
import com.infosys.ainauto.docwb.web.data.DocumentData;
import com.infosys.ainauto.docwb.web.data.EmailData;
import com.infosys.ainauto.docwb.web.type.EnumExtractType;
import com.infosys.ainauto.docwb.web.type.EnumSystemAttributeName;

@Component
public class Rule010ExtractBodyHtml extends AttributeExtractRuleAsyncBase {

	public void doExtractAsync(List<Object> objList, IAttributeExtractRuleListener attributeExtractRuleListener) {

		EmailData emailData = (EmailData) objList.get(0);

		String emailBodyHtml = emailData.getEmailBodyHtml();

		if (emailBodyHtml == null) {
			emailBodyHtml = "";
		} else {
			// To remove comments from html content to avoid issue in outbound email.
			emailBodyHtml = emailBodyHtml.replaceAll("(?=<!--)([\\s\\S]*?)-->", "");
		}

		AttributeData attributeData = new AttributeData();
		attributeData.setAttrNameCde(EnumSystemAttributeName.CONTENT_HTML.getCde()) // 10=EmailBodyHtml
				.setAttrValue(emailBodyHtml).setExtractType(EnumExtractType.DIRECT_COPY).setConfidencePct(100);

		List<AttributeData> attributeDataList = new ArrayList<>(Arrays.asList(attributeData));
		DocumentData responseDocumentData = new DocumentData();
		responseDocumentData.setAttributes(attributeDataList);
		attributeExtractRuleListener.onAttributeExtractionComplete(null, responseDocumentData);
	}

}
