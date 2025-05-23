/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.extractor.service.file;

import java.util.List;

import com.infosys.ainauto.docwb.web.data.AttachmentData;
import com.infosys.ainauto.docwb.web.data.EmailData;
import com.infosys.ainauto.docwb.web.data.InputData;

public interface IFileReaderService {

	public List<InputData> getNewFiles();

	public void updateFileAsRead(String dataInputRecordId);

	public void connectToFileAdapter();

	public void disconnectFromFileAdapter();

	public List<AttachmentData> convertEmailToHtml(EmailData emailData, String fileName, String groupName)
			throws Exception;

}
