/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.core.model.api;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.infosys.ainauto.scriptexecutor.data.ScriptResponseItemData;

public class ScriptResponseWrapperData extends ScriptResponseItemData{
	
	@JsonGetter("transactionid")
	@Override
	public String getTransactionId() {
		return super.getTransactionId();
	}
	
}
