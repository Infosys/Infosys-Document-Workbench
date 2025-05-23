/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.extractor.rule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

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
import com.infosys.ainauto.docwb.engine.extractor.common.EngineExtractorConstants;
import com.infosys.ainauto.docwb.engine.extractor.service.attribute.AttributeExtractorService.AttributeMetaData;
import com.infosys.ainauto.docwb.engine.extractor.service.attribute.AttributeExtractorService.AttributeResData;
import com.infosys.ainauto.docwb.engine.extractor.service.attribute.AttributeExtractorService.AttributeServiceApiV2ResData;
import com.infosys.ainauto.docwb.engine.extractor.service.attribute.AttributeExtractorService.AttributeServiceResponseData;
import com.infosys.ainauto.docwb.engine.extractor.service.attribute.AttributeExtractorService.ExtractorData;
import com.infosys.ainauto.docwb.engine.extractor.service.attribute.AttributeExtractorService.ExtractorData.ExtractorConfigAttributeData;
import com.infosys.ainauto.docwb.engine.extractor.service.attribute.AttributeExtractorService.ExtractorData.ExtractorConfigData;
import com.infosys.ainauto.docwb.engine.extractor.service.attribute.AttributeExtractorService.ExtractorData.ExtractorConfigMappingData;
import com.infosys.ainauto.docwb.engine.extractor.service.attribute.AttributeExtractorService.ExtractorData.ExtractorConfigResponseData;
import com.infosys.ainauto.docwb.engine.extractor.service.attribute.AttributeExtractorService.RowValueResData;
import com.infosys.ainauto.docwb.engine.extractor.service.attribute.AttributeExtractorService.RowValueResData.ColumnValueResData;
import com.infosys.ainauto.docwb.engine.extractor.service.attribute.AttributeExtractorService.TableValueReData;
import com.infosys.ainauto.docwb.engine.extractor.service.attribute.IAttributeExtractorService;
import com.infosys.ainauto.docwb.web.api.IAttachmentService;
import com.infosys.ainauto.docwb.web.common.AttachmentDataHelper;
import com.infosys.ainauto.docwb.web.data.AnnotationData;
import com.infosys.ainauto.docwb.web.data.AttachmentData;
import com.infosys.ainauto.docwb.web.data.AttributeData;
import com.infosys.ainauto.docwb.web.data.DocumentData;
import com.infosys.ainauto.docwb.web.type.EnumExtractType;
import com.infosys.ainauto.docwb.web.type.EnumFileType;
import com.infosys.ainauto.docwb.web.type.EnumSystemAttributeName;

@Component
public class Rule000ExtractUsingConfigurationV2 extends Rule000ExtractUsingConfigurationBase {
	private static final String OCCURRENCE_NUM = "occurrenceNum";

	private static final String CONFIDENCE_PCT = "confidencePct";

	private static final String ROW = "row";

	private static final String GROUP_REGEX = "group\\d+";

	private static final String GROUP = "group";

	private static final Logger logger = LoggerFactory.getLogger(Rule000ExtractUsingConfigurationV2.class);

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

	private String extractorApiResponseV2Schema;

	private static final String ATTRIBUTE_EXTRACTOR_API_MAPPING_CONFIG = "attributeExtractorApiMappingConfigV2.json";
	private static final String ATTRIBUTE_EXTRACTOR_API_MAPPING_CONFIG_SCHEMA = "schemaAttributeExtractorApiMappingConfigV2.json";
	private static final String ATTRIBUTE_EXTRACTOR_API_RESPONSE_V2_SCHEMA = "schemaAttributeExtractorApiResponseV2.json";

	@PostConstruct
	private void init() {
		mapper = new ObjectMapper();
		docwbAttachmentService = docWbApiClient.getAttachmentService();
		extractorData = readAttributeExtractorConfigFile(ATTRIBUTE_EXTRACTOR_API_MAPPING_CONFIG);
		extractorConfigDataSchema = FileUtility.readResourceFile(ATTRIBUTE_EXTRACTOR_API_MAPPING_CONFIG_SCHEMA);
		extractorApiResponseV2Schema = FileUtility.readResourceFile(ATTRIBUTE_EXTRACTOR_API_RESPONSE_V2_SCHEMA);
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
				logger.error("Error occurred in processing schema in V2 : " + e.toString());
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
			List<AnnotationData> annotationDataList = new ArrayList<>();
			if (ruleType.isEmpty() || ruleType.equals(DocwbEngineCoreConstants.RULE_TYPE_DOCUMENT)) {
				ExtractorConfigData extractorConfigData = AttributeDataHelper
						.getConfiguredExtractorApiDataByAttrNameCde(extractorData,
								EnumSystemAttributeName.DOCUMENT_TYPE.getCde(), documentData);

				if (extractorConfigData != null) {
					List<ExtractorConfigAttributeData> extractorConfigAttributeDataList = extractorConfigData
							.getResponse().getMapping().getAttributes();
					// If validation fails against configured attribute mapping will throw exception
					validateConfigAttributeData(extractorConfigAttributeDataList);

					List<AttachmentData> attachmentDataList = AttachmentDataHelper
							.sortAttachmentDataBasedOnLogicalNames(docwbAttachmentService
									.getAttachmentList(documentData.getDocId(), tempDownloadPath, EnumFileType.ALL));
					Map<String, List<AttachmentData>> attachmentGroup = AttachmentDataHelper
							.groupAttachment(attachmentDataList);

					if (ListUtility.hasValue(attachmentDataList) && extractorConfigData != null) {
						for (String groupName : attachmentGroup.keySet()) {
							// TODO 12/02/2020  - Remove if condition & break statement for attribute
							// extraction for all files inside zip.
							if (groupName.equals(attachmentDataList.get(0).getGroupName())) {
								List<AttachmentData> groupedAttachmentList = attachmentGroup.get(groupName);
								extractAttributes(documentData, extractorConfigData, groupedAttachmentList,
										responseDocumentData, reExtractParamData, annotationDataList,
										DocwbEngineCoreConstants.RULE_TYPE_DOCUMENT);
								break;
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
			List<AttachmentData> attachmentDataList, AttributeServiceResponseData attributeServiceResponseData,
			ExtractorConfigData extractorConfigData, DocumentData reExtractParamData,
			List<AnnotationData> annotationDataList) {
		AttachmentData resultAttachmentData = new AttachmentData();
		AttachmentData attachmentData = AttachmentDataHelper.getMainAttachmentData(attachmentDataList);
		long attachmentId = attachmentData.getAttachmentId();
		resultAttachmentData.setAttachmentId(attachmentId);
		List<AttributeData> attributes = convertResAttributeToConfigAttribute(attributeServiceResponseData,
				extractorConfigData, annotationDataList);
		attributes = filterReExtractParamAttachAttributes(attributes, reExtractParamData, attachmentId);
		resultAttachmentData.setAttributes(attributes);
		resultAttachmentData.setAnnotations(annotationDataList);
		responseDocumentData.setAttachmentDataList(new ArrayList<>());
		responseDocumentData.getAttachmentDataList().add(resultAttachmentData);

	}

	/**
	 * Main method to map the API responses as configured attributes in config file
	 */
	private List<AttributeData> convertResAttributeToConfigAttribute(
			AttributeServiceResponseData attributeServiceResponseData, ExtractorConfigData extractorConfigData,
			List<AnnotationData> annotationDataList) {
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
		if (!attributeServiceResponseData.getTables().isEmpty()) {
			Map<String, List<ExtractorConfigAttributeData>> extractorConfigAttributeDataTableMap = extractorConfigAttributeDataList
					.stream()
					.collect(Collectors.groupingBy(ExtractorConfigAttributeData::getTableName, Collectors.toList()));
			attributeServiceResponseData.getTables().forEach((tableId, table) -> {
				ExtractorConfigAttributeData extractorConfigAttributeData = null;
				if (extractorConfigAttributeDataTableMap.isEmpty()) {
					extractorConfigAttributeData = new ExtractorConfigAttributeData();
					extractorConfigAttributeData.setAttrNameCde(EnumSystemAttributeName.MULTI_ATTRIBUTE_TABLE.getCde());
					extractorConfigAttributeData.setTableName(table.getName());
					extractorConfigAttributeData.setAttributes(new ArrayList<>());
				} else {
					if (extractorConfigAttributeDataTableMap.containsKey(table.getName())) {
						extractorConfigAttributeData = extractorConfigAttributeDataTableMap.get(table.getName()).get(0);
					} else {
						extractorConfigAttributeData = new ExtractorConfigAttributeData();
						extractorConfigAttributeData
								.setAttrNameCde(EnumSystemAttributeName.MULTI_ATTRIBUTE_TABLE.getCde());
						extractorConfigAttributeData.setTableName(table.getName());
						extractorConfigAttributeData.setAttributes(new ArrayList<>());
					}
				}
				AttributeMetaData attributeMetaData = createAttributeMetaData(extractorConfigAttributeData);
				if (attributeMetaData.getTableName().equals(table.getName())) {
					tableNameCdeMap.put(attributeMetaData.getTableName(), attributeMetaData.getAttrNameCde());
					consolidateTabularAttributeDataList(nonMultiAttributeDataList, multiAttributesGrpByTableName,
							tableNameCdeMap, extractorConfigAttributeData, table, attributeMetaData);
				}

			});
		}
		if (ListUtility.hasValue(attributeServiceResponseData.getAttributes())) {
			Map<String, List<ExtractorConfigAttributeData>> extractorConfigAttributeDataMap = extractorConfigAttributeDataList
					.stream()
					.filter(configData -> configData.getAttrNameCde() != EnumSystemAttributeName.MULTI_ATTRIBUTE_TABLE
							.getCde())
					.collect(Collectors.groupingBy(ExtractorConfigAttributeData::getResAttrName, Collectors.toList()));
			attributeServiceResponseData.getAttributes().forEach(attribute -> {
				consolidateNonTabularAttributeDataList(nonMultiAttributeDataList, multiAttributesGrpByTableName,
						extractorConfigAttributeDataMap, tableNameCdeMap, attribute, annotationDataList);
			});
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
	 * Create Multi-Attribute Table Data objects as configured in config file
	 */
	private void consolidateTabularAttributeDataList(List<AttributeData> nonMultiAttributeDataList,
			Map<String, List<AttributeData>> multiAttributesGrpByTableName, Map<String, Integer> tableNameCdeMap,
			ExtractorConfigAttributeData extractorConfigAttributeData, TableValueReData table,
			AttributeMetaData attributeMetaData) {
		table.getRowObjects().sort(Comparator.comparing(RowValueResData::getRowName));
		Map<String, String> tableColNameOrderMap = new LinkedHashMap<String, String>(); // ordered
		if (table.getColumnLabels() != null && table.getColumnOrder().size() == table.getColumnLabels().size()) {
			for (int i = 0; i < table.getColumnOrder().size(); i++) {
				tableColNameOrderMap.put(table.getColumnOrder().get(i), table.getColumnLabels().get(i));
			}
		}
		for (RowValueResData rowValueResData : table.getRowObjects()) {
			int rowId = Integer.valueOf(rowValueResData.getRowName().split(ROW)[1]);
			if (rowId == 1) {
				if (table.getColumnOrder().size() != rowValueResData.getColumns().size()) {
					List<ColumnValueResData> columns = new ArrayList<>();
					Map<String, List<ColumnValueResData>> colNameMap = rowValueResData.getColumns().stream()
							.collect(Collectors.groupingBy(ColumnValueResData::getColName));
					table.getColumnOrder().forEach(colStr -> {
						if (colNameMap.containsKey(colStr)) {
							columns.addAll(colNameMap.get(colStr));
						} else {
							ColumnValueResData columnValueResData = new ColumnValueResData();
							columnValueResData.setColName(colStr);
							columnValueResData.setColValue(null);
							columns.add(columnValueResData);
						}
					});
					if (columns.size() > rowValueResData.getColumns().size())
						rowValueResData.setColumns(columns);

				}
			}
			rowValueResData.getColumns()
					.sort(Comparator.comparing(col -> table.getColumnOrder().indexOf(col.getColName())));
			for (ColumnValueResData columnValueResData : rowValueResData.getColumns()) {
				if (columnValueResData != null) {
					String attrColName = columnValueResData.getColName();
					String attrNameTxt = getAttrNameTxtByConfName(extractorConfigAttributeData, attrColName);
					attributeMetaData.setAttrNameTxt(attrNameTxt);
					AttributeData attributeData = new AttributeData();
					// AnnotationData annotationData = new AnnotationData();
					if (columnValueResData.getColValue() != null) {
						columnValueResData.getColValue().forEach((key, value) -> {
							if (key.equalsIgnoreCase(EngineExtractorConstants.VALUE)) {
								attributeData.setAttrValue(StringUtility.hasTrimmedValue(String.valueOf(value))
										? (String.valueOf(value)).trim()
										: "");
								// annotationData.setValue(attributeData.getAttrValue());
							} else if (key.equalsIgnoreCase(CONFIDENCE_PCT)) {
								attributeData.setConfidencePct(Float.valueOf(String.valueOf(value)));
							}
							// else if (key.equalsIgnoreCase("ocurrenceNum")) {
							// annotationData.setOccurrenceNum(Integer.valueOf(String.valueOf(value)));
							// }
						});
					}
					attributeData
							.setAttrNameCde(
									attributeMetaData.isMultiAttributes() ? 0 : attributeMetaData.getAttrNameCde())
							.setExtractType(EnumExtractType.CUSTOM_LOGIC);
					if (StringUtility.hasTrimmedValue(attributeMetaData.getAttrNameTxt())) {
						attributeData.setAttrNameTxt(attributeMetaData.getAttrNameTxt());
					} else {
						if (tableColNameOrderMap.containsKey(attrColName)) {
							attributeData.setAttrNameTxt(tableColNameOrderMap.get(attrColName));
						} else {
							attributeData.setAttrNameTxt(attrColName);
						}
					}

					consolidateBuiltAttributeData(nonMultiAttributeDataList, multiAttributesGrpByTableName,
							attributeMetaData, rowId, attributeData);
					// if (StringUtility.hasTrimmedValue(annotationData.getValue())) {
					// annotationData.setLabel(attributeData.getAttrNameTxt());
					// annotationDataList.add(annotationData);
					// }
				}
			}
		}
	}

	/**
	 * Create Multi-Attribute & normal attribute Data objects as configured in
	 * config file
	 */
	private void consolidateNonTabularAttributeDataList(List<AttributeData> nonMultiAttributeDataList,
			Map<String, List<AttributeData>> multiAttributesGrpByTableName,
			Map<String, List<ExtractorConfigAttributeData>> extractorConfigAttributeDataMap,
			Map<String, Integer> tableNameCdeMap, AttributeResData attributeResData,
			List<AnnotationData> annotationDataList) {
		if (attributeResData != null) {
			AttributeMetaData attributeMetaData = new AttributeMetaData();
			AttributeData attributeData = new AttributeData();
			String attrName = attributeResData.getAttrName();
			if (!extractorConfigAttributeDataMap.isEmpty() && extractorConfigAttributeDataMap.containsKey(attrName)) {
				ExtractorConfigAttributeData extractorConfigAttributeData = extractorConfigAttributeDataMap
						.get(attrName).get(0);
				attributeMetaData = createAttributeMetaData(extractorConfigAttributeData);
				String attrNameTxt = getAttrNameTxtByConfName(extractorConfigAttributeData, attrName);
				attributeMetaData.setAttrNameTxt(attrNameTxt);
				if (attributeMetaData.isMultiAttribute()) {
					if (!attrNameTxt.isEmpty()) {
						tableNameCdeMap.put(attributeMetaData.getGroupName(), attributeMetaData.getAttrNameCde());
					}
					attributeData = createAttributeFromResponse(attributeResData, attributeMetaData, attrName,
							annotationDataList);
				} else {
					if (attributeMetaData.getResAttrName().equalsIgnoreCase(attributeResData.getAttrName())) {
						attributeData = createAttributeFromResponse(attributeResData, attributeMetaData, attrName,
								annotationDataList);
					}
				}
			} else if (!extractorConfigAttributeDataMap.isEmpty() && extractorConfigAttributeDataMap.containsKey("")) {
				ExtractorConfigAttributeData extractorConfigAttributeData = extractorConfigAttributeDataMap.get("")
						.get(0);
				attributeMetaData = createAttributeMetaData(extractorConfigAttributeData);
				String attrNameTxt = getAttrNameTxtByConfName(extractorConfigAttributeData, attrName);
				attributeMetaData.setAttrNameTxt(attrNameTxt);
				tableNameCdeMap.put(attributeMetaData.getGroupName(), attributeMetaData.getAttrNameCde());
				attributeData = createAttributeFromResponse(attributeResData, attributeMetaData, attrName,
						annotationDataList);

			} else {
				attributeMetaData.setAttrNameCde(EnumSystemAttributeName.MULTI_ATTRIBUTE.getCde());
				attributeMetaData.setMultiAttribute(true);
				attributeMetaData.setMultiAttributes(true);
				if (tableNameCdeMap.keySet().parallelStream().noneMatch(groupName -> groupName.matches(GROUP_REGEX))) {
					attributeMetaData.setGroupName(GROUP + StringUtility.getRangeOfRandomNumberInInt(100000, 1000000));
					tableNameCdeMap.put(attributeMetaData.getGroupName(), attributeMetaData.getAttrNameCde());
				} else {
					attributeMetaData.setGroupName(tableNameCdeMap.keySet().parallelStream()
							.filter(groupName -> groupName.matches(GROUP_REGEX)).collect(Collectors.toList()).get(0));
				}
				attributeData = createAttributeFromResponse(attributeResData, attributeMetaData, attrName,
						annotationDataList);

			}
			consolidateBuiltAttributeData(nonMultiAttributeDataList, multiAttributesGrpByTableName, attributeMetaData,
					0, attributeData);
		}
	}

	/**
	 * Create a attributeData object from response obj and metadata obj
	 * 
	 * @param attributeResData
	 * @param attributeMetaData
	 * @param attrName
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private AttributeData createAttributeFromResponse(AttributeResData attributeResData,
			AttributeMetaData attributeMetaData, String attrName, List<AnnotationData> annotationDataList) {
		AttributeData attributeData = new AttributeData();
		attributeData.setAttrNameCde(attributeMetaData.isMultiAttributes() ? 0 : attributeMetaData.getAttrNameCde())
				.setExtractType(EnumExtractType.CUSTOM_LOGIC);
		if (StringUtility.hasTrimmedValue(attributeMetaData.getAttrNameTxt())) {
			attributeData.setAttrNameTxt(attributeMetaData.getAttrNameTxt());
		} else {
			if (StringUtility.hasTrimmedValue(attributeResData.getLabel()))
				attributeData.setAttrNameTxt(attributeResData.getLabel());
			else
				attributeData.setAttrNameTxt(attrName);
		}
		for (Map.Entry<String, Object> entry : attributeResData.getAttrValue().entrySet()) {
			if (entry.getValue() instanceof List) {
				if (entry.getKey().equalsIgnoreCase(EngineExtractorConstants.VALUE)) {
					List<String> valueList = (List<String>) entry.getValue();
					valueList.removeAll(Collections.singleton(null));
					attributeData.setAttrValue(AttributeDataHelper.concatAttrValueByDelimiter(valueList));
				} else if (entry.getKey().equalsIgnoreCase(CONFIDENCE_PCT)) {
					List<String> strConfList = Arrays
							.asList(entry.getValue().toString().replace("[", "").replace("]", "").split(","));
					List<Float> confList = new ArrayList<>();
					strConfList.forEach(str -> {
						if (!str.trim().contains("null")) {
							confList.add(Float.valueOf(str));
						}
					});
					attributeData.setConfidencePct(AttributeDataHelper.getConfidenceAvg(confList));
				}
			} else {
				if (entry.getKey().equalsIgnoreCase(EngineExtractorConstants.VALUE)) {
					attributeData.setAttrValue(StringUtility.hasTrimmedValue(String.valueOf(entry.getValue()))
							? (String.valueOf(entry.getValue())).trim()
							: "");
				} else if (entry.getKey().equalsIgnoreCase(CONFIDENCE_PCT)) {
					attributeData.setConfidencePct(Float.valueOf(String.valueOf(entry.getValue())));
				}
			}
		}
		annotationDataList.addAll(getAnnotationsFromAttribute(attributeData, attributeResData));
		return attributeData;
	}

	/**
	 * consolidate the given attribute into tabular or multi-attribute or normal
	 * attribute
	 * 
	 * @param nonMultiAttributeDataList
	 * @param multiAttributesGrpByTableName
	 * @param attributeMetaData
	 * @param rowId
	 * @param attributeData
	 */
	private void consolidateBuiltAttributeData(List<AttributeData> nonMultiAttributeDataList,
			Map<String, List<AttributeData>> multiAttributesGrpByTableName, AttributeMetaData attributeMetaData,
			int rowId, AttributeData attributeData) {
		if (!attributeMetaData.isMultiAttributes()) {
			createNormalAttributeDataList(nonMultiAttributeDataList, attributeData);
		} else if (attributeMetaData.isMultiAttributes()) {
			if (attributeMetaData.isMultiAttributeTable()) {
				attributeData.setId(rowId);
				if (multiAttributesGrpByTableName.containsKey(attributeMetaData.getTableName())) {
					multiAttributesGrpByTableName.get(attributeMetaData.getTableName()).add(attributeData);
				} else {
					List<AttributeData> attributeDataListTemp = new ArrayList<AttributeData>();
					attributeDataListTemp.add(attributeData);
					multiAttributesGrpByTableName.put(attributeMetaData.getTableName(), attributeDataListTemp);
				}
			} else {
				if (multiAttributesGrpByTableName.containsKey(attributeMetaData.getGroupName())) {
					multiAttributesGrpByTableName.get(attributeMetaData.getGroupName()).add(attributeData);
				} else {
					List<AttributeData> attributeDataListTemp = new ArrayList<AttributeData>();
					attributeDataListTemp.add(attributeData);
					multiAttributesGrpByTableName.put(attributeMetaData.getGroupName(), attributeDataListTemp);
				}
			}
		}
	}

	private List<AnnotationData> getAnnotationsFromAttribute(AttributeData attribute,
			AttributeResData attributeResData) {
		List<AnnotationData> annotationDatas = new ArrayList<>();
		if (StringUtility.hasTrimmedValue(attribute.getAttrValue())) {
			List<String> values = Arrays
					.asList(attribute.getAttrValue().split(EngineExtractorConstants.ATTR_NAME_VALUE_DELIMITER));
			values.forEach(value -> {
				AnnotationData annotationData = new AnnotationData();
				annotationData.setLabel(attribute.getAttrNameTxt());
				annotationData.setValue(value);
				annotationDatas.add(annotationData);
			});
		}

		if (attributeResData.getAttrValue().containsKey(OCCURRENCE_NUM)) {
			if (attributeResData.getAttrValue().get(OCCURRENCE_NUM) instanceof List) {
				@SuppressWarnings("unchecked")
				List<String> valueList = (List<String>) attributeResData.getAttrValue().get(OCCURRENCE_NUM);
				if (valueList.size() == annotationDatas.size()) {
					for (int i = 0; i < valueList.size(); i++) {
						if (valueList.get(i) != null) {
							annotationDatas.get(i).setOccurrenceNum(Integer.valueOf(String.valueOf(valueList.get(i))));
						}
					}
				}
			} else {
				annotationDatas.get(0).setOccurrenceNum((int) attributeResData.getAttrValue().get(OCCURRENCE_NUM));
			}
		}
		return annotationDatas;

	}

	private void extractAttributes(DocumentData documentData, ExtractorConfigData extractorConfigData,
			List<AttachmentData> attachmentDataList, DocumentData responseDocumentData, DocumentData reExtractParamData,
			List<AnnotationData> annotationDataList, String ruleType) throws Exception {
		String responseData = attributeExtractorService.extractAttributes(extractorConfigData,
				extractorApiResponseV2Schema, documentData, createDBAttachmentReqList(attachmentDataList), ruleType);
		if (StringUtility.hasValue(responseData)) {
			AttributeServiceApiV2ResData attributeServiceApiResData = mapper.readValue(responseData,
					AttributeServiceApiV2ResData.class);
			AttributeServiceResponseData attributeServiceResponseData = new AttributeServiceResponseData();
			attributeExtractorService.handleAttributeServiceApiResponse(attributeServiceApiResData,
					attributeServiceResponseData);
			if (attributeServiceResponseData != null && (!attributeServiceResponseData.getTables().isEmpty()
					|| !attributeServiceResponseData.getAttributes().isEmpty())) {
				setAttachmentAttributesToDocumentData(responseDocumentData, attachmentDataList,
						attributeServiceResponseData, extractorConfigData, reExtractParamData, annotationDataList);
			}
		}
	}
}
