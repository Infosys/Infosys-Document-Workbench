/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.core.rule;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public abstract class AttributeExtractRuleAsyncBase implements IAttributeExtractRuleAsync {

	/*** >>>>>> Asynchronous Extraction >>>>>> ***/
	public abstract void doExtractAsync(List<Object> objList, IAttributeExtractRuleListener attributeExtractRuleListener);

	public void doExtractAsync(Object object, IAttributeExtractRuleListener attributeExtractRuleListener) {
		List<Object> objectList = new ArrayList<Object>();
		objectList.add(object);

		doExtractAsync(objectList, attributeExtractRuleListener);
	};
	/*** <<<<<< Asynchronous Extraction <<<<<< ***/
}
