/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.service.sample;

import java.util.List;

import com.infosys.ainauto.docwb.engine.service.sample.FileAttributeExtractorService.FilePathData;

public interface IFileAttributeExtractorService {
	public List<FileAttributeExtractorService.AttributeData> extractAttributes(FilePathData filePathData);
}
