/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.web.api;

import java.util.List;
import java.util.Map;

import com.infosys.ainauto.docwb.web.data.AttachmentData;
import com.infosys.ainauto.docwb.web.data.AttributeData;
import com.infosys.ainauto.docwb.web.data.DocumentData;

public interface IAttributeService {

	List<AttributeData> getDocAttributeList(Long docId);

	public Map<Integer, String> getAttributeNames();

	public List<AttachmentData> getAttachmentAttributeList(Long docId);

	public void addAttributes(DocumentData documentData);
	
	public String getAttributesToExport(Long docId);
}
