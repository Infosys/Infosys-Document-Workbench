/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.datainout.config;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.infosys.ainauto.datasource.config.DataSourceConfig;

public abstract class AbstractDataInOutConfig {

	private List<ProviderDataSourceConfig> providerDataSourceConfigList = new ArrayList<>();

	public List<ProviderDataSourceConfig> getProviderDataSourceConfigList() {
		return providerDataSourceConfigList;
	}

	public void addToProviderDataSourceConfigList(ProviderDataSourceConfig providerDataSourceConfig) {
		providerDataSourceConfigList.add(providerDataSourceConfig);
	}

	public static class ProviderDataSourceConfig {
		private final String providerClassFullName;
		private final DataSourceConfig dataSourceConfig;
		private final String configId;

		public ProviderDataSourceConfig(String providerClassFullName, DataSourceConfig dataSourceConfig) {
			super();
			this.providerClassFullName = providerClassFullName;
			this.dataSourceConfig = dataSourceConfig;
			// Generate unique string to be used as config id
			String configId = UUID.randomUUID().toString();
			this.dataSourceConfig.setConfigId(configId);
			this.configId = configId;
		}

		public String getProviderClassFullName() {
			return providerClassFullName;
		}

		public DataSourceConfig getDataSourceConfig() {
			return dataSourceConfig;
		}

		public String getConfigId() {
			return configId;
		}
	}
}
