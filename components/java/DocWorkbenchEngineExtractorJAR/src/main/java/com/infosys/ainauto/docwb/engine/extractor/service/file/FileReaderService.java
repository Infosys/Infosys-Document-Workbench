/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.extractor.service.file;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.ProviderNotFoundException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.infosys.ainauto.commonutils.FileUtility;
import com.infosys.ainauto.commonutils.ListUtility;
import com.infosys.ainauto.commonutils.PatternUtility;
import com.infosys.ainauto.commonutils.StringUtility;
import com.infosys.ainauto.datainout.DataInOutApi;
import com.infosys.ainauto.datainout.api.IDataInputReader;
import com.infosys.ainauto.datainout.config.AbstractDataInOutConfig.ProviderDataSourceConfig;
import com.infosys.ainauto.datainout.config.DataInputConfig;
import com.infosys.ainauto.datainout.model.DataInputRecord;
import com.infosys.ainauto.datainout.model.file.FileRecord;
import com.infosys.ainauto.datainout.spi.IDataInOutProvider;
import com.infosys.ainauto.datasource.config.file.FileSystemDataSourceReaderConfig;
import com.infosys.ainauto.docwb.engine.extractor.common.EmailDataHelper;
import com.infosys.ainauto.docwb.engine.extractor.common.EngineExtractorConstants;
import com.infosys.ainauto.docwb.web.data.AttachmentData;
import com.infosys.ainauto.docwb.web.data.EmailAddressData;
import com.infosys.ainauto.docwb.web.data.EmailData;
import com.infosys.ainauto.docwb.web.data.FileData;
import com.infosys.ainauto.docwb.web.data.InputData;
import com.infosys.ainauto.docwb.web.type.EnumExtractType;

@Component
public class FileReaderService implements IFileReaderService {
	private static Logger logger = LoggerFactory.getLogger(FileReaderService.class);

	private IDataInputReader dataInputReader;

	@Autowired
	Environment environment;

	private static final String PROP_NAME_DATA_INPUT_BASIC_PROVIDER = "dio.reader.basic.provider";
	private static final String PROP_NAME_DATA_SOURCE_FILE_SYSTEM_READER_CLIENT_CONNECT = "ds.file-system.reader.client.connect";
	private static final String PROP_NAME_DATA_SOURCE_FILE_SYSTEM_READER_PROVIDER = "ds.file-system.reader.provider";
	private static final String PROP_NAME_DATA_SOURCE_FILE_SYSTEM_READER_SOURCE_DIR = "ds.file-system.reader.dir.source";
	private static final String PROP_NAME_DATA_SOURCE_FILE_SYSTEM_READER_ARCHIVAL_DIR = "ds.file-system.reader.dir.archival";
	private static final String PROP_NAME_DATA_SOURCE_FILE_SYSTEM_READER_ONCONNERROR_IGNORE = "ds.file-system.reader.on-connection-error.ignore";
	private static final String PROP_NAME_DATA_SOURCE_FILE_SYSTEM_READER_SOURCE_PERMANENT_SUB_DIR = "ds.file-system.reader.dir.source.permanent.subfolders";
	private static final String PROP_NAME_TEMP_FOLDER = "dio.reader.temp.path";

	private static final int NO_OF_TRIES = 2;

	@PostConstruct
	private void init() {

		String dataInputBasicProvider = environment.getProperty(PROP_NAME_DATA_INPUT_BASIC_PROVIDER);

		if (StringUtility.hasTrimmedValue(dataInputBasicProvider)) {
			DataInputConfig dataInputConfig = new DataInputConfig();
			String tempFolderPath = environment.getProperty(PROP_NAME_TEMP_FOLDER);
			dataInputConfig.setAttachmentDownloadFolder(tempFolderPath);

			// Source 1 - File System
			boolean isFileReaderEnabled = Boolean
					.valueOf(environment.getProperty(PROP_NAME_DATA_SOURCE_FILE_SYSTEM_READER_CLIENT_CONNECT));
			if (isFileReaderEnabled) {
				FileSystemDataSourceReaderConfig fileSystemDataSourceReaderConfig = new FileSystemDataSourceReaderConfig();
				fileSystemDataSourceReaderConfig
						.setFileSourceDir(environment.getProperty(PROP_NAME_DATA_SOURCE_FILE_SYSTEM_READER_SOURCE_DIR));
				fileSystemDataSourceReaderConfig.setFileArchivalDir(
						environment.getProperty(PROP_NAME_DATA_SOURCE_FILE_SYSTEM_READER_ARCHIVAL_DIR));
				fileSystemDataSourceReaderConfig.setFileSourcePermanentSubDir(
						environment.getProperty(PROP_NAME_DATA_SOURCE_FILE_SYSTEM_READER_SOURCE_PERMANENT_SUB_DIR));

				String onConnectionErrorIgnore = environment
						.getProperty(PROP_NAME_DATA_SOURCE_FILE_SYSTEM_READER_ONCONNERROR_IGNORE, "false");
				fileSystemDataSourceReaderConfig.setOnConnectionErrorIgnore(Boolean.valueOf(onConnectionErrorIgnore));

				fileSystemDataSourceReaderConfig.setFileTempDir(tempFolderPath);
				ProviderDataSourceConfig providerDataSourceConfig = new ProviderDataSourceConfig(
						environment.getProperty(PROP_NAME_DATA_SOURCE_FILE_SYSTEM_READER_PROVIDER),
						fileSystemDataSourceReaderConfig);
				dataInputConfig.addToProviderDataSourceConfigList(providerDataSourceConfig);
			}

			try {
				IDataInOutProvider dataInOutProvider = DataInOutApi.getProviderByClassName(dataInputBasicProvider);

				dataInputReader = dataInOutProvider.getDataInputReader(dataInputConfig);

				// Make connection on initialization
				connectToFileAdapter();
			} catch (ProviderNotFoundException ex) {
				logger.warn("FileReaderService could not be instantiated. Reason:", ex);
			}
		} else {
			logger.warn(
					"WARNING!! FileReaderService could not be instantiated. Reason: missing properties in application.properties file");
		}
	}

	public List<InputData> getNewFiles() {
		List<DataInputRecord> dataInputRecordList = new ArrayList<>();
		// Make two attempts to connect to file adapter if not connected
		for (int i = 1; i <= NO_OF_TRIES; i++) {
			// Connect if not connected
			try {
				dataInputRecordList = dataInputReader.getNewItems();
				break; // Break from loop if call was successful
			} catch (Exception e) {
				logger.error("Error occured while getting new files", e);
				// Assume error is due to connection so set status as false
				connectToFileAdapter();
			}
		}

		InputData inputData;
		List<InputData> inputDataList = new ArrayList<InputData>();
		for (DataInputRecord dataInputRecord : dataInputRecordList) {
			inputData = new InputData();
			FileRecord fileRecord = dataInputRecord.getFileRecord();
			if (fileRecord != null) {
				FileData fileData = new FileData();
				// Copy all file data to return object
				BeanUtils.copyProperties(fileRecord, fileData);
				fileData.setFileCreationTime(fileRecord.getBasicFileAttributes().creationTime());
				inputData.setFileData(fileData);

				List<InputData> subInputDataList = null;
				List<DataInputRecord> subDataInputRecordList = dataInputRecord.getDataInputRecordList();
				if (ListUtility.hasValue(subDataInputRecordList)) {
					InputData subInputData;
					subInputDataList = new ArrayList<>();
					for (DataInputRecord subDataInputRecord : subDataInputRecordList) {
						subInputData = new InputData();
						FileRecord subFileRecord = subDataInputRecord.getFileRecord();
						if (subFileRecord != null) {
							FileData subFileData = new FileData();
							// Copy all file data to return object
							BeanUtils.copyProperties(subFileRecord, subFileData);
							subInputData.setFileData(subFileData);
						}

						EmailData emailData = EmailDataHelper.convertDataInputRecordToEmailData(subDataInputRecord);
						if (emailData != null) {
							subInputData.setEmailData(emailData);
						}

						subInputDataList.add(subInputData);
					}
				}
				inputData.setInputDataList(subInputDataList);
			}
			EmailData emailData = EmailDataHelper.convertDataInputRecordToEmailData(dataInputRecord);
			if (emailData != null) {
				inputData.setEmailData(emailData);
			}
			inputDataList.add(inputData);
		}
		return inputDataList;
	}

	public void updateFileAsRead(String dataInputRecordId) {
		try {
			dataInputReader.updateItemAsRead(dataInputRecordId);
		} catch (Exception e) {
			logger.error("error occured while marking file as read:- ", e);
		}
	}

	public void connectToFileAdapter() {
		try {
			logger.info("Calling method to connect to file adapter");
			dataInputReader.connect();
		} catch (Exception e) {
			logger.error("Failed to connect to file adapter", e);
		}

	}

	public void disconnectFromFileAdapter() {
		try {
			logger.info("Calling method to disconnect from file adapter");
			dataInputReader.disconnect();
		} catch (Exception e) {
			logger.error("Failed To disconnect from file adapter", e);
		}

	}

	@Override
	public List<AttachmentData> convertEmailToHtml(EmailData emailData, String fileName, String groupName)
			throws Exception {
		String attachmentTempFolder = environment.getProperty(PROP_NAME_TEMP_FOLDER);
		String convertedFileName = fileName.substring(0, fileName.lastIndexOf("."))
				+ EngineExtractorConstants.FILE_EXTENSION_HTML;
		String filePhysicalName = FileUtility.generateUniqueFileName(convertedFileName);
		String targetFilePath = FileUtility.getConcatenatedName(attachmentTempFolder, filePhysicalName);
		long startTime = System.nanoTime();
		List<AttachmentData> attachmentDataList = new ArrayList<>();
		generateHtml(emailData, targetFilePath);
		double timeElapsed = (System.nanoTime() - startTime) / 1000000000.0;
		logger.info("Time taken for converting {} to {} is {} secs", StringUtility.sanitizeReqData(targetFilePath),
				timeElapsed);
		AttachmentData attachmentData = new AttachmentData();
		attachmentData.setPhysicalPath(targetFilePath);
		attachmentData.setLogicalName(convertedFileName);
		attachmentData.setExtractTypeCde(EnumExtractType.CUSTOM_LOGIC.getValue());
		attachmentData.setGroupName(groupName);
		attachmentDataList.add(attachmentData);
		List<AttachmentData> externalAttachmentDataList = emailData.getAttachmentDataList();
		if (ListUtility.hasValue(externalAttachmentDataList)) {
			attachmentDataList.addAll(externalAttachmentDataList);
		}
		return attachmentDataList;
	}

	private void generateHtml(EmailData emailData, String outputFilePath) throws Exception {
		try {
			String ex_emailSubj = emailData.getEmailSubject();
			String ex_emailSender = emailData.getEmailAddressFrom().getEmailId();
			String ex_emailReceiver = "";
			for (EmailAddressData emailAddressData : emailData.getEmailAddressToList()) {
				ex_emailReceiver += emailAddressData.getEmailId() + ",";
			}
			if (StringUtility.hasTrimmedValue(ex_emailReceiver)) {
				ex_emailReceiver = ex_emailReceiver.substring(0, ex_emailReceiver.length() - 1);
			}
			String ex_ReceivedDate = emailData.getEmailDate().toString();
			String headerStr = "<div class=WordSection1> <p class=MsoNormal style=\'margin-left:120.0pt;text-indent:-120.0pt;tab-stops:120.0pt;mso-layout-grid align:none;text-autospace:none\'><b><span style=\'font-family:\"Calibri\",sans-serif;color:black\'>From:<span style=\'mso-tab-count:1\'>                              </span></span></b><span style=\'font-family:\"Calibri\",sans-serif;color:black\'>"
					+ ex_emailSender
					+ "<o:p></o:p></span></p> <p class=MsoNormal style=\'margin-left:120.0pt;text-indent:-120.0pt;tab-stops: 120.0pt;mso-layout-grid-align:none;text-autospace:none\'><b><span style=\'font-family:\"Calibri\",sans-serif;color:black\'>Sent:<span style=\'mso-tab-count:1\'>                               </span></span></b><span style=\'font-family:\"Calibri\",sans-serif;color:black\'>"
					+ ex_ReceivedDate
					+ "<o:p></o:p></span></p>  <p class=MsoNormal style=\'margin-left:120.0pt;text-indent:-120.0pt;tab-stops: 120.0pt;mso-layout-grid-align:none;text-autospace:none\'><b><span style=\'font-family:\"Calibri\",sans-serif;color:black\'>To:<span style=\'mso-tab-count: 1\'>                                   </span></span></b><span style=\'font-family: \"Calibri\",sans-serif;color:black\'>"
					+ ex_emailReceiver
					+ "<o:p></o:p></span></p>  <p class=MsoNormal style=\'margin-left:120.0pt;text-indent:-120.0pt;tab-stops: 120.0pt;mso-layout-grid-align:none;text-autospace:none\'><b><span style=\'font-family:\"Calibri\",sans-serif;color:black\'>Subject:<span style=\'mso-tab-count:1\'>                          </span></span></b><span style=\'font-family:\"Calibri\",sans-serif;color:black\'>"
					+ ex_emailSubj + "<o:p></o:p></span></p><div>";
			String bodyText = emailData.getEmailBodyText();
			String bodyHtml = emailData.getEmailBodyHtml();
			if (StringUtility.hasValue(bodyHtml)) {
				bodyHtml = base64Conversion(emailData, bodyHtml);
				createHtmlFile(bodyHtml, headerStr, outputFilePath);
			} else if (StringUtility.hasValue(bodyText)) {
				createHtmlFile(bodyText, headerStr, outputFilePath);
			} else {
				throw new Exception("HTML file creation failed due to empty body content");
			}
		} catch (Exception e) {
			throw new Exception("Error occurred while converting Eml to html", e);
		}

	}

	private String base64Conversion(EmailData emailData, String body) {
		List<String> fileList = PatternUtility.getHtmlImgSrcValues(body);
		for (String fileName : fileList) {
			String[] strings = fileName.split("\\.");
			String extension = strings.length > 2 ? strings[1].split("@")[0] : "";
			String fileExtension = StringUtility.getBase64Extension1(extension);
			String base64String = "";
			try {
				// Reading a Image file from file system and convert it into Base64 String
				for (AttachmentData attachmentData : emailData.getInlineImageAttachmentDataList()) {
					if (fileName.equalsIgnoreCase(attachmentData.getLogicalName())) {
						byte[] imageData = FileUtility.readFile(attachmentData.getPhysicalPath());
						base64String = fileExtension + "," + Base64.getEncoder().encodeToString(imageData);
					}
				}
				body = body.replace(fileName, base64String);
			} catch (Exception e) {
				logger.error("Exception while reading the Image " + e);
			}
		}
		return body;
	}

	private void createHtmlFile(String body, String headers, String filePathToWrite) throws Exception {
		String fileContent = headers + "\n" + body;
		FileWriter fileWriter = new FileWriter(FileUtility.cleanPath(filePathToWrite));
		PrintWriter printWriter = new PrintWriter(fileWriter);
		printWriter.print(fileContent);
		printWriter.close();
	}

}
