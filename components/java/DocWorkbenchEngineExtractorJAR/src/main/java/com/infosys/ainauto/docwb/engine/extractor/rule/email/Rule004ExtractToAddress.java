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
import com.infosys.ainauto.docwb.web.data.EmailAddressData;
import com.infosys.ainauto.docwb.web.data.EmailData;
import com.infosys.ainauto.docwb.web.type.EnumSystemAttributeName;
import com.infosys.ainauto.docwb.web.type.EnumExtractType;

@Component
public class Rule004ExtractToAddress extends AttributeExtractRuleAsyncBase {

	public void doExtractAsync(List<Object> objList, IAttributeExtractRuleListener attributeExtractRuleListener) {

		EmailData emailData = (EmailData) objList.get(0);

		List<EmailAddressData> emailAddressToList = emailData.getEmailAddressToList();
		AttributeData attributeData = new AttributeData();

		String attrValue = "";
		for (int i = 0; i < emailAddressToList.size(); i++) {
			String toAddress = emailAddressToList.get(i).getEmailName();
			if (toAddress != null && toAddress.length() > 0) {
				attrValue += toAddress;

			} else {
				attrValue += " ";

			}
			attrValue += ";";

		}
		if (attrValue != null && attrValue.length() > 0) {
			attrValue = attrValue.substring(0, attrValue.length() - 1);
		}

		attributeData.setAttrNameCde(EnumSystemAttributeName.TO_ADDRESS.getCde()) // 4=To Address
				.setAttrValue(attrValue).setExtractType(EnumExtractType.DIRECT_COPY).setConfidencePct(100);

		List<AttributeData> attributeDataList = new ArrayList<>(Arrays.asList(attributeData));
		DocumentData responseDocumentData = new DocumentData();
		responseDocumentData.setAttributes(attributeDataList);
		attributeExtractRuleListener.onAttributeExtractionComplete(null, responseDocumentData);
	}

}
