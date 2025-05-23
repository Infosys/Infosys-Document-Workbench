/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.service.ner;

import java.util.List;

import com.infosys.ainauto.docwb.engine.service.ner.ExtractService.FilePathData;
import com.infosys.ainauto.docwb.engine.service.ner.ExtractService.NERResData;
import com.infosys.ainauto.docwb.web.data.AttributeData;
import com.infosys.ainauto.docwb.web.exception.DocwbWebException;

public interface IExtractService {
	public AttributeData getExtract(String text, List<String> entityList);

	public List<NERResData> extractAttributes(List<FilePathData> filePathDataList, String paramFileName)
			throws DocwbWebException;
}
