/** =============================================================================================================== *
 * Copyright 2022 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


package com.infosys.ainauto.docwb.ui.process.query;

import javax.json.JsonObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.infosys.ainauto.docwb.ui.common.DocWorkbenchUIException;
import com.infosys.ainauto.docwb.ui.model.api.query.QueryReqData;

public interface IQueryProcess {

	JsonObject executeQuery(QueryReqData queryReqData) throws DocWorkbenchUIException, JsonProcessingException;

}
