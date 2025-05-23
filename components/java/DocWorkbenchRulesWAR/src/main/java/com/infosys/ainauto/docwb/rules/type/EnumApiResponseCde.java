/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.rules.type;

import java.util.HashMap;
import java.util.Map;

public enum EnumApiResponseCde {
	
	SUCCESS(0,"Success"), 
	NO_RECORDS(100, "No records"), 
	INVALID_RESOURCE(101, "Invalid resource"), 
	SERVER_ERROR(102,"Server error"),
	INVALID_REQUEST(103,"Invalid request"),
	INVALID_TENANT_ID(104, "Invalid tenantId");
	
	//
//	public static final int API_RESPONSE_CDE_SUCCESS = 0;
//	public static final String API_RESPONSE_MSG_SUCCESS = "Success";
//	public static final int API_RESPONSE_CDE_NO_RECORDS = 100;
//	public static final String API_RESPONSE_MSG_NO_RECORDS = "No records";
//	public static final int API_RESPONSE_CDE_INVALID_RESOURCE = 101;
//	public static final String API_RESPONSE_MSG_INVALID_RESOURCE = "Invalid resource";
//	public static final int API_RESPONSE_CDE_SERVER_ERROR = 102;
//	public static final String API_RESPONSE_MSG_SERVER_ERROR = "Server error";
//	public static final int API_RESPONSE_CDE_INVALID_REQUEST = 103;
//	public static final String API_RESPONSE_MSG_INVALID_REQUEST = "Invalid request";
//	public static final int API_RESPONSE_CDE_INVALID_TENANT_ID = 104;
//	public static final String API_RESPONSE_MSG_INVALID_TENANT_ID = "Invalid tenantId";
	//

	private int cde;
	private String message;

	private EnumApiResponseCde(int cde, String message) {
		this.cde= cde;
		this.message = message;
	}

	public int getCdeValue() {
		return this.cde;
	}
	
	public String getMessageValue() {
		return this.message;
	}

	// Reverse Lookup Logic 
	private static final Map<Integer, EnumApiResponseCde> cdeLookup = new HashMap<>();
	
	static {
		for (EnumApiResponseCde enumType : EnumApiResponseCde.values()) {
			cdeLookup.put(enumType.getCdeValue(), enumType);
		}
	}
	
	public static EnumApiResponseCde get(int cde) {
		return cdeLookup.get(cde);
	}
	

}
