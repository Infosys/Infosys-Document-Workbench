/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.datasource.impl.filesystem.basic.common;

import java.util.Properties;

import com.infosys.ainauto.datasource.DataSourceApi;
import com.infosys.ainauto.datasource.api.IDataSourceReader;
import com.infosys.ainauto.datasource.api.IDataSourceWriter;
import com.infosys.ainauto.datasource.config.file.FileSystemDataSourceReaderConfig;
import com.infosys.ainauto.datasource.config.file.FileSystemDataSourceWriterConfig;
import com.infosys.ainauto.datasource.impl.filesystem.basic.api.FileSystemDataSourceReader;
import com.infosys.ainauto.datasource.impl.filesystem.basic.api.FileSystemDataSourceWriter;
import com.infosys.ainauto.datasource.spi.IDataSourceProvider;
import com.infosys.ainauto.testutils.FileTestUtility;

public class TestHelper {

	private static final String PROVIDER_CLASS_NAME_DATA_SOURCE_FILE_SYSTEM = "com.infosys.ainauto.datasource.impl.filesystem.basic.api.FileSystemDataSourceProvider";

	public static FileSystemDataSourceReaderConfig getFileSystemDataSourceReaderConfig(String propertiesFileName)
			throws Exception {

		Properties properties = FileTestUtility.readPropertiesFileFromClassPath(propertiesFileName);

		FileSystemDataSourceReaderConfig fileSystemDataSourceConfig = new FileSystemDataSourceReaderConfig();
		fileSystemDataSourceConfig.setFileSourceDir(properties.getProperty("ds.file-system.reader.dir.source"));
		fileSystemDataSourceConfig.setFileArchivalDir(properties.getProperty("ds.file-system.reader.dir.archival"));
		fileSystemDataSourceConfig.setFileSourcePermanentSubDir(
				properties.getProperty("ds.file-system.reader.dir.source.permanent.subfolders"));
		fileSystemDataSourceConfig.setFileTempDir(properties.getProperty("ds.file-system.reader.dir.temp"));

		return fileSystemDataSourceConfig;
	}

	public static FileSystemDataSourceWriterConfig getFileSystemDataSourceWriterConfig(String propertiesFileName)
			throws Exception {

		Properties properties = FileTestUtility.readPropertiesFileFromClassPath(propertiesFileName);

		FileSystemDataSourceWriterConfig fileSystemDataSourceWriterConfig = new FileSystemDataSourceWriterConfig();
		fileSystemDataSourceWriterConfig.setSaveToPath(properties.getProperty("ds.file-system.writer.dir.target"));

		return fileSystemDataSourceWriterConfig;
	}

	public static IDataSourceReader getDataSourceReader(
			FileSystemDataSourceReaderConfig fileSystemDataSourceReaderConfig) {
		IDataSourceProvider dataSourceProvider = DataSourceApi
				.getProviderByClassName(PROVIDER_CLASS_NAME_DATA_SOURCE_FILE_SYSTEM);
		IDataSourceReader dataSourceReader = (FileSystemDataSourceReader) dataSourceProvider
				.getDataSourceReader(PROVIDER_CLASS_NAME_DATA_SOURCE_FILE_SYSTEM, fileSystemDataSourceReaderConfig);
		return dataSourceReader;
	}

	public static IDataSourceWriter getDataSourceWriter(
			FileSystemDataSourceWriterConfig fileSystemDataSourceWriterConfig) {
		IDataSourceProvider dataSourceProvider = DataSourceApi
				.getProviderByClassName(PROVIDER_CLASS_NAME_DATA_SOURCE_FILE_SYSTEM);
		IDataSourceWriter dataSourceWriter = (FileSystemDataSourceWriter) dataSourceProvider
				.getDataSourceWriter(PROVIDER_CLASS_NAME_DATA_SOURCE_FILE_SYSTEM, fileSystemDataSourceWriterConfig);
		return dataSourceWriter;
	}
}
