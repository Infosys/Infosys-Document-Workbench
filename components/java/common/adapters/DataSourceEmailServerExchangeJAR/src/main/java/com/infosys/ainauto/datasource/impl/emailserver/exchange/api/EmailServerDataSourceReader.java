/** =============================================================================================================== *
 * Copyright 2021 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


package com.infosys.ainauto.datasource.impl.emailserver.exchange.api;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.infosys.ainauto.datasource.api.IDataSourceReader;
import com.infosys.ainauto.datasource.common.DataSourceException;
import com.infosys.ainauto.datasource.config.DataSourceConfig;
import com.infosys.ainauto.datasource.config.email.EmailServerDataSourceReaderConfig;
import com.infosys.ainauto.datasource.model.DataSourceFolder;
import com.infosys.ainauto.datasource.model.DataSourceRecord;
import com.infosys.ainauto.datasource.model.email.EmailServerDataSourceFolder;
import com.infosys.ainauto.datasource.model.email.EmailServerDataSourceRecord;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.PropertySet;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.enumeration.property.BasePropertySet;
import microsoft.exchange.webservices.data.core.enumeration.property.BodyType;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.enumeration.search.FolderTraversal;
import microsoft.exchange.webservices.data.core.enumeration.search.LogicalOperator;
import microsoft.exchange.webservices.data.core.enumeration.service.ConflictResolutionMode;
import microsoft.exchange.webservices.data.core.service.folder.Folder;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.core.service.schema.EmailMessageSchema;
import microsoft.exchange.webservices.data.core.service.schema.FolderSchema;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.property.complex.FolderId;
import microsoft.exchange.webservices.data.search.FindFoldersResults;
import microsoft.exchange.webservices.data.search.FindItemsResults;
import microsoft.exchange.webservices.data.search.FolderView;
import microsoft.exchange.webservices.data.search.ItemView;
import microsoft.exchange.webservices.data.search.filter.SearchFilter;

public class EmailServerDataSourceReader implements IDataSourceReader {

	private final static Logger logger = LoggerFactory.getLogger(EmailServerDataSourceReader.class);
	private Folder folderToRead = null;
	private ExchangeService service;
	private EmailServerDataSourceReaderConfig emailServerDataSourceReaderConfig;
	private Map<String, EmailMessage> uidMessageMap = new HashMap<String, EmailMessage>();

	private String name;

	public EmailServerDataSourceReader(String name, DataSourceConfig dataSourceReaderConfig) {
		this.name = name;
		this.emailServerDataSourceReaderConfig = (EmailServerDataSourceReaderConfig) dataSourceReaderConfig;
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * @Description this method is used to connect the mail server
	 */
	@Override
	public boolean connect() throws DataSourceException {
		boolean operationResult = false; // Assume operation will fail
		try {
			service = new ExchangeService(ExchangeVersion.Exchange2010);
			ExchangeCredentials credentials = new WebCredentials(
					emailServerDataSourceReaderConfig.getMailboxUserName(),
					emailServerDataSourceReaderConfig.getMailboxPassword(),
					emailServerDataSourceReaderConfig.getMailboxDomain());
			service.setCredentials(credentials);
			service.setUrl(new URI(emailServerDataSourceReaderConfig.getServiceUri()));
			operationResult = true;
		} catch (Exception e) {
			logger.error("Error occurred in connect method", e);
			if (emailServerDataSourceReaderConfig.isOnConnectionErrorIgnore()) {
				return operationResult;
			}
			throw new DataSourceException("Error occurred in connect method", e);
		}
		return operationResult;
	}

	@Override
	public boolean openFolder(DataSourceFolder dataSourceFolder) throws DataSourceException {
		boolean result = false;
		try {

			EmailServerDataSourceFolder emailServerDataSourceFolder = (EmailServerDataSourceFolder) dataSourceFolder;

			PropertySet folderViewPropertySet = new PropertySet(BasePropertySet.IdOnly);
			folderViewPropertySet.add(FolderSchema.DisplayName);

			FolderView folderView = new FolderView(100);
			folderView.setPropertySet(folderViewPropertySet);
			folderView.setTraversal(FolderTraversal.Deep);

			FindFoldersResults findFolderResults = service.findFolders(WellKnownFolderName.Root, folderView);
			FolderId folderId = null;

			for (Folder folder : findFolderResults) {
				// Find specific folderId to bind.
				if (folder.getDisplayName().equalsIgnoreCase(emailServerDataSourceFolder.getFolderName())) {
					folderId = folder.getId();
				}
			}

			if (folderId != null) {
				folderToRead = Folder.bind(service, folderId);
			} else {
				throw new DataSourceException("Folder not found in mailbox.");
			}
			result = true;
		} catch (Exception ex) {
			logger.error("Error occurred in openFolder method", ex);
			if (folderToRead == null && emailServerDataSourceReaderConfig.isOnConnectionErrorIgnore()) {
				return result;
			}
			throw new DataSourceException("Error occurred in openFolder method", ex);
		}
		return result;
	}

	@Override
	public List<DataSourceRecord> getNewItems() throws DataSourceException {
		List<DataSourceRecord> dataSourceRecordList = new ArrayList<>();
		if (folderToRead == null && emailServerDataSourceReaderConfig.isOnConnectionErrorIgnore()) {
			return dataSourceRecordList;
		}

		if (folderToRead == null) {
			throw new DataSourceException("A mailbox folder needs to be opened first by calling openFolder() method.");
		}

		try {
			ItemView view = new ItemView(50);
			FindItemsResults<Item> findResults;
			do {
				// Filter used to read only unreal emails.
				SearchFilter sf = new SearchFilter.SearchFilterCollection(LogicalOperator.And,
						new SearchFilter.IsEqualTo(EmailMessageSchema.IsRead, false));

				findResults = folderToRead.findItems(sf, view);
				int mailCount = findResults.getTotalCount();
				logger.debug("Number of mails identified: " + mailCount);
				if (mailCount > 0) {
					// PropertySet used to get email body content.
					PropertySet propertySet = new PropertySet(BasePropertySet.FirstClassProperties);
					propertySet.setRequestedBodyType(BodyType.HTML);
					service.loadPropertiesForItems(findResults.getItems(), propertySet);

					String messageId;
					for (Item item : findResults.getItems()) {
						if (item instanceof EmailMessage) {
							EmailMessage emailMessage = (EmailMessage) item;
							messageId = item.getId().getUniqueId();
							uidMessageMap.put(messageId, emailMessage);
							dataSourceRecordList.add(new EmailServerDataSourceRecord(messageId, emailMessage, null));
						}
					}
					view.setOffset(view.getOffset() + 50);
				}
			} while (findResults.isMoreAvailable());

		} catch (Exception e) {
			logger.error("Error occurred in getUnreadEmails method of Exchange onprem", e);
			throw new DataSourceException("Error occurred in getUnreadEmails method of Exchange onprem", e);
		}
		return dataSourceRecordList;
	}

	@Override
	public boolean disconnect() throws DataSourceException {
		boolean operationResult = false; // Assume operation will fail
		try {
			if (service != null) {
				service.close();
				service = null;
				operationResult = true;
				logger.info("Connection to the Exchange Onprem server closed successfully");
			}
		} catch (Exception e) {
			logger.error("Failed to close the Exchange onprem connection", e);
		}
		return operationResult;
	}

	@Override
	public boolean updateItemAsRead(String dataSourceRecordId) throws DataSourceException {
		boolean operationResult = false; // Assume operation will fail
		EmailMessage emailMessage = uidMessageMap.get(dataSourceRecordId);
		try {
			emailMessage.setIsRead(true);
			emailMessage.update(ConflictResolutionMode.AlwaysOverwrite);
			operationResult = true;
		} catch (Exception e) {
			logger.error("Error occurred in updateItemAsRead of Exchange onprem", e);
		}
		return operationResult;
	}

	@Override
	public boolean updateItemAsUnread(String dataSourceRecordId) throws DataSourceException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean closeFolder() throws DataSourceException {
		boolean result = false;
		if (folderToRead == null && emailServerDataSourceReaderConfig.isOnConnectionErrorIgnore()) {
			return result;
		}

		if (folderToRead == null) {
			throw new DataSourceException("No open mailbox folder found to close.");
		}

		folderToRead = null;
		result = true;
		return result;
	}

	@Override
	public boolean createFolder(DataSourceFolder dataSourceFolder) throws DataSourceException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteFolder(DataSourceFolder dataSourceFolder) throws DataSourceException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int moveItemToFolder(List<String> dataSourceRecordIdList, DataSourceFolder sourceDataSourceFolder,
			DataSourceFolder targetDataSourceFolder) throws DataSourceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int copyItemToFolder(List<String> dataSourceRecordIdList, DataSourceFolder sourceDataSourceFolder,
			DataSourceFolder targetDataSourceFolder) throws DataSourceException {
		// TODO Auto-generated method stub
		return 0;
	}

}
