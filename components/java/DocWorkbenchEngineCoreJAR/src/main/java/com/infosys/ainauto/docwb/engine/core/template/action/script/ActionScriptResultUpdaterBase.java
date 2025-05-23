/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.core.template.action.script;

import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.engine.core.common.DocwbEngineCoreConstants;
import com.infosys.ainauto.docwb.engine.core.common.InMemoryPropertiesManager;
import com.infosys.ainauto.docwb.engine.core.common.type.EnumExecutionEventType;
import com.infosys.ainauto.docwb.engine.core.common.type.EnumExecutorType;
import com.infosys.ainauto.docwb.engine.core.common.type.EnumStatusType;
import com.infosys.ainauto.docwb.engine.core.db.logger.IDbLogger;
import com.infosys.ainauto.docwb.engine.core.model.SummaryData;
import com.infosys.ainauto.docwb.engine.core.model.db.TransactionDbData;
import com.infosys.ainauto.docwb.engine.core.process.action.IAutomationExecutionProcess;
import com.infosys.ainauto.docwb.engine.core.service.client.IMetricsService;
import com.infosys.ainauto.docwb.engine.core.service.client.MetricsService.EnumMetric;
import com.infosys.ainauto.docwb.engine.core.service.script.ScriptExecutorProxy;
import com.infosys.ainauto.scriptexecutor.api.IScriptExecutorService;
import com.infosys.ainauto.scriptexecutor.data.ScriptResponseData;

@Component
public abstract class ActionScriptResultUpdaterBase {

	@Autowired
	private IDbLogger dbLogger;

	@Autowired
	private IAutomationExecutionProcess automationExecutionProcess;

	private IScriptExecutorService scriptExecutorService;

	@Autowired
	private ScriptExecutorProxy scriptExecutorProxy;

	@Autowired
	private IMetricsService metricsService;

	private static final String PROP_NAME_ACTION_SCRIPT_RESULT_UPDATER_ENABLED = "action.script.result.updater.enabled";

	private static Logger logger = LoggerFactory.getLogger(ActionScriptResultUpdaterBase.class);

	private static final String ERROR_PREFIX = "ActionScriptResultUpdater - Error occurred in method ";

	@PostConstruct
	private void init() {
		scriptExecutorService = scriptExecutorProxy.getScriptExecutorService();
	}

	public void execute(String name) {
		// MDC Logging
		MDC.put(DocwbEngineCoreConstants.MDC_TEMPLATE_NAME, name + "-" + StringUtility.generateTransactionId());
		int successfulCount = 0;
		int failedCount = 0;
		int totalCount = 0;
		String errorMessage = "";
		long executionId = DocwbEngineCoreConstants.EXECUTION_ID_EMPTY;
		long executionEventId = 0;
		boolean isSuccess = true;

		metricsService.startTimer(EnumMetric.WORKFLOW_EXECUTOR_START_ELAPSED_TIME,
				EnumExecutorType.ACTION_SCRIPT_RESULT_UPDATER, true);

		try {
			List<TransactionDbData> transactionDbDataList = automationExecutionProcess
					.getTransactionByStatus(EnumStatusType.QUEUED.getCdeValue());
			logger.debug("Transaction List Retrived");
			if (transactionDbDataList != null && transactionDbDataList.size() > 0) {
				for (TransactionDbData transactionDbData : transactionDbDataList) {
					ScriptResponseData scriptResponseData = null;
					try {
						scriptResponseData = triggerTransactionStatus(transactionDbData);
						logger.debug("Retrived Transaction status successfully");
					} catch (Exception e) {
						isSuccess = false;
						errorMessage = ERROR_PREFIX + "triggerTransactionStatus. Service call failed";
						logger.error(errorMessage, e);
					}
					if (scriptResponseData != null){
						if( ListUtility.hasValue(scriptResponseData.getScriptResponseItemDataList())
							&& scriptResponseData.getScriptResponseItemDataList().get(0).getStatus()
									.equalsIgnoreCase("SUCCESS")) {
							try {
								automationExecutionProcess.updateExternalTransaction(
										scriptResponseData.getScriptResponseItemDataList().get(0).getTransactionId(),
										scriptResponseData.getScriptResponseItemDataList().get(0).getStatus());
								automationExecutionProcess
										.updateResultsForAction(scriptResponseData.getScriptResponseItemDataList().get(0));
								successfulCount++;
								logger.debug("Updated transaction status with transactionId ",
										scriptResponseData.getScriptResponseItemDataList().get(0).getTransactionId());
							} catch (Exception e) {
								failedCount++;
								errorMessage = ERROR_PREFIX
										+ "automationExecutionProcess. update External Transaction call failed";
								logger.error(errorMessage, e);
							}
						}
					}
				}
				if (transactionDbDataList != null && transactionDbDataList.size() > 0)
					totalCount += transactionDbDataList.size();
				if (executionEventId > 0) {
					dbLogger.updateEventMsg(executionEventId, totalCount + " transaction status(es)");
				} else {
					executionId = dbLogger.startExecution(EnumExecutorType.ACTION_SCRIPT_RESULT_UPDATER, name, "");
					executionEventId = dbLogger.addEvent(executionId, EnumExecutionEventType.WORK_STARTED,
							totalCount + " transaction status(es)");
				}
			}
			if (isSuccess) {
				InMemoryPropertiesManager.getInstance().setProperty(PROP_NAME_ACTION_SCRIPT_RESULT_UPDATER_ENABLED,
						"false");
			}
		} catch (Exception e) {
			errorMessage = ERROR_PREFIX + "automationExecutionProcess. get Transaction By Status call failed";
			logger.error(errorMessage, e);
		}

		String dbExecutionSummary = "";
		try {
			SummaryData summaryData = new SummaryData(name + " [Error Recovery]", " Transaction status");
			summaryData.setTotalCount(totalCount);
			summaryData.setFailedCount(failedCount);
			summaryData.setSuccessfulCount(successfulCount);
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
					EnumExecutorType.ACTION_SCRIPT_RESULT_UPDATER, true);
		}

	}

	protected ScriptResponseData triggerTransactionStatus(TransactionDbData transactionDbData) throws Exception {
		return scriptExecutorService.getTransactionStatusAndResult(transactionDbData.getTransactionExtId());
	}

	protected void terminate(SummaryData summaryData) throws Exception {
		logger.info(summaryData.toString());
	};
}
