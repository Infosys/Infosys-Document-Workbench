/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.core.template.emailsender;

import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.FileUtility;
import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.engine.core.common.DocwbEngineCoreConstants;
import com.infosys.ainauto.docwb.engine.core.common.type.EnumExecutionEventType;
import com.infosys.ainauto.docwb.engine.core.common.type.EnumExecutorType;
import com.infosys.ainauto.docwb.engine.core.db.logger.IDbLogger;
import com.infosys.ainauto.docwb.engine.core.model.SummaryData;
import com.infosys.ainauto.docwb.engine.core.service.DocWbApiClient;
import com.infosys.ainauto.docwb.engine.core.service.client.IMetricsService;
import com.infosys.ainauto.docwb.engine.core.service.client.MetricsService.EnumMetric;
import com.infosys.ainauto.docwb.engine.core.service.email.IEmailSenderCoreService;
import com.infosys.ainauto.docwb.web.api.IDocumentService;
import com.infosys.ainauto.docwb.web.api.IOutboundEmailService;
import com.infosys.ainauto.docwb.web.data.AttachmentData;
import com.infosys.ainauto.docwb.web.data.EmailData;
import com.infosys.ainauto.docwb.web.exception.DocwbWebException;
import com.infosys.ainauto.docwb.web.type.EnumEventType;
import com.infosys.ainauto.docwb.web.type.EnumTaskStatus;

@Component
public abstract class OutboundEmailSenderBase {

	private static Logger logger = LoggerFactory.getLogger(OutboundEmailSenderBase.class);

	@Autowired
	private DocWbApiClient docWbApiClient;

	@Autowired
	private IMetricsService metricsService;

	@Autowired
	private IDbLogger dbLogger;

	private IDocumentService documentService;
	private IOutboundEmailService outboundEmailService;

	@Autowired
	private IEmailSenderCoreService emailSenderCoreService;

	@Autowired
	private Environment environment;

	private static final String ERROR_PREFIX = "OutboundEmailSender - Error occurred in method ";
	private static final String OUTBOUND_EMAIL_SAVE_FEATURE_ENABLED = "email.sender.save.sent.mail.enabled";

	@PostConstruct
	private void init() {
		documentService = docWbApiClient.getDocumentService();
		outboundEmailService = docWbApiClient.getOutboundEmailService();
	}

	public void execute(String name, Properties properties) {
		// MDC Logging
		MDC.put(DocwbEngineCoreConstants.MDC_TEMPLATE_NAME, name + "-" + StringUtility.generateTransactionId());
		boolean isCallSuccessful = false;
		int successfulCount = 0;
		int failedCount = 0;
		int totalCount = 0;
		String errorMessage = "";

		long executionId = DocwbEngineCoreConstants.EXECUTION_ID_EMPTY;

		metricsService.startTimer(EnumMetric.WORKFLOW_EXECUTOR_START_ELAPSED_TIME,
				EnumExecutorType.OUTBOUND_EMAIL_SENDER, true);

		String tempDownloadPath = FileUtility.getAbsolutePath(environment.getProperty("docwb.engine.temp.path"));
		boolean isSaveSentMailEnabled = Boolean.valueOf(environment.getProperty(OUTBOUND_EMAIL_SAVE_FEATURE_ENABLED));

		try {
			isCallSuccessful = initialize(properties);
		} catch (Exception ex) {
			errorMessage = ERROR_PREFIX + "initialize";
			logger.error(errorMessage, ex);
		}

		if (isCallSuccessful) {
			List<EmailData> emailDataList = null;
			try {
				emailDataList = outboundEmailService.getOutboundEmailList(EnumTaskStatus.YET_TO_START,
						tempDownloadPath);
			} catch (DocwbWebException e1) {
				errorMessage = ERROR_PREFIX + "getEmailList" + " (Service Call)";
				logger.error(errorMessage, e1);
			}
			if ((emailDataList == null) || (emailDataList.size() == 0)) {
				try {
					noEmailsToSend();
				} catch (Exception ex) {
					errorMessage = ERROR_PREFIX + "noEmailsToSend";
					logger.error(errorMessage, ex);
				}
			} else {
				try {
					emailDataList = doFilter(emailDataList);
				} catch (Exception ex) {
					errorMessage = ERROR_PREFIX + "doFilter";
					logger.error(errorMessage, ex);
				}

				if (emailDataList != null && emailDataList.size() > 0) {
					totalCount = emailDataList.size();
					executionId = dbLogger.startExecution(EnumExecutorType.OUTBOUND_EMAIL_SENDER, name, "");
					dbLogger.addEvent(executionId, EnumExecutionEventType.WORK_STARTED, totalCount + " email(s)");
					for (EmailData emailData : emailDataList) {
						try {
							Exception exceptionFromServiceCall = null;
							String serviceCallMethod = "";
							emailData = sendEmail(emailData);
							if (emailData != null) {
								try {
									serviceCallMethod = "sendEmail";
									emailSenderCoreService.sendEmail(emailData, isSaveSentMailEnabled);
									serviceCallMethod = "updateOutboundEmailStatus";
									outboundEmailService.updateOutboundEmailStatus(emailData.getEmailOutboundId(),
											EnumTaskStatus.COMPLETE);
									serviceCallMethod = "insertDocEventType";
									documentService.insertDocEventType(emailData.getDocId(), EnumEventType.EMAIL_SENT);
									successfulCount++;
								} catch (Exception ex) {
									exceptionFromServiceCall = ex;
									errorMessage = ERROR_PREFIX + serviceCallMethod + " (Service Call)";
									failedCount++;
								} finally {
									for (AttachmentData attachmentData : emailData.getAttachmentDataList())
										FileUtility.deleteFile(attachmentData.getPhysicalPath());
									for (AttachmentData attachmentData : emailData.getInlineImageAttachmentDataList())
										FileUtility.deleteFile(attachmentData.getPhysicalPath());
								}
							}
							sendEmailResult(exceptionFromServiceCall, emailData);
						} catch (Exception ex) {
							errorMessage = ERROR_PREFIX + "noEmailsToSend";
							logger.error(errorMessage, ex);
						}
					}
				}

			}
		}
		String dbExecutionSummary = "";
		try {
			SummaryData summaryData = new SummaryData(name + " [Outbound Email Sender]", "email");
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
					EnumExecutorType.OUTBOUND_EMAIL_SENDER, true);
		}
	}

	protected boolean initialize(Properties properties) throws Exception {
		return true;
	}

	protected void noEmailsToSend() throws Exception {
		logger.debug("No emails found to send");
	}

	protected List<EmailData> doFilter(List<EmailData> emailDataList) throws Exception {
		// No filter needed so return the same list as-is
		return emailDataList;
	}

	protected EmailData sendEmail(EmailData emailData) throws Exception {
		// No changes needed so return the same object as-is
		return emailData;
	}

	protected void sendEmailResult(Exception ex, EmailData outboundEmailData) throws Exception {
		if (ex != null) {
			logger.error("Error occurred in send email", ex);
		} else {
			logger.info("Email sent successfully with docId=" + outboundEmailData.getDocId());
		}
	}

	protected void terminate(SummaryData summaryData) throws Exception {
		logger.info(summaryData.toString());
	}

}
