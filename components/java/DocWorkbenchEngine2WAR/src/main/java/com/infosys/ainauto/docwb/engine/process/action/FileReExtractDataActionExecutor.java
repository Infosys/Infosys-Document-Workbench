/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.process.action;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.infosys.ainauto.docwb.engine.core.stereotype.ExtractAttributeActionExecutor;
import com.infosys.ainauto.docwb.engine.core.template.action.attribute.ExtractAttributeActionExecutorBase;

@Component
@ExtractAttributeActionExecutor(title = "Re-Extract Data - File", jsonConfigFile = "attributeRuleMappingConfig.json")
public class FileReExtractDataActionExecutor extends ExtractAttributeActionExecutorBase {

	@PostConstruct
	private void init() {

	}

}
