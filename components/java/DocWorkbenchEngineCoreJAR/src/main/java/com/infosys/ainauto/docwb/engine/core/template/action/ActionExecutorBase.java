/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.core.template.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.infosys.ainauto.docwb.engine.core.db.logger.IDbLogger;
import com.infosys.ainauto.docwb.engine.core.exception.DocwbEngineException;
import com.infosys.ainauto.docwb.engine.core.model.SummaryData;
import com.infosys.ainauto.docwb.engine.core.service.DocWbApiClient;
import com.infosys.ainauto.docwb.engine.core.service.client.IMetricsService;
import com.infosys.ainauto.docwb.engine.core.service.client.MetricsService.EnumMetric;
import com.infosys.ainauto.docwb.web.api.IActionService;
import com.infosys.ainauto.docwb.web.api.IDocumentService;
import com.infosys.ainauto.docwb.web.api.IOutboundEmailService;
import com.infosys.ainauto.docwb.web.api.ITemplateService;
import com.infosys.ainauto.docwb.web.data.ActionParamAttrMappingData;
import com.infosys.ainauto.docwb.web.data.ActionTempMappingData;
import com.infosys.ainauto.docwb.web.data.DocActionData;
import com.infosys.ainauto.docwb.web.data.EmailData;
import com.infosys.ainauto.docwb.web.type.EnumEventOperator;
import com.infosys.ainauto.docwb.web.type.EnumEventType;
import com.infosys.ainauto.docwb.web.type.EnumTaskStatus;
import com.infosys.ainauto.docwb.web.type.EnumTaskType;

@Component
public abstract class ActionExecutorBase {

	@Autowired
	private DocWbApiClient docWbApiClient;

	@Autowired
	private IMetricsService metricsService;

	@Autowired
	private IDbLogger dbLogger;

	@Autowired
	private Environment environment;

	private IDocumentService documentService;
	private IActionService actionService;
	private ITemplateService templateService;
	private IOutboundEmailService outboundEmailService;

	private static final String PROP_NAME_EXECUTOR_THREAD_POOL = "action.executor.thread.pool.count";

	private AtomicInteger successfulCount;
	private AtomicInteger failedCount;
	private AtomicInteger waitingCount;

	private int nThreads;

	private ThreadPoolTaskExecutor taskExecutor;

	private static Logger logger = LoggerFactory.getLogger(ActionExecutorBase.class);

	private static final String ERROR_PREFIX = "ActionExecutor - Error occurred in method ";

	@PostConstruct
	private void init() {
		nThreads = Integer.parseInt(environment.getProperty(PROP_NAME_EXECUTOR_THREAD_POOL));
		taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setCorePoolSize(nThreads);
		taskExecutor.setMaxPoolSize(nThreads);
		taskExecutor.setThreadNamePrefix(PatternUtility.formatThreadName(getClass().getSimpleName()));
		taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
		taskExecutor.initialize();
		documentService = docWbApiClient.getDocumentService();
		actionService = docWbApiClient.getActionService();
		templateService = docWbApiClient.getTemplateService();
		outboundEmailService = docWbApiClient.getOutboundEmailService();
	}

	@PreDestroy
	private void onDestroy() {
		taskExecutor.shutdown();
	}

	public void execute(String name, Properties properties) {
		// MDC Logging
		MDC.put(DocwbEngineCoreConstants.MDC_TEMPLATE_NAME, name + "-" + StringUtility.generateTransactionId());
		boolean isCallSuccessful = false;
		successfulCount = new AtomicInteger();
		failedCount = new AtomicInteger();
		waitingCount = new AtomicInteger();
		// Exception exceptionFromServiceCall =null;
		// String serviceCallMethod = "";
		AtomicInteger totalCount = new AtomicInteger(0);
		String errorMessage = "";

		long executionId = DocwbEngineCoreConstants.EXECUTION_ID_EMPTY;

		metricsService.startTimer(EnumMetric.WORKFLOW_EXECUTOR_START_ELAPSED_TIME, EnumExecutorType.ACTION_EXECUTOR,
				true);

		try {
			isCallSuccessful = initialize(properties);
		} catch (Exception ex) {
			errorMessage = ERROR_PREFIX + "initialize";
			logger.error(errorMessage, ex);
		}

		if (isCallSuccessful) {
			List<List<DocActionData>> docActionDataListOfList = new ArrayList<>();
			try {
				docActionDataListOfList = getActions();
			} catch (Exception ex) {
				errorMessage = ERROR_PREFIX + "getActions";
				logger.error(errorMessage, ex);
			}
			try {
				if (ListUtility.hasValue(docActionDataListOfList)) {
					List<DocActionData> docActionDataList = new ArrayList<DocActionData>();
					docActionDataListOfList.forEach(list -> {
						if (ListUtility.hasValue(list)) {
							totalCount.addAndGet(list.size());
							docActionDataList.addAll(list);
						}
					});
					if (totalCount.get() > 0 && ListUtility.hasValue(docActionDataList)) {
						executionId = dbLogger.startExecution(EnumExecutorType.ACTION_EXECUTOR, name, "");
						dbLogger.addEvent(executionId, EnumExecutionEventType.WORK_STARTED,
								totalCount.get() + " doc(s)");
						waitingCount.set(totalCount.get());
						List<Future<?>> futures = new ArrayList<>();
						try {
							List<DocActionData> docActionDataSortedList = getSortedActionList(docActionDataList);
							List<List<DocActionData>> dataSubList = ListUtility
									.convertListToPartitions(docActionDataSortedList, nThreads);
							dataSubList.forEach(list -> {
								Runnable myRunnable = new Runnable() {
									@Override
									public void run() {
										processActions(list);
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
			// Thread.sleep(5000);
			SummaryData summaryData = new SummaryData(name + " [Action Executor]", "action");
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
			metricsService.startTimer(EnumMetric.WORKFLOW_EXECUTOR_END_ELAPSED_TIME, EnumExecutorType.ACTION_EXECUTOR,
					true);
		}

	}

	private List<DocActionData> getSortedActionList(List<DocActionData> actionDatas) {
		List<DocActionData> datas = new ArrayList<>();
		Map<Long, DocActionData> actionDataMap = new HashMap<>();
		List<Long> docActionRelIdList = new ArrayList<>();
		actionDatas.stream().forEach(action -> {
			if (ListUtility.hasValue(action.getActionParamAttrMappingDataList())) {
				long docActionRelId = action.getActionParamAttrMappingDataList().get(0).getDocActionRelId();
				actionDataMap.put(docActionRelId, action);
				docActionRelIdList.add(docActionRelId);
			}
		});
		Collections.sort(docActionRelIdList);
		for (long relId : docActionRelIdList) {
			datas.add(actionDataMap.get(relId));
		}
		return datas;

	}

	/**
	 * @param successfulCount
	 * @param failedCount
	 * @param waitingCount
	 * @param totalCount
	 * @param executionId
	 * @param docActionDataList
	 * @return
	 */
	private void processActions(List<DocActionData> docActionDataList) {
		String errorMessage;
		if (docActionDataList != null && docActionDataList.size() > 0) {
			for (DocActionData docActionData : docActionDataList) {
				long docId = docActionData.getDocId();
				documentService.updateDocTaskStatus(docId, EnumTaskStatus.IN_PROGRESS);
				List<ActionParamAttrMappingData> actionParamAttrMappingDataList = docActionData
						.getActionParamAttrMappingDataList();

				for (ActionParamAttrMappingData actionParamAttrMappingData : actionParamAttrMappingDataList) {
					int actionNameCde = actionParamAttrMappingData.getActionNameCde();
					try {
						boolean isUpdateActionStatus = isPublishActionStatusInProgress(actionParamAttrMappingData);
						if (isUpdateActionStatus) {
							// Update action as in-progress in Workbench
							actionService.updateAction(actionParamAttrMappingData, EnumTaskStatus.IN_PROGRESS);
						}

						executeAction(actionParamAttrMappingData, new IActionExecutorListener() {
							@Override
							public void onActionExecutionComplete(Exception exception, String actionResult) {
								String errorMessage = "";
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
													EnumTaskStatus.COMPLETE);
											boolean isAutoSendEmail = isAutoSendEmail(actionParamAttrMappingData);
											Exception exceptionFromServiceCall = null;
											EmailData emailData = null;
											// If task was added by system then auto compose and save email
											if (isAutoSendEmail && actionParamAttrMappingData
													.getTaskType() == EnumTaskType.SYSTEM) {
												String serviceCallMethod = "";
												try {
													serviceCallMethod = "getFlattenedTemplates";
													emailData = new EmailData();
													List<ActionTempMappingData> actionTemplateDataList = templateService
															.getFlattenedTemplates(docId);
													boolean isEmailDataAvailable = false;
													for (ActionTempMappingData actionTempMappingData : actionTemplateDataList) {
														if (actionTempMappingData.getIsRecommendedTemplate()) {
															emailData.setEmailBodyText(
																	actionTempMappingData.getTemplateText());
															emailData.setEmailBodyHtml(
																	actionTempMappingData.getTemplateHtml());
															isEmailDataAvailable = true;
														}
													}

													if (!isEmailDataAvailable) {
														throw new DocwbEngineException(
																"Recommended template not found.");
													}
													serviceCallMethod = "getDraftEmailDataList";
													EmailData draftEmailData = outboundEmailService
															.getEmailDraft(docId);
													emailData.setDocId(docId);
													emailData.setEmailAddressToList(
															draftEmailData.getEmailAddressToList());
													emailData.setEmailAddressCcList(
															draftEmailData.getEmailAddressCcList());
													emailData.setEmailAddressBccList(
															draftEmailData.getEmailAddressBccList());
													emailData.setEmailSubject(draftEmailData.getEmailSubject());

													serviceCallMethod = "addOutboundEmail";
													boolean isSuccess = outboundEmailService
															.addOutboundEmail(emailData);
													if (!isSuccess) {
														throw new DocwbEngineException(
																"Outbound email has more attachments");
													}
												} catch (Exception ex) {
													exceptionFromServiceCall = ex;
													errorMessage = ERROR_PREFIX + serviceCallMethod + " (Service Call)";
													logger.error(errorMessage, ex);
												}
											}

											try {
												autoSendEmailResult(exceptionFromServiceCall,
														actionParamAttrMappingData, emailData);
											} catch (Exception ex) {
												errorMessage = ERROR_PREFIX + "autoSendEmailResult";
												logger.error(errorMessage, ex);
											}

											try {
												documentService.insertDocEventType(docId,
														EnumEventType.ACTION_COMPLETED);
												// If no more actions pending, then change case status
												// TODO  Change this call to get action list for a particular
												// doc only
												List<DocActionData> actionList = actionService.getActionListForDoc(
														actionNameCde, EnumTaskStatus.COMPLETE, 0,
														EnumEventOperator.LESS_THAN, docId);
												EnumTaskStatus docTaskStatus = getActionCompleteTaskStatus(actionList);
												if (null != docTaskStatus) {
													documentService.updateDocTaskStatus(docId, docTaskStatus);
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
													documentService.updateDocTaskStatus(docId, EnumTaskStatus.COMPLETE);
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
										logger.error(errorMessage, exception);
										try {
											boolean isUpdateTask = actionExecutionResult((Exception) exception,
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
									logger.error(errorMessage, exception);
									try {
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
						});

					} catch (Exception ex) {
						failedCount.incrementAndGet();
						waitingCount.decrementAndGet();
						errorMessage = ERROR_PREFIX + "executeAction. Service call failed";
						logger.error(errorMessage, ex);
						try {
							boolean isUpdateTask = actionExecutionResult(ex, actionParamAttrMappingData, "");
							if (isUpdateTask) {
								actionService.updateAction(actionParamAttrMappingData, EnumTaskStatus.FAILED);
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

	protected boolean initialize(Properties properties) throws Exception {
		return true;
	}

	protected abstract List<List<DocActionData>> getActions() throws Exception;

	protected void noActions() throws Exception {
		logger.info("No Data downloaded");
	}

	protected boolean isPublishActionStatusInProgress(ActionParamAttrMappingData actionParamAttrMappingData)
			throws Exception {
		return true;
	}

	/*** Execute Action Begin ***/
	protected abstract void executeAction(ActionParamAttrMappingData actionParamAttrMappingData,
			IActionExecutorListener actionExecutorListener) throws Exception;

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

	/*** Send Email Begin ***/
	protected boolean isAutoSendEmail(ActionParamAttrMappingData actionParamAttrMappingData) throws Exception {
		// Don't auto send email
		return false;
	}

	protected void autoSendEmailResult(Exception ex, ActionParamAttrMappingData actionParamAttrMappingData,
			EmailData emailData) throws Exception {
		if (ex != null) {
			logger.error("Error occurred while auto sending email for docId=" + actionParamAttrMappingData.getDocId(),
					ex);
		} else {
			if (emailData != null) {
				logger.info("Auto sending of email successful for docId=" + actionParamAttrMappingData.getDocId());
			} else {
				logger.info("No email was sent automatically for docId=" + actionParamAttrMappingData.getDocId());
			}
		}
	}

	/*** Send Email End ***/
	
	/*** Close Case Begin ***/
	protected boolean isCloseCaseForDocument(ActionParamAttrMappingData actionParamAttrMappingData) throws Exception {
		return false;
	}

	/*** Close Case End ***/
	
	protected EnumTaskStatus getActionCompleteTaskStatus(List<DocActionData> actionList)  throws Exception {
		if (actionList.size() <= 0) {
			return EnumTaskStatus.FOR_YOUR_REVIEW;
		}
		return null;
	}


	protected void terminate(SummaryData summaryData) throws Exception {
		logger.info(summaryData.toString());
	}

}
