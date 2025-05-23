/** =============================================================================================================== *
 * Copyright 2023 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.api.attachment;

public class AttaAttaRelReqData {
	private int docId;
	private long attachmentId1;
	private long attachmentId2;
	private int attaRelTypeCde;
	
	public int getDocId() {
		return docId;
	}
	public void setDocId(int docId) {
		this.docId = docId;
	}
	public long getAttachmentId1() {
		return attachmentId1;
	}
	public void setAttachmentId1(long attachmentId1) {
		this.attachmentId1 = attachmentId1;
	}
	public long getAttachmentId2() {
		return attachmentId2;
	}
	public void setAttachmentId2(long attachmentId2) {
		this.attachmentId2 = attachmentId2;
	}
	public int getAttaRelTypeCde() {
		return attaRelTypeCde;
	}
	public void setAttaRelTypeCde(int attaRelTypeCde) {
		this.attaRelTypeCde = attaRelTypeCde;
	}
	
}
