/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.datasource.impl.emailserver.basic.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.UIDFolder;
import javax.mail.search.FlagTerm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.infosys.ainauto.datasource.api.IDataSourceReader;
import com.infosys.ainauto.datasource.common.DataSourceException;
import com.infosys.ainauto.datasource.config.DataSourceConfig;
import com.infosys.ainauto.datasource.config.email.EmailServerDataSourceReaderConfig;
import com.infosys.ainauto.datasource.model.email.EmailServerDataSourceRecord;
import com.infosys.ainauto.datasource.model.DataSourceFolder;
import com.infosys.ainauto.datasource.model.DataSourceRecord;
import com.infosys.ainauto.datasource.model.email.EmailServerDataSourceFolder;

public class EmailServerDataSourceReader implements IDataSourceReader {

	private final static Logger logger = LoggerFactory.getLogger(EmailServerDataSourceReader.class);
	private Store store;
	private String name;
	private EmailServerDataSourceReaderConfig emaiServerlDataSourceReaderConfig;
	private Map<String, Message> uidMessageMap = new HashMap<String, Message>();
	private Folder folderToRead = null;

	public EmailServerDataSourceReader(String name, DataSourceConfig dataSourceReaderConfig) {
		this.name = name;
		this.emaiServerlDataSourceReaderConfig = (EmailServerDataSourceReaderConfig) dataSourceReaderConfig;
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
		String hostname, username, password, protocol, sslEnabled;
		int port;
		try {

			protocol = emaiServerlDataSourceReaderConfig.getStoreProtocol();

			hostname = emaiServerlDataSourceReaderConfig.getHostName();
			port = emaiServerlDataSourceReaderConfig.getPortNumber();

			username = emaiServerlDataSourceReaderConfig.getMailboxUserName().trim();
			password = emaiServerlDataSourceReaderConfig.getMailboxPassword().trim();

			sslEnabled = emaiServerlDataSourceReaderConfig.getSslEnabled();

			Properties props = new Properties();
			props.setProperty("mail.store.protocol", protocol);
			props.setProperty("mail.imap.ssl.enable", sslEnabled);

			// session creation
			Session session = Session.getInstance(props, null);

			// store class is a class that models a message store and its access
			// protocol,
			// for storing and retrieving messages
			store = session.getStore(protocol);
			store.connect(hostname, port, username, password);
			logger.info("Connecting to " + hostname + " " + port);
			if (store != null) {
				operationResult = true;
			}
		} catch (Exception e) {
			logger.error("Error occurred in connect method", e);
			if (emaiServerlDataSourceReaderConfig.isOnConnectionErrorIgnore()) {
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
			folderToRead = store.getFolder(emailServerDataSourceFolder.getFolderName());
			folderToRead.open(Folder.READ_WRITE);
			result = true;
		} catch (Exception ex) {
			logger.error("Error occurred in openFolder method", ex);
			if (folderToRead == null && emaiServerlDataSourceReaderConfig.isOnConnectionErrorIgnore()) {
				return result;
			}
			throw new DataSourceException("Error occurred in openFolder method", ex);
		}
		return result;
	}

	@Override
	public boolean closeFolder() throws DataSourceException {
		boolean result = false;
		if (folderToRead == null && emaiServerlDataSourceReaderConfig.isOnConnectionErrorIgnore()) {
			return result;
		}
				
		if (folderToRead == null) {
			throw new DataSourceException("No open mailbox folder found to close.");
		}
		
		try {
			folderToRead.close(true);
			folderToRead = null;
			result = true;
		} catch (Exception ex) {
			logger.error("Error occurred in closeFolder method", ex);
			throw new DataSourceException("Error occurred in closeFolder method", ex);
		}
		return result;
	}

	@Override
	public List<DataSourceRecord> getNewItems() throws DataSourceException {
		// List<Message> messageList = new ArrayList<>();
		List<DataSourceRecord> dataSourceRecordList = new ArrayList<>();
		UIDFolder uidFolder;
		Message messages[] = null;

		if (folderToRead == null && emaiServerlDataSourceReaderConfig.isOnConnectionErrorIgnore()) {
			return dataSourceRecordList;
		}
		
		if (folderToRead == null) {
			throw new DataSourceException("A mailbox folder needs to be opened first by calling openFolder() method.");
		}

		try {
			uidFolder = (UIDFolder) folderToRead;
			messages = folderToRead.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
			int mailCount = messages.length;
			logger.debug("Number of mails identified: " + mailCount);

			Long messageId;
			for (int i = 0; i < mailCount; i++) {
				Message message = messages[i];
				messageId = uidFolder.getUID(message);

				// Fix for issue - email is automatically marked as read/seen after reading it
				// This is new behavior after CCD changed mail server
				// So, to make it work as before, mark email as unread using below conditional
				// logic

				// Logic shifted to parent component
//				if (message.getFlags().contains(Flags.Flag.SEEN)) {
//					toggleMessageFlag(message, Flags.Flag.SEEN, false);
//				}

				dataSourceRecordList.add(new EmailServerDataSourceRecord(String.valueOf(messageId), message, null));

				// messageList.add(message);

				uidMessageMap.put(String.valueOf(messageId), messages[i]);
				logger.debug("EMail " + (i + 1) + " | Email_UID : " + messageId + " | Received Date : "
						+ messages[i].getReceivedDate() + " added to list successfully");
			} // End of for loop : for (int i = 0; i<mailCount; i++)
			logger.debug("email List size is :" + dataSourceRecordList.size());
		} catch (Exception e) {
			logger.error("Error occurred in getUnreadEmails method", e);
			throw new DataSourceException("Error occurred in getUnreadEmails method", e);
		}
		return dataSourceRecordList;
	}

	/**
	 * @Description this method is use to update particular ticket as read
	 * @param uId
	 */
	@Override
	public boolean updateItemAsRead(String dataSourceRecordId) {
		boolean operationResult = false; // Assume operation will fail
		Message message = uidMessageMap.get(dataSourceRecordId);
		operationResult = toggleMessageFlag(message, Flags.Flag.SEEN, true);
		return operationResult;
	}

	/**
	 * @Description this method is use to update particular ticket as read
	 * @param uId
	 */
	@Override
	public boolean updateItemAsUnread(String dataSourceRecordId) {
		boolean operationResult = false; // Assume operation will fail
		Message message = uidMessageMap.get(dataSourceRecordId);
		operationResult = toggleMessageFlag(message, Flags.Flag.SEEN, false);
		return operationResult;
	}

	/**
	 * @Description this method to disconnect or close the connection
	 */
	@Override
	public boolean disconnect() {
		boolean operationResult = false; // Assume operation will fail
		try {
			if (store != null) {
				store.close();
				store = null;
				operationResult = true;
				logger.info("Connection to the mail server closed successfully");
			}
		} catch (Exception e) {
			logger.error("Failed To Close the connection", e);
		}
		return operationResult;
	}

	/**
	 * @Description create a folder inside INBOX
	 * @param dataSourceFolder
	 */
	@Override
	public boolean createFolder(DataSourceFolder dataSourceFolder) {
		boolean operationResult = false; // Assume operation will fail
		try {
			EmailServerDataSourceFolder emailServerDataSourceFolder = (EmailServerDataSourceFolder) dataSourceFolder;

			String folderNames[] = (emailServerDataSourceFolder.getFolderName()).split(Pattern.quote("/"));
			Folder folderToCreate = null;
			for (String fn : folderNames) {
				if (folderToCreate == null) {
					folderToCreate = store.getFolder(fn);
				} else {
					folderToCreate = folderToCreate.getFolder(fn);
				}
				if (!folderToCreate.exists()) {
					boolean isCreated = folderToCreate.create(Folder.HOLDS_MESSAGES);
					if (!isCreated) {
						throw new Exception("Could not create new folder");
					}

					// Thread.sleep(5000);
				}
			}
			operationResult = true;
		} catch (Exception e) {
			logger.error("Some error while creating folder" + e);
		}
		return operationResult;
	}

	/**
	 * @Description delete a folder inside INBOX
	 * @param targetFolderName
	 */
	@Override
	public boolean deleteFolder(DataSourceFolder dataSourceFolder) {
		boolean operationResult = false; // Assume operation will fail
		try {
			Folder defaultFolder = store.getFolder(emaiServerlDataSourceReaderConfig.getMailboxDefaultFolder());
			defaultFolder.open(Folder.READ_WRITE);

			// Folder folderToDelete =
			// defaultFolder.getFolder(targetFolderName);

			EmailServerDataSourceFolder emailServerDataSourceFolder = (EmailServerDataSourceFolder) dataSourceFolder;
			Folder folderToDelete = store.getFolder(emailServerDataSourceFolder.getFolderName());
			if (folderToDelete.exists()) {
				boolean isDeleted = folderToDelete.delete(true);
				if (!isDeleted) {
					throw new Exception("Could not delete existing folder");
				}
				operationResult = true;
				// Thread.sleep(5000);
			}

		} catch (Exception e) {
			logger.error("Some error while creating folder" + e);
		}
		return operationResult;
	}

	@Override
	public int moveItemToFolder(List<String> dataSourceRecordIdList, DataSourceFolder sourceDataSourceFolderName,
			DataSourceFolder targetDataSourceFolderName) {
		String sourceFolderName = ((EmailServerDataSourceFolder) sourceDataSourceFolderName).getFolderName();
		String targetFolderName = ((EmailServerDataSourceFolder) targetDataSourceFolderName).getFolderName();

		return copyEmail(dataSourceRecordIdList, sourceFolderName, targetFolderName, true);
	}

	@Override
	public int copyItemToFolder(List<String> dataSourceRecordIdList, DataSourceFolder sourceDataSourceFolderName,
			DataSourceFolder targetDataSourceFolderName) {

		String sourceFolderName = ((EmailServerDataSourceFolder) sourceDataSourceFolderName).getFolderName();
		String targetFolderName = ((EmailServerDataSourceFolder) targetDataSourceFolderName).getFolderName();

		return copyEmail(dataSourceRecordIdList, sourceFolderName, targetFolderName, false);
	}

	private boolean toggleMessageFlag(Message message, Flag flag, boolean status) {
		boolean operationResult = false; // Assume operation will fail
		try {
			if (message != null) {
				message.setFlag(flag, status);
				operationResult = true;
			}
		} catch (MessagingException e) {
			logger.error("Error occurred in toggleMessageFlag", e);
		}
		return operationResult;
	}

	/**
	 * @Description Copy email(s) from source to destination
	 * 
	 */
	private int copyEmail(List<String> uidList, String sourceFolderName, String destinationFolderName,
			boolean deleteOriginal) {
		int processedCount = 0;
		try {
			String uid;
			Vector<Message> matchedMsgs = new Vector<>();
			for (int i = 0; i < uidList.size(); i++) {
				uid = String.valueOf(uidList.get(i));
				matchedMsgs.addElement(uidMessageMap.get(uid));
				processedCount++;
			}

			// tempMessages is the mail list to be moved
			Message[] tempMessages = new Message[matchedMsgs.size()];
			matchedMsgs.copyInto(tempMessages);

			Folder sourceFolder = store.getFolder(sourceFolderName);
			sourceFolder.open(Folder.READ_WRITE);

			Folder destinationFolder = store.getFolder(destinationFolderName); // sourceFolder.getFolder(destinationFolderName);
			if (uidList.size() != 0) {
				// copy the selected emails from Inbox to Destination folder
				// inside the Inbox
				sourceFolder.copyMessages(tempMessages, destinationFolder);
				// set all the selected mail to deleted
				if (deleteOriginal) {
					sourceFolder.setFlags(tempMessages, new Flags(Flags.Flag.DELETED), true);
				}
			}
		} catch (Exception e) {
			logger.error("Error while copying email", e);
		}
		return processedCount;
	}
}
