/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.datasource.config;

/**
 * Abstract class to be implemented by data source provider 
 *
 */
public abstract class DataSourceConfig {
	
	private String configId;
	private boolean onConnectionErrorIgnore = false;

	public String getConfigId() {
		return configId;
	}

	public void setConfigId(String configId) {
		this.configId = configId;
	}
	
	/**
	 * Decides whether to raise or swallow exception related to connection
	 * @return
	 */
	public boolean isOnConnectionErrorIgnore() {
		return onConnectionErrorIgnore;
	}

	/**
	 * Decides whether to raise or swallow exception related to connection
	 * @param onConnectionErrorIgnore
	 */
	public void setOnConnectionErrorIgnore(boolean onConnectionErrorIgnore) {
		this.onConnectionErrorIgnore = onConnectionErrorIgnore;
	}
}
