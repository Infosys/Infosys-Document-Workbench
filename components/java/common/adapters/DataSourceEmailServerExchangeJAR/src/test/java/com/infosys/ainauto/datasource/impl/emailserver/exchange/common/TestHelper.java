/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.datasource.impl.emailserver.exchange.common;

import java.util.Properties;

import com.infosys.ainauto.datasource.DataSourceApi;
import com.infosys.ainauto.datasource.api.IDataSourceReader;
import com.infosys.ainauto.datasource.api.IDataSourceWriter;
import com.infosys.ainauto.datasource.config.DataSourceConfig;
import com.infosys.ainauto.datasource.config.email.EmailServerDataSourceReaderConfig;
import com.infosys.ainauto.datasource.config.email.EmailServerDataSourceWriterConfig;
import com.infosys.ainauto.datasource.impl.emailserver.exchange.api.EmailServerDataSourceReader;
import com.infosys.ainauto.datasource.impl.emailserver.exchange.api.EmailServerDataSourceWriter;
import com.infosys.ainauto.datasource.spi.IDataSourceProvider;
import com.infosys.ainauto.testutils.FileTestUtility;

public class TestHelper {

	private static final String PROVIDER_CLASS_NAME_DATA_SOURCE_EXCHANGE_ONPREM = "com.infosys.ainauto.datasource.impl.exchangeonprem.basic.api.ExchangeOnpremDataSourceProvider";

	public static EmailServerDataSourceReaderConfig getExchangeOnpremDataSourceReaderConfig(String propertiesFileName)
			throws Exception {

		Properties properties = FileTestUtility.readPropertiesFileFromClassPath(propertiesFileName);

		EmailServerDataSourceReaderConfig emailServerDataSourceReaderConfig = new EmailServerDataSourceReaderConfig();
		emailServerDataSourceReaderConfig
				.setMailboxDomain(properties.getProperty("ds.email-server.exchange.reader.domain"));
		emailServerDataSourceReaderConfig
				.setMailboxPassword(properties.getProperty("ds.email-server.exchange.reader.password"));
		emailServerDataSourceReaderConfig
				.setMailboxUserName(properties.getProperty("ds.email-server.exchange.reader.username"));
		emailServerDataSourceReaderConfig
				.setServiceUri(properties.getProperty("ds.email-server.exchange.reader.service.uri"));
		return emailServerDataSourceReaderConfig;
	}

	public static EmailServerDataSourceWriterConfig getExchangeOnpremDataSourceWriterConfig(String propertiesFileName)
			throws Exception {

//		Properties properties = FileTestUtility.readPropertiesFileFromClassPath(propertiesFileName);

		EmailServerDataSourceWriterConfig emailServerDataSourceWriterConfig = new EmailServerDataSourceWriterConfig();

		return emailServerDataSourceWriterConfig;
	}

	public static IDataSourceReader getDataSourceReader(DataSourceConfig dataSourceReaderConfig) {
		IDataSourceProvider dataSourceProvider = DataSourceApi
				.getProviderByClassName(PROVIDER_CLASS_NAME_DATA_SOURCE_EXCHANGE_ONPREM);
		IDataSourceReader dataSourceReader = (EmailServerDataSourceReader) dataSourceProvider
				.getDataSourceReader(PROVIDER_CLASS_NAME_DATA_SOURCE_EXCHANGE_ONPREM, dataSourceReaderConfig);
		return dataSourceReader;
	}

	public static IDataSourceWriter getDataSourceWriter(DataSourceConfig dataSourceWriterConfig) {
		IDataSourceProvider dataSourceProvider = DataSourceApi
				.getProviderByClassName(PROVIDER_CLASS_NAME_DATA_SOURCE_EXCHANGE_ONPREM);
		IDataSourceWriter dataSourceWriter = (EmailServerDataSourceWriter) dataSourceProvider
				.getDataSourceWriter(PROVIDER_CLASS_NAME_DATA_SOURCE_EXCHANGE_ONPREM, dataSourceWriterConfig);
		return dataSourceWriter;
	}

}
