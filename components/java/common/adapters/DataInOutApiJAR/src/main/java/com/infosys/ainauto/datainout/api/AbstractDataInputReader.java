/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.datainout.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.infosys.ainauto.datainout.common.DataInputException;
import com.infosys.ainauto.datainout.common.IDataSourceReaderOperationReturnBoolean;
import com.infosys.ainauto.datainout.common.IDataSourceReaderOperationReturnInt;
import com.infosys.ainauto.datainout.config.AbstractDataInOutConfig.ProviderDataSourceConfig;
import com.infosys.ainauto.datainout.config.DataInputConfig;
import com.infosys.ainauto.datainout.model.DataInputRecord;
import com.infosys.ainauto.datasource.DataSourceApi;
import com.infosys.ainauto.datasource.api.IDataSourceReader;
import com.infosys.ainauto.datasource.config.DataSourceConfig;
import com.infosys.ainauto.datasource.model.DataSourceFolder;
import com.infosys.ainauto.datasource.model.DataSourceRecord;
import com.infosys.ainauto.datasource.spi.IDataSourceProvider;

public abstract class AbstractDataInputReader implements IDataInputReader {

	private final static Logger logger = LoggerFactory.getLogger(AbstractDataInputReader.class);
	private static final int DATA_SOURCE_READER_INDEX_NONE = -1;
	private static final List<Integer> DATA_SOURCE_READER_INDEX_ALL_LIST = Arrays.asList(-100);
	protected DataInputConfig dataInputConfig;
	protected TreeMap<Integer, IDataSourceReader> providedDataSourceReaderMap;
	protected TreeMap<Integer, List<String>> readerIndexToRecordIdListMap;

	protected AbstractDataInputReader(DataInputConfig dataInputConfig) {
		this.dataInputConfig = dataInputConfig;
		initialize();
	}

	private void initialize() {
		providedDataSourceReaderMap = new TreeMap<>();
		readerIndexToRecordIdListMap = new TreeMap<>();
		for (int i = 0; i < dataInputConfig.getProviderDataSourceConfigList().size(); i++) {
			ProviderDataSourceConfig providerDataSourceConfig = dataInputConfig.getProviderDataSourceConfigList()
					.get(i);
			IDataSourceProvider dataSourceProvider = DataSourceApi
					.getProviderByClassName(providerDataSourceConfig.getProviderClassFullName());

			IDataSourceReader dataSourceReader = dataSourceProvider.getDataSourceReader(
					providerDataSourceConfig.getProviderClassFullName(),
					providerDataSourceConfig.getDataSourceConfig());

			providedDataSourceReaderMap.put(i, dataSourceReader);
			readerIndexToRecordIdListMap.put(i, new ArrayList<String>());
		}
		logger.info("Initialized");
	}

	@Override
	public List<Boolean> connect() throws DataInputException {
		IDataSourceReaderOperationReturnBoolean dataSourceReaderOperation = (
				IDataSourceReader dataSourceReader) -> dataSourceReader.connect();
		List<Boolean> resultList = executeDataSourceOperationAndReturnBoolean(dataSourceReaderOperation,
				DATA_SOURCE_READER_INDEX_ALL_LIST);
		return resultList;
	}

	@Override
	public List<Boolean> disconnect() throws DataInputException {
		IDataSourceReaderOperationReturnBoolean dataSourceReaderOperation = (
				IDataSourceReader dataSourceReader) -> dataSourceReader.disconnect();
		List<Boolean> resultList = executeDataSourceOperationAndReturnBoolean(dataSourceReaderOperation,
				DATA_SOURCE_READER_INDEX_ALL_LIST);
		return resultList;
	}

	@Override
	public void closeFolder() throws DataInputException {
		IDataSourceReaderOperationReturnBoolean dataSourceReaderOperation = (
				IDataSourceReader dataSourceReader) -> dataSourceReader.closeFolder();
		executeDataSourceOperationAndReturnBoolean(dataSourceReaderOperation, DATA_SOURCE_READER_INDEX_ALL_LIST);
	}

	@Override
	public void openFolder(DataSourceFolder dataSourceFolder) throws DataInputException {
		IDataSourceReaderOperationReturnBoolean dataSourceReaderOperation = (
				IDataSourceReader dataSourceReader) -> dataSourceReader.openFolder(dataSourceFolder);
		executeDataSourceOperationAndReturnBoolean(dataSourceReaderOperation, DATA_SOURCE_READER_INDEX_ALL_LIST);
	}

	@Override
	public List<DataInputRecord> getNewItems() throws DataInputException {
		List<DataInputRecord> dataInputRecordList = new ArrayList<>();
		StatusData statusData = null;
		String dataSourceName = "";
		List<DataSourceRecord> dataSourceRecordList;
		List<StatusData> statusDataList = new ArrayList<>();

		for (Map.Entry<Integer, IDataSourceReader> entry : providedDataSourceReaderMap.entrySet()) {
			try {
				Integer dsrIndex = entry.getKey();
				IDataSourceReader dataSourceReader = entry.getValue();
				DataSourceConfig dataSourceConfig = dataInputConfig.getProviderDataSourceConfigList().get(dsrIndex)
						.getDataSourceConfig();

				dataSourceName = dataSourceReader.getName();
				dataSourceRecordList = dataSourceReader.getNewItems();
				for (final DataSourceRecord dataSourceRecord : dataSourceRecordList) {
					try {
						DataInputRecord dataInputRecord = parseDataSource(dataSourceRecord, dataSourceConfig, dataSourceReader);
						String recordId = dataSourceRecord.getDataSourceRecordId();
						// Copy recordId from DataSourceRecord to DataInputRecord
						dataInputRecord.setDataInputRecordId(recordId);
						// Store recorId with reader index in map
						readerIndexToRecordIdListMap.get(dsrIndex).add(recordId);

						dataInputRecordList.add(dataInputRecord);
					} catch (Exception ex) {
						statusData = new StatusData(false,
								"Error occurred while parsing record from datasource: " + dataSourceName, ex);
						statusDataList.add(statusData);
					}

				}
			} catch (Exception ex) {
				statusData = new StatusData(false,
						"Error occurred while getting new items from datasource: " + dataSourceName, ex);
				statusDataList.add(statusData);
			}
		}
		List<StatusData> statusDataWithExceptionList = statusDataList.stream()
				.filter(x -> x.getExceptionObject() != null).collect(Collectors.toList());
		if (statusDataWithExceptionList.size() > 0) {
			String errorMessage = "";
			for (StatusData statusData2 : statusDataWithExceptionList) {
				errorMessage += statusData2.getErrorMessage() + "|" + statusData2.getExceptionObject().getMessage();
			}
			throw new DataInputException(errorMessage);
		}
		return dataInputRecordList;
	}

	@Override
	public List<Boolean> updateItemAsRead(String dataInputRecordId) throws DataInputException {
		int dataSourceReaderIndex = getDataSourceReaderIndex(dataInputRecordId);

		// Validate and throw error if no DSR index found
		if (dataSourceReaderIndex == DATA_SOURCE_READER_INDEX_NONE) {
			String errorMessage = "Invalid data provied => dataInputRecord=" + dataInputRecordId;
			throw new DataInputException(errorMessage);
		}

		// Frame function for execution
		IDataSourceReaderOperationReturnBoolean dataSourceReaderOperation = (
				IDataSourceReader dataSourceReader) -> dataSourceReader.updateItemAsRead(dataInputRecordId);

		// Call for execution of framed function
		List<Boolean> resultList = executeDataSourceOperationAndReturnBoolean(dataSourceReaderOperation,
				Arrays.asList(dataSourceReaderIndex));
		return resultList;
	}

	@Override
	public List<Boolean> updateItemAsUnread(String dataInputRecordId) throws DataInputException {
		int dataSourceReaderIndex = getDataSourceReaderIndex(dataInputRecordId);

		// Validate and throw error if no DSR index found
		if (dataSourceReaderIndex == DATA_SOURCE_READER_INDEX_NONE) {
			String errorMessage = "Invalid data provied => dataInputRecord=" + dataInputRecordId;
			throw new DataInputException(errorMessage);
		}

		// Frame function for execution
		IDataSourceReaderOperationReturnBoolean dataSourceReaderOperation = (
				IDataSourceReader dataSourceReader) -> dataSourceReader.updateItemAsUnread(dataInputRecordId);

		// Call for execution of framed function
		List<Boolean> resultList = executeDataSourceOperationAndReturnBoolean(dataSourceReaderOperation,
				Arrays.asList(dataSourceReaderIndex));
		return resultList;
	}



	@Override
	public List<Boolean> createFolder(DataSourceFolder dataSourceFolder) throws DataInputException {
		// Frame function for execution
		IDataSourceReaderOperationReturnBoolean dataSourceReaderOperation = (
				IDataSourceReader dataSourceReader) -> dataSourceReader.createFolder(dataSourceFolder);

		// Call for execution of framed function
		List<Boolean> resultList = executeDataSourceOperationAndReturnBoolean(dataSourceReaderOperation,
				DATA_SOURCE_READER_INDEX_ALL_LIST);
		return resultList;
	}

	@Override
	public List<Boolean> deleteFolder(DataSourceFolder dataSourceFolder) throws DataInputException {
		// Frame function for execution
		IDataSourceReaderOperationReturnBoolean dataSourceReaderOperation = (
				IDataSourceReader dataSourceReader) -> dataSourceReader.deleteFolder(dataSourceFolder);

		// Call for execution of framed function
		List<Boolean> resultList = executeDataSourceOperationAndReturnBoolean(dataSourceReaderOperation,
				DATA_SOURCE_READER_INDEX_ALL_LIST);
		return resultList;
	}

	@Override
	public List<Integer> moveItemToFolder(List<String> dataInputRecordIdList, DataSourceFolder sourceDataSourceFolder,
			DataSourceFolder targetDataSourceFolder) throws DataInputException {
		int dataSourceReaderIndex = getDataSourceReaderIndex(dataInputRecordIdList);

		IDataSourceReaderOperationReturnInt dataSourceReaderOperation = (
				IDataSourceReader dataSourceReader) -> dataSourceReader.moveItemToFolder(dataInputRecordIdList,
						sourceDataSourceFolder, targetDataSourceFolder);
		List<Integer> resultList = executeDataSourceOperationAndReturnInt(dataSourceReaderOperation,
				Arrays.asList(dataSourceReaderIndex));
		return resultList;
	}

	@Override
	public List<Integer> copyItemToFolder(List<String> dataInputRecordIdList, DataSourceFolder sourceDataSourceFolder,
			DataSourceFolder targetDataSourceFolder) throws DataInputException {
		int dataSourceReaderIndex = getDataSourceReaderIndex(dataInputRecordIdList);

		IDataSourceReaderOperationReturnInt dataSourceReaderOperation = (
				IDataSourceReader dataSourceReader) -> dataSourceReader.copyItemToFolder(dataInputRecordIdList,
						sourceDataSourceFolder, targetDataSourceFolder);
		List<Integer> resultList = executeDataSourceOperationAndReturnInt(dataSourceReaderOperation,
				Arrays.asList(dataSourceReaderIndex));
		return resultList;
	}

	private int getDataSourceReaderIndex(String dataInputRecordId) throws DataInputException {
		int dsrIndex = DATA_SOURCE_READER_INDEX_NONE;

		for (Map.Entry<Integer, List<String>> entry : readerIndexToRecordIdListMap.entrySet()) {
			if (entry.getValue().contains(dataInputRecordId)) {
				return entry.getKey();
			}
		}
		return dsrIndex;
	}

	private int getDataSourceReaderIndex(List<String> dataInputRecordIdList) throws DataInputException {
		List<Integer> dsrIndexList = new ArrayList<>();
		int dsrIndex;
		for (String dataInputRecordId : dataInputRecordIdList) {
			dsrIndex = getDataSourceReaderIndex(dataInputRecordId);
			if (dsrIndex == DATA_SOURCE_READER_INDEX_NONE) {
				String errorMessage = "Invalid data provied => dataInputRecord=" + dataInputRecordId;
				throw new DataInputException(errorMessage);
			} else {
				dsrIndexList.add(dsrIndex);
			}
		}

		List<Integer> uniqueDsrIndexList = dsrIndexList.stream().distinct().collect(Collectors.toList());

		if (uniqueDsrIndexList.size()!=1) {
			String errorMessage = "Multiple dataSourceReaders found for provided list of dataInputRecordId(s). Expecting only one.";
			throw new DataInputException(errorMessage);
		}
		
		return uniqueDsrIndexList.get(0);

	}

	/**
	 * Generic/reusable method to perform a given operation on all data sources and
	 * returns a list of <b>Boolean</b> values
	 * 
	 * @param dataSourceReaderOperation
	 * @return
	 * @throws DataInputException
	 */
	private List<Boolean> executeDataSourceOperationAndReturnBoolean(
			IDataSourceReaderOperationReturnBoolean dataSourceReaderOperation, List<Integer> dataSourceReaderIndexList)
			throws DataInputException {
		StatusData statusData;
		String name = "";
		boolean result;
		List<StatusData> statusDataList = new ArrayList<>();
		for (Map.Entry<Integer, IDataSourceReader> entry : providedDataSourceReaderMap.entrySet()) {
			try {
				int dsrIndex = entry.getKey();
				if (!dataSourceReaderIndexList.containsAll(DATA_SOURCE_READER_INDEX_ALL_LIST)) {
					if (!dataSourceReaderIndexList.contains(dsrIndex)) {
						continue;
					}
				}
				IDataSourceReader dataSourceReader = entry.getValue();
				name = dataSourceReader.getName();
				result = dataSourceReaderOperation.execute(dataSourceReader);
				statusData = new StatusData(result, null, null);
			} catch (Exception ex) {
				statusData = new StatusData(false, "Error occurred while executing operation on datasource: " + name,
						ex);
			}
			statusDataList.add(statusData);
		}
		List<StatusData> statusDataWithExceptionList = statusDataList.stream()
				.filter(x -> x.getExceptionObject() != null).collect(Collectors.toList());
		if (statusDataWithExceptionList.size() > 0) {
			String errorMessage = "";
			for (StatusData statusData2 : statusDataWithExceptionList) {
				errorMessage += statusData2.getErrorMessage() + "|" + statusData2.getExceptionObject().getMessage();
			}
			throw new DataInputException(errorMessage);
		}
		List<Boolean> resultList = statusDataList.stream().map(StatusData::isResult).collect(Collectors.toList());
		return resultList;
	}

	/**
	 * Generic/reusable method to perform a given operation on all data sources and
	 * returns a list of <b>Integer</b> values
	 * 
	 * @param dataSourceReaderOperation
	 * @return
	 * @throws DataInputException
	 */
	private List<Integer> executeDataSourceOperationAndReturnInt(
			IDataSourceReaderOperationReturnInt dataSourceReaderOperation, List<Integer> dataSourceReaderIndexList)
			throws DataInputException {
		StatusData statusData;
		String name = "";
		int result;
		List<StatusData> statusDataList = new ArrayList<>();
		for (Map.Entry<Integer, IDataSourceReader> entry : providedDataSourceReaderMap.entrySet()) {
			try {
				int dsrIndex = entry.getKey();
				if (!dataSourceReaderIndexList.containsAll(DATA_SOURCE_READER_INDEX_ALL_LIST)) {
					if (!dataSourceReaderIndexList.contains(dsrIndex)) {
						continue;
					}
				}

				IDataSourceReader dataSourceReader = entry.getValue();
				name = dataSourceReader.getName();
				result = dataSourceReaderOperation.execute(dataSourceReader);
				statusData = new StatusData(result, null, null);
			} catch (Exception ex) {
				statusData = new StatusData(0, "Error occurred while executing operation on datasource: " + name, ex);
			}
			statusDataList.add(statusData);
		}
		List<StatusData> statusDataWithExceptionList = statusDataList.stream()
				.filter(x -> x.getExceptionObject() != null).collect(Collectors.toList());
		if (statusDataWithExceptionList.size() > 0) {
			String errorMessage = "";
			for (StatusData statusData2 : statusDataWithExceptionList) {
				errorMessage += statusData2.getErrorMessage() + "|" + statusData2.getExceptionObject().getMessage();
			}
			throw new DataInputException(errorMessage);
		}
		List<Integer> resultList = statusDataList.stream().map(StatusData::getCount).collect(Collectors.toList());
		return resultList;
	}
	
	protected abstract DataInputRecord parseDataSource(DataSourceRecord dataSourceRecord,
			DataSourceConfig dataSourceConfig, IDataSourceReader dataSourceReader) throws DataInputException;

	protected static class StatusData {
		private boolean result;
		private int count;
		private String errorMessage;
		private Exception exceptionObject;

		public StatusData(boolean result, String errorMessage, Exception exceptionObject) {
			this.result = result;
			this.errorMessage = errorMessage;
			this.exceptionObject = exceptionObject;
		}

		public StatusData(int count, String errorMessage, Exception exceptionObject) {
			this.count = count;
			this.errorMessage = errorMessage;
			this.exceptionObject = exceptionObject;
		}

		public boolean isResult() {
			return result;
		}

		public int getCount() {
			return count;
		}

		public String getErrorMessage() {
			return errorMessage;
		}

		public Exception getExceptionObject() {
			return exceptionObject;
		}
	}
}
