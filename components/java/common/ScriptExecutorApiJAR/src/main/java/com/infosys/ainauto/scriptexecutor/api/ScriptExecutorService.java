/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.scriptexecutor.api;

import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.infosys.ainauto.commonutils.HttpClientBase;
import com.infosys.ainauto.scriptexecutor.common.PropertyManager;
import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.scriptexecutor.data.ScriptResponseItemData;
import com.infosys.ainauto.scriptexecutor.data.ParameterData;
import com.infosys.ainauto.scriptexecutor.data.ScriptIdentifierData;
import com.infosys.ainauto.scriptexecutor.data.ScriptResponseData;

public class ScriptExecutorService extends HttpClientBase implements IScriptExecutorService {

	private static Logger logger = LoggerFactory.getLogger(ScriptExecutorService.class);

	private static final String PROP_NAME_API_INITIATE_EXECUTION_URL = "script.executor.api.initiate.execution.url";
	private static final String PROP_NAME_API_GET_TRANSACTION_STATUS_URL = "script.executor.api.transaction.status.url";

	private static final String CATEGORY_ID = "CategoryId";
	private static final String COMPANY_ID = "CompanyId";
	private static final String EXECUTION_MODE = "ExecutionMode";
	private static final String REFERENCE_KEY = "ReferenceKey";
	private static final String REMOTE_SERVER_NAMES = "RemoteServerNames";
	private static final String PARAMETERS = "Parameters";
	private static final String PARAMETER_NAME = "ParameterName";
	private static final String PARAMETER_VALUE = "ParameterValue";
	private static final String RESPONSE_NOTIFICATION_CALLBACK_URL = "ResponseNotificationCallbackURL";
	private static final String SCRIPT_IDENTIFIER = "ScriptIdentifier";
	private static final String SCRIPT_ID = "ScriptId";
	private static final String SCRIPT_NAME = "ScriptName";
	private static final String TRANSACTION_ID = "TransactionId";

	// Protected constructor to avoid instantiation by outside world
	protected ScriptExecutorService() {
		super();
	}

	public ScriptResponseData initiateExecution(ScriptIdentifierData scriptIdentifierData) {

		String initiateExecutionUrl = PropertyManager.getInstance().getProperty(PROP_NAME_API_INITIATE_EXECUTION_URL);

		JsonObjectBuilder jsonRequestBuilder = Json.createObjectBuilder();
		JsonObjectBuilder jsonRequestBuilderFinal = Json.createObjectBuilder();

		jsonRequestBuilder.add(CATEGORY_ID, scriptIdentifierData.getCategoryId());
		jsonRequestBuilder.add(COMPANY_ID, scriptIdentifierData.getCompanyId());
		jsonRequestBuilder.add(EXECUTION_MODE, scriptIdentifierData.getExecutionMode());

		JsonArrayBuilder jsonArrayBuilder1 = Json.createArrayBuilder();

		if (scriptIdentifierData.getParameterDataList() != null) {
			for (int i = 0; i < scriptIdentifierData.getParameterDataList().size(); i++) {
				JsonObjectBuilder jsonRequestBuilder1 = Json.createObjectBuilder();
				jsonRequestBuilder1.add(PARAMETER_NAME,
						scriptIdentifierData.getParameterDataList().get(i).getParameterName());
				jsonRequestBuilder1.add(PARAMETER_VALUE,
						scriptIdentifierData.getParameterDataList().get(i).getParameterValue());
				jsonArrayBuilder1.add(jsonRequestBuilder1);
			}
		}

		jsonRequestBuilder.add(PARAMETERS, jsonArrayBuilder1);

		if (StringUtility.hasValue(scriptIdentifierData.getReferenceKey())) {
			jsonRequestBuilder.add(REFERENCE_KEY, scriptIdentifierData.getReferenceKey());
		}
		if (StringUtility.hasValue(scriptIdentifierData.getRemoteServerNames())) {
			jsonRequestBuilder.add(REMOTE_SERVER_NAMES, scriptIdentifierData.getRemoteServerNames());
		}
		if (StringUtility.hasValue(scriptIdentifierData.getResponseNotificationCallbackURL())) {
			jsonRequestBuilder.add(RESPONSE_NOTIFICATION_CALLBACK_URL,
					scriptIdentifierData.getResponseNotificationCallbackURL());
		}
		jsonRequestBuilder.add(SCRIPT_ID, scriptIdentifierData.getScriptId());
		if (StringUtility.hasValue(scriptIdentifierData.getScriptName())) {
			jsonRequestBuilder.add(SCRIPT_NAME, scriptIdentifierData.getScriptName());
		}
		jsonRequestBuilderFinal.add(SCRIPT_IDENTIFIER, jsonRequestBuilder);

		JsonObject jsonRequest = jsonRequestBuilderFinal.build();
		JsonObject jsonResponse = (JsonObject) executeHttpCall(HttpCallType.POST, initiateExecutionUrl, jsonRequest);

		ScriptResponseData scriptResponseData = extractScriptResponseDataFromJson(jsonResponse);

		return scriptResponseData;
	}

	public ScriptResponseData getTransactionStatusAndResult(String transactionId) {

		String getTransactionStatusUrl = PropertyManager.getInstance()
				.getProperty(PROP_NAME_API_GET_TRANSACTION_STATUS_URL);

		JsonObjectBuilder jsonRequestBuilder = Json.createObjectBuilder();
		jsonRequestBuilder.add(TRANSACTION_ID, transactionId);

		JsonObject jsonRequest = jsonRequestBuilder.build();
		JsonObject jsonResponse = (JsonObject) executeHttpCall(HttpCallType.POST, getTransactionStatusUrl, jsonRequest);

		ScriptResponseData scriptResponseData = extractScriptResponseDataFromJson(jsonResponse);

		return scriptResponseData;

	}

	private ScriptResponseData extractScriptResponseDataFromJson(JsonObject jsonResponse) {
		ScriptResponseData scriptResponseData = null;
		if (jsonResponse != null && !jsonResponse.isNull("ScriptResponse")) {
			JsonArray response = jsonResponse.getJsonArray("ScriptResponse");
			if (!response.isEmpty()) {
				List<ScriptResponseItemData> scriptResponseItemList = new ArrayList<>();
				ScriptResponseItemData scriptResponseItem = null;
				for (int i = 0; i < response.size(); i++) {
					JsonObject jsonObject = response.getJsonObject(i);
					scriptResponseItem = new ScriptResponseItemData();
					scriptResponseItem.setComputerName(jsonObject.getString("ComputerName", null));
					scriptResponseItem.setErrorMessage(jsonObject.getString("ErrorMessage", null));
					scriptResponseItem.setInputCommand(jsonObject.getString("InputCommand", null));
					scriptResponseItem.setIsSuccess(jsonObject.getBoolean("IsSuccess", false));
					scriptResponseItem.setLogData(jsonObject.getString("LogData", null));
					
					if (!jsonObject.isNull("OutParameters")) {
						List<ParameterData> parameterDataList = new ArrayList<ParameterData>();
						JsonArray outParameters = jsonObject.getJsonArray("OutParameters");
						if (outParameters.size() > 0) {
							for (int j = 0; j < outParameters.size(); j++) {
								ParameterData parameterData = new ParameterData();
								JsonObject jsonObject1 = outParameters.getJsonObject(j);
								parameterData.setParameterName(jsonObject1.getString("ParameterName", null));
								parameterData.setParameterValue(jsonObject1.getString("ParameterValue", null));
								parameterDataList.add(parameterData);
							}
						}
						scriptResponseItem.setOutParameters(parameterDataList);
					}
					
					scriptResponseItem.setSourceTransactionId(jsonObject.getString("SourceTransactionId", null));
					scriptResponseItem.setStatus(jsonObject.getString("Status", null));
					scriptResponseItem.setSuccessMessage(jsonObject.getString("SuccessMessage", null));
					scriptResponseItem.setTransactionId(jsonObject.getString("TransactionId", null));
					scriptResponseItemList.add(scriptResponseItem);
					logger.info("Request received with cid={} and satus={}", scriptResponseItem.getTransactionId(),
							scriptResponseItem.getStatus());
				}
				scriptResponseData = new ScriptResponseData();
				scriptResponseData.setScriptResponseItemDataList(scriptResponseItemList);
			}

		}
		return scriptResponseData;
	}
}
