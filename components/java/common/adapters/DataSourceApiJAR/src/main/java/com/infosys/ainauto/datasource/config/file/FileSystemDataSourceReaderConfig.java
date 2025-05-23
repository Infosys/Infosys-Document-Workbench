/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.datasource.config.file;

import com.infosys.ainauto.datasource.config.DataSourceConfig;

public class FileSystemDataSourceReaderConfig extends DataSourceConfig {
	private String fileSourceDir;
	private String fileArchivalDir;
	private String fileSourcePermanentSubDir;
	private String fileTempDir;

	public String getFileSourcePermanentSubDir() {
		return fileSourcePermanentSubDir;
	}

	public void setFileSourcePermanentSubDir(String fileSourcePermanentSubDir) {
		this.fileSourcePermanentSubDir = fileSourcePermanentSubDir;
	}

	public String getFileSourceDir() {
		return fileSourceDir;
	}

	public void setFileSourceDir(String fileSourceDir) {
		this.fileSourceDir = fileSourceDir;
	}

	public String getFileArchivalDir() {
		return fileArchivalDir;
	}

	public void setFileArchivalDir(String fileArchivalDir) {
		this.fileArchivalDir = fileArchivalDir;
	}

	public String getFileTempDir() {
		return fileTempDir;
	}

	public void setFileTempDir(String fileTempDir) {
		this.fileTempDir = fileTempDir;
	}

}