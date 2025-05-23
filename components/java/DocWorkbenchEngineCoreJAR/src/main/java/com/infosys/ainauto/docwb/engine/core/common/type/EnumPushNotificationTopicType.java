/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.core.common.type;

public enum EnumPushNotificationTopicType {
	DOCUMENT_DOWNLOADER("docdownloader"),
	ATTRIBUTE_EXTRACTOR("attributeextractor"),
	CASE_OPENER("caseopener"),
	ACTION_EXECUTOR("actionexecutor"),
	OUTBOUND_EMAIL_SENDER("outboundemailsender"),
	ACTION_SCRIPT_EXECUTOR("actionscriptexecutor"),
	ACTION_SCRIPT_RESULT_UPDATER("actionscriptresultupdater"),
	EXTRACT_ATTRIBUTE_ACTION_EXECUTOR("extractattributeactionexecutor")
	;

	private String propertyValue;

	private EnumPushNotificationTopicType(String s) {
		propertyValue = s;
	}

	public String getValue() {
		return propertyValue;
	}
}
