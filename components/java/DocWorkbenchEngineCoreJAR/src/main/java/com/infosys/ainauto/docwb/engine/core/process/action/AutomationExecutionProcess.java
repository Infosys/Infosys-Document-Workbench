/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.core.process.action;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.docwb.engine.core.common.type.EnumStatusType;
import com.infosys.ainauto.docwb.engine.core.db.storage.IStorageDataAccess;
import com.infosys.ainauto.docwb.engine.core.db.transaction.ITransactionDataAccess;
import com.infosys.ainauto.docwb.engine.core.exception.DocwbEngineException;
import com.infosys.ainauto.docwb.engine.core.model.db.KeyValuePairData;
import com.infosys.ainauto.docwb.engine.core.model.db.TransactionDbData;
import com.infosys.ainauto.docwb.engine.core.service.DocWbApiClient;
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
import com.infosys.ainauto.scriptexecutor.data.ParameterData;
import com.infosys.ainauto.scriptexecutor.data.ScriptResponseItemData;

@Component
public class AutomationExecutionProcess implements IAutomationExecutionProcess {

	private static final Logger logger = LoggerFactory.getLogger(AutomationExecutionProcess.class);
	private static final String REPLACEMENT_FOR_PIPE = Character.toString((char) 25);
	@Autowired
	private ITransactionDataAccess transactionDataAccess;

	@Autowired
	private IStorageDataAccess storageDataAccess;

	private IActionService actionService;
	private ITemplateService templateService;
	private IDocumentService documentService;
	private IOutboundEmailService outboundEmailService;

	private static final String ERROR_PREFIX = "AutomationExecutionProcess - Error occurred in method ";

	@Autowired
	private DocWbApiClient docWbApiClient;

	@PostConstruct
	private void init() {
		documentService = docWbApiClient.getDocumentService();
		actionService = docWbApiClient.getActionService();
		outboundEmailService = docWbApiClient.getOutboundEmailService();
		templateService = docWbApiClient.getTemplateService();
	}

	public int updateResultsForAction(ScriptResponseItemData scriptResponseItemData) {
		List<KeyValuePairData> keyValuePairDataList = new ArrayList<KeyValuePairData>();
		int rowsImpacted = 0;
		long docId = 0;
		boolean isAutoSendEmail = false;
		String errorMessage = "";
		try {
			keyValuePairDataList = transactionDataAccess.getKeyValuePairList(scriptResponseItemData.getTransactionId());
			for (KeyValuePairData keyValuePairData : keyValuePairDataList) {
				if (keyValuePairData.getKeyName().equalsIgnoreCase("docActionRelId,docId,isAutoSendEmail")) {
					ActionParamAttrMappingData actionParamAttrMappingData = new ActionParamAttrMappingData();
					String[] values = keyValuePairData.getKeyValue().split(",");
					actionParamAttrMappingData.setDocActionRelId(Long.parseLong(values[0]));
					docId = Long.parseLong(values[1]);
					actionParamAttrMappingData.setDocId(docId);
					// actionParamAttrMappingData.setDocActionRelId(Long.getLong(parameterData.getParameterValue()));
					// To get Error (uncomment)
					String result = "";
					List<String> output = new ArrayList<String>();
					ParameterData parameter;
					for (int i = 0; i < scriptResponseItemData.getOutParameters().size(); i++) {
						parameter = scriptResponseItemData.getOutParameters().get(i);
						if (parameter.getParameterValue().contains("|")) {
							String pValue = parameter.getParameterValue().replace("|", REPLACEMENT_FOR_PIPE);
							parameter.setParameterValue(pValue);
						}
						output.add(parameter.getParameterName() + "=" + parameter.getParameterValue());

					}
					result = output.stream().collect(Collectors.joining("|"));
					actionParamAttrMappingData.setTaskActionResult(result);
					actionService.updateAction(actionParamAttrMappingData, EnumTaskStatus.COMPLETE);

					isAutoSendEmail = Boolean.parseBoolean(values[2]);
					EmailData emailData = null;
					if (isAutoSendEmail) {
						String serviceCallMethod = "";
						try {
							serviceCallMethod = "getFlattenedTemplates";
							emailData = new EmailData();
							List<ActionTempMappingData> actionTemplateDataList = templateService
									.getFlattenedTemplates(docId);
							boolean isEmailDataAvailable = false;
							for (ActionTempMappingData actionTempMappingData : actionTemplateDataList) {
								if (actionTempMappingData.getIsRecommendedTemplate()) {
									emailData.setEmailBodyText(actionTempMappingData.getTemplateText());
									emailData.setEmailBodyHtml(actionTempMappingData.getTemplateHtml());
									isEmailDataAvailable = true;
									break;
								}
							}

							if (!isEmailDataAvailable) {
								throw new DocwbEngineException("Recommended template not found.");
							}
							serviceCallMethod = "getDraftEmailDataList";
							EmailData draftEmailData = outboundEmailService.getEmailDraft(docId);
							emailData.setDocId(docId);
							emailData.setEmailAddressToList(draftEmailData.getEmailAddressToList());
							emailData.setEmailAddressCcList(draftEmailData.getEmailAddressCcList());
							emailData.setEmailAddressBccList(draftEmailData.getEmailAddressBccList());
							emailData.setEmailSubject(draftEmailData.getEmailSubject());
							// Add External attachments here for auto send email if necessary.
							
							serviceCallMethod = "addOutboundEmail";
							boolean isSuccess = outboundEmailService.addOutboundEmail(emailData);
							if (!isSuccess) {
								throw new Exception("Outbound email has more attachments");
							}
						} catch (Exception ex) {
							errorMessage = ERROR_PREFIX + serviceCallMethod + " (Service Call)";
							logger.error(errorMessage, ex);
						}
					}

					try {
						documentService.insertDocEventType(docId, EnumEventType.ACTION_COMPLETED);
						rowsImpacted = updateTransaction(scriptResponseItemData.getTransactionId(),
								scriptResponseItemData.getLogData(), scriptResponseItemData.getStatus());
						List<DocActionData> actionList = actionService.getActionListForDoc(0, EnumTaskStatus.COMPLETE,
								0, EnumEventOperator.LESS_THAN, docId);
						if (actionList.size() <= 0) {
							documentService.updateDocTaskStatus(docId, EnumTaskStatus.FOR_YOUR_REVIEW);
						}
					} catch (Exception e) {
						errorMessage = ERROR_PREFIX + "insertDocEventType";
						logger.error(errorMessage, e);
					}
				}
			}

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return rowsImpacted;
	}

	public long insertTransaction(String transactionExtId, String parameterName, String parameterValue) {
		long transactionIdRel = 0;
		TransactionDbData transactionData = new TransactionDbData();
		try {
			transactionData.setTransactionExtId(transactionExtId);
			transactionData.setKeyName(parameterName);
			transactionData.setKeyValue(parameterValue);
			transactionData.setStatusTypeCde(EnumStatusType.QUEUED.getCdeValue());

			transactionIdRel = transactionDataAccess.addTransaction(transactionData);

		} catch (

		Exception e) {
			logger.error(e.getMessage(), e);
		}
		return transactionIdRel;
	}

	@Override
	public int updateTransaction(String transactionIdExt, String transactionExtMsg, String statusTypeTxt) {
		TransactionDbData transactionData = new TransactionDbData();
		int rowsImpacted = 0;
		try {
			transactionData.setTransactionExtId(transactionIdExt);
			transactionData.setTransactionExtMessage(transactionExtMsg);
			if (statusTypeTxt.contains("SUCCESS")) {
				transactionData.setStatusTypeCde(EnumStatusType.SUCCESS.getCdeValue());
			} else if (statusTypeTxt.contains("FAILED")) {
				transactionData.setStatusTypeCde(EnumStatusType.FAILED.getCdeValue());
			}
			rowsImpacted = transactionDataAccess.updateTransaction(transactionData);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return rowsImpacted;
	}

	@Override
	public int updateExternalTransaction(String transactionIdExt, String transactionExtStatus) {
		TransactionDbData transactionData = new TransactionDbData();
		int rowsImpacted = 0;
		try {
			transactionData.setTransactionExtId(transactionIdExt);
			transactionData.setTransactionExtStatusTxt(transactionExtStatus);
			rowsImpacted = transactionDataAccess.updateExternalTransaction(transactionData);

		} catch (

		Exception e) {
			logger.error(e.getMessage(), e);
		}
		return rowsImpacted;
	}

	@Override
	public List<TransactionDbData> getTransactionByStatus(int statusTypeCde) {
		List<TransactionDbData> transactionDbDataList = new ArrayList<TransactionDbData>();
		try {
			transactionDbDataList = transactionDataAccess.getTransactionByStatus(statusTypeCde);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return transactionDbDataList;

	}

	@Override
	public String getValue(String key) {
		String value = null;
		try {
			value = storageDataAccess.getValue(key);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return value;
	}

	@Override
	public int updateKeyValue(String key, String value) {
		int rowsImpacted = 0;
		try {
			rowsImpacted = storageDataAccess.updateKeyValue(key, value);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return rowsImpacted;
	}

	@Override
	public long addKeyValue(String key, String value) {
		long keyValueId = 0;
		try {
			keyValueId = storageDataAccess.addKeyValue(key, value);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return keyValueId;
	}
}