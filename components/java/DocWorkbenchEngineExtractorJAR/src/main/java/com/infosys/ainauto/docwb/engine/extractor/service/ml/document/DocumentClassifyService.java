/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.extractor.service.ml.document;

import java.util.ArrayList;
import java.util.List;

import javax.json.JsonObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.HttpClientBase;
import com.infosys.ainauto.commonutils.HttpClientBase.Authentication.BasicAuthenticationConfig;
import com.infosys.ainauto.docwb.web.data.AttributeData;
import com.infosys.ainauto.docwb.web.data.FileData;

@Component
public class DocumentClassifyService extends HttpClientBase implements IDocumentClassifyService {
	@Autowired
	private Environment environment;

	private static final String DOCUMENT_CLASSIFICATION_URL = "service.document.classify.api.url";
	// TODO user clean up application.properties and create standard for adding new
	// service class url and creds
	private static final String DOCUMENT_CLASSIFICATION_USERNAME = "service.document.classify.api.username";
	private static final String DOCUMENT_CLASSIFICATION_DROWSSAP = "service.document.classify.api.drowssap";
	private static final String DOC_CLASSIFY_CID_NAME = "file";
	private static final String APPLICATION_TYPE_PDF = "application/pdf";

	@Autowired
	protected DocumentClassifyService(Environment environment) {
		super(null, new BasicAuthenticationConfig(environment.getProperty(DOCUMENT_CLASSIFICATION_USERNAME),
				environment.getProperty(DOCUMENT_CLASSIFICATION_DROWSSAP), true));
	}

	public AttributeData getDocumentType(FileData fileData) {
		String apiUrl = environment.getProperty(DOCUMENT_CLASSIFICATION_URL);

		List<HttpFileRequestData> httpFileDataList = new ArrayList<>();
		httpFileDataList.add(new HttpFileRequestData(fileData.getFileName(), fileData.getFileAbsolutePath(),
				DOC_CLASSIFY_CID_NAME, APPLICATION_TYPE_PDF));

		JsonObject jsonResponse = executePostAttachmentWithAuthCall(apiUrl, httpFileDataList).getResponse();
		AttributeData attributeData = null;
		if (jsonResponse != null && jsonResponse.getInt("responseCde") == 0) {
			JsonObject jsonObject = jsonResponse.getJsonObject("response");
			attributeData = new AttributeData();
			attributeData.setAttrValue(jsonObject.getString("documentType"))
					.setConfidencePct(jsonObject.getInt("confidencePct"));
		}
		return attributeData;
	}
}
