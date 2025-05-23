/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


// The file contents for the current environment will overwrite these during build.
// The build system defaults to the dev environment which uses `environment.ts`, but if you do
// `ng build --env=prod` then `environment.prod.ts` will be used instead.
// The list of which env maps to which file can be found in `.angular-cli.json`.

export const CONSTANTS = {
  APIS: {
    DOCWBSERVICE: {
      GET_AUTHORIZATION_TOKEN: '/api/v1/auth',
      GET_DOCUMENT_LIST: '/api/v1/document',
      GET_DOCUMENT_USER_LIST: '/api/v1/document/user',
      GET_STATUS_VAL: '/api/v1/val?entity=',
      GET_ACTION_MAPPING: '/api/v1/action/mapping',
      SAVE_ACTION_LIST: '/api/v1/action',
      GET_ACTION_LIST: '/api/v1/action',
      MANAGE_ATTRIBUTE_DATA: '/api/v1/multiattribute/manage',
      ADD_ATTRIBUTE_DATA: '/api/v1/attribute/add',
      EDIT_ATTRIBUTE_DATA: '/api/v1/attribute/edit',
      GET_ATTRIBUTE_TEXT: '/api/v1/attribute/names',
      GET_ATTRIBUTE_ATTRIBUTE_MAPPING: '/api/v1/attribute/attribute/mapping',
      GET_ATTRIBUTE_SORTKEY: '/api/v1/attribute/sortkey',
      GET_ATTRIBUTES: '/api/v1/attribute/document',
      GET_ATTACHMENT_ATTRIBUTES: '/api/v1/attribute/attachment',
      GET_ATTRIBUTES_NOTIFICATION: '/api/v1/attribute/notification',
      GET_TEMPLATES: '/api/v1/template/flattened?docId=',
      DELETE_ATTRIBUTE_DATA: '/api/v1/attribute/delete',
      SEND_EMAIL: '/api/v1/email',
      GET_SENT_EMAIL_LIST: '/api/v1/email',
      GET_PRODUCT_DATA: '/api/v1/about',
      GET_DRAFT_EMAIL: '/api/v1/email/draft',
      SAVE_EMAIL_DRAFT: '/api/v1/email/draft',
      GET_ATTACHMENT_LIST: '/api/v1/attachment/doc',
      GET_ATTACHMENT_FILE: '/api/v1/attachment/doc/file',
      GET_ATTACHMENT_FILE_PATH: '/api/v1/attachment/doc/filepath',
      GET_RECOMMENDED_ACTION: '/api/v1/action/recommendation',
      GET_QUEUE_COUNT: '/api/v1/queue/stats',
      GET_USER_LIST: '/api/v1/user',
      GET_LOGGED_USER: '/api/v1/session',
      TERMINATE_SESSION: '/api/v1/session/terminate',
      ADD_USER_TO_DOC: '/api/v1/document/user',
      CLOSE_CASE_FOR_DOC: '/api/v1/document/case',
      EDIT_USER_ROLE: '/api/v1/role',
      EDIT_USER_ENABLED_DATA: '/api/v1/user',
      ADD_USER_QUEUE_DATA: '/api/v1/user/queue',
      DELETE_USER_QUEUE_DATA: '/api/v1/user/queue',
      GET_QUEUE_LIST_FOR_USER: '/api/v1/user/queue',
      GET_LIST_OF_TEAMMATES:'/api/v1/user/team/currentuser',
      GET_USER_LIST_FOR_QUEUE: '/api/v1/queue/users',
      GET_ATTACHMENT_LIST_EMAIL: '/api/v1/attachment/email',
      GET_ATTACHMENT_FILE_EMAIL: '/api/v1/attachment/email/file',
      GET_QUEUE_LIST: '/api/v1/queue',
      EDIT_QUEUE_LIST: '/api/v1/queue',
      GET_CURRENT_USER_QUEUE_LIST:'/api/v1/queue/currentuser',
      EDIT_QUEUE_VISIBILITY_DATE:'/api/v1/queue/currentuser',
      GET_AUDIT_DOCUMENT: '/api/v1/audit/document',
      GET_AUDIT_USER: '/api/v1/audit/user',
      GET_AUDIT_APP_VARIABLE: '/api/v1/audit/variable',
      GET_AUDIT_DOCUMENT_CURRENT_USER:'/api/v1/audit/document/currentuser',
      EDIT_USER_PASSWRD: '/api/v1/session/password',
      ADD_NEW_USER: '/api/v1/user/register',
      GET_ATTR_NAME_VALUES: '/api/v1/attribute/values',
      GET_APP_VARIABLE_DATA: '/api/v1/variable',
      EDIT_APP_VARIABLE_DATA:'/api/v1/variable',
      GET_QUERY_ANS:'/api/v1/model/inference/qna'
    },
    DOCWBWEB: {
      GET_PRODUCT_DATA: '/api/v1/about',
      QUERY_ES:'/api/v1/query/elasticsearch',
      POST_TELEMETRY_DATA:'/api/v1/telemetry/add'
    },
    DOCWBES:{
      ATTR_SRC_INDEX_SEARCH:"/idx-docwb-1/_search"
    }
  },
  ATTR_NAME_CDE: {
    FROM: 1,
    RECEIVED_DATE: 2,
    SUBJECT: 3,
    EMAIL_TO_ID: 5,
    EMAIL_CC_ID: 7,
    CONTENT_TXT: 9,
    CONTENT_HTML: 10,
    UPSTREAM_DOCID:11,
    CATEGORY: 19,
    FROM_ID: 20,
    SENTIMENT: 27,
    FILE_NAME: 30,
    DOCUMENT_TYPE: 31,
    MULTI_ATTRIBUTE: 44,
    MULTI_ATTRIBUTE_TABLE: 45,
    CONTENT_ANNOTATION_ANNOTATOR: 46,
    CONTENT_ANNOTATION: 47,
  },
  ATTRIBUTES: {
    UNDEFINED: 0,
    CONFIDENCE_PCT: {
      MAX: 100,
      MIN: 0,
      UNDEFINED: -1
    },
    EXTRACT_TYPE_CDE: {
      DIRECT_COPY: 1,
      CUSTOM_LOGIC: 2,
      MANUALLY_CORRECTED: 3
    },
    EXTRACT_TYPE_TXT: {
      MANUALLY_CORRECTED: 'Manually Corrected'
    },
    UNKNOWN_ATTR_VALUE: 'Unknown',
    ATTR_VALUE_DELIMITER: ',',
    ATTR_VALUE_TEXTBOX_OFFSET: 5.3,
    ATTR_NAME_TXT: {
      DOCUMENT_TYPE: 'Document Type'
    },
    DELIMITER: '\n----\n',
    ATTR_NAME_SUFFIX_LIST: '::list'
  },
  BROWSER_TYPE: {
    CHROME: 'CHROME',
    INTERNET_EXPLORER: 'INTERNET_EXPLORER',
    FIREFOX: 'FIREFOX',
    UNKNOWN: 'UNKNOWN'
  },
  DOC_TYPE: {
    EMAIL: 1,
    FILE: 2
  },
  DOC_ROLE_TYPE:{
    CASE_OWNER:1,
    CASE_REVIEWER:2
  },
  CONFIG: {
    DOCWBSERVICE_BASE_URL: 'docwbServiceBaseUrl',
    DOCWBFILESERVER_BASE_URL: 'docwbFileServerBaseUrl',
    DOCWBWEB_BASE_URL: 'docwbWebBaseUrl',
    TENANT_ID: 'tenantId',
    TITLE: 'title',
    ENV:'environment',
    TELEMETRYSERVICE_BASER_URL : "telemetry.serviceBaseUrl",
    TELEMETRY_ENABLED : "telemetry.enabled",
    TELEMETRY_AUTHTOKEN: "telemetry.authtoken",
    MODEL_SERVICE_URL:"modelServiceUrl",
  },
  ERROR_CDE: {
    MULTI_ATTRIBUTE_ALREADY_EXIST: 109
  },
  ACTION_TASK_STATUS_CDE: {
    FOR_YOUR_REVIEW: 400,
    COMPLETED: 900,
    INPROGRESS: 200,
    FAILED: 901,
    RETRY: 500,
    REWORK: 450
  },
  ACTION_NAME_CDE: {
    RE_EXTRACT_DATA: 101,
    DATA_ENTRY_COMPLETE: 204,
    DATA_ENTRY_REJECT:205,
    DATA_ENTRY_APPROVE: 206
  },
  RE_EXTRACT_CONFIG: {
    ISSAVE: 'isSave',
    ISDELETED: 'isDeleted',
    ISUPDATED: 'isUpdated',
    ISADDED: 'isAdded',
    ACCEPTED: 'ACCEPTED',
    REJECTED: 'REJECTED',
    ADDATTRIBUTE: 'addAttribute',
    UPDATEATTRIBUTE: 'updateAttribute',
    DELETEATTRIBUTE: 'deleteAttribute',
  },
  EMPTY: '',
  OBSERVABLE_NULL: null,
  EVENT: {
    CANCEL: 'Cancel',
    SAVE: 'Save',
    TABULAR_DATA_SAVE: 'TD_Save'
  },
  USER_TYPE_CDE: {
    USER: 1,
    SYSTEM: 2
  },
  PDF_VIEWERS: {
    PDFJS: 'pdfJS',
    EMBED: 'embed',
    NG2PDF: 'ng2PDF',
    NG2PDFJS: 'ng2PDFJS',
    NG2PDFJSEXTENDED: 'ng2PDFJSX'
  },
  OPERATION_TYPE: {
    ADD: 'A',
    DELETE: 'D',
    EDIT: 'E',
    UNDO_DELETE: 'U'
  },
  ANNOTATOR: {
    SELECTOR: '#textContent'
  },
  INPUT: {
    TEXTAREA: {
      OFFSET: 2
    }
  },
  CUSTOM_EVENT: {
    EXTRACTED_DATA: {
      REFRESH_COMPLETED: 0,
      SAVE_COMPLETED: 1,
      READ_ONLY_ACTIVATED: 2,
      ADD_CLICKED: 3,
      READ_ONLY_DEACTIVATED: -1
    }
  },
  EXTRACTED_DATA_OPERATION_TYPE: {
    ROW_LEVEL: {
      EDIT: 'RLE',
      OPTION_CHECKED: 'RLO'
    }
  },
  CACHE_ENTITY: {
    ATTR_ATTR_MAPPING: 'attr-attr-mapping',
    ATTR_SORTKEY: 'attr-sortkey'
  },
  FILE_METADATA: {
    PDF_SCANNED: 'PDF Scanned',
    PDF_NATIVE: 'PDF Native',
    PLAIN_TXT: 'Plain Text'
  },
  POP_OUT: {
    RE_EXTRACT_CONFIRM: 53
  },
  ANNOTATION_CONFIG: {
    HIDDEN: 1,
    READONLY: 2,
    EDITABLE: 3
  },
  FEATURE_ID_CONFIG:{
    CLOSE_CASE_EDIT:'close_case-edit',
    CASE_DELETE:'case-delete',
    CASE_USER_EDIT:'case-user-edit',
    AUDIT_CASE_VIEW : 'audit_case-view',
    CASE_USER_CREATE: 'case-user-create',
    ACTION_VIEW: 'action-view',
    ACTION_CREATE: 'action-create',
    ACTION_DATA_ENTRY_COMPLETED:'action_data_entry_completed-create',
    ACTION_DATA_ENTRY_APPROVED:'action_data_entry_approved-create',
    ACTION_DATA_ENTRY_REJECT:'action_data_entry_rejected-create',
    AUDIT_USER_VIEW: 'audit_user-view',
    AUDIT_RBAC_VIEW:'audit_rbac-view',
    USER_LIST: 'user-list',
    USER_VIEW: 'user-view',
    USER_EDIT: 'user-edit',
    USER_DELETE: 'user-delete',
    CASE_LIST: 'case-list',
    CORE_VIEW: 'core-view',
    OUTBOUND_EMAIL_VIEW: 'outbound_email-view',
    ATTACHMENT_VIEW: 'attachment-view',
    ATTRIBUTE_VIEW: 'attribute-view',
    ATTRIBUTE_EDIT: 'attribute-edit',
    ATTRIBUTE_CREATE:'attribute-create',
    ATTRIBUTE_DELETE:'attribute-delete',
    ATTRIBUTE_ANNOTAION_VIEW: 'attribute_annotation-view',
    ATTRIBUTE_ANNOTAION_CREATE:'attribute_annotation-create',
    ATTRIBUTE_ANNOTAION_DELETE:'attribute_annotation-delete',
    ATTRIBUTE_SOURCE_VIEW: 'attribute_source-view',
    ATTRIBUTE_EXTRACTIONPATH_VIEW: 'attribute_extractionpath-view',
    OUTBOUND_EMAIL_CREATE: 'outbound_email-create',
    QUEUE_LIST:'queue-list',
    QUEUE_USER_VIEW:'queue_user-view',
    QUEUE_USER_EDIT:'queue_user-edit',
    QUEUE_EDIT:'queue-edit',
    RBAC_VIEW:'rbac-view',
    RBAC_EDIT:'rbac-edit',
    DASHBOARD_CASE_ALL_VIEW:'dashboard_case_all-view',
    DASHBOARD_CASE_TEAM_VIEW:'dashboard_case_team-view',
    CASE_REVIEW_USER_CREATE:'case_review-user-create',
    CASE_REVIEW_USER_EDIT:'case_review-user-edit'
  },
  FEATURE_ERROR_MSG:{
    NOT_ALLOWED :"You are not authorized to this content."
  },
  APP_VARIABLE_KEYS:{
    RBAC:'rbac'
  },
  FILE_READER_MODE:{
    DOCUMENT:1,
    TEXTLAYER:2,
    ANNOTATION:3
  },
  TELEMETRY_EVENT:{
    START:'START',
    IMPRESSION:'IMPRESSION',
    INTERACT:'INTERACT',
    END:'END',
    LOG:'LOG',
    FEEDBACK:'FEEDBACK',
  },
  TELEMETRY_INTERACT_NAME:{
    ACT_LIS:{
      REFRESH: "ACTION_LIST-REFRESH",
      OPEN_RESULT: "ACTION-RESULT"
    },
    ACT_PAN:{
      SHOW_ACTION: "ACTION_PANEL-SHOW_ACTION"
    },
    ACT_PER:{
      EXECUTE: "ACTION_PERFORM-EXECUTE",
      ACTION_DATA_ENTRY_COMP: "ACTION_PERFORM-EXECUTE_DATA_ENTRY_COMPLETE",
      ACTION_DATA_ENTRY_APPRV: "ACTION_PERFORM-EXECUTE_DATA_ENTRY_APPROVE",
      ACTION_DATA_ENTRY_REJECT: "ACTION_PERFORM-EXECUTE_DATA_ENTRY_REJECT",
    },
    CAS_ASS:{
      REASSIGN: "CASE_ASSIGN-REASSIGN"
    },
    CAS_PAN:{
      CASE_HIS:"CASE_PANEL-CASE_HISTORY",
      ASSIGN_TO_ME: "CASE_PANEL-ASSIGN_TO_ME"
    },
    DOC_LIS:{
      UNASSING_TAB : "DOC_LIST-UNASSIGNED",
      ASSIGNED_TAB : "DOC_LIST-ASSIGNED",
      MYCASE_TAB : "DOC_LIST-MYCASE",
      MYREVIEW_TAB:"DOC_LIST-MYREVIEW"
    },
    EXT_DAT:{
      ADD:"EXTRACTED_DATA-ADD",
      CANCEL:"EXTRACTED_DATA-CANCEL",
      DELETE:"EXTRACTED_DATA-DELETE",
      EXPAND_ATTR_ROW:"EXTRACTED_DATA-EXPAND_ATTRIBUTE_ROW",
      EXTRACTION_PATH: "EXTRACTED_DATA-GOTO_EXTRACTION_PATH",
      GOTO_PDF_PAGE:"EXTRACTED_DATA-GOTO_PDF_PAGE",
      HIGHLIGH_ATTR_BBOX:"EXTRACTED_DATA-HIGHLIGH_ATTR_BBOX",
      MOD_OR_REXTRACT: "EXTRACTED_DATA-MOD_OR_REEXTRACT_TOGGLE",
      REEXTRACT:"EXTRACTED_DATA-REEXTRACT",
      REFRESH: "EXTRACTED_DATA-REFRESH",
      SAVE:"EXTRACTED_DATA-SAVE",
      UNDO: "EXTRACTED_DATA-UNDO",
      VIEW_TABLE: "EXTRACTED_DATA-VIEW_TABLE"
    },
    EXT_PAT:{
      GOTO_ED: "EXTRACTION_PATH-GOTO_ED",
      HIGHLIGH_ATTR_BBOX:"EXTRACTION_PATH-HIGHLIGH_ATTR_BBOX"
    },
    FIL_CON:{
      DOWNLOAD:"FILE_CONTENT-DOWNLOAD",
      GOTO_ANNOTATION_LAYER_TAB:"FILE_CONTENT-GOTO_ANNOTATION_LAYER_TAB",
      GOTO_ATTACHMENT:"FILE_CONTENT-GOTO_ATTACHMENT_TAB",
      GOTO_EMAIL:"FILE_CONTENT-GOTO_EMAIL_TAB",
      GOTO_TEXT_LAYER_TAB :"FILE_CONTENT-GOTO_TEXT_LAYER_TAB",
      GOTO_ORIG_DOC_TAB: "FILE_CONTENT-GOTO_ORIG_DOC_TAB",
      GOTO_ORIG_PLAIN_TEXT_TAB:"FILE_CONTENT-GOTO_ORIG_PLAIN_TEXT_TAB",
      GOTO_ORIG_TAB:"FILE_CONTENT-GOTO_ORIGINAL_TAB",
      OPEN_IN_NEW_TAB:"FILE_CONTENT-OPEB_IN_NEW_TAB",
      OPEN_IN_NEW_WINDOW:"FILE_CONTENT-OPEN_IN_NEW_WINDOW"
    },
    HEADER:{
      LOGOUT: "HEADER_LOGOUT"
    },
    MAN_PAS:{
      CHANGE_PASS: "MANAGE_PASSWORD-CHANGE_PASSWORD"
    },
    RBA_LIS:{
      RBAC_HIS : "RBAC_LIST-HISTORY",
      SAVE: "RABC_LIST-SAVE"
    },
    RE_EXT_CON:{
      OK : "RE_EXTRAC_CONFIRM-OK"
    },
    RE_EXT_REV:{
      SAVE : "RE_EXTRAC_REVIEW-SAVE",
      CANCEL:"RE_EXTRAC_REVIEW-CANCEL"
    },
    USE_LIS:{
      SAVE: "USER_LIST-SAVE",
      USER_HIS: "USER_LIST-USER_HISTORY",
      REFRESH: "USER_LIST-REFRESH"
    },
    DASHBOARD:{
      MY_CASES_FLAT:"DASHBOARD-MY_CASES_FLAT_TABLE_STRUCTURE",
      MY_CASES_TREE:"DASHBOARD-MY_CASES_HIERARCHY_TABLE_STRUCTURE",
      ALL_CASES_FLAT:"DASHBOARD-ALL_CASES_FLAT_TABLE_STRUCTURE",
      ALL_CASES_TREE:"DASHBOARD-ALL_CASES_HIERARCHY_TABLE_STRUCTURE",
      DROPDOWN_SEL:"DASHBOARD-DROPDOWN_SELECT",
      REFRESH:"DASHBOARD-TABLE_REFRESH",
      LAST_QUEUE_WORKED:"DASHBOARD-LAST_QUEUE_WORKED_LINK",
      LAST_CASE_WORKED:"DASHBOARD-LAST_CASE_WORKED_LINK",
      MY_CASES:"DASHBOARD-MY_CASES_TAB",
      ALL_CASES:"DASHBOARD-ALL_CASES_TAB"
    },
    QUE_MAN:{
      PER_QUE_MANAGE_OPEN_QUEUE:"PERSONAL_QUEUE_MANAGEMENT-OPEN_QUEUE_RADIO_BUTTON",
      PER_QUE_MANAGE_CLOSED_QUEUE:"PERSONAL_QUEUE_MANAGEMENT-CLOSED_QUEUE_RADIO_BUTTON",
      PER_SAVE:"PERSONAL_QUEUE_MANAGEMENT-SAVE_BUTTON",
      PER_REFRESH:"PERSONAL_QUEUE_MANAGEMENT-REFRESH",
      ADM_OPEN_QUEUE:"GENERAL_QUEUE_MANAGEMENT-OPEN_QUEUE_RADIO_BUTTON",
      ADM_CLOSED_QUEUE:"GENERAL_QUEUE_MANAGEMENT-CLOSED_QUEUE_RADIO_BUTTON",
      ADM_SAVE:"GENERAL_QUEUE_MANAGEMENT-SAVE_BUTTON",
      ADM_REFRESH:"GENERAL_QUEUE_MANAGEMENT-REFRESH"
    }
  },
  DASHBOARD_ATTRIBUTES:{
    TASK_STATUS_CDE_MAP:{
      100: "newCase",
      200: "inProgress",
      400: "forYourReview",
      450: "forYourRework",
      900: "complete",
    },
    TABLE_HEADERS: [
      'queue',
      'newCase',
      'inProgress',
      'forYourReview',
      'forYourRework',
      'complete',
      'actionable'
    ],

  }


};
