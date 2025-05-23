/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.process.rule.custom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.FileUtility;
import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.engine.common.EngineConstants;
import com.infosys.ainauto.docwb.engine.core.common.DocwbEngineCoreConstants;
import com.infosys.ainauto.docwb.engine.core.rule.AttributeExtractRuleAsyncBase;
import com.infosys.ainauto.docwb.engine.core.rule.IAttributeExtractRuleListener;
import com.infosys.ainauto.docwb.engine.core.service.DocWbApiClient;
import com.infosys.ainauto.docwb.engine.extractor.service.creditcard.CreditCardService;
import com.infosys.ainauto.docwb.engine.extractor.service.docwbextractor.DocwbExtractorService;
import com.infosys.ainauto.docwb.web.api.IAttachmentService;
import com.infosys.ainauto.docwb.web.common.AttributeHelper;
import com.infosys.ainauto.docwb.web.data.AttachmentData;
import com.infosys.ainauto.docwb.web.data.AttributeData;
import com.infosys.ainauto.docwb.web.data.DocumentData;
import com.infosys.ainauto.docwb.web.type.EnumExtractType;
import com.infosys.ainauto.docwb.web.type.EnumFileType;

@Component
public class Rule107ExtractCreditCardLastTransactionDetails extends AttributeExtractRuleAsyncBase {
	private static final Logger logger = LoggerFactory.getLogger(Rule107ExtractCreditCardLastTransactionDetails.class);
//	@Autowired
//	private Environment environment;

//	@Autowired
//	private DocwbExtractorService docwbExtractorService;

	@Autowired
	private CreditCardService creditCardService;

	@Autowired
	private DocWbApiClient docWbApiClient;

	private IAttachmentService attachmentService;

	private List<String> keywordsList = Arrays.asList("Credit card", "credit card", "number", "card", "CC-",
			"Credit card", "No", "no");
	private List<String> continuationList = Arrays.asList("and", "card", "no");

	@PostConstruct
	private void init() {
		attachmentService = docWbApiClient.getAttachmentService();
	}

	@Override
	public void doExtractAsync(List<Object> objList, IAttributeExtractRuleListener attributeExtractRuleListener) {

		try {
			DocumentData documentData = (DocumentData) objList.get(0);

			DocumentData responseDocumentData = new DocumentData();
			List<AttachmentData> attachmentAttrDataList = new ArrayList<>();
			AttachmentData attachmentAttrData = new AttachmentData();
			AttributeData attributeData1 = new AttributeData();
			AttributeData attributeData2 = new AttributeData();
			String creditCardNum = documentData.getAttachmentDataList().get(1).getAttributes().get(0).getAttrValue();
			List<CreditCardService.LastTransactionResData> lastTransactionResDataList = creditCardService
					.getLastTransaction(creditCardNum);
			List<AttributeData> attributeDataList = new ArrayList<>();
			String transactionStatus = lastTransactionResDataList.get(0).getTransactionStatus();
			String transactionAmount = String.valueOf(lastTransactionResDataList.get(0).getTransactionAmount());
			attributeData1.setAttrNameCde(EngineConstants.ATTR_NAME_CDE_LAST_TRANSACTION_AMOUNT)
					.setAttrValue(transactionAmount).setExtractType(EnumExtractType.CUSTOM_LOGIC).setConfidencePct(90);
			attributeDataList.add(attributeData1);
			attributeData2.setAttrNameCde(EngineConstants.ATTR_NAME_CDE_LAST_TRANSACTION_STATUS)
					.setAttrValue(transactionStatus).setExtractType(EnumExtractType.CUSTOM_LOGIC).setConfidencePct(90);
			attributeDataList.add(attributeData2);
			// attachmentId taken previous rules object o/p
			attachmentAttrData.setAttachmentId(documentData.getAttachmentDataList().get(0).getAttachmentId());
			attachmentAttrData.setAttributes(attributeDataList);
			attachmentAttrDataList.add(attachmentAttrData);
			responseDocumentData.setAttachmentDataList(attachmentAttrDataList);
			attributeExtractRuleListener.onAttributeExtractionComplete(null, responseDocumentData);
		} catch (Exception e) {
			logger.error("Error occurred in extracting data", e);
			attributeExtractRuleListener.onAttributeExtractionComplete(e, null);
		}

	}

}