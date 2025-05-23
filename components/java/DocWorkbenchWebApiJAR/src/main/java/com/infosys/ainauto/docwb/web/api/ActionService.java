/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.web.api;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.infosys.ainauto.commonutils.HttpClientBase;
import com.infosys.ainauto.commonutils.SimpleThreadFactory;
import com.infosys.ainauto.docwb.web.common.DocwbWebConstants;
import com.infosys.ainauto.docwb.web.common.IServiceThreadHandler;
import com.infosys.ainauto.docwb.web.common.PropertyManager;
import com.infosys.ainauto.docwb.web.data.ActionData;
import com.infosys.ainauto.docwb.web.data.ActionParamAttrMappingData;
import com.infosys.ainauto.docwb.web.data.DocActionData;
import com.infosys.ainauto.docwb.web.data.ParamAttrData;
import com.infosys.ainauto.docwb.web.data.RecommendedActionData;
import com.infosys.ainauto.docwb.web.type.EnumEventOperator;
import com.infosys.ainauto.docwb.web.type.EnumTaskStatus;
import com.infosys.ainauto.docwb.web.type.EnumTaskType;

public class ActionService extends HttpClientBase implements IActionService, IServiceThreadHandler {

	private static Logger logger = LoggerFactory.getLogger(ActionService.class);

	private static final String PROP_NAME_API_ACTION_URL = "docwb.api.action.url";
	private static final String PROP_NAME_API_ACTION_PARAM_ATTR_RECOMMENDATION_URL = "docwb.api.action.param.attr.recommendation.url";
	private static final String PROP_NAME_API_GET_RECOMMENDED_ACTION = "docwb.api.recommendation.url";
	private ThreadPoolExecutor executor;

	// Protected constructor to avoid instantiation by outside world
	protected ActionService(HttpClientBase.Authentication.BearerAuthenticationConfig bearerAuthConfig, int defaultMaxPerRoute) {
		super(new HttpClientConfig().setDefaultMaxPerRoute(defaultMaxPerRoute), bearerAuthConfig);

	}

	public List<DocActionData> getActionListForDoc(int actionNameCde, EnumTaskStatus enumTaskStatus, int queueNameCde,
			EnumEventOperator taskStatusOperator, long docId) {
		List<DocActionData> docActionDataList = new ArrayList<DocActionData>();
		String url = "";
		try {
			URIBuilder uriBuilder = new URIBuilder(PropertyManager.getInstance().getProperty(PROP_NAME_API_ACTION_URL));
			if (actionNameCde > 0)
				uriBuilder.addParameter(DocwbWebConstants.ACTION_NAME_CDE, String.valueOf(actionNameCde));
			if (enumTaskStatus.getValue() > 0) {
				uriBuilder.addParameter(DocwbWebConstants.TASK_STATUS_CDE, String.valueOf(enumTaskStatus.getValue()));
				uriBuilder.addParameter(DocwbWebConstants.TASK_STATUS_OPERATOR, taskStatusOperator.getValue());
			}
			if (queueNameCde > 0)
				uriBuilder.addParameter(DocwbWebConstants.QUEUE_NAME_CDE, String.valueOf(queueNameCde));
			if (docId > 0) {
				uriBuilder.addParameter(DocwbWebConstants.DOC_ID, String.valueOf(docId));
			}
			url = uriBuilder.build().toURL().toString();
		} catch (Exception e) {
			logger.error("Error occurred in getActionList", e);
		}
		JsonObject jsonResponse = (JsonObject) executeHttpCall(HttpCallType.GET, url);
		JsonArray jsonResponseArray = jsonResponse.getJsonArray(DocwbWebConstants.RESPONSE);

		for (int i = 0; i < jsonResponseArray.size(); i++) {
			List<ActionParamAttrMappingData> actionDataList = new ArrayList<ActionParamAttrMappingData>();

			JsonObject object = jsonResponseArray.getJsonObject(i);
			DocActionData docActionData = new DocActionData();
			docActionData.setDocId(object.getInt(DocwbWebConstants.DOC_ID));
			JsonArray jSONArray = object.getJsonArray(DocwbWebConstants.ACTION_DATA_LIST);
			for (int k = 0; k < jSONArray.size(); k++) {

				JsonObject object1 = jSONArray.getJsonObject(k);
				ActionParamAttrMappingData actionData = new ActionParamAttrMappingData();
				actionData.setDocId(object.getInt(DocwbWebConstants.DOC_ID));
				actionData.setDocActionRelId(object1.getInt(DocwbWebConstants.DOC_ACTION_REL_ID));
				actionData.setActionNameCde(object1.getInt(DocwbWebConstants.ACTION_NAME_CDE));
				actionData.setActionNameTxt(object1.getString(DocwbWebConstants.ACTION_NAME_TXT));
				actionData.setTaskStatus(EnumTaskStatus.get(object1.getInt(DocwbWebConstants.TASK_STATUS_CDE)));
				actionData.setTaskStatusTxt(object1.getString(DocwbWebConstants.TASK_STATUS_TXT));
				actionData.setTaskType(EnumTaskType.get(object1.getInt(DocwbWebConstants.TASK_TYPE_CDE)));
				actionData.setCreateDtm(object1.getString(DocwbWebConstants.CREATE_DTM));
				JsonArray paramList = object1.getJsonArray(DocwbWebConstants.PARAM_LIST);
				List<ParamAttrData> paramAttrDataList = new ArrayList<ParamAttrData>();
				for (int j = 0; j < paramList.size(); j++) {
					JsonObject object2 = paramList.getJsonObject(j);
					if (!object2.isNull(DocwbWebConstants.PARAM_NAME_TXT)) {
						ParamAttrData paramAttrData = new ParamAttrData();
						paramAttrData.setParamNameCde(object2.getInt(DocwbWebConstants.PARAM_NAME_CDE));
						paramAttrData.setParamNameTxt(object2.getString(DocwbWebConstants.PARAM_NAME_TXT));
						paramAttrData.setParamValue(object2.getString(DocwbWebConstants.PARAM_VALUE, ""));
						paramAttrDataList.add(paramAttrData);
					}
				}
				actionData.setParamAttrDataList(paramAttrDataList);
				actionDataList.add(actionData);
			}
			docActionData.setActionParamAttrMappingDataList(actionDataList);
			docActionDataList.add(docActionData);
		}
		return docActionDataList;
	}

	public List<DocActionData> getActionList(int actionNameCde, EnumTaskStatus enumTaskStatus, int queueNameCde,
			EnumEventOperator taskStatusOperator) {
		List<DocActionData> docActionDataList = new ArrayList<DocActionData>();
		String url = "";
		try {
			URIBuilder uriBuilder = new URIBuilder(PropertyManager.getInstance().getProperty(PROP_NAME_API_ACTION_URL));
			if (actionNameCde > 0)
				uriBuilder.addParameter(DocwbWebConstants.ACTION_NAME_CDE, String.valueOf(actionNameCde));
			if (enumTaskStatus.getValue() > 0) {
				uriBuilder.addParameter(DocwbWebConstants.TASK_STATUS_CDE, String.valueOf(enumTaskStatus.getValue()));
				uriBuilder.addParameter(DocwbWebConstants.TASK_STATUS_OPERATOR, taskStatusOperator.getValue());
			}
			if (queueNameCde > 0)
				uriBuilder.addParameter(DocwbWebConstants.QUEUE_NAME_CDE, String.valueOf(queueNameCde));
			url = uriBuilder.build().toURL().toString();
		} catch (Exception e) {
			logger.error("Error occurred in getActionList", e);
		}
		JsonObject jsonResponse = (JsonObject) executeHttpCall(HttpCallType.GET, url);
		JsonArray jsonResponseArray = jsonResponse.getJsonArray(DocwbWebConstants.RESPONSE);

		for (int i = 0; i < jsonResponseArray.size(); i++) {
			List<ActionParamAttrMappingData> actionDataList = new ArrayList<ActionParamAttrMappingData>();

			JsonObject object = jsonResponseArray.getJsonObject(i);
			DocActionData docActionData = new DocActionData();
			docActionData.setDocId(object.getInt(DocwbWebConstants.DOC_ID));
			JsonArray jSONArray = object.getJsonArray(DocwbWebConstants.ACTION_DATA_LIST);
			for (int k = 0; k < jSONArray.size(); k++) {

				JsonObject object1 = jSONArray.getJsonObject(k);
				ActionParamAttrMappingData actionData = new ActionParamAttrMappingData();
				actionData.setDocId(object.getInt(DocwbWebConstants.DOC_ID));
				actionData.setDocActionRelId(object1.getInt(DocwbWebConstants.DOC_ACTION_REL_ID));
				actionData.setActionNameCde(object1.getInt(DocwbWebConstants.ACTION_NAME_CDE));
				actionData.setActionNameTxt(object1.getString(DocwbWebConstants.ACTION_NAME_TXT));
				actionData.setTaskStatus(EnumTaskStatus.get(object1.getInt(DocwbWebConstants.TASK_STATUS_CDE)));
				actionData.setTaskStatusTxt(object1.getString(DocwbWebConstants.TASK_STATUS_TXT));
				actionData.setTaskType(EnumTaskType.get(object1.getInt(DocwbWebConstants.TASK_TYPE_CDE)));
				actionData.setCreateDtm(object1.getString(DocwbWebConstants.CREATE_DTM));

				JsonArray paramList = object1.getJsonArray(DocwbWebConstants.PARAM_LIST);
				List<ParamAttrData> paramAttrDataList = new ArrayList<ParamAttrData>();
				for (int j = 0; j < paramList.size(); j++) {
					JsonObject object2 = paramList.getJsonObject(j);
					if (!object2.isNull(DocwbWebConstants.PARAM_NAME_TXT)) {
						ParamAttrData paramAttrData = new ParamAttrData();
						paramAttrData.setParamNameCde(object2.getInt(DocwbWebConstants.PARAM_NAME_CDE));
						paramAttrData.setParamNameTxt(object2.getString(DocwbWebConstants.PARAM_NAME_TXT));
						paramAttrData.setParamValue(object2.getString(DocwbWebConstants.PARAM_VALUE, ""));
						paramAttrDataList.add(paramAttrData);
					}
				}
				actionData.setParamAttrDataList(paramAttrDataList);
				actionDataList.add(actionData);
			}
			docActionData.setActionParamAttrMappingDataList(actionDataList);
			docActionDataList.add(docActionData);
		}
		return docActionDataList;
	}

	/**
	 * Method gets a list of action list for the given queuenameCdes list used for
	 * creating multiple threads to get the list from api.
	 * 
	 */
	public List<List<DocActionData>> getActionList(int actionNameCde, EnumTaskStatus enumTaskStatus,
			List<String> queueNameCdes, EnumEventOperator taskStatusOperator) {
		List<List<DocActionData>> docActionDataListOfList = new ArrayList<>();
		final class ActionListCallable implements Callable<List<DocActionData>> {
			private int queueNameCde;

			public ActionListCallable(int queueNameCde) {
				this.queueNameCde = queueNameCde;
			}

			public List<DocActionData> call() throws Exception {
				return getActionList(actionNameCde, enumTaskStatus, queueNameCde, taskStatusOperator);
			}

		}
		try {
			List<Future<List<DocActionData>>> resultList = new ArrayList<>();
			for (String queueCde : queueNameCdes) {
				ActionListCallable actionListCallable = new ActionListCallable(Integer.parseInt(queueCde));
				Future<List<DocActionData>> result = executor.submit(actionListCallable);
				resultList.add(result);
			}
			for (Future<List<DocActionData>> future : resultList) {
				try {
					docActionDataListOfList.add(future.get());
				} catch (Exception e) {
					logger.error("Thread error", e);
				}
			}
		} catch (Exception e) {
			logger.error("Error in Action Service", e);
		}
		return docActionDataListOfList;

	}

	public int updateAction(ActionParamAttrMappingData actionData, EnumTaskStatus EnumTaskStatus) {
		JsonArrayBuilder jsonActionArrayBuilder = Json.createArrayBuilder();
		JsonObjectBuilder jsonActionObjectBuilder = Json.createObjectBuilder();
		jsonActionObjectBuilder.add(DocwbWebConstants.DOC_ACTION_REL_ID, actionData.getDocActionRelId());
		jsonActionObjectBuilder.add(DocwbWebConstants.TASK_STATUS_CDE, EnumTaskStatus.getValue());
		if (actionData.getTaskActionResult() != null) {
			jsonActionObjectBuilder.add(DocwbWebConstants.ACTION_RESULT, actionData.getTaskActionResult());
		}
		jsonActionArrayBuilder.add(jsonActionObjectBuilder.build());
		JsonObjectBuilder jsonRequestBuilder1 = Json.createObjectBuilder();
		jsonRequestBuilder1.add(DocwbWebConstants.ACTION_DATA_LIST, jsonActionArrayBuilder.build());
		JsonArrayBuilder jsonArrayBuilder1 = Json.createArrayBuilder();
		jsonArrayBuilder1.add(jsonRequestBuilder1.build());
		JsonArray jsonRequest = jsonArrayBuilder1.build();
		int statusCode = 0;
		executeHttpCall(HttpCallType.PUT, PropertyManager.getInstance().getProperty(PROP_NAME_API_ACTION_URL),
				jsonRequest);
		return statusCode;
	}

	public ActionData getActionData(int actionNameCde, long docId) {
		String url = "";
		ActionData actionData = null;

		try {
			URIBuilder uriBuilder = new URIBuilder(
					PropertyManager.getInstance().getProperty(PROP_NAME_API_ACTION_PARAM_ATTR_RECOMMENDATION_URL));
			if (actionNameCde > 0)
				uriBuilder.addParameter(DocwbWebConstants.ACTION_NAME_CDE, String.valueOf(actionNameCde));
			if (docId > 0)
				uriBuilder.addParameter(DocwbWebConstants.DOC_ID, String.valueOf(docId));
			url = uriBuilder.build().toURL().toString();
		} catch (Exception e) {
			logger.error("Error occurred in getActionData", e);
		}
		JsonObject jsonResponse = (JsonObject) executeHttpCall(HttpCallType.GET, url);
		if (jsonResponse != null && !jsonResponse.isNull(DocwbWebConstants.RESPONSE)) {
			JsonArray responseArray = jsonResponse.getJsonArray(DocwbWebConstants.RESPONSE);
			if (!responseArray.isEmpty()) {
				JsonObject actionDataObj = responseArray.getJsonObject(0);
				actionData = new ActionData();
				actionData.setActionNameCde(actionDataObj.getInt(DocwbWebConstants.ACTION_NAME_CDE));
				actionData.setActionNameTxt(actionDataObj.getString(DocwbWebConstants.ACTION_NAME_TXT));
				JsonArray jSONArray = actionDataObj.getJsonArray(DocwbWebConstants.MAPPING_LIST);

				List<ParamAttrData> mappingList = new ArrayList<ParamAttrData>();
				for (int k = 0; k < jSONArray.size(); k++) {
					JsonObject paramAttrDataObj = jSONArray.getJsonObject(k);
					if (!paramAttrDataObj.isNull(DocwbWebConstants.PARAM_NAME_TXT)) {
						ParamAttrData paramAttrData = new ParamAttrData();
						paramAttrData.setAttrNameCde(paramAttrDataObj.getInt(DocwbWebConstants.ATTR_NAME_CDE));
						paramAttrData.setAttrNameTxt(paramAttrDataObj.getString(DocwbWebConstants.ATTR_NAME_TXT));
						if (!paramAttrDataObj.isNull(DocwbWebConstants.ATTR_VALUES)) {
							JsonArray attrValuesArray = paramAttrDataObj.getJsonArray(DocwbWebConstants.ATTR_VALUES);
							List<String> attrValues = new ArrayList<String>();
							for (int l = 0; l < attrValuesArray.size(); l++) {
								attrValues.add(attrValuesArray.getString(l));
							}
							paramAttrData.setAttrValues(attrValues);
						}
						paramAttrData.setParamNameCde(paramAttrDataObj.getInt(DocwbWebConstants.PARAM_NAME_CDE));
						paramAttrData.setParamNameTxt(paramAttrDataObj.getString(DocwbWebConstants.PARAM_NAME_TXT));
						mappingList.add(paramAttrData);
					}
				}
				actionData.setMappingList(mappingList);
			}
		}
		return actionData;

	}

	public void addAction(DocActionData docActionData) {
		JsonObjectBuilder jsonRequestBuilder = Json.createObjectBuilder();
		jsonRequestBuilder.add(DocwbWebConstants.DOC_ID, docActionData.getDocId());
		List<ActionData> actionDataList = docActionData.getActionDataList();
		JsonArrayBuilder builder = Json.createArrayBuilder();
		for (ActionData actionData : actionDataList) {
			if (actionData != null) {
				JsonObjectBuilder action = Json.createObjectBuilder();
				action.add(DocwbWebConstants.ACTION_NAME_CDE, actionData.getActionNameCde());
				action.add(DocwbWebConstants.ACTION_NAME_TXT, actionData.getActionNameTxt());
				action.add(DocwbWebConstants.TASK_TYPE_CDE, docActionData.getTaskType().getValue());
				List<ParamAttrData> mappingList = actionData.getMappingList();
				JsonArrayBuilder builder1 = Json.createArrayBuilder();
				for (ParamAttrData paramAttrData : mappingList) {
					if (paramAttrData != null && paramAttrData.getParamValue() != null) {
						JsonObjectBuilder paramAttr = Json.createObjectBuilder();
						paramAttr.add(DocwbWebConstants.ATTR_NAME_TXT, paramAttrData.getAttrNameTxt());
						paramAttr.add(DocwbWebConstants.PARAM_NAME_CDE, paramAttrData.getParamNameCde());
						paramAttr.add(DocwbWebConstants.ATTR_NAME_CDE, paramAttrData.getAttrNameCde());
						paramAttr.add(DocwbWebConstants.PARAM_VALUE, paramAttrData.getParamValue());
						builder1.add(paramAttr);
					}
				}
				action.add(DocwbWebConstants.MAPPING_LIST, builder1.build());
				builder.add(action.build());
			}
		}
		jsonRequestBuilder.add(DocwbWebConstants.ACTION_DATA_LIST, builder.build());
		JsonArrayBuilder jsonRequest = Json.createArrayBuilder();
		jsonRequest.add(jsonRequestBuilder);
		executeHttpCall(HttpCallType.POST, PropertyManager.getInstance().getProperty(PROP_NAME_API_ACTION_URL),
				jsonRequest.build());

	}

	public RecommendedActionData getRecommendation(long docId) {
		RecommendedActionData recommendedActionData = null;
		String url = "";
		try {
			URIBuilder uriBuilder = new URIBuilder(
					PropertyManager.getInstance().getProperty(PROP_NAME_API_GET_RECOMMENDED_ACTION));
			uriBuilder.addParameter(DocwbWebConstants.DOC_ID, String.valueOf(docId));
			url = uriBuilder.build().toURL().toString();
		} catch (Exception e) {
			logger.error("Error occurred in getRecommendation", e);
		}
		JsonObject jsonResponseObj = (JsonObject) executeHttpCall(HttpCallType.GET, url);
		if (jsonResponseObj != null && !jsonResponseObj.isNull(DocwbWebConstants.RESPONSE)) {
			JsonObject jsonResponse = jsonResponseObj.getJsonObject(DocwbWebConstants.RESPONSE);
			recommendedActionData = new RecommendedActionData();
			recommendedActionData.setActionNameCde(jsonResponse.getInt(DocwbWebConstants.ACTION_NAME_CDE));
			recommendedActionData.setConfidencePct(jsonResponse.getInt(DocwbWebConstants.CONFIDENCE_PCT));
			recommendedActionData.setRecommendedPct(jsonResponse.getInt(DocwbWebConstants.RECOMMENDED_PCT));
		}

		return recommendedActionData;
	}

	@Override
	public void startThreads(int threadCount) {
		executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadCount,
				new SimpleThreadFactory(DocwbWebConstants.THREAD_NAME_ACTION_SERVICE));
		logger.info("Started thread pool with {} threads", threadCount);
	}

	@Override
	public void stopThreads() {
		executor.shutdown();
		try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			logger.error("Error while shutting down Action Service", e);
		}
		logger.info("Stopped thread pool");
	}

}
