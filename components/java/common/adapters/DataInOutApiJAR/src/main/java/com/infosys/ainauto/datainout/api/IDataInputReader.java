/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.datainout.api;

import java.util.List;

import com.infosys.ainauto.datainout.common.DataInputException;
import com.infosys.ainauto.datainout.model.DataInputRecord;
import com.infosys.ainauto.datasource.model.DataSourceFolder;

public interface IDataInputReader {

	/** Basic Operations **/
	public List<Boolean> connect() throws DataInputException;

	public List<Boolean> disconnect() throws DataInputException;

	public List<DataInputRecord> getNewItems() throws DataInputException;

	public List<Boolean> updateItemAsRead(String dataInputRecordId) throws DataInputException;

	public List<Boolean> updateItemAsUnread(String dataInputRecordId) throws DataInputException;

	/** Additional Items **/
	public void openFolder(DataSourceFolder dataSourceFolder) throws DataInputException;

	public void closeFolder() throws DataInputException;

	public List<Boolean> createFolder(DataSourceFolder dataSourceFolder) throws DataInputException;

	public List<Boolean> deleteFolder(DataSourceFolder dataSourceFolder) throws DataInputException;

	public List<Integer> moveItemToFolder(List<String> dataInputRecordIdList, DataSourceFolder sourceDataSourceFolder, DataSourceFolder targetDataSourceFolder)
			throws DataInputException;

	public List<Integer> copyItemToFolder(List<String> dataInputRecordIdList, DataSourceFolder sourceDataSourceFolder, DataSourceFolder targetDataSourceFolder)
			throws DataInputException;
	
	/**
	 * Cleanup temp files created as part of processing
	 * @param dataInputRecordId
	 * @return
	 */
	public boolean deleteSavedAttachments(String dataInputRecordId);

}
