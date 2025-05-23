/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.process.auth;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.DateUtility;
import com.infosys.ainauto.docwb.service.common.InvalidTenantIdException;
import com.infosys.ainauto.docwb.service.common.TokenHelper;
import com.infosys.ainauto.docwb.service.common.WorkbenchConstants;
import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.dao.user.IUserDataAccess;
import com.infosys.ainauto.docwb.service.model.api.AuthResData;
import com.infosys.ainauto.docwb.service.model.db.AppUserDbData;
import com.infosys.ainauto.docwb.service.model.security.UserDetailsData;
import com.infosys.ainauto.docwb.service.security.ValidAuthenticationToken;

import io.jsonwebtoken.Claims;

@Component
public class AuthorizationProcess implements IAuthorizationProcess {
	@Autowired
	private IUserDataAccess userDataAccess;

	@Autowired
	private IAuthenticationProcess authenticationProcess;

	@Autowired
	UserDetailsService springUserDetailService;

	@Value("${jwt.secret}")
	private String TOKEN_SECRET;

	@Value("${jwt.expiry.secs}")
	private long TOKEN_EXPIRY_IN_SECS;

	private static final Logger logger = LoggerFactory.getLogger(AuthorizationProcess.class);

	@Override
	public AuthResData getAuthToken(String userName, String rawPassword, String tenantId) throws WorkbenchException {
		AuthResData authResData = new AuthResData();
		try {
			boolean isAuthenticateUser = authenticationProcess.authenticateUser(userName, rawPassword, tenantId);
			if (isAuthenticateUser) {
				AppUserDbData appUserDbData = userDataAccess.getUserData(userName, tenantId);
				if (null == appUserDbData) {
					throw new UsernameNotFoundException("User not found in DB");
				}
				UserDetailsData userDetailsData = new UserDetailsData(appUserDbData);
				if (!userDetailsData.isEnabled()) {
					authResData.setErrorCode(WorkbenchConstants.AUTH_TOKEN_ERROR_CDE_ACCOUNT_DISABLED_OR_INACTIVE);
				} else if (userDetailsData.getRoleTypeCde() == 0) {
					authResData.setErrorCode(WorkbenchConstants.AUTH_TOKEN_ERROR_CDE_ACCOUNT_ROLE_NOT_ASSIGNED);
				} else {
					ValidAuthenticationToken validAuthenticationToken = new ValidAuthenticationToken(userDetailsData);
					Map<String, Object> claimsMap = new HashMap<String, Object>();
					claimsMap.put(WorkbenchConstants.TENANT_ID, tenantId);
					String jwt = TokenHelper.generateToken(userName, claimsMap, TOKEN_SECRET, TOKEN_EXPIRY_IN_SECS);
					validAuthenticationToken.setToken(jwt);
					SecurityContextHolder.getContext().setAuthentication(validAuthenticationToken);
					logger.debug("Created valid authentication token for user=" + userName);
					// Compose response
					authResData.setToken(jwt);
					Claims claims = TokenHelper.getClaimsFromToken(jwt, TOKEN_SECRET);
					authResData.setExpiryDtm(
							DateUtility.toString(claims.getExpiration(), WorkbenchConstants.API_TIMESTAMP_FORMAT_12HR));
				}
			} else {
				authResData.setErrorCode(WorkbenchConstants.AUTH_TOKEN_ERROR_CDE_BAD_CREDENTIALS);
			}
		} catch (InvalidTenantIdException e) {
			authResData.setErrorCode(WorkbenchConstants.AUTH_TOKEN_ERROR_CDE_INVALID_TENANT_ID);
		} catch (UsernameNotFoundException ex) {
			authResData.setErrorCode(WorkbenchConstants.AUTH_TOKEN_ERROR_CDE_BAD_CREDENTIALS);
		} catch(AuthorizationServiceException ex) {
			authResData.setErrorCode(WorkbenchConstants.AUTH_TOKEN_ERROR_CDE_USER_UNAUTHORIZED);
		}
		return authResData;
	}
}
