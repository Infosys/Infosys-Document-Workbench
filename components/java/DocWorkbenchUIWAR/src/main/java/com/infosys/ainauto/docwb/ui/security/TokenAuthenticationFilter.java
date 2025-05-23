/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.ui.security;

import java.io.IOException;

import javax.json.JsonObject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.infosys.ainauto.docwb.ui.common.DocWorkbenchUIConstants;
import com.infosys.ainauto.docwb.ui.common.DocWorkbenchUIException;
import com.infosys.ainauto.docwb.ui.process.auth.IAuthorizationProcess;

public class TokenAuthenticationFilter implements Filter {

	private static final Logger logger = LoggerFactory.getLogger(TokenAuthenticationFilter.class);
	
	@Autowired
	private IAuthorizationProcess authorizationProcess;
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
//		This line needed to Autowire bean inside Filter Implementation class
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		boolean isRestrictedApi = isRestrictedApi(httpRequest.getRequestURI(), httpRequest.getContextPath());

		if (isRestrictedApi) {
			JsonObject jsonResponse = null;
			try {
				// call docwbservice valid auth api
				jsonResponse = authorizationProcess.validate(getAuthHeaderFromRequest(httpRequest));
			} catch (DocWorkbenchUIException ex) {
				httpResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
				logger.error(ex.getMessage());
				throw new ServletException("Error while trying to Validate User.", ex);
			}
			if(jsonResponse==null) {
				httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				httpResponse.setContentType("text/html");
				httpResponse.setCharacterEncoding("UTF-8");
				httpResponse.getWriter().write("This request is not authorized.");
			}else if (jsonResponse.getInt("responseCde")!=0) {
				httpResponse.setStatus(jsonResponse.getInt("responseCde"));
				httpResponse.setContentType("text/html");
				httpResponse.setCharacterEncoding("UTF-8");
				httpResponse.getWriter().write(jsonResponse.getString("responseMsg"));
			} else {
				chain.doFilter(request, response);
			}
		} else {
			chain.doFilter(request, response);
		}

	}

	private boolean isRestrictedApi(String url, String contextPath) {
		if (url.startsWith(contextPath + "/api/")) {
			if (url.endsWith("/about")) {
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}

	private String getAuthHeaderFromRequest(HttpServletRequest request) {
		return request.getHeader(DocWorkbenchUIConstants.AUTH_HEADER_PROP_NAME);
	}

	@Override
	public void destroy() {
		
	}

}