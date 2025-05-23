/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.extractor.service.docwbextractor;

import java.util.List;

import com.infosys.ainauto.docwb.engine.extractor.service.docwbextractor.DocwbExtractorService.FileContentResData;
import com.infosys.ainauto.docwb.engine.extractor.service.docwbextractor.DocwbExtractorService.FilePathData;
import com.infosys.ainauto.docwb.web.exception.DocwbWebException;

public interface IDocwbExtractorService {

	public List<FileContentResData> getFileContent(List<FilePathData> filePathDataList) throws DocwbWebException;
}
