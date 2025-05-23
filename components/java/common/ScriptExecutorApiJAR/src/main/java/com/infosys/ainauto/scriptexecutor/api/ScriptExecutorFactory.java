/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.scriptexecutor.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.infosys.ainauto.scriptexecutor.common.PropertyManager;

public final class ScriptExecutorFactory {

	private static final Logger logger = LoggerFactory.getLogger(ScriptExecutorFactory.class);

	private IScriptExecutorService scriptExecutionService;

	public ScriptExecutorFactory(String baseUrl) {
		PropertyManager.getInstance().setProperty("script.executor.base.url", baseUrl);
		scriptExecutionService = new ScriptExecutorService();
		logger.debug("New instance of ScriptExecutorFactory created");
	}

	public IScriptExecutorService getScriptExecutorService() {
		return scriptExecutionService;
	}

}
