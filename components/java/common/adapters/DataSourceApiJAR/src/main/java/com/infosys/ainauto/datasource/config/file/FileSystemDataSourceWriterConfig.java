/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.datasource.config.file;

import com.infosys.ainauto.datasource.config.DataSourceConfig;

public class FileSystemDataSourceWriterConfig extends DataSourceConfig {
	private String saveToPath;

	public String getSaveToPath() {
		return saveToPath;
	}

	public void setSaveToPath(String saveToPath) {
		this.saveToPath = saveToPath;
	}
	
}