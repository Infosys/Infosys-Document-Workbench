/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.extractor.rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.naming.ConfigurationException;

import org.everit.json.schema.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infosys.ainauto.commonutils.FileUtility;
import com.infosys.ainauto.commonutils.JsonSchemaUtil;
import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.engine.core.common.DocwbEngineCoreConstants;
import com.infosys.ainauto.docwb.engine.core.rule.IAttributeExtractRuleListener;
import com.infosys.ainauto.docwb.engine.core.service.DocWbApiClient;
import com.infosys.ainauto.docwb.engine.extractor.common.AttributeDataHelper;
import com.infosys.ainauto.docwb.engine.extractor.service.attribute.AttributeExtractorService.AttributeMetaData;
import com.infosys.ainauto.docwb.engine.extractor.service.attribute.AttributeExtractorService.AttributeServiceApiResData;
import com.infosys.ainauto.docwb.engine.extractor.service.attribute.AttributeExtractorService.AttributeServiceApiResData.AttributeServiceResData;
import com.infosys.ainauto.docwb.engine.extractor.service.attribute.AttributeExtractorService.AttributeServiceApiResData.CellValueData;
import com.infosys.ainauto.docwb.engine.extractor.service.attribute.AttributeExtractorService.AttributeServiceApiResData.CloumnValueData;
import com.infosys.ainauto.docwb.engine.extractor.service.attribute.AttributeExtractorService.AttributeServiceApiResData.RowValueData;
import com.infosys.ainauto.docwb.engine.extractor.service.attribute.AttributeExtractorService.AttributeServiceApiResData.TableValueData;
import com.infosys.ainauto.docwb.engine.extractor.service.attribute.AttributeExtractorService.ExtractorData;
import com.infosys.ainauto.docwb.engine.extractor.service.attribute.AttributeExtractorService.ExtractorData.ExtractorConfigAttributeData;
import com.infosys.ainauto.docwb.engine.extractor.service.attribute.AttributeExtractorService.ExtractorData.ExtractorConfigData;
import com.infosys.ainauto.docwb.engine.extractor.service.attribute.AttributeExtractorService.ExtractorData.ExtractorConfigMappingData;
import com.infosys.ainauto.docwb.engine.extractor.service.attribute.AttributeExtractorService.ExtractorData.ExtractorConfigResponseData;
import com.infosys.ainauto.docwb.engine.extractor.service.attribute.IAttributeExtractorService;
import com.infosys.ainauto.docwb.web.api.IAttachmentService;
import com.infosys.ainauto.docwb.web.common.AttachmentDataHelper;
import com.infosys.ainauto.docwb.web.data.AttachmentData;
import com.infosys.ainauto.docwb.web.data.AttributeData;
import com.infosys.ainauto.docwb.web.data.DocumentData;
import com.infosys.ainauto.docwb.web.type.EnumExtractType;
import com.infosys.ainauto.docwb.web.type.EnumFileType;
import com.infosys.ainauto.docwb.web.type.EnumSystemAttributeName;

@Component
public class Rule000ExtractUsingConfigurationV1 extends Rule000ExtractUsingConfigurationBase {

	private static final Logger logger = LoggerFactory.getLogger(Rule000ExtractUsingConfigurationV1.class);

	@Autowired
	private IAttributeExtractorService attributeExtractorService;
	@Autowired
	private Environment environment;
	@Autowired
	private DocWbApiClient docWbApiClient;

	private ObjectMapper mapper;

	private IAttachmentService docwbAttachmentService;

	private ExtractorData extractorData;

	private String extractorConfigDataSchema;

	private String extractorApiResponseSchema;

	private static final String ATTRIBUTE_EXTRACTOR_API_MAPPING_CONFIG = "attributeExtractorApiMappingConfigV1.json";
	private static final String ATTRIBUTE_EXTRACTOR_API_MAPPING_CONFIG_SCHEMA = "schemaAttributeExtractorApiMappingConfigV1.json";
	private static final String ATTRIBUTE_EXTRACTOR_API_RESPONSE_V1_SCHEMA = "schemaAttributeExtractorApiResponseV1.json";

	@PostConstruct
	private void init() {
		mapper = new ObjectMapper();
		docwbAttachmentService = docWbApiClient.getAttachmentService();
		extractorData = readAttributeExtractorConfigFile(ATTRIBUTE_EXTRACTOR_API_MAPPING_CONFIG);
		extractorConfigDataSchema = FileUtility.readResourceFile(ATTRIBUTE_EXTRACTOR_API_MAPPING_CONFIG_SCHEMA);
		extractorApiResponseSchema = FileUtility.readResourceFile(ATTRIBUTE_EXTRACTOR_API_RESPONSE_V1_SCHEMA);
		if (extractorData != null) {
			try {
				ValidationException exception = JsonSchemaUtil.validateSchema(extractorConfigDataSchema,
						mapper.writeValueAsString(extractorData));
				if (exception != null) {
					logger.error("Violated Schema : " + exception.getViolatedSchema().toString());
					logger.error(
							"Json Schema Validation failed due to following reasons : " + exception.getAllMessages());
					throw exception;
				}
			} catch (JsonProcessingException e) {
				logger.error("Error occurred in processing schema in V1 : " + e.toString());
			}
		}
	}

	public void doExtractAsync(List<Object> objList, IAttributeExtractRuleListener attributeExtractRuleListener) {
		String tempDownloadPath = FileUtility.getAbsolutePath(environment.getProperty("docwb.engine.temp.path"));
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
						.getConfiguredExtractorApiDataByAttrNameCde(extractorData,
								EnumSystemAttributeName.DOCUMENT_TYPE.getCde(), documentData);
				if (extractorConfigData != null) {
					List<ExtractorConfigAttributeData> extractorConfigAttributeDataList = extractorConfigData
							.getResponse().getMapping().getAttributes();
					// If validation fails against configured attribute mapping will throw exception
					validateConfigAttributeData(extractorConfigAttributeDataList);

					List<AttachmentData> attachmentDataList = docwbAttachmentService
							.getAttachmentList(documentData.getDocId(), tempDownloadPath, EnumFileType.SUPPORTED);

					if (ListUtility.hasValue(attachmentDataList) && extractorConfigData != null) {
						String responseData = attributeExtractorService.extractAttributes(extractorConfigData,
								extractorApiResponseSchema, documentData,
								createDBAttachmentReqList(attachmentDataList), DocwbEngineCoreConstants.RULE_TYPE_DOCUMENT);
						if (StringUtility.hasValue(responseData)) {
							AttributeServiceApiResData attributeServiceApiResData = mapper.readValue(responseData,
									AttributeServiceApiResData.class);
							AttributeServiceResData attributeServiceResData = attributeServiceApiResData.getResponse();
							if (attributeServiceResData != null
									&& ListUtility.hasValue(attributeServiceResData.getTables())) {
								setAttachmentAttributesToDocumentData(responseDocumentData, attachmentDataList,
										attributeServiceResData, extractorConfigData, reExtractParamData);

							}
						}
						attributeExtractRuleListener.onAttributeExtractionComplete(null, responseDocumentData);
					} else {
						attributeExtractRuleListener.onAttributeExtractionComplete(null, responseDocumentData);
					}
				} else {
					attributeExtractRuleListener.onAttributeExtractionComplete(null, responseDocumentData);
				}
			} else {
				attributeExtractRuleListener.onAttributeExtractionComplete(null, responseDocumentData);
			}

		} catch (Exception e) {
			logger.error("Error occurred in RuleToExtractCustomAttributes", e);
			attributeExtractRuleListener.onAttributeExtractionComplete(e, null);
		}
	}

	/**
	 * Set Attachment attributes result in response Document data
	 */
	private void setAttachmentAttributesToDocumentData(DocumentData responseDocumentData,
			List<AttachmentData> attachmentDataList, AttributeServiceResData attributeServiceResData,
			ExtractorConfigData extractorConfigData, DocumentData reExtractParamData) throws ConfigurationException {
		AttachmentData resultAttachmentData = new AttachmentData();
		AttachmentData attachmentData = AttachmentDataHelper.getMainAttachmentData(attachmentDataList);
		long attachmentId = attachmentData.getAttachmentId();
		resultAttachmentData.setAttachmentId(attachmentId);
		List<AttributeData> attributes = convertResAttributeToConfigAttribute(attributeServiceResData,
				extractorConfigData);
		attributes = filterReExtractParamAttachAttributes(attributes, reExtractParamData, attachmentId);
		resultAttachmentData.setAttributes(attributes);
		responseDocumentData.setAttachmentDataList(new ArrayList<>());
		responseDocumentData.getAttachmentDataList().add(resultAttachmentData);
	}

	/**
	 * Main method to map the API responses as configured attributes in config file
	 */
	private List<AttributeData> convertResAttributeToConfigAttribute(AttributeServiceResData attributeServiceResData,
			ExtractorConfigData extractorConfigData) throws ConfigurationException {
		List<AttributeData> attributeDataList = new ArrayList<AttributeData>();
		List<AttributeData> nonMultiAttributeDataList = new ArrayList<AttributeData>();
		List<AttributeData> multiAttributeDataList = new ArrayList<AttributeData>();
		Map<String, List<AttributeData>> multiAttributesGrpByTableName = new HashMap<String, List<AttributeData>>();
		ExtractorConfigResponseData extractorConfigResponseData = extractorConfigData.getResponse();
		if (extractorConfigResponseData == null || extractorConfigResponseData.getMapping() == null) {
			return attributeDataList;
		}
		ExtractorConfigMappingData extractorConfigMappingData = extractorConfigResponseData.getMapping();
		List<ExtractorConfigAttributeData> extractorConfigAttributeDataList = extractorConfigMappingData
				.getAttributes();
		Map<String, Integer> tableNameCdeMap = new HashMap<String, Integer>();
		for (ExtractorConfigAttributeData extractorConfigAttributeData : extractorConfigAttributeDataList) {

			for (TableValueData tableValueData : attributeServiceResData.getTables()) {
				AttributeMetaData attributeMetaData = createAttributeMetaData(extractorConfigAttributeData);
				if (!attributeMetaData.getTableName().equals(tableValueData.getTableName())) {
					continue;
				}

				for (CloumnValueData cloumnValueData : tableValueData.getColumns()) {
					String attrColName = cloumnValueData.getColName();
					if (!attributeMetaData.isMultiAttributes()
							&& !attrColName.equals(extractorConfigAttributeData.getColName())) {
						continue;
					}
					String attrNameTxt = getAttrNameTxtByColName(extractorConfigAttributeData, attrColName);
					attributeMetaData.setAttrNameTxt(attrNameTxt);
					if (attributeMetaData.isMultiAttributes() && !attrNameTxt.isEmpty()) {
						tableNameCdeMap.put(attributeMetaData.getTableName(), attributeMetaData.getAttrNameCde());
					}

					for (RowValueData rowValueData : cloumnValueData.getRows()) {
						consolidateAttributeDataList(multiAttributesGrpByTableName, nonMultiAttributeDataList,
								cloumnValueData, rowValueData, attributeMetaData);
					}
				}
			}
		}
		multiAttributeDataList = mapMultiAttributesFromConsolidatedAttrList(multiAttributesGrpByTableName,
				tableNameCdeMap);
		if (ListUtility.hasValue(multiAttributeDataList)) {
			attributeDataList.addAll(multiAttributeDataList);
		}
		if (ListUtility.hasValue(nonMultiAttributeDataList)) {
			attributeDataList.addAll(nonMultiAttributeDataList);
		}

		return attributeDataList;
	}

	/**
	 * Create Attribute Data object as configured in config file
	 */
	private void consolidateAttributeDataList(Map<String, List<AttributeData>> multiAttributesGrpByTableName,
			List<AttributeData> nonMultiAttributeDataList, CloumnValueData cloumnValueData, RowValueData rowValueData,
			AttributeMetaData attributeMetaData) {
		int rowId = rowValueData.getRowId();
		CellValueData cellValueData = rowValueData.getCell();
		if (cellValueData != null) {
			AttributeData attributeData = new AttributeData();
			attributeData.setAttrNameCde(attributeMetaData.isMultiAttributes() ? 0 : attributeMetaData.getAttrNameCde())
					.setExtractType(EnumExtractType.CUSTOM_LOGIC)
					.setAttrValue(
							StringUtility.hasTrimmedValue(cellValueData.getValue()) ? cellValueData.getValue().trim()
									: "")
					.setConfidencePct(cellValueData.getConfidencePct());
			if (StringUtility.hasTrimmedValue(attributeMetaData.getAttrNameTxt())) {
				attributeData.setAttrNameTxt(attributeMetaData.getAttrNameTxt());
			}
			if (!attributeMetaData.isMultiAttributes()) {
				createNormalAttributeDataList(nonMultiAttributeDataList, attributeData);
			} else if (attributeMetaData.isMultiAttributes() && !attributeMetaData.getAttrNameTxt().isEmpty()) {
				if (attributeMetaData.isMultiAttributeTable()) {
					attributeData.setId(rowId);
				}
				if (multiAttributesGrpByTableName.containsKey(attributeMetaData.getTableName())) {
					multiAttributesGrpByTableName.get(attributeMetaData.getTableName()).add(attributeData);
				} else {
					List<AttributeData> attributeDataListTemp = new ArrayList<AttributeData>();
					attributeDataListTemp.add(attributeData);
					multiAttributesGrpByTableName.put(attributeMetaData.getTableName(), attributeDataListTemp);
				}
			}
		}
	}

}
