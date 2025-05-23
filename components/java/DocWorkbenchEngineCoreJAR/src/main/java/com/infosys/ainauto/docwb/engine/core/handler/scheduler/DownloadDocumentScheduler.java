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
import com.infosys.ainauto.docwb.engine.core.stereotype.DocumentDownloader;
import com.infosys.ainauto.docwb.engine.core.template.download.DocumentDownloaderBase;

@Component
public class DownloadDocumentScheduler {

	@Autowired
	private ApplicationContext context;

	@Autowired
	private IMetricsService metricsService;

	@Autowired
	private Environment environment;

	private static final String PROP_NAME_DOWNLOADER_TRIGGER_FREQUENCY = "document.downloader.interval.milliseconds";
	private static final String PROP_NAME_DOWNLOADER_ENABLED = "document.downloader.enabled";
	private static Logger logger = LoggerFactory.getLogger(DownloadDocumentScheduler.class);

	@Scheduled(fixedDelayString = "${document.downloader.interval.milliseconds}")
	public void scheduleFixedDelayTask() {
		boolean isFeatureEnabled = Boolean.parseBoolean(environment.getProperty(PROP_NAME_DOWNLOADER_ENABLED));
		if (!isFeatureEnabled) {
			return;
		}

		// MDC Logging
		MDC.put(DocwbEngineCoreConstants.MDC_SCHEDULER_NAME,
				"DownloadDocSch" + "-" + StringUtility.generateTransactionId());

		logger.info("Triggering new DocDownloader event [Frequency=Every {} milliseconds]",
				StringUtility.sanitizeReqData(environment.getProperty(PROP_NAME_DOWNLOADER_TRIGGER_FREQUENCY)));

		Map<String, Object> beanMap = context.getBeansWithAnnotation(DocumentDownloader.class);

		for (String key : beanMap.keySet()) {
			@SuppressWarnings("rawtypes") // Added on purpose
			DocumentDownloaderBase documentDownloader = (DocumentDownloaderBase) beanMap.get(key);
			DocumentDownloader documentDownloaderAnnotation = documentDownloader.getClass()
					.getAnnotation(DocumentDownloader.class);
			String title = documentDownloaderAnnotation.title();
			String propertiesFile = documentDownloaderAnnotation.propertiesFile();
			Properties properties = new Properties();
			if (StringUtility.hasTrimmedValue(propertiesFile)) {
				properties = FileUtility.readProperties(propertiesFile);
			}
			try {
				documentDownloader.execute(title, properties);
			} catch (Exception e) {
				logger.error("Error occured while executing DocumentDownloader", e);
			}
		}

		metricsService.startTimer(EnumMetric.WORKFLOW_SCHEDULER_END_ELAPSED_TIME, EnumExecutorType.DOCUMENT_DOWNLOADER,
				true);
	}

}
