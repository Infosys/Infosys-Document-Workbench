/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.extractor.service.file.formatconverter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.HttpClientBase;
import com.infosys.ainauto.docwb.engine.extractor.common.EngineExtractorConstants;
import com.infosys.ainauto.docwb.web.data.AttachmentData;
import com.infosys.ainauto.docwb.web.data.FileData;
import com.infosys.ainauto.docwb.web.type.EnumExtractType;

@Component
public class FormatConverterService extends HttpClientBase implements IFormatConverterService {

	@Autowired
	private Environment environment;

	private static final String FORMAT_CONVERTER_URL = "format.converter.api.url";
	private static final String ATTACHMENT_SAVE_FOLDER = "docwb.engine.temp.path";
	private static final String DOC_CLASSIFY_CID_NAME = "file";

	public AttachmentData convertWordToPdf(FileData fileData, String sourceFormat, String targetFormat)
			throws Exception {
		AttachmentData attachmentData = null;
		String apiUrl = environment.getProperty(FORMAT_CONVERTER_URL);
		String attachmentSaveFolder = environment.getProperty(ATTACHMENT_SAVE_FOLDER);
		apiUrl = apiUrl.replace(EngineExtractorConstants.API_PATH_PARAM_SOURCE_FORMAT, sourceFormat);
		apiUrl = apiUrl.replace(EngineExtractorConstants.API_PATH_PARAM_TARGET_FORMAT, targetFormat);
		List<HttpFileRequestData> httpFileDataList = new ArrayList<>();

		String fileName = fileData.getFileName();
		int index = fileName.indexOf(EngineExtractorConstants.FILE_SEPARATOR);
		String tempFileName = fileName;
		if (index != -1) {
			String[] parts = fileName.split(Pattern.quote(EngineExtractorConstants.FILE_SEPARATOR));
			fileName = parts[parts.length - 1];
			tempFileName = tempFileName.substring(0, tempFileName.indexOf(fileName));
		}

		httpFileDataList.add(new HttpFileRequestData(fileName, fileData.getFileAbsolutePath(), DOC_CLASSIFY_CID_NAME,
				EngineExtractorConstants.APPLICATION_TYPE_PDF));
		HttpClientBase.HttpFileResponseData httpFileResData = executePostAttachmentCall(apiUrl, httpFileDataList,
				attachmentSaveFolder);
		if (httpFileResData != null) {
			attachmentData = new AttachmentData();
			String responseFileName = httpFileResData.getFileName();
			if (index != -1) {
				responseFileName = tempFileName + responseFileName;
			}
			attachmentData.setLogicalName(responseFileName);
			attachmentData.setPhysicalName(httpFileResData.getFilePhysicalName());
			attachmentData.setPhysicalPath(httpFileResData.getFilePhysicalPath());
			attachmentData.setExtractTypeCde(EnumExtractType.CUSTOM_LOGIC.getValue());
		} else {
			throw new Exception("Word to PDF Format conversion failed");
		}
		return attachmentData;
	}
}
