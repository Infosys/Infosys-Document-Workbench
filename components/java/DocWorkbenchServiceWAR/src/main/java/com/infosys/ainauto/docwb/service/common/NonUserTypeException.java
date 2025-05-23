/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.common;

import org.springframework.security.core.AuthenticationException;

public class NonUserTypeException extends AuthenticationException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NonUserTypeException(String msg) {
		super(msg);

	}

	public NonUserTypeException(String msg, Throwable t) {
		super(msg, t);
	}
}
