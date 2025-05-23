/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.web.data;

import java.nio.file.attribute.FileTime;

public class FileData {

	private String fileName;
	private String fileSubPath;
	private String fileAbsolutePath;
	private String fileId;
	private FileTime fileCreationTime;
	private boolean hasChildren;

	public String getFileAbsolutePath() {
		return fileAbsolutePath;
	}

	public void setFileAbsolutePath(String fileAbsolutePath) {
		this.fileAbsolutePath = fileAbsolutePath;
	}

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileSubPath() {
		return fileSubPath;
	}

	public void setFileSubPath(String fileSubPath) {
		this.fileSubPath = fileSubPath;
	}

	public FileTime getFileCreationTime() {
		return fileCreationTime;
	}

	public void setFileCreationTime(FileTime fileCreationTime) {
		this.fileCreationTime = fileCreationTime;
	}

	public boolean isHasChildren() {
		return hasChildren;
	}

	public void setHasChildren(boolean hasChildren) {
		this.hasChildren = hasChildren;
	}

}
