/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.security;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

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
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.service.common.SessionHelper;
import com.infosys.ainauto.docwb.service.process.auth.IApiRoleAuthorizationProcess;

@Component
public class ApiRoleAuthorizationFilter implements Filter {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApiRoleAuthorizationFilter.class);
	private static final Logger PERF_LOGGER = LoggerFactory.getLogger("performance");
	private static final String MDC_TRANSACTION_ID = "transactionId";
	private static final String MDC_LOGIN_USERNAME = "loginUsername";

	private ObjectMapper objectMapper = new ObjectMapper();
	
	@Autowired
	IApiRoleAuthorizationProcess apiRoleAuthorizationProcess;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
//		This line needed to Autowire bean inside Filter Implementation class
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		long startTime = System.nanoTime();
		MDC.put(MDC_TRANSACTION_ID, String.valueOf(StringUtility.getRangeOfRandomNumberInInt(100000000, 999999999)));
		MDC.put(MDC_LOGIN_USERNAME, SessionHelper.getLoginUsername());

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String requestUrl = StringUtility.sanitizeReqData(httpRequest.getRequestURL().toString());
		String requestMethod = StringUtility.sanitizeReqData(httpRequest.getMethod());
		String message = requestMethod + "|" + requestUrl + "|" + SessionHelper.getLoginUsername();
//		LOGGER.info("API - RequestUrl=" + requestUrl + "|Method=" + requestMethod);
		boolean isApiAuthorizedToAccess = false;
		try {
			String api = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());
			isApiAuthorizedToAccess = apiRoleAuthorizationProcess.isApiAccessAllowed(api, requestMethod, false);
			if (isApiAuthorizedToAccess) {
				LOGGER.info("ACCESS GRANTED|" + message);
				chain.doFilter(request, response);
			}
		} catch (Exception ex) {
			isApiAuthorizedToAccess=false;
			LOGGER.error(ex.getMessage());
		}

		if (!isApiAuthorizedToAccess) {
			
			LOGGER.error("ACCESS DENIED|" + message);
			HttpServletResponse httpResponse = (HttpServletResponse) response;

			httpResponse.setStatus(HttpStatus.FORBIDDEN.value());
			Map<String, Object> data = new HashMap<>();
			data.put("timestamp", Calendar.getInstance().getTime());
			data.put("exception", "You are not authorized to this content.");

			httpResponse.getOutputStream().println(objectMapper.writeValueAsString(data));
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		String perfMessage = " - in (secs): " + timeElapsed;
		LOGGER.info("COMPLETED API AUTH - " + requestUrl + perfMessage);

		PERF_LOGGER.info("INBOUND,{},{},{},secs", requestMethod, requestUrl, timeElapsed);

		MDC.remove(MDC_TRANSACTION_ID);
		MDC.remove(MDC_LOGIN_USERNAME);
	}
	
	@Override
	public void destroy() {
	}

}
