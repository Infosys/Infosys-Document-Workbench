/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.service.common;

public class WorkbenchConstants {

	public static final String SQL_DELIMITER_RETURNING = "returning";
	public static final String API_TIMESTAMP_FORMAT_12HR = "yyyy-MM-dd hh:mm:ss a";
	public static final String API_DATE_FORMAT = "yyyy-MM-dd";
	public static final String TIMESTAMP_FORMAT_24HR = "yyyy-MM-dd hh:mm:ss";

	public static final int EVENT_TYPE_CDE_ACTION_CREATED = 500;
	public static final int EVENT_TYPE_CDE_CASE_ASSIGNED = 400;
	public static final int EVENT_TYPE_CDE_CASE_CLOSED = 800;

	public static final int EMAIL = 1;
	public static final int CREATED_BY_TYPE_CDE_SYSTEM = 1;

	// API Response Code and Message
	public static final int API_RESPONSE_CDE_SUCCESS = 0;
	public static final String API_RESPONSE_MSG_SUCCESS = "Success";
	public static final int API_RESPONSE_CDE_FAILURE = 999;
	public static final String API_RESPONSE_MSG_FAILURE = "Failure";
	public static final int API_RESPONSE_CDE_PARTIAL_SUCCESS = 50;
	public static final String API_RESPONSE_MSG_PARTIAL_SUCCESS = "PartialSuccess";
	public static final int API_RESPONSE_CDE_NO_RECORDS = 100;
	public static final String API_RESPONSE_MSG_NO_RECORDS = "No records";
	public static final int API_RESPONSE_CDE_REQUEST_VALIDATION_FAILED = 101;
	public static final String API_RESPONSE_MSG_REQUEST_VALIDATION_FAILED = "Request validation failed";
	public static final int API_RESPONSE_CDE_NOT_SERVICE_ACCOUNT = 102;
	public static final String API_RESPONSE_MSG_NOT_SERVICE_ACCOUNT = "Token can be generated only for a service account";
	public static final int API_RESPONSE_CDE_BAD_CREDENTIALS = 103;
	public static final String API_RESPONSE_MSG_BAD_CREDENTIALS = "Bad Credentials";
	public static final int API_RESPONSE_CDE_BASIC_AUTH_MISSING = 104;
	public static final String API_RESPONSE_MSG_BASIC_AUTH_MISSING = "Credentials should be passed as Basic Auth";
	public static final int API_RESPONSE_CDE_CONCURRENT = 105;
	public static final String API_RESPONSE_MSG_CONCURRENT = "Failed due to Concurrency";
	public static final int API_RESPONSE_CDE_INVALID_TENANT_ID = 106;
	public static final String API_RESPONSE_MSG_INVALID_TENANT_ID = "Please provide valid tenant id.";
	public static final int API_RESPONSE_CDE_CASE_CANT_BE_CLOSED = 107;
	public static final String API_RESPONSE_MSG_CASE_CANT_BE_CLOSED = "Case Cannot be clossed due to pending actions";
	public static final int API_RESPONSE_CDE_ACCOUNT_DISABLED_OR_INACTIVE = 108;
	public static final String API_RESPONSE_MSG_ACCOUNT_DISABLED_OR_INACTIVE = "User is disabled or account is not activated. Please contact admin.";
	public static final int API_RESPONSE_CDE_MULTI_ATTRIBUTE_ALREADY_EXIST = 109;
	public static final String API_RESPONSE_MSG_MULTI_ATTRIBUTE_ALREADY_EXIST = "Multi attribute is already exists. Please update existing one.";
	public static final int API_RESPONSE_CDE_ACCOUNT_ROLE_NOT_ASSIGNED = 110;
	public static final String API_RESPONSE_MSG_ACCOUNT_ROLE_NOT_ASSIGNED = "User is not assigned to any Role. Please contact admin.";
	public static final int API_RESPONSE_CDE_NOT_AUTHORIZED = 111;
	public static final String API_RESPONSE_MSG_NOT_AUTHORIZED = "You are not authorized to use this application. Please contact admin.";

	// db entities that are used in service
	public static final String ATTR_NAME_TXT_MULTI_ATTRIBUTE = "Multi-Attribute";
	public static final String ATTR_NAME_TXT_MULTI_ATTRIBUTE_TABLE = "Multi-Attribute Table";

	public static final int CONFIDENCE_PCT_UNSET = -1; // Confidence value undefined
	public static final String END_DTM_UNSET = null; // enddtm value undefined
	public static final int DOC_ACTION_REL_ID_UNSET = -1;

	public static final int AUTH_TOKEN_ERROR_CDE_NOT_SERVICE_ACCOUNT = 1;
	public static final int AUTH_TOKEN_ERROR_CDE_BAD_CREDENTIALS = 2;
	public static final int AUTH_TOKEN_ERROR_CDE_INVALID_TENANT_ID = 3;
	public static final int AUTH_TOKEN_ERROR_CDE_ACCOUNT_DISABLED_OR_INACTIVE = 4;
	public static final int AUTH_TOKEN_ERROR_CDE_ACCOUNT_ROLE_NOT_ASSIGNED = 5;
	public static final int AUTH_TOKEN_ERROR_CDE_USER_UNAUTHORIZED = 6;

	public static final String EXTRACT_TYPE_VAL_TABLE_NAME = "extract-type";

	// String constants
	public static final String JWT_APP_NAME = "DocumentWorkbench";
	public static final String DB_ERROR_MSG_USERNAME_ALREADY_EXISTS = "Username already exists";
	public static final String FIND_REPLACE_STRING = "FindReplace((";
	public static final String AUTH_ERROR_MSG_ACCESS_DENIED_NON_USER = "Access denied for non user type accounts.";
	public static final String INVALID_TENANT_ID = "Please provide valid tenant id.";
	public static final String HTML_TAG_REGEX = "<[/]?html[^>]*>";
	public static final String ENTITY_IDENTIFIER = "{ENTITY_IDENTIFIER}";
	public static final String ROLE_TYPE_TXT_PLACEHOLDER = "{ROLE_TYPE_TXT}";
	public static final String ROLE_TYPE_TXT_OWNER = "Owner";
	public static final String ROLE_TYPE_TXT_REVIEWER = "Reviewer";
	public static final String ATTR_ENTITY = "ENTITY";
	public static final String COLUMN = "{COLUMN_TXT}";
	public static final String USER_TYPE_EXTERNAL = "External";
	public static final String USER_TYPE_SERVICE = "Service";
	public static final String ENABLED = "Enabled";
	public static final String DISABLED = "Disabled";
	public static final String AUTH_HEADER_PROP_NAME = "Authorization";
	public static final String AUTH_HEADER_PROP_VALUE_PREFIX = "Bearer ";
	public static final String TENANT_ID = "tenantId";
	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";
	public static final String LESSER_THAN = "<";
	public static final String RULE_FILE_NAME_NO_ACTIONS = "NoActions";
	public static final String RULE_FILE_NAME_MULTIPLE_ACTIONS = "MultipleActions";
	public static final String DOC_FILTER = "<<DOCFILTER>>";
	public static final String DOC_TABLE_FILTER = "<<DOCTABLEFILTER>>";
	public static final String ATTR_FILTER_BY_SEARCH_CRITERIA = "<<ATTR_FILTER_BY_SEARCH_CRITERIA>>";
	public static final String ATTR_FILTER = "<<ATTRFILTER>>";
	public static final String DATE_FILTER_1 = "<<DATEFILTER1>>";
	public static final String DATE_FILTER_2 = "<<DATEFILTER2>>";
	public static final String PAGE_FILTER = "<<PAGEFILTER>>";
	public static final String ATTACH_FILTER = "<<ATTACHFILTER>>";
	public static final String SORT_BY_ATTR_NAME_CDE_FILTER_1="<<SORTBYATTRNAMECDEFILTER1>>";
	public static final String SORT_BY_ATTR_NAME_CDE_FILTER_2="<<SORTBYATTRNAMECDEFILTER2>>";
	public static final String DOC_ATTR_NAME_CDE_FILTER="<<DOC_ATTR_NAME_CDE_FILTER>>";
	public static final String ATTACH_ATTR_NAME_CDE_FILTER="<<ATTACH_ATTR_NAME_CDE_FILTER>>";
	public static final String DOC_ATTR_DETAIL="<<DOC_ATTR_DETAIL>>";
	public static final String ATTACH_ATTR_DETAIL="<<ATTACH_ATTR_DETAIL>>";
	public static final String DOC_COUNT_FOR_USER_FILTER="<<DOCCOUNTFORUSERFILTER>>";
	public static final String APP_USER_ID_ARRAY_FILTER="<<APPUSERIDARRAYFILTER>>";
	public static final String DB_ERROR_MSG_ATTRIBUTE_ALREADY_EXISTS = "Attribute already exists";
	public static final String DOC_ATTR_REL_ID = "docAttrRelId";
	public static final String ATTACH_ATTR_REL_ID = "attachmentAttrRelId";
	public static final String EMAIL_REPLY_SUBJECT_PREFIX = "RE: ";
	public static final String EMAIL_REPLY_SUBJECT_SUFFIX = " - Case# ";
	public static final String EMAIL_BODY_TXT_STANDARD_FILE_NAME = "EmailBody.txt";
	public static final String EMAIL_BODY_HTML_STANDARD_FILE_NAME = "EmailBody.html";
	public static final String CONTENT_FROM_FILE_SYSTEM = "<<ContentFromFileSystem>>";
	public static final String PLACEHOLDER_CONTENT_TXT = "<<Content>>";
	public static final String PLACEHOLDER_CONTENT_HTML = "<<ContentHtml>>";
	
	public static final String SQL_BLOCK_HIGHEST_EVENT_BEGIN = "<HIGHEST-EVENT-BLOCK-BEGIN>";
	public static final String SQL_BLOCK_HIGHEST_EVENT_END = "<HIGHEST-EVENT-BLOCK-END>";
	public static final String SQL_BLOCK_LATEST_EVENT_BEGIN = "<LATEST-EVENT-BLOCK-BEGIN>";
	public static final String SQL_BLOCK_LATEST_EVENT_END = "<LATEST-EVENT-BLOCK-END>";
	
	// Situations
	public static final int PASSWORD_MISMATCH_EXISTING = -1;
	public static final int PASSWORD_SAME_AS_EXISTING = -2;
	public static final int PASSWORD_MANAGED_BY_LDAP = -3;
	public static final int CASE_OWNER_NOT_SAME_AS_USER = -1;
	public static final int CASE_STATUS_DIFFERENT = -99;
	public static final int CASE_HAS_PENDING_ACTIONS = -2;
	public static final int PREV_USER_DETAILS_OUTDATED = -99;
	public static final int CASE_IS_UNASSIGNED = -2; // Unassigned
	public static final int CASE_IS_ASSIGNED = -1; // Assigned to other 
	public static final int CASE_FOR_MY_REVIEW = -3; //MyReview Cases

	public static final String JSON_MULTI_ATTR_GROUP_NAME = "gn";
	public static final String JSON_MULTI_ATTR_ITEMS = "items";
	public static final String JSON_MULTI_ATTR_PARAMETER_ID = "pid";
	public static final String JSON_MULTI_ATTR_PARAMETER_NAME = "pn";
	public static final String JSON_MULTI_ATTR_PARAMETER_VAlUE = "pv";
	public static final String JSON_MULTI_ATTR_PARAMETER_CONFIDENCE_PCT = "pc";
	public static final String JSON_MULTI_ATTR_PARAMETER_EXTRACT_TYPE_CDE = "pe";
	public static final String JSON_MULTI_ATTR_PARAMETER_CREATE_BY = "cb";
	public static final String JSON_MULTI_ATTR_PARAMETER_CREATE_DATE = "cd";
	public static final String JSON_MULTI_ATTR_PARAMETER_LAST_UPDATE_BY = "lb";
	public static final String JSON_MULTI_ATTR_PARAMETER_LAST_UPDATE_DATE = "ld";

	public static final String JSON_MULTI_ATTR_PARAMETER_END_DTM = "pdtm";

	public static final String ID = "id";
	public static final String ATTR_NAME_TXT = "attrNameTxt";
	public static final String ATTR_VALUE = "attrValue";
	public static final String CONFIDENCE_PCT = "confidencePct";
	public static final String DOCID = "docId";
	public static final String ATTR_DATA_FIELD_DOCUMENT = "document";
	public static final String ATTR_DATA_FIELD_CREATEBY = "createBy";

	public static final int ATTR_CONFIDENCE_PCT_ROUND_OFF_DECIMAL_POINT = 2;

	public static final String FILE_EXTENSION_PDF = "pdf";
	public static final String FILE_EXTENSION_JPG = "jpg";
	public static final String FILE_EXTENSION_JPEG = "jpeg";
	public static final String FILE_EXTENSION_GIF = "gif";
	public static final String FILE_EXTENSION_PNG = "png";
	public static final String FILE_EXTENSION_TXT = "txt";
	public static final String FILE_EXTENSION_HTML = "html";
	public static final String FILE_EXTENSION_BMP = "bmp";
	public static final String FILE_EXTENSION_CONLL = "conll";
	public static final String FILE_EXTENSION_ZIP = "zip";

	public static final String DEFAULT_TIMESTAMP = " 00:00:00.000";
	public static final String EXPORT_ANNOTATION_TAG_B = " B-";
	public static final String EXPORT_ANNOTATION_TAG_I = " I-";
	public static final String EXPORT_ANNOTATION_TAG_O = " O";

	public final static String FILE_SEPARATOR = "/";
	
	public static final String FEATURE_ID_CASE_ASSIGN = "case-user-create";
	public static final String FEATURE_ID_CASE_REASSIGN = "case-user-edit";
	public static final String FEATURE_ID_CASE_REVIEW_USER_ASSIGN = "case_review-user-create";
	public static final String FEATURE_ID_CASE_REVIEW_USER_REASSIGN = "case_review-user-edit";
	public static final String FEATURE_ID_CASE_OWN_USER_ALLOW = "case-user-allow";
	public static final String FEATURE_ID_CASE_REVIEW_USER_ALLOW = "case_review-user-allow";
	public static final String FEATURE_ID_ACTION_LIST = "action-list";
	public static final String FEATURE_ID_ACTION_VIEW = "action-view";
	public static final String FEATURE_ID_ATTRIBUTE_CREATE = "attribute-create";
	public static final String FEATURE_ID_ATTRIBUTE_EDIT = "attribute-edit";
	public static final String FEATURE_ID_ATTRIBUTE_DELETE = "attribute-delete";
	
	public static final String APP_VARIABLE_KEY_RBAC = "rbac";
	
	public static final int USER_SOURCE_INTERNAL = 1;
	public static final int USER_SOURCE_EXTERNAL = 2;
	
	public static final String MESSAGE_PROP_QUEUE_NAME_CDE = "queueNameCde";
	public static final String MESSAGE_PROP_DOC_ID = "docId";
	public static final String MESSAGE_PROP_TASK_STATUS_CDE = "taskStatusCde";
	public static final String MESSAGE_PROP_EVENT_TYPE_CDE = "eventTypeCde";
	public static final String MESSAGE_PROP_TENANT_ID="tenantId";
	
	public static final long DOC_ROLE_TYPE_CDE_FOR_OWNER=1;
	public static final long SEQUENCE_NUM = 0;

}
