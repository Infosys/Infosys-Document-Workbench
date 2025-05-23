/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.ui.common;

import java.io.IOException;

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

public class HttpRequestFilter implements Filter {

	private static final Logger logger = LoggerFactory.getLogger(HttpRequestFilter.class);
	
	@Override
	public void init(FilterConfig config) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		try {
			String[] requestUrlTokens = httpRequest.getRequestURI().split("/");

			//If request is for API and pre-fetch (i.e. OPTIONS), then set header and do not chain
			if (requestUrlTokens.length >= 3
					&& requestUrlTokens[2].equals("api")
							& httpRequest.getHeader("Access-Control-Request-Method") != null
					&& "OPTIONS".equals(httpRequest.getMethod())) {
				httpResponse.addHeader("Access-Control-Allow-Origin", "*");
				httpResponse.addHeader("Access-Control-Allow-Headers", "*");
				httpResponse.addHeader("Access-Control-Allow-Methods", "POST, PUT, GET, DELETE, OPTIONS");
			} else {
				// Ref https://content-security-policy.com/
				// Changes as per recommendations APP VA report for “AMS Workbench (L2 extension
				// of Mana)” Application
				// TODO Blocking Content-Security-Policy header as Swagger is not working
//				httpResponse.addHeader("Content-Security-Policy",
//						"default-src 'self'; style-src 'self' 'unsafe-inline'; frame-src *");
				httpResponse.addHeader("X-XSS-Protection", "1; mode=block");
				httpResponse.addHeader("X-Content-Type-Options", "nosniff");
				logger.debug(
						"Added HTTP headers : Content-Security-Policy , X-XSS-Protection , X-Content-Type-Options");
				chain.doFilter(request, response);
			}
		} catch (Exception ex) {
			// Do nothing
		}
	}

	@Override
	public void destroy() {
	}

}
