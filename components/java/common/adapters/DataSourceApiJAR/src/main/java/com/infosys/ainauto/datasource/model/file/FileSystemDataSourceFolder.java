/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.datasource.model.file;

import com.infosys.ainauto.datasource.model.DataSourceFolder;


public class FileSystemDataSourceFolder extends DataSourceFolder {

	private String folderName;
	
	public FileSystemDataSourceFolder(String folderName) {
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
