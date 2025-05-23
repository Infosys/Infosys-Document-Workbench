/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.extractor.service.attribute;

import java.util.List;

import com.infosys.ainauto.docwb.engine.extractor.service.attribute.AttributeExtractorService.AttributeServiceApiV2ResData;
import com.infosys.ainauto.docwb.engine.extractor.service.attribute.AttributeExtractorService.AttributeServiceResponseData;
import com.infosys.ainauto.docwb.engine.extractor.service.attribute.AttributeExtractorService.ExtractorData.ExtractorConfigData;
import com.infosys.ainauto.docwb.web.data.AttachmentData;
import com.infosys.ainauto.docwb.web.data.DocumentData;

public interface IAttributeExtractorService {
	String extractAttributes(ExtractorConfigData extractorConfigData,
			String extractorApiInterfaceSchema, DocumentData documentData, List<AttachmentData> attachmentDataList, String ruleType)
			throws Exception;
	void handleAttributeServiceApiResponse(AttributeServiceApiV2ResData attributeServiceApiResData,
			AttributeServiceResponseData attributeServiceResponseData);
}
