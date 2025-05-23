/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.security;

import java.io.IOException;
import java.util.Calendar;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.process.auth.IApiRoleAuthorizationProcess;

@Component
public class AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private static final Logger logger = LoggerFactory.getLogger(AuthenticationSuccessHandler.class);

	private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

	@Value("${jwt.secret}")
	private String TOKEN_SECRET;

	@Value("${jwt.expiry.secs}")
	private long TOKEN_EXPIRY_IN_SECS;

	@Autowired
	IApiRoleAuthorizationProcess apiRoleAuthorizationProcess;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {

		//  - This logic is no longer needed as /api/v1/auth is used to generate
		// token
		// UserDetailsData userDetailsData = (UserDetailsData)
		// authentication.getPrincipal();
		// Map<String, Object> claimsMap = new HashMap<String, Object>();
		// String jwt = TokenHelper.generateToken(userDetailsData.getUsername(),
		// claimsMap, TOKEN_SECRET,
		// TOKEN_EXPIRY_IN_SECS);
		// logger.info("Token generated for user=" + userDetailsData.getUsername() + "
		// is " + jwt);
		clearAuthenticationAttributes(request);
		handle(request, response, authentication);
	}

	protected void handle(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
			throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		boolean isAuthorizedUrl = false;
		try {
			String requestMethod = StringUtility.sanitizeReqData(httpRequest.getMethod());
			String api = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());
			isAuthorizedUrl = apiRoleAuthorizationProcess.isApiAccessAllowed(api, requestMethod, true);
		} catch (WorkbenchException e) {
			logger.error(e.getMessage());
		}
		
//		TODO: : 12/08/2021 - Below logic is not needed as RBAC implemented in following lines to handling swagger-ui.
//		String targetUrl = determineTargetUrl(authentication);
//		if (!StringUtility.hasValue(targetUrl)) {
//			response.setStatus(HttpStatus.FORBIDDEN.value());
//			Map<String, Object> data = new HashMap<>();
//			data.put("timestamp", Calendar.getInstance().getTime());
//			data.put("exception", "You are not authorized to this content.");
//			response.getOutputStream().println(objectMapper.writeValueAsString(data));
//			request.setAttribute("exception", "You are not authorized to this content.");
//			request.setAttribute("timestamp", Calendar.getInstance().getTime());
//			HttpSession session = request.getSession(true);
//			if(session!=null) {
//				session.invalidate();
//			}
		
		if (!isAuthorizedUrl) {	
//			TODO: : 12/08/2021 - Have added this line to clear current auth related token info from session.
			request.setAttribute("exception", "You are not authorized to this content.");
			request.setAttribute("timestamp", Calendar.getInstance().getTime());
			SecurityContextHolder.clearContext();
			RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/login.jsp");
			dispatcher.forward(request, response);
		} else {
			String targetUrl = "/swagger-ui.html";
			if (response.isCommitted()) {
				logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
				return;
			}
			redirectStrategy.sendRedirect(request, response, targetUrl);
		}
	}

//	TODO: : 12/08/2021 - Commented as RBAC implemented
//	protected String determineTargetUrl(Authentication authentication) {
//		boolean isUser = false;
//		boolean isAdmin = false;
//		boolean isManager = false;
//		Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
//
//		for (GrantedAuthority grantedAuthority : authorities) {
//			logger.error(grantedAuthority.getAuthority());
//			if (grantedAuthority.getAuthority().equals("ROLE_AGENT")) {
//				isUser = true;
//
//				break;
//			} else if (grantedAuthority.getAuthority().equals("ROLE_ADMIN")) {
//				isAdmin = true;
//
//				break;
//			} else if (grantedAuthority.getAuthority().equals("ROLE_MANAGER")) {
//				isManager = true;
//
//				break;
//			}
//		}
//
//		if (isUser || isAdmin || isManager) {
//			return "/swagger-ui.html";
//		} else {
//			return "";
//		}
//	}

	public void setRedirectStrategy(RedirectStrategy redirectStrategy) {
		this.redirectStrategy = redirectStrategy;
	}

	protected RedirectStrategy getRedirectStrategy() {
		return redirectStrategy;
	}
}
