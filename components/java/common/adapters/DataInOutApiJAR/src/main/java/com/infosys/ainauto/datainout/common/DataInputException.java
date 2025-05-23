/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.datainout.common;

public class DataInputException extends Exception {

	private static final long serialVersionUID = 1L;

	public DataInputException() {
	}

	public DataInputException(String message) {
		super(message);
	}

	public DataInputException(Throwable t) {
		super(t);
	}

	public DataInputException(String message, Throwable t) {
		super(message, t);
	}

}
