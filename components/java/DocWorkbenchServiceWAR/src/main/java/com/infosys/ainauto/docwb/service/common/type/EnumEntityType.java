/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.common.type;

public enum EnumEntityType {
	ACTION("action"), ATTACHMENT("attachment"),ATTACHMENT_REL("attachment_relation"), ATTRIBUTE("attribute"), DOC_ATTRIBUTE(
			"documentattribute"), ATTACH_ATTRIBUTE("attachmentattribute"), EMAIL("email"), DOCUMENT(
					"document"), CASE_OWNER_ASSIGNMENT("caseownerassignment"), CASE_REVIEWER_ASSIGNMENT("casereviewerassignment"), ROLE("role"), USER("user"), 
	QUEUE_ASSIGNMENT("queueassignment"), APP_VARIABLE("app_variable"),QUEUE("queue");

	private String propertyValue;

	private EnumEntityType(String s) {
		propertyValue = s;
	}

	public String getValue() {
		return propertyValue;
	}
}
