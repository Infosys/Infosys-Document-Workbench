/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.security;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.StringUtility;

@Component
public class LogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {

	private static final Logger logger = LoggerFactory.getLogger(LogoutSuccessHandler.class);

	@Override
	public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
			throws IOException, ServletException {
		logger.info("User logged out successfully from url: "
				+ StringUtility.sanitizeReqData(request.getHeader("Referer")));

		RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/logoutsuccess.jsp");
		dispatcher.forward(request, response);
	}

}
