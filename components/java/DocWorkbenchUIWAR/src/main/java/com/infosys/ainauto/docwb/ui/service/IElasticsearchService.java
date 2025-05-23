/** =============================================================================================================== *
 * Copyright 2022 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


package com.infosys.ainauto.docwb.ui.service;

import javax.json.JsonObject;

import com.infosys.ainauto.docwb.ui.common.DocWorkbenchUIException;

public interface IElasticsearchService {
	JsonObject postQuery(String hostUrl, String bodyJsonStr) throws DocWorkbenchUIException;
}
