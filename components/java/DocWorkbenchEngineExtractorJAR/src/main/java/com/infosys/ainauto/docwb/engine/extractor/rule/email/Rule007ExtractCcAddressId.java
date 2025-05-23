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
public class Rule007ExtractCcAddressId extends AttributeExtractRuleAsyncBase {

	public void doExtractAsync(List<Object> objList, IAttributeExtractRuleListener attributeExtractRuleListener) {

		EmailData emailData = (EmailData) objList.get(0);

		List<EmailAddressData> emailAddressCcList = emailData.getEmailAddressCcList();
		AttributeData attributeData = null;
		String attrValue = "";
		for (int i = 0; i < emailAddressCcList.size(); i++) {

			String ccAddressId = emailAddressCcList.get(i).getEmailId().toLowerCase();
			if (ccAddressId != null && ccAddressId.length() > 0) {
				attrValue += ccAddressId;

			} else {
				attrValue += " ";

			}
			attrValue += ";";

		}
		if (attrValue != null && attrValue.length() > 0) {
			attrValue = attrValue.substring(0, attrValue.length() - 1);
			attributeData = new AttributeData();
			attributeData.setAttrNameCde(EnumSystemAttributeName.CC_ADDRESS_ID.getCde()) // 7=CC Address Id
					.setAttrValue(attrValue).setExtractType(EnumExtractType.DIRECT_COPY).setConfidencePct(100);
		}

		List<AttributeData> attributeDataList = new ArrayList<>(Arrays.asList(attributeData));
		DocumentData responseDocumentData = new DocumentData();
		responseDocumentData.setAttributes(attributeDataList);
		attributeExtractRuleListener.onAttributeExtractionComplete(null, responseDocumentData);
	}

}
