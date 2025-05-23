/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.ui.common;

public class DocWorkbenchUIException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public DocWorkbenchUIException() {
    }

    public DocWorkbenchUIException(String message) {
        super(message);
    }

    public DocWorkbenchUIException(Throwable t) {
        super(t);
    }

    public DocWorkbenchUIException(String message, Throwable t) {
        super(message, t);
    }

}
