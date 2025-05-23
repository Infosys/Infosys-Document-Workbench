/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class SpringConfig {

	private static final String PROP_NAME_CORE_THREAD_POOL_COUNT = "core.thread.pool.count";
	private static final String PROP_NAME_MAX_THREAD_POOL_COUNT = "max.thread.pool.count";

	@Autowired
	private Environment environment;

	/*
	 * The TaskExecutor customization below was added to make no. of threads configurable
	 * for @async methods 
	 */
	@Bean
	public TaskExecutor threadPoolTaskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(Integer.parseInt(environment.getProperty(PROP_NAME_CORE_THREAD_POOL_COUNT)));
		executor.setMaxPoolSize(Integer.parseInt(environment.getProperty(PROP_NAME_MAX_THREAD_POOL_COUNT)));
		executor.setThreadNamePrefix("default_task_executor_thread");
		executor.initialize();
		return executor;
	}

}
