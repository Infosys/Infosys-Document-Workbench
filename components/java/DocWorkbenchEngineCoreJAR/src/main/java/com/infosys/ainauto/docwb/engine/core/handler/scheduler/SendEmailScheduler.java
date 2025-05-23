/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.core.handler.scheduler;

import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.FileUtility;
import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.engine.core.common.DocwbEngineCoreConstants;
import com.infosys.ainauto.docwb.engine.core.common.type.EnumExecutorType;
import com.infosys.ainauto.docwb.engine.core.service.client.IMetricsService;
import com.infosys.ainauto.docwb.engine.core.service.client.MetricsService.EnumMetric;
import com.infosys.ainauto.docwb.engine.core.stereotype.EmailSender;
import com.infosys.ainauto.docwb.engine.core.template.emailsender.OutboundEmailSenderBase;

@Component
public class SendEmailScheduler {

	@Autowired
	private ApplicationContext context;

	@Autowired
	private IMetricsService metricsService;

	@Autowired
	private Environment environment;

	private static final String PROP_NAME_SENDER_TRIGGER_FREQUENCY = "email.sender.interval.milliseconds";
	private static final String PROP_NAME_SENDER_ENABLED = "email.sender.enabled";
	private static Logger logger = LoggerFactory.getLogger(SendEmailScheduler.class);

	@Scheduled(fixedDelayString = "${email.sender.interval.milliseconds}")
	public void scheduleFixedDelayTask() {
		boolean isFeatureEnabled = Boolean.parseBoolean(environment.getProperty(PROP_NAME_SENDER_ENABLED));
		if (!isFeatureEnabled) {
			return;
		}

		// MDC Logging
		MDC.put(DocwbEngineCoreConstants.MDC_SCHEDULER_NAME,
				"SendEmailSch" + "-" + StringUtility.generateTransactionId());

		logger.info("Triggering new emailSender event [Frequency=Every {} milliseconds]",
				StringUtility.sanitizeReqData(environment.getProperty(PROP_NAME_SENDER_TRIGGER_FREQUENCY)));

		Map<String, Object> beanMap = context.getBeansWithAnnotation(EmailSender.class);

		for (String key : beanMap.keySet()) {
			OutboundEmailSenderBase outboundEmailSender = (OutboundEmailSenderBase) beanMap.get(key);
			EmailSender emailSenderAnnotation = outboundEmailSender.getClass().getAnnotation(EmailSender.class);
			String title = emailSenderAnnotation.title();
			String propertiesFile = emailSenderAnnotation.propertiesFile();
			Properties properties = new Properties();
			if (StringUtility.hasTrimmedValue(propertiesFile)) {
				properties = FileUtility.readProperties(propertiesFile);
			}
			try {
				outboundEmailSender.execute(title, properties);
			} catch (Exception e) {
				logger.error("Error occured while executing EmailSender", e);
			}
		}

		metricsService.startTimer(EnumMetric.WORKFLOW_SCHEDULER_END_ELAPSED_TIME,
				EnumExecutorType.OUTBOUND_EMAIL_SENDER, true);
	}
}
