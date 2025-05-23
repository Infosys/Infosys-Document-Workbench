/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.common;

public class WorkbenchException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public WorkbenchException() {
    }

    public WorkbenchException(String message) {
        super(message);
    }

    public WorkbenchException(Throwable t) {
        super(t);
    }

    public WorkbenchException(String message, Throwable t) {
        super(message, t);
    }

}
