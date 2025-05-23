/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.core.service.script;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.scriptexecutor.api.IScriptExecutorService;
import com.infosys.ainauto.scriptexecutor.api.ScriptExecutorFactory;

@Component
public class ScriptExecutorProxy {

	@Autowired
	private Environment environment;

	private static Logger logger = LoggerFactory.getLogger(ScriptExecutorProxy.class);
	private static final String PROP_NAME_SCRIPT_EXECUTOR_BASE_URL = "script.executor.base.url";

	private ScriptExecutorFactory scriptExecutorFactory;

	@PostConstruct
	private void init() {
		scriptExecutorFactory = new ScriptExecutorFactory(environment.getProperty(PROP_NAME_SCRIPT_EXECUTOR_BASE_URL));
		logger.info("Initialized");
	}

	
	public IScriptExecutorService getScriptExecutorService() {
		return scriptExecutorFactory.getScriptExecutorService();
	}
}
