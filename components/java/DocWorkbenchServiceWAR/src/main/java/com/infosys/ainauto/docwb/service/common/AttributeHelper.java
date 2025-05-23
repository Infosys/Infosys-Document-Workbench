/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.common;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.service.common.type.EnumOperationType;
import com.infosys.ainauto.docwb.service.common.type.EnumSystemAttributeName;
import com.infosys.ainauto.docwb.service.dao.user.IUserDataAccess;
import com.infosys.ainauto.docwb.service.model.db.AttributeDbData;

public class AttributeHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(AttributeHelper.class);

	public static List<AttributeDbData> getAlphabeticallySortedAttributeList(List<AttributeDbData> attributeDbList) {
		Collections.sort(attributeDbList, new Comparator<AttributeDbData>() {
			@Override
			public int compare(AttributeDbData attrDbData1, AttributeDbData attrDbData2) {
				if (StringUtility.hasValue(attrDbData1.getAttrNameTxt())
						&& StringUtility.hasValue(attrDbData2.getAttrNameTxt()))
					return attrDbData1.getAttrNameTxt().compareToIgnoreCase(attrDbData2.getAttrNameTxt());
				else if (StringUtility.hasValue(attrDbData1.getAttrNameTxt()))
					return attrDbData1.getAttrNameTxt().compareToIgnoreCase("");
				else if (StringUtility.hasValue(attrDbData2.getAttrNameTxt()))
					return -attrDbData2.getAttrNameTxt().compareToIgnoreCase("");
				else
					return 0;
			}
		});
		return attributeDbList;
	}

	public static List<List<AttributeDbData>> getMultiAttrDataModified(String oldText, String newText,
			EnumOperationType operationType) {
		List<List<AttributeDbData>> multiAttrParameterDatas = new ArrayList<>();
		List<AttributeDbData> oldParameterDataList = convertJsonStringToMultiAttr(oldText).getAttributes();
		List<AttributeDbData> newParameterDataList = convertJsonStringToMultiAttr(newText).getAttributes();
		List<AttributeDbData> oldMultiAttrParameterDataList = new ArrayList<>();
		List<AttributeDbData> newMultiAttrParameterDataList = new ArrayList<>();
		if (ListUtility.hasValue(oldParameterDataList) && ListUtility.hasValue(newParameterDataList)) {
			if (operationType == EnumOperationType.UPDATE) {
				// Since Added/deleted/updated attributes occurs in single operation for
				// Multi-Attibute Table checking for the condition like
				// Newparam size should not be less than oldParam
				if (oldParameterDataList.size() <= newParameterDataList.size()) {
					for (int i = 0; i < oldParameterDataList.size(); i++) {
						if (oldParameterDataList.get(i).getId() == newParameterDataList.get(i).getId()) {
							if (!oldParameterDataList.get(i).getAttrValue()
									.equals(newParameterDataList.get(i).getAttrValue())) {
								oldMultiAttrParameterDataList.add(oldParameterDataList.get(i));
								newMultiAttrParameterDataList.add(newParameterDataList.get(i));
							}
						}

					}
				}
			} else if (operationType == EnumOperationType.DELETE) {
				if (oldParameterDataList.size() == newParameterDataList.size()) {
					for (int i = 0; i < oldParameterDataList.size(); i++) {
						if (oldParameterDataList.get(i).getId() == newParameterDataList.get(i).getId()
								&& oldParameterDataList.get(i).getEndDtm() == null
								&& StringUtility.hasValue(newParameterDataList.get(i).getEndDtm())) {
							oldMultiAttrParameterDataList.add(oldParameterDataList.get(i));
							newMultiAttrParameterDataList.add(newParameterDataList.get(i));
						}
					}
				}
			} else if (operationType == EnumOperationType.INSERT) {
				if (newParameterDataList.size() > oldParameterDataList.size()) {
					newMultiAttrParameterDataList = newParameterDataList.subList(oldParameterDataList.size(),
							newParameterDataList.size());
				}
			}
		}
		multiAttrParameterDatas.add(newMultiAttrParameterDataList);
		multiAttrParameterDatas.add(oldMultiAttrParameterDataList);
		return multiAttrParameterDatas;
	}

	private static List<AttributeDbData> convertJsontoInnerMutliAttr(JsonArray jsonArray,
			IUserDataAccess userDataAccess) {
		List<AttributeDbData> paramDataList = new ArrayList<AttributeDbData>();
		for (int i = 0, size = jsonArray.size(); i < size; i++) {
			AttributeDbData paramData = new AttributeDbData();
			JsonObject objectInArray = jsonArray.getJsonObject(i);
			paramData.setId(Long.parseLong(objectInArray.getString(WorkbenchConstants.JSON_MULTI_ATTR_PARAMETER_ID)));
			paramData.setAttrNameTxt(objectInArray.getString(WorkbenchConstants.JSON_MULTI_ATTR_PARAMETER_NAME));

			Object paramValue = objectInArray.get(WorkbenchConstants.JSON_MULTI_ATTR_PARAMETER_VAlUE);
			if (paramValue instanceof JsonArray) {
				paramData.setAttributes(convertJsontoInnerMutliAttr(
						objectInArray.getJsonArray(WorkbenchConstants.JSON_MULTI_ATTR_PARAMETER_VAlUE),
						userDataAccess));
			} else {
				paramData.setAttrValue(objectInArray.getString(WorkbenchConstants.JSON_MULTI_ATTR_PARAMETER_VAlUE));
			}

			if (objectInArray.containsKey(WorkbenchConstants.JSON_MULTI_ATTR_PARAMETER_CONFIDENCE_PCT)) {
				paramData.setConfidencePct(new BigDecimal(Float.parseFloat(
						String.valueOf(objectInArray.get(WorkbenchConstants.JSON_MULTI_ATTR_PARAMETER_CONFIDENCE_PCT))))
								.setScale(WorkbenchConstants.ATTR_CONFIDENCE_PCT_ROUND_OFF_DECIMAL_POINT,
										RoundingMode.HALF_EVEN)
								.floatValue());
			} else {
				paramData.setConfidencePct(WorkbenchConstants.CONFIDENCE_PCT_UNSET);
			}
			if (objectInArray.containsKey(WorkbenchConstants.JSON_MULTI_ATTR_PARAMETER_END_DTM)) {
				paramData.setEndDtm(objectInArray.getString(WorkbenchConstants.JSON_MULTI_ATTR_PARAMETER_END_DTM));
			}
			if (objectInArray.containsKey(WorkbenchConstants.JSON_MULTI_ATTR_PARAMETER_EXTRACT_TYPE_CDE)) {
				paramData.setExtractTypeCde(
						objectInArray.getInt(WorkbenchConstants.JSON_MULTI_ATTR_PARAMETER_EXTRACT_TYPE_CDE));
			}
			if (objectInArray.containsKey(WorkbenchConstants.JSON_MULTI_ATTR_PARAMETER_CREATE_BY)) {
				paramData.setCreateByUserLoginId(
						objectInArray.getString(WorkbenchConstants.JSON_MULTI_ATTR_PARAMETER_CREATE_BY));
				if (userDataAccess != null) {
					try {
						String createByKey = paramData.getCreateByUserLoginId() + "_" + SessionHelper.getTenantId();
						paramData.setCreateByUserFullName(userDataAccess.getUserData(createByKey).getUserFullName());
						paramData
								.setCreateByUserTypeCde((int) userDataAccess.getUserData(createByKey).getUserTypeCde());
					} catch (WorkbenchException e) {
					}
				}
			}
			if (objectInArray.containsKey(WorkbenchConstants.JSON_MULTI_ATTR_PARAMETER_CREATE_DATE)) {
				paramData.setCreateDtm(
						objectInArray.getString(WorkbenchConstants.JSON_MULTI_ATTR_PARAMETER_CREATE_DATE));
			}
			if (objectInArray.containsKey(WorkbenchConstants.JSON_MULTI_ATTR_PARAMETER_LAST_UPDATE_BY)) {
				paramData.setLastModByUserLoginId(
						objectInArray.getString(WorkbenchConstants.JSON_MULTI_ATTR_PARAMETER_LAST_UPDATE_BY));
				if (userDataAccess != null) {
					try {
						String lastModByKey = paramData.getLastModByUserLoginId() + "_" + SessionHelper.getTenantId();
						paramData.setLastModByUserFullName(userDataAccess.getUserData(lastModByKey).getUserFullName());
						paramData.setLastModByUserTypeCde(
								(int) userDataAccess.getUserData(lastModByKey).getUserTypeCde());
					} catch (WorkbenchException e) {
					}
				}
			}
			if (objectInArray.containsKey(WorkbenchConstants.JSON_MULTI_ATTR_PARAMETER_LAST_UPDATE_DATE)) {
				paramData.setLastModDtm(
						objectInArray.getString(WorkbenchConstants.JSON_MULTI_ATTR_PARAMETER_LAST_UPDATE_DATE));
			}

			paramDataList.add(paramData);
		}

		return paramDataList;
	}

	public static AttributeDbData convertJsonStringToMultiAttr(AttributeDbData attributeDbData,
			IUserDataAccess userDataAccess) {
		if (attributeDbData != null && StringUtility.hasValue(attributeDbData.getAttrValue())) {
			try {
				JsonReader reader = Json.createReader(new StringReader(attributeDbData.getAttrValue()));
				JsonObject jsonObject = reader.readObject();
				if (jsonObject.containsKey(WorkbenchConstants.JSON_MULTI_ATTR_GROUP_NAME)) {
					attributeDbData.setAttrNameTxt(jsonObject.getString(WorkbenchConstants.JSON_MULTI_ATTR_GROUP_NAME));
				}

				if (jsonObject.containsKey(WorkbenchConstants.JSON_MULTI_ATTR_PARAMETER_CONFIDENCE_PCT)) {
					attributeDbData.setConfidencePct(new BigDecimal(Float.parseFloat(String
							.valueOf(jsonObject.get(WorkbenchConstants.JSON_MULTI_ATTR_PARAMETER_CONFIDENCE_PCT))))
									.setScale(WorkbenchConstants.ATTR_CONFIDENCE_PCT_ROUND_OFF_DECIMAL_POINT,
											RoundingMode.HALF_EVEN)
									.floatValue());
				} else {
					attributeDbData.setConfidencePct(WorkbenchConstants.CONFIDENCE_PCT_UNSET);
				}

				JsonArray jsonArray = jsonObject.getJsonArray(WorkbenchConstants.JSON_MULTI_ATTR_ITEMS);
				attributeDbData.setAttributes(convertJsontoInnerMutliAttr(jsonArray, userDataAccess));
			} catch (Exception e) {
				LOGGER.error("Exception occured in convertJsonStringToMultiAttr()", e);
			}
		}
		return attributeDbData;
	}

	public static AttributeDbData convertJsonStringToMultiAttr(String textToProcess) {
		AttributeDbData attrData = null;
		if (StringUtility.hasValue(textToProcess)) {
			attrData = new AttributeDbData();
			attrData.setAttrValue(textToProcess);
			return convertJsonStringToMultiAttr(attrData, null);
		}
		return attrData;

	}

	private static JsonArrayBuilder convertInnerAttibutesToJson(List<AttributeDbData> attributeDbData,
			Map<String, Integer> attrNameOrderMapForTable) {
		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
		if (ListUtility.hasValue(attributeDbData)) {
			int i = 0;
			for (AttributeDbData data : attributeDbData) {
				i = i + 1;
				JsonObjectBuilder objectBuilder = Json.createObjectBuilder().add(
						WorkbenchConstants.JSON_MULTI_ATTR_PARAMETER_NAME,
						StringUtility.hasValue(data.getAttrNameTxt()) ? data.getAttrNameTxt() : "");
				if (attrNameOrderMapForTable != null) {
					if (!attrNameOrderMapForTable.isEmpty())
						objectBuilder.add(WorkbenchConstants.JSON_MULTI_ATTR_PARAMETER_ID,
								String.valueOf(attrNameOrderMapForTable.getOrDefault(data.getAttrNameTxt(), i)));
				} else {
					objectBuilder.add(WorkbenchConstants.JSON_MULTI_ATTR_PARAMETER_ID, String.valueOf(i));
				}
				if (StringUtility.hasValue(data.getAttrValue()))
					objectBuilder.add(WorkbenchConstants.JSON_MULTI_ATTR_PARAMETER_VAlUE, data.getAttrValue());
				else
					objectBuilder.add(WorkbenchConstants.JSON_MULTI_ATTR_PARAMETER_VAlUE,
							convertInnerAttibutesToJson(data.getAttributes(), attrNameOrderMapForTable));
				if (data.getConfidencePct() != WorkbenchConstants.CONFIDENCE_PCT_UNSET) {
					objectBuilder.add(WorkbenchConstants.JSON_MULTI_ATTR_PARAMETER_CONFIDENCE_PCT,
							data.getConfidencePct());
				}
				if (data.getEndDtm() != null) {
					objectBuilder.add(WorkbenchConstants.JSON_MULTI_ATTR_PARAMETER_END_DTM, data.getEndDtm());
				}
				objectBuilder.add(WorkbenchConstants.JSON_MULTI_ATTR_PARAMETER_EXTRACT_TYPE_CDE,
						data.getExtractTypeCde());
				if (data.getCreateByUserLoginId() != null) {
					objectBuilder.add(WorkbenchConstants.JSON_MULTI_ATTR_PARAMETER_CREATE_BY,
							data.getCreateByUserLoginId());
					objectBuilder.add(WorkbenchConstants.JSON_MULTI_ATTR_PARAMETER_CREATE_DATE, data.getCreateDtm());
				}
				if (data.getLastModByUserLoginId() != null) {
					objectBuilder.add(WorkbenchConstants.JSON_MULTI_ATTR_PARAMETER_LAST_UPDATE_BY,
							data.getLastModByUserLoginId());
					objectBuilder.add(WorkbenchConstants.JSON_MULTI_ATTR_PARAMETER_LAST_UPDATE_DATE,
							data.getLastModDtm());
				}
				arrayBuilder.add(objectBuilder);
			}
		}

		return arrayBuilder;
	}

	public static String convertMultiAttrToJsonString(AttributeDbData attributeDbData) {
		JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
		String text = "";
		if (attributeDbData == null) {
			return text;
		}
		if (StringUtility.hasValue(attributeDbData.getAttrNameTxt())) {
			jsonObjectBuilder.add(WorkbenchConstants.JSON_MULTI_ATTR_GROUP_NAME, attributeDbData.getAttrNameTxt());
		} else {
			jsonObjectBuilder.add(WorkbenchConstants.JSON_MULTI_ATTR_GROUP_NAME, "");
		}

		jsonObjectBuilder.add(WorkbenchConstants.JSON_MULTI_ATTR_PARAMETER_CONFIDENCE_PCT,
				attributeDbData.getConfidencePct());

		if (attributeDbData.getAttrNameCde() == EnumSystemAttributeName.MULTI_ATTRIBUTE_TABLE.getCde())
			jsonObjectBuilder.add(WorkbenchConstants.JSON_MULTI_ATTR_ITEMS,
					convertTableAttibutesToJson(attributeDbData.getAttributes()));
		else
			jsonObjectBuilder.add(WorkbenchConstants.JSON_MULTI_ATTR_ITEMS,
					convertInnerAttibutesToJson(attributeDbData.getAttributes(), null));
		Writer writer = new StringWriter();
		JsonObject obj = jsonObjectBuilder.build();
		Json.createWriter(writer).writeObject(obj);
		text = writer.toString();
		return text;
	}

	private static JsonArrayBuilder convertTableAttibutesToJson(List<AttributeDbData> attributes) {
		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
		if (ListUtility.hasValue(attributes)) {
			Map<String, Integer> colNameOrderMap = new LinkedHashMap<String, Integer>();
			attributes.stream().forEach(rowAttr -> {
				if (ListUtility.hasValue(rowAttr.getAttributes())) {
					rowAttr.getAttributes().stream().forEachOrdered(colAttr -> {
						if (!colNameOrderMap.containsKey(colAttr.getAttrNameTxt())) {
							colNameOrderMap.put(colAttr.getAttrNameTxt(),
									colNameOrderMap.isEmpty() ? 1 : colNameOrderMap.size() + 1);
						}
					});
				}
			});
			arrayBuilder = convertInnerAttibutesToJson(attributes, colNameOrderMap);
		}

		return arrayBuilder;
	}

	public static JsonArrayBuilder convertMultiAttrDataListToJsonArrayBuilder(List<AttributeDbData> attrDbDataList) {
		JsonArrayBuilder multiAttrJsonArrayBuilder = Json.createArrayBuilder();
		for (AttributeDbData attrDbData : attrDbDataList) {
			JsonObjectBuilder multiAttrDataJsonBuilder = Json.createObjectBuilder();
			String multiAttrNameTxt = StringUtility.findAndReplace1(attrDbData.getAttrNameTxt(),
					StringUtility.NON_ASCI_REGEX, "");
			String attrValue = StringUtility.findAndReplace1(attrDbData.getAttrValue(), StringUtility.NON_ASCI_REGEX,
					"");
			multiAttrDataJsonBuilder.add(WorkbenchConstants.ID, attrDbData.getId());
			multiAttrDataJsonBuilder.add(WorkbenchConstants.ATTR_NAME_TXT, multiAttrNameTxt);
			multiAttrDataJsonBuilder.add(WorkbenchConstants.ATTR_VALUE, attrValue);
			multiAttrDataJsonBuilder.add(WorkbenchConstants.CONFIDENCE_PCT, attrDbData.getConfidencePct());
			multiAttrJsonArrayBuilder.add(multiAttrDataJsonBuilder);
		}
		return multiAttrJsonArrayBuilder;
	}
}