/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.core.template.attribute;

import java.util.ArrayList;
import java.util.Comparator;
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
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.commonutils.PatternUtility;
import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.engine.core.common.AttributeHelper;
import com.infosys.ainauto.docwb.engine.core.common.DocwbEngineCoreConstants;
import com.infosys.ainauto.docwb.engine.core.common.type.EnumExecutionEventType;
import com.infosys.ainauto.docwb.engine.core.common.type.EnumExecutorType;
import com.infosys.ainauto.docwb.engine.core.config.DownloaderQueueMappingConfig;
import com.infosys.ainauto.docwb.engine.core.db.logger.IDbLogger;
import com.infosys.ainauto.docwb.engine.core.model.SummaryData;
import com.infosys.ainauto.docwb.engine.core.rule.IAttributeExtractRuleAsync;
import com.infosys.ainauto.docwb.engine.core.rule.IAttributeExtractRuleListener;
import com.infosys.ainauto.docwb.engine.core.service.DocWbApiClient;
import com.infosys.ainauto.docwb.engine.core.service.client.IMetricsService;
import com.infosys.ainauto.docwb.engine.core.service.client.MetricsService.EnumMetric;
import com.infosys.ainauto.docwb.web.api.IAnnotationService;
import com.infosys.ainauto.docwb.web.api.IAttributeService;
import com.infosys.ainauto.docwb.web.api.IDocumentService;
import com.infosys.ainauto.docwb.web.data.AnnotationData;
import com.infosys.ainauto.docwb.web.data.AttachmentData;
import com.infosys.ainauto.docwb.web.data.AttributeData;
import com.infosys.ainauto.docwb.web.data.DocumentData;
import com.infosys.ainauto.docwb.web.type.EnumEventType;
import com.infosys.ainauto.docwb.web.type.EnumSystemAttributeName;

@Component
public abstract class AttributeExtractorBase {
	private static Logger logger = LoggerFactory.getLogger(AttributeExtractorBase.class);

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

	@Autowired
	private ApplicationContext context;

	private IAttributeService attributeService;
	private IAnnotationService annotationService;
	private IDocumentService documentService;

	private static final String PROP_NAME_EXTRACTOR_THREAD_POOL = "attribute.extractor.thread.pool.count";

	private AtomicInteger successfulCount;
	private AtomicInteger failedCount;
	private AtomicInteger waitingCount;

	private int nThreads;

	private ThreadPoolTaskExecutor taskExecutor;

	private static final String ERROR_PREFIX = "AttributeExtractorBase - Error occurred in method ";

	private static Map<String, IAttributeExtractRuleAsync> beanMap;
	private static final String UNKNOWN_ATTR_VALUE = "Unknown";
	private List<String> defaultRulesConfig;
	private Map<String, List<String>> attrValueToConditionalRulesMapConfig;
	private List<String> noMatchRulesConfig;

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
		attributeService = docWbApiClient.getAttributeService();
		annotationService = docWbApiClient.getAnnotationService();
		beanMap = context.getBeansOfType(IAttributeExtractRuleAsync.class);

		// Read zeroth index item from config file
		defaultRulesConfig = downloaderQueueMappingConfig.getData().getDownloaderQueueMapping().get(0)
				.getAttributeExtractor().getDefaultRules();

		attrValueToConditionalRulesMapConfig = downloaderQueueMappingConfig.getData().getDownloaderQueueMapping().get(0)
				.getAttributeExtractor().getConditionalRules().stream()
				.filter(obj -> obj.getAttrNameCde() == EnumSystemAttributeName.CATEGORY.getCde()).findFirst().get()
				.getAttrValueToRuleMap();

		noMatchRulesConfig = downloaderQueueMappingConfig.getData().getDownloaderQueueMapping().get(0)
				.getAttributeExtractor().getConditionalRules().stream()
				.filter(obj -> obj.getAttrNameCde() == EnumSystemAttributeName.CATEGORY.getCde()).findFirst().get()
				.getNoMatchRules();

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
		AtomicInteger totalDocCount = new AtomicInteger(0);
		AtomicInteger totalCount = new AtomicInteger(0);
		String errorMessage = "";

		long executionId = DocwbEngineCoreConstants.EXECUTION_ID_EMPTY;

		metricsService.startTimer(EnumMetric.WORKFLOW_EXECUTOR_START_ELAPSED_TIME, EnumExecutorType.ATTRIBUTE_EXTRACTOR,
				true);

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
							totalDocCount.addAndGet(list.size());
							documentDataList.addAll(list);
						}
					});
					if (totalDocCount.get() > 0 && ListUtility.hasValue(documentDataList)) {
						documentDataList.sort(Comparator.comparing(DocumentData::getDocId));
						executionId = dbLogger.startExecution(EnumExecutorType.ATTRIBUTE_EXTRACTOR, name, "");
						dbLogger.addEvent(executionId, EnumExecutionEventType.WORK_STARTED,
								totalDocCount.get() + " doc(s)");
						List<Future<?>> futures = new ArrayList<>();
						try {
							List<List<DocumentData>> dataSubList = ListUtility.convertListToPartitions(documentDataList,
									nThreads);
							dataSubList.forEach(list -> {
								Runnable myRunnable = new Runnable() {

									@Override
									public void run() {
										processSortedDocListOfAllQueues(list, totalCount);
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
			SummaryData summaryData = new SummaryData(name + " [Attribute Extractor]", "attribute");
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
			metricsService.startTimer(EnumMetric.WORKFLOW_EXECUTOR_END_ELAPSED_TIME,
					EnumExecutorType.ATTRIBUTE_EXTRACTOR, true);
		}
	}

	private void handleRuleResult(AtomicInteger successfulCount, AtomicInteger failedCount, AtomicInteger waitingCount,
			HashMap<String, Integer> docToRulesMap, DocumentData documentData, Exception exception,
			DocumentData documentDataResult) {
		Integer noOfRulesPendingForDoc = docToRulesMap.get(String.valueOf(documentData.getDocId()));
		docToRulesMap.put(String.valueOf(documentData.getDocId()), --noOfRulesPendingForDoc);
		if (exception != null) {
			failedCount.incrementAndGet();
			waitingCount.decrementAndGet();
			String errorMessage = ERROR_PREFIX + "doExtract. Rule execution logic failed";
			logger.error(errorMessage, exception);
			try {
				extractionFailed((Exception) exception, documentData);
			} catch (Exception ex2) {
				errorMessage = ERROR_PREFIX + "extractionFailed";
				logger.error(errorMessage, ex2);
			}
		} else {
			successfulCount.incrementAndGet();
			waitingCount.decrementAndGet();
			boolean isSaveToDb = false;
			try {
				isSaveToDb = extractionComplete(documentData, documentDataResult);
			} catch (Exception ex) {
				String errorMessage = ERROR_PREFIX + "extractionComplete";
				logger.error(errorMessage, ex);
			}
			try {

				if (isSaveToDb && documentDataResult != null) {
					boolean isCallSave = false;

					if (ListUtility.hasValue(documentDataResult.getAttributes())) {
						isCallSave = true;
					}
					if (!isCallSave) {
						if (ListUtility.hasValue(documentDataResult.getAttachmentDataList())) {
							isCallSave = documentDataResult.getAttachmentDataList().stream()
									.anyMatch(a -> ListUtility.hasValue(a.getAttributes()));
						}
					}
					if (isCallSave) {
						documentDataResult.setDocId(documentData.getDocId());
						// Call service method to save attributes both at document and attachment level
						attributeService.addAttributes(documentDataResult);
						// Call service method to save annotations both at document and attachment level
						annotationService.addAnnotation(documentDataResult);

					}
				}
			} catch (Exception ex) {
				String errorMessage = ERROR_PREFIX + "addAttributes. Service call failed";
				logger.error(errorMessage, ex);
			}
		}
		// Add "Attributes Extracted" event if all rules have been processed
		noOfRulesPendingForDoc = docToRulesMap.get(String.valueOf(documentData.getDocId()));
		if (noOfRulesPendingForDoc <= 0) {
			documentService.insertDocEventType(documentData.getDocId(), EnumEventType.ATTRIBUTES_EXTRACTED);
		}
	}

	private void processSortedDocListOfAllQueues(List<DocumentData> documentDataList, AtomicInteger totalCount) {
		String errorMessage = "";
		HashMap<String, Integer> docToRulesMap = new HashMap<>();
		if (documentDataList != null && documentDataList.size() > 0) {
			for (DocumentData documentData : documentDataList) {
				if (documentData != null) {
					List<IAttributeExtractRuleAsync> attributeExtractionRuleList = new ArrayList<>();
					try {
						attributeExtractionRuleList = extractAttributesUsingRules(documentData);
					} catch (Exception ex) {
						errorMessage = ERROR_PREFIX + "extractAttributesUsingRules";
						logger.error(errorMessage, ex);
					}
					int noOfRulesForDoc = attributeExtractionRuleList.size();
					if (noOfRulesForDoc <= 0) {
						documentService.insertDocEventType(documentData.getDocId(), EnumEventType.ATTRIBUTES_EXTRACTED);
						continue;
					}

					docToRulesMap.put(String.valueOf(documentData.getDocId()), new Integer(noOfRulesForDoc));
					totalCount.addAndGet(noOfRulesForDoc);
					waitingCount.addAndGet(noOfRulesForDoc);
					// Add "Attributes Extracted - Pending" event so that during asynchronous wait,
					// document is not picked next time
					if (noOfRulesForDoc > 0) {
						documentService.insertDocEventType(documentData.getDocId(),
								EnumEventType.ATTRIBUTES_EXTRACTED_PENDING);
					}

					for (IAttributeExtractRuleAsync rule : attributeExtractionRuleList) {
						try {

							rule.doExtractAsync(documentData, new IAttributeExtractRuleListener() {
								@Override
								public void onAttributeExtractionComplete(Exception exception,
										DocumentData documentDataResult) {

									if (exception == null) {
										// Check if output and input point to same object reference
										if (documentData == documentDataResult) {
											exception = new Exception("Rule class should not modify input data");
										}

										AttributeHelper.removeNullAttributes(documentDataResult);
										// Add the result to current documentData to be used by next rule in queue
										{
											if (ListUtility.hasValue(documentDataResult.getAttributes())) {
												if (!ListUtility.hasValue(documentData.getAttributes())) {
													documentData.setAttributes(new ArrayList<AttributeData>());
												}

												documentData.getAttributes().addAll(documentDataResult.getAttributes());
											}
											if (ListUtility.hasValue(documentDataResult.getAnnotations())) {
												if (!ListUtility.hasValue(documentData.getAnnotations())) {
													documentData.setAnnotations(new ArrayList<AnnotationData>());
												}

												documentData.getAnnotations()
														.addAll(documentDataResult.getAnnotations());
											}
											if (ListUtility.hasValue(documentDataResult.getAttachmentDataList())) {
												if (!ListUtility.hasValue(documentData.getAttachmentDataList())) {
													documentData.setAttachmentDataList(new ArrayList<AttachmentData>());
												}
												documentData.getAttachmentDataList()
														.addAll(documentDataResult.getAttachmentDataList());
											}
										}
									}

									// Pass everything to below method as-is
									handleRuleResult(successfulCount, failedCount, waitingCount, docToRulesMap,
											documentData, exception, documentDataResult);
								}

							});
						} catch (Exception ex) {
							errorMessage = ERROR_PREFIX + "doExtractAsync. Rule call failed";
							logger.error(errorMessage, ex);
							handleRuleResult(successfulCount, failedCount, waitingCount, docToRulesMap, documentData,
									ex, null);
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

	protected List<IAttributeExtractRuleAsync> extractAttributesUsingRules(DocumentData documentData) throws Exception {
		List<IAttributeExtractRuleAsync> ruleList = new ArrayList<>();
		String categoryValue = documentData.getAttributes().stream()
				.filter(attr -> attr.getAttrNameCde() == EnumSystemAttributeName.CATEGORY.getCde()).findFirst().get()
				.getAttrValue();

		List<String> rulesFiltered = new ArrayList<>();
		// Add all default rules
		if (ListUtility.hasValue(defaultRulesConfig)) {
			rulesFiltered.addAll(defaultRulesConfig);
		}
		// Add rules corresponding to matching category attribute value configured in
		// json file
		if (hasKeyValue(categoryValue)) {
			rulesFiltered.addAll(attrValueToConditionalRulesMapConfig.get(categoryValue));
		} else if (!categoryValue.equals(UNKNOWN_ATTR_VALUE) && ListUtility.hasValue(noMatchRulesConfig)) {
			rulesFiltered.addAll(noMatchRulesConfig);
		} // Add noMatchRules configured if no match found

		rulesFiltered.forEach(rule -> {
			ruleList.add(beanMap.get(rule));
		});
		return ruleList;
	}

	private boolean hasKeyValue(String key) {
		return attrValueToConditionalRulesMapConfig != null && attrValueToConditionalRulesMapConfig.containsKey(key)
				&& ListUtility.hasValue(attrValueToConditionalRulesMapConfig.get(key));
	}

	protected boolean extractionComplete(DocumentData documentData, DocumentData documentDataResult) throws Exception {
		logger.info("Extraction Complete");
		return true;
	}

	protected boolean extractionFailed(Exception ex, DocumentData documentData) throws Exception {
		logger.info("Extraction Failed");
		return false;
	}

	protected void terminate(SummaryData summaryData) throws Exception {
		logger.info(summaryData.toString());
	}

}
