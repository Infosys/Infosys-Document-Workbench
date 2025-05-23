/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.datasource.model.email;

import com.infosys.ainauto.datasource.model.DataSourceRecord;

public class EmailServerDataSourceRecord extends DataSourceRecord {

	private Object message;
	private Object mimeMessage;

	public EmailServerDataSourceRecord(String recordId, Object message, Object mimeMessage) {
		this.setDataSourceRecordId(recordId);
		this.message = message;
		this.mimeMessage = mimeMessage;
	}

	public Object getMessage() {
		return message;
	}

	public void setMessage(Object message) {
		this.message = message;
	}

	public Object getMimeMessage() {
		return mimeMessage;
	}

	public void setMimeMessage(Object mimeMessage) {
		this.mimeMessage = mimeMessage;
	}
}
