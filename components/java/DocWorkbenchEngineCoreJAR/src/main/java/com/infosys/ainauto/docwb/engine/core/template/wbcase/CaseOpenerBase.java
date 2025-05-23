/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.core.template.wbcase;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.commonutils.PatternUtility;
import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.engine.core.common.DocwbEngineCoreConstants;
import com.infosys.ainauto.docwb.engine.core.common.type.EnumExecutionEventType;
import com.infosys.ainauto.docwb.engine.core.common.type.EnumExecutorType;
import com.infosys.ainauto.docwb.engine.core.config.DownloaderQueueMappingConfig;
import com.infosys.ainauto.docwb.engine.core.config.DownloaderQueueMappingConfig.DownloaderQueueMappingConfigData.CaseOpenerData;
import com.infosys.ainauto.docwb.engine.core.config.DownloaderQueueMappingConfig.DownloaderQueueMappingConfigData.GenericAttributeEntityRelData;
import com.infosys.ainauto.docwb.engine.core.db.logger.IDbLogger;
import com.infosys.ainauto.docwb.engine.core.model.SummaryData;
import com.infosys.ainauto.docwb.engine.core.service.DocWbApiClient;
import com.infosys.ainauto.docwb.engine.core.service.client.IMetricsService;
import com.infosys.ainauto.docwb.engine.core.service.client.MetricsService.EnumMetric;
import com.infosys.ainauto.docwb.web.api.IActionService;
import com.infosys.ainauto.docwb.web.api.IDocumentService;
import com.infosys.ainauto.docwb.web.data.ActionData;
import com.infosys.ainauto.docwb.web.data.AttributeData;
import com.infosys.ainauto.docwb.web.data.DocActionData;
import com.infosys.ainauto.docwb.web.data.DocumentData;
import com.infosys.ainauto.docwb.web.data.RecommendedActionData;
import com.infosys.ainauto.docwb.web.type.EnumEventType;
import com.infosys.ainauto.docwb.web.type.EnumSystemAttributeName;
import com.infosys.ainauto.docwb.web.type.EnumTaskStatus;
import com.infosys.ainauto.docwb.web.type.EnumTaskType;

@Component
public abstract class CaseOpenerBase {

	private static Logger logger = LoggerFactory.getLogger(CaseOpenerBase.class);

	@Autowired
	private DocWbApiClient docWbApiClient;

	@Autowired
	private IMetricsService metricsService;

	@Autowired
	private IDbLogger dbLogger;

	@Autowired
	private Environment environment;

	@Autowired
	private DownloaderQueueMappingConfig downloaderQueueMappingConfig;

	private IDocumentService documentService;
	private IActionService actionService;

	private AtomicInteger successfulCount;
	private AtomicInteger failedCount;
	private AtomicInteger waitingCount;

	private int nThreads;

	private ThreadPoolTaskExecutor taskExecutor;

	private Map<String, String> attrValueToUserIdMapConfig;
	private int noMatchUserIdConfig;
	private boolean isAutoTriggerAction;

	private static final String PROP_NAME_EXTRACTOR_THREAD_POOL = "case.opener.thread.pool.count";
	private static final String ATTR_VALUE_DELIMITER = ",";
	protected static final int CASE_IS_UNASSIGNED = -2; // Unassigned

	private static final String ERROR_PREFIX = "WbCaseOpener - Error occurred in method ";

	@PostConstruct
	private void init() {
		nThreads = Integer.parseInt(environment.getProperty(PROP_NAME_EXTRACTOR_THREAD_POOL));
		taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setCorePoolSize(nThreads);
		taskExecutor.setMaxPoolSize(nThreads);
		taskExecutor.setThreadNamePrefix(PatternUtility.formatThreadName(getClass().getSimpleName()));
		taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
		taskExecutor.initialize();
		documentService = docWbApiClient.getDocumentService();
		actionService = docWbApiClient.getActionService();

		// Set default value as no-assignment
		noMatchUserIdConfig = CASE_IS_UNASSIGNED;

		CaseOpenerData caseOpenerConfig = downloaderQueueMappingConfig.getData().getDownloaderQueueMapping().get(0)
				.getCaseOpener();

		if (caseOpenerConfig.getUserAssignments() != null) {
			Optional<GenericAttributeEntityRelData> userAssignmentsConfig = caseOpenerConfig.getUserAssignments()
					.stream()
					.filter(x -> x.getAttrNameCde() == EnumSystemAttributeName.CATEGORY.getCde()
							&& x.getEntity().contentEquals(
									DownloaderQueueMappingConfig.DownloaderQueueMappingConfigData.EnumEntity.USER_ID
											.getValue()))
					.findFirst();

			if (userAssignmentsConfig.isPresent()) {
				attrValueToUserIdMapConfig = userAssignmentsConfig.get().getAttrValueToEntityMap();
				if (userAssignmentsConfig.get().getNoMatchEntity() != 0) {
					noMatchUserIdConfig = userAssignmentsConfig.get().getNoMatchEntity();
				}
			}
		}

		isAutoTriggerAction = caseOpenerConfig.getIsAutoTriggerAction();
	}

	@PreDestroy
	private void onDestroy() {
		taskExecutor.shutdown();
	}

	public void execute(String name, Properties properties) {
		// MDC Logging
		MDC.put(DocwbEngineCoreConstants.MDC_TEMPLATE_NAME, name + "-" + StringUtility.generateTransactionId());
		boolean isCallSuccessful = false;
		successfulCount = new AtomicInteger(0);
		failedCount = new AtomicInteger(0);
		waitingCount = new AtomicInteger(0);
		AtomicInteger totalCount = new AtomicInteger(0);
		String errorMessage = "";
		long executionId = DocwbEngineCoreConstants.EXECUTION_ID_EMPTY;

		metricsService.startTimer(EnumMetric.WORKFLOW_EXECUTOR_START_ELAPSED_TIME, EnumExecutorType.CASE_OPENER, true);

		try {
			isCallSuccessful = initialize(properties);
		} catch (Exception ex) {
			errorMessage = ERROR_PREFIX + "initialize";
			logger.error(errorMessage, ex);
		}

		if (isCallSuccessful) {
			List<List<DocumentData>> documentDataListOfList = new ArrayList<>();
			try {
				documentDataListOfList = getDocuments();
			} catch (Exception ex) {
				errorMessage = ERROR_PREFIX + "getDocuments";
				logger.error(errorMessage, ex);
			}
			try {
				if (ListUtility.hasValue(documentDataListOfList)) {
					List<DocumentData> documentDataList = new ArrayList<>();
					documentDataListOfList.forEach(list -> {
						if (ListUtility.hasValue(list)) {
							totalCount.addAndGet(list.size());
							documentDataList.addAll(list);
						}
					});
					if (totalCount.get() > 0 && ListUtility.hasValue(documentDataList)) {
						documentDataList.sort(Comparator.comparing(DocumentData::getDocId));

						executionId = dbLogger.startExecution(EnumExecutorType.CASE_OPENER, name, "");
						dbLogger.addEvent(executionId, EnumExecutionEventType.WORK_STARTED,
								totalCount.get() + " doc(s)");
						List<Future<?>> futures = new ArrayList<>();
						try {
							List<List<DocumentData>> dataSubList = ListUtility.convertListToPartitions(documentDataList,
									nThreads);
							dataSubList.forEach(list -> {
								Runnable myRunnable = new Runnable() {

									@Override
									public void run() {
										processSortedDocListOfAllQueues(list);
									}

								};
								//
								futures.add(taskExecutor.submit(myRunnable));
							});

						} catch (Exception e) {
							logger.error(e.toString());
						} finally {
							for (Future<?> future : futures) {
								future.get();
							}
						}
					}
				}
			} catch (Exception e) {
				logger.error("Error occurred while creation/execution of threads", e);
			}
		}
		String dbExecutionSummary = "";
		try {
			SummaryData summaryData = new SummaryData(name + " [Case Opener]", "document");
			summaryData.setTotalCount(totalCount.get());
			summaryData.setWaitingCount(waitingCount.get());
			summaryData.setFailedCount(failedCount.get());
			summaryData.setSuccessfulCount(successfulCount.get());
			if (totalCount.get() > 0) {
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
			metricsService.startTimer(EnumMetric.WORKFLOW_EXECUTOR_END_ELAPSED_TIME, EnumExecutorType.CASE_OPENER,
					true);
		}

	}

	/**
	 * @param totalCount
	 * @param executionId
	 * @param documentDataList
	 * @return
	 */
	private void processSortedDocListOfAllQueues(List<DocumentData> documentDataList) {
		String errorMessage;
		Exception exceptionFromServiceCall = null;
		String serviceCallMethod = "";
		if (documentDataList != null && documentDataList.size() > 0) {
			for (DocumentData documentData : documentDataList) {
				if (documentData != null) {
					boolean isOpenCaseForDocument = true;
					try {
						isOpenCaseForDocument = isOpenCaseForDocument(documentData);
					} catch (Exception ex) {
						errorMessage = ERROR_PREFIX + "isOpenCaseForDocument";
						logger.error(errorMessage, ex);
					}
					if (isOpenCaseForDocument) {
						exceptionFromServiceCall = null;
						serviceCallMethod = "";
						try {
							serviceCallMethod = "addDocEventType";
							documentService.insertDocEventType(documentData.getDocId(), EnumEventType.CASE_OPENED);
							successfulCount.incrementAndGet();
						} catch (Exception ex) {
							failedCount.incrementAndGet();
							exceptionFromServiceCall = ex;
							errorMessage = ERROR_PREFIX + serviceCallMethod + " (Service Call)";
							logger.error(errorMessage, ex);
						}
						try {
							openCaseForDocumentResult(exceptionFromServiceCall, documentData);
						} catch (Exception ex) {
							errorMessage = ERROR_PREFIX + "openCaseForDocumentResult";
							logger.error(errorMessage, ex);
						}

						long userId = CASE_IS_UNASSIGNED;
						try {
							// Get assign-case-to userId details from child class
							userId = assignCaseTo(documentData);
						} catch (Exception ex) {
							errorMessage = ERROR_PREFIX + "assignCaseTo";
							logger.error(errorMessage, ex);
						}

						// Call only if valid userId provided by child class
						if (userId != CASE_IS_UNASSIGNED) {
							exceptionFromServiceCall = null;
							try {
								serviceCallMethod = "assignCase";
								documentService.assignCase(documentData.getDocId(), userId);
							} catch (Exception ex) {
								exceptionFromServiceCall = ex;
								errorMessage = ERROR_PREFIX + serviceCallMethod + " (Service Call)";
								logger.error(errorMessage, ex);
							}

							// Provide result to child class
							try {
								assignCaseToResult(exceptionFromServiceCall, documentData);
							} catch (Exception ex) {
								errorMessage = ERROR_PREFIX + "assignCaseToResult";
								logger.error(errorMessage, ex);
							}
						}

						List<ActionData> actionDataList = null;
						try {
							actionDataList = addActions(documentData);
						} catch (Exception ex) {
							errorMessage = ERROR_PREFIX + "addActions";
							logger.error(errorMessage, ex);
						}
						boolean isAtleastOneActionAdded = false;
						if (actionDataList != null && actionDataList.size() > 0) {
							try {
								serviceCallMethod = "addDocEventType";
								documentService.insertDocEventType(documentData.getDocId(),
										EnumEventType.CASE_ASSIGNED);
							} catch (Exception ex) {
								errorMessage = ERROR_PREFIX + serviceCallMethod + " (Service Call)";
								logger.error(errorMessage, ex);
							}

							for (ActionData actionData : actionDataList) {
								if (actionData != null) {
									exceptionFromServiceCall = null;
									serviceCallMethod = "";
									try {
										DocActionData docActionData = new DocActionData();
										docActionData.setDocId(documentData.getDocId());
										docActionData.setTaskStatus(EnumTaskStatus.YET_TO_START);
										docActionData.setTaskType(EnumTaskType.SYSTEM);

										List<ActionData> actionDataToAddList = new ArrayList<>();
										actionDataToAddList.add(actionData);
										docActionData.setActionDataList(actionDataToAddList);

										serviceCallMethod = "addAction";
										actionService.addAction(docActionData);
										isAtleastOneActionAdded = true;

									} catch (Exception ex) {
										exceptionFromServiceCall = ex;
										errorMessage = ERROR_PREFIX + serviceCallMethod + " (Service Call)";
										logger.error(errorMessage, ex);
									}
									try {
										addActionResult(exceptionFromServiceCall, documentData, actionData);
									} catch (Exception ex) {
										errorMessage = ERROR_PREFIX + "addActionResult";
										logger.error(errorMessage, ex);
									}
								}
							}
						}

						// Finally, set the case status as YTS (Manual Flow) or IP (Automated flow)
						// Default status for every new document is YTS
						EnumTaskStatus taskStatusForNewDocument = EnumTaskStatus.YET_TO_START;
						if (isAtleastOneActionAdded) {
							taskStatusForNewDocument = EnumTaskStatus.IN_PROGRESS;
						}
						try {
							serviceCallMethod = "updateDocTaskStatus";
							documentService.updateDocTaskStatus(documentData.getDocId(), taskStatusForNewDocument);
						} catch (Exception ex) {
							exceptionFromServiceCall = ex;
							errorMessage = ERROR_PREFIX + serviceCallMethod + " (Service Call)";
							logger.error(errorMessage, ex);
						}
					}
				}
			}
		} else {
			try {
				noDocumentsFound();
			} catch (Exception ex) {
				errorMessage = ERROR_PREFIX + "noDocuments";
				logger.error(errorMessage, ex);
			}
		}
	}

	protected boolean initialize(Properties properties) throws Exception {
		return true;
	}

	protected abstract List<List<DocumentData>> getDocuments() throws Exception;

	protected void noDocumentsFound() throws Exception {
		logger.info("No documents found for processing");
	}

	protected boolean isOpenCaseForDocument(DocumentData documentData) throws Exception {
		// Yes, add event for all documents
		return true;
	}

	protected void openCaseForDocumentResult(Exception ex, DocumentData documentData) throws Exception {
		if (ex != null) {
			logger.error("Error occurred while opening case for docId=" + documentData.getDocId(), ex);
		} else {
			logger.info("Case opened successfully for docId=" + documentData.getDocId());
		}
	}

	protected long assignCaseTo(DocumentData documentData) throws Exception {
		int assignToUserId = noMatchUserIdConfig; // Set as default
		if (attrValueToUserIdMapConfig != null && !attrValueToUserIdMapConfig.isEmpty()) {
			String categoryAttrValue = documentData.getAttributes().stream()
					.filter(attr -> attr.getAttrNameCde() == EnumSystemAttributeName.CATEGORY.getCde()).findFirst()
					.orElse(new AttributeData()).getAttrValue();
			if (StringUtility.hasTrimmedValue(categoryAttrValue)) {
				categoryAttrValue = categoryAttrValue.trim();
				if (attrValueToUserIdMapConfig.containsKey(categoryAttrValue)) {
					assignToUserId = Integer.valueOf(attrValueToUserIdMapConfig.get(categoryAttrValue));
				}
			}
		}

		return assignToUserId;
	}

	protected void assignCaseToResult(Exception ex, DocumentData documentData) throws Exception {
		if (ex != null) {
			logger.error("Error occurred while assigning case for docId=" + documentData.getDocId(), ex);
		} else {
			logger.info("Case assigned successfully for docId=" + documentData.getDocId());
		}
	}

	protected List<ActionData> addActions(DocumentData documentData) throws Exception {
		List<ActionData> actionDataList = new ArrayList<>();
		if (isAutoTriggerAction) {
			if (ListUtility.hasValue(documentData.getAttributes())) {
				Optional<AttributeData> optionalObject = documentData.getAttributes().stream()
						.filter(a -> a.getAttrNameCde() == EnumSystemAttributeName.CATEGORY.getCde()).findFirst();
				if (optionalObject.isPresent()) {
					AttributeData attributeData = optionalObject.get();
					boolean isAttrValNull = false;
					if ((attributeData.getAttrValue() != null) && (attributeData.getConfidencePct() > 0)) {
						RecommendedActionData recommendedActionData = actionService
								.getRecommendation(documentData.getDocId());
						if (recommendedActionData != null && recommendedActionData.getActionNameCde() > 0
								&& attributeData.getConfidencePct() >= recommendedActionData.getConfidencePct()) {
							logger.info("Found action to be added automatically on document");
							ActionData actionData = actionService
									.getActionData(recommendedActionData.getActionNameCde(), documentData.getDocId());
							if (actionData != null) {
								if (actionData.getMappingList().size() > 0) {
									for (int i = 0; i < actionData.getMappingList().size(); i++) {
										if (actionData.getMappingList().get(i).getAttrValues() == null) {
											isAttrValNull = true;
											break;
										} else {
											HashSet<String> attrValues = new HashSet<>();
											String paramValue = "";
											attrValues.addAll(actionData.getMappingList().get(i).getAttrValues());
											for (String attrValue : attrValues) {
												paramValue += attrValue + ATTR_VALUE_DELIMITER;
											}
											actionData.getMappingList().get(i)
													.setParamValue(paramValue.substring(0, paramValue.length() - 1));
										}
									}
									if (!isAttrValNull) {
										actionDataList.add(actionData);
									}
								} else {
									actionDataList.add(actionData);
								}
							}
						}
					}
				}
			}
		}

		return actionDataList;
	}

	protected void addActionResult(Exception ex, DocumentData documentData, ActionData actionData) throws Exception {
		if (ex != null) {
			logger.error("Error occurred while adding action for docId=" + documentData.getDocId(), ex);
		} else {
			logger.info("Action added successfully for docId=" + documentData.getDocId());
		}
	}

	protected void terminate(SummaryData summaryData) throws Exception {
		logger.info(summaryData.toString());
	};
}
