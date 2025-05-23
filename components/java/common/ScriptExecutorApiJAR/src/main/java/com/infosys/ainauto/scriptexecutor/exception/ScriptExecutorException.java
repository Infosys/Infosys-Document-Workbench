/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.scriptexecutor.exception;

public class ScriptExecutorException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public ScriptExecutorException() {
    }

    public ScriptExecutorException(String message) {
        super(message);
    }

    public ScriptExecutorException(Throwable t) {
        super(t);
    }

    public ScriptExecutorException(String message, Throwable t) {
        super(message, t);
    }

}
