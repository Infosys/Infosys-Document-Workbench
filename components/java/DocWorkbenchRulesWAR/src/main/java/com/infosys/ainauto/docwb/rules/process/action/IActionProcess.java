/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.rules.process.action;

import java.util.List;

import com.infosys.ainauto.docwb.rules.common.DocWbRulesException;
import com.infosys.ainauto.docwb.rules.model.api.action.GetRecommendedActionResData;
import com.infosys.ainauto.docwb.rules.model.domain.DocumentData;

public interface IActionProcess {

	List<GetRecommendedActionResData> getRecommendedAction(String tenantId, DocumentData documentData)
			throws DocWbRulesException;

}
