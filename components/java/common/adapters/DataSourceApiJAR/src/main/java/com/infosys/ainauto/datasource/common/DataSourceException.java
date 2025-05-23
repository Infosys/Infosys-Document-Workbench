/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.datasource.common;

public class DataSourceException extends Exception {

	private static final long serialVersionUID = 1L;

	public DataSourceException() {
	}

	public DataSourceException(String message) {
		super(message);
	}

	public DataSourceException(Throwable t) {
		super(t);
	}

	public DataSourceException(String message, Throwable t) {
		super(message, t);
	}

}
