/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.api;

public class AuthResData {
	
	private String token;
	private String expiryDtm;
	private int errorCode;

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public String getExpiryDtm() {
		return expiryDtm;
	}

	public void setExpiryDtm(String expiryDtm) {
		this.expiryDtm = expiryDtm;
	}
    
}
