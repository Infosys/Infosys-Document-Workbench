/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.datainout.model;

import com.infosys.ainauto.datainout.model.email.EmailRecord;
import com.infosys.ainauto.datainout.model.file.FileRecord;

public class DataOutputRecord {
	private String dataOutputRecordId;
	private EmailRecord emailRecord;
	private FileRecord fileRecord;

	public String getDataOutputRecordId() {
		return dataOutputRecordId;
	}

	public void setDataOutputRecordId(String dataOutputRecordId) {
		this.dataOutputRecordId = dataOutputRecordId;
	}

	public EmailRecord getEmailRecord() {
		return emailRecord;
	}

	public void setEmailRecord(EmailRecord emailRecord) {
		this.emailRecord = emailRecord;
	}

	public FileRecord getFileRecord() {
		return fileRecord;
	}

	public void setFileRecord(FileRecord fileRecord) {
		this.fileRecord = fileRecord;
	}
}