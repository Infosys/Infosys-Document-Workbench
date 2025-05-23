/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.datasource.model;

public abstract class DataSourceRecord{

	private String dataSourceRecordId;
	private String dataSourceRecordType;
	
	public DataSourceRecord() {
		this.dataSourceRecordType = this.getClass().getCanonicalName();
	}
	
	public String getDataSourceRecordType() {
		return dataSourceRecordType;
	}
	
	public String getDataSourceRecordId() {
		return dataSourceRecordId;
	}

	public void setDataSourceRecordId(String dataSourceRecordId) {
		this.dataSourceRecordId = dataSourceRecordId;
	}
}
