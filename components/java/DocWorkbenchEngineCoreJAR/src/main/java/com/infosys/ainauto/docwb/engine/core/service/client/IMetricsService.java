/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.core.service.client;

import com.infosys.ainauto.docwb.engine.core.common.type.EnumExecutorType;
import com.infosys.ainauto.docwb.engine.core.service.client.MetricsService.EnumMetric;

public interface IMetricsService {

	public void updateValue(EnumMetric enumMetric, long executionId, EnumExecutorType enumExecutorType,
			String instanceName);

	public void startTimer(EnumMetric enumMetric, EnumExecutorType enumExecutorType, boolean doRestart);
}