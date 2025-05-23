/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.extractor.service.attribute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.json.JsonObject;
import javax.naming.ConfigurationException;

import org.everit.json.schema.ValidationException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infosys.ainauto.commonutils.FileUtility;
import com.infosys.ainauto.commonutils.HttpClientBase;
import com.infosys.ainauto.commonutils.HttpClientBase.Authentication.BasicAuthenticationConfig;
import com.infosys.ainauto.commonutils.JsonSchemaUtil;
import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.engine.core.common.DocwbEngineCoreConstants;
import com.infosys.ainauto.docwb.engine.extractor.common.AttributeDataHelper;
import com.infosys.ainauto.docwb.engine.extractor.common.EngineExtractorConstants;
import com.infosys.ainauto.docwb.engine.extractor.service.attribute.AttributeExtractorService.ExtractorData.ExtractorConfigAuthData;
import com.infosys.ainauto.docwb.engine.extractor.service.attribute.AttributeExtractorService.ExtractorData.ExtractorConfigBodyData;
import com.infosys.ainauto.docwb.engine.extractor.service.attribute.AttributeExtractorService.ExtractorData.ExtractorConfigBodyJsonData;
import com.infosys.ainauto.docwb.engine.extractor.service.attribute.AttributeExtractorService.ExtractorData.ExtractorConfigData;
import com.infosys.ainauto.docwb.engine.extractor.service.attribute.AttributeExtractorService.ExtractorData.ExtractorConfigJsonParamData;
import com.infosys.ainauto.docwb.engine.extractor.service.attribute.AttributeExtractorService.ExtractorData.ExtractorConfigParamData;
import com.infosys.ainauto.docwb.engine.extractor.service.attribute.AttributeExtractorService.ExtractorData.ExtractorConfigRequestData;
import com.infosys.ainauto.docwb.engine.extractor.service.attribute.AttributeExtractorService.RowValueResData.ColumnValueResData;
import com.infosys.ainauto.docwb.web.data.AttachmentData;
import com.infosys.ainauto.docwb.web.data.DocumentData;

@Component
public class AttributeExtractorService extends HttpClientBase implements IAttributeExtractorService {

	private static final String TRANSACTION_ID = "transactionId";
	private static final String PARAMETERS = "parameters";
	private static final String PAGE = "page";
	private static final String COLUMN_LABELS = "columnLabels";
	private static final String COLUMN_ORDER = "columnOrder";
	private static final String NAME = "name";
	private static final String ROW_REGEX = "^row[1-9]+\\d*$";
	private static final String LABEL = "label";
	private static final String ATTRIBUTES = "attributes";
	private static final String TABLE_REGEX = "^table[1-9]+\\d*$";
	private static final String RESPONSE_CDE = "responseCde";
	private static Logger logger = LoggerFactory.getLogger(AttributeExtractorService.class);
	private static final String PROP_NAME_ATTACHMENT_PATH = "docwb.engine.temp.path";
	private static final String PROP_NAME_SOCKET_TIMEOUT_VALUE = "service.attribute-extractor.socket-timeout-secs";
	private static final String APPLICATION_TYPE_JSON = "application/json";

	private ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private Environment environment;

	@Autowired
	protected AttributeExtractorService(Environment environment) {
		super(new HttpClientConfig()
				.setSocketTimeoutInSecs(Integer.valueOf(environment.getProperty(PROP_NAME_SOCKET_TIMEOUT_VALUE))));
	}

	@Override
	public String extractAttributes(ExtractorConfigData extractorConfigData, String extractorApiInterfaceSchema,
			DocumentData documentData, List<AttachmentData> attachmentDataList, String ruleType) throws Exception {
		logger.debug("Entering");
		String hostUrl = extractorConfigData.getApi();
		ExtractorConfigRequestData requestData = extractorConfigData.getRequest();
		JsonObject jsonResponse = null;
		jsonResponse = executeHttpMethod(requestData, documentData, attachmentDataList, hostUrl, ruleType);
		if (jsonResponse != null) {
			String jsonString = jsonResponse.toString();
			ValidationException ex = JsonSchemaUtil.validateSchema(extractorApiInterfaceSchema, jsonString);
			if (ex == null) {
				if (jsonResponse.getInt(RESPONSE_CDE) == 0) {
					return jsonString;
				} else {
					throw new Exception("Extractor API response is " + jsonResponse.getInt(RESPONSE_CDE)
							+ ", it is not equal to 0");
				}
			} else {
				logger.error("Violated Schema : " + ex.getViolatedSchema().toString());
				throw new Exception("Json Schema Validation failed due to following reasons : " + ex.getAllMessages());
			}
		} else {
			throw new Exception("Extractor API response is null");
		}
	}

	/**
	 * Method loops through response map and builds tabular & non-tabular data &
	 * textData
	 * 
	 * @param attributeServiceApiResData
	 * @param attributeServiceResponseData
	 */
	public void handleAttributeServiceApiResponse(AttributeServiceApiV2ResData attributeServiceApiResData,
			AttributeServiceResponseData attributeServiceResponseData) {
		Map<String, TableValueReData> tableMap = new TreeMap<String, TableValueReData>();
		attributeServiceApiResData.getResponse().forEach((key, value) -> {
			if (key.matches(TABLE_REGEX)) {
				buildTableMapFromResponse(tableMap, key, value);
			} else {
				if (key.matches(ATTRIBUTES)) {
					buildNonTabularDataFromResponse(attributeServiceResponseData, value);
				} else {
					try {
						JSONObject textContent = new JSONObject(mapper.writeValueAsString(value));
						Map<Integer, String> textContentMap = new TreeMap<Integer, String>();
						textContent.keySet().stream().forEach(page -> {
							int pageNo = Integer.valueOf(page.split(PAGE)[1]);
							textContentMap.put(pageNo, textContent.getString(page));
						});
						attributeServiceResponseData.setTextContent(textContentMap);
					} catch (JsonProcessingException e) {
						logger.error("Json Object conversion failed", e);
					}

				}
			}
		});
		attributeServiceResponseData.setTables(tableMap);
	}

	/**
	 * Method builds Non-tabular data from response
	 * 
	 * @param attributeServiceResponseData
	 * @param value
	 */
	@SuppressWarnings("unchecked")
	private void buildNonTabularDataFromResponse(AttributeServiceResponseData attributeServiceResponseData,
			Object value) {
		List<AttributeResData> attributes = new ArrayList<>();
		JSONArray attributeArray;
		try {
			attributeArray = new JSONArray(mapper.writeValueAsString(value));
			attributeArray.forEach(attrObj -> {
				AttributeResData attributeResData = new AttributeResData();
				JSONObject attribute = new JSONObject(attrObj.toString());
				Iterator<String> keys = attribute.keys();
				while (keys.hasNext()) {
					String currentObj = (String) keys.next();
					if (currentObj.matches(LABEL)) {
						attributeResData.setLabel(attribute.getString(currentObj));
					} else {
						attributeResData.setAttrName(currentObj);
						Map<String, Object> attrValueMap = new HashMap<String, Object>();
						if (attribute.optJSONArray(currentObj) != null) {
							JSONArray attrValArray = attribute.optJSONArray(currentObj);
							for (int i = 0; i < attrValArray.length(); i++) {
								if (attrValArray.optJSONObject(i) != null) {
									Map<String, Object> currentMap = attrValArray.optJSONObject(i).toMap();
									AttributeDataHelper.addValuesFromMapToMap(attrValueMap, currentMap);
								} else {
									if (attrValueMap.containsKey(EngineExtractorConstants.VALUE)) {
										Object prevValue = attrValueMap.get(EngineExtractorConstants.VALUE);
										List<Object> attrValues = new ArrayList<>();
										if (prevValue instanceof List) {
											attrValues.addAll((Collection<? extends Object>) attrValueMap
													.get(EngineExtractorConstants.VALUE));
										} else {
											attrValues.add(attrValueMap.get(EngineExtractorConstants.VALUE));
										}
										attrValues.add(attrValArray.opt(i));
										attrValueMap.put(EngineExtractorConstants.VALUE, attrValues);
									} else {
										attrValueMap.put(EngineExtractorConstants.VALUE, attrValArray.opt(i));
									}
								}
							}
						} else if (attribute.optJSONObject(currentObj) != null) {
							attrValueMap = attribute.optJSONObject(currentObj).toMap();
						} else {
							attrValueMap.put(EngineExtractorConstants.VALUE, attribute.get(currentObj));
						}
						if (!attrValueMap.isEmpty()) {
							attributeResData.setAttrValue(attrValueMap);
						}
					}
				}
				attributes.add(attributeResData);
			});
		} catch (JSONException | JsonProcessingException e) {
			logger.error("Json conversion failed", e);
		}
		attributeServiceResponseData.setAttributes(attributes);
	}

	/**
	 * Method adds table from mapped value to a table map
	 * 
	 * @param tableMap
	 * @param key
	 * @param value
	 */
	private void buildTableMapFromResponse(Map<String, TableValueReData> tableMap, String key, Object value) {
		TableValueReData tableValueReData = new TableValueReData();
		try {
			JSONObject tableObj = new JSONObject(mapper.writeValueAsString(value));
			tableValueReData.setName(tableObj.optString(NAME));
			tableValueReData.setColumnOrder(tableObj.optJSONArray(COLUMN_ORDER).toList().stream()
					.map(stringObj -> Objects.toString(stringObj, null)).collect(Collectors.toList()));
			List<String> columnLabels = tableObj.optJSONArray(COLUMN_LABELS) != null
					? tableObj.optJSONArray(COLUMN_LABELS).toList().stream()
							.map(stringObj -> Objects.toString(stringObj, null)).collect(Collectors.toList())
					: new ArrayList<>();
			tableValueReData.setColumnLabels(columnLabels);
			Iterator<String> keys = tableObj.keys();
			List<RowValueResData> rowList = new ArrayList<>();
			while (keys.hasNext()) {
				String currentKey = (String) keys.next();
				if (currentKey.matches(ROW_REGEX)) {
					RowValueResData rowValueResData = new RowValueResData();
					rowValueResData.setRowName(currentKey);
					JSONObject currentRowValue = tableObj.getJSONObject(currentKey);
					Iterator<String> columns = currentRowValue.keys();
					List<ColumnValueResData> columnList = new ArrayList<>();
					while (columns.hasNext()) {
						String currentCol = (String) columns.next();
						ColumnValueResData columnValueResData = new ColumnValueResData();
						columnValueResData.setColName(currentCol);
						Map<String, Object> columnValueMap = new HashMap<String, Object>();
						if (currentRowValue.optJSONObject(currentCol) != null) {
							columnValueMap = currentRowValue.getJSONObject(currentCol).toMap();
						} else {
							columnValueMap.put(EngineExtractorConstants.VALUE, currentRowValue.get(currentCol));
						}
						columnValueResData.setColValue(columnValueMap);
						columnList.add(columnValueResData);
					}
					rowValueResData.setColumns(columnList);
					rowList.add(rowValueResData);
				}

			}
			tableValueReData.setRowObjects(rowList);
			tableMap.put(key, tableValueReData);
		} catch (JsonProcessingException e) {
			logger.error("Json conversion failed", e);
		}
	}

	/**
	 * Set Get Dynamic values to Application JSON param name using the AttrNameCde
	 * given in Config file
	 */
	private JSONObject getJsonParamValuesUpdatedData(ExtractorConfigBodyJsonData extractorConfigBodyJsonData,
			DocumentData documentData, String ruleType) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(TRANSACTION_ID, generateCorrelationId());
		JSONArray jsonArray = new JSONArray();
		for (ExtractorConfigJsonParamData param : extractorConfigBodyJsonData.getParameters()) {
			JSONObject paramObj = new JSONObject();
			int paramCde = param.getAttrNameCde();
			String attrValues = param.getValue();
			if (paramCde > 0) {
				if (ruleType.equals(DocwbEngineCoreConstants.RULE_TYPE_EMAIL)) {
					attrValues = AttributeDataHelper.getAttrValueByCde(documentData.getAttributes(), paramCde);
				} else if (ruleType.equals(DocwbEngineCoreConstants.RULE_TYPE_DOCUMENT)) {
					// TODO 02/23/2021 - Check how to get particular attachment while
					// processing for more than one attachment.
					for (AttachmentData attachments : documentData.getAttachmentDataList()) {
						attrValues = AttributeDataHelper.getAttrValueByCde(attachments.getAttributes(), paramCde);
						if (StringUtility.hasTrimmedValue(attrValues)) {
							break;
						}
					}
				}
			}
			paramObj.put(NAME, param.getName());
			paramObj.put(EngineExtractorConstants.VALUE, attrValues);
			for (Entry<String, Object> additionalParam : param.getAdditionalParams().entrySet()) {
				paramObj.put(additionalParam.getKey(), additionalParam.getValue());
			}
			jsonArray.put(paramObj);
		}
		jsonObject.put(PARAMETERS, jsonArray);
		return jsonObject;
	}

	/**
	 * Get the Content ID from Config file based on file type.
	 * 
	 * @throws Exception
	 */
	private String getAttachmentContentId(ExtractorConfigBodyData requestBody, String type)
			throws ConfigurationException {
		String contentId = "";
		try {
			contentId = requestBody.getAttachmentContentData().stream()
					.filter(content -> content.getContentType().equals(type)).findFirst().get().getContentID();
		} catch (NoSuchElementException e) {
			logger.error("Configuration Exception",
					"Configure /body/attachmentContentData property for " + StringUtility.sanitizeReqData(type));
		}
		return contentId;
	}

	/**
	 * Method identifies whether to send in request only json/ json + pdf / only pdf
	 * to external service
	 * 
	 * @throws Exception
	 */
	private JsonObject executeHttpMethod(ExtractorConfigRequestData requestData, DocumentData documentData,
			List<AttachmentData> attachmentDataList, String hostUrl, String ruleType) throws Exception {
		JsonObject jsonResponse = null;
		String username = "";
		String drowssap = "";
		String bodyJsonStr = "";
		ExtractorConfigBodyData requestBody = requestData.getBody();
		if (requestBody.isIncludeParamData()) {
			if (requestBody.getJsonData() != null && ListUtility.hasValue(requestBody.getJsonData().getParameters())) {
				bodyJsonStr = getJsonParamValuesUpdatedData(requestBody.getJsonData(), documentData, ruleType)
						.toString();
			} else {
				throw new ConfigurationException(
						"Configure jsonData or /jsonData/parameters properties when \"includeParamData\": true");
			}
		}

		if (!requestBody.isIncludeFileAttachment()) {
			if (requestBody.isIncludeParamData()) {
				return jsonResponse = (JsonObject) executeHttpCall(HttpCallType.POST, hostUrl, bodyJsonStr);
			}
		} else {
			if (!ListUtility.hasValue(requestBody.getAttachmentContentData())) {
				throw new ConfigurationException(
						"Configure /body/attachmentContentData properties when \"includeFileAttachment\": true");
			}
		}

		ExtractorConfigAuthData authData = requestData.getAuth();
		if (authData.isAuthRequired()) {
			if (ListUtility.hasValue(authData.getParameters())) {
				username = authData.getParameters().stream()
						.filter(parameter -> parameter.getName()
								.equals(EngineExtractorConstants.EXTRACTOR_API_USERNAME))
						.collect(Collectors.toList()).get(0).getValue();
				drowssap = authData.getParameters().stream()
						.filter(parameter -> parameter.getName().equals(EngineExtractorConstants.EXTRACTOR_API_PASSWRD))
						.collect(Collectors.toList()).get(0).getValue();
			} else {
				throw new ConfigurationException("Configure /auth/parameters properties when \"authRequired\": true");
			}
		}

		List<HttpFileRequestData> httpFileDataList = new ArrayList<>();
		for (AttachmentData attachmentData : attachmentDataList) {
			String contentType = FileUtility.getContentType(attachmentData.getPhysicalPath(),
					FileUtility.getFileExtension(attachmentData.getPhysicalName()));
			if (StringUtility.hasValue(contentType)) {
				String contentId = getAttachmentContentId(requestBody, contentType);
				if (StringUtility.hasValue(contentId))
					httpFileDataList.add(new HttpFileRequestData(attachmentData.getPhysicalName(),
							attachmentData.getPhysicalPath(), contentId, contentType));
			}
		}

		String tempFileName = UUID.randomUUID().toString() + EngineExtractorConstants.EXTRACTION_JSON_FILE_EXTENSION;
		String tempFilePath = FileUtility.getConcatenatedName(environment.getProperty(PROP_NAME_ATTACHMENT_PATH),
				tempFileName);
		if (requestBody.isIncludeParamData() && StringUtility.hasTrimmedValue(bodyJsonStr)) {

			boolean isSuccess = FileUtility.saveFile(tempFilePath, bodyJsonStr);
			if (!isSuccess) {
				logger.error("Error occured in creating JSON File");
				return jsonResponse;
			}
			httpFileDataList.add(new HttpFileRequestData(tempFileName, tempFilePath,
					getAttachmentContentId(requestBody, APPLICATION_TYPE_JSON), APPLICATION_TYPE_JSON));
		}

		BasicAuthenticationConfig basicAuthConfig = new BasicAuthenticationConfig(username, drowssap, true);

		jsonResponse = executePostAttachmentWithAuthCall(hostUrl, httpFileDataList, basicAuthConfig).getResponse();

		if (FileUtility.doesFileExist(tempFilePath))
			FileUtility.deleteFile(tempFilePath);
		return jsonResponse;
	}

	public static class AttributeServiceApiV2ResData {
		private LinkedHashMap<String, Object> response;
		private String transactionId;
		private int responseCde;
		private String responseMsg;
		private float responseTimeInSecs;
		private String timestamp;

		public LinkedHashMap<String, Object> getResponse() {
			return response;
		}

		public void setResponse(LinkedHashMap<String, Object> response) {
			this.response = response;
		}

		public String getTransactionId() {
			return transactionId;
		}

		public void setTransactionId(String transactionId) {
			this.transactionId = transactionId;
		}

		public int getResponseCde() {
			return responseCde;
		}

		public void setResponseCde(int responseCde) {
			this.responseCde = responseCde;
		}

		public String getResponseMsg() {
			return responseMsg;
		}

		public void setResponseMsg(String responseMsg) {
			this.responseMsg = responseMsg;
		}

		public float getResponseTimeInSecs() {
			return responseTimeInSecs;
		}

		public void setResponseTimeInSecs(float responseTimeInSecs) {
			this.responseTimeInSecs = responseTimeInSecs;
		}

		public String getTimestamp() {
			return timestamp;
		}

		public void setTimestamp(String timestamp) {
			this.timestamp = timestamp;
		}
	}

	public static class AttributeServiceResponseData {
		private Map<String, TableValueReData> tables;
		private List<AttributeResData> attributes;
		private Map<Integer, String> textContent;

		public Map<String, TableValueReData> getTables() {
			return tables;
		}

		public void setTables(Map<String, TableValueReData> tableMap) {
			this.tables = tableMap;
		}

		public List<AttributeResData> getAttributes() {
			return attributes;
		}

		public void setAttributes(List<AttributeResData> attributes) {
			this.attributes = attributes;
		}

		public Map<Integer, String> getTextContent() {
			return textContent;
		}

		public void setTextContent(Map<Integer, String> textContent) {
			this.textContent = textContent;
		}
	}

	public static class TableValueReData {
		private String name;
		private List<String> columnOrder;
		private List<String> columnLabels;
		private List<RowValueResData> rowObjects;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public List<String> getColumnOrder() {
			return columnOrder;
		}

		public void setColumnOrder(List<String> columnOrder) {
			this.columnOrder = columnOrder;
		}

		public List<String> getColumnLabels() {
			return columnLabels;
		}

		public void setColumnLabels(List<String> columnLabels) {
			this.columnLabels = columnLabels;
		}

		public List<RowValueResData> getRowObjects() {
			return rowObjects;
		}

		public void setRowObjects(List<RowValueResData> rowObjects) {
			this.rowObjects = rowObjects;
		}

	}

	public static class RowValueResData {
		private String rowName;
		private List<ColumnValueResData> columns;

		public static class ColumnValueResData {
			private String colName;
			private Map<String, Object> colValue;

			public String getColName() {
				return colName;
			}

			public void setColName(String colName) {
				this.colName = colName;
			}

			public Map<String, Object> getColValue() {
				return colValue;
			}

			public void setColValue(Map<String, Object> colValue) {
				this.colValue = colValue;
			}
		}

		public String getRowName() {
			return rowName;
		}

		public void setRowName(String rowName) {
			this.rowName = rowName;
		}

		public List<ColumnValueResData> getColumns() {
			return columns;
		}

		public void setColumns(List<ColumnValueResData> columns) {
			this.columns = columns;
		}

	}

	public static class AttributeResData {
		private String label;
		private String attrName;
		private Map<String, Object> attrValue;

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public String getAttrName() {
			return attrName;
		}

		public void setAttrName(String attrName) {
			this.attrName = attrName;
		}

		public Map<String, Object> getAttrValue() {
			return attrValue;
		}

		public void setAttrValue(Map<String, Object> attrValue) {
			this.attrValue = attrValue;
		}
	}

	public static class AttributeServiceApiResData {
		private AttributeServiceResData response;
		private String transactionId;
		private int responseCde;
		private String responseMsg;
		private float responseTimeInSecs;
		private String timestamp;

		public static class AttributeServiceResData {
			private List<TableValueData> tables;

			public List<TableValueData> getTables() {
				return tables;
			}

			public void setTables(List<TableValueData> tables) {
				this.tables = tables;
			}
		}

		public static class CloumnValueData {
			private int colId;
			private String colName;
			private List<RowValueData> rows;

			public int getColId() {
				return colId;
			}

			public void setColId(int colId) {
				this.colId = colId;
			}

			public String getColName() {
				return colName;
			}

			public void setColName(String colName) {
				this.colName = colName;
			}

			public List<RowValueData> getRows() {
				return rows;
			}

			public void setRows(List<RowValueData> rows) {
				this.rows = rows;
			}
		}

		public static class RowValueData {
			private int rowId;
			private CellValueData cell;

			public int getRowId() {
				return rowId;
			}

			public void setRowId(int rowId) {
				this.rowId = rowId;
			}

			public CellValueData getCell() {
				return cell;
			}

			public void setCell(CellValueData cell) {
				this.cell = cell;
			}
		}

		public static class CellValueData {
			private String value;
			private float confidencePct;

			public String getValue() {
				return value;
			}

			public void setValue(String value) {
				this.value = value;
			}

			public float getConfidencePct() {
				return confidencePct;
			}

			public void setConfidencePct(float confidencePct) {
				this.confidencePct = confidencePct;
			}

		}

		public static class TableValueData {
			private int tableId;
			private String tableName;
			private List<CloumnValueData> columns;

			public int getTableId() {
				return tableId;
			}

			public void setTableId(int tableId) {
				this.tableId = tableId;
			}

			public List<CloumnValueData> getColumns() {
				return columns;
			}

			public void setColumns(List<CloumnValueData> columns) {
				this.columns = columns;
			}

			public String getTableName() {
				return tableName;
			}

			public void setTableName(String tableName) {
				this.tableName = tableName;
			}
		}

		public String getTransactionId() {
			return transactionId;
		}

		public void setTransactionId(String transactionId) {
			this.transactionId = transactionId;
		}

		public int getResponseCde() {
			return responseCde;
		}

		public void setResponseCde(int responseCde) {
			this.responseCde = responseCde;
		}

		public String getResponseMsg() {
			return responseMsg;
		}

		public void setResponseMsg(String responseMsg) {
			this.responseMsg = responseMsg;
		}

		public String getTimestamp() {
			return timestamp;
		}

		public void setTimestamp(String timestamp) {
			this.timestamp = timestamp;
		}

		public float getResponseTimeInSecs() {
			return responseTimeInSecs;
		}

		public void setResponseTimeInSecs(float responseTimeInSecs) {
			this.responseTimeInSecs = responseTimeInSecs;
		}

		public AttributeServiceResData getResponse() {
			return response;
		}

		public void setResponse(AttributeServiceResData response) {
			this.response = response;
		}

	}

	public static class ExtractorData {
		private List<ExtractorConfigData> attributeExtractorApiMapping;

		public List<ExtractorConfigData> getAttributeExtractorApiMapping() {
			return attributeExtractorApiMapping;
		}

		public void setAttributeExtractorApiMapping(List<ExtractorConfigData> attributeExtractorApiMapping) {
			this.attributeExtractorApiMapping = attributeExtractorApiMapping;
		}

		public static class ExtractorConfigData {
			private ExtractorConfigExecuteRuleData executeRuleTrueCondition;
			private String api;
			@JsonInclude(JsonInclude.Include.NON_DEFAULT)
			private String modelId;
			@JsonInclude(JsonInclude.Include.NON_DEFAULT)
			private String ocpApimSubscriptionKey;
			private ExtractorConfigRequestData request;
			private ExtractorConfigResponseData response;

			public String getApi() {
				return api;
			}

			public void setApi(String api) {
				this.api = api;
			}

			public ExtractorConfigRequestData getRequest() {
				return request;
			}

			public void setRequest(ExtractorConfigRequestData request) {
				this.request = request;
			}

			public ExtractorConfigResponseData getResponse() {
				return response;
			}

			public void setResponse(ExtractorConfigResponseData response) {
				this.response = response;
			}

			public ExtractorConfigExecuteRuleData getExecuteRuleTrueCondition() {
				return executeRuleTrueCondition;
			}

			public void setExecuteRuleTrueCondition(ExtractorConfigExecuteRuleData executeRuleTrueCondition) {
				this.executeRuleTrueCondition = executeRuleTrueCondition;
			}

			public String getModelId() {
				return modelId;
			}

			public void setModelId(String modelId) {
				this.modelId = modelId;
			}

			public String getOcpApimSubscriptionKey() {
				return ocpApimSubscriptionKey;
			}

			public void setOcpApimSubscriptionKey(String ocpApimSubscriptionKey) {
				this.ocpApimSubscriptionKey = ocpApimSubscriptionKey;
			}
		}

		public static class ExtractorConfigExecuteRuleData {
			private int attrNameCde;
			private String attrNameValue;

			public int getAttrNameCde() {
				return attrNameCde;
			}

			public void setAttrNameCde(int attrNameCde) {
				this.attrNameCde = attrNameCde;
			}

			public String getAttrNameValue() {
				return attrNameValue;
			}

			public void setAttrNameValue(String attrNameValue) {
				this.attrNameValue = attrNameValue;
			}

		}

		public static class ExtractorConfigRequestData {
			private ExtractorConfigAuthData auth;
			private ExtractorConfigBodyData body;

			public ExtractorConfigAuthData getAuth() {
				return auth;
			}

			public void setAuth(ExtractorConfigAuthData auth) {
				this.auth = auth;
			}

			public ExtractorConfigBodyData getBody() {
				return body;
			}

			public void setBody(ExtractorConfigBodyData body) {
				this.body = body;
			}
		}

		public static class ExtractorConfigBodyJsonData {
			private String transactionId;
			private List<ExtractorConfigJsonParamData> parameters;

			public String getTransactionId() {
				return transactionId;
			}

			public void setTransactionId(String transactionId) {
				this.transactionId = transactionId;
			}

			public List<ExtractorConfigJsonParamData> getParameters() {
				return parameters;
			}

			public void setParameters(List<ExtractorConfigJsonParamData> parameters) {
				this.parameters = parameters;
			}

		}

		public static class ExtractorConfigBodyContentData {
			private String contentType;
			private String contentID;

			public String getContentType() {
				return contentType;
			}

			public void setContentType(String contentType) {
				this.contentType = contentType;
			}

			public String getContentID() {
				return contentID;
			}

			public void setContentID(String contentID) {
				this.contentID = contentID;
			}
		}

		public static class ExtractorConfigBodyData {
			private boolean includeFileAttachment;
			private boolean includeParamData;
			private List<ExtractorConfigBodyContentData> attachmentContentData;
			private ExtractorConfigBodyJsonData jsonData;

			public boolean isIncludeFileAttachment() {
				return includeFileAttachment;
			}

			public void setIncludeFileAttachment(boolean includeFileAttachment) {
				this.includeFileAttachment = includeFileAttachment;
			}

			public boolean isIncludeParamData() {
				return includeParamData;
			}

			public void setIncludeParamData(boolean includeParamData) {
				this.includeParamData = includeParamData;
			}

			public ExtractorConfigBodyJsonData getJsonData() {
				return jsonData;
			}

			public void setJsonData(ExtractorConfigBodyJsonData jsonData) {
				this.jsonData = jsonData;
			}

			public List<ExtractorConfigBodyContentData> getAttachmentContentData() {
				return attachmentContentData;
			}

			public void setAttachmentContentData(List<ExtractorConfigBodyContentData> attachmentContentData) {
				this.attachmentContentData = attachmentContentData;
			}
		}

		public static class ExtractorConfigJsonParamData {
			private int attrNameCde;
			private String name;
			private String value;
			private Map<String, Object> additionalParams = new HashMap<>();

			public int getAttrNameCde() {
				return attrNameCde;
			}

			public void setAttrNameCde(int attrNameCde) {
				this.attrNameCde = attrNameCde;
			}

			public String getName() {
				return name;
			}

			public void setName(String name) {
				this.name = name;
			}

			public String getValue() {
				return value;
			}

			public void setValue(String value) {
				this.value = value;
			}

			@JsonAnyGetter
			public Map<String, Object> getAdditionalParams() {
				return additionalParams;
			}

			@JsonAnySetter
			public void setAdditionalParams(String key, Object value) {
				this.additionalParams.put(key, value);
			}
		}

		public static class ExtractorConfigAuthData {
			private boolean authRequired;
			private String type;
			private List<ExtractorConfigParamData> parameters;

			public boolean isAuthRequired() {
				return authRequired;
			}

			public void setAuthRequired(boolean authRequired) {
				this.authRequired = authRequired;
			}

			public String getType() {
				return type;
			}

			public void setType(String type) {
				this.type = type;
			}

			public List<ExtractorConfigParamData> getParameters() {
				return parameters;
			}

			public void setParameters(List<ExtractorConfigParamData> parameters) {
				this.parameters = parameters;
			}
		}

		public static class ExtractorConfigParamData {
			private String name;
			private String value;

			public String getName() {
				return name;
			}

			public void setName(String name) {
				this.name = name;
			}

			public String getValue() {
				return value;
			}

			public void setValue(String value) {
				this.value = value;
			}
		}

		public static class ExtractorConfigResponseData {
			private ExtractorConfigMappingData mapping;

			public ExtractorConfigMappingData getMapping() {
				return mapping;
			}

			public void setMapping(ExtractorConfigMappingData mapping) {
				this.mapping = mapping;
			}
		}

		public static class ExtractorConfigMappingData {
			private List<ExtractorConfigAttributeData> attributes;

			public List<ExtractorConfigAttributeData> getAttributes() {
				return attributes;
			}

			public void setAttributes(List<ExtractorConfigAttributeData> attributes) {
				this.attributes = attributes;
			}
		}

		public static class ExtractorConfigAttributeData {
			private int attrNameCde;
			private String tableName = "";
			private String colName;
			private String attrNameTxt = "";
			private String groupName;
			private String resAttrName = "";
			private String resColName;
			private List<ExtractorConfigAttributeData> attributes;

			public int getAttrNameCde() {
				return attrNameCde;
			}

			public void setAttrNameCde(int attrNameCde) {
				this.attrNameCde = attrNameCde;
			}

			public String getTableName() {
				return tableName;
			}

			public void setTableName(String tableName) {
				this.tableName = tableName;
			}

			public String getColName() {
				return colName;
			}

			public void setColName(String colName) {
				this.colName = colName;
			}

			public String getAttrNameTxt() {
				return attrNameTxt;
			}

			public void setAttrNameTxt(String attrNameTxt) {
				this.attrNameTxt = attrNameTxt;
			}

			public List<ExtractorConfigAttributeData> getAttributes() {
				return attributes;
			}

			public void setAttributes(List<ExtractorConfigAttributeData> attributes) {
				this.attributes = attributes;
			}

			public String getGroupName() {
				return groupName;
			}

			public void setGroupName(String groupName) {
				this.groupName = groupName;
			}

			public String getResAttrName() {
				return resAttrName;
			}

			public void setResAttrName(String resAttrName) {
				this.resAttrName = resAttrName;
			}

			public String getResColName() {
				return resColName;
			}

			public void setResColName(String resColName) {
				this.resColName = resColName;
			}

		}

	}

	public static class ExtractorJsonData {
		private String transactionId;
		private List<ExtractorConfigParamData> parameters;

		public List<ExtractorConfigParamData> getParameters() {
			return parameters;
		}

		public void setParameters(List<ExtractorConfigParamData> parameters) {
			this.parameters = parameters;
		}

		public String getTransactionId() {
			return transactionId;
		}

		public void setTransactionId(String transactionId) {
			this.transactionId = transactionId;
		}

	}

	public static class AttributeMetaData {
		private boolean isMultiAttributes;
		private boolean isMultiAttribute;
		private boolean isMultiAttributeTable;
		private String attrNameTxt;
		private int attrNameCde;
		private String tableName;
		private String groupName;
		private String resAttrName;

		public boolean isMultiAttributes() {
			return isMultiAttributes;
		}

		public void setMultiAttributes(boolean isMultiAttributes) {
			this.isMultiAttributes = isMultiAttributes;
		}

		public boolean isMultiAttributeTable() {
			return isMultiAttributeTable;
		}

		public void setMultiAttributeTable(boolean isMultiAttributeTable) {
			this.isMultiAttributeTable = isMultiAttributeTable;
		}

		public String getAttrNameTxt() {
			return attrNameTxt;
		}

		public void setAttrNameTxt(String attrNameTxt) {
			this.attrNameTxt = attrNameTxt;
		}

		public String getTableName() {
			return tableName;
		}

		public void setTableName(String tableName) {
			this.tableName = tableName;
		}

		public int getAttrNameCde() {
			return attrNameCde;
		}

		public void setAttrNameCde(int attrNameCde) {
			this.attrNameCde = attrNameCde;
		}

		public boolean isMultiAttribute() {
			return isMultiAttribute;
		}

		public void setMultiAttribute(boolean isMultiAttribute) {
			this.isMultiAttribute = isMultiAttribute;
		}

		public String getGroupName() {
			return groupName;
		}

		public void setGroupName(String groupName) {
			this.groupName = groupName;
		}

		public String getResAttrName() {
			return resAttrName;
		}

		public void setResAttrName(String resAttrName) {
			this.resAttrName = resAttrName;
		}
	}
}
