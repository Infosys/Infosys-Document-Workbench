/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.extractor.service.file.formatconverter;

import com.infosys.ainauto.docwb.web.data.AttachmentData;
import com.infosys.ainauto.docwb.web.data.FileData;

public interface IFormatConverterService {

	public AttachmentData convertWordToPdf(FileData fileData, String sourceFormat, String targetFormat)
			throws Exception;
}
