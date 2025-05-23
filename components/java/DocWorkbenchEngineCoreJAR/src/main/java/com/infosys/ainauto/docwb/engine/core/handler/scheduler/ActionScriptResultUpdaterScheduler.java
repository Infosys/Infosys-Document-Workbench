/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.core.handler.scheduler;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.engine.core.common.DocwbEngineCoreConstants;
import com.infosys.ainauto.docwb.engine.core.common.InMemoryPropertiesManager;
import com.infosys.ainauto.docwb.engine.core.common.type.EnumExecutorType;
import com.infosys.ainauto.docwb.engine.core.service.client.IMetricsService;
import com.infosys.ainauto.docwb.engine.core.service.client.MetricsService.EnumMetric;
import com.infosys.ainauto.docwb.engine.core.stereotype.ActionScriptResultUpdater;
import com.infosys.ainauto.docwb.engine.core.template.action.script.ActionScriptResultUpdaterBase;

@Component
public class ActionScriptResultUpdaterScheduler {

	@Autowired
	private ApplicationContext context;

	@Autowired
	private IMetricsService metricsService;

	@Autowired
	private ConfigurableEnvironment environment;

	private static final String PROP_NAME_ACTION_SCRIPT_RESULT_UPDATER_TRIGGER_FREQUENCY = "action.script.result.updater.interval.milliseconds";
	private static final String PROP_NAME_ACTION_SCRIPT_RESULT_UPDATER_ENABLED = "action.script.result.updater.enabled";

	private static Logger logger = LoggerFactory.getLogger(ActionScriptResultUpdaterScheduler.class);

	@Scheduled(fixedDelayString = "${action.script.result.updater.interval.milliseconds}")
	public void scheduleFixedDelayTask() {
		boolean isFeatureEnabled = Boolean
				.parseBoolean(environment.getProperty(PROP_NAME_ACTION_SCRIPT_RESULT_UPDATER_ENABLED));
		if (!isFeatureEnabled) {
			return;
		}

		// Check if service needs to be stopped from in memory update
		if (!Boolean.parseBoolean(InMemoryPropertiesManager.getInstance()
				.getProperty(PROP_NAME_ACTION_SCRIPT_RESULT_UPDATER_ENABLED, "true"))) {
			return;
		}

		// MDC Logging
		MDC.put(DocwbEngineCoreConstants.MDC_SCHEDULER_NAME,
				"ActionScriptResultUpdaterSch" + "-" + StringUtility.generateTransactionId());

		logger.info("Triggering new Error Recovery event [Frequency=Every {} milliseconds]", StringUtility
				.sanitizeReqData(environment.getProperty(PROP_NAME_ACTION_SCRIPT_RESULT_UPDATER_TRIGGER_FREQUENCY)));

		Map<String, Object> beanMap = context.getBeansWithAnnotation(ActionScriptResultUpdater.class);

		for (String key : beanMap.keySet()) {
			ActionScriptResultUpdaterBase actionScriptResultUpdater = (ActionScriptResultUpdaterBase) beanMap.get(key);
			ActionScriptResultUpdater actionScriptResultUpdaterAnnotation = actionScriptResultUpdater.getClass()
					.getAnnotation(ActionScriptResultUpdater.class);
			String title = actionScriptResultUpdaterAnnotation.title();
			try {
				actionScriptResultUpdater.execute(title);
			} catch (Exception e) {
				logger.error("Error occured while executing ActionExecutor", e);
			}
		}

		metricsService.startTimer(EnumMetric.WORKFLOW_SCHEDULER_END_ELAPSED_TIME,
				EnumExecutorType.ACTION_SCRIPT_RESULT_UPDATER, true);
	}

}
