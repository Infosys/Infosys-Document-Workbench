/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.core.service.client;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.docwb.engine.core.common.type.EnumExecutorType;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.TimeGauge;

@Component
public class MetricsService implements IMetricsService {

	private static Logger LOGGER = LoggerFactory.getLogger(MetricsService.class);

	@Autowired
	private MeterRegistry meterRegistry;

	// Metric values need to be stored in memory without being
	// subjected to garbage collection
	private HashMap<String, AtomicLong> gaugesMap = new HashMap<>();
	private HashMap<String, TimeKeeper> timeKeeperMap = new HashMap<>();

	public MetricsService() {
		LOGGER.info("Metrics Service initialized");
	}

	@Override
	public void updateValue(EnumMetric enumMetric, long metricValue, EnumExecutorType enumExecutorType,
			String instanceName) {
		String keyName = enumExecutorType.getTextValue() + "-" + instanceName + "-" + enumMetric.getNameValue();
		AtomicLong atomicValue = gaugesMap.get(keyName);
		if (atomicValue == null) {
			atomicValue = new AtomicLong();
			atomicValue.set(metricValue);
			String tagPrefix = String.valueOf(enumExecutorType.getCdeValue()) + "-";
			Gauge.builder(enumMetric.getNameValue(), atomicValue, AtomicLong::get)
					.description(enumMetric.getDecriptionValue()).tags(Tags.of("ExecutorType",
							tagPrefix + enumExecutorType.getTextValue(), "InstanceName", tagPrefix + instanceName))
					.register(meterRegistry);

			gaugesMap.put(keyName, atomicValue);
		} else {
			atomicValue.set(metricValue);
		}
	}

	@Override
	public void startTimer(EnumMetric enumMetric, EnumExecutorType enumExecutorType, boolean doRestart) {
		String keyName = enumExecutorType.getTextValue() + "-" + enumMetric.getNameValue();
		TimeKeeper timeKeeper = timeKeeperMap.get(keyName);
		if (timeKeeper == null) {
			timeKeeper = new TimeKeeper();
			timeKeeper.startTimer();
			String tagPrefix = String.valueOf(enumExecutorType.getCdeValue()) + "-";
			TimeGauge.builder(enumMetric.getNameValue(), timeKeeper, TimeUnit.SECONDS, TimeKeeper::getLapTimeInSecs)
					.description(enumMetric.getDecriptionValue())
					.tag("ExecutorType", tagPrefix + enumExecutorType.getTextValue()).register(meterRegistry);

			timeKeeperMap.put(keyName, timeKeeper);
		} else if (doRestart) {
			timeKeeper.restartTimer();
		}
	}

	public static class TimeKeeper {
		private long startTime;
		private double elapsedTime;

		public void startTimer() {
			this.startTime = System.nanoTime();
		}

		public void restartTimer() {
			startTimer();
		}

		public TimeKeeper stopTimer() {
			this.elapsedTime = calculateElapsedTime();
			return this;
		}

		public double getLapTimeInSecs() {
			return calculateElapsedTime();
		}

		public double getElapsedTimeInSecs() {
			return this.elapsedTime;
		}

		private double calculateElapsedTime() {
			return Math.round(((System.nanoTime() - this.startTime) / 1000000000.0) * 1_000.0) / 1_000.0;
		}
	}

	public enum EnumMetric {
		WORKFLOW_SCHEDULER_END_ELAPSED_TIME(1, "docwb.engine.workflow.scheduler.end.elapsed.time",
				"Time elapsed since completion of last run of scheduler"),
		WORKFLOW_EXECUTOR_START_ELAPSED_TIME(2, "docwb.engine.workflow.executor.start.elapsed.time",
				"Time elapsed since start of current run of executor"),
		WORKFLOW_EXECUTOR_END_ELAPSED_TIME(3, "docwb.engine.workflow.executor.end.elapsed.time",
				"Time elapsed since completion of last run of executor");

		private int cde;
		private String name;
		private String description;

		private EnumMetric(int cde, String name, String description) {
			this.cde = cde;
			this.name = name;
			this.description = description;
		}

		public int getCdeValue() {
			return cde;
		}

		public String getNameValue() {
			return this.name;
		}

		public String getDecriptionValue() {
			return this.description;
		}
	}
}
