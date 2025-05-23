/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.core.rule;

import java.util.List;

public interface IAttributeExtractRuleAsync {

	/*** >>>>>> Asynchronous Extraction >>>>>> ***/
	public void doExtractAsync(List<Object> objList, IAttributeExtractRuleListener attributeExtractRuleListener);

	public void doExtractAsync(Object object, IAttributeExtractRuleListener attributeExtractRuleListener);
	/*** <<<<<< Asynchronous Extraction <<<<<< ***/
	
}
