/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.extractor.service.docwbextractor;

import java.util.ArrayList;
import java.util.List;

import javax.json.JsonArray;
import javax.json.JsonObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.HttpClientBase;
import com.infosys.ainauto.commonutils.HttpClientBase.Authentication.BasicAuthenticationConfig;
import com.infosys.ainauto.docwb.web.exception.DocwbWebException;

@PropertySource("classpath:engineextractor.properties")
@Component
public class DocwbExtractorService extends HttpClientBase implements IDocwbExtractorService {

	// Data class only for this service
	public static class FilePathData {
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

	public static class FileContentResData {
		private int fileNumber;
		private String fileName;
		private String fileContent;

		public String getFileName() {
			return fileName;
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		public String getFileContent() {
			return fileContent;
		}

		public void setFileContent(String fileContent) {
			this.fileContent = fileContent;
		}

		public int getFileNumber() {
			return fileNumber;
		}

		public void setFileNumber(int fileNumber) {
			this.fileNumber = fileNumber;
		}
	}

	private static final String PROP_NAME_DOCWB_EXTRACTOR_USERNAME = "docwb.extractor.username";
	private static final String PROP_NAME_DOCWB_EXTRACTOR_DROWSSAP = "docwb.extractor.drowssap";
	private static final String CONTENT_ID_FILE = "file";
	private static final String APPLICATION_TYPE_PDF = "application/pdf";
	private static final String RESPONSE = "response";
	private static final String FILE_NUMBER = "fileNumber";
	private static final String FILE_NAME = "fileName";
	private static final String FILE_CONTENT = "fileContent";

	@Value("${docwb.extractor.file.content.api.url}")
	private String extractFileContentApi;

	@Autowired
	protected DocwbExtractorService(Environment environment) {
		super(null, new BasicAuthenticationConfig(environment.getProperty(PROP_NAME_DOCWB_EXTRACTOR_USERNAME),
				environment.getProperty(PROP_NAME_DOCWB_EXTRACTOR_DROWSSAP), true));
	}

	@Override
	public List<FileContentResData> getFileContent(List<FilePathData> filePathDataList) throws DocwbWebException {

		List<FileContentResData> fileContentResDataList = new ArrayList<FileContentResData>();

		List<HttpFileRequestData> httpFileDataList = new ArrayList<>();
		for (FilePathData filePathData : filePathDataList) {
			httpFileDataList.add(new HttpFileRequestData(filePathData.getFileName(), filePathData.getFilePath(),
					CONTENT_ID_FILE, APPLICATION_TYPE_PDF));
		}

		JsonObject jsonObj = executePostAttachmentWithAuthCall(extractFileContentApi, httpFileDataList).getResponse();

		if (jsonObj != null && !jsonObj.isNull(RESPONSE)) {
			JsonArray jsonResponseArray = jsonObj.getJsonArray(RESPONSE);
			for (int k = 0; k < jsonResponseArray.size(); k++) {
				JsonObject fileContentJsonObj = jsonResponseArray.getJsonObject(k);
				FileContentResData fileContentResData = new FileContentResData();
				fileContentResData.setFileNumber(fileContentJsonObj.getInt(FILE_NUMBER));
				fileContentResData.setFileName(fileContentJsonObj.getString(FILE_NAME));
				fileContentResData.setFileContent(fileContentJsonObj.getString(FILE_CONTENT));
				fileContentResDataList.add(fileContentResData);
			}
		}

		return fileContentResDataList;
	}
}
