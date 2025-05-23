/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.core.template.action.script;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.json.JsonArray;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.engine.core.common.DocwbEngineCoreConstants;
import com.infosys.ainauto.docwb.engine.core.common.type.EnumExecutionEventType;
import com.infosys.ainauto.docwb.engine.core.common.type.EnumExecutorType;
import com.infosys.ainauto.docwb.engine.core.db.logger.IDbLogger;
import com.infosys.ainauto.docwb.engine.core.exception.DocwbEngineException;
import com.infosys.ainauto.docwb.engine.core.model.SummaryData;
import com.infosys.ainauto.docwb.engine.core.process.action.IAutomationExecutionProcess;
import com.infosys.ainauto.docwb.engine.core.service.DocWbApiClient;
import com.infosys.ainauto.docwb.engine.core.service.client.IMetricsService;
import com.infosys.ainauto.docwb.engine.core.service.client.MetricsService.EnumMetric;
import com.infosys.ainauto.docwb.engine.core.service.script.ScriptExecutorProxy;
import com.infosys.ainauto.docwb.web.api.IActionService;
import com.infosys.ainauto.docwb.web.api.IAttributeService;
import com.infosys.ainauto.docwb.web.api.IDocumentService;
import com.infosys.ainauto.docwb.web.data.ActionParamAttrMappingData;
import com.infosys.ainauto.docwb.web.data.AttributeData;
import com.infosys.ainauto.docwb.web.data.DocActionData;
import com.infosys.ainauto.docwb.web.data.ParamAttrData;
import com.infosys.ainauto.docwb.web.type.EnumEventOperator;
import com.infosys.ainauto.docwb.web.type.EnumSystemAttributeName;
import com.infosys.ainauto.docwb.web.type.EnumTaskStatus;
import com.infosys.ainauto.docwb.web.type.EnumTaskType;
import com.infosys.ainauto.scriptexecutor.api.IScriptExecutorService;
import com.infosys.ainauto.scriptexecutor.data.ParameterData;
import com.infosys.ainauto.scriptexecutor.data.ScriptIdentifierData;
import com.infosys.ainauto.scriptexecutor.data.ScriptResponseData;
import com.infosys.ainauto.scriptexecutor.data.ScriptResponseItemData;

@Component
public abstract class ActionScriptExecutorBase {

	@Autowired
	private DocWbApiClient docWbApiClient;

	@Autowired
	private ScriptExecutorProxy scriptExecutorProxy;

	@Autowired
	private IMetricsService metricsService;

	@Autowired
	private IAutomationExecutionProcess automationExecutionProcess;

	@Autowired
	private IDbLogger dbLogger;

	private IDocumentService documentService;
	private IActionService actionService;
	private IScriptExecutorService scriptExecutorService;
	private IAttributeService attributeService;

	private static Logger logger = LoggerFactory.getLogger(ActionScriptExecutorBase.class);

	private static final String ERROR_PREFIX = "ActionScriptExecutor - Error occurred in method ";

	@PostConstruct
	private void init() {
		documentService = docWbApiClient.getDocumentService();
		actionService = docWbApiClient.getActionService();
		attributeService = docWbApiClient.getAttributeService();
		scriptExecutorService = scriptExecutorProxy.getScriptExecutorService();
	}

	public void execute(String name, JsonObject jsonConfig, int retryCount) {
		// MDC Logging
		MDC.put(DocwbEngineCoreConstants.MDC_TEMPLATE_NAME, name + "-" + StringUtility.generateTransactionId());
		int successfulCount = 0;
		int failedCount = 0;
		int totalCount = 0;
		String errorMessage = "";

		long executionId = DocwbEngineCoreConstants.EXECUTION_ID_EMPTY;
		long executionEventId = 0;

		metricsService.startTimer(EnumMetric.WORKFLOW_EXECUTOR_START_ELAPSED_TIME,
				EnumExecutorType.ACTION_SCRIPT_EXECUTOR, true);

		int queueNameCde;
		int actionNameCde;
		JsonObject jsonObjScriptExecutor;
		if (jsonConfig != null && !jsonConfig.isNull("actionScriptMapping")) {
			JsonArray jsonArrMapping = jsonConfig.getJsonArray("actionScriptMapping");
			if (!jsonArrMapping.isEmpty()) {
				for (int i = 0; i < jsonArrMapping.size(); i++) {
					JsonObject jsonObjItem = jsonArrMapping.getJsonObject(i);
					actionNameCde = jsonObjItem.getInt("actionNameCde", 0);
					queueNameCde = jsonObjItem.getInt("queueNameCde", 0);

					if (jsonObjItem.isNull("scriptExecutor")) {
						continue;
					}
					jsonObjScriptExecutor = jsonObjItem.getJsonObject("scriptExecutor");

					List<DocActionData> docActionDataList = null;
					try {
						docActionDataList = getActions(queueNameCde, actionNameCde);
					} catch (Exception ex) {
						errorMessage = ERROR_PREFIX + "getActions";
						logger.error(errorMessage, ex);
					}

					if (docActionDataList != null && docActionDataList.size() > 0) {
						totalCount += docActionDataList.size();
						// DB Logging - Total count cannot be not known up front so add event first
						// and then update this event with every iteration
						if (executionEventId > 0) {
							dbLogger.updateEventMsg(executionEventId, totalCount + " action(s)");
						} else {
							executionId = dbLogger.startExecution(EnumExecutorType.ACTION_EXECUTOR, name, "");
							executionEventId = dbLogger.addEvent(executionId, EnumExecutionEventType.WORK_STARTED,
									totalCount + " action(s)");
						}

						for (DocActionData docActionData : docActionDataList) {
							long docId = docActionData.getDocId();
							try {
								documentService.updateDocTaskStatus(docId, EnumTaskStatus.IN_PROGRESS);
							} catch (Exception ex) {
								errorMessage = ERROR_PREFIX + "updateDocTaskStatus";
								logger.error(errorMessage, ex);
							}

							List<ActionParamAttrMappingData> actionParamAttrMappingDataList = docActionData
									.getActionParamAttrMappingDataList();

							for (ActionParamAttrMappingData actionParamAttrMappingData : actionParamAttrMappingDataList) {
								try {

									// Call to external system
									String externalTransactionId = triggerExternalAction(actionParamAttrMappingData,
											jsonObjScriptExecutor);
									if (externalTransactionId != null && externalTransactionId.length() > 0) {
										actionService.updateAction(actionParamAttrMappingData,
												EnumTaskStatus.IN_PROGRESS);
									}
									boolean isAutoSendEmail = isAutoSendEmail(actionParamAttrMappingData, jsonObjItem);
									long transactionId = automationExecutionProcess.insertTransaction(
											externalTransactionId, "docActionRelId,docId,isAutoSendEmail",
											String.valueOf(actionParamAttrMappingData.getDocActionRelId()) + ","
													+ String.valueOf(actionParamAttrMappingData.getDocId()) + ","
													+ isAutoSendEmail);
									logger.debug("Created transaction with transactionId {}", transactionId);
									successfulCount++;
									try {
										triggerExternalActionResult(null, actionParamAttrMappingData,
												externalTransactionId);
									} catch (Exception ex2) {
										errorMessage = ERROR_PREFIX + "actionExecutionFailed";
										logger.error(errorMessage, ex2);
									}
								} catch (Exception ex) {
									failedCount++;
									errorMessage = ERROR_PREFIX + "executeAction. Service call failed";
									logger.error(errorMessage, ex);
									try {
										String key = DocwbEngineCoreConstants.KEY_DOC_ACTION
												+ actionParamAttrMappingData.getDocActionRelId();
										String value = automationExecutionProcess.getValue(key);
										int count = 0;
										if (value != null && value.length() > 0) {
											count = (int) Integer.valueOf(value);
										}

										if (count == retryCount) {
											actionService.updateAction(actionParamAttrMappingData,
													EnumTaskStatus.FAILED);
											List<DocActionData> actionList = actionService.getActionListForDoc(
													actionNameCde, EnumTaskStatus.COMPLETE, 0,
													EnumEventOperator.LESS_THAN, docId);
											if (actionList.size() <= 0) {
												documentService.updateDocTaskStatus(docId,
														EnumTaskStatus.FOR_YOUR_REVIEW);
											}
											triggerExternalActionResult(ex, actionParamAttrMappingData, "");
										} else {
											count++;
											value = String.valueOf(count);
											if (count > 1)
												automationExecutionProcess.updateKeyValue(key, value);
											else
												automationExecutionProcess.addKeyValue(key, value);
											actionService.updateAction(actionParamAttrMappingData,
													EnumTaskStatus.RETRY_LATER);
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
							noActions(queueNameCde, actionNameCde);
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
			SummaryData summaryData = new SummaryData(name + " [Action Script Executor]", "action");
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
					EnumExecutorType.ACTION_SCRIPT_EXECUTOR, true);
		}

	}

	protected List<DocActionData> getActions(int queueNameCde, int actionNameCde) throws Exception {
		List<DocActionData> docActionDataList = actionService.getActionList(actionNameCde, EnumTaskStatus.YET_TO_START,
				queueNameCde, EnumEventOperator.EQUALS);
		docActionDataList.addAll(actionService.getActionList(actionNameCde, EnumTaskStatus.RETRY_LATER, queueNameCde,
				EnumEventOperator.EQUALS));
		return docActionDataList;
	};

	protected void noActions(int queueNameCde, int actionNameCde) throws Exception {
		logger.debug("No actions found for queueNameCde={} and actionNameCde={}", queueNameCde, actionNameCde);
	};

	protected String triggerExternalAction(ActionParamAttrMappingData actionParamAttrMappingData,
			JsonObject jsonConfigEntry) throws Exception {
		String externalTransactionId = null;
		ScriptIdentifierData scriptIdentifierData = new ScriptIdentifierData();
		scriptIdentifierData.setScriptId(jsonConfigEntry.getInt("scriptId", 0));
		scriptIdentifierData.setScriptName(jsonConfigEntry.getString("scriptName", null));
		scriptIdentifierData.setCategoryId(jsonConfigEntry.getInt("categoryId", 0));
		scriptIdentifierData.setCompanyId(jsonConfigEntry.getInt("companyId", 0));
		scriptIdentifierData.setExecutionMode(jsonConfigEntry.getInt("executionMode", 0));
		scriptIdentifierData.setReferenceKey(jsonConfigEntry.getString("referenceKey", null));
		scriptIdentifierData.setRemoteServerNames(jsonConfigEntry.getString("remoteServerNames", null));
		scriptIdentifierData.setResponseNotificationCallbackURL(jsonConfigEntry.getString("callbackUrl", null));

		if (actionParamAttrMappingData.getParamAttrDataList() != null
				&& actionParamAttrMappingData.getParamAttrDataList().size() > 0) {
			List<ParameterData> parameterDataList = new ArrayList<ParameterData>();
			for (ParamAttrData paramAttrData : actionParamAttrMappingData.getParamAttrDataList()) {
				ParameterData parameterData = new ParameterData();
				parameterData.setParameterName(paramAttrData.getParamNameTxt());
				parameterData.setParameterValue(paramAttrData.getParamValue());
				parameterDataList.add(parameterData);
			}
			scriptIdentifierData.setParameterDataList(parameterDataList);
		}

		ScriptResponseData scriptResponseData = scriptExecutorService.initiateExecution(scriptIdentifierData);
		if (scriptResponseData != null && scriptResponseData.getScriptResponseItemDataList() != null
				&& scriptResponseData.getScriptResponseItemDataList().size() > 0) {
			ScriptResponseItemData scriptResponseItemData = scriptResponseData.getScriptResponseItemDataList().get(0);
			if ("QUEUED".equals(scriptResponseItemData.getStatus())) {
				externalTransactionId = scriptResponseItemData.getTransactionId();
			} else {
				throw new DocwbEngineException("Error occurred while calling ScriptExecutorService: "
						+ scriptResponseItemData.getErrorMessage());
			}
		} else {
			throw new DocwbEngineException("Error occurred while calling ScriptExecutorService");
		}
		return externalTransactionId;
	};

	protected void triggerExternalActionResult(Exception ex, ActionParamAttrMappingData actionParamAttrMappingData,
			String externalTransactionId) throws Exception {
		logger.debug("Created transaction with external transactionId {}", externalTransactionId);
	};

	protected boolean isAutoSendEmail(ActionParamAttrMappingData actionParamAttrMappingData, JsonObject jsonConfigEntry)
			throws Exception {
		boolean isAutoSendEmail = jsonConfigEntry.getBoolean("isAutoSendEmail", false);
		int minConfidencePctRequired = jsonConfigEntry.getInt("minConfidencePctRequired", 100);
		if (actionParamAttrMappingData.getTaskType() != EnumTaskType.SYSTEM)
			return false;
		if (actionParamAttrMappingData != null && isAutoSendEmail) {
			long docId = actionParamAttrMappingData.getDocId();
			try {
				List<AttributeData> attributeDataList = attributeService.getDocAttributeList(docId);
				for (AttributeData attributeData : attributeDataList) {
					if (attributeData.getAttrNameCde() == EnumSystemAttributeName.CATEGORY.getCde()) {
						if (attributeData.getConfidencePct() >= minConfidencePctRequired) {
							return true;
						} else {
							break;
						}
					}
					// Add logic here if need to compare attribute value for autosend email.
				}
			} catch (Exception e) {
				logger.error("Error occurred in isAutoSendEmail()", e);
			}
		}
		return false;
	}

	protected void terminate(SummaryData summaryData) throws Exception {
		logger.info(summaryData.toString());
	};

}
