/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.datasource.impl.emailserver.basic.api;

import com.infosys.ainauto.datasource.api.IDataSourceReader;
import com.infosys.ainauto.datasource.api.IDataSourceWriter;
import com.infosys.ainauto.datasource.config.DataSourceConfig;
import com.infosys.ainauto.datasource.spi.IDataSourceProvider;

public class EmailServerDataSourceProvider implements IDataSourceProvider {

	@Override
	public IDataSourceReader getDataSourceReader(String name, DataSourceConfig dataSourceConfig) {
		return new EmailServerDataSourceReader(name, dataSourceConfig);
	}
	
	@Override
	public IDataSourceWriter getDataSourceWriter(String name, DataSourceConfig dataSourceWriterConfig) {
		return new EmailServerDataSourceWriter(name, dataSourceWriterConfig);
	}
}
