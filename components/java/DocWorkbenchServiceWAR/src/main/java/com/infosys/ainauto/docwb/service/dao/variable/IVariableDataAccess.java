/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.dao.variable;

import java.util.List;

import org.springframework.stereotype.Component;

import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.model.db.AppVariableDbData;

@Component
public interface IVariableDataAccess {

	long deleteAppVariable(long rbacId) throws WorkbenchException;

	long insertAppVariable(String appVarKey, String appVarVal) throws WorkbenchException;

	List<AppVariableDbData> getAppVariableDataFor(String appVarKey, boolean isFetchAll) throws WorkbenchException;

}
