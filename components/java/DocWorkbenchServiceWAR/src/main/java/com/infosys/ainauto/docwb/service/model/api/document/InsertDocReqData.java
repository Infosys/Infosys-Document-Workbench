/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.api.document;

import java.util.List;

public class InsertDocReqData {

	private List<InsertDocData> attributes;
	private String docLocation;
	private int docTypeCde;
	private int lockStatusCde;
	private int queueNameCde;

	public List<InsertDocData> getAttributes() {
		return attributes;
	}
	public void setAttributes(List<InsertDocData> attributes) {
		this.attributes = attributes;
	}
	public String getDocLocation() {
		return docLocation;
	}
	public void setDocLocation(String docLocation) {
		this.docLocation = docLocation;
	}
	public int getDocTypeCde() {
		return docTypeCde;
	}
	public void setDocTypeCde(int docTypeCde) {
		this.docTypeCde = docTypeCde;
	}
	public int getLockStatusCde() {
		return lockStatusCde;
	}
	public void setLockStatusCde(int lockStatusCde) {
		this.lockStatusCde = lockStatusCde;
	}
	public int getQueueNameCde() {
		return queueNameCde;
	}
	public void setQueueNameCde(int queueNameCde) {
		this.queueNameCde = queueNameCde;
	}
}
