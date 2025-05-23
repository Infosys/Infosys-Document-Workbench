/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.datasource.config.email;

import com.infosys.ainauto.datasource.config.DataSourceConfig;

public class EmailServerDataSourceWriterConfig extends DataSourceConfig {

	private String hostName;
	private String port;
	private String senderName;
	private String senderEmailId;
	private String senderPassword;
	private String authenticateUser;
	private String storeProtocol = "imap";
	private int StoreProtocolPort = 143;

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

	public String getAuthenticateUser() {
		return authenticateUser;
	}

	public void setAuthenticateUser(String authenticateUser) {
		this.authenticateUser = authenticateUser;
	}

	public String getsenderName() {
		return senderName;
	}

	public void setSenderName(String senderName) {
		this.senderName = senderName;
	}

	public String getSenderEmailId() {
		return senderEmailId;
	}

	public void setSenderEmailId(String senderEmailId) {
		this.senderEmailId = senderEmailId;
	}

	public String getSenderPassword() {
		return senderPassword;
	}

	public void setSenderPassword(String senderPassword) {
		this.senderPassword = senderPassword;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

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
	 * @return
	 */
	public void setStoreProtocol(String storeProtocol) {
		this.storeProtocol = storeProtocol;
	}

	/**
	 * Mailbox server imap port number . Default value is <b>143</b>
	 * 
	 * @return
	 */
	public int getStoreProtocolPort() {
		return StoreProtocolPort;
	}

	public void setStoreProtocolPort(int storeProtocolPort) {
		StoreProtocolPort = storeProtocolPort;
	}
}
