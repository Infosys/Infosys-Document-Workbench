/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.service.sample;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.HttpClientBase;

@Component
public class FileAttributeExtractorService extends HttpClientBase implements IFileAttributeExtractorService {

	// Data class only for this service
	public class FilePathData {
		String fileNumber;
		String fileName;
		String filePath;

		public String getFileName() {
			return fileName;
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		public String getFilePath() {
			return filePath;
		}

		public void setFilePath(String filePath) {
			this.filePath = filePath;
		}
	}

	public class AttributeData {
		private String attrName;
		private String attrValue;
		private float confidencePct;

		public AttributeData(String attrName, String attrValue, float confidencePct) {
			this.attrName = attrName;
			this.attrValue = attrValue;
			this.confidencePct = confidencePct;
		}

		public String getAttrName() {
			return attrName;
		}

		public void setAttrName(String attrName) {
			this.attrName = attrName;
		}

		public String getAttrValue() {
			return attrValue;
		}

		public void setAttrValue(String attrValue) {
			this.attrValue = attrValue;
		}

		public float getConfidencePct() {
			return confidencePct;
		}

		public void setConfidencePct(float confidencePct) {
			this.confidencePct = confidencePct;
		}
	}

	private static Logger logger = LoggerFactory.getLogger(FileAttributeExtractorService.class);

	@Override
	public List<AttributeData> extractAttributes(FilePathData filePathData) {

		logger.debug("Entering");

		List<AttributeData> attributeDataList = new ArrayList<>();
		AttributeData attributeData = new AttributeData("Surname", "Obama", 80);
		attributeDataList.add(attributeData);

		attributeData = new AttributeData("Name", "Michelle", 80);
		attributeDataList.add(attributeData);

		attributeData = new AttributeData("Passport Number", "H833802", 80);
		attributeDataList.add(attributeData);

		attributeData = new AttributeData("DOB", "17/1/1964", 80);
		attributeDataList.add(attributeData);

		attributeData = new AttributeData("Expiry Date", "8/24/2020", 80);
		attributeDataList.add(attributeData);

		attributeData = new AttributeData("Address", "6430 Village Park Dr, Apr #204", 80);
		attributeDataList.add(attributeData);

		attributeData = new AttributeData("DocumentType", "Passport", 80);
		attributeDataList.add(attributeData);

		return attributeDataList;

	}

}
