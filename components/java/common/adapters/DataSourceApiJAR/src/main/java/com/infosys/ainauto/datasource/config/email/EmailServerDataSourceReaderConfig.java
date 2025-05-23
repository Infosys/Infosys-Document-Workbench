/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.datasource.config.email;

import com.infosys.ainauto.datasource.config.DataSourceConfig;

public class EmailServerDataSourceReaderConfig extends DataSourceConfig {

	private String storeProtocol = "imap";
	private String hostName;
	private int portNumber = 143;
	private String sslEnabled;
	private String mailboxDefaultFolder = "Inbox";
	private String mailboxUserName;
	private String mailboxPassword;
	private String mailboxDomain;
	private String serviceUri;

	/**
	 * Store Protocol. Default value is <b>imap</b>
	 * 
	 * @return
	 */
	public String getStoreProtocol() {
		return storeProtocol;
	}

	/**
	 * Store Protocol. Default value is <b>imap</b>
	 * 
	 * @param storeProtocol
	 */
	public void setStoreProtocol(String storeProtocol) {
		this.storeProtocol = storeProtocol;
	}

	/**
	 * Mailbox server name or IP address
	 * 
	 * @return
	 */
	public String getHostName() {
		return hostName;
	}

	/**
	 * Mailbox server name or IP address
	 * 
	 * @param hostName
	 */
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	/**
	 * Mailbox server port number. Default value is <b>143</b>
	 * 
	 * @return
	 */
	public int getPortNumber() {
		return portNumber;
	}

	/**
	 * Mailbox server port number. Default value is <b>143</b>
	 * 
	 * @param portNumber
	 */
	public void setPortNumber(int portNumber) {
		this.portNumber = portNumber;
	}

	/**
	 * Mailbox default folder name. Default value is <b>Inbox</b>
	 * 
	 * @return
	 */
	public String getMailboxDefaultFolder() {
		return mailboxDefaultFolder;
	}

	/**
	 * Mailbox default folder name. Default value is <b>Inbox</b>
	 * 
	 * @param mailboxDefaultFolder
	 */
	public void setMailboxDefaultFolder(String mailboxDefaultFolder) {
		this.mailboxDefaultFolder = mailboxDefaultFolder;
	}

	/**
	 * Username for connecting to mailbox. E.g. user@infosys.com
	 * 
	 * @return
	 */
	public String getMailboxUserName() {
		return mailboxUserName;
	}

	/**
	 * Username for connecting to mailbox. E.g. user@infosys.com
	 * 
	 * @param mailboxUserName
	 */
	public void setMailboxUserName(String mailboxUserName) {
		this.mailboxUserName = mailboxUserName;
	}

	/**
	 * Plain text password for connecting to mailbox <br>
	 * <b>Last mile encryption/decryption</b> needs to be handled on client side
	 * 
	 * @return
	 */
	public String getMailboxPassword() {
		return mailboxPassword;
	}

	/**
	 * Plain text password for connecting to mailbox. <br>
	 * <b>Last mile encryption/decryption</b> needs to be handled on client side
	 * 
	 * @param mailboxPassword
	 */
	public void setMailboxPassword(String mailboxPassword) {
		this.mailboxPassword = mailboxPassword;
	}

	public String getSslEnabled() {
		return sslEnabled;
	}

	public void setSslEnabled(String sslEnabled) {
		this.sslEnabled = sslEnabled;
	}

	public String getMailboxDomain() {
		return mailboxDomain;
	}

	public void setMailboxDomain(String mailboxDomain) {
		this.mailboxDomain = mailboxDomain;
	}

	public String getServiceUri() {
		return serviceUri;
	}

	public void setServiceUri(String serviceUri) {
		this.serviceUri = serviceUri;
	}

}
