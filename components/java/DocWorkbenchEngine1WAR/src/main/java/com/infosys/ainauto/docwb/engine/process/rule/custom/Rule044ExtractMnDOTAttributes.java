/** =============================================================================================================== *
 * Copyright 2022 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


package com.infosys.ainauto.docwb.engine.process.rule.custom;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

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
import com.infosys.ainauto.docwb.engine.service.ner.ExtractService;
import com.infosys.ainauto.docwb.engine.service.ner.ExtractService.NERResData;
import com.infosys.ainauto.docwb.engine.service.ner.IExtractService;
import com.infosys.ainauto.docwb.web.api.IAttachmentService;
import com.infosys.ainauto.docwb.web.common.AttributeHelper;
import com.infosys.ainauto.docwb.web.data.AttachmentData;
import com.infosys.ainauto.docwb.web.data.AttributeData;
import com.infosys.ainauto.docwb.web.data.DocumentData;
import com.infosys.ainauto.docwb.web.type.EnumExtractType;
import com.infosys.ainauto.docwb.web.type.EnumFileType;
import com.infosys.ainauto.docwb.web.type.EnumSystemAttributeName;

@Component
public class Rule044ExtractMnDOTAttributes extends AttributeExtractRuleAsyncBase {

	@Autowired
	private Environment environment;

	@Autowired
	private DocWbApiClient docWbApiClient;

	private IAttachmentService attachmentService;

	private static final Logger logger = LoggerFactory.getLogger(Rule044ExtractMnDOTAttributes.class);

	@Autowired
	private IExtractService iExtractService;

	private static final String DOCWB_ENGINE_TEMP_PATH = "docwb.engine.temp.path";
	private static final String NAME = "name";
	private static final String TRANSACTION_ID = "transactionId";
	private static final String VALUE = "value";
	private static final String BODY_CONTENT = "bodyContent";
	private static final String PARAMETERS = "parameters";

	@PostConstruct
	private void init() {
		attachmentService = docWbApiClient.getAttachmentService();
	}

	public void doExtractAsync(List<Object> objList, IAttributeExtractRuleListener attributeExtractRuleListener) {
		try {
			DocumentData responseDocumentData = new DocumentData();

			String attachmentTempPath = FileUtility.getAbsolutePath(environment.getProperty(DOCWB_ENGINE_TEMP_PATH));
			DocumentData documentData = (DocumentData) objList.get(0);
			String ruleType = (objList != null && objList.size() > 1) ? objList.get(1).toString() : "";
			List<AttachmentData> attachmentDataList = attachmentService.getAttachmentList(documentData.getDocId(),
					attachmentTempPath, EnumFileType.ALL);
			String content = AttributeHelper.getAttributeValue(documentData, EnumSystemAttributeName.CONTENT.getCde()); // 9=Content

			ExtractService extractService = new ExtractService(environment);
			List<ExtractService.FilePathData> fileDataList = new ArrayList<>();
			String paramFileName = UUID.randomUUID().toString() + EngineConstants.EXTRACTION_JSON_FILE_EXTENSION;

			if (StringUtility.hasTrimmedValue(content)) {
				ExtractService.FilePathData filePathData = extractService.new FilePathData();

				JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
				jsonObjectBuilder.add(TRANSACTION_ID, StringUtility.generateTransactionId());
				JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();

				JsonObjectBuilder parameterJsonObjectBuilder = Json.createObjectBuilder();
				parameterJsonObjectBuilder.add(NAME, BODY_CONTENT);
				parameterJsonObjectBuilder.add(VALUE, content);
				jsonArrayBuilder.add(parameterJsonObjectBuilder);

				jsonObjectBuilder.add(PARAMETERS, jsonArrayBuilder);

				JsonObject jsonObject = jsonObjectBuilder.build();
				String fileContent = jsonObject.toString();

				String filePath = FileUtility.getConcatenatedPath(attachmentTempPath, paramFileName);
				FileUtility.saveFile(filePath, fileContent);
				filePathData.setFileName(paramFileName);
				filePathData.setFilePath(filePath);
				fileDataList.add(filePathData);
			}

			// Step 1 - Check if attachments are present
			if (ListUtility.hasValue(attachmentDataList)) {
				// Step 2 - Make service call to get extracted data
				for (AttachmentData attachmentData : attachmentDataList) {
					ExtractService.FilePathData filePathData = extractService.new FilePathData();
					filePathData.setFileName(attachmentData.getPhysicalName());
					filePathData.setFilePath(attachmentData.getPhysicalPath());
					fileDataList.add(filePathData);
				}
				List<NERResData> nerResDataList = iExtractService.extractAttributes(fileDataList, paramFileName);
				for (NERResData nerResData : nerResDataList) {
					if (nerResData.getFileName().equals(paramFileName)) {
						List<AttributeData> attributeDataList = convertAttributeDataList(nerResData.getAttributes());
						if (ListUtility.hasValue(attributeDataList)) {
							responseDocumentData.setAttributes(attributeDataList);
						}
					} else {
						for (AttachmentData attachmentData : attachmentDataList) {
							if (nerResData.getFileName().equals(attachmentData.getPhysicalName())) {
								List<AttributeData> attachmentAttributeDataList = convertAttributeDataList(
										nerResData.getAttributes());
								if (ListUtility.hasValue(attachmentAttributeDataList)) {
									// Finally, add attachmentAttributeDataList to attachment object
									AttachmentData resultAttachmentData = new AttachmentData();
									resultAttachmentData.setAttachmentId(attachmentData.getAttachmentId());
									resultAttachmentData.setAttributes(attachmentAttributeDataList);
									if (responseDocumentData.getAttachmentDataList() == null) {
										responseDocumentData.setAttachmentDataList(new ArrayList<>());
									}
									responseDocumentData.getAttachmentDataList().add(resultAttachmentData);
								}
								break;
							}
						}
					}
				}
			} else {
				if (ListUtility.hasValue(fileDataList)) {
					List<NERResData> nerResDataList = iExtractService.extractAttributes(fileDataList, paramFileName);
					for (NERResData nerResData : nerResDataList) {
						if (nerResData.getFileName().equals(paramFileName)) {
							List<AttributeData> attributeDataList = convertAttributeDataList(
									nerResData.getAttributes());
							if (ListUtility.hasValue(attributeDataList)) {
								responseDocumentData.setAttributes(attributeDataList);
							}
							break;
						}
					}
				}
			}
			if (ListUtility.hasValue(fileDataList)) {
				for (ExtractService.FilePathData filePathData : fileDataList) {
					FileUtility.deleteFile(filePathData.getFilePath());
				}
			}

			if (ruleType.equals(DocwbEngineCoreConstants.RULE_TYPE_DOCUMENT)) {
				responseDocumentData.setAttributes(null);
			} else if (ruleType.equals(DocwbEngineCoreConstants.RULE_TYPE_EMAIL)) {
				responseDocumentData.setAttachmentDataList(null);
			}

			attributeExtractRuleListener.onAttributeExtractionComplete(null, responseDocumentData);
		} catch (Exception ex) {
			logger.error("Error occurred in extracting data", ex);
			attributeExtractRuleListener.onAttributeExtractionComplete(ex, null);

		}

	}

	private List<AttributeData> convertAttributeDataList(List<AttributeData> fileAttributeDataList) {

		List<AttributeData> attributeDataList = null;
		if (ListUtility.hasValue(fileAttributeDataList)) {
			attributeDataList = new ArrayList<>();
			List<AttributeData> innerAttributeDataList = new ArrayList<>();

			for (AttributeData fileAttributeData : fileAttributeDataList) {
				AttributeData innerAttributeData = new AttributeData();
				innerAttributeData.setAttrNameTxt(fileAttributeData.getAttrNameTxt());
				innerAttributeData.setAttrValue(fileAttributeData.getAttrValue());
				innerAttributeData.setConfidencePct(fileAttributeData.getConfidencePct());
				innerAttributeData.setExtractType(EnumExtractType.CUSTOM_LOGIC);
				innerAttributeDataList.add(innerAttributeData);
			}
			// Add all nested attributes under single multi-attribute
			AttributeData attributeData = new AttributeData();
			attributeData.setAttrNameCde(EnumSystemAttributeName.MULTI_ATTRIBUTE.getCde())
					.setAttrNameTxt(EngineConstants.GROUP_NAME_MNDOT)
					.setConfidencePct(EngineConstants.CONFIDENCE_PCT_UNDEFINED)
					.setExtractType(EnumExtractType.DIRECT_COPY).setAttributeDataList(innerAttributeDataList);
			attributeDataList.add(attributeData);

		}

		return attributeDataList;
	}
}
