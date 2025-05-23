/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


package com.infosys.ainauto.docwb.service.process.variable;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.dao.variable.IVariableDataAccess;
import com.infosys.ainauto.docwb.service.model.api.AppVarReqData;
import com.infosys.ainauto.docwb.service.model.api.AppVarResData;
import com.infosys.ainauto.docwb.service.model.db.AppVariableDbData;
import com.infosys.ainauto.docwb.service.model.db.EntityDbData;

@Component
public class VariableProcess implements IVariableProcess {

	@Autowired
	private IVariableDataAccess variableDataAccess;

	@Override
	public List<EntityDbData> updateAppVariableValue(AppVarReqData appVarReqData) throws WorkbenchException {
		List<EntityDbData> entityDbDataList = new ArrayList<EntityDbData>();
		long prevId = variableDataAccess.deleteAppVariable(appVarReqData.getPrevAppVarId());
		if (prevId > 0) {
			long newId = variableDataAccess.insertAppVariable(appVarReqData.getAppVarKey(),
					appVarReqData.getAppVarValue());
			EntityDbData entityDbData = new EntityDbData();
			if (newId > 0) {
				entityDbData.setAppVariableId(newId);
				entityDbData.setAppVariableKey(appVarReqData.getAppVarKey());
			}
			entityDbDataList.add(entityDbData);
		}
		return entityDbDataList;
	}

	@Override
	public AppVarResData getAppVariableData(String appVarKey) throws WorkbenchException {
		List<AppVariableDbData> appVariableDbDataList = variableDataAccess.getAppVariableDataFor(appVarKey, false);
		AppVarResData appVarResData = new AppVarResData();
		if (appVariableDbDataList != null && appVariableDbDataList.size()>0) {
			AppVariableDbData appVariableDbData = appVariableDbDataList.get(0);
			appVarResData.setAppVarId(appVariableDbData.getAppVarId());
			appVarResData.setAppVarKey(appVariableDbData.getAppVarKey());
			appVarResData.setAppVarValue(appVariableDbData.getAppVarValue());
		}
		return appVarResData;
	}
}
