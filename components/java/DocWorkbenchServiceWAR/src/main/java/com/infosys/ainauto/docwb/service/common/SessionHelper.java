/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.common;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.infosys.ainauto.docwb.service.model.security.UserDetailsData;

public final class SessionHelper {

//	private static final Logger logger = LoggerFactory.getLogger(SessionHelper.class);
//
//	private static SessionHelper instance = null;

//	private SessionManager() {
//	}
//
//	public static SessionManager getInstance() {
//		if (instance == null) {
//			instance = new SessionManager();
//		}
//		return (instance);
//	}

	public static UserDetailsData getLoginUserData() {
		UserDetailsData userDetailsData = null;
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		Object obj = (auth != null) ? auth.getPrincipal() : null;

		if (obj instanceof UserDetailsData) {
			userDetailsData = (UserDetailsData) obj;
		}
		return userDetailsData;
	}
	
	public static String getLoginUsername() {
		String loginUsername = "";
		UserDetailsData userDetailsData = getLoginUserData();
		if (userDetailsData!=null) {
			loginUsername = userDetailsData.getUsername();
		}
		return loginUsername;
	}
	
	public static String getTenantId() {
		String tenantId = "";
		UserDetailsData userDetailsData = getLoginUserData();
		if (userDetailsData!=null) {
			tenantId = userDetailsData.getTenantId();
		}
		return tenantId;
	}
}
