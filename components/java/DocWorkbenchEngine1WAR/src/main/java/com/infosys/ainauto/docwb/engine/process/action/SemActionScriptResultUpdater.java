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

import com.infosys.ainauto.docwb.engine.core.stereotype.ActionScriptResultUpdater;
import com.infosys.ainauto.docwb.engine.core.template.action.script.ActionScriptResultUpdaterBase;

@Component
@ActionScriptResultUpdater(title = "SEM Action Script Result Updater")
public class SemActionScriptResultUpdater extends ActionScriptResultUpdaterBase {

	private static Logger logger = LoggerFactory.getLogger(SemActionScriptResultUpdater.class);

	@PostConstruct
	private void init() {
		logger.debug("Initialized SemActionScriptResultUpdater");
	}
}
