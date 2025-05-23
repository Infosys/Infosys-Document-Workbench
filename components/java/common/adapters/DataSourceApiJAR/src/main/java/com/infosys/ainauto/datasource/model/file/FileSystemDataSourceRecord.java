/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.datasource.model.file;

import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import com.infosys.ainauto.datasource.model.DataSourceRecord;

public class FileSystemDataSourceRecord extends DataSourceRecord {

	private String fileName;
	private String fileSubPath;
	private String fileAbsolutePath;
	private BasicFileAttributes basicFileAttributes;
	private byte[] fileContentToSave;
	private String fileNameToSave;

	private List<FileSystemDataSourceRecord> fileSystemDataSourceRecordList;
	private boolean hasChildren;
	private boolean isTempFile;

	public String getFileAbsolutePath() {
		return fileAbsolutePath;
	}

	public void setFileAbsolutePath(String fileAbsolutePath) {
		this.fileAbsolutePath = fileAbsolutePath;
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

	public BasicFileAttributes getBasicFileAttributes() {
		return basicFileAttributes;
	}

	public void setBasicFileAttributes(BasicFileAttributes basicFileAttributes) {
		this.basicFileAttributes = basicFileAttributes;
	}

	public byte[] getFileContent() {
		return fileContentToSave;
	}

	public void setFileContent(byte[] fileContent) {
		this.fileContentToSave = fileContent;
	}

	public String getFileNameToSave() {
		return fileNameToSave;
	}

	public void setFileNameToSave(String fileNameToSave) {
		this.fileNameToSave = fileNameToSave;
	}

	public List<FileSystemDataSourceRecord> getFileSystemDataSourceRecordList() {
		return fileSystemDataSourceRecordList;
	}

	public void setFileSystemDataSourceRecordList(List<FileSystemDataSourceRecord> fileSystemDataSourceRecordList) {
		this.fileSystemDataSourceRecordList = fileSystemDataSourceRecordList;
	}

	public boolean isHasChildren() {
		return hasChildren;
	}

	public void setHasChildren(boolean hasChildren) {
		this.hasChildren = hasChildren;
	}

	public boolean isTempFile() {
		return isTempFile;
	}

	public void setTempFile(boolean isTempFile) {
		this.isTempFile = isTempFile;
	}

}
