/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.extractor.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.docwb.engine.extractor.service.attribute.AttributeExtractorService.ExtractorData;
import com.infosys.ainauto.docwb.engine.extractor.service.attribute.AttributeExtractorService.ExtractorData.ExtractorConfigAttributeData;
import com.infosys.ainauto.docwb.engine.extractor.service.attribute.AttributeExtractorService.ExtractorData.ExtractorConfigData;
import com.infosys.ainauto.docwb.web.data.AttachmentData;
import com.infosys.ainauto.docwb.web.data.AttributeData;
import com.infosys.ainauto.docwb.web.data.DocumentData;
import com.infosys.ainauto.docwb.web.type.EnumExtractType;
import com.infosys.ainauto.docwb.web.type.EnumSystemAttributeName;

public class AttributeDataHelper {

	public static AttributeData createMultipleAttibuteElement(int attrNameCde, String groupName,
			List<AttributeData> attributes) {
		AttributeData attributeData = new AttributeData();
		attributeData.setAttrNameCde(attrNameCde).setAttrNameTxt(groupName)
				.setConfidencePct(EngineExtractorConstants.CONFIDENCE_PCT_UNDEFINED)
				.setExtractType(groupName.equals(EngineExtractorConstants.GROUP_NAME_ROW) ? EnumExtractType.DIRECT_COPY
						: EnumExtractType.CUSTOM_LOGIC)
				.setAttributeDataList(attributes);
		return attributeData;
	}

	public static String getAttrValueByCde(List<AttributeData> attributes, int paramCde) {
		String attrValue = "";
		try {
			attrValue = attributes.stream().filter(attr -> attr.getAttrNameCde() == paramCde).findFirst().get()
					.getAttrValue();
		} catch (NoSuchElementException e) {
		}
		return attrValue;
	}

	public static List<AttributeData> filterByAttrNameCde(List<AttributeData> attrDataList, int attrNameCde) {
		return attrDataList.stream().filter(attribute -> attribute.getAttrNameCde() == attrNameCde)
				.collect(Collectors.toList());
	}

	public static List<AttributeData> filterByAttrNameTxt(List<AttributeData> attrDataList, String attrNameTxt) {
		return attrDataList.stream().filter(attribute -> attribute.getAttrNameTxt().equalsIgnoreCase(attrNameTxt))
				.collect(Collectors.toList());
	}

	public static List<ExtractorConfigAttributeData> updateAttrNameTxtToAttributes(
			List<ExtractorConfigAttributeData> attributes, Map<Integer, String> attributeDBNamesMapByCde) {
		attributes.forEach(attr -> {
			int cde = attr.getAttrNameCde();
			attr.setAttrNameTxt(attributeDBNamesMapByCde.get(cde));
		});

		return attributes;
	}

	public static boolean isMultiAttributes(ExtractorConfigAttributeData extractorConfigAttributeData) {
		return isMultiAttribute(extractorConfigAttributeData) || isMultiAttributeTable(extractorConfigAttributeData);
	}

	public static boolean isMultiAttribute(ExtractorConfigAttributeData extractorConfigAttributeData) {
		return extractorConfigAttributeData.getAttrNameCde() == EnumSystemAttributeName.MULTI_ATTRIBUTE.getCde();
	}

	public static boolean isMultiAttributeTable(ExtractorConfigAttributeData extractorConfigAttributeData) {
		return extractorConfigAttributeData.getAttrNameCde() == EnumSystemAttributeName.MULTI_ATTRIBUTE_TABLE.getCde();
	}

	public static String concatAttrValueByDelimiter(List<String> attrValues) {
		List<String> listWithoutDuplicates = new ArrayList<>(new HashSet<>(attrValues));
		return String.join(EngineExtractorConstants.ATTR_NAME_VALUE_DELIMITER, listWithoutDuplicates);
	}

	public static float getConfidenceAvg(List<Float> confidenceList) {
		return (float) confidenceList.stream().mapToDouble(a -> a).average().orElse(Double.NaN);
	}

	public static ExtractorConfigData getConfiguredExtractorApiDataByAttrNameCde(ExtractorData extractorConfiData,
			int attrNameCdeParam, DocumentData documentData) throws Exception {
		ExtractorConfigData extractorConfigData = null;
		if (extractorConfiData != null && extractorConfiData.getAttributeExtractorApiMapping() != null
				&& documentData != null) {
			List<ExtractorConfigData> extractorConfigDataList = extractorConfiData.getAttributeExtractorApiMapping();

			if (attrNameCdeParam == EnumSystemAttributeName.DOCUMENT_TYPE.getCde()) {
				if (documentData.getAttachmentDataList() != null) {
					for (AttachmentData attachmentData : documentData.getAttachmentDataList()) {
						AttributeData attributeData = attachmentData.getAttributes().stream()
								.filter(attribute -> attribute.getAttrNameCde() == attrNameCdeParam).findFirst()
								.orElse(null);
						if (attributeData != null) {
							extractorConfigData = extractorConfigDataList.stream()
									.filter(configData -> configData.getExecuteRuleTrueCondition()
											.getAttrNameCde() == attributeData.getAttrNameCde()
											&& configData.getExecuteRuleTrueCondition().getAttrNameValue()
													.equals(attributeData.getAttrValue()))
									.findFirst().orElse(null);
							break;
						}
					}
				}
			} else if (attrNameCdeParam == EnumSystemAttributeName.CATEGORY.getCde()) {
				AttributeData attributeData = documentData.getAttributes().stream()
						.filter(attribute -> attribute.getAttrNameCde() == attrNameCdeParam).findFirst().orElse(null);
				if (attributeData != null) {
					extractorConfigData = extractorConfigDataList.stream()
							.filter(configData -> configData.getExecuteRuleTrueCondition()
									.getAttrNameCde() == attributeData.getAttrNameCde()
									&& configData.getExecuteRuleTrueCondition().getAttrNameValue()
											.equals(attributeData.getAttrValue()))
							.findFirst().orElse(null);
				}
			}
		} else {
			throw new Exception("Configure AttributeExtractorApiMapping in configuration file");
		}
		return extractorConfigData;
	}

	@SuppressWarnings("unchecked")
	public static void addValuesFromMapToMap(Map<String, Object> attrValueMap, Map<String, Object> currentAttrMap) {
		attrValueMap.keySet().forEach(key -> {
			if (!currentAttrMap.containsKey(key)) {
				Object prevValue = attrValueMap.get(key);
				List<Object> attrValues = new ArrayList<>();
				if (prevValue instanceof List) {
					attrValues.addAll((Collection<? extends Object>) attrValueMap.get(key));
				} else {
					attrValues.add(attrValueMap.get(key));
				}
				attrValues.add(null);
				attrValueMap.put(key, attrValues);
			}
		});
		for (Map.Entry<String, Object> e : currentAttrMap.entrySet()) {
			if (!attrValueMap.containsKey(e.getKey())) {
				if (attrValueMap.isEmpty())
					attrValueMap.put(e.getKey(), e.getValue());
				else {
					List<Object> attrValues = new ArrayList<>();
					attrValues.add(null);
					attrValues.add(e.getValue());
					attrValueMap.put(e.getKey(), attrValues);
				}
			} else {
				Object prevValue = attrValueMap.get(e.getKey());
				List<Object> attrValues = new ArrayList<>();
				if (prevValue instanceof List) {
					attrValues.addAll((Collection<? extends Object>) attrValueMap.get(e.getKey()));
				} else {
					attrValues.add(attrValueMap.get(e.getKey()));
				}
				attrValues.add(currentAttrMap.get(e.getKey()));
				attrValueMap.put(e.getKey(), attrValues);
			}
		}
	}

	public static DocumentData convertJsonToDocumentData(String attributes) {
		DocumentData documentData = new DocumentData();
		JSONObject jsonResponse = new JSONObject(attributes);
		documentData.setDocId(jsonResponse.getInt("docId"));
		documentData.setAttributes(convertJsonToAttrData(jsonResponse));
		List<AttachmentData> attachmentDataList = new ArrayList<>();
		JSONArray attachmentsArray = jsonResponse.getJSONArray("attachments");
		if (attachmentsArray != null) {
			for (int i = 0; i < attachmentsArray.length(); i++) {
				JSONObject attachObj = attachmentsArray.getJSONObject(i);
				AttachmentData attachmentData = new AttachmentData();
				attachmentData.setAttachmentId(attachObj.getLong("attachmentId"));
				attachmentData.setAttributes(convertJsonToAttrData(attachObj));
				attachmentDataList.add(attachmentData);
			}
		}
		documentData.setAttachmentDataList(attachmentDataList);
		return documentData;
	}

	public static Map<String, List<String>> convertAttributesToCsv(DocumentData documentData, String fileName) {
		Map<String, List<String>> tableAttrContentMap = new HashMap<>();

		List<String> nonTabularAttributeContent = new ArrayList<>();
		nonTabularAttributeContent.add("\"Type\",\"Name\",\"Value\"\n");
		tableAttrContentMap.put(fileName, nonTabularAttributeContent);

		List<Integer> excludeAttrNameCdes = new ArrayList<>();
		excludeAttrNameCdes.add(EnumSystemAttributeName.CONTENT_ANNOTATION_ANNOTATOR.getCde());
		excludeAttrNameCdes.add(EnumSystemAttributeName.CONTENT_ANNOTATION.getCde());

		if (ListUtility.hasValue(documentData.getAttributes())) {
			nonTabularAttributesToCsv(documentData.getAttributes(), nonTabularAttributeContent, excludeAttrNameCdes,
					"Case");
			tabularAttributesToCsv(documentData.getAttributes(), fileName, tableAttrContentMap);
		}
		if (ListUtility.hasValue(documentData.getAttachmentDataList())) {
			for (AttachmentData attachmentData : documentData.getAttachmentDataList()) {
				if (ListUtility.hasValue(attachmentData.getAttributes())) {
					nonTabularAttributesToCsv(attachmentData.getAttributes(), nonTabularAttributeContent,
							excludeAttrNameCdes, "Attachment");
					tabularAttributesToCsv(attachmentData.getAttributes(), fileName, tableAttrContentMap);
				}
			}
		}
		tableAttrContentMap.put(fileName, nonTabularAttributeContent);
		return tableAttrContentMap;
	}

	private static List<AttributeData> convertJsonToAttrData(JSONObject jsonResponse) {
		List<AttributeData> attributeDataList = new ArrayList<>();
		JSONArray jsonArray = jsonResponse.getJSONArray("attributes");
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject attrObj = jsonArray.getJSONObject(i);
			AttributeData attrData = new AttributeData();
			attrData.setAttrNameCde(attrObj.getInt("attrNameCde"));
			attrData.setAttrNameTxt(attrObj.getString("attrNameTxt"));
			attrData.setAttrValue(attrObj.getString("attrValue"));
			attrData.setConfidencePct((float) attrObj.getDouble("confidencePct"));
			List<AttributeData> subAttributeDataList = new ArrayList<>();
			JSONArray subAttrArray = attrObj.getJSONArray("attributes");
			for (int j = 0; j < subAttrArray.length(); j++) {
				JSONObject subAttrObj = subAttrArray.getJSONObject(j);
				AttributeData subAttrData = new AttributeData();
				subAttrData.setAttrNameTxt(subAttrObj.getString("attrNameTxt"));
				subAttrData.setAttrValue(subAttrObj.getString("attrValue"));
				subAttrData.setConfidencePct((float) subAttrObj.getDouble("confidencePct"));

				List<AttributeData> tableAttributeDataList = new ArrayList<>();
				JSONArray tableAttrArray = subAttrObj.getJSONArray("attributes");
				for (int k = 0; k < tableAttrArray.length(); k++) {
					JSONObject tableAttrObj = tableAttrArray.getJSONObject(k);
					AttributeData tableAttrData = new AttributeData();
					tableAttrData.setAttrNameTxt(tableAttrObj.getString("attrNameTxt"));
					tableAttrData.setAttrValue(tableAttrObj.getString("attrValue"));
					tableAttrData.setConfidencePct((float) tableAttrObj.getDouble("confidencePct"));
					tableAttributeDataList.add(tableAttrData);
				}
				subAttrData.setAttributeDataList(tableAttributeDataList);
				subAttributeDataList.add(subAttrData);
			}
			attrData.setAttributeDataList(subAttributeDataList);
			attributeDataList.add(attrData);
		}
		return attributeDataList;
	}

	private static void nonTabularAttributesToCsv(List<AttributeData> attributes,
			List<String> nonTabularAttributeContent, List<Integer> excludeAttrNameCdes, String type) {
		for (AttributeData attributeData : attributes) {
			int attrNameCde = attributeData.getAttrNameCde();
			if (attrNameCde == EnumSystemAttributeName.MULTI_ATTRIBUTE.getCde()) {
				for (AttributeData subAttributeData : attributeData.getAttributeDataList()) {
					nonTabularAttributeContent.add("\"" + type + "\",\"" + subAttributeData.getAttrNameTxt() + "\",\""
							+ subAttributeData.getAttrValue() + "\"\n");
				}
			} else if (attrNameCde != EnumSystemAttributeName.MULTI_ATTRIBUTE_TABLE.getCde()
					&& !excludeAttrNameCdes.contains(attrNameCde)) {
				nonTabularAttributeContent.add("\"" + type + "\",\"" + attributeData.getAttrNameTxt() + "\",\""
						+ attributeData.getAttrValue() + "\"\n");
			}
		}
	}

	private static void tabularAttributesToCsv(List<AttributeData> attributes, String fileName,
			Map<String, List<String>> tableAttrContentMap) {
		String[] fileNameParts = fileName.split("_");
		for (AttributeData attributeData : attributes) {
			if (attributeData.getAttrNameCde() == EnumSystemAttributeName.MULTI_ATTRIBUTE_TABLE.getCde()) {
				List<String> contents = new ArrayList<>();
				String tableFileName = fileNameParts[0] + "_" + attributeData.getAttrNameTxt() + "_" + fileNameParts[1];
				List<Map<String, String>> headerRowMapList = new ArrayList<>();
				List<String> headerList = new ArrayList<>();
				for (int i = 0; i < attributeData.getAttributeDataList().size(); i++) {
					AttributeData attrData = attributeData.getAttributeDataList().get(i);
					if (i == 0) {
						// Table Header
						for (AttributeData headerData : attrData.getAttributeDataList()) {
							headerList.add(headerData.getAttrNameTxt());
							contents.add("\"" + headerData.getAttrNameTxt() + "\",");
						}
					}
					// Table Cells
					Map<String, String> rowMap = new HashMap<>();
					for (AttributeData cellData : attrData.getAttributeDataList()) {
						rowMap.put(cellData.getAttrNameTxt(), cellData.getAttrValue());
					}
					headerRowMapList.add(rowMap);
				}
				for (Map<String, String> rowMap : headerRowMapList) {
					contents.add("\n");
					for (String content : headerList) {
						String val = rowMap.get(content);
						if (val == null) {
							contents.add("\"\",");
						} else {
							contents.add("\"" + val + "\",");
						}
					}
				}
				tableAttrContentMap.put(tableFileName, contents);
			}
		}
	}

}
