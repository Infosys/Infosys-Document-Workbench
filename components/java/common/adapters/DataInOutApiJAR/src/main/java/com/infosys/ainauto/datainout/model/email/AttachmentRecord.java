/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.datainout.model.email;

public class AttachmentRecord {

	private String actualFileName;
	private String storedFileName;
	private String storedFileFullPath;

	public String getActualFileName() {
		return actualFileName;
	}

	public void setActualFileName(String actualFileName) {
		this.actualFileName = actualFileName;
	}

	public String getStoredFileName() {
		return storedFileName;
	}

	public void setStoredFileName(String storedFileName) {
		this.storedFileName = storedFileName;
	}

	public String getStoredFileFullPath() {
		return storedFileFullPath;
	}

	public void setStoredFileFullPath(String storedFileFullPath) {
		this.storedFileFullPath = storedFileFullPath;
	}

	@Override
	public String toString() {
		return "AttachmentRecord [actualFileName=" + actualFileName + ", storedFileName=" + storedFileName
				+ ", storedFileFullPath=" + storedFileFullPath + "]";
	}

}
