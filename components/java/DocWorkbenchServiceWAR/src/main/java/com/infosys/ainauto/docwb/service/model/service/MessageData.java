/** =============================================================================================================== *
 * Copyright 2022 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.model.service;

import java.util.Date;

import com.infosys.ainauto.commonutils.DateUtility;
import com.infosys.ainauto.docwb.service.common.WorkbenchConstants;

public class MessageData<T> {

	private T message;
	private String sender;
	private String hostname;
	private String hostIp;
	private String timestamp;

	public T getMessage() {
		return message;
	}

	public void setMessage(T message) {
		this.message = message;
		this.timestamp = DateUtility.toString(new Date(), WorkbenchConstants.API_TIMESTAMP_FORMAT_12HR); // Set current
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public String getHostIp() {
		return hostIp;
	}

	public void setHostIp(String hostIp) {
		this.hostIp = hostIp;
	}

}
