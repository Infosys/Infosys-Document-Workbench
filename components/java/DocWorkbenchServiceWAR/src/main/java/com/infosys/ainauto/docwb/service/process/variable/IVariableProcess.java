/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.process.variable;

import java.util.List;

import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.model.api.AppVarReqData;
import com.infosys.ainauto.docwb.service.model.api.AppVarResData;
import com.infosys.ainauto.docwb.service.model.db.EntityDbData;

public interface IVariableProcess {

	List<EntityDbData> updateAppVariableValue(AppVarReqData appVarReqData) throws WorkbenchException;

	AppVarResData getAppVariableData(String appVarKey) throws WorkbenchException;
}
