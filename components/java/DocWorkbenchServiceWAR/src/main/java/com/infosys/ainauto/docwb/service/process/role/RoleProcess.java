/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.process.role;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.dao.role.IRoleDataAccess;
import com.infosys.ainauto.docwb.service.model.api.role.AddRoleReqData;
import com.infosys.ainauto.docwb.service.model.api.role.DeleteRoleReqData;
import com.infosys.ainauto.docwb.service.model.db.EntityDbData;
import com.infosys.ainauto.docwb.service.model.db.UserRoleDbData;

@Component
public class RoleProcess implements IRoleProcess {
	@Autowired
	private IRoleDataAccess roleDataAccess;

	@Override
	public List<EntityDbData> addNewRole(AddRoleReqData roleRequestData) throws WorkbenchException {
		UserRoleDbData userRoleDbData = new UserRoleDbData();
		List<EntityDbData> entityDbDataList = new ArrayList<EntityDbData>();
		EntityDbData prevEntityDbData = new EntityDbData();
		EntityDbData latestEntityDbData = new EntityDbData();
		List<Long> appUserRoleRelIdList = new ArrayList<Long>();

		List<UserRoleDbData> roleList = getRole(roleRequestData.getAppUserId());
		userRoleDbData.setAppUserId(roleRequestData.getAppUserId());
		int userRoleTypeCde = roleRequestData.getUserRoleType();

		if (roleList.isEmpty()) { // if the user isn't assigned any role at all
			userRoleDbData.setUserRoleTypeCde(userRoleTypeCde);
			appUserRoleRelIdList.add(roleDataAccess.insertUserRole(userRoleDbData));
			latestEntityDbData.setAppUserRoleRelIdList(appUserRoleRelIdList);
			entityDbDataList.add(latestEntityDbData);

		} else {
			for (int j = 0; j < roleList.size(); j++) {
				List<Long> deleteUserRoleList = new ArrayList<Long>();
				if (roleList.get(j).getUserRoleTypeCde() == userRoleTypeCde) { // if the user is already assigned the req
																			// role
					appUserRoleRelIdList.add(0l);
					latestEntityDbData.setAppUserRoleRelIdList(appUserRoleRelIdList);
					entityDbDataList.add(latestEntityDbData);

				} else { // if the user is assigned a role diff than that req
					long roleTypeCde = roleList.get(j).getUserRoleTypeCde();
					userRoleDbData.setUserRoleTypeCde((int) roleTypeCde);
					long deleteUserRole = roleDataAccess.deleteUserRole(userRoleDbData);
					deleteUserRoleList.add(deleteUserRole);
					prevEntityDbData.setAppUserRoleRelIdList(deleteUserRoleList);

					userRoleDbData.setUserRoleTypeCde(userRoleTypeCde);
					appUserRoleRelIdList.add(roleDataAccess.insertUserRole(userRoleDbData));
					latestEntityDbData.setAppUserRoleRelIdList(appUserRoleRelIdList);

					entityDbDataList.add(latestEntityDbData);
					entityDbDataList.add(prevEntityDbData);
				}
			}
		}
		return entityDbDataList;

	}

	@Override
	public List<Long> deleteRole(List<DeleteRoleReqData> deleteRoleReqDataList) throws WorkbenchException {
		UserRoleDbData userRoleDbData = new UserRoleDbData();
		List<Long> appUserRoleRelIdList = new ArrayList<>(); // AC

		for (int i = 0; i < deleteRoleReqDataList.size(); i++) {
			userRoleDbData.setAppUserId(deleteRoleReqDataList.get(i).getAppUserId());
			userRoleDbData.setUserRoleTypeCde(deleteRoleReqDataList.get(i).getUserRoleType());
			appUserRoleRelIdList.add(i, roleDataAccess.deleteUserRole(userRoleDbData));
		}
		return appUserRoleRelIdList;
	}

	@Override
	public List<UserRoleDbData> getRole(int appUserId) throws WorkbenchException {
		return roleDataAccess.getRole(appUserId);
	}

}
