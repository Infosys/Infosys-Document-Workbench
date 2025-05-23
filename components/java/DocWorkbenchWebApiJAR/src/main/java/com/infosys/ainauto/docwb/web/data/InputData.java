/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.web.data;

import java.util.List;

public class InputData {

	private FileData fileData;
	private EmailData emailData;
	private List<InputData> inputDataList;

	public FileData getFileData() {
		return fileData;
	}

	public void setFileData(FileData fileData) {
		this.fileData = fileData;
	}

	public EmailData getEmailData() {
		return emailData;
	}

	public void setEmailData(EmailData emailData) {
		this.emailData = emailData;
	}

	public List<InputData> getInputDataList() {
		return inputDataList;
	}

	public void setInputDataList(List<InputData> inputDataList) {
		this.inputDataList = inputDataList;
	}

}
