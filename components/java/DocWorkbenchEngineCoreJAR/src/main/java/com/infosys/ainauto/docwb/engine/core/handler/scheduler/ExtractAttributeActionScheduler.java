/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.core.handler.scheduler;

import java.util.Map;

import javax.json.JsonObject;

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
import com.infosys.ainauto.docwb.engine.core.stereotype.ExtractAttributeActionExecutor;
import com.infosys.ainauto.docwb.engine.core.template.action.attribute.ExtractAttributeActionExecutorBase;

@Component
public class ExtractAttributeActionScheduler {

	@Autowired
	private ApplicationContext context;

	@Autowired
	private IMetricsService metricsService;

	@Autowired
	private Environment environment;

	private static final String PROP_NAME_EXTRACTOR_TRIGGER_FREQUENCY = "re.extract.action.executor.interval.milliseconds";
	private static final String PROP_NAME_EXTRACTOR_ENABLED = "re.extract.action.executor.enabled";
	private static Logger logger = LoggerFactory.getLogger(ExtractAttributeActionScheduler.class);

	@Scheduled(fixedDelayString = "${re.extract.action.executor.interval.milliseconds}")
	public void scheduleFixedDelayTask() {
		boolean isFeatureEnabled = Boolean.valueOf(environment.getProperty(PROP_NAME_EXTRACTOR_ENABLED));
		if (!isFeatureEnabled) {
			return;
		}

		// MDC Logging
		MDC.put(DocwbEngineCoreConstants.MDC_SCHEDULER_NAME,
				"ExtractAttrActionSch" + "-" + StringUtility.generateTransactionId());

		logger.info("Triggering new attrExtractor event [Frequency=Every " + StringUtility
				.sanitizeReqData(environment.getProperty(PROP_NAME_EXTRACTOR_TRIGGER_FREQUENCY) + " milliseconds]"));

		Map<String, Object> beanMap = context.getBeansWithAnnotation(ExtractAttributeActionExecutor.class);

		for (String key : beanMap.keySet()) {
			ExtractAttributeActionExecutorBase extractAttributeActionExecutor = (ExtractAttributeActionExecutorBase) beanMap
					.get(key);
			ExtractAttributeActionExecutor extractAttributeActionAnnotation = extractAttributeActionExecutor.getClass()
					.getAnnotation(ExtractAttributeActionExecutor.class);
			String title = extractAttributeActionAnnotation.title();
			String jsonConfigFile = extractAttributeActionAnnotation.jsonConfigFile();
			JsonObject jsonObject = null;
			if (StringUtility.hasTrimmedValue(jsonConfigFile)) {
				jsonObject = FileUtility.readJsonAsObject(jsonConfigFile);
			}
			try {
				extractAttributeActionExecutor.execute(title, jsonObject);
			} catch (Exception e) {
				logger.error("Error occured while executing ExtractAttributeActionExecutor", e);
			}
		}

		metricsService.startTimer(EnumMetric.WORKFLOW_SCHEDULER_END_ELAPSED_TIME,
				EnumExecutorType.EXTRACT_ATTRIBUTE_ACTION_EXECUTOR, true);
	}

}
