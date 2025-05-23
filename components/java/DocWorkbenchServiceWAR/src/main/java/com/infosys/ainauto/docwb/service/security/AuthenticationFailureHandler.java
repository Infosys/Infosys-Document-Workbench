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

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.service.common.WorkbenchConstants;

@Component
public class AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {

		// super.onAuthenticationFailure(request, response, exception);

		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		String exceptionStr = exception.getMessage();
		if (!StringUtility.hasValue(exceptionStr)) {
			exceptionStr = "Internal Server Error";
		} else if (exceptionStr.contains("User is disabled")) {
			exceptionStr = "User account is disabled or not activated. Please contact admin.";
		} else if (exceptionStr.contains(WorkbenchConstants.API_RESPONSE_MSG_NOT_AUTHORIZED)) {
			exceptionStr=WorkbenchConstants.API_RESPONSE_MSG_NOT_AUTHORIZED;
		}else {
			exceptionStr = WorkbenchConstants.API_RESPONSE_MSG_BAD_CREDENTIALS;
		}
		request.setAttribute("exception", exceptionStr);
		request.setAttribute("timestamp", Calendar.getInstance().getTime());

		RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/login.jsp");
		dispatcher.forward(request, response);

	}
}
