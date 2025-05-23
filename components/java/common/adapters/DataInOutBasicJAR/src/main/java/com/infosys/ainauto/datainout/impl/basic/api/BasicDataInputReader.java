/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.datainout.impl.basic.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.mail.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.infosys.ainauto.commonutils.FileUtility;
import com.infosys.ainauto.datainout.api.AbstractDataInputReader;
import com.infosys.ainauto.datainout.api.IDataInputReader;
import com.infosys.ainauto.datainout.common.DataInputException;
import com.infosys.ainauto.datainout.config.DataInputConfig;
import com.infosys.ainauto.datainout.impl.basic.common.DataInOutConstants;
import com.infosys.ainauto.datainout.impl.basic.common.EmailHelper;
import com.infosys.ainauto.datainout.model.DataInputRecord;
import com.infosys.ainauto.datainout.model.email.AttachmentRecord;
import com.infosys.ainauto.datainout.model.email.EmailRecord;
import com.infosys.ainauto.datainout.model.file.FileRecord;
import com.infosys.ainauto.datasource.api.IDataSourceReader;
import com.infosys.ainauto.datasource.common.DataSourceException;
import com.infosys.ainauto.datasource.config.DataSourceConfig;
import com.infosys.ainauto.datasource.model.DataSourceRecord;
import com.infosys.ainauto.datasource.model.email.EmailServerDataSourceRecord;
import com.infosys.ainauto.datasource.model.file.FileSystemDataSourceRecord;

import microsoft.exchange.webservices.data.core.service.item.EmailMessage;

public class BasicDataInputReader extends AbstractDataInputReader implements IDataInputReader {

	private final static Logger logger = LoggerFactory.getLogger(BasicDataInputReader.class);
	private DataInputConfig dataInputConfig;
	private Map<String, List<AttachmentRecord>> uidSavedAttachmentsListMap = new HashMap<>();
	private Map<String, List<AttachmentRecord>> uidSavedInlineAttachmentsListMap = new HashMap<>();

	public BasicDataInputReader(DataInputConfig dataInputConfig) {
		super(dataInputConfig);
		this.dataInputConfig = (DataInputConfig) dataInputConfig;
		logger.info("New instance created");
	}

	@Override
	protected DataInputRecord parseDataSource(DataSourceRecord dataSourceRecord, DataSourceConfig dataSourceConfig,
			IDataSourceReader dataSourceReader) throws DataInputException {

		DataInputRecord dataInputRecord = new DataInputRecord();
		if (dataSourceRecord instanceof EmailServerDataSourceRecord) {
			EmailServerDataSourceRecord emailServerDataSourceRecord = (EmailServerDataSourceRecord) dataSourceRecord;

			EmailRecord emailRecord = null;
			if (emailServerDataSourceRecord.getMessage() instanceof Message) {
				emailRecord = convertEmailMessageToRecord((Message) emailServerDataSourceRecord.getMessage(),
						emailServerDataSourceRecord.getDataSourceRecordId(), "Email");
			} else if (emailServerDataSourceRecord.getMessage() instanceof EmailMessage) {
				emailRecord = convertExchangeMessageToRecord((EmailMessage) emailServerDataSourceRecord.getMessage(),
						emailServerDataSourceRecord.getDataSourceRecordId(), "Email");
			}

			// Fix for issue - email is automatically marked as read/seen after reading it
			// This is new behavior after CCD changed mail server
			// So, to make it work as before, mark email as unread using below conditional
			// logic

			try {
				dataSourceReader.updateItemAsUnread(dataSourceRecord.getDataSourceRecordId());
			} catch (DataSourceException e) {
				logger.error("Unable to mark item as unread");
			}

			dataInputRecord.setEmailRecord(emailRecord);
		} else if (dataSourceRecord instanceof FileSystemDataSourceRecord) {
			FileSystemDataSourceRecord fileSystemDataSourceRecord = (FileSystemDataSourceRecord) dataSourceRecord;

			String fileName = fileSystemDataSourceRecord.getFileName();
			String fileAbsolutePath = fileSystemDataSourceRecord.getFileAbsolutePath();
			if (fileName.toLowerCase(Locale.ENGLISH).endsWith(".eml")) {
				dataInputRecord.setEmailRecord(convertEmailMessageToRecord(EmailHelper.readEmlFile(fileAbsolutePath),
						dataSourceRecord.getDataSourceRecordId(), fileName));
			}

			FileRecord fileRecord = new FileRecord();
			fileRecord.setBasicFileAttributes(fileSystemDataSourceRecord.getBasicFileAttributes());
			fileRecord.setFileAbsolutePath(fileAbsolutePath);
			fileRecord.setFileId(fileSystemDataSourceRecord.getDataSourceRecordId());
			fileRecord.setFileName(fileName);
			fileRecord.setFileSubPath(fileSystemDataSourceRecord.getFileSubPath());
			fileRecord.setHasChildren(fileSystemDataSourceRecord.isHasChildren());

			if (fileSystemDataSourceRecord.isHasChildren()) {
				List<DataInputRecord> dataInputRecordList = null;
				List<AttachmentRecord> savedAttachmentRecordList = null;
				for (FileSystemDataSourceRecord subFileSystemDataSourceRecord : fileSystemDataSourceRecord
						.getFileSystemDataSourceRecordList()) {
					if (dataInputRecordList == null) {
						dataInputRecordList = new ArrayList<>();
						savedAttachmentRecordList = new ArrayList<>();
					}
					FileRecord subFileRecord = new FileRecord();
					String absolutePath = subFileSystemDataSourceRecord.getFileAbsolutePath();
					String subFileName = subFileSystemDataSourceRecord.getFileName();
					subFileRecord.setBasicFileAttributes(fileSystemDataSourceRecord.getBasicFileAttributes());
					subFileRecord.setFileAbsolutePath(absolutePath);
					subFileRecord.setFileName(subFileName);
					DataInputRecord subDataInputRecord = new DataInputRecord();

					if (subFileName.toLowerCase(Locale.ENGLISH).endsWith(".eml")) {
						subDataInputRecord
								.setEmailRecord(convertEmailMessageToRecord(EmailHelper.readEmlFile(absolutePath),
										subFileSystemDataSourceRecord.getDataSourceRecordId(), subFileName));
					}

					subDataInputRecord.setFileRecord(subFileRecord);
					dataInputRecordList.add(subDataInputRecord);
					if (subFileSystemDataSourceRecord.isTempFile() && savedAttachmentRecordList != null) {
						AttachmentRecord attachmentRecord = new AttachmentRecord();
						attachmentRecord.setActualFileName(subFileName);
						attachmentRecord.setStoredFileFullPath(absolutePath);
						savedAttachmentRecordList.add(attachmentRecord);
					}
				}
				dataInputRecord.setDataInputRecordList(dataInputRecordList);
				if (savedAttachmentRecordList != null) {
					uidSavedAttachmentsListMap.put(dataInputRecord.getDataInputRecordId(), savedAttachmentRecordList);
				}
			}

			dataInputRecord.setFileRecord(fileRecord);
		}
		return dataInputRecord;
	}

	private EmailRecord convertEmailMessageToRecord(Message message, String dataInputRecordId, String parent)
			throws DataInputException {
		EmailRecord emailRecord = null;
		try {
			boolean isAllowedByFilterCondition = EmailHelper.isAllowedByFilterCondition(message, dataInputConfig);
			if (isAllowedByFilterCondition) {
				emailRecord = new EmailRecord();
				EmailHelper.updateEmailAttributes(message, emailRecord);
				EmailHelper.updateBodyAndAttachments(message, emailRecord,
						dataInputConfig.getAttachmentDownloadFolder());
				List<AttachmentRecord> externalAttachmentRecordList = emailRecord.getAttachmentRecordList();
				if (externalAttachmentRecordList != null && externalAttachmentRecordList.size() > 0) {
					uidSavedAttachmentsListMap.put(dataInputRecordId, externalAttachmentRecordList);
					externalAttachmentRecordList.stream()
							.forEach(attachmentRecord -> attachmentRecord.setActualFileName(
									parent + DataInOutConstants.FILE_SEPARATOR + attachmentRecord.getActualFileName()));
				}
				if (emailRecord.getInlineAttachmentRecordList() != null
						&& emailRecord.getInlineAttachmentRecordList().size() > 0) {
					uidSavedInlineAttachmentsListMap.put(dataInputRecordId,
							emailRecord.getInlineAttachmentRecordList());
				}
			}
		} catch (Exception e) {
			logger.error("Error occurred in convertEmailMessageToRecord method", e);
			throw new DataInputException("Error occurred in convertEmailMessageToRecord method", e);
		}
		return emailRecord;
	}

	@Override
	public boolean deleteSavedAttachments(String dataInputRecordId) {
		boolean isSuccess = false;
		try {
			if (uidSavedAttachmentsListMap.containsKey(dataInputRecordId)) {
				List<AttachmentRecord> attachmentRecordList = uidSavedAttachmentsListMap.get(dataInputRecordId);
				for (AttachmentRecord attachmentRecord : attachmentRecordList) {
					FileUtility.deleteFile(attachmentRecord.getStoredFileFullPath());
				}
				uidSavedAttachmentsListMap.remove(dataInputRecordId);
			} else if (uidSavedInlineAttachmentsListMap.containsKey(dataInputRecordId)) {
				List<AttachmentRecord> attachmentRecordList = uidSavedInlineAttachmentsListMap.get(dataInputRecordId);
				for (AttachmentRecord attachmentRecord : attachmentRecordList) {
					FileUtility.deleteFile(attachmentRecord.getStoredFileFullPath());
				}
				uidSavedInlineAttachmentsListMap.remove(dataInputRecordId);
			}
			isSuccess = true;
		} catch (Exception ex) {
			logger.error("Failed to delete saved attachment(s)", ex);
		}
		return isSuccess;
	}

	private EmailRecord convertExchangeMessageToRecord(EmailMessage message, String dataInputRecordId, String parent)
			throws DataInputException {
		EmailRecord emailRecord = null;
		try {
			boolean isAllowedByFilterCondition = EmailHelper.isAllowedByFilterCondition(message, dataInputConfig);
			if (isAllowedByFilterCondition) {
				emailRecord = new EmailRecord();

				EmailHelper.updateEmailAttributes(message, emailRecord);
				EmailHelper.updateBodyAndAttachmentsExchangeOnprem(message, emailRecord,
						dataInputConfig.getAttachmentDownloadFolder());
				List<AttachmentRecord> externalAttachmentRecordList = emailRecord.getAttachmentRecordList();
				if (externalAttachmentRecordList != null && externalAttachmentRecordList.size() > 0) {
					uidSavedAttachmentsListMap.put(dataInputRecordId, externalAttachmentRecordList);
					externalAttachmentRecordList.stream()
							.forEach(attachmentRecord -> attachmentRecord.setActualFileName(
									parent + DataInOutConstants.FILE_SEPARATOR + attachmentRecord.getActualFileName()));
				}
				if (emailRecord.getInlineAttachmentRecordList() != null
						&& emailRecord.getInlineAttachmentRecordList().size() > 0) {
					uidSavedInlineAttachmentsListMap.put(dataInputRecordId,
							emailRecord.getInlineAttachmentRecordList());
				}

			}
		} catch (Exception e) {
			logger.error("Error occurred in convertExchangeMessageToRecord method", e);
			throw new DataInputException("Error occurred in convertExchangeMessageToRecord method", e);
		}
		return emailRecord;
	}
}
