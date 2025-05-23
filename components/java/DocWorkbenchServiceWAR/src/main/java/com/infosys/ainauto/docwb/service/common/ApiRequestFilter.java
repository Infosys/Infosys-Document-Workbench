/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.common;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.infosys.ainauto.commonutils.StringUtility;

public class ApiRequestFilter implements Filter {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApiRequestFilter.class);
	private static final Logger PERF_LOGGER = LoggerFactory.getLogger("performance");
	private static final String MDC_TRANSACTION_ID = "transactionId";
	private static final String MDC_LOGIN_USERNAME = "loginUsername";

	@Override
	public void init(FilterConfig config) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		long startTime = System.nanoTime();
		MDC.put(MDC_TRANSACTION_ID, String.valueOf(StringUtility.getRangeOfRandomNumberInInt(100000000, 999999999)));
		MDC.put(MDC_LOGIN_USERNAME, SessionHelper.getLoginUsername());

		HttpServletRequest httpRequest = (HttpServletRequest) request;

		String refererUrl = StringUtility.sanitizeReqData(httpRequest.getHeader("referer"));
		String requestUrl = StringUtility.sanitizeReqData(httpRequest.getRequestURL().toString());
		String requestMethod = StringUtility.sanitizeReqData(httpRequest.getMethod());

		LOGGER.info("RECEIVED REQUEST - RequestUrl=" + requestUrl + "|ReferrerUrl=" + refererUrl);

		try {
			chain.doFilter(request, response);
		} catch (Exception ex) {
			LOGGER.error("Error occurred in doFilter", ex);
		}
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		String perfMessage = " - in (secs): " + timeElapsed;
		LOGGER.info("COMPLETED REQUEST - " + requestUrl + perfMessage);

		PERF_LOGGER.info("INBOUND,{},{},{},secs", requestMethod, requestUrl, timeElapsed);

		MDC.remove(MDC_TRANSACTION_ID);
		MDC.remove(MDC_LOGIN_USERNAME);
	}

	@Override
	public void destroy() {
	}

}
