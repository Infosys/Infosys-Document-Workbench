/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.datasource.impl.filesystem.basic.api;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.infosys.ainauto.commonutils.FileUtility;
import com.infosys.ainauto.datasource.api.IDataSourceWriter;
import com.infosys.ainauto.datasource.common.DataSourceException;
import com.infosys.ainauto.datasource.config.DataSourceConfig;
import com.infosys.ainauto.datasource.config.file.FileSystemDataSourceWriterConfig;
import com.infosys.ainauto.datasource.model.DataSourceRecord;
import com.infosys.ainauto.datasource.model.file.FileSystemDataSourceRecord;

public class FileSystemDataSourceWriter implements IDataSourceWriter {

	private final static Logger logger = LoggerFactory.getLogger(FileSystemDataSourceWriter.class);
	private String name;
	private FileSystemDataSourceWriterConfig fileSystemDataSourceWriterConfig;
	private File saveToPath;

	public FileSystemDataSourceWriter(String name, DataSourceConfig dataSourceWriterConfig) {
		this.name = name;
		this.fileSystemDataSourceWriterConfig = (FileSystemDataSourceWriterConfig) dataSourceWriterConfig;
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * @Description this method is used to connect to source dir
	 */
	@Override
	public boolean connect() throws DataSourceException {
		boolean operationResult = false; // Assume operation will fail
		try {

			String saveToPath = fileSystemDataSourceWriterConfig.getSaveToPath();
			this.saveToPath = new File(saveToPath);
		} catch (Exception e) {
			logger.error("Error occurred in connect method", e);
			if (fileSystemDataSourceWriterConfig.isOnConnectionErrorIgnore()) {
				return operationResult;
			}
			throw new DataSourceException("Error occurred in connect method", e);
		}

		if (saveToPath.exists()) {
			logger.info("Connected to source directory: " + saveToPath.getAbsolutePath());
		} else {
			logger.error("Source directory does not exist: " + saveToPath.getAbsolutePath());
			if (fileSystemDataSourceWriterConfig.isOnConnectionErrorIgnore()) {
				return operationResult;
			}
			throw new DataSourceException("Source directory does not exist: " + saveToPath.getAbsolutePath());

		}
		operationResult = true;
		return operationResult;
	}

	@Override
	public boolean disconnect() throws DataSourceException {
		this.saveToPath= null;
		return true;
	}

	@Override
	public DataSourceRecord generateNewItem() throws DataSourceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean writeItem(DataSourceRecord dataSourceRecord, Map<String, String> paramsMap)
			throws DataSourceException {

		boolean operationResult = false; // Assume operation will fail

		FileSystemDataSourceRecord fileSystemDataSourceRecord = (FileSystemDataSourceRecord) dataSourceRecord;
		String fileFullPath = FileUtility.cleanPath(this.saveToPath + "/"
				+ fileSystemDataSourceRecord.getFileNameToSave());

		try (OutputStream outStream = new FileOutputStream(new File(fileFullPath))) {
			outStream.write(fileSystemDataSourceRecord.getFileContent());
		} catch (Exception ex) {
			logger.error("Error while calling writeItem", ex);
			throw new DataSourceException("Error while calling writeItem", ex);
		}
		operationResult = true;
		return operationResult;
	}
}
