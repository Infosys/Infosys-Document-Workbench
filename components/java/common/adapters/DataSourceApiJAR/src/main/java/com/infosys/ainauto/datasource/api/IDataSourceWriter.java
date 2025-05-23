/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.datasource.api;

import java.util.Map;

import com.infosys.ainauto.datasource.common.DataSourceException;
import com.infosys.ainauto.datasource.model.DataSourceRecord;

public interface IDataSourceWriter {

	/** Metadata **/
	public String getName() throws DataSourceException;

	/** Basic Operations **/
	public boolean connect() throws DataSourceException;

	public boolean disconnect() throws DataSourceException;

	/**
	 * Generates a new <b>DataSourceRecord</b> item to be used for setting values.
	 * @return
	 * @throws DataSourceException
	 */
	public DataSourceRecord generateNewItem() throws DataSourceException;

	public boolean writeItem(DataSourceRecord dataSourceRecord, Map<String, String> paramsMap) throws DataSourceException;

}
