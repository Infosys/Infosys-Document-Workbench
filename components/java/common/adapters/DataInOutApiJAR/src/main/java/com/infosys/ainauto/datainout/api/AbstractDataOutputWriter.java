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

import com.infosys.ainauto.datainout.common.DataOutputException;
import com.infosys.ainauto.datainout.common.IDataSourceWriterOperationReturnBoolean;
import com.infosys.ainauto.datainout.config.AbstractDataInOutConfig.ProviderDataSourceConfig;
import com.infosys.ainauto.datainout.config.DataOutputConfig;
import com.infosys.ainauto.datainout.model.DataOutputRecord;
import com.infosys.ainauto.datasource.DataSourceApi;
import com.infosys.ainauto.datasource.api.IDataSourceWriter;
import com.infosys.ainauto.datasource.config.DataSourceConfig;
import com.infosys.ainauto.datasource.model.DataSourceRecord;
import com.infosys.ainauto.datasource.spi.IDataSourceProvider;

public abstract class AbstractDataOutputWriter implements IDataOutputWriter {

	private final static Logger logger = LoggerFactory.getLogger(AbstractDataOutputWriter.class);
	private static final List<Integer> DATA_SOURCE_WRITER_INDEX_ALL_LIST = Arrays.asList(-100);
	protected DataOutputConfig dataOutputConfig;
	protected TreeMap<Integer, IDataSourceWriter> providedDataSourceWriterMap;

	protected AbstractDataOutputWriter(DataOutputConfig dataOutputConfig) {
		this.dataOutputConfig = dataOutputConfig;
		initialize();
	}

	private void initialize() {
		providedDataSourceWriterMap = new TreeMap<>();
		for (int i = 0; i < dataOutputConfig.getProviderDataSourceConfigList().size(); i++) {
			ProviderDataSourceConfig providerDataSourceConfig = dataOutputConfig.getProviderDataSourceConfigList()
					.get(i);
			IDataSourceProvider dataSourceProvider = DataSourceApi
					.getProviderByClassName(providerDataSourceConfig.getProviderClassFullName());

			IDataSourceWriter dataSourceWriter = dataSourceProvider.getDataSourceWriter(
					providerDataSourceConfig.getProviderClassFullName(),
					providerDataSourceConfig.getDataSourceConfig());

			providedDataSourceWriterMap.put(i, dataSourceWriter);
		}
		logger.info("Initialized");
	}

	@Override
	public List<Boolean> connect() throws DataOutputException {
		IDataSourceWriterOperationReturnBoolean dataSourceWriterOperation = (
				IDataSourceWriter dataSourceWriter) -> dataSourceWriter.connect();
		List<Boolean> resultList = executeDataSourceOperationAndReturnBoolean(dataSourceWriterOperation,
				DATA_SOURCE_WRITER_INDEX_ALL_LIST);
		return resultList;
	}

	@Override
	public List<Boolean> disconnect() throws DataOutputException {
		IDataSourceWriterOperationReturnBoolean dataSourceWriterOperation = (
				IDataSourceWriter dataSourceWriter) -> dataSourceWriter.disconnect();
		List<Boolean> resultList = executeDataSourceOperationAndReturnBoolean(dataSourceWriterOperation,
				DATA_SOURCE_WRITER_INDEX_ALL_LIST);
		return resultList;
	}

	@Override
	public boolean writeItem(DataOutputRecord dataOutputRecord, Map<String, String> paramsMap) throws DataOutputException {
		boolean operationResult = false; // Assume it will fail
		StatusData statusData = null;
		String dataSourceName = "";
		List<StatusData> statusDataList = new ArrayList<>();

		for (Map.Entry<Integer, IDataSourceWriter> entry : providedDataSourceWriterMap.entrySet()) {
			try {
				Integer dsrIndex = entry.getKey();
				IDataSourceWriter dataSourceWriter = entry.getValue();
				DataSourceConfig dataSourceConfig = dataOutputConfig.getProviderDataSourceConfigList().get(dsrIndex)
						.getDataSourceConfig();
				dataSourceName = dataSourceWriter.getName();

				DataSourceRecord dataSourceRecord = parseDataSource(dataOutputRecord, dataSourceConfig,
						dataSourceWriter);
				// Save record only if parse method returns a not null object
				if (dataSourceRecord!=null) {
					operationResult = dataSourceWriter.writeItem(dataSourceRecord, paramsMap);
				}
			} catch (Exception ex) {
				statusData = new StatusData(false,
						"Error occurred while parsing record from datasource: " + dataSourceName, ex);
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
			throw new DataOutputException(errorMessage);
		}

		return operationResult;
	}

	protected abstract DataSourceRecord parseDataSource(DataOutputRecord dataOutputRecord,
			DataSourceConfig dataSourceConfig, IDataSourceWriter dataSourceWriter) throws DataOutputException;

	/**
	 * Generic/reusable method to perform a given operation on all data sources and
	 * returns a list of <b>Boolean</b> values
	 * 
	 * @param dataSourceWriterOperation
	 * @return
	 * @throws DataOutputException
	 */
	private List<Boolean> executeDataSourceOperationAndReturnBoolean(
			IDataSourceWriterOperationReturnBoolean dataSourceWriterOperation, List<Integer> dataSourceWriterIndexList)
			throws DataOutputException {
		StatusData statusData;
		String name = "";
		boolean result;
		List<StatusData> statusDataList = new ArrayList<>();
		for (Map.Entry<Integer, IDataSourceWriter> entry : providedDataSourceWriterMap.entrySet()) {
			try {
				int dsrIndex = entry.getKey();
				if (!dataSourceWriterIndexList.containsAll(DATA_SOURCE_WRITER_INDEX_ALL_LIST)) {
					if (!dataSourceWriterIndexList.contains(dsrIndex)) {
						continue;
					}
				}
				IDataSourceWriter dataSourceWriter = entry.getValue();
				name = dataSourceWriter.getName();
				result = dataSourceWriterOperation.execute(dataSourceWriter);
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
			throw new DataOutputException(errorMessage);
		}
		List<Boolean> resultList = statusDataList.stream().map(StatusData::isResult).collect(Collectors.toList());
		return resultList;
	}

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
