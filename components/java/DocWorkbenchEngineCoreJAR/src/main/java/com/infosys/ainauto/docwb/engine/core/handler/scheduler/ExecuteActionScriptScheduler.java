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
import com.infosys.ainauto.docwb.engine.core.stereotype.ActionScriptExecutor;
import com.infosys.ainauto.docwb.engine.core.template.action.script.ActionScriptExecutorBase;

@Component
public class ExecuteActionScriptScheduler {

	@Autowired
	private ApplicationContext context;

	@Autowired
	private IMetricsService metricsService;

	@Autowired
	private Environment environment;

	private static final String PROP_NAME_ACTION_SCRIPT_EXECUTOR_TRIGGER_FREQUENCY = "action.script.executor.interval.milliseconds";
	private static final String PROP_NAME_ACTION_SCRIPT_EXECUTOR_ENABLED = "action.script.executor.enabled";
	private static Logger logger = LoggerFactory.getLogger(ExecuteActionScriptScheduler.class);

	@Scheduled(fixedDelayString = "${action.script.executor.interval.milliseconds}")
	public void scheduleFixedDelayTask() {
		boolean isFeatureEnabled = Boolean
				.parseBoolean(environment.getProperty(PROP_NAME_ACTION_SCRIPT_EXECUTOR_ENABLED));
		if (!isFeatureEnabled) {
			return;
		}

		// MDC Logging
		MDC.put(DocwbEngineCoreConstants.MDC_SCHEDULER_NAME,
				"ExecuteActScriptSch" + "-" + StringUtility.generateTransactionId());

		logger.info("Triggering new ExecuteActionScript event [Frequency=Every {} milliseconds]", StringUtility
				.sanitizeReqData(environment.getProperty(PROP_NAME_ACTION_SCRIPT_EXECUTOR_TRIGGER_FREQUENCY)));

		Map<String, Object> beanMap = context.getBeansWithAnnotation(ActionScriptExecutor.class);

		for (String key : beanMap.keySet()) {
			ActionScriptExecutorBase actionExecutor = (ActionScriptExecutorBase) beanMap.get(key);
			ActionScriptExecutor actionExecutorAnnotation = actionExecutor.getClass()
					.getAnnotation(ActionScriptExecutor.class);
			String title = actionExecutorAnnotation.title();
			String jsonConfigFile = actionExecutorAnnotation.jsonConfigFile();
			int retryCount = actionExecutorAnnotation.retryCount();
			JsonObject jsonObject = null;
			if (StringUtility.hasTrimmedValue(jsonConfigFile)) {
				jsonObject = FileUtility.readJsonAsObject(jsonConfigFile);
			}
			try {
				actionExecutor.execute(title, jsonObject, retryCount);
			} catch (Exception e) {
				logger.error("Error occured while executing ActionExecutor", e);
			}
		}
		metricsService.startTimer(EnumMetric.WORKFLOW_SCHEDULER_END_ELAPSED_TIME,
				EnumExecutorType.ACTION_SCRIPT_EXECUTOR, true);
	}

}
