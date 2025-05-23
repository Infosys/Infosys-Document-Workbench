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
import com.infosys.ainauto.docwb.engine.core.stereotype.AttributeExtractor;
import com.infosys.ainauto.docwb.engine.core.template.attribute.AttributeExtractorBase;

@Component
public class ExtractAttributeScheduler {

	@Autowired
	private ApplicationContext context;

	@Autowired
	private IMetricsService metricsService;

	@Autowired
	private Environment environment;

	private static final String PROP_NAME_EXTRACTOR_TRIGGER_FREQUENCY = "attribute.extractor.interval.milliseconds";
	private static final String PROP_NAME_EXTRACTOR_ENABLED = "attribute.extractor.enabled";
	private static Logger logger = LoggerFactory.getLogger(ExtractAttributeScheduler.class);

	@Scheduled(fixedDelayString = "${attribute.extractor.interval.milliseconds}")
	public void scheduleFixedDelayTask() {
		boolean isFeatureEnabled = Boolean.parseBoolean(environment.getProperty(PROP_NAME_EXTRACTOR_ENABLED));
		if (!isFeatureEnabled) {
			return;
		}

		// MDC Logging
		MDC.put(DocwbEngineCoreConstants.MDC_SCHEDULER_NAME,
				"ExtractAttrSch" + "-" + StringUtility.generateTransactionId());

		logger.info("Triggering new attrExtractor event [Frequency=Every {} milliseconds]",
				StringUtility.sanitizeReqData(environment.getProperty(PROP_NAME_EXTRACTOR_TRIGGER_FREQUENCY)));

		Map<String, Object> beanMap = context.getBeansWithAnnotation(AttributeExtractor.class);

		for (String key : beanMap.keySet()) {
			AttributeExtractorBase attributeExtractor = (AttributeExtractorBase) beanMap.get(key);
			AttributeExtractor attributeExtractorAnnotation = attributeExtractor.getClass()
					.getAnnotation(AttributeExtractor.class);
			String title = attributeExtractorAnnotation.title();
			String propertiesFile = attributeExtractorAnnotation.propertiesFile();
			Properties properties = new Properties();
			if (StringUtility.hasTrimmedValue(propertiesFile)) {
				properties = FileUtility.readProperties(propertiesFile);
			}
			try {
				attributeExtractor.execute(title, properties);
			} catch (Exception e) {
				logger.error("Error occured while executing AttributeExtractor", e);
			}
		}

		metricsService.startTimer(EnumMetric.WORKFLOW_SCHEDULER_END_ELAPSED_TIME, EnumExecutorType.ATTRIBUTE_EXTRACTOR,
				true);
	}

}
