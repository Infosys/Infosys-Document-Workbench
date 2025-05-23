/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.security;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.service.common.InvalidTenantIdException;
import com.infosys.ainauto.docwb.service.common.WorkbenchConstants;
import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.dao.user.IUserDataAccess;
import com.infosys.ainauto.docwb.service.model.db.AppUserDbData;
import com.infosys.ainauto.docwb.service.model.security.UserDetailsData;
import com.infosys.ainauto.docwb.service.process.auth.IAuthenticationProcess;

@Component
public class SpringUserDetailsService implements UserDetailsService {

	private static final Logger logger = LoggerFactory.getLogger(SpringUserDetailsService.class);

	@Autowired
	private IUserDataAccess UserDataAccess;

	@Autowired
	private IAuthenticationProcess authenticationProcess;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private HttpServletRequest request;

	@Override
	public UserDetails loadUserByUsername(String username) {
		if (username == null) {
			throw new UsernameNotFoundException(username);
		} else {
			try {
				final String userSecretAttribute = request.getParameter(WorkbenchConstants.PASSWORD);
				final String tenantId = request.getParameter(WorkbenchConstants.TENANT_ID);
				boolean isAuthenticated = authenticationProcess.authenticateUser(
						request.getParameter(WorkbenchConstants.USERNAME), userSecretAttribute, tenantId);
				AppUserDbData appUserDbData = UserDataAccess.getUserData(username.toLowerCase(), tenantId);
				if (isAuthenticated && !StringUtility.hasTrimmedValue(appUserDbData.getUserPassword())) {
					appUserDbData.setUserPassword(passwordEncoder.encode(userSecretAttribute));
				}
				if (appUserDbData == null) {
					throw new UsernameNotFoundException(username);
				}
				// As part of RBAC, service account will be a new role. Hence, user type code
				// check is not required.
//				if (appUserDbData.getUserTypeCde() != 1) {
//					throw new NonUserTypeException(WorkbenchConstants.AUTH_ERROR_MSG_ACCESS_DENIED_NON_USER);
//				}

				return new UserDetailsData(appUserDbData);
			} catch (InvalidTenantIdException e) {
				logger.error("Invalid tenant id");
			} catch (UsernameNotFoundException ex) {
				throw new UsernameNotFoundException(username);
			} catch(AuthorizationServiceException ex){
				throw new AuthorizationServiceException(ex.getMessage());
			}catch (WorkbenchException e) {
				logger.error("Exception thrwon while fetching authentication" + e.getMessage());
			}
		}
		return null;

	}
}
