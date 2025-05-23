/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.web.api;

import java.util.List;

import com.infosys.ainauto.docwb.web.data.AttachmentData;
import com.infosys.ainauto.docwb.web.data.DocumentData;
import com.infosys.ainauto.docwb.web.exception.DocwbWebException;
import com.infosys.ainauto.docwb.web.type.EnumFileType;

public interface IAttachmentService {

	/**
	 * Method adds attachments and inline images to a specified group of the case.
	 */
	public long addAttachmentToGroup(DocumentData documentData, boolean isInlineImage, String groupName)
			throws DocwbWebException;

	/**
	 * Method adds attachments and inline images to a case.
	 */
	public long addUngroupedAttachment(DocumentData documentData, boolean isInlineImage) throws DocwbWebException;

	/**
	 * Method adds inline images and attachments together to a new group on the
	 * case.
	 */
	public long addGroupedAttachments(DocumentData documentData, boolean isInlineImage) throws DocwbWebException;

	public List<AttachmentData> getAttachmentList(long docId, String attachmentSaveFolder, EnumFileType enumFileType)
			throws DocwbWebException;
}
