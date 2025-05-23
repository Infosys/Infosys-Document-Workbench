/** =============================================================================================================== *
 * Copyright 2022 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


package com.infosys.ainauto.docwb.service.process.auth;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.service.common.InvalidTenantIdException;
import com.infosys.ainauto.docwb.service.common.WorkbenchConstants;
import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.common.type.EnumEntityType;
import com.infosys.ainauto.docwb.service.common.type.EnumOperationType;
import com.infosys.ainauto.docwb.service.dao.user.IUserDataAccess;
import com.infosys.ainauto.docwb.service.model.api.role.AddRoleReqData;
import com.infosys.ainauto.docwb.service.model.api.user.InsertUserQueueReqData;
import com.infosys.ainauto.docwb.service.model.api.user.InsertUserReqData;
import com.infosys.ainauto.docwb.service.model.api.user.UpdateUserReqData;
import com.infosys.ainauto.docwb.service.model.db.AppUserDbData;
import com.infosys.ainauto.docwb.service.model.db.EntityDbData;
import com.infosys.ainauto.docwb.service.model.security.IdmsConfigData.NewUserAccountSettingData;
import com.infosys.ainauto.docwb.service.model.security.IdmsConfigData.TenantConfigData;
import com.infosys.ainauto.docwb.service.model.security.UserDetailsData;
import com.infosys.ainauto.docwb.service.model.service.IdmsUserReqData;
import com.infosys.ainauto.docwb.service.model.service.IdmsUserResData;
import com.infosys.ainauto.docwb.service.process.audit.IAuditProcess;
import com.infosys.ainauto.docwb.service.process.role.IRoleProcess;
import com.infosys.ainauto.docwb.service.process.user.IUserProcess;
import com.infosys.ainauto.docwb.service.security.ValidAuthenticationToken;
import com.infosys.ainauto.docwb.service.service.idms.IIdentityManagementSystemService;

@Component
public class AuthenticationProcess implements IAuthenticationProcess {

	@Autowired
	private IUserDataAccess userDataAccess;

	@Autowired
	private IIdentityManagementSystemService identityManagementSystemService;

	@Autowired
	private IUserProcess userProcess;

	@Autowired
	private IAuditProcess auditProcess;

	@Autowired
	private IRoleProcess roleProcess;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private static final Logger logger = LoggerFactory.getLogger(AuthorizationProcess.class);

	@Override
	public boolean authenticateUser(String userName, String rawPassword, String tenantId) throws WorkbenchException {
		boolean isAuthenticated = false;
		if (tenantId == null || tenantId.length() == 0) {
			throw new InvalidTenantIdException(WorkbenchConstants.INVALID_TENANT_ID);
		}
		List<String> tenants = userDataAccess.getTenants();
		if (!tenants.contains(tenantId)) {
			throw new InvalidTenantIdException(WorkbenchConstants.INVALID_TENANT_ID);
		}
		AppUserDbData appUserDbData = userDataAccess.getUserData(userName, tenantId);
		boolean isDocwbDbAuthUser = false;
		TenantConfigData tenantIdmsConfigData = identityManagementSystemService.getTenantConfigData(tenantId);
		if (null != tenantIdmsConfigData) {
			List<String> lowerCaseDbAuthenticationList = tenantIdmsConfigData.getDbAuthentication().stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toList());
			if(lowerCaseDbAuthenticationList.contains(userName.toLowerCase())) {
				isDocwbDbAuthUser = true;
			}
			
		}
		if (!isDocwbDbAuthUser && identityManagementSystemService.isLdapAuthEnabled()) {
			logger.info("LDAP auth enabled");
//			If not valid a user then throws UsernameNotFoundException
			IdmsUserResData idmsUserResData = getLdapAuthUserData(userName, rawPassword);
			if (null == appUserDbData) {
				logger.info("Found new ldap authenticated user " + userName);
				manageLdapAuthUserData(idmsUserResData, userName, tenantId);
			}
			isAuthenticated = true;
		} else {
			if (appUserDbData == null) {
				logger.info("User not found in DB");
				throw new UsernameNotFoundException(userName);
			}
			logger.info("Authenticate User with DB");
			isAuthenticated = passwordEncoder.matches(rawPassword, appUserDbData.getUserPassword());
		}
		logger.info("Is User Authenticated " + isAuthenticated);
		return isAuthenticated;
	}

	private IdmsUserResData getLdapAuthUserData(String userName, String rawPassword) throws WorkbenchException {
		IdmsUserReqData idmsUserReqData = new IdmsUserReqData();
		idmsUserReqData.setNetid(userName);
		idmsUserReqData.setPassword(rawPassword);
		IdmsUserResData idmsUserResData = identityManagementSystemService.getLdapAuthData(idmsUserReqData);
//		Auth failed
		if (null == idmsUserResData) {
			throw new UsernameNotFoundException(userName);
		} else if (!StringUtility.hasTrimmedValue(idmsUserResData.getUserId())) {
			throw new AuthorizationServiceException(WorkbenchConstants.API_RESPONSE_MSG_NOT_AUTHORIZED);
		}
		return idmsUserResData;
	}

	private void manageLdapAuthUserData(IdmsUserResData idmsUserResData, String userName, String tenantId)
			throws WorkbenchException {
//		Set SecurityContext to do below operation for idms.
		setSecurityContext(userName, tenantId);
		long userId = insertNewLdapAuthUserToDB(tenantId, idmsUserResData);
		if (userId > 0) {
			NewUserAccountSettingData newUserAccountSettingData = identityManagementSystemService
					.getTenantConfigData(tenantId).getNewUserAccountSetting();
			if (newUserAccountSettingData.getAddRole().isEnabled())
				insertNewUserRoleToDB(userId, newUserAccountSettingData.getAddRole().getRoleTypeCde());
			if (newUserAccountSettingData.getAddQueue().isEnabled())
				insertNewUserQueueToDB(userId, newUserAccountSettingData.getAddQueue().getQueueNameCde());
			if (newUserAccountSettingData.isActivateAccountEnabled())
				updateUserAccountEnabled(userId);
		}
	}

	private void setSecurityContext(String userName, String tenantId) {
		AppUserDbData appUserDbData = new AppUserDbData();
		appUserDbData.setTenantId(tenantId);
		appUserDbData.setUserLoginId(userName);
		UserDetailsData userDetailsData = new UserDetailsData(appUserDbData);
		ValidAuthenticationToken validAuthenticationToken = new ValidAuthenticationToken(userDetailsData);
		SecurityContextHolder.getContext().setAuthentication(validAuthenticationToken);
	}

	private long insertNewLdapAuthUserToDB(String tenantId, IdmsUserResData idmsUserResData) throws WorkbenchException {
		logger.info("Add new user to DB" + idmsUserResData.getUserId());
		InsertUserReqData insertUserReqData = new InsertUserReqData();
		insertUserReqData.setTenantId(tenantId);
		insertUserReqData.setUserEmail(idmsUserResData.getEmailId());
		insertUserReqData.setUserFullName(idmsUserResData.getFullname());
		insertUserReqData.setUserName(idmsUserResData.getUserId());
		insertUserReqData.setUserTypeCde(1);
		TenantConfigData tenantIdmsConfigData = identityManagementSystemService.getTenantConfigData(tenantId);
		if (null != tenantIdmsConfigData
				&& tenantIdmsConfigData.getServiceAccountNames().contains(idmsUserResData.getUserId())) {
			insertUserReqData.setUserTypeCde(2);
		}

//		IMPORTANT: Don't save password to DB when LDAP enabled.
////////insertUserReqData.setUserPassword(userPassword);
		EntityDbData entityDbData = userProcess.addUser(insertUserReqData, tenantId);
		if (entityDbData.getAppUserId() > 0) {
			List<EntityDbData> entityDbDataList = new ArrayList<>();
			entityDbDataList.add(entityDbData);
			auditProcess.addAuditDetails(entityDbDataList, EnumEntityType.USER, EnumOperationType.INSERT);
		}
		return entityDbData.getAppUserId();
	}

	private void updateUserAccountEnabled(long userId) throws WorkbenchException {
		logger.info("Enable new user account");
		UpdateUserReqData updateUserReqData = new UpdateUserReqData();
		updateUserReqData.setUserId(userId);
		updateUserReqData.setAccountEnabled(true);
		EntityDbData entityDbData = userProcess.updateUserAccountEnabled(updateUserReqData);
		List<EntityDbData> entityDbDataList = new ArrayList<EntityDbData>();
		entityDbDataList.add(entityDbData);
		auditProcess.addAuditDetails(entityDbDataList, EnumEntityType.USER, EnumOperationType.UPDATE);
	}

	private void insertNewUserRoleToDB(long userId, int roleTypeCde) throws WorkbenchException {
		logger.info("Add role to new user account");
		AddRoleReqData addRoleReqData = new AddRoleReqData();
		addRoleReqData.setAppUserId((int) userId);
		addRoleReqData.setUserRoleType(roleTypeCde);
		List<EntityDbData> entityDbDataList = roleProcess.addNewRole(addRoleReqData);
		EntityDbData latestEntityDbData = entityDbDataList.get(0);
		if (ListUtility.hasValue(latestEntityDbData.getAppUserRoleRelIdList())) {
			Long appUserRoleRelId = latestEntityDbData.getAppUserRoleRelIdList().get(0);
			if (appUserRoleRelId > 0) {
				auditProcess.addAuditDetails(entityDbDataList, EnumEntityType.ROLE, EnumOperationType.INSERT);
			}
		}
	}

	private void insertNewUserQueueToDB(long userId, int queueNameCde) throws WorkbenchException {
		logger.info("Add queue to new user account");
		InsertUserQueueReqData insertUserQueueReqData = new InsertUserQueueReqData();
		insertUserQueueReqData.setAppUserId(userId);
		insertUserQueueReqData.setQueueNameCde(queueNameCde);
		long appUserQueueRelId = userProcess.addUserToQueue(insertUserQueueReqData);
		if (appUserQueueRelId > 0) {
			List<Long> appUserQueueRelIdList = new ArrayList<Long>();
			appUserQueueRelIdList.add(appUserQueueRelId);
			List<EntityDbData> entityDbDataList = new ArrayList<EntityDbData>();
			EntityDbData entityDbData = new EntityDbData();
			entityDbData.setAppUserQueueRelIdList(appUserQueueRelIdList);
			entityDbDataList.add(entityDbData);
			auditProcess.addAuditDetails(entityDbDataList, EnumEntityType.QUEUE_ASSIGNMENT, EnumOperationType.INSERT);
		}
	}
}
