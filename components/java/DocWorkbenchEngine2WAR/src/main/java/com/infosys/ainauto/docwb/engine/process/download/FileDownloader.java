
/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.process.download;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.FileUtility;
import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.docwb.engine.common.EngineConstants;
import com.infosys.ainauto.docwb.engine.core.stereotype.DocumentDownloader;
import com.infosys.ainauto.docwb.engine.core.template.download.DocumentDownloaderBase;
import com.infosys.ainauto.docwb.engine.extractor.service.file.IFileReaderService;
import com.infosys.ainauto.docwb.engine.extractor.service.file.formatconverter.IFormatConverterService;
import com.infosys.ainauto.docwb.web.data.AttachmentData;
import com.infosys.ainauto.docwb.web.data.DocumentData;
import com.infosys.ainauto.docwb.web.data.FileData;
import com.infosys.ainauto.docwb.web.data.InputData;
import com.infosys.ainauto.docwb.web.type.EnumDocType;
import com.infosys.ainauto.docwb.web.type.EnumExtractType;
import com.infosys.ainauto.docwb.web.type.EnumFileUnsupported;
import com.infosys.ainauto.docwb.web.type.EnumLockStatus;
import com.infosys.ainauto.docwb.web.type.EnumTaskStatus;

@Component
@DocumentDownloader(title = "Download Files", propertiesFile = "customization.properties")
public class FileDownloader extends DocumentDownloaderBase<InputData> {

	private final static Logger logger = LoggerFactory.getLogger(FileDownloader.class);

	@Autowired
	private Environment environment;

	@Autowired
	private IFileReaderService fileReaderService;

	@Autowired
	private IFormatConverterService formatConverterService;

	private List<String> wordExtensionList;
	private List<String> emlExtensionList;

	@Override
	protected boolean initialize(Properties properties) throws Exception {
		this.wordExtensionList = Arrays.asList(environment.getProperty("file.word.extension.to.pdf").split(","));
		this.emlExtensionList = Arrays.asList(environment.getProperty("file.eml.extension.to.html").split(","));
		return true;
	}

	@Override
	protected List<InputData> downloadData() throws Exception {
		List<InputData> fileDatalist = new ArrayList<InputData>();
		fileDatalist = fileReaderService.getNewFiles();
		return fileDatalist;
	}

	@Override
	protected DocumentData createDocument(InputData inputData) throws Exception {
		DocumentData documentData = new DocumentData();
		FileData fileData = inputData.getFileData();
		documentData.setDocType(EnumDocType.FILE).setDocLocation(fileData.getFileId())
				.setLockStatus(EnumLockStatus.UNLOCKED).setTaskStatus(EnumTaskStatus.UNDEFINED);
		List<AttachmentData> attachmentDataList = new ArrayList<>();
		try {
			List<String> unsupportedFormatList = new ArrayList<>();
			Arrays.asList(EnumFileUnsupported.values())
					.forEach(fileType -> unsupportedFormatList.add(fileType.getValue().toLowerCase()));

			if (fileData.isHasChildren()) {
				for (InputData childInputData : inputData.getInputDataList()) {
					convertInputDataToAttachmentData(unsupportedFormatList, attachmentDataList, childInputData);
				}
			} else {
				convertInputDataToAttachmentData(unsupportedFormatList, attachmentDataList, inputData);
			}
		} catch (Exception e) {
			logger.error("Error Occured in conversion service - " + e.getMessage());
		}
		documentData.setAttachmentDataList(attachmentDataList);
		return documentData;
	}

	@Override
	protected void uploadDocumentResult(Exception ex, InputData inputData, DocumentData documentData) throws Exception {
		if (ex != null) {
			logger.error("Error occurred in upload document", ex);
		} else {
			logger.info("Document uploaded with docId=" + documentData.getDocId());
			fileReaderService.updateFileAsRead(inputData.getFileData().getFileId());
		}
	}

	private void convertInputDataToAttachmentData(List<String> unsupportedFormatList,
			List<AttachmentData> attachmentDataList, InputData inputData) {
		try {
			FileData fileData = inputData.getFileData();
			AttachmentData attachmentData = new AttachmentData();
			attachmentData.setPhysicalPath(fileData.getFileAbsolutePath());
			attachmentData.setLogicalName(fileData.getFileName());
			attachmentData.setExtractTypeCde(EnumExtractType.DIRECT_COPY.getValue());

			String fileExtension = FileUtility.getFileExtension(fileData.getFileName()).toLowerCase();
			// Overall unsupportedFormatList configured in web api.
			if (unsupportedFormatList.contains(fileExtension)) {

				String groupName = "Temp - " + StringUtility.getUniqueString();
				attachmentData.setGroupName(groupName);

				// Here possible word & eml extenstion are configured in file downloader
				// property
				// 1. calling format converter python service because it deals with only word to
				// pdf conversion.
				// 2. calling fileReaderService for convert eml to html.
				if (wordExtensionList.contains(fileExtension)) {
					AttachmentData convertedAttachmentData = formatConverterService.convertWordToPdf(fileData,
							EngineConstants.API_SOURCE_FORMAT_WORD, EngineConstants.API_TARGET_FORMAT_PDF);
					convertedAttachmentData.setGroupName(groupName);
					attachmentDataList.add(convertedAttachmentData);
				} else if (emlExtensionList.contains(fileExtension)) {
					attachmentDataList.addAll(fileReaderService.convertEmailToHtml(inputData.getEmailData(),
							fileData.getFileName(), groupName));
				}
			}
			attachmentDataList.add(attachmentData);
		} catch (Exception e) {
			logger.error("Error Occured in conversion service - " + e.getMessage());
		}
	}
}
