/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.core.exception;

public class DocwbEngineException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public DocwbEngineException() {
    }

    public DocwbEngineException(String message) {
        super(message);
    }

    public DocwbEngineException(Throwable t) {
        super(t);
    }

    public DocwbEngineException(String message, Throwable t) {
        super(message, t);
    }

}
