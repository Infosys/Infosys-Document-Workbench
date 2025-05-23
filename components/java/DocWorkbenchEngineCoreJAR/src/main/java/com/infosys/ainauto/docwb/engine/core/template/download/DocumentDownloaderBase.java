/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.core.template.download;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
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
import com.infosys.ainauto.docwb.engine.core.config.DownloaderQueueMappingConfig.DownloaderQueueMappingConfigData.DocumentDownloaderData;
import com.infosys.ainauto.docwb.engine.core.config.DownloaderQueueMappingConfig.DownloaderQueueMappingConfigData.GenericAttributeEntityRelData;
import com.infosys.ainauto.docwb.engine.core.db.logger.IDbLogger;
import com.infosys.ainauto.docwb.engine.core.model.SummaryData;
import com.infosys.ainauto.docwb.engine.core.rule.IAttributeExtractRuleAsync;
import com.infosys.ainauto.docwb.engine.core.rule.IAttributeExtractRuleListener;
import com.infosys.ainauto.docwb.engine.core.service.DocWbApiClient;
import com.infosys.ainauto.docwb.engine.core.service.client.IMetricsService;
import com.infosys.ainauto.docwb.engine.core.service.client.MetricsService.EnumMetric;
import com.infosys.ainauto.docwb.web.api.IAttachmentService;
import com.infosys.ainauto.docwb.web.api.IDocumentService;
import com.infosys.ainauto.docwb.web.data.AttachmentData;
import com.infosys.ainauto.docwb.web.data.AttributeData;
import com.infosys.ainauto.docwb.web.data.DocumentData;
import com.infosys.ainauto.docwb.web.type.EnumEventType;
import com.infosys.ainauto.docwb.web.type.EnumSystemAttributeName;

@Component
public abstract class DocumentDownloaderBase<T> {

	private static Logger logger = LoggerFactory.getLogger(DocumentDownloaderBase.class);

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

	private IDocumentService documentService;
	private IAttachmentService attachmentService;
	private static final String PROP_NAME_DOWNLOADER_THREAD_POOL = "document.downloader.thread.pool.count";
	private static final String ERROR_PREFIX = "DocumentDownloader - Error occurred in method ";

	private AtomicInteger atomicSuccessfulCountInt;
	private AtomicInteger atomicFailedCountInt;

	private int nThreads;

	private ThreadPoolTaskExecutor taskExecutor;

	private static Map<String, IAttributeExtractRuleAsync> beanMap;
	private List<String> defaultRulesConfig;
	private Map<String, String> attrValueToQueueNameCdeMapConfig;
	private int noMatchQueueNameCdeConfig;

	@PostConstruct
	private void init() {
		nThreads = Integer.parseInt(environment.getProperty(PROP_NAME_DOWNLOADER_THREAD_POOL));
		taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setCorePoolSize(nThreads);
		taskExecutor.setMaxPoolSize(nThreads);
		taskExecutor.setThreadNamePrefix(PatternUtility.formatThreadName(getClass().getSimpleName()));
		taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
		taskExecutor.initialize();
		documentService = docWbApiClient.getDocumentService();
		attachmentService = docWbApiClient.getAttachmentService();
		beanMap = context.getBeansOfType(IAttributeExtractRuleAsync.class);

		{ // Read config file
			// Read zeroth index item
			DocumentDownloaderData documentDownloaderConfig = downloaderQueueMappingConfig.getData()
					.getDownloaderQueueMapping().get(0).getDocumentDownloader();
			defaultRulesConfig = documentDownloaderConfig.getDefaultRules();

			if (documentDownloaderConfig.getQueueAssignments() != null) {
				Optional<GenericAttributeEntityRelData> queueAssignmentsConfig = documentDownloaderConfig
						.getQueueAssignments().stream()
						.filter(x -> x.getAttrNameCde() == EnumSystemAttributeName.CATEGORY.getCde()
								&& x.getEntity().contentEquals(
										DownloaderQueueMappingConfig.DownloaderQueueMappingConfigData.EnumEntity.QUEUE_NAME_CDE
												.getValue()))
						.findFirst();
				if (queueAssignmentsConfig.isPresent()) {
					attrValueToQueueNameCdeMapConfig = queueAssignmentsConfig.get().getAttrValueToEntityMap();
					noMatchQueueNameCdeConfig = queueAssignmentsConfig.get().getNoMatchEntity();
				}
			}
		}
	}

	@PreDestroy
	private void onDestroy() {
		taskExecutor.shutdown();
	}

	public void execute(String name, Properties properties) {
		// MDC Logging
		MDC.put(DocwbEngineCoreConstants.MDC_TEMPLATE_NAME, name + "-" + StringUtility.generateTransactionId());
		boolean isCallSuccessful = false;
		int totalCount = 0;
		atomicSuccessfulCountInt = new AtomicInteger(0);
		atomicFailedCountInt = new AtomicInteger(0);
		String errorMessage = "";

		long executionId = DocwbEngineCoreConstants.EXECUTION_ID_EMPTY;

		metricsService.startTimer(EnumMetric.WORKFLOW_EXECUTOR_START_ELAPSED_TIME, EnumExecutorType.DOCUMENT_DOWNLOADER,
				true);

		try {
			isCallSuccessful = initialize(properties);
		} catch (Exception ex) {
			errorMessage = ERROR_PREFIX + "initialize";
			logger.error(errorMessage, ex);
		}

		if (isCallSuccessful) {
			List<T> externalDataList = null;
			try {
				externalDataList = downloadData();
			} catch (Exception ex) {
				errorMessage = ERROR_PREFIX + "downloadData";
				logger.error(errorMessage, ex);
			}
			if (externalDataList != null && externalDataList.size() > 0) {
				totalCount = externalDataList.size();
				executionId = dbLogger.startExecution(EnumExecutorType.DOCUMENT_DOWNLOADER, name, "");
				dbLogger.addEvent(executionId, EnumExecutionEventType.WORK_STARTED, totalCount + " doc(s)");
				List<Future<?>> futures = new ArrayList<>();
				try {
					List<List<T>> dataSubList = ListUtility.convertListToPartitions(externalDataList, nThreads);
					for (List<T> subList : dataSubList) {
						//
						Runnable myRunnable = new Runnable() {
							@Override
							public void run() {
								process(subList);
							}
						};
						//
						futures.add(taskExecutor.submit(myRunnable));
					}

				} catch (Exception e) {
					logger.error(e.toString());
				} finally {
					for (Future<?> future : futures) {
						try {
							future.get();
						} catch (Exception e) {
							logger.error("Error occurred while execution of threads", e);
						}
					}
				}
			} else {
				try {
					noDataDownloaded();
				} catch (Exception ex) {
					errorMessage = ERROR_PREFIX + "noDataDownloaded";
					logger.error(errorMessage, ex);
				}
			}
		}
		String dbExecutionSummary = "";
		try {
			SummaryData summaryData = new SummaryData(name + " [Document Downloader]", "document");
			logger.debug("Successful:" + atomicSuccessfulCountInt.get() + "--------" + "Failed:"
					+ atomicFailedCountInt.get());
			summaryData.setTotalCount(totalCount);
			summaryData.setFailedCount(atomicFailedCountInt.get());
			summaryData.setSuccessfulCount(atomicSuccessfulCountInt.get());
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
					EnumExecutorType.DOCUMENT_DOWNLOADER, true);
		}

	}

	/**
	 * Put initialization logic here. Return <b>true</b> to continue processing.
	 * 
	 * @param properties
	 * @return
	 * @throws Exception
	 */
	protected boolean initialize(Properties properties) throws Exception {
		return true;
	}

	/**
	 * Put logic to download from external source. Return a data list to continue
	 * processing.
	 * 
	 * @return
	 * @throws Exception
	 */
	protected abstract List<T> downloadData() throws Exception;

	/**
	 * This method is called if there is no data to be processed.
	 * 
	 * @throws Exception
	 */
	protected void noDataDownloaded() throws Exception {
		logger.info("No Data downloaded");
	}

	/**
	 * Put logic to convert external data into <b>Document</b> data type.
	 * 
	 * @param externalData
	 * @return
	 * @throws Exception
	 */
	protected abstract DocumentData createDocument(T externalData) throws Exception;

	/**
	 * Return list of rules for extracting attributes from external data
	 * 
	 * @return
	 * @throws Exception
	 */
	protected List<IAttributeExtractRuleAsync> extractAttributesUsingRules() throws Exception {
		List<IAttributeExtractRuleAsync> ruleList = new ArrayList<>();

		List<String> ruleNameList = new ArrayList<>();
		// Add all default rules
		if (ListUtility.hasValue(defaultRulesConfig)) {
			ruleNameList.addAll(defaultRulesConfig);
		}
		ruleNameList.forEach(rule -> {
			ruleList.add(beanMap.get(rule));
		});
		return ruleList;
	}

	/**
	 * This method is called to provide the result of
	 * <b>extractAttributesUsingRules</b> method
	 * 
	 * @param exceptionList
	 * @param attributeExtractRuleList
	 * @return
	 * @throws Exception
	 */
	protected void extractAttributesUsingRulesResult(List<Exception> exceptionList,
			List<IAttributeExtractRuleAsync> attributeExtractRuleList) throws Exception {
		for (int i = 0; i < attributeExtractRuleList.size(); i++) {
			logger.error("Error occurred in rule {}. Error is {} ", attributeExtractRuleList.get(i),
					exceptionList.get(i));
		}

	}

	/**
	 * Put logic to make any modifications to the <b>DocumentData</b> object and
	 * return final version for upload
	 * 
	 * @param documentData
	 * @return
	 * @throws Exception
	 */
	protected DocumentData uploadDocument(DocumentData documentData, T externalData) throws Exception {
		int queueNameCde = noMatchQueueNameCdeConfig; // Set as default
		if (attrValueToQueueNameCdeMapConfig != null && !attrValueToQueueNameCdeMapConfig.isEmpty()) {
			String categoryAttrValue = documentData.getAttributes().stream()
					.filter(attr -> attr.getAttrNameCde() == EnumSystemAttributeName.CATEGORY.getCde()).findFirst()
					.orElse(new AttributeData()).getAttrValue();
			if (StringUtility.hasTrimmedValue(categoryAttrValue)) {
				categoryAttrValue = categoryAttrValue.trim();
				if (attrValueToQueueNameCdeMapConfig.containsKey(categoryAttrValue)) {
					queueNameCde = Integer.valueOf(attrValueToQueueNameCdeMapConfig.get(categoryAttrValue));
				}
			}
		}
		documentData.setQueueNameCde(queueNameCde);
		return documentData;
	}

	protected void uploadDocumentResult(Exception ex, T externalData, DocumentData documentData) throws Exception {
		if (ex != null) {
			logger.error("Error occurred in upload document", ex);
		} else {
			logger.info("Document uploaded with docId=" + documentData.getDocId());
		}
	}

	/**
	 * This method is called at the end of processing of all items returned from
	 * <b>downloadData</b>
	 * 
	 * @param summaryData
	 * @throws Exception
	 */
	protected void terminate(SummaryData summaryData) throws Exception {
		logger.info(summaryData.toString());
	}

	public void process(List<T> externalDataList) {
		String errorMessage = "";
		String serviceCallMethod = "";
		for (T externalData : externalDataList) {
			DocumentData documentData = null;
			try {
				documentData = createDocument(externalData);
			} catch (Exception ex) {
				errorMessage = ERROR_PREFIX + "createDocument";
				logger.error(errorMessage, ex);
			}
			if (documentData != null) {
				List<AttributeData> attributeDataList = new ArrayList<AttributeData>();
				List<IAttributeExtractRuleAsync> attributeExtractionRuleList = new ArrayList<>();
				try {
					attributeExtractionRuleList = extractAttributesUsingRules();
				} catch (Exception ex) {
					errorMessage = ERROR_PREFIX + "extractAttributesUsingRules";
					logger.error(errorMessage, ex);
				}
				List<IAttributeExtractRuleAsync> attributeExtractionRuleFailedList = new ArrayList<>();
				List<Exception> attrExtractionExceptionList = new ArrayList<>();
				serviceCallMethod = "doExtractAsync";
				for (IAttributeExtractRuleAsync rule : attributeExtractionRuleList) {
					try {
						rule.doExtractAsync(externalData, new IAttributeExtractRuleListener() {
							@Override
							public void onAttributeExtractionComplete(Exception exception, DocumentData documentData) {
								if (exception == null) {
									logger.debug("Rule: {} was successful", rule);
									List<AttributeData> processedAttributeDataList = documentData.getAttributes();
									attributeDataList.addAll(processedAttributeDataList);
								} else {
									logger.debug("Rule: {} was unsuccessful", rule);
									attributeExtractionRuleFailedList.add(rule);
									attrExtractionExceptionList.add(exception);
									String serviceCallMethod = "doExtractAsync";
									String errorMessage = ERROR_PREFIX + serviceCallMethod + " (Service Call)";
									logger.error(errorMessage, exception);
								}
							}

						});
					} catch (Exception ex) {
						logger.debug("Rule: {} was unsuccessful", rule);
						attributeExtractionRuleFailedList.add(rule);
						attrExtractionExceptionList.add(ex);
						errorMessage = ERROR_PREFIX + serviceCallMethod + " (Service Call)";
						logger.error(errorMessage, ex);
					}
				}

				try {
					extractAttributesUsingRulesResult(attrExtractionExceptionList, attributeExtractionRuleFailedList);
				} catch (Exception ex) {
					errorMessage = ERROR_PREFIX + "extractAttributesUsingRulesResult";
					logger.error(errorMessage, ex);
				}

				if (attributeDataList != null && attributeDataList.size() > 0) {
					documentData.setAttributes(attributeDataList);
				}
				DocumentData documentDataToUpload = null;
				try {
					documentDataToUpload = uploadDocument(documentData, externalData);
				} catch (Exception ex) {
					errorMessage = ERROR_PREFIX + "uploadDocument";
					logger.error(errorMessage, ex);
				}
				if (documentDataToUpload != null) {
					Exception exceptionFromServiceCall = null;
					serviceCallMethod = "";
					try {
						serviceCallMethod = "addNewDocumentWithAttributes";
						long docId = documentService.addNewDocumentWithAttributes(documentDataToUpload);
						if (!(docId > 0)) {
							throw new Exception("docId generated is " + docId);
						}
						documentData.setDocId(docId);
						// Adding inline images as attachments
						if (documentDataToUpload.getInlineAttachmentDataList() != null
								&& documentDataToUpload.getInlineAttachmentDataList().size() > 0) {
							serviceCallMethod = "addAttachment";
							attachmentService.addUngroupedAttachment(documentDataToUpload, true);
						}
						// Adding attachments
						if (documentDataToUpload.getAttachmentDataList() != null
								&& documentDataToUpload.getAttachmentDataList().size() > 0) {
							serviceCallMethod = "addAttachment";
							addAttachments(documentDataToUpload);
						}

						serviceCallMethod = "addDocEventType";
						documentService.insertDocEventType(docId, EnumEventType.DOCUMENT_CREATED);
						// Consider success only if attribute exception list is empty
						if (attrExtractionExceptionList.isEmpty()) {
							atomicSuccessfulCountInt.incrementAndGet();
						} else {
							atomicFailedCountInt.incrementAndGet();
						}
					} catch (Exception ex) {
						exceptionFromServiceCall = ex;
						atomicFailedCountInt.incrementAndGet();
						errorMessage = ERROR_PREFIX + serviceCallMethod + " (Service Call)";
						logger.error(errorMessage, ex);
					}
					try {
						uploadDocumentResult(exceptionFromServiceCall, externalData, documentData);
					} catch (Exception ex) {
						errorMessage = ERROR_PREFIX + "uploadDocumentResult";
						logger.error(errorMessage, ex);
					}
				}
			}
		}
	}

	// Seperating group and ungroup attachments for service call
	private void addAttachments(DocumentData documentDataToUpload) throws Exception {

		List<AttachmentData> groupedAttachmentDataList = documentDataToUpload.getAttachmentDataList().stream()
				.filter(attachmentData -> StringUtility.hasValue(attachmentData.getGroupName()))
				.collect(Collectors.toList());
		List<AttachmentData> ungroupedAttachmentDataList = documentDataToUpload.getAttachmentDataList().stream()
				.filter(attachmentData -> !StringUtility.hasValue(attachmentData.getGroupName()))
				.collect(Collectors.toList());

		if (ListUtility.hasValue(ungroupedAttachmentDataList)) {
			DocumentData ungroupDocumentData = new DocumentData();
			BeanUtils.copyProperties(documentDataToUpload, ungroupDocumentData);
			ungroupDocumentData.setAttachmentDataList(ungroupedAttachmentDataList);
			attachmentService.addUngroupedAttachment(ungroupDocumentData, false);
		}

		if (ListUtility.hasValue(groupedAttachmentDataList)) {
			Map<String, List<AttachmentData>> attachmentGroupMap = new HashMap<>();
			for (AttachmentData attachmentData : groupedAttachmentDataList) {
				String groupName = attachmentData.getGroupName();
				List<AttachmentData> attachmentDataList = attachmentGroupMap.get(groupName);
				if (attachmentDataList == null) {
					attachmentDataList = new ArrayList<>();
				}
				attachmentDataList.add(attachmentData);
				attachmentGroupMap.put(groupName, attachmentDataList);
			}
			for (String groupName : attachmentGroupMap.keySet()) {
				DocumentData groupDocumentData = new DocumentData();
				BeanUtils.copyProperties(documentDataToUpload, groupDocumentData);
				groupDocumentData.setAttachmentDataList(attachmentGroupMap.get(groupName));
				attachmentService.addGroupedAttachments(groupDocumentData, false);
			}
		}

	}
}
