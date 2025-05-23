/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.api.attachment;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public class AttachmentReqData {

	private List<MultipartFile> multipartFileList;
	private int docId;
	private boolean isInlineImage;
	private List<Integer> extractTypeCdeList;
	private String groupName;
	private boolean isPrimary;
	
	
	public boolean isPrimary() {
		return isPrimary;
	}

	public void setPrimary(boolean isPrimary) {
		this.isPrimary = isPrimary;
	}

	public boolean isInlineImage() {
		return isInlineImage;
	}

	public void setInlineImage(boolean isInlineImage) {
		this.isInlineImage = isInlineImage;
	}

	public int getDocId() {
		return docId;
	}

	public void setDocId(int docId) {
		this.docId = docId;
	}

	public List<MultipartFile> getMultipartFileList() {
		return multipartFileList;
	}

	public void setMultipartFileList(List<MultipartFile> multipartFileList) {
		this.multipartFileList = multipartFileList;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public List<Integer> getExtractTypeCdeList() {
		return extractTypeCdeList;
	}

	public void setExtractTypeCdeList(List<Integer> extractTypeCdeList) {
		this.extractTypeCdeList = extractTypeCdeList;
	}

}
