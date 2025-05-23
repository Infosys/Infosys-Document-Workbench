/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.rules.process.attribute;

import com.infosys.ainauto.docwb.rules.common.DocWbRulesException;
import com.infosys.ainauto.docwb.rules.model.domain.DocumentData;

public interface IAttributeProcess {

	public DocumentData getAttributesNotification(String tenantId, DocumentData documentData)
			throws DocWbRulesException;

	public Object getAttributeAttributeMapping(String tenantId) throws DocWbRulesException;
	
	public Object getAttributeSortingKey(String tenantId) throws DocWbRulesException;

}
