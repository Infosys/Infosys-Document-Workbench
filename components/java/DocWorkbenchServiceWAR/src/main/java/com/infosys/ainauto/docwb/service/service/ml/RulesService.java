/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.service.ml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import javax.annotation.PostConstruct;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.HttpClientBase;
import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.service.common.AttributeHelper;
import com.infosys.ainauto.docwb.service.model.api.ActionResData;
import com.infosys.ainauto.docwb.service.model.api.DocumentResData;
import com.infosys.ainauto.docwb.service.model.api.ParamResData;
import com.infosys.ainauto.docwb.service.model.api.RecommendedActionResData;
import com.infosys.ainauto.docwb.service.model.api.attribute.AttributeNameResData;
import com.infosys.ainauto.docwb.service.model.api.attribute.GetAttributeNotificationResData;
import com.infosys.ainauto.docwb.service.model.db.ActionTempMappingDbData;
import com.infosys.ainauto.docwb.service.model.db.AttributeDbData;

@Component
public class RulesService extends HttpClientBase implements IRulesService {

	@Autowired
	private Environment environment;

	private static final String DOCID = "docId";
	private static final String DOCTYPECDE = "docTypeCde";
	private static final String ID = "id";

	private static final String ACTION_NAME_CDE = "actionNameCde";
	private static final String ACTION_NAME_TXT = "actionNameTxt";
	private static final String ACTION_RESULT = "actionResult";
	private static final String ACTION_REL_ID = "actionRelationId";

	private static final String ACTION_DATA_LIST = "actionDataList";
	private static final String ACTION_DATA_PARAM_LIST = "paramList";
	private static final String PARAM_NAME_CDE = "paramNameCde";
	private static final String PARAM_NAME_TXT = "paramNameTxt";
	private static final String PARAM_VALUE = "paramValue";

	private static final String ATTR_NAME_CDE = "attrNameCde";
	private static final String ATTR_NAME_TXT = "attrNameTxt";
	private static final String ATTR_VALUE = "attrValue";
	private static final String ATTRIBUTES = "attributes";
	private static final String ATTR_NAME_VALUES = "attrNameValues";
	private static final String ATTACHMENTS = "attachments";

	private static final String ATTACHMENT_ID = "attachmentId";

	private static final String EXTRACT_TYPE_CDE = "extractTypeCde";
	private static final String CONFIDENCE_PCT = "confidencePct";
	private static final String RECOMMENDED_PCT = "recommendedPct";
	private static final String NOTIFICATION = "notification";

	private static final String TENANT_ID = "tenantId";
	private static final String TABULAR_NAME_USING_REGEX = "orderColumnUsingAnyOfRegExp";
	private static final String TABULAR_DATA = "tabular";
	private static final String NONTABULAR_DATA = "nonTabular";

	private static final String REGEXP_PATTERN = "pattern";
	private static final String REGEXP_FLAG = "flag";

	private static final String RESPONSE = "response";
	private static final String TEMPLATE_NAME = "templateName";
	private static final String TEMPLATE_TXT = "templateText";
	private static final String TEMPLATE_TYPE = "templateType";
	private static final String TEMPLATE_HTML = "templateHtml";
	private static final String IS_RECOMMENDED_TEMPLATE = "isRecommendedTemplate";

	private static final String DOCWB_RULES_API_TEMPLATE_LIST_URL = "docwb.rules.api.template.list";
	private static final String DOCWB_RULES_API_ATTRIBUTE_ATTRIBUTE_MAPPING_URL = "docwb.rules.api.attribute.attribute.mapping";
	private static final String DOCWB_RULES_API_ATTRIBUTE_SORTKEY_URL = "docwb.rules.api.attribute.sortkey";
	private static final String DOCWB_RULES_API_TEMPLATE_FLATTENED_URL = "docwb.rules.api.template.flattened";
	private static final String DOCWB_RULES_API_ATTRIBUTE_NOTIFICATION_URL = "docwb.rules.api.attribute.notification";
	private static final String DOCWB_RULES_API_ACTION_RECOMMENDED_URL = "docwb.rules.api.action.recommended";

	private static final Integer[] GROUP_ATTR_NAME_CDES = { 44, 45, 46 };
	private static final List<Integer> GROUP_ATTR_NAME_CDE_LIST = Arrays.asList(GROUP_ATTR_NAME_CDES);

	private static final Logger LOGGER = LoggerFactory.getLogger(RulesService.class);

	@PostConstruct
	private void init() {
		// To be used if using proxy
		// this.setProxyWithAuth("10.68.248.34", 80,"your-windows-user-id",
		// "your-windows-password");
		LOGGER.debug("Initialized");
	}

	public List<ActionTempMappingDbData> getTemplates(String tenantId) {
		List<ActionTempMappingDbData> templateDataList = new ArrayList<>();
		String docwbRulesUrl = environment.getProperty(DOCWB_RULES_API_TEMPLATE_LIST_URL);
		HashMap<String, String> headerPropertiesMap = new HashMap<>();
		headerPropertiesMap.put(TENANT_ID, tenantId);
		JsonObject jsonResponse = (JsonObject) executeHttpCall(HttpCallType.GET, docwbRulesUrl, headerPropertiesMap);
		if (jsonResponse != null) {
			if (!jsonResponse.isNull(RESPONSE)) {
				JsonArray jsonArray = jsonResponse.getJsonArray(RESPONSE);
				for (int i = 0; i < jsonArray.size(); i++) {
					ActionTempMappingDbData tempData = new ActionTempMappingDbData();
					tempData.setTemplateName(jsonArray.getJsonObject(i).getString(TEMPLATE_NAME));
					tempData.setTemplateText(jsonArray.getJsonObject(i).getString(TEMPLATE_TXT));
					tempData.setTemplateType(jsonArray.getJsonObject(i).getString(TEMPLATE_TYPE));
					templateDataList.add(tempData);

				}
			}
		}
		return templateDataList;
	}

	public GetAttributeNotificationResData getAttributesNotification(String tenantId, long docId, int docTypeCde,
			List<AttributeDbData> attributeDbDataList, List<AttributeDbData> attachmentAttributeDbDataList) {

		String docwbRulesUrl = environment.getProperty(DOCWB_RULES_API_ATTRIBUTE_NOTIFICATION_URL);
		JsonObject jsonRequest = createJsonRequest(docId, docTypeCde, null, attributeDbDataList,
				attachmentAttributeDbDataList);

		HashMap<String, String> headerPropertiesMap = new HashMap<>();
		headerPropertiesMap.put(TENANT_ID, tenantId);

		JsonObject jsonResponse = (JsonObject) executeHttpCall(HttpCallType.POST, docwbRulesUrl, jsonRequest,
				headerPropertiesMap);
		GetAttributeNotificationResData responseData = null;
		if (jsonResponse != null) {
			if (!jsonResponse.isNull(RESPONSE)) {
				responseData = new GetAttributeNotificationResData();
				responseData.setAttributes(populateGanAttributes(attributeDbDataList));
				List<GetAttributeNotificationResData.AttachmentData> attachments = null;
				if (ListUtility.hasValue(attachmentAttributeDbDataList)) {
					attachments = new ArrayList<>();
					for (AttributeDbData attrDbData : attachmentAttributeDbDataList) {
						GetAttributeNotificationResData.AttachmentData attachmentData = new GetAttributeNotificationResData.AttachmentData();
						attachmentData.setAttachmentId(attrDbData.getAttachmentId());
						attachmentData.setAttributes(populateGanAttributes(attrDbData.getAttributes()));
						attachments.add(attachmentData);
					}
				}
				responseData.setAttachments(attachments);
				JsonObject responseObject = jsonResponse.getJsonObject(RESPONSE);
				if (!responseObject.isNull(ATTRIBUTES)) {
					JsonArray responseAttrArray = responseObject.getJsonArray(ATTRIBUTES);
					responseData
							.setAttributes(setAttributeNotification(responseData.getAttributes(), responseAttrArray));
				}

				if (!responseObject.isNull(ATTACHMENTS)) {
					JsonArray responseAttachmentAttrArray = responseObject.getJsonArray(ATTACHMENTS);
					for (int i = 0; i < responseAttachmentAttrArray.size(); i++) {
						JsonObject attachmentObject = responseAttachmentAttrArray.getJsonObject(i);
						long attachmentId = attachmentObject.getInt(ATTACHMENT_ID);
						for (GetAttributeNotificationResData.AttachmentData attachmentData : responseData
								.getAttachments()) {
							if (attachmentData.getAttachmentId() == attachmentId) {
								if (!attachmentObject.isNull(ATTRIBUTES)) {
									JsonArray responseAttrArray = attachmentObject.getJsonArray(ATTRIBUTES);
									attachmentData.setAttributes(setAttributeNotification(
											attachmentData.getAttributes(), responseAttrArray));
								}
								break;
							}
						}
					}
				}
			}
		}
		return responseData;
	}

	@Override
	public RecommendedActionResData getRecommendedAction(String tenantId, long docId, int docTypeCde,
			List<DocumentResData> actionParamAttrMappingDataList, List<AttributeDbData> attributeDbDataList,
			List<AttributeDbData> attachmentAttributeDbDataList) {
		RecommendedActionResData recommendedActionResData = null;
		String docwbRulesUrl = environment.getProperty(DOCWB_RULES_API_ACTION_RECOMMENDED_URL);

		JsonObject jsonRequest = createJsonRequest(docId, docTypeCde, actionParamAttrMappingDataList,
				attributeDbDataList, attachmentAttributeDbDataList);

		HashMap<String, String> headerPropertiesMap = new HashMap<>();
		headerPropertiesMap.put(TENANT_ID, tenantId);

		JsonObject jsonResponse = (JsonObject) executeHttpCall(HttpCallType.POST, docwbRulesUrl, jsonRequest,
				headerPropertiesMap);
		if (jsonResponse != null) {
			if (!jsonResponse.isNull(RESPONSE) && !jsonResponse.isEmpty()) {
				recommendedActionResData = new RecommendedActionResData();
				JsonArray jsonResArray = jsonResponse.getJsonArray(RESPONSE);
				for (int i = 0; i < jsonResArray.size(); i++) {
					recommendedActionResData.setActionNameCde(jsonResArray.getJsonObject(i).getInt(ACTION_NAME_CDE));
					recommendedActionResData.setConfidencePct(jsonResArray.getJsonObject(i).getInt(CONFIDENCE_PCT));
					recommendedActionResData.setRecommendedPct(jsonResArray.getJsonObject(i).getInt(RECOMMENDED_PCT));
				}
			}
		}

		return recommendedActionResData;
	}

	@Override
	public List<AttributeNameResData> getAttributeAttributeMapping(String tenantId,
			Map<Integer, String> attributeDbDataMap) {
		String docwbRulesUrl = environment.getProperty(DOCWB_RULES_API_ATTRIBUTE_ATTRIBUTE_MAPPING_URL);

		HashMap<String, String> headerPropertiesMap = new HashMap<>();
		headerPropertiesMap.put(TENANT_ID, tenantId);

		JsonObject jsonResponse = (JsonObject) executeHttpCall(HttpCallType.GET, docwbRulesUrl, headerPropertiesMap);
		List<AttributeNameResData> attributeNameResDataList = null;
		if (jsonResponse != null && !jsonResponse.isNull(RESPONSE)) {
			attributeNameResDataList = getAttrNameValueResponse(jsonResponse, attributeDbDataMap);
		}
		return attributeNameResDataList;
	}

	@Override
	public List<AttributeNameResData> getAttributeSortingKey(String tenantId, Map<Integer, String> attributeDbDataMap) {
		String docwbRulesUrl = environment.getProperty(DOCWB_RULES_API_ATTRIBUTE_SORTKEY_URL);

		HashMap<String, String> headerPropertiesMap = new HashMap<>();
		headerPropertiesMap.put(TENANT_ID, tenantId);

		JsonObject jsonResponse = (JsonObject) executeHttpCall(HttpCallType.GET, docwbRulesUrl, headerPropertiesMap);
		List<AttributeNameResData> attributeNameResDataList = null;
		if (jsonResponse != null && !jsonResponse.isNull(RESPONSE)) {
			attributeNameResDataList = getAttrNameValueResponse(jsonResponse, attributeDbDataMap);
		}
		return attributeNameResDataList;
	}

	private List<AttributeNameResData> getAttrNameValueResponse(JsonObject jsonResponse,
			Map<Integer, String> attributeDbDataMap) {
		JsonArray jsonArray = jsonResponse.getJsonArray(RESPONSE);
		List<AttributeNameResData> attributeNameResDataList = new ArrayList<AttributeNameResData>();
		for (int i = 0; i < jsonArray.size(); i++) {
			AttributeNameResData attributeNameResData = new AttributeNameResData();
			JsonObject jsonObject = jsonArray.getJsonObject(i);
			int attrNameCde = jsonObject.getInt(ATTR_NAME_CDE);
			if (attributeDbDataMap.containsKey(attrNameCde)) {
				attributeNameResData.setAttrNameCde(attrNameCde);
				attributeNameResData.setAttrNameTxt(attributeDbDataMap.get(attrNameCde));
				attributeNameResData.setAttrNameValues(getAttrNameValueDataList(jsonObject, attributeDbDataMap));
				attributeNameResDataList.add(attributeNameResData);
			}
		}

		return attributeNameResDataList;
	}

	private List<AttributeNameResData.AttributeNameValueData> getAttrNameValueDataList(JsonObject jsonObject,
			Map<Integer, String> attributeDbDataMap) {
		List<AttributeNameResData.AttributeNameValueData> attrNameValueDataList = new ArrayList<AttributeNameResData.AttributeNameValueData>();
		if (!jsonObject.isNull(ATTR_NAME_VALUES)) {
			JsonArray jsonInnerArray = jsonObject.getJsonArray(ATTR_NAME_VALUES);
			for (int j = 0; j < jsonInnerArray.size(); j++) {
				AttributeNameResData.AttributeNameValueData attrNameValueData = new AttributeNameResData.AttributeNameValueData();
				JsonObject jsonInnerObject = jsonInnerArray.getJsonObject(j);
				attrNameValueData.setAttrValue(jsonInnerObject.getString(ATTR_VALUE));
				attrNameValueData.setAttributes(getAttrDataList(jsonInnerObject, attributeDbDataMap));
				attrNameValueDataList.add(attrNameValueData);
			}
		}
		return attrNameValueDataList;
	}

	private List<AttributeNameResData.AttributeData> getAttrDataList(JsonObject jsonObject,
			Map<Integer, String> attributeDbDataMap) {
		List<AttributeNameResData.AttributeData> attributeDataList = new ArrayList<AttributeNameResData.AttributeData>();
		if (!jsonObject.isNull(ATTRIBUTES)) {
			JsonArray jsonArray = jsonObject.getJsonArray(ATTRIBUTES);
			for (int k = 0; k < jsonArray.size(); k++) {
				JsonObject jsonInnerObject = jsonArray.getJsonObject(k);
				if (jsonInnerObject.containsKey(NONTABULAR_DATA)) {
					List<AttributeNameResData.AttributeData> nonTabAttributeDataList = new ArrayList<AttributeNameResData.AttributeData>();
					JsonArray jsonNonTabularArray = jsonInnerObject.getJsonArray(NONTABULAR_DATA);
					for (int nt = 0; nt < jsonNonTabularArray.size(); nt++) {
						JsonObject jsonNonTabularObject = jsonNonTabularArray.getJsonObject(nt);
						addAttributeToList(jsonNonTabularObject, nonTabAttributeDataList, attributeDbDataMap);
					}
					AttributeNameResData.AttributeData attributeData = new AttributeNameResData.AttributeData();
					attributeData.setNonTabular(nonTabAttributeDataList);
					attributeDataList.add(attributeData);
				}
				if (jsonInnerObject.containsKey(TABULAR_DATA)) {
					List<AttributeNameResData.TabularAttributeData> tabularAttrDataList = new ArrayList<AttributeNameResData.TabularAttributeData>();
					JsonArray jsonTabularArray = jsonInnerObject.getJsonArray(TABULAR_DATA);
					for (int nt = 0; nt < jsonTabularArray.size(); nt++) {
						JsonObject jsonTabularObject = jsonTabularArray.getJsonObject(nt);
						if (jsonTabularObject.containsKey(TABULAR_NAME_USING_REGEX)) {
							List<AttributeNameResData.AttributeData> tabAttributeDataList = new ArrayList<AttributeNameResData.AttributeData>();
							JsonArray jsonTabularColArray = jsonTabularObject.getJsonArray(ATTRIBUTES);
							for (int col = 0; col < jsonTabularColArray.size(); col++) {
								addAttributeToList(jsonTabularColArray.getJsonObject(col), tabAttributeDataList,
										attributeDbDataMap);
							}
							JsonArray jsonRegExpArray = jsonTabularObject.getJsonArray(TABULAR_NAME_USING_REGEX);
							List<AttributeNameResData.RegExpData> reDataList = new ArrayList<AttributeNameResData.RegExpData>();
							for (int re = 0; re < jsonRegExpArray.size(); re++) {
								AttributeNameResData.RegExpData reData = new AttributeNameResData.RegExpData();
								JsonObject reObject = jsonRegExpArray.getJsonObject(re);
								reData.setPattern(reObject.getString(REGEXP_PATTERN));
								reData.setFlag(reObject.getString(REGEXP_FLAG));
								reDataList.add(reData);
							}
							AttributeNameResData.TabularAttributeData tabAttributeData = new AttributeNameResData.TabularAttributeData();
							tabAttributeData.setOrderColumnUsingAnyOfRegExp(reDataList);
							tabAttributeData.setAttributes(tabAttributeDataList);
							tabularAttrDataList.add(tabAttributeData);
						}

					}
					AttributeNameResData.AttributeData attributeData = new AttributeNameResData.AttributeData();
					attributeData.setTabular(tabularAttrDataList);
					attributeDataList.add(attributeData);
				}

				if (!jsonInnerObject.containsKey(NONTABULAR_DATA) && !jsonInnerObject.containsKey(TABULAR_DATA)) {
					addAttributeToList(jsonInnerObject, attributeDataList, attributeDbDataMap);
				}
			}
		}
		return attributeDataList;
	}

	private void addAttributeToList(JsonObject jsonInnerObject,
			List<AttributeNameResData.AttributeData> attributeDataList, Map<Integer, String> attributeDbDataMap) {
		int attrNameCde = jsonInnerObject.getInt(ATTR_NAME_CDE);
		String attrNameTxt = jsonInnerObject.containsKey(ATTR_NAME_TXT) ? jsonInnerObject.getString(ATTR_NAME_TXT) : "";
		AttributeNameResData.AttributeData attributeData = new AttributeNameResData.AttributeData();
		if (GROUP_ATTR_NAME_CDE_LIST.contains(attrNameCde)) {
			if (StringUtility.hasValue(attrNameTxt) && !isAttrNameExist(attributeDataList, attrNameTxt)) {
				attributeData.setAttrNameCde(attrNameCde);
				attributeData.setAttrNameTxt(attrNameTxt);
				attributeDataList.add(attributeData);
			}
		} else if (attributeDbDataMap.containsKey(attrNameCde)
				&& !isAttrNameExist(attributeDataList, attributeDbDataMap.get(attrNameCde))) {
			attributeData.setAttrNameCde(attrNameCde);
			attributeData.setAttrNameTxt(attributeDbDataMap.get(attrNameCde));
			attributeDataList.add(attributeData);
		}
	}

	private boolean isAttrNameExist(List<AttributeNameResData.AttributeData> attributeDataList, String attrNameTxt) {
		Predicate<AttributeNameResData.AttributeData> nameMatch = attribute -> attribute.getAttrNameTxt()
				.equals(attrNameTxt);
		return ListUtility.hasValue(attributeDataList) && attributeDataList.stream().anyMatch(nameMatch);
	}

	public List<ActionTempMappingDbData> getFlattenedTemplate(String tenantId, long docId, int docTypeCde,
			List<DocumentResData> actionParamAttrMappingDataList, List<AttributeDbData> attributeDbDataList,
			List<AttributeDbData> attachmentAttributeDbDataList) {

		List<ActionTempMappingDbData> actionTempMappingDataList = new ArrayList<ActionTempMappingDbData>();
		String docwbRulesUrl = environment.getProperty(DOCWB_RULES_API_TEMPLATE_FLATTENED_URL);

		JsonObject jsonRequest = createJsonRequest(docId, docTypeCde, actionParamAttrMappingDataList,
				attributeDbDataList, attachmentAttributeDbDataList);

		HashMap<String, String> headerPropertiesMap = new HashMap<>();
		headerPropertiesMap.put(TENANT_ID, tenantId);

		JsonObject jsonResponse = (JsonObject) executeHttpCall(HttpCallType.POST, docwbRulesUrl, jsonRequest,
				headerPropertiesMap);
		if (jsonResponse != null) {
			if (!jsonResponse.isNull(RESPONSE)) {
				JsonArray jsonResArray = jsonResponse.getJsonArray(RESPONSE);
				for (int i = 0; i < jsonResArray.size(); i++) {
					ActionTempMappingDbData actionTempMappingDbData = new ActionTempMappingDbData();
					actionTempMappingDbData.setIsRecommendedTemplate(
							jsonResArray.getJsonObject(i).getBoolean(IS_RECOMMENDED_TEMPLATE));
					actionTempMappingDbData.setTemplateHtml(jsonResArray.getJsonObject(i).getString(TEMPLATE_HTML));
					actionTempMappingDbData.setTemplateName(jsonResArray.getJsonObject(i).getString(TEMPLATE_NAME));
					actionTempMappingDbData.setTemplateText(jsonResArray.getJsonObject(i).getString(TEMPLATE_TXT));
					actionTempMappingDbData.setTemplateType(jsonResArray.getJsonObject(i).getString(TEMPLATE_TYPE));
					actionTempMappingDataList.add(actionTempMappingDbData);
				}
			}
		}
		return actionTempMappingDataList;
	}

	private JsonArrayBuilder addActionJsonArray(List<DocumentResData> actionParamAttrMappingDataList) {
		JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
		for (DocumentResData documentResData : actionParamAttrMappingDataList) {
			if (ListUtility.hasValue(documentResData.getActionDataList())) {
				for (ActionResData actionResData : documentResData.getActionDataList()) {
					String actionNameTxt = StringUtility.findAndReplace1(actionResData.getActionNameTxt(),
							StringUtility.NON_ASCI_REGEX, "");
					String actionResult = "";
					if (StringUtility.hasValue(actionResData.getActionResult())) {
						actionResult = StringUtility.findAndReplace1(actionResData.getActionResult(),
								StringUtility.NON_ASCI_REGEX, "");
					}
					JsonObjectBuilder jsonRequestBuilder = Json.createObjectBuilder();
					jsonRequestBuilder.add(ACTION_NAME_CDE, actionResData.getActionNameCde());
					jsonRequestBuilder.add(ACTION_NAME_TXT, actionNameTxt);
					jsonRequestBuilder.add(ACTION_RESULT, actionResult);
					jsonRequestBuilder.add(ACTION_REL_ID, actionResData.getDocActionRelId());
					if (ListUtility.hasValue(actionResData.getParamList())) {
						JsonArrayBuilder paramListArrayBuilder = Json.createArrayBuilder();
						for (ParamResData paramResData : actionResData.getParamList()) {
							JsonObjectBuilder paramRequestBuilder = Json.createObjectBuilder();
							paramRequestBuilder.add(PARAM_NAME_CDE, paramResData.getParamNameCde());
							paramRequestBuilder.add(PARAM_NAME_TXT,
									StringUtility.hasValue(paramResData.getParamNameTxt())
											? paramResData.getParamNameTxt()
											: "");
							paramRequestBuilder.add(PARAM_VALUE,
									StringUtility.hasValue(paramResData.getParamValue()) ? paramResData.getParamValue()
											: "");
							paramListArrayBuilder.add(paramRequestBuilder);
						}
						jsonRequestBuilder.add(ACTION_DATA_PARAM_LIST, paramListArrayBuilder);
					}
					jsonArrayBuilder.add(jsonRequestBuilder);
				}
			}
		}

		return jsonArrayBuilder;
	}

	private JsonObject createJsonRequest(long docId, int docTypeCde,
			List<DocumentResData> actionParamAttrMappingDataList, List<AttributeDbData> attributeDbDataList,
			List<AttributeDbData> attachmentAttributeDbDataList) {
		JsonObjectBuilder jsonRequestBuilder = Json.createObjectBuilder();
		jsonRequestBuilder.add(DOCID, docId);
		jsonRequestBuilder.add(DOCTYPECDE, docTypeCde);
		jsonRequestBuilder.add(ACTION_DATA_LIST,
				ListUtility.hasValue(actionParamAttrMappingDataList)
						? addActionJsonArray(actionParamAttrMappingDataList)
						: Json.createArrayBuilder());

		jsonRequestBuilder.add(ATTRIBUTES,
				ListUtility.hasValue(attributeDbDataList) ? addAttributesJsonArray(attributeDbDataList)
						: Json.createArrayBuilder());
		jsonRequestBuilder.add(ATTACHMENTS,
				ListUtility.hasValue(attachmentAttributeDbDataList)
						? addAttachmentAttributesJsonArray(attachmentAttributeDbDataList)
						: Json.createArrayBuilder());

		return jsonRequestBuilder.build();
	}

	private JsonArrayBuilder addAttributesJsonArray(List<AttributeDbData> attributeDbDataList) {
		long extractTypeCde;
		JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
		for (AttributeDbData attributeDbData : attributeDbDataList) {
			long attrNameCde = attributeDbData.getAttrNameCde();
			String attrNameTxt = attributeDbData.getAttrNameTxt();
			extractTypeCde = attributeDbData.getExtractTypeCde();
			float confidencePct = attributeDbData.getConfidencePct();

			JsonObjectBuilder jsonRequestBuilder2 = Json.createObjectBuilder();
			jsonRequestBuilder2.add(ID, attributeDbData.getId());
			jsonRequestBuilder2.add(ATTR_NAME_CDE, attrNameCde);
			jsonRequestBuilder2.add(ATTR_NAME_TXT, attrNameTxt);
			jsonRequestBuilder2.add(EXTRACT_TYPE_CDE, extractTypeCde);
			jsonRequestBuilder2.add(CONFIDENCE_PCT, confidencePct);
			if (ListUtility.hasValue(attributeDbData.getAttributes())) {
				jsonRequestBuilder2.add(ATTRIBUTES,
						AttributeHelper.convertMultiAttrDataListToJsonArrayBuilder(attributeDbData.getAttributes()));
			} else {
				String attrValue = attributeDbData.getAttrValue();
				jsonRequestBuilder2.add(ATTR_VALUE, StringUtility.hasValue(attrValue) ? attrValue : "");
			}
			jsonArrayBuilder.add(jsonRequestBuilder2);
		}

		return jsonArrayBuilder;
	}

	private JsonArrayBuilder addAttachmentAttributesJsonArray(List<AttributeDbData> attachmentAttributeDbDataList) {
		JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
		for (AttributeDbData attributeDbData : attachmentAttributeDbDataList) {
			JsonObjectBuilder jsonRequestBuilder = Json.createObjectBuilder();
			jsonRequestBuilder.add(ATTACHMENT_ID, attributeDbData.getAttachmentId());
			jsonRequestBuilder.add(ATTRIBUTES,
					ListUtility.hasValue(attributeDbData.getAttributes())
							? addAttributesJsonArray(attributeDbData.getAttributes())
							: Json.createArrayBuilder());
			jsonArrayBuilder.add(jsonRequestBuilder);
		}
		return jsonArrayBuilder;
	}

	private List<GetAttributeNotificationResData.AttributeData> setAttributeNotification(
			List<GetAttributeNotificationResData.AttributeData> attributeDbDataList, JsonArray responseAttrArray) {
		for (int i = 0; i < responseAttrArray.size(); i++) {
			JsonObject jsonObject = responseAttrArray.getJsonObject(i);
			if (jsonObject != null) {
				for (GetAttributeNotificationResData.AttributeData attributeData : attributeDbDataList) {
					if (attributeData.getId() == jsonObject.getInt(ID)) {
						if (ListUtility.hasValue(attributeData.getAttributes())) {
							attributeData.setAttributes(setAttributeNotification(attributeData.getAttributes(),
									jsonObject.getJsonArray(ATTRIBUTES)));
						} else if (!jsonObject.isNull(NOTIFICATION)) {
							attributeData.setNotification(jsonObject.getString(NOTIFICATION));
						}
						break;
					}
				}
			}
		}
		return attributeDbDataList;
	}

	private List<GetAttributeNotificationResData.AttributeData> populateGanAttributes(
			List<AttributeDbData> attributeDataList) {
		List<GetAttributeNotificationResData.AttributeData> ganAttributeDataList = new ArrayList<>();
		if (ListUtility.hasValue(attributeDataList)) {
			ganAttributeDataList = new ArrayList<>();
			for (AttributeDbData attributeData : attributeDataList) {
				GetAttributeNotificationResData.AttributeData ganAttributeData = new GetAttributeNotificationResData.AttributeData();
				BeanUtils.copyProperties(attributeData, ganAttributeData);
				// Call recursively if child attributes found
				if (ListUtility.hasValue(attributeData.getAttributes())) {
					ganAttributeData.setAttributes(populateGanAttributes(attributeData.getAttributes()));
				}
				ganAttributeDataList.add(ganAttributeData);
			}
		}
		return ganAttributeDataList;
	}

}