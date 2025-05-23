/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.datainout.config;

import java.util.List;

public class DataInputConfig extends AbstractDataInOutConfig {
	private String attachmentDownloadFolder;
	private List<String> stringMatchForIncludeList;
	private List<String> stringMatchForExcludeList;
	private String stringMatchForIncludeCondition;
	private String stringMatchForExcludeCondition;

	public String getAttachmentDownloadFolder() {
		return attachmentDownloadFolder;
	}

	public void setAttachmentDownloadFolder(String attachmentDownloadFolder) {
		this.attachmentDownloadFolder = attachmentDownloadFolder;
	}
	
	/**
	 * List of strings used to filter IN emails based on match with subject of email
	 * @return
	 */
	public List<String> getStringMatchForIncludeList() {
		return stringMatchForIncludeList;
	}

	/**
	 * List of strings used to filter IN emails based on match with subject of email
	 * @param stringMatchForIncludeList
	 */
	public void setStringMatchForIncludeList(List<String> stringMatchForIncludeList) {
		this.stringMatchForIncludeList = stringMatchForIncludeList;
	}

	/**
	 * List of strings used to filter OUT emails based on match with subject of email
	 * @return
	 */
	public List<String> getStringMatchForExcludeList() {
		return stringMatchForExcludeList;
	}

	/**
	 * List of strings used to filter OUT emails based on match with subject of email
	 * @param stringMatchForExcludeList
	 */
	public void setStringMatchForExcludeList(List<String> stringMatchForExcludeList) {
		this.stringMatchForExcludeList = stringMatchForExcludeList;
	}

	
	/**
	 * Relationship between the list of strings used to filter IN emails. Values are <b>OR</b> or <b>AND</b> 
	 * @return
	 */
	public String getStringMatchForIncludeCondition() {
		return stringMatchForIncludeCondition;
	}

	/**
	 * Relationship between the list of strings used to filter IN emails. Values are <b>OR</b> or <b>AND</b>
	 * @param stringMatchForIncludeCondition
	 */
	public void setStringMatchForIncludeCondition(String stringMatchForIncludeCondition) {
		this.stringMatchForIncludeCondition = stringMatchForIncludeCondition;
	}

	/**
	 * Relationship between the list of strings used to filter OUT emails. Values are <b>OR</b> or <b>AND</b>
	 * @return
	 */
	public String getStringMatchForExcludeCondition() {
		return stringMatchForExcludeCondition;
	}

	/**
	 * Relationship between the list of strings used to filter OUT emails. Values are <b>OR</b> or <b>AND</b>
	 * @param stringMatchForExcludeCondition
	 */
	public void setStringMatchForExcludeCondition(String stringMatchForExcludeCondition) {
		this.stringMatchForExcludeCondition = stringMatchForExcludeCondition;
	}
	
}