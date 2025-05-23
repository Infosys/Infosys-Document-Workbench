/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.process.action;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.docwb.engine.core.stereotype.ActionScriptExecutor;
import com.infosys.ainauto.docwb.engine.core.template.action.script.ActionScriptExecutorBase;

@Component
@ActionScriptExecutor(title = "SEM Action Script Executor", jsonConfigFile = "actionScriptMappingConfig.json", retryCount = 3)
public class SemActionScriptExecutor extends ActionScriptExecutorBase {

	private static Logger logger = LoggerFactory.getLogger(SemActionScriptExecutor.class);

	@PostConstruct
	private void init() {
		logger.debug("Initialized SemActionScriptExecutor");

	}

}
