/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.web.api;

import java.util.List;

import com.infosys.ainauto.docwb.web.data.ActionTempMappingData;

public interface ITemplateService {
	
	List<ActionTempMappingData> getFlattenedTemplates(long docId);

}
