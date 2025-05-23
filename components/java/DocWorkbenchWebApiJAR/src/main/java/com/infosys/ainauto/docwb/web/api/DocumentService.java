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
import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.commonutils.SimpleThreadFactory;
import com.infosys.ainauto.docwb.web.common.DocwbWebConstants;
import com.infosys.ainauto.docwb.web.common.IServiceThreadHandler;
import com.infosys.ainauto.docwb.web.common.PropertyManager;
import com.infosys.ainauto.docwb.web.data.AttributeData;
import com.infosys.ainauto.docwb.web.data.DocumentData;
import com.infosys.ainauto.docwb.web.exception.DocwbWebException;
import com.infosys.ainauto.docwb.web.type.EnumDocType;
import com.infosys.ainauto.docwb.web.type.EnumEventOperator;
import com.infosys.ainauto.docwb.web.type.EnumEventType;
import com.infosys.ainauto.docwb.web.type.EnumExtractType;
import com.infosys.ainauto.docwb.web.type.EnumSystemAttributeName;
import com.infosys.ainauto.docwb.web.type.EnumTaskStatus;

public class DocumentService extends HttpClientBase implements IDocumentService, IServiceThreadHandler {

	private static Logger logger = LoggerFactory.getLogger(DocumentService.class);

	private static final String PROP_NAME_API_DOCUMENT = "docwb.api.doc.url";
	private static final String PROP_NAME_API_UPDATE_DOC_TASK_STATUS = "docwb.api.task.status.url";
	private static final String PROP_NAME_API_INSERT_DOC_EVENT_TYPE = "docwb.api.event.type.url";
	private static final String PROP_NAME_API_DOCUMENT_ASSIGN_REASSIGN = "docwb.api.doc.user.url";

	private static final int APP_USER_ID_NONE = 0;

	private static final String DOC_TYPE_CDE = "docTypeCde";
	private static final String DOC_LOCATION = "docLocation";
	private static final String TASK_STATUS_CODE = "taskStatusCde";
	private static final String LOCK_STATUS_CDE = "lockStatusCde";
	private static final String EVENT_TYPE_CDE = "eventTypeCde";
	private static final String QUEUE_NAME_CDE = "queueNameCde";
	private static final String APP_USER_ID = "appUserId";
	private static final String PREV_APP_USER_ID = "prevAppUserId";

	private String documentApiUrl = "";
	private String updateDocTaskStatusApiUrl = "";
	private String insertDocEventTypeApiUrl = "";
	private String assignReassignApiUrl = "";
	private ThreadPoolExecutor executor;

	// Protected constructor to avoid instantiation by outside world
	protected DocumentService(HttpClientBase.Authentication.BearerAuthenticationConfig bearerAuthConfig,
			int defaultMaxPerRoute) {
		super(new HttpClientConfig().setDefaultMaxPerRoute(defaultMaxPerRoute), bearerAuthConfig);
		documentApiUrl = PropertyManager.getInstance().getProperty(PROP_NAME_API_DOCUMENT);
		updateDocTaskStatusApiUrl = PropertyManager.getInstance().getProperty(PROP_NAME_API_UPDATE_DOC_TASK_STATUS);
		insertDocEventTypeApiUrl = PropertyManager.getInstance().getProperty(PROP_NAME_API_INSERT_DOC_EVENT_TYPE);
		assignReassignApiUrl = PropertyManager.getInstance().getProperty(PROP_NAME_API_DOCUMENT_ASSIGN_REASSIGN);
	}

	public long addNewDocumentWithAttributes(DocumentData documentData) throws DocwbWebException {

		long docId = 0;
		JsonObjectBuilder jsonRequestBuilder = Json.createObjectBuilder();
		jsonRequestBuilder.add(DOC_TYPE_CDE, documentData.getDocType().getValue());
		jsonRequestBuilder.add(DOC_LOCATION, documentData.getDocLocation());
		jsonRequestBuilder.add(LOCK_STATUS_CDE, documentData.getLockStatus().getValue());
		jsonRequestBuilder.add(QUEUE_NAME_CDE, documentData.getQueueNameCde());
		List<AttributeData> attributeDataList = documentData.getAttributes();

		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
		for (AttributeData attributeData : attributeDataList) {
			if (attributeData != null) {
				JsonObjectBuilder attribute = Json.createObjectBuilder();
				attribute.add(DocwbWebConstants.ATTR_NAME_CDE, attributeData.getAttrNameCde());
				attribute.add(DocwbWebConstants.ATTR_VALUE, attributeData.getAttrValue());
				attribute.add(DocwbWebConstants.EXTRACT_TYPE_CDE, attributeData.getExtractType().getValue());
				attribute.add(DocwbWebConstants.CONFIDENCE_PCT, attributeData.getConfidencePct());
				arrayBuilder.add(attribute);
			}
		}

		jsonRequestBuilder.add(DocwbWebConstants.ATTRIBUTES, arrayBuilder.build());
		JsonObject jsonRequest = jsonRequestBuilder.build();
		JsonObject jsonResponse = (JsonObject) executeHttpCall(HttpCallType.POST, documentApiUrl, jsonRequest);

		if (jsonResponse != null && jsonResponse.getInt("responseCde") == 0) {
			docId = jsonResponse.getJsonObject("response").getInt("docId");
		} else {
			throw new DocwbWebException("Error occurred in adding document with attributes" + " ResponseCde:  "
					+ jsonResponse.getInt("responseCde") + " Response: " + jsonResponse.get("response"));
		}

		return docId;
	}

	public void updateDocTaskStatus(long docId, EnumTaskStatus taskStatus) {
		JsonObjectBuilder jsonRequestBuilder = Json.createObjectBuilder();
		jsonRequestBuilder.add(DocwbWebConstants.DOC_ID, docId);
		jsonRequestBuilder.add(TASK_STATUS_CODE, taskStatus.getValue());

		JsonObject jsonRequest = jsonRequestBuilder.build();
		executeHttpCall(HttpCallType.POST, updateDocTaskStatusApiUrl, jsonRequest);
	}

	public List<DocumentData> getDocumentList(EnumEventType highestEventType,
			EnumEventOperator highestEventTypeOperator, EnumEventType latestEventType,
			EnumEventOperator latestEventTypeOperator, long docId, int queueNameCde, String attrNameCdes) {
		List<DocumentData> documentList = new ArrayList<DocumentData>();
		String url = "";
		try {

			URIBuilder uriBuilder = new URIBuilder(documentApiUrl);
			uriBuilder.addParameter(QUEUE_NAME_CDE, String.valueOf(queueNameCde));
			uriBuilder.addParameter("sortOrder", "ASC");
			if (highestEventType != null && highestEventTypeOperator != null) {
				uriBuilder.addParameter("highestEventTypeCde", String.valueOf(highestEventType.getValue()));
				uriBuilder.addParameter("highestEventTypeOperator", highestEventTypeOperator.getValue());
			}
			if (latestEventType != null && latestEventTypeOperator != null) {
				uriBuilder.addParameter("latestEventTypeCde", String.valueOf(latestEventType.getValue()));
				uriBuilder.addParameter("latestEventTypeOperator", latestEventTypeOperator.getValue());
			}
			if (docId > 0) {
				uriBuilder.addParameter(DocwbWebConstants.DOC_ID, String.valueOf(docId));
			}
			if (attrNameCdes != null) {
				uriBuilder.addParameter("attrNameCdes", attrNameCdes);
			}

			url = uriBuilder.build().toURL().toString();
		} catch (Exception e) {
			logger.error("Error occurred in getDocumentList", e);
		}
		JsonObject jsonResponseObj = (JsonObject) executeHttpCall(HttpCallType.GET, url);
		if (jsonResponseObj != null) {
			JsonArray jsonResponse = jsonResponseObj.getJsonArray("response");
			for (int k = 0; k < jsonResponse.size(); k++) {
				JsonObject documentDataObj = jsonResponse.getJsonObject(k);
				DocumentData documentData = new DocumentData();
				List<AttributeData> attributeDataList = new ArrayList<AttributeData>();
				documentData.setDocId(documentDataObj.getInt(DocwbWebConstants.DOC_ID));
				documentData.setDocType(EnumDocType.get(documentDataObj.getInt(DOC_TYPE_CDE)));
				documentData.setQueueNameCde(documentDataObj.getInt(DocwbWebConstants.QUEUE_NAME_CDE));
				JsonArray attributesList = documentDataObj.getJsonArray(DocwbWebConstants.ATTRIBUTES);
				for (int i = 0; i < attributesList.size(); i++) {
					JsonObject docAttributeObj = attributesList.getJsonObject(i);
					AttributeData attributeData = new AttributeData();
					attributeData.setAttrNameCde(docAttributeObj.getInt(DocwbWebConstants.ATTR_NAME_CDE));
					attributeData.setAttrNameTxt(docAttributeObj.getString(DocwbWebConstants.ATTR_NAME_TXT));
					attributeData.setAttrValue(docAttributeObj.getString(DocwbWebConstants.ATTR_VALUE, ""));
					if (attributeData.getAttrNameCde() == EnumSystemAttributeName.MULTI_ATTRIBUTE.getCde()) {
						JsonArray multiAttrArray = docAttributeObj.getJsonArray(DocwbWebConstants.ATTRIBUTES);
						if (ListUtility.hasValue(multiAttrArray)) {
							List<AttributeData> multiAttrDataList = new ArrayList<>();
							for (int j = 0; j < multiAttrArray.size(); j++) {
								JsonObject multiAttrobject = multiAttrArray.getJsonObject(j);
								AttributeData multiAttrData = new AttributeData();
								multiAttrData
										.setAttrNameTxt(multiAttrobject.getString(DocwbWebConstants.ATTR_NAME_TXT));
								multiAttrData.setAttrValue(multiAttrobject.getString(DocwbWebConstants.ATTR_VALUE));
								multiAttrData
										.setConfidencePct(multiAttrobject.getInt(DocwbWebConstants.CONFIDENCE_PCT));
								multiAttrDataList.add(multiAttrData);
							}
							attributeData.setAttributeDataList(multiAttrDataList);
						}
					}
					attributeData.setExtractType(
							EnumExtractType.get(docAttributeObj.getInt(DocwbWebConstants.EXTRACT_TYPE_CDE)));
					attributeData.setConfidencePct(docAttributeObj.getJsonNumber(DocwbWebConstants.CONFIDENCE_PCT)
							.bigDecimalValue().floatValue());
					attributeDataList.add(attributeData);
				}
				documentData.setAttributes(attributeDataList);
				documentList.add(documentData);
			}
		}
		return documentList;
	}

	public List<List<DocumentData>> getDocumentList(EnumEventType highestEventType,
			EnumEventOperator highestEventTypeOperator, EnumEventType latestEventType,
			EnumEventOperator latestEventTypeOperator, long docId, List<String> queueNameCdes, String attrNameCdes) {
		List<List<DocumentData>> documentDataListOfList = new ArrayList<>();
		final class DocumentListCallable implements Callable<List<DocumentData>> {
			private int queueNameCde;

			public DocumentListCallable(int queueNameCde) {
				this.queueNameCde = queueNameCde;
			}

			public List<DocumentData> call() throws Exception {
				return getDocumentList(highestEventType, highestEventTypeOperator, latestEventType,
						latestEventTypeOperator, docId, queueNameCde, attrNameCdes);
			}
		}

		try {
			List<Future<List<DocumentData>>> resultList = new ArrayList<>();
			for (String queueCde : queueNameCdes) { // 6 queues 5 threads
				DocumentListCallable docListCallable = new DocumentListCallable(Integer.parseInt(queueCde));
				Future<List<DocumentData>> result = executor.submit(docListCallable);
				resultList.add(result);
			}

			for (Future<List<DocumentData>> future : resultList) {
				try {
					documentDataListOfList.add(future.get()); // 60 documents
				} catch (Exception e) {
					logger.error("Thread error", e);
				}
			}
		} catch (Exception e) {
			logger.error("Error in Document Service", e);
		}
		return documentDataListOfList;
	}

	public void insertDocEventType(long docId, EnumEventType eventType) {
		JsonObjectBuilder jsonRequestBuilder = Json.createObjectBuilder();
		jsonRequestBuilder.add(DocwbWebConstants.DOC_ID, docId);
		jsonRequestBuilder.add(EVENT_TYPE_CDE, eventType.getValue());

		JsonObject jsonRequest = jsonRequestBuilder.build();
		executeHttpCall(HttpCallType.POST, insertDocEventTypeApiUrl, jsonRequest);
	}

	@Override
	public boolean assignCase(long docId, long appUserId) throws DocwbWebException {
		JsonObjectBuilder jsonRequestBuilder = Json.createObjectBuilder();
		jsonRequestBuilder.add(DocwbWebConstants.DOC_ID, docId);
		jsonRequestBuilder.add(APP_USER_ID, appUserId);
		jsonRequestBuilder.add(PREV_APP_USER_ID, APP_USER_ID_NONE);

		JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
		jsonArrayBuilder.add(jsonRequestBuilder);

		JsonArray jsonArray = jsonArrayBuilder.build();
		JsonObject jsonResponse = (JsonObject) executeHttpCall(HttpCallType.POST, assignReassignApiUrl, jsonArray);
		
		if (jsonResponse != null) {
			int responseCde = jsonResponse.getInt("responseCde");
			if (responseCde == 0) {
				return true;
			} else {
				throw new DocwbWebException("Error occurred while assigning case." + " ResponseCde:  " + responseCde
						+ " Response: " + jsonResponse.get("response"));
			}
		} else {
			throw new DocwbWebException("Error occurred while assigning case. No json response");
		}
	}

	@Override
	public void startThreads(int threadCount) {
		executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadCount,
				new SimpleThreadFactory(DocwbWebConstants.THREAD_NAME_DOC_SERVICE));
		logger.info("Started thread pool with {} threads", threadCount);
	}

	@Override
	public void stopThreads() {
		executor.shutdown();
		try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			logger.error("Error while shutting down Document Service", e);
		}
		logger.info("Stopped thread pool");
	}
}
