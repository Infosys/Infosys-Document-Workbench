/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.datainout.impl.basic.api;

import com.infosys.ainauto.datainout.api.IDataInputReader;
import com.infosys.ainauto.datainout.api.IDataOutputWriter;
import com.infosys.ainauto.datainout.config.DataInputConfig;
import com.infosys.ainauto.datainout.config.DataOutputConfig;
import com.infosys.ainauto.datainout.spi.IDataInOutProvider;

public class DataInOutProvider implements IDataInOutProvider {

	@Override
	public IDataInputReader getDataInputReader(DataInputConfig dataInputConfig) {
		return new BasicDataInputReader(dataInputConfig);
	}

	@Override
	public IDataOutputWriter getDataOutputWriter(DataOutputConfig dataOutputConfig) {
		return new BasicDataOutputWriter(dataOutputConfig);
	}
}