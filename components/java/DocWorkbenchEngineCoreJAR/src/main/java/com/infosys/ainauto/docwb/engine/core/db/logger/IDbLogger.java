/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.core.db.logger;

import org.springframework.stereotype.Component;

import com.infosys.ainauto.docwb.engine.core.common.type.EnumExecutionEventType;
import com.infosys.ainauto.docwb.engine.core.common.type.EnumExecutorType;

@Component
public interface IDbLogger {

	public long startExecution(EnumExecutorType executorType, String executionTitle, String eventMsg);

	public long endExecution(long executionId, String eventMsg);

	public long addEvent(long executionId, EnumExecutionEventType eventType, String eventMsg);
	
	public long updateEventMsg(long execEventRelId, String eventMsg);
}
