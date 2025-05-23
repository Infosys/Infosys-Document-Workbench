/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.extractor.service.azure.cognitive.vision;

import javax.json.JsonArray;

import com.infosys.ainauto.docwb.engine.extractor.service.attribute.AttributeExtractorService.ExtractorData.ExtractorConfigData;
import com.infosys.ainauto.docwb.web.data.AttachmentData;

public interface IFormRecognizerService {
	
	public String postAnalyzeForm(ExtractorConfigData extractorConfigData, AttachmentData attachmentData)
			throws Exception;

	public String postAnalyzeLayout() throws Exception;

	public String postAnalyzeReceipt() throws Exception;

	public String postCopyCustomModel() throws Exception;

	public String postGenerateCopyAuthorization() throws Exception;

	public String postTrainCustomModel() throws Exception;

	public JsonArray getAnalyzeFormResult(ExtractorConfigData extractorConfigData, String operationLocationUrl)
			throws Exception;

	public String getAnalyzeLayoutResult() throws Exception;

	public String getAnalyzeReceiptResult() throws Exception;

	public String getCopyModelResult() throws Exception;

	public String getCustomModel() throws Exception;

	public String getCustomModelList() throws Exception;

}
