/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.datainout.common;

public class DataOutputException extends Exception {

	private static final long serialVersionUID = 1L;

	public DataOutputException() {
	}

	public DataOutputException(String message) {
		super(message);
	}

	public DataOutputException(Throwable t) {
		super(t);
	}

	public DataOutputException(String message, Throwable t) {
		super(message, t);
	}

}
