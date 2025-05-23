/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.process.attribute;

import java.util.List;

import com.infosys.ainauto.docwb.service.common.WorkbenchException;
import com.infosys.ainauto.docwb.service.model.api.annotation.ExportIOBReqData;
import com.infosys.ainauto.docwb.service.model.api.annotation.InsertAnnotationReqData;
import com.infosys.ainauto.docwb.service.model.db.AttachmentDbData;
import com.infosys.ainauto.docwb.service.model.db.EntityDbData;

public interface IAnnotationProcess {

	public List<EntityDbData> addAnnotation(List<InsertAnnotationReqData> insertAnnotationReqDatas)
			throws WorkbenchException;

	public AttachmentDbData getAnnotationIOB(ExportIOBReqData exportIOBReqData) throws WorkbenchException;
}
