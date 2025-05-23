/** =============================================================================================================== *
 * Copyright 2022 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.service.messaging;

import com.infosys.ainauto.docwb.service.common.WorkbenchException;

public interface IMessagingService {
	public void putMessageInQueue(final String queueName, final Object obj) throws WorkbenchException;
}
