/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.datainout.model;

import java.util.List;

import com.infosys.ainauto.datainout.model.email.EmailRecord;
import com.infosys.ainauto.datainout.model.file.FileRecord;

public class DataInputRecord {
	private String dataInputRecordId;
	private EmailRecord emailRecord;
	private FileRecord fileRecord;
	private List<DataInputRecord> dataInputRecordList;

	public String getDataInputRecordId() {
		return dataInputRecordId;
	}

	public void setDataInputRecordId(String dataInputRecordId) {
		this.dataInputRecordId = dataInputRecordId;
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

	public List<DataInputRecord> getDataInputRecordList() {
		return dataInputRecordList;
	}

	public void setDataInputRecordList(List<DataInputRecord> dataInputRecordList) {
		this.dataInputRecordList = dataInputRecordList;
	}

}