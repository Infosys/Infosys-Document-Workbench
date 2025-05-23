/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.web.exception;

public class DocwbWebException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public DocwbWebException() {
    }

    public DocwbWebException(String message) {
        super(message);
    }

    public DocwbWebException(Throwable t) {
        super(t);
    }

    public DocwbWebException(String message, Throwable t) {
        super(message, t);
    }

}
