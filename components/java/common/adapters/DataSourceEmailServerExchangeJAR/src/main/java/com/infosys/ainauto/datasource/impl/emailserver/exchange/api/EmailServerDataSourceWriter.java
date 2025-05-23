/** =============================================================================================================== *
 * Copyright 2021 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


package com.infosys.ainauto.datasource.impl.emailserver.exchange.api;

import java.util.Map;

import com.infosys.ainauto.datasource.api.IDataSourceWriter;
import com.infosys.ainauto.datasource.common.DataSourceException;
import com.infosys.ainauto.datasource.config.DataSourceConfig;
import com.infosys.ainauto.datasource.model.DataSourceRecord;

public class EmailServerDataSourceWriter implements IDataSourceWriter {

	private String name;

	public EmailServerDataSourceWriter(String name, DataSourceConfig dataSourceWriterConfig) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean connect() throws DataSourceException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean disconnect() throws DataSourceException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public DataSourceRecord generateNewItem() throws DataSourceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean writeItem(DataSourceRecord dataSourceRecord, Map<String, String> paramsMap)
			throws DataSourceException {
		// TODO Auto-generated method stub
		return false;
	}

}
