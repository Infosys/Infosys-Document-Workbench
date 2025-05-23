/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;

import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.service.common.SessionHelper;
import com.infosys.ainauto.docwb.service.common.TokenHelper;
import com.infosys.ainauto.docwb.service.common.WorkbenchConstants;
import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.dao.user.IUserDataAccess;
import com.infosys.ainauto.docwb.service.model.db.AppUserDbData;
import com.infosys.ainauto.docwb.service.model.security.UserDetailsData;

public class TokenAuthenticationFilter extends OncePerRequestFilter {

	private static final Logger logger = LoggerFactory.getLogger(TokenAuthenticationFilter.class);

	@Value("${jwt.secret}")
	private String SECRET;

	@Autowired
	UserDetailsService springUserDetailService;

	@Autowired
	private IUserDataAccess userDataAccess;

	@Override
	public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		boolean isRestrictedApi = isRestrictedApi(request.getRequestURI(), request.getContextPath());

		if (isRestrictedApi) {
			// Assume user is unauthorized
			boolean isUnauthorized = true;
			// Get token from request
			String authTokenFromRequest = getTokenFromRequest(request);
			// Get username from token
			if (authTokenFromRequest != null) {
				boolean hasTokenExpired = TokenHelper.hasTokenExpired(authTokenFromRequest, SECRET);
				if (!hasTokenExpired) {
					String usernameFromToken = TokenHelper.getSubjectFromToken(authTokenFromRequest, SECRET);
					String tenantId = TokenHelper.getTenantId(authTokenFromRequest, SECRET);
					if (usernameFromToken != null) {
						// If username is present in token, then request is valid
						UserDetailsData userDetails = null;
						try {
							AppUserDbData appUserDbData = userDataAccess.getUserData(usernameFromToken, tenantId);
							if (appUserDbData == null) {
								throw new UsernameNotFoundException(usernameFromToken);
							} 
							if (appUserDbData.isAccountEnabled()) {
								userDetails = new UserDetailsData(appUserDbData);
								ValidAuthenticationToken validAuthenticationToken = new ValidAuthenticationToken(userDetails);
								validAuthenticationToken.setToken(authTokenFromRequest);
								SecurityContextHolder.getContext().setAuthentication(validAuthenticationToken);
								logger.debug("Created valid authentication token for user=" + usernameFromToken);
								isUnauthorized = false;
							}
						} catch (WorkbenchException ex) {
							response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
							throw new ServletException("Error while trying to get User details.", ex);
						}
					}
				}
			} else if (StringUtility.hasValue(SessionHelper.getLoginUsername())) {
				// If username is present in session, then request is valid
				isUnauthorized = false;
			}
			// If not authorized, then return 401 error
			if (isUnauthorized) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.setContentType("text/html");
				response.setCharacterEncoding("UTF-8");
				response.getWriter().write("This request is not authorized.");
			} else {
				chain.doFilter(request, response);
			}
		} else {
			chain.doFilter(request, response);
		}

	}

	private boolean isRestrictedApi(String url, String contextPath) {
		if (url.startsWith(contextPath + "/api/")) {
			// Only /api/v1/auth is allowed API in list of all APIs
			if (url.endsWith("/auth") || url.endsWith("user/register")) {
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}

	private String getTokenFromRequest(HttpServletRequest request) {
		String authPropValue = request.getHeader(WorkbenchConstants.AUTH_HEADER_PROP_NAME);
		if (authPropValue != null && authPropValue.startsWith(WorkbenchConstants.AUTH_HEADER_PROP_VALUE_PREFIX)) {
			return authPropValue.substring(7);
		}
		return null;
	}

}