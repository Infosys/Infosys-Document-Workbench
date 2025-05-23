/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.core.template.action.attribute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.json.JsonArray;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.engine.core.common.AttributeHelper;
import com.infosys.ainauto.docwb.engine.core.common.DocwbEngineCoreConstants;
import com.infosys.ainauto.docwb.engine.core.common.type.EnumExecutionEventType;
import com.infosys.ainauto.docwb.engine.core.common.type.EnumExecutorType;
import com.infosys.ainauto.docwb.engine.core.db.logger.IDbLogger;
import com.infosys.ainauto.docwb.engine.core.model.SummaryData;
import com.infosys.ainauto.docwb.engine.core.rule.IAttributeExtractRuleAsync;
import com.infosys.ainauto.docwb.engine.core.rule.IAttributeExtractRuleListener;
import com.infosys.ainauto.docwb.engine.core.service.DocWbApiClient;
import com.infosys.ainauto.docwb.engine.core.service.client.IMetricsService;
import com.infosys.ainauto.docwb.engine.core.service.client.MetricsService.EnumMetric;
import com.infosys.ainauto.docwb.engine.core.template.attribute.AttributeExtractorBase;
import com.infosys.ainauto.docwb.web.api.IActionService;
import com.infosys.ainauto.docwb.web.api.IAttributeService;
import com.infosys.ainauto.docwb.web.api.IDocumentService;
import com.infosys.ainauto.docwb.web.common.DocwbWebConstants;
import com.infosys.ainauto.docwb.web.data.ActionParamAttrMappingData;
import com.infosys.ainauto.docwb.web.data.AnnotationData;
import com.infosys.ainauto.docwb.web.data.AttachmentData;
import com.infosys.ainauto.docwb.web.data.AttributeData;
import com.infosys.ainauto.docwb.web.data.DocActionData;
import com.infosys.ainauto.docwb.web.data.DocumentData;
import com.infosys.ainauto.docwb.web.type.EnumEventOperator;
import com.infosys.ainauto.docwb.web.type.EnumEventType;
import com.infosys.ainauto.docwb.web.type.EnumSystemAttributeName;
import com.infosys.ainauto.docwb.web.type.EnumTaskStatus;

@Component
public abstract class ExtractAttributeActionExecutorBase {

	private static Logger logger = LoggerFactory.getLogger(AttributeExtractorBase.class);

	@Autowired
	private DocWbApiClient docWbApiClient;

	@Autowired
	private IMetricsService metricsService;

	@Autowired
	private IDbLogger dbLogger;

	@Autowired
	private ApplicationContext context;

	@Autowired
	private Environment environment;

	private IDocumentService documentService;
	private IActionService actionService;
	private IAttributeService attributeService;

	private static final String ERROR_PREFIX = "ExtractAttributeActionExecutorBase - Error occurred in method ";
	private static final String PROP_NAME_DB_USERNAME = "spring.datasource.username";

	private int actionNameCde = DocwbEngineCoreConstants.ACTION_RE_EXTRACT_DATA;

	private static Map<String, IAttributeExtractRuleAsync> beanMap;
	private static Map<Integer, String> attrNameMap;
	private static String dbUserName;

	@PostConstruct
	private void init() {
		dbUserName = environment.getProperty(PROP_NAME_DB_USERNAME);
		documentService = docWbApiClient.getDocumentService();
		actionService = docWbApiClient.getActionService();
		attributeService = docWbApiClient.getAttributeService();
		beanMap = context.getBeansOfType(IAttributeExtractRuleAsync.class);
		attrNameMap = attributeService.getAttributeNames();
		logger.info(beanMap.keySet().toString());
	}

	public void execute(String name, JsonObject jsonConfig) {
		// MDC Logging
		MDC.put(DocwbEngineCoreConstants.MDC_TEMPLATE_NAME, name + "-" + StringUtility.generateTransactionId());
		AtomicInteger successfulCount = new AtomicInteger();
		AtomicInteger failedCount = new AtomicInteger();
		AtomicInteger waitingCount = new AtomicInteger();
		AtomicInteger successfulRuleCount = new AtomicInteger();
		AtomicInteger failedRuleCount = new AtomicInteger();
		AtomicInteger waitingRuleCount = new AtomicInteger();
		int totalCount = 0;
		@SuppressWarnings("unused")
		int totalRuleCount = 0;
		String errorMessage = "";
		HashMap<String, Integer> docToRulesMap = new HashMap<>();

		long executionId = DocwbEngineCoreConstants.EXECUTION_ID_EMPTY;

		metricsService.startTimer(EnumMetric.WORKFLOW_EXECUTOR_START_ELAPSED_TIME,
				EnumExecutorType.EXTRACT_ATTRIBUTE_ACTION_EXECUTOR, true);

		int queueNameCde;
		JsonArray jsonArrayOfBaseRules;
		if (jsonConfig != null && !jsonConfig.isNull("attributeRuleMapping")) {
			JsonArray jsonArrMapping = jsonConfig.getJsonArray("attributeRuleMapping");
			if (!jsonArrMapping.isEmpty()) {
				for (int i = 0; i < jsonArrMapping.size(); i++) {

					JsonObject jsonObjItem = jsonArrMapping.getJsonObject(i);
					queueNameCde = jsonObjItem.getInt("queueNameCde", 0);
					if (jsonObjItem.isNull("baseRules")) {
						continue;
					}
					jsonArrayOfBaseRules = jsonObjItem.getJsonArray("baseRules");

					List<DocActionData> docActionDataList = new ArrayList<>();
					try {
						docActionDataList = getActions(queueNameCde, actionNameCde);
					} catch (Exception ex) {
						errorMessage = ERROR_PREFIX + "getActions";
						logger.error(errorMessage, ex);
					}

					if (ListUtility.hasValue(docActionDataList)) {
						executionId = dbLogger.startExecution(EnumExecutorType.EXTRACT_ATTRIBUTE_ACTION_EXECUTOR, name,
								"");
						dbLogger.addEvent(executionId, EnumExecutionEventType.WORK_STARTED, totalCount + " action(s)");
						for (DocActionData docActionData : docActionDataList) {
							long docId = docActionData.getDocId();
							DocumentData documentDbData = null;
							documentService.updateDocTaskStatus(docId, EnumTaskStatus.IN_PROGRESS);
							try {
								documentDbData = getDocument(docId, queueNameCde);
							} catch (Exception ex) {
								errorMessage = ERROR_PREFIX + "getDocument";
								logger.error(errorMessage, ex);
							}
							try {
								List<AttachmentData> attachmentDatas = attributeService
										.getAttachmentAttributeList(docId);
								if (ListUtility.hasValue(attachmentDatas) && documentDbData != null)
									documentDbData.setAttachmentDataList(attachmentDatas);
							} catch (Exception e) {
								errorMessage = ERROR_PREFIX + "getAttachmentAttributes";
								logger.error(errorMessage, e);
							}
							final DocumentData documentData = documentDbData;
							List<ActionParamAttrMappingData> actionParamAttrMappingDataList = docActionData
									.getActionParamAttrMappingDataList();
							totalCount += actionParamAttrMappingDataList.size();
							waitingCount.set(totalCount);
							for (ActionParamAttrMappingData actionParamAttrMappingData : actionParamAttrMappingDataList) {
								DocumentData resultDocumentData = new DocumentData();
								String paramValueAsJson = actionParamAttrMappingData.getParamAttrDataList().get(0)
										.getParamValue();
								DocumentData paramDocumentData = AttributeHelper
										.convertJsonStringToAttr(paramValueAsJson);
								List<IAttributeExtractRuleAsync> baseAttributeExtractionRuleList = null;
								List<IAttributeExtractRuleAsync> attributeExtractionRuleList = null;
								try {
									boolean isUpdateActionStatus = isPublishActionStatusInProgress(
											actionParamAttrMappingData);
									if (isUpdateActionStatus) {
										// Update action as in-progress in Workbench
										actionService.updateAction(actionParamAttrMappingData,
												EnumTaskStatus.IN_PROGRESS);
									}

									if (documentData != null) {
										try {
											List<String> attrNameCdeAndTxtList = new ArrayList<>();
											if (paramDocumentData.getAttributes() != null) {
												attrNameCdeAndTxtList = paramDocumentData.getAttributes().stream().map(
														data -> data.getAttrNameCde() + "," + data.getAttrNameTxt())
														.collect(Collectors.toList());
												baseAttributeExtractionRuleList = extractAttributesUsingBaseRules(
														jsonArrayOfBaseRules, documentData, attrNameCdeAndTxtList,
														DocwbEngineCoreConstants.RULE_TYPE_EMAIL, 0);
												if (ListUtility.hasValue(baseAttributeExtractionRuleList)) {
													int noOfRulesForDoc = baseAttributeExtractionRuleList.size();
													docToRulesMap.put(String.valueOf(documentData.getDocId()),
															new Integer(noOfRulesForDoc));
													totalRuleCount += noOfRulesForDoc;
													waitingRuleCount.addAndGet(noOfRulesForDoc);
													executeRule(baseAttributeExtractionRuleList, resultDocumentData,
															docToRulesMap, waitingRuleCount, successfulRuleCount,
															failedRuleCount, documentData,
															DocwbEngineCoreConstants.RULE_TYPE_EMAIL, 0,
															paramDocumentData);
												}
												attributeExtractionRuleList = extractAttributesUsingRules(
														documentData.getAttributes(), attrNameCdeAndTxtList,
														jsonArrayOfBaseRules, resultDocumentData.getAttributes());
												if (ListUtility.hasValue(attributeExtractionRuleList)) {
													int noOfRulesForDoc = attributeExtractionRuleList.size();
													docToRulesMap.put(String.valueOf(docId),
															new Integer(noOfRulesForDoc));
													totalRuleCount += noOfRulesForDoc;
													waitingRuleCount.addAndGet(noOfRulesForDoc);
													executeRule(attributeExtractionRuleList, resultDocumentData,
															docToRulesMap, waitingRuleCount, successfulRuleCount,
															failedRuleCount, documentData,
															DocwbEngineCoreConstants.RULE_TYPE_EMAIL, 0,
															paramDocumentData);
												}
											}
											if (paramDocumentData.getAttachmentDataList() != null) {
												List<AttachmentData> dataList = new ArrayList<>();
												if (ListUtility.hasValue(paramDocumentData.getAttachmentDataList()))
													dataList = paramDocumentData.getAttachmentDataList();
												if (ListUtility.hasValue(dataList)) {
													for (AttachmentData attachmentData : dataList) {
														List<AttributeData> inputAttrDataList = new ArrayList<>();
														if (documentData.getAttachmentDataList() != null && ListUtility
																.hasValue(documentData.getAttachmentDataList().stream()
																		.filter(data -> data
																				.getAttachmentId() == attachmentData
																						.getAttachmentId())
																		.collect(Collectors.toList())))
															inputAttrDataList = documentData.getAttachmentDataList()
																	.stream()
																	.filter(data -> data
																			.getAttachmentId() == attachmentData
																					.getAttachmentId())
																	.collect(Collectors.toList()).get(0)
																	.getAttributes();
														attrNameCdeAndTxtList = attachmentData.getAttributes().stream()
																.map(data -> data.getAttrNameCde() + ","
																		+ data.getAttrNameTxt())
																.collect(Collectors.toList());
														baseAttributeExtractionRuleList = extractAttributesUsingBaseRules(
																jsonArrayOfBaseRules, documentData,
																attrNameCdeAndTxtList,
																DocwbEngineCoreConstants.RULE_TYPE_DOCUMENT,
																attachmentData.getAttachmentId());
														if (ListUtility.hasValue(baseAttributeExtractionRuleList)) {
															int noOfRulesForDoc = baseAttributeExtractionRuleList
																	.size();
															docToRulesMap.put(String.valueOf(documentData.getDocId()),
																	new Integer(noOfRulesForDoc));
															totalRuleCount += noOfRulesForDoc;
															waitingRuleCount.addAndGet(noOfRulesForDoc);
															executeRule(baseAttributeExtractionRuleList,
																	resultDocumentData, docToRulesMap, waitingRuleCount,
																	successfulRuleCount, failedRuleCount, documentData,
																	DocwbEngineCoreConstants.RULE_TYPE_DOCUMENT,
																	attachmentData.getAttachmentId(),
																	paramDocumentData);
														}
														List<AttributeData> resultAttrDataList = new ArrayList<>();
														if (ListUtility
																.hasValue(resultDocumentData.getAttachmentDataList()))
															if (ListUtility.hasValue(
																	resultDocumentData.getAttachmentDataList().stream()
																			.filter(data -> data
																					.getAttachmentId() == attachmentData
																							.getAttachmentId())
																			.collect(Collectors.toList())))
																resultAttrDataList = resultDocumentData
																		.getAttachmentDataList().stream()
																		.filter(data -> data
																				.getAttachmentId() == attachmentData
																						.getAttachmentId())
																		.collect(Collectors.toList()).get(0)
																		.getAttributes();
														attributeExtractionRuleList = extractAttributesUsingRules(
																inputAttrDataList, attrNameCdeAndTxtList,
																jsonArrayOfBaseRules, resultAttrDataList);
														if (ListUtility.hasValue(attributeExtractionRuleList)) {
															int noOfRulesForDoc = attributeExtractionRuleList.size();
															docToRulesMap.put(String.valueOf(docId),
																	new Integer(noOfRulesForDoc));
															totalRuleCount += noOfRulesForDoc;
															waitingRuleCount.addAndGet(noOfRulesForDoc);
															executeRule(attributeExtractionRuleList, resultDocumentData,
																	docToRulesMap, waitingRuleCount,
																	successfulRuleCount, failedRuleCount, documentData,
																	DocwbEngineCoreConstants.RULE_TYPE_DOCUMENT,
																	attachmentData.getAttachmentId(),
																	paramDocumentData);
															List<AttachmentData> attachDataList = resultDocumentData
																	.getAttachmentDataList().stream()
																	.filter(data -> data
																			.getAttachmentId() == attachmentData
																					.getAttachmentId())
																	.collect(Collectors.toList());
															if (ListUtility.hasValue(attachDataList)) {
																AttachmentData attachData = attachDataList.get(0);
																List<AttributeData> attrDataList = ListUtility
																		.hasValue(attachData.getAttributes())
																				? attachData.getAttributes()
																				: new ArrayList<>();
																List<AnnotationData> annDataList = ListUtility
																		.hasValue(attachData.getAnnotations())
																				? attachData.getAnnotations()
																				: new ArrayList<>();
																for (int j = 1; j < attachDataList.size(); j++) {
																	attrDataList.addAll(
																			attachDataList.get(j).getAttributes());
																	if (ListUtility.hasValue(
																			attachDataList.get(j).getAnnotations()))
																		annDataList.addAll(
																				attachDataList.get(j).getAnnotations());
																}
																if (ListUtility.hasValue(attrDataList))
																	attachData.setAttributes(attrDataList);
																if (ListUtility.hasValue(annDataList))
																	attachData.setAnnotations(annDataList);
																List<AttachmentData> attachmentDataList = resultDocumentData
																		.getAttachmentDataList().stream()
																		.filter(data -> data
																				.getAttachmentId() != attachmentData
																						.getAttachmentId())
																		.collect(Collectors.toList());
																attachmentDataList.add(attachData);
																resultDocumentData
																		.setAttachmentDataList(attachmentDataList);
															}
														}

													}
												}
											}

										} catch (Exception ex) {
											errorMessage = ERROR_PREFIX + "extractAttributesUsingRules";
											logger.error(errorMessage, ex);
										}
										int count = (docToRulesMap.size() > 0)
												? docToRulesMap.get(String.valueOf(docId))
												: 0;
										if (count <= 0) {
											// && (ListUtility.hasValue(resultDocumentData.getAttributes())
											// || ListUtility.hasValue(resultDocumentData.getAttachmentDataList()))

											String actionResult = AttributeHelper.convertAttrToJsonString(
													resultDocumentData, paramDocumentData, attrNameMap, dbUserName);
											if (actionResult != null && actionResult.length() > 0) {
												successfulCount.incrementAndGet();
												waitingCount.decrementAndGet();
												actionParamAttrMappingData.setTaskActionResult(actionResult);
												try {
													boolean isUpdateActionResult = actionExecutionResult(null,
															actionParamAttrMappingData, actionResult);
													if (isUpdateActionResult) {
														// Update action as complete in
														// Workbench
														actionService.updateAction(actionParamAttrMappingData,
																EnumTaskStatus.FOR_YOUR_REVIEW);
														try {
															documentService.insertDocEventType(docId,
																	EnumEventType.ACTION_COMPLETED);
															// If no more actions pending, then change case status
															// TODO  Change this call to get action list for a
															// particular
															// doc only
															List<DocActionData> actionList = actionService
																	.getActionListForDoc(actionNameCde,
																			EnumTaskStatus.IN_PROGRESS, 0,
																			EnumEventOperator.EQUALS, docId);
															if (actionList.size() <= 0) {
																documentService.updateDocTaskStatus(docId,
																		EnumTaskStatus.FOR_YOUR_REVIEW);
															}
														} catch (Exception e) {
															errorMessage = ERROR_PREFIX + "insertDocEventType";
															logger.error(errorMessage, e);
														}

														// Close the case if customization logic has asked for it
														try {
															boolean isCloseCaseForDocument = isCloseCaseForDocument(
																	actionParamAttrMappingData);
															if (isCloseCaseForDocument) {
																documentService.updateDocTaskStatus(docId,
																		EnumTaskStatus.COMPLETE);
															}
														} catch (Exception e) {
															errorMessage = ERROR_PREFIX + "isCloseCaseForDocument";
															logger.error(errorMessage, e);
														}
													}
												} catch (Exception e) {
													failedCount.incrementAndGet();
													waitingCount.decrementAndGet();
													errorMessage = ERROR_PREFIX + "updateAction. Service call failed";
													logger.error(errorMessage, e);
													try {
														boolean isUpdateTask = actionExecutionResult((Exception) e,
																actionParamAttrMappingData, "");
														if (isUpdateTask) {
															actionService.updateAction(actionParamAttrMappingData,
																	EnumTaskStatus.FAILED);
														}
													} catch (Exception ex2) {
														errorMessage = ERROR_PREFIX + "updateAction";
														logger.error(errorMessage, ex2);
													}
												}

											} else {
												failedCount.incrementAndGet();
												waitingCount.decrementAndGet();
												errorMessage = ERROR_PREFIX + "executeAction. Service call failed";
												logger.error(errorMessage);
												try {
													Exception exception = new Exception();
													boolean isUpdateTask = actionExecutionResult((Exception) exception,
															actionParamAttrMappingData, "");
													if (isUpdateTask) {
														actionService.updateAction(actionParamAttrMappingData,
																EnumTaskStatus.FAILED);
													}
												} catch (Exception ex2) {
													errorMessage = ERROR_PREFIX + "actionExecutionFailed";
													logger.error(errorMessage, ex2);
												}
											}
										}
									}
								} catch (Exception ex) {
									failedCount.incrementAndGet();
									waitingCount.decrementAndGet();
									errorMessage = ERROR_PREFIX + "executeAction. Service call failed";
									logger.error(errorMessage, ex);
									try {
										boolean isUpdateTask = actionExecutionResult(ex, actionParamAttrMappingData,
												"");
										if (isUpdateTask) {
											actionService.updateAction(actionParamAttrMappingData,
													EnumTaskStatus.FAILED);
										}
									} catch (Exception ex2) {
										errorMessage = ERROR_PREFIX + "actionExecutionFailed";
										logger.error(errorMessage, ex2);
									}
								}
							}
						}

					} else {
						try {
							noActions();
						} catch (Exception ex) {
							errorMessage = ERROR_PREFIX + "noActions";
							logger.error(errorMessage, ex);
						}
					}
				}
			}
		}
		String dbExecutionSummary = "";
		try {
			// Thread.sleep(5000);
			SummaryData summaryData = new SummaryData(name + " [Extract Attribute Action Executor]", "action");
			summaryData.setTotalCount(totalCount);
			summaryData.setWaitingCount(waitingCount.get());
			summaryData.setFailedCount(failedCount.get());
			summaryData.setSuccessfulCount(successfulCount.get());
			if (totalCount > 0) {
				dbExecutionSummary = summaryData.toSimpleString();
			}
			terminate(summaryData);
		} catch (Exception ex) {
			errorMessage = ERROR_PREFIX + "terminate";
			logger.error(errorMessage, ex);
		} finally {
			if (executionId != DocwbEngineCoreConstants.EXECUTION_ID_EMPTY) {
				dbLogger.endExecution(executionId, dbExecutionSummary);
			}
			metricsService.startTimer(EnumMetric.WORKFLOW_EXECUTOR_END_ELAPSED_TIME,
					EnumExecutorType.EXTRACT_ATTRIBUTE_ACTION_EXECUTOR, true);
		}

	}

	private <T> void executeRule(List<IAttributeExtractRuleAsync> ruleList, DocumentData resultDocumentData,
			HashMap<String, Integer> docToRulesMap, AtomicInteger waitingRuleCount, AtomicInteger successfulRuleCount,
			AtomicInteger failedRuleCount, DocumentData documentData, String ruleType, long attachmentId,
			DocumentData paramDocumentData) {
		String errorMessage = "";
		if (ListUtility.hasValue(ruleList)) {
			for (IAttributeExtractRuleAsync rule : ruleList) {
				try {
					List<Object> objList = new ArrayList<Object>();
					objList.add(documentData);
					objList.add(ruleType);
					objList.add(paramDocumentData);
					rule.doExtractAsync(objList, new IAttributeExtractRuleListener() {
						@Override
						public void onAttributeExtractionComplete(Exception exception,
								DocumentData ruleResultDocumentData) {
							if (exception == null) {

								// Check if output and input point to same object reference
								if (documentData == ruleResultDocumentData) {
									exception = new Exception("Rule class should not modify input data");
								}

								if (ListUtility.hasValue(documentData.getAttributes())
										&& documentData.getAttributes() == ruleResultDocumentData.getAttributes()) {
									exception = new Exception("Rule class should not modify input data");
								}

								if (ListUtility.hasValue(documentData.getAttachmentDataList()) && documentData
										.getAttachmentDataList() == ruleResultDocumentData.getAttachmentDataList()) {
									exception = new Exception("Rule class should not modify input data");
								}

								AttributeHelper.removeNullAttributes(ruleResultDocumentData);

								// Add the result attributes to current documentData to be used by next rule in
								// queue
								AttributeHelper.refreshAttributesWithNewerValues(documentData,
										ruleResultDocumentData.getAttributes(),
										ruleResultDocumentData.getAttachmentDataList());

								if (ruleType.equalsIgnoreCase(DocwbEngineCoreConstants.RULE_TYPE_DOCUMENT)) {
									List<AttachmentData> attachmentDatas = new ArrayList<>();
									if (resultDocumentData.getAttachmentDataList() != null)
										attachmentDatas.addAll(resultDocumentData.getAttachmentDataList());
									List<AttachmentData> resultAttachList = ruleResultDocumentData
											.getAttachmentDataList().stream()
											.filter(data -> data.getAttachmentId() == attachmentId)
											.collect(Collectors.toList());
									if (ListUtility.hasValue(resultAttachList))
										attachmentDatas.addAll(resultAttachList);
									resultDocumentData.setAttachmentDataList(attachmentDatas);
								} else if (ruleType.equalsIgnoreCase(DocwbEngineCoreConstants.RULE_TYPE_EMAIL)) {
									List<AttributeData> attributeDatas = new ArrayList<>();
									List<AnnotationData> annotationDatas = new ArrayList<>();
									if (ListUtility.hasValue(resultDocumentData.getAttributes()))
										attributeDatas.addAll(resultDocumentData.getAttributes());
									if (ListUtility.hasValue(ruleResultDocumentData.getAttributes()))
										attributeDatas.addAll(ruleResultDocumentData.getAttributes());
									if (ListUtility.hasValue(resultDocumentData.getAnnotations()))
										annotationDatas.addAll(resultDocumentData.getAnnotations());
									if (ListUtility.hasValue(ruleResultDocumentData.getAnnotations()))
										annotationDatas.addAll(ruleResultDocumentData.getAnnotations());
									resultDocumentData.setAnnotations(annotationDatas);
									;
									resultDocumentData.setAttributes(attributeDatas);
								}
							}
							handleRuleResult(successfulRuleCount, failedRuleCount, waitingRuleCount, docToRulesMap,
									documentData.getDocId(), attachmentId, exception, resultDocumentData, ruleType);
						}

					});
				} catch (Exception ex) {
					errorMessage = ERROR_PREFIX + "doExtractAsync. Rule call failed";
					logger.error(errorMessage, ex);
					handleRuleResult(successfulRuleCount, failedRuleCount, waitingRuleCount, docToRulesMap,
							documentData.getDocId(), attachmentId, ex, null, ruleType);
				}
			}
		}
	}

	private void handleRuleResult(AtomicInteger successfulCount, AtomicInteger failedCount, AtomicInteger waitingCount,
			HashMap<String, Integer> docToRulesMap, long docId, long attachmentId, Exception exception,
			DocumentData resultDocumentData, String entity) {
		Integer noOfRulesPendingForDoc = docToRulesMap.get(String.valueOf(docId));
		docToRulesMap.put(String.valueOf(docId), --noOfRulesPendingForDoc);
		boolean isSaveToDb = false;
		if (exception != null) {
			failedCount.incrementAndGet();
			waitingCount.decrementAndGet();
			String errorMessage = ERROR_PREFIX + "doExtract. Rule execution logic failed";
			logger.error(errorMessage, exception);
			try {
				isSaveToDb = extractionFailed((Exception) exception);
			} catch (Exception ex2) {
				errorMessage = ERROR_PREFIX + "extractionFailed";
				logger.error(errorMessage, ex2);
			}
		} else {
			successfulCount.incrementAndGet();
			waitingCount.decrementAndGet();
			try {
				isSaveToDb = extractionComplete();
			} catch (Exception ex) {
				String errorMessage = ERROR_PREFIX + "extractionComplete";
				logger.error(errorMessage, ex);
			}
		}
		try {
			if (isSaveToDb && resultDocumentData != null) {
				if (entity.equalsIgnoreCase(DocwbEngineCoreConstants.RULE_TYPE_EMAIL)) {
					if (ListUtility.hasValue(resultDocumentData.getAttributes())) {
						resultDocumentData.getAttributes().stream().filter(a -> a != null);
					}
					if (ListUtility.hasValue(resultDocumentData.getAnnotations())) {
						resultDocumentData.getAnnotations().stream().filter(a -> a != null);
					}
				}
				if (ListUtility.hasValue(resultDocumentData.getAttachmentDataList())) {
					for (AttachmentData attachmentData : resultDocumentData.getAttachmentDataList()) {
						if (entity.equalsIgnoreCase(DocwbEngineCoreConstants.RULE_TYPE_DOCUMENT)
								&& attachmentData.getAttachmentId() == attachmentId) {
							if (ListUtility.hasValue(attachmentData.getAttributes())) {
								attachmentData.getAttributes().stream().filter(a -> a != null);
							}
							if (ListUtility.hasValue(attachmentData.getAnnotations())) {
								attachmentData.getAnnotations().stream().filter(a -> a != null);
							}
						}
					}
				}
			}
		} catch (Exception ex) {
			String errorMessage = ERROR_PREFIX + "Handle Attributes failed";
			logger.error(errorMessage, ex);
		}

	}

	protected List<DocActionData> getActions(int queueNameCde, int actionNameCde) throws Exception {
		List<DocActionData> docActionDataList = actionService.getActionList(actionNameCde, EnumTaskStatus.YET_TO_START,
				queueNameCde, EnumEventOperator.EQUALS);
		return docActionDataList;
	}

	protected void noActions() throws Exception {
		logger.info("No Data downloaded");
	}

	protected boolean isPublishActionStatusInProgress(ActionParamAttrMappingData actionParamAttrMappingData)
			throws Exception {
		return true;
	}

	/*** Execute Action Begin ***/
	/**
	 * Return the list of <b>base</b> rules to execute
	 * 
	 * @param ruleArray
	 * @param inputDocumentData
	 * @param attributeNameCdeAndTxtList
	 * @param ruleType
	 * @param id
	 * @return
	 */
	protected List<IAttributeExtractRuleAsync> extractAttributesUsingBaseRules(JsonArray ruleArray,
			DocumentData inputDocumentData, List<String> attributeNameCdeAndTxtList, String ruleType, long id) {
		List<IAttributeExtractRuleAsync> ruleList = new ArrayList<>();
		List<Integer> attrNameCdes = new ArrayList<>();
		List<Integer> attributeNameCdeList = attributeNameCdeAndTxtList.stream()
				.map(data -> Integer.valueOf(data.split(",")[0])).collect(Collectors.toList());
		for (int j = 0; j < ruleArray.size(); j++) {
			JsonObject objectInArray = ruleArray.getJsonObject(j);
			attrNameCdes.add(objectInArray.getInt("attrNameCde"));
			if (ruleType.equalsIgnoreCase(objectInArray.getString("ruleType"))) {
				if (attributeNameCdeList.contains(objectInArray.getInt("attrNameCde"))) {
					if (beanMap.containsKey(objectInArray.getString("ruleClass"))) {
						IAttributeExtractRuleAsync ruleToBeExecuted = beanMap.get(objectInArray.getString("ruleClass"));
						ruleList.add(ruleToBeExecuted);
					}

				} else {
					boolean isRuleToBeAdded = false;
					if (attributeNameCdeList.isEmpty()) {
						if (ruleType.equalsIgnoreCase(DocwbEngineCoreConstants.RULE_TYPE_EMAIL)) {
							if (ListUtility.hasValue(inputDocumentData.getAttributes())
									&& inputDocumentData.getAttributes().stream()
											.filter(e -> attrNameCdes.contains(e.getAttrNameCde())).count() <= 0) {
								isRuleToBeAdded = true;
							} else if (!ListUtility.hasValue(inputDocumentData.getAttributes())) {
								isRuleToBeAdded = true;
							}
						} else if (ruleType.equalsIgnoreCase(DocwbEngineCoreConstants.RULE_TYPE_DOCUMENT)) {
							if (ListUtility.hasValue(inputDocumentData.getAttachmentDataList())) {
								if (inputDocumentData.getAttachmentDataList().stream()
										.filter(data -> data.getAttachmentId() == id
												&& !ListUtility.hasValue(data.getAttributes()))
										.count() > 0) {
									isRuleToBeAdded = true;
								} else {
									for (AttachmentData attachData : inputDocumentData.getAttachmentDataList()) {
										if (attachData.getAttachmentId() == id) {
											if (attachData.getAttributes().stream()
													.filter(e -> attrNameCdes.contains(e.getAttrNameCde()))
													.count() <= 0) {
												isRuleToBeAdded = true;
												break;
											}
										}
									}

								}
							}
							// else {
							// isRuleToBeAdded = true;
							// }
						}
						if (isRuleToBeAdded && beanMap.containsKey(objectInArray.getString("ruleClass"))) {
							IAttributeExtractRuleAsync ruleToBeExecuted = beanMap
									.get(objectInArray.getString("ruleClass"));
							ruleList.add(ruleToBeExecuted);
						}
					}
				}
			}
		}
		return ruleList;
	}

	/**
	 * Return the list of <b>non-base</b> rules to execute
	 * 
	 * @param inputAttrDataList
	 * @param attributeNameCdeAndTxtList
	 * @param jsonBaseRuleArray
	 * @param resultAttrDataList
	 * @return
	 * @throws Exception
	 */
	protected List<IAttributeExtractRuleAsync> extractAttributesUsingRules(List<AttributeData> inputAttrDataList,
			List<String> attributeNameCdeAndTxtList, JsonArray jsonBaseRuleArray,
			List<AttributeData> resultAttrDataList) throws Exception {
		List<IAttributeExtractRuleAsync> ruleList = new ArrayList<>();
		List<Integer> attributeNameCdeList = attributeNameCdeAndTxtList.stream()
				.map(data -> Integer.valueOf(data.split(",")[0])).collect(Collectors.toList());
		for (int i = 0; i < jsonBaseRuleArray.size(); i++) {
			JsonObject jsonObjBaseRule = jsonBaseRuleArray.getJsonObject(i);
			// Major objectives:
			// Check if user requested attribute matches with base rule attribute
			// Check if user requested for all attributes (attributeNameCdeList would be
			// empty)
			// Check if already some attributes have been extracted (resultAttrDataList
			// would not be empty )

			// If no child rules exist, then iterate to next
			if (jsonObjBaseRule.getJsonArray("rules").isEmpty())
				continue;

			JsonArray jsonRulesArray = jsonObjBaseRule.getJsonArray("rules");

			int baseRuleAttrNameCde = jsonObjBaseRule.getInt("attrNameCde");

			if (attributeNameCdeList.contains(baseRuleAttrNameCde) || ListUtility.hasValue(resultAttrDataList)) {
				// If no attributes have been previously extracted, return from this function
				if (!ListUtility.hasValue(resultAttrDataList)) {
					return ruleList;
				}

				// Get the already extracted attribute that matches with base rule attribute
				List<AttributeData> baseRuleResultAttrDataList = resultAttrDataList.stream()
						.filter(data -> data.getAttrNameCde() == baseRuleAttrNameCde).collect(Collectors.toList());

				for (int j = 0; j < jsonRulesArray.size(); j++) {
					JsonObject jsonObjRule = jsonRulesArray.getJsonObject(j);
					JsonArray jArray = jsonObjRule.getJsonArray("ruleResult");
					if (baseRuleResultAttrDataList.stream().filter(data -> isRuleExist(jArray, data.getAttrValue()))
							.count() > 0) {

						JsonArray rules = jsonObjRule.getJsonArray("rules");
						for (int k = 0; k < rules.size(); k++) {
							JsonObject rule = rules.getJsonObject(k);
							if (beanMap.containsKey(rule.getString("ruleClass"))) {
								IAttributeExtractRuleAsync ruleToBeExecuted = beanMap.get(rule.getString("ruleClass"));
								ruleList.add(ruleToBeExecuted);
							}
						}
					}
				}
			} else {
				if (ListUtility.hasValue(inputAttrDataList)) {
					for (int j = 0; j < jsonRulesArray.size(); j++) {
						JsonObject jsonObjRule = jsonRulesArray.getJsonObject(j);
						JsonArray jArray = jsonObjRule.getJsonArray("ruleResult");
						if (inputAttrDataList.stream().filter(data -> data.getAttrNameCde() == baseRuleAttrNameCde
								&& isRuleExist(jArray, data.getAttrValue())).count() > 0) {
							JsonArray rules = jsonObjRule.getJsonArray("rules");
							for (int k = 0; k < rules.size(); k++) {
								JsonObject rule = rules.getJsonObject(k);

								int ruleAttrNameCde = rule.getInt("attrNameCde");
								String ruleClass = rule.getString("ruleClass");

								// // If rule is configured for multi-attribute, then check for matching group
								// name
								// // as well
								if (ruleAttrNameCde == EnumSystemAttributeName.MULTI_ATTRIBUTE.getCde()
										|| ruleAttrNameCde == EnumSystemAttributeName.MULTI_ATTRIBUTE_TABLE.getCde()) {
									String key = String.valueOf(ruleAttrNameCde) + "," + rule.getString("groupName");
									if (!attributeNameCdeAndTxtList.contains(key)
											&& ListUtility.hasValue(attributeNameCdeList)) {
										continue; // iterate to next item in loop
									}
								}

								// If user requested for specific attributes, then only include those rules
								if (ListUtility.hasValue(attributeNameCdeList)) {
									if (!isFrameworkRuleToBeExecuted(rule)
											&& !attributeNameCdeList.contains(ruleAttrNameCde)) {
										continue; // iterate to next item in loop
									}
								}

								if (beanMap.containsKey(ruleClass)) {
									IAttributeExtractRuleAsync ruleToBeExecuted = beanMap.get(ruleClass);
									ruleList.add(ruleToBeExecuted);
								}
							}
						}
					}
				}
			}

		}
		return ruleList;
	}

	/**
	 * If special attributes then add the 'framework rule' to extract custom
	 * attributes
	 */
	private boolean isFrameworkRuleToBeExecuted(JsonObject rule) {
		try {
			int ruleAttrNameCde = rule.getInt("attrNameCde");
			String groupName = rule.getString("groupName");
			return (ruleAttrNameCde == DocwbWebConstants.ATTR_NAME_CDE_SPECIAL
					&& groupName.equals(DocwbWebConstants.ATTR_GRP_NAME_TXT_SPECIAL));
		} catch (Exception e) {
			logger.info("Error occured while checking for isFrameworkRuleToBeExecuted");
			return false;
		}
	}

	private boolean isRuleExist(JsonArray jsonArray, String txt) {
		boolean found = false;
		for (int i = 0; i < jsonArray.size(); i++) {
			if (jsonArray.getString(i).equalsIgnoreCase(txt)) {
				found = true;
				break;
			}
		}
		return found;
	}

	protected boolean extractionComplete() throws Exception {
		logger.info("Extraction Complete");
		return true;
	}

	protected boolean extractionFailed(Exception ex) throws Exception {
		logger.info("Extraction Failed");
		return true;
	}

	protected boolean actionExecutionResult(Exception ex, ActionParamAttrMappingData actionParamAttrMappingData,
			String actionResult) throws Exception {
		if (ex != null) {
			logger.error("Error occurred while executing action for docId=" + actionParamAttrMappingData.getDocId(),
					ex);
		} else {
			logger.info("Action executed successfully for docId=" + actionParamAttrMappingData.getDocId());
		}
		return true;
	}

	/*** Execute Action End ***/

	/*** Close Case Begin ***/
	protected boolean isCloseCaseForDocument(ActionParamAttrMappingData actionParamAttrMappingData) throws Exception {
		return false;
	}

	protected void terminate(SummaryData summaryData) throws Exception {
		logger.info(summaryData.toString());
	}

	protected DocumentData getDocument(long docId, int queueNameCde) throws Exception {
		DocumentData documentData = null;
		List<DocumentData> documentDataList = documentService.getDocumentList(null, null, null, null, docId,
				queueNameCde, "");
		if (ListUtility.hasValue(documentDataList)) {
			documentData = documentDataList.get(0);
		}
		return documentData;
	}
}
