/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.extractor.rule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.naming.ConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.FileUtility;
import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.engine.core.common.DocwbEngineCoreConstants;
import com.infosys.ainauto.docwb.engine.core.rule.IAttributeExtractRuleListener;
import com.infosys.ainauto.docwb.engine.core.service.DocWbApiClient;
import com.infosys.ainauto.docwb.engine.extractor.common.AttributeDataHelper;
import com.infosys.ainauto.docwb.engine.extractor.service.attribute.AttributeExtractorService.ExtractorData;
import com.infosys.ainauto.docwb.engine.extractor.service.attribute.AttributeExtractorService.ExtractorData.ExtractorConfigData;
import com.infosys.ainauto.docwb.engine.extractor.service.azure.cognitive.vision.IFormRecognizerService;
import com.infosys.ainauto.docwb.web.api.IAttachmentService;
import com.infosys.ainauto.docwb.web.common.AttachmentDataHelper;
import com.infosys.ainauto.docwb.web.data.AttachmentData;
import com.infosys.ainauto.docwb.web.data.AttributeData;
import com.infosys.ainauto.docwb.web.data.DocumentData;
import com.infosys.ainauto.docwb.web.type.EnumExtractType;
import com.infosys.ainauto.docwb.web.type.EnumFileType;
import com.infosys.ainauto.docwb.web.type.EnumSystemAttributeName;

@Component
public class Rule000ExtractUsingAzureConfiguration extends Rule000ExtractUsingConfigurationBase {

	private static final Logger logger = LoggerFactory.getLogger(Rule000ExtractUsingAzureConfiguration.class);

	@Autowired
	private Environment environment;

	@Autowired
	private IFormRecognizerService formRecognizerService;

	@Autowired
	private DocWbApiClient docWbApiClient;

	private ExtractorData extractorConfiData;

	private IAttachmentService attachmentService;

	private final String attributeExtractorApiMappingConfig = "attributeExtractorAzureApiMappingConfig.json";

	@PostConstruct
	private void init() {
		attachmentService = docWbApiClient.getAttachmentService();
		extractorConfiData = readAttributeExtractorConfigFile(attributeExtractorApiMappingConfig);
	}

	public void doExtractAsync(List<Object> objList, IAttributeExtractRuleListener attributeExtractRuleListener) {
		String tempDownloadPath = FileUtility.getAbsolutePath(environment.getProperty(TEMP_PATH_TXT));
		try {
			DocumentData documentData = (DocumentData) objList.get(0);
			String ruleType = "";
			DocumentData reExtractParamData = null;
			if (objList != null && objList.size() > 1) {
				ruleType = objList.get(1).toString();
				if (objList.size() > 2)
					reExtractParamData = (DocumentData) objList.get(2);
			}

			List<AttachmentData> attachmentAttrDataList = null;
			DocumentData responseDocumentData = new DocumentData();
			responseDocumentData.setAttachmentDataList(attachmentAttrDataList);
			if (ruleType.isEmpty() || ruleType.equals(DocwbEngineCoreConstants.RULE_TYPE_DOCUMENT)) {
				ExtractorConfigData extractorConfigData = AttributeDataHelper
						.getConfiguredExtractorApiDataByAttrNameCde(extractorConfiData,
								EnumSystemAttributeName.DOCUMENT_TYPE.getCde(), documentData);

				if (extractorConfigData != null) {
					List<AttachmentData> attachmentDataList = attachmentService
							.getAttachmentList(documentData.getDocId(), tempDownloadPath, EnumFileType.ALL);

					if (ListUtility.hasValue(attachmentDataList)) {
						AttachmentData attachmentData = AttachmentDataHelper.getMainAttachmentData(attachmentDataList);
						String operationLocationUrl = formRecognizerService.postAnalyzeForm(extractorConfigData,
								attachmentData);

						if (StringUtility.hasValue(operationLocationUrl)) {
							JsonArray jsonArray = formRecognizerService.getAnalyzeFormResult(extractorConfigData,
									operationLocationUrl);
							if (ListUtility.hasValue(jsonArray)) {
								setAttachmentAttributesToDocumentData(responseDocumentData, attachmentDataList,
										jsonArray, extractorConfigData, reExtractParamData);

							}
						}
						attributeExtractRuleListener.onAttributeExtractionComplete(null, responseDocumentData);
					} else {
						attributeExtractRuleListener.onAttributeExtractionComplete(null, responseDocumentData);
					}
				} else {
					attributeExtractRuleListener.onAttributeExtractionComplete(null, responseDocumentData);
				}
			}
		} catch (Exception ex) {
			logger.error("Error occurred in extracting data", ex);
			attributeExtractRuleListener.onAttributeExtractionComplete(ex, null);
		}
	}

	/**
	 * Set Attachment attributes result in response Document data
	 */
	private void setAttachmentAttributesToDocumentData(DocumentData responseDocumentData,
			List<AttachmentData> attachmentDataList, JsonArray documentResultJsonArray,
			ExtractorConfigData extractorConfigData, DocumentData reExtractParamData) throws ConfigurationException {
		AttachmentData resultAttachmentData = new AttachmentData();
		AttachmentData attachmentData = AttachmentDataHelper.getMainAttachmentData(attachmentDataList);
		long attachmentId = attachmentData.getAttachmentId();
		resultAttachmentData.setAttachmentId(attachmentId);
		List<AttributeData> attributes = convertResultDataToAttributeData(extractorConfigData, documentResultJsonArray);
		attributes = filterReExtractParamAttachAttributes(attributes, reExtractParamData, attachmentId);
		resultAttachmentData.setAttributes(attributes);
		responseDocumentData.setAttachmentDataList(new ArrayList<>());
		responseDocumentData.getAttachmentDataList().add(resultAttachmentData);
	}

	private List<AttributeData> convertResultDataToAttributeData(ExtractorConfigData extractorConfigData,
			JsonArray documentResultJsonArray) {
		List<AttributeData> attributes = new ArrayList<AttributeData>();
		for (int i = 0; i < documentResultJsonArray.size(); i++) {
			JsonObject resultJsonObject = documentResultJsonArray.getJsonObject(i);
			if (resultJsonObject.containsKey("fields") && !resultJsonObject.isNull("fields")) {
				JsonObject fieldJsonObject = resultJsonObject.getJsonObject("fields");
				for (Entry<String, JsonValue> fields : fieldJsonObject.entrySet()) {
					AttributeData attributeData = new AttributeData();
					String attrNameTxt = fields.getKey();
					attributeData.setAttrNameTxt(attrNameTxt);
					if (!fieldJsonObject.isNull(attrNameTxt)) {
						JsonObject jsonAttrData = fieldJsonObject.getJsonObject(attrNameTxt);
						attributeData.setAttrValue(jsonAttrData.getString("valueString"));
						float pct = Float.valueOf(jsonAttrData.get("confidence").toString()) * 100;
						attributeData.setConfidencePct(pct);
					}
					attributeData.setExtractType(EnumExtractType.CUSTOM_LOGIC);
					attributes.add(attributeData);
				}
			}
		}
		String groupName = extractorConfigData.getExecuteRuleTrueCondition().getAttrNameValue();
		List<AttributeData> attributes1 = new ArrayList<AttributeData>();
		attributes1.add(AttributeDataHelper.createMultipleAttibuteElement(
				EnumSystemAttributeName.MULTI_ATTRIBUTE.getCde(), groupName, attributes));
		return attributes1;
	}

}
