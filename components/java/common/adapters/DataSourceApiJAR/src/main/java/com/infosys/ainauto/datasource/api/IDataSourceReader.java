/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.datasource.api;

import java.util.List;

import com.infosys.ainauto.datasource.common.DataSourceException;
import com.infosys.ainauto.datasource.model.DataSourceFolder;
import com.infosys.ainauto.datasource.model.DataSourceRecord;

public interface IDataSourceReader {

	/** Metadata **/
	public String getName() throws DataSourceException;

	/** Basic Operations **/
	public boolean connect() throws DataSourceException;

	public boolean disconnect() throws DataSourceException;

	public List<DataSourceRecord> getNewItems() throws DataSourceException;

	public boolean updateItemAsRead(String dataSourceRecordId) throws DataSourceException;

	public boolean updateItemAsUnread(String dataSourceRecordId) throws DataSourceException;

	/** Advanced Operations **/
	public boolean openFolder(DataSourceFolder dataSourceFolder) throws DataSourceException;

	public boolean closeFolder() throws DataSourceException;

	public boolean createFolder(DataSourceFolder dataSourceFolder) throws DataSourceException;

	public boolean deleteFolder(DataSourceFolder dataSourceFolder) throws DataSourceException;

	public int moveItemToFolder(List<String> dataSourceRecordIdList, DataSourceFolder sourceDataSourceFolder,
			DataSourceFolder targetDataSourceFolder) throws DataSourceException;

	public int copyItemToFolder(List<String> dataSourceRecordIdList, DataSourceFolder sourceDataSourceFolder,
			DataSourceFolder targetDataSourceFolder) throws DataSourceException;
}
