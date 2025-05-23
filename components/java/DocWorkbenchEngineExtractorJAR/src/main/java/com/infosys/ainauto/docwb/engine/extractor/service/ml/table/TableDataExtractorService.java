/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.extractor.service.ml.table;

import java.util.ArrayList;
import java.util.List;

import javax.json.JsonArray;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.HttpClientBase;
import com.infosys.ainauto.commonutils.HttpClientBase.Authentication.BasicAuthenticationConfig;
import com.infosys.ainauto.docwb.engine.extractor.common.AttributeDataHelper;
import com.infosys.ainauto.docwb.engine.extractor.common.EngineExtractorConstants;
import com.infosys.ainauto.docwb.web.data.AttributeData;
import com.infosys.ainauto.docwb.web.type.EnumExtractType;
import com.infosys.ainauto.docwb.web.type.EnumSystemAttributeName;

@Component
public class TableDataExtractorService extends HttpClientBase implements ITableDataExtractorService {

	public static class AttachmentData {
		private long attachmentId;
		private String physicalName;
		private String physicalPath;
		private List<AttributeData> attributes;

		public long getAttachmentId() {
			return attachmentId;
		}

		public void setAttachmentId(long attachmentId) {
			this.attachmentId = attachmentId;
		}

		public String getPhysicalName() {
			return physicalName;
		}

		public void setPhysicalName(String physicalName) {
			this.physicalName = physicalName;
		}

		public String getPhysicalPath() {
			return physicalPath;
		}

		public void setPhysicalPath(String physicalPath) {
			this.physicalPath = physicalPath;
		}

		public List<AttributeData> getAttributes() {
			return attributes;
		}

		public void setAttributes(List<AttributeData> attributes) {
			this.attributes = attributes;
		}
	}

	@Autowired
	private Environment environment;

	private static final String SERVICE_TABLE_DATA_EXTRACTOR_API_URL = "service.tabledataextractor.api.url";
	private static final String SERVICE_TABLE_DATA_EXTRACTOR_API_USERNAME = "service.tabledataextractor.api.username";
	private static final String SERVICE_TABLE_DATA_EXTRACTOR_API_DROWSSAP = "service.tabledataextractor.api.drowssap";
	private static final String ATTACHMENT_CID_NAME = "pdf-file";

	private static Logger logger = LoggerFactory.getLogger(TableDataExtractorService.class);

	@Autowired
	protected TableDataExtractorService(Environment environment) {
		super(null, new BasicAuthenticationConfig(environment.getProperty(SERVICE_TABLE_DATA_EXTRACTOR_API_USERNAME),
				environment.getProperty(SERVICE_TABLE_DATA_EXTRACTOR_API_DROWSSAP), true));
	}

	public List<AttachmentData> getTabularData(List<AttachmentData> attachmentDataList) {
		logger.debug("Entering");
		String pdfApiUrl = environment.getProperty(SERVICE_TABLE_DATA_EXTRACTOR_API_URL);
		String fileName = "";
		List<AttachmentData> responseDataList = new ArrayList<>();
		for (AttachmentData attachmentData : attachmentDataList) {
			List<AttributeData> attributes = new ArrayList<>();
			AttachmentData responseData = new AttachmentData();

			List<HttpFileRequestData> httpFileDataList = new ArrayList<>();
			httpFileDataList
					.add(new HttpFileRequestData(attachmentData.getPhysicalName(), attachmentData.getPhysicalPath(),
							ATTACHMENT_CID_NAME, EngineExtractorConstants.APPLICATION_TYPE_PDF));

			JsonObject jsonResponse = executePostAttachmentWithAuthCall(pdfApiUrl, httpFileDataList).getResponse();
			if (jsonResponse != null) {

				JsonArray jsonArray = jsonResponse.getJsonArray("extracted_fields");
				jsonArray.forEach(jsonObject -> {
					List<AttributeData> innerAttributes = new ArrayList<>();
					innerAttributes
							.add(getAttribute("Invoice Number", ((JsonObject) jsonObject).getString("invoice-num")));
					innerAttributes
							.add(getAttribute("Invoice Date", ((JsonObject) jsonObject).getString("invoice-date")));
					innerAttributes
							.add(getAttribute("Invoice Amount", ((JsonObject) jsonObject).getString("invoice-amount")));

					attributes.add(AttributeDataHelper.createMultipleAttibuteElement(
							EnumSystemAttributeName.MULTI_ATTRIBUTE_TABLE.getCde(),
							EngineExtractorConstants.GROUP_NAME_ROW, innerAttributes));
				});
			}
			responseData.setPhysicalName(fileName);
			responseData.setAttachmentId(attachmentData.getAttachmentId());
			responseData.setAttributes(attributes);
			responseDataList.add(responseData);
		}

		return responseDataList;
	}

	private AttributeData getAttribute(String attrName, String attrvalue) {
		AttributeData attributeData = new AttributeData();
		attributeData.setAttrNameTxt(attrName);
		attributeData.setAttrValue(attrvalue.replaceAll("[^A-Za-z0-9 ]", "").trim());
		attributeData.setConfidencePct(EngineExtractorConstants.CONFIDENCE_PCT_UNDEFINED);
		attributeData.setExtractType(EnumExtractType.CUSTOM_LOGIC);

		return attributeData;
	}
}
