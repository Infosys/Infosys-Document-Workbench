/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import com.infosys.ainauto.docwb.web.api.DocWbApiFactory;

@Configuration
@EnableScheduling
public class SpringConfig implements SchedulingConfigurer , ApplicationListener<ContextClosedEvent> {

	private static final Logger logger = LoggerFactory.getLogger(DocWbApiFactory.class);
	
	@Autowired
	private Environment environment;
	
	private ThreadPoolTaskScheduler scheduler;

	private static final String PROP_NAME_SCHEDULER_THREAD_POOL_COUNT = "scheduler.thread.pool.count";
	private static final String SCHEDULER_THREAD_POOL_NAME_PREFIX = "Scheduled-Task-Pool-";

	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		scheduler = new ThreadPoolTaskScheduler();
		scheduler.setPoolSize(Integer.parseInt(environment.getProperty(PROP_NAME_SCHEDULER_THREAD_POOL_COUNT)));
		scheduler.setThreadNamePrefix(SCHEDULER_THREAD_POOL_NAME_PREFIX);
		scheduler.setWaitForTasksToCompleteOnShutdown(true);
		scheduler.initialize();
		taskRegistrar.setTaskScheduler(scheduler);
	}

	@Override
	public void onApplicationEvent(ContextClosedEvent event) {
		scheduler.shutdown();
		logger.info("Stopped scheduler thread pool");
	}

	/*
	 * The TaskExecutor customization below was added to make no. of threads
	 * configurable
	 * 
	 *  Commented async block of code as it not required for current fixedDelay
	 * scheduler
	 */
	// private static final String PROP_NAME_CORE_THREAD_POOL_COUNT =
	// "core.thread.pool.count";
	// private static final String PROP_NAME_MAX_THREAD_POOL_COUNT =
	// "max.thread.pool.count";
	// @Bean
	// public TaskExecutor threadPoolTaskExecutor() {
	// ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
	// executor.setCorePoolSize(Integer.parseInt(environment.getProperty(PROP_NAME_CORE_THREAD_POOL_COUNT)));
	// executor.setMaxPoolSize(Integer.parseInt(environment.getProperty(PROP_NAME_MAX_THREAD_POOL_COUNT)));
	// executor.setThreadNamePrefix("default_task_executor_thread");
	// executor.initialize();
	// return executor;
	// }
	// @Bean(destroyMethod = "shutdown")
	// public Executor taskScheduler() {
	// int threadPoolCount =
	// Integer.parseInt(environment.getProperty(PROP_NAME_SCHEDULER_THREAD_POOL_COUNT));
	// logger.info("Scheduled thread pool count = {}", threadPoolCount);
	// return Executors.newScheduledThreadPool(threadPoolCount);
	// }

}
