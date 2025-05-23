/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.api;

public class InsertUserResData {
	
	private String userName;
	private long userId;
	
	private int errCde;
	private String errTxt;
	
	public int getErrCde() {
		return errCde;
	}
	public void setErrCde(int errCde) {
		this.errCde = errCde;
	}
	public String getErrTxt() {
		return errTxt;
	}
	public void setErrTxt(String errTxt) {
		this.errTxt = errTxt;
	}
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
}
