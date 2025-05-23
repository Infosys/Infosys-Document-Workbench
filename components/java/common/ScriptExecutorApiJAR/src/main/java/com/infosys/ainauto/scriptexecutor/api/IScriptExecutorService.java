/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.scriptexecutor.api;

import com.infosys.ainauto.scriptexecutor.data.ScriptIdentifierData;
import com.infosys.ainauto.scriptexecutor.data.ScriptResponseData;

public interface IScriptExecutorService {

	public ScriptResponseData initiateExecution(ScriptIdentifierData scriptIdentifierData);

	public ScriptResponseData getTransactionStatusAndResult(String transactionId);
}
