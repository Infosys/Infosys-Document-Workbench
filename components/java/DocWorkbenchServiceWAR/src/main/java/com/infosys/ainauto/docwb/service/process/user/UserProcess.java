/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.process.user;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.docwb.service.common.InvalidTenantIdException;
import com.infosys.ainauto.docwb.service.common.SessionHelper;
import com.infosys.ainauto.docwb.service.common.WorkbenchConstants;
import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.controller.api.BaseController;
import com.infosys.ainauto.docwb.service.dao.user.IUserDataAccess;
import com.infosys.ainauto.docwb.service.model.api.UserQueueResData;
import com.infosys.ainauto.docwb.service.model.api.UserResData;
import com.infosys.ainauto.docwb.service.model.api.UserTeammateResData;
import com.infosys.ainauto.docwb.service.model.api.user.InsertUserQueueReqData;
import com.infosys.ainauto.docwb.service.model.api.user.InsertUserReqData;
import com.infosys.ainauto.docwb.service.model.api.user.UpdateUserReqData;
import com.infosys.ainauto.docwb.service.model.db.AppUserDbData;
import com.infosys.ainauto.docwb.service.model.db.EntityDbData;
import com.infosys.ainauto.docwb.service.model.db.UserQueueDbData;
import com.infosys.ainauto.docwb.service.model.db.UserRoleDbData;
import com.infosys.ainauto.docwb.service.model.db.UserTeammateDbData;
import com.infosys.ainauto.docwb.service.model.process.AppUserData;
import com.infosys.ainauto.docwb.service.model.security.UserDetailsData;
import com.infosys.ainauto.docwb.service.model.security.IdmsConfigData.TenantConfigData;
import com.infosys.ainauto.docwb.service.service.idms.IIdentityManagementSystemService;

@Component
public class UserProcess extends BaseController implements IUserProcess {

	@Autowired
	private IUserDataAccess userDataAccess;

	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private IIdentityManagementSystemService identityManagementSystemService;

	@Override
	public EntityDbData addUser(InsertUserReqData insertUserReqData, String tenantId) throws WorkbenchException {
		long userId = 0;
		EntityDbData entityDbData = new EntityDbData();
		AppUserDbData appUserDbData = new AppUserDbData();
		appUserDbData.setUserLoginId(insertUserReqData.getUserName());
		appUserDbData.setUserPassword(insertUserReqData.getUserPassword());
		appUserDbData.setUserEmail(insertUserReqData.getUserEmail());
		appUserDbData.setUserFullName(insertUserReqData.getUserFullName());
		appUserDbData.setUserTypeCde(insertUserReqData.getUserTypeCde());
		if (tenantId.length() > 0 && tenantId != null) {
			appUserDbData.setTenantId(tenantId);
		} else {
			throw new InvalidTenantIdException(WorkbenchConstants.INVALID_TENANT_ID);
		}
		List<String> tenants = userDataAccess.getTenants();
		if (tenants.contains(appUserDbData.getTenantId())) {
			userId = userDataAccess.insertUser(appUserDbData);
			if (userId > 0) {
				entityDbData.setAppUserId(userId);
				entityDbData.setTenantId(appUserDbData.getTenantId());
			}
		} else {
			throw new InvalidTenantIdException(WorkbenchConstants.INVALID_TENANT_ID);
		}
		return entityDbData;
	}

	public UserResData getLoggedInUserDetails() throws WorkbenchException {
		UserDetailsData userDetailsData = SessionHelper.getLoginUserData();
		UserResData userResData = new UserResData();
		if (userDetailsData != null) {
			userResData.setUserName(userDetailsData.getUsername());
			userResData.setUserFullName(userDetailsData.getUserFullName());
			userResData.setUserEmail(userDetailsData.getUserEmail());
			userResData.setUserTypeCde(userDetailsData.getUserTypeCde());
			userResData.setUserTypeTxt(userDetailsData.getUserTypeTxt());
			userResData.setUserId(userDetailsData.getUserId());
			userResData.setRoleTypeCde(userDetailsData.getRoleTypeCde());
			userResData.setRoleTypeTxt(userDetailsData.getRoleTypeTxt());
			userResData.setQueueDataList(getUserQueueDetails(userDetailsData.getUserId()));
			userResData.setUserPassword(userDetailsData.getPassword());
			userResData.setTenantId(userDetailsData.getTenantId());
			if (!identityManagementSystemService.isLdapAuthEnabled())	
			{
				userResData.setUserSourceCde(WorkbenchConstants.USER_SOURCE_INTERNAL);
			}
			else {
				userResData.setUserSourceCde(WorkbenchConstants.USER_SOURCE_EXTERNAL);
				TenantConfigData tenantIdmsConfigData = identityManagementSystemService.getTenantConfigData(userDetailsData.getTenantId());
				
				if (null != tenantIdmsConfigData && tenantIdmsConfigData.getDbAuthentication().contains(userDetailsData.getUsername())) {
					userResData.setUserSourceCde(WorkbenchConstants.USER_SOURCE_INTERNAL);	
				}
			}
						
		}
		return userResData;
	}

	@Override
	public long addUserToQueue(InsertUserQueueReqData insertUserQueueReqData) throws WorkbenchException {

		UserQueueDbData userQueueDbData = new UserQueueDbData();
		userQueueDbData.setAppUserId(insertUserQueueReqData.getAppUserId());			
		userQueueDbData.setQueueNameCde(insertUserQueueReqData.getQueueNameCde());
		return userDataAccess.insertUserQueueRel(userQueueDbData);
	}

	@Override
	public long deleteUserFromQueue(long appUserQueueRelId) throws WorkbenchException {
		return userDataAccess.deleteUserQueueRel(appUserQueueRelId);
	}

	@Override
	public List<UserQueueResData> getUserQueueDetails(long appUserId) throws WorkbenchException {
		List<UserQueueResData> userQueueResDataList = new ArrayList<>();
		List<UserQueueDbData> userQueueDbDataList = userDataAccess.getUserQueueDetails(appUserId);

		for (UserQueueDbData userQueueDbData : userQueueDbDataList) {
			UserQueueResData userQueueResData = new UserQueueResData();
			userQueueResData.setAppUserId(appUserId);
			userQueueResData.setQueueNameCde(userQueueDbData.getQueueNameCde());
			userQueueResData.setQueueNameTxt(userQueueDbData.getQueueNameTxt());
			userQueueResData.setAppUserQueueRelId(userQueueDbData.getAppUserQueueRelId());
			userQueueResData.setDocTypeCde(userQueueDbData.getDocTypeCde());
			userQueueResData.setDocTypeTxt(userQueueDbData.getDocTypeTxt());
			userQueueResData.setQueueClosedDtm(userQueueDbData.getQueueClosedDtm());
			userQueueResData.setQueueStatus(userQueueDbData.getQueueStatus());
			userQueueResData.setQueueHideAfterDtm(userQueueDbData.getQueueHideAfterDtm());
			userQueueResData.setUserQueueHideAfterDtm(userQueueDbData.getUserQueueHideAfterDtm());
			userQueueResDataList.add(userQueueResData);
		}
		return userQueueResDataList;
	}

	@Override
	public List<UserResData> getUserListDetails() throws WorkbenchException {
		List<UserResData> userResDataList = new ArrayList<>();
		List<UserRoleDbData> userRoleDbDataList = userDataAccess.getUserListDetails();

		for (UserRoleDbData userRoleDbData : userRoleDbDataList) {
			UserResData userResData = new UserResData();
			userResData.setUserId(userRoleDbData.getAppUserId());
			userResData.setQueueDataList(getUserQueueDetails(userResData.getUserId()));
			userResData.setUserFullName((userRoleDbData.getUserFullName()));
			userResData.setUserEmail(userRoleDbData.getUserEmail());
			userResData.setUserName(userRoleDbData.getUserLoginId());
			userResData.setUserTypeTxt(userRoleDbData.getUserTypeTxt());
			userResData.setUserTypeCde(userRoleDbData.getUserTypeCde());
			userResData.setAccountEnabled(userRoleDbData.getAccountEnabled());
			userResData.setRoleTypeTxt(userRoleDbData.getUserRoleTypeTxt());
			userResData.setRoleTypeCde(userRoleDbData.getUserRoleTypeCde());
			userResDataList.add(userResData);
		}
		return userResDataList;

	}

	@Override
	public EntityDbData updateUserAccountEnabled(UpdateUserReqData updateUserReqData) throws WorkbenchException {
		AppUserDbData appUserDbData = new AppUserDbData();
		appUserDbData.setAppUserId(updateUserReqData.getUserId());
		appUserDbData.setAccountEnabled(updateUserReqData.isAccountEnabled());
		return userDataAccess.updateUserAccount(appUserDbData);

	}

	@Override
	public EntityDbData changePassword(String oldPassword, String newPassword) throws WorkbenchException {
		long appUserId = 0;
		EntityDbData entityDbData = new EntityDbData();
		UserResData userResData = new UserResData();
		userResData = getLoggedInUserDetails();
		boolean isPasswordCorrect = passwordEncoder.matches(oldPassword, userResData.getUserPassword());
		String apiResponse = "";
		if (userResData.getUserSourceCde() == WorkbenchConstants.USER_SOURCE_EXTERNAL) {
			appUserId = WorkbenchConstants.PASSWORD_MANAGED_BY_LDAP;
			apiResponse = "LDAP user is not allowed to change password.";
		} else {
			if (isPasswordCorrect) { // checking current pwd is same as in DB or not

				if (!newPassword.equals(oldPassword)) { // checking new pwd and old pwd are not same
					String encodedNewPassword = passwordEncoder.encode(newPassword);
					appUserId = userDataAccess.changePassword(userResData.getUserId(), encodedNewPassword);

					if (appUserId > 0) // when pwd is successfully changed
						apiResponse = appUserId + " appUserId was updated";
				} else { // when new pwd and old pwd are same
					appUserId = WorkbenchConstants.PASSWORD_SAME_AS_EXISTING;
					apiResponse = "New password and Current password can not be same";
				}
			} else { // when current pwd entered doesn't match with the one in DB
				appUserId = WorkbenchConstants.PASSWORD_MISMATCH_EXISTING;
				apiResponse = "Current password entered is incorrect";
			}
		}
		entityDbData.setApiResponseData(apiResponse);
		entityDbData.setAppUserId(appUserId);

		return entityDbData;
	}
	
	public AppUserData getUserDetailsFromLoginId(String userLoginId, String tenantId) throws WorkbenchException {
		AppUserData appUserData = new AppUserData();
		AppUserDbData userDataFormLoginId = userDataAccess.getUserDetailsFromLoginId(userLoginId, tenantId);
		if (userDataFormLoginId != null) {
			appUserData.setAppUserId(userDataFormLoginId.getAppUserId());
			appUserData.setAppUserLoginId(userDataFormLoginId.getUserLoginId());
		}
		return appUserData;
	}
	
	private List<UserTeammateResData> isUserExist(List<UserTeammateResData> userTeammateResDataList ,UserTeammateDbData userTeammateDbData ) {
		return userTeammateResDataList.stream().filter(x-> x.getUserId()==userTeammateDbData.getAppUserId()&& 
				x.getRoleTypeCde()==userTeammateDbData.getUserRoleTypeCde()).collect(Collectors.toList());
		 
	}
	private UserQueueResData getQueuResData(UserTeammateDbData userTeammateDbData) {
		UserQueueResData userQueueResData = new UserQueueResData();
		userQueueResData.setQueueNameCde(userTeammateDbData.getQueueNameCde());
		userQueueResData.setQueueNameTxt(userTeammateDbData.getQueueNameTxt());
		userQueueResData.setDocTypeCde(userTeammateDbData.getDocTypeCde());
		userQueueResData.setDocTypeTxt(userTeammateDbData.getDocTypeTxt());
		return userQueueResData;
	}
	@Override
	public List<UserTeammateResData> getTeammateListDetails() throws WorkbenchException {
		TenantConfigData tenantIdmsConfigData = identityManagementSystemService.getTenantConfigData(SessionHelper.getTenantId());
		List<String> lowerCaseDbAuthenticationList=null;
		if (null != tenantIdmsConfigData) {
			lowerCaseDbAuthenticationList = tenantIdmsConfigData.getDbAuthentication().stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toList());
		}
		List<UserTeammateResData> userTeammateResDataList = new ArrayList<UserTeammateResData>();
		for (UserTeammateDbData userTeammateDbData : userDataAccess.getTeammateUserListDetails()) {
			List<UserTeammateResData> userTeammateResDataList1=isUserExist(userTeammateResDataList,userTeammateDbData);
			if(ListUtility.hasValue(userTeammateResDataList1)) {
				
				userTeammateResDataList1.get(0).getCommonQueueList().add(getQueuResData(userTeammateDbData));
			}else {
				UserTeammateResData userTeammateResData = new UserTeammateResData();
				userTeammateResData.setUserId(userTeammateDbData.getAppUserId());
				userTeammateResData.setUserFullName((userTeammateDbData.getUserFullName()));
				userTeammateResData.setUserEmail(userTeammateDbData.getUserEmail());
				userTeammateResData.setUserName(userTeammateDbData.getUserLoginId());
				userTeammateResData.setUserTypeTxt(userTeammateDbData.getUserTypeTxt());
				userTeammateResData.setUserTypeCde(userTeammateDbData.getUserTypeCde());
				userTeammateResData.setRoleTypeTxt(userTeammateDbData.getUserRoleTypeTxt());
				userTeammateResData.setRoleTypeCde(userTeammateDbData.getUserRoleTypeCde());
				if(lowerCaseDbAuthenticationList.contains(userTeammateResData.getUserName().toLowerCase())) {
					userTeammateResData.setUserSourceCde(WorkbenchConstants.USER_SOURCE_INTERNAL);
				}else
				{userTeammateResData.setUserSourceCde(WorkbenchConstants.USER_SOURCE_EXTERNAL);}
				List<UserQueueResData> userQueueResDataList = new ArrayList<>();
				userQueueResDataList.add(getQueuResData(userTeammateDbData));
				userTeammateResData.setCommonQueueList(userQueueResDataList);
				userTeammateResDataList.add(userTeammateResData);
			}
			
		}
		
		return userTeammateResDataList;

	}

}

