/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.datasource.model.email;

import com.infosys.ainauto.datasource.model.DataSourceFolder;

public class EmailServerDataSourceFolder extends DataSourceFolder {

	private String folderName;

	public EmailServerDataSourceFolder(String folderName) {
		super();
		this.folderName = folderName;
	}

	public String getFolderName() {
		return folderName;
	}

	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}
}
