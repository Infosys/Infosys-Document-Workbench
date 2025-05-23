/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.rules.process.template;

import java.util.List;

import com.infosys.ainauto.docwb.rules.common.DocWbRulesException;
import com.infosys.ainauto.docwb.rules.model.api.template.FlattenedTemplateResData;
import com.infosys.ainauto.docwb.rules.model.domain.DocumentData;
import com.infosys.ainauto.docwb.rules.model.domain.TemplateData;

public interface ITemplateProcess {

//	TemplateData getRecommendedTemplateName(String tenantId, DocumentData documentData)
//			throws DocWbRulesException;
	
	List<TemplateData> getTemplateList(String tenantId) throws DocWbRulesException;
	
	List<FlattenedTemplateResData> getFlattenedTemplates(String tenantId, DocumentData documentData)
			throws DocWbRulesException;
}
