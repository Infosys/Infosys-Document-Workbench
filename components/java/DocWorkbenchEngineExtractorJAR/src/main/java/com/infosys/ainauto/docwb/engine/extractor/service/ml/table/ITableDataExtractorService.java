/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.extractor.service.ml.table;

import java.util.List;

import com.infosys.ainauto.docwb.engine.extractor.service.ml.table.TableDataExtractorService.AttachmentData;

public interface ITableDataExtractorService {
	public List<AttachmentData> getTabularData(List<AttachmentData> attachmentDataList);
}
