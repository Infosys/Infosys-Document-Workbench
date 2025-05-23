ALTER SCHEMA public RENAME TO docwbmain;
ALTER SCHEMA docwbmain OWNER TO admin;

CREATE SCHEMA workflow;
GRANT USAGE ON SCHEMA workflow TO docwbengine;

SET search_path TO docwbmain;
CREATE SEQUENCE sq_tenant_num MINVALUE 1000;

CREATE TABLE TENANT 
(
	tenant_num BIGINT NOT NULL DEFAULT NEXTVAL('sq_tenant_num'),
	tenant_id VARCHAR(50) NOT NULL,
	tenant_name VARCHAR(255),
	end_dtm timestamp(3),
	create_by VARCHAR(50),
	create_dtm timestamp(3),
	last_mod_by VARCHAR(50),
	last_mod_dtm timestamp(3),
	PRIMARY KEY (tenant_num),
	CONSTRAINT uq_tenant_tenant_id UNIQUE (tenant_id)
);

CREATE TABLE ACTION_NAME
(
	action_name_cde BIGINT NOT NULL,
	action_name_txt VARCHAR(255),
	tenant_id VARCHAR(50) NOT NULL,
	create_by VARCHAR(50),
	create_dtm TIMESTAMP(3),
	last_mod_by VARCHAR(50),
	last_mod_dtm TIMESTAMP(3),
	PRIMARY KEY (action_name_cde,tenant_id),
	FOREIGN KEY (tenant_id) REFERENCES TENANT (tenant_id)
);

CREATE INDEX ix_action_name_tenant_id ON ACTION_NAME (tenant_id);

CREATE TABLE ATTRIBUTE_NAME
(
	attr_name_cde BIGINT NOT NULL,
	attr_name_txt VARCHAR(255),
	tenant_id VARCHAR(50) NOT NULL,
	create_by VARCHAR(50),
	create_dtm TIMESTAMP(3),
	last_mod_by VARCHAR(50),
	last_mod_dtm TIMESTAMP(3),
	PRIMARY KEY (attr_name_cde,tenant_id),
	FOREIGN KEY (tenant_id) REFERENCES TENANT (tenant_id)
);

CREATE INDEX ix_attribute_name_tenant_id ON ATTRIBUTE_NAME (tenant_id);

CREATE TABLE DOCUMENT_TYPE_VAL
(
	doc_type_cde BIGINT NOT NULL,
	doc_type_txt VARCHAR(255),
	create_by VARCHAR(50),
	create_dtm TIMESTAMP(3),
	last_mod_by VARCHAR(50),
	last_mod_dtm TIMESTAMP(3),
	PRIMARY KEY (doc_type_cde)
);


CREATE TABLE EVENT_TYPE_VAL
(
	event_type_cde BIGINT NOT NULL,
	event_type_txt VARCHAR(255),
	create_by VARCHAR(50),
	create_dtm TIMESTAMP(3),
	last_mod_by VARCHAR(50),
	last_mod_dtm TIMESTAMP(3),
	PRIMARY KEY (event_type_cde)
);


CREATE TABLE EXTRACT_TYPE_VAL
(
	extract_type_cde BIGINT NOT NULL,
	extract_type_txt VARCHAR(255),
	create_by VARCHAR(50),
	create_dtm TIMESTAMP(3),
	last_mod_by VARCHAR(50),
	last_mod_dtm TIMESTAMP(3),
	PRIMARY KEY (extract_type_cde)
);


CREATE TABLE LOCK_STATUS_VAL
(
	lock_status_cde BIGINT NOT NULL,
	lock_status_txt VARCHAR(255),
	create_by VARCHAR(50),
	create_dtm TIMESTAMP(3),
	last_mod_by VARCHAR(50),
	last_mod_dtm TIMESTAMP(3),
	PRIMARY KEY (lock_status_cde)
);

CREATE TABLE PARAM_NAME
(
	param_name_cde BIGINT NOT NULL,
	param_name_txt VARCHAR(255),
	tenant_id VARCHAR(50) NOT NULL,
	create_by VARCHAR(50),
	create_dtm TIMESTAMP(3),
	last_mod_by VARCHAR(50),
	last_mod_dtm TIMESTAMP(3),
	PRIMARY KEY (param_name_cde,tenant_id),
	FOREIGN KEY (tenant_id) REFERENCES TENANT (tenant_id)
);

CREATE INDEX ix_param_name_tenant_id ON PARAM_NAME (tenant_id);

CREATE SEQUENCE sq_queue_name_cde MINVALUE 10000;

CREATE TABLE QUEUE_NAME
(
	queue_name_cde BIGINT NOT NULL DEFAULT NEXTVAL('sq_queue_name_cde'),
	queue_name_txt VARCHAR(255),
	tenant_id VARCHAR(50) NOT NULL,
	doc_type_cde BIGINT NOT NULL,
	hide_after_dtm TIMESTAMP(3),
	end_dtm TIMESTAMP(3),
	create_by VARCHAR(50),
	create_dtm TIMESTAMP(3),
	last_mod_by VARCHAR(50),
	last_mod_dtm TIMESTAMP(3),
	PRIMARY KEY (queue_name_cde,tenant_id),
	FOREIGN KEY (tenant_id) REFERENCES TENANT (tenant_id),
	FOREIGN KEY (doc_type_cde) REFERENCES DOCUMENT_TYPE_VAL (doc_type_cde)
);

CREATE INDEX ix_queue_name_tenant_id ON QUEUE_NAME (tenant_id);

CREATE TABLE ROLE_TYPE_VAL
(
	role_type_cde BIGINT NOT NULL,
	role_type_txt VARCHAR(255),
	create_by VARCHAR(50),
	create_dtm TIMESTAMP(3),
	last_mod_by VARCHAR(50),
	last_mod_dtm TIMESTAMP(3),
	PRIMARY KEY (role_type_cde)
);

CREATE TABLE DOC_ROLE_TYPE_VAL
(
	doc_role_type_cde BIGINT NOT NULL,
	doc_role_type_txt VARCHAR(255),
	create_by VARCHAR(50),
	create_dtm TIMESTAMP(3),
	last_mod_by VARCHAR(50),
	last_mod_dtm TIMESTAMP(3),
	PRIMARY KEY (doc_role_type_cde)
);


CREATE TABLE TASK_STATUS_VAL
(
	task_status_cde BIGINT NOT NULL,
	task_status_txt VARCHAR(255),
	create_by VARCHAR(50),
	create_dtm TIMESTAMP(3),
	last_mod_by VARCHAR(50),
	last_mod_dtm TIMESTAMP(3),
	PRIMARY KEY (task_status_cde)
);


CREATE TABLE TASK_TYPE_VAL
(
	task_type_cde BIGINT NOT NULL,
	task_type_txt VARCHAR(255),
	create_by VARCHAR(50),
	create_dtm TIMESTAMP(3),
	last_mod_by VARCHAR(50),
	last_mod_dtm TIMESTAMP(3),
	PRIMARY KEY (task_type_cde)
);


CREATE TABLE USER_TYPE_VAL
(
	user_type_cde BIGINT NOT NULL,
	user_type_txt VARCHAR(255),
	create_by VARCHAR(50),
	create_dtm TIMESTAMP(3),
	last_mod_by VARCHAR(50),
	last_mod_dtm TIMESTAMP(3),
	PRIMARY KEY (user_type_cde)
);

CREATE SEQUENCE sq_app_user_id MINVALUE 1000;

CREATE TABLE APP_USER
(
	app_user_id BIGINT NOT NULL DEFAULT NEXTVAL('sq_app_user_id'),
	user_login_id VARCHAR(255),
	user_password VARCHAR(255),
	user_full_name VARCHAR(255),
	user_type_cde BIGINT NOT NULL,
	tenant_id VARCHAR(50) NOT NULL,
	user_email VARCHAR(255),
	access_token BOOLEAN DEFAULT TRUE,
	account_non_locked BOOLEAN DEFAULT TRUE,
	account_non_expired BOOLEAN DEFAULT TRUE,
	credentials_non_expired BOOLEAN DEFAULT TRUE,
	account_enabled BOOLEAN DEFAULT TRUE,
	create_by VARCHAR(50),
	create_dtm TIMESTAMP(3),
	last_mod_by VARCHAR(50),
	last_mod_dtm TIMESTAMP(3),
	PRIMARY KEY (app_user_id),
	CONSTRAINT uq_au_user_login_id_tenant_id UNIQUE (user_login_id,tenant_id),
	FOREIGN KEY (tenant_id) REFERENCES TENANT (tenant_id),
	FOREIGN KEY (user_type_cde) REFERENCES USER_TYPE_VAL (user_type_cde)
);

CREATE INDEX ix_app_user_tenant_id ON APP_USER (tenant_id);

CREATE SEQUENCE sq_doc_action_rel_id MINVALUE 1000;

CREATE TABLE DOC_ACTION_REL
(
	doc_action_rel_id BIGINT NOT NULL DEFAULT NEXTVAL('sq_doc_action_rel_id'),
	doc_id BIGINT NOT NULL,
	action_name_cde BIGINT NOT NULL,
	task_status_cde BIGINT NOT NULL,
	task_type_cde BIGINT NOT NULL,
	tenant_id VARCHAR(50) NOT NULL,
	action_result VARCHAR,
	end_dtm TIMESTAMP(3),
	create_by VARCHAR(50),
	create_dtm TIMESTAMP(3),
	last_mod_by VARCHAR(50),
	last_mod_dtm TIMESTAMP(3),
	snap_shot json,
	PRIMARY KEY (doc_action_rel_id),
	FOREIGN KEY (action_name_cde,tenant_id) REFERENCES ACTION_NAME (action_name_cde,tenant_id),
	FOREIGN KEY (tenant_id) REFERENCES TENANT (tenant_id),
	FOREIGN KEY (task_status_cde) REFERENCES TASK_STATUS_VAL (task_status_cde),
	FOREIGN KEY (task_type_cde) REFERENCES TASK_TYPE_VAL (task_type_cde)
);

CREATE INDEX ix_doc_act_rel_action_name_cde ON DOC_ACTION_REL (action_name_cde);
CREATE INDEX ix_doc_act_rel_task_status_cde ON DOC_ACTION_REL (task_status_cde);
CREATE INDEX ix_doc_act_rel_task_type_cde ON DOC_ACTION_REL (task_type_cde);
CREATE INDEX ix_doc_act_rel_tenant_id ON DOC_ACTION_REL (tenant_id);

CREATE SEQUENCE sq_doc_id MINVALUE 1000000;

CREATE TABLE DOCUMENT
(
	doc_id BIGINT NOT NULL DEFAULT NEXTVAL('sq_doc_id'),
	doc_type_cde BIGINT NOT NULL,
	doc_location VARCHAR(255),
	queue_name_cde BIGINT NOT NULL,
	lock_status_cde BIGINT NOT NULL,
	task_status_cde BIGINT NOT NULL,
	tenant_id VARCHAR(50) NOT NULL,
	end_dtm TIMESTAMP(3),
	create_by VARCHAR(50),
	create_dtm TIMESTAMP(3),
	last_mod_by VARCHAR(50),
	last_mod_dtm TIMESTAMP(3),
	PRIMARY KEY (doc_id),
	FOREIGN KEY (doc_type_cde) REFERENCES DOCUMENT_TYPE_VAL (doc_type_cde),
	FOREIGN KEY (tenant_id) REFERENCES TENANT (tenant_id),
	FOREIGN KEY (lock_status_cde) REFERENCES LOCK_STATUS_VAL (lock_status_cde),
	FOREIGN KEY (queue_name_cde,tenant_id) REFERENCES QUEUE_NAME (queue_name_cde,tenant_id),
	FOREIGN KEY (task_status_cde) REFERENCES TASK_STATUS_VAL (task_status_cde)
);

CREATE INDEX ix_document_doc_type_cde ON DOCUMENT (doc_type_cde);
CREATE INDEX ix_document_lock_status_cde ON DOCUMENT (lock_status_cde);
CREATE INDEX ix_document_queue_name_cde ON DOCUMENT (queue_name_cde);
CREATE INDEX ix_document_task_status_cde ON DOCUMENT (task_status_cde);
CREATE INDEX ix_document_tenant_id ON DOCUMENT (tenant_id);

CREATE SEQUENCE sq_action_category_map_id MINVALUE 1000;

CREATE SEQUENCE sq_action_param_attr_map_id MINVALUE 1000;

CREATE TABLE ACTION_PARAM_ATTR_MAPPING
(
	action_param_attr_map_id BIGINT NOT NULL DEFAULT NEXTVAL('sq_action_param_attr_map_id'),
	action_name_cde BIGINT NOT NULL,
	param_name_cde BIGINT NOT NULL,
	attr_name_cde BIGINT NOT NULL,
	attr_name_txt VARCHAR(255),
	tenant_id VARCHAR(50) NOT NULL,
	create_by VARCHAR(50),
	create_dtm TIMESTAMP(3),
	last_mod_by VARCHAR(50),
	last_mod_dtm TIMESTAMP(3),
	PRIMARY KEY (action_param_attr_map_id),
	FOREIGN KEY (action_name_cde,tenant_id) REFERENCES ACTION_NAME (action_name_cde,tenant_id),
	FOREIGN KEY (attr_name_cde,tenant_id) REFERENCES ATTRIBUTE_NAME (attr_name_cde,tenant_id),
	FOREIGN KEY (tenant_id) REFERENCES TENANT (tenant_id),
	FOREIGN KEY (param_name_cde,tenant_id) REFERENCES PARAM_NAME (param_name_cde,tenant_id),
	CONSTRAINT uq_apam_aap_cdes_tenant_id UNIQUE (action_name_cde, attr_name_cde, param_name_cde, tenant_id)
);

CREATE INDEX ix_apam_tenant_id ON ACTION_PARAM_ATTR_MAPPING (tenant_id);

CREATE SEQUENCE sq_app_user_queue_rel_id MINVALUE 1000;

CREATE TABLE APP_USER_QUEUE_REL
(
	app_user_queue_rel_id BIGINT NOT NULL DEFAULT NEXTVAL('sq_app_user_queue_rel_id'),
	app_user_id BIGINT NOT NULL,
	queue_name_cde BIGINT NOT NULL,
	tenant_id VARCHAR(50) NOT NULL,
	end_dtm TIMESTAMP(3),
	hide_after_dtm TIMESTAMP(3),
	create_by VARCHAR(50),
	create_dtm TIMESTAMP(3),
	last_mod_by VARCHAR(50),
	last_mod_dtm TIMESTAMP(3),
	PRIMARY KEY (app_user_queue_rel_id),
	FOREIGN KEY (app_user_id) REFERENCES APP_USER (app_user_id),
	FOREIGN KEY (tenant_id) REFERENCES TENANT (tenant_id),
	FOREIGN KEY (queue_name_cde,tenant_id) REFERENCES QUEUE_NAME (queue_name_cde,tenant_id)
);

CREATE INDEX ix_auqr_tenant_id ON APP_USER_QUEUE_REL (tenant_id);

CREATE SEQUENCE sq_app_user_role_rel_id MINVALUE 1000;

CREATE TABLE APP_USER_ROLE_REL
(
	app_user_role_rel_id BIGINT NOT NULL DEFAULT NEXTVAL('sq_app_user_role_rel_id'),
	app_user_id BIGINT NOT NULL,
	role_type_cde BIGINT NOT NULL,
	tenant_id VARCHAR(50) NOT NULL,
	end_dtm TIMESTAMP(3),
	create_by VARCHAR(50),
	create_dtm TIMESTAMP(3),
	last_mod_by VARCHAR(50),
	last_mod_dtm TIMESTAMP(3),
	PRIMARY KEY (app_user_role_rel_id),
	FOREIGN KEY (app_user_id) REFERENCES APP_USER (app_user_id),
	FOREIGN KEY (tenant_id) REFERENCES TENANT (tenant_id),
	FOREIGN KEY (role_type_cde) REFERENCES ROLE_TYPE_VAL (role_type_cde)
);

CREATE INDEX ix_aurr_tenant_id ON APP_USER_ROLE_REL (tenant_id);

CREATE SEQUENCE sq_doc_app_user_rel_id MINVALUE 1000;

CREATE TABLE DOC_APP_USER_REL
(
	doc_app_user_rel_id BIGINT NOT NULL DEFAULT NEXTVAL('sq_doc_app_user_rel_id'),
	doc_id BIGINT NOT NULL,
	app_user_id BIGINT NOT NULL,
	doc_role_type_cde BIGINT NOT NULL,
	tenant_id VARCHAR(50) NOT NULL,
	end_dtm TIMESTAMP(3),
	create_by VARCHAR(50),
	create_dtm TIMESTAMP(3),
	last_mod_by VARCHAR(50),
	last_mod_dtm TIMESTAMP(3),
	PRIMARY KEY (doc_app_user_rel_id),
	FOREIGN KEY (app_user_id) REFERENCES APP_USER (app_user_id),
	FOREIGN KEY (doc_role_type_cde) REFERENCES DOC_ROLE_TYPE_VAL (doc_role_type_cde),
	FOREIGN KEY (tenant_id) REFERENCES TENANT (tenant_id),
	FOREIGN KEY (doc_id) REFERENCES DOCUMENT (doc_id)
	
);

CREATE INDEX ix_daur_app_user_id ON DOC_APP_USER_REL (app_user_id);
CREATE INDEX ix_daur_doc_id ON DOC_APP_USER_REL (doc_id);
CREATE INDEX ix_daur_tenant_id ON DOC_APP_USER_REL (tenant_id);

CREATE SEQUENCE sq_action_param_attr_rel_id MINVALUE 1000;

CREATE TABLE ACTION_PARAM_ATTR_REL
(
	action_param_attr_rel_id BIGINT NOT NULL DEFAULT NEXTVAL('sq_action_param_attr_rel_id'),
	doc_action_rel_id BIGINT NOT NULL,
	param_name_cde BIGINT NOT NULL,
	attr_name_cde BIGINT NOT NULL,
	attr_name_txt VARCHAR(255),
	param_value VARCHAR(10000),
	tenant_id VARCHAR(50) NOT NULL,
	end_dtm TIMESTAMP(3),
	create_by VARCHAR(50),
	create_dtm TIMESTAMP(3),
	last_mod_by VARCHAR(50),
	last_mod_dtm TIMESTAMP(3),
	PRIMARY KEY (action_param_attr_rel_id),
	FOREIGN KEY (attr_name_cde,tenant_id) REFERENCES ATTRIBUTE_NAME (attr_name_cde,tenant_id),
	FOREIGN KEY (doc_action_rel_id) REFERENCES DOC_ACTION_REL (doc_action_rel_id),
	FOREIGN KEY (tenant_id) REFERENCES TENANT (tenant_id),
	FOREIGN KEY (param_name_cde,tenant_id) REFERENCES PARAM_NAME (param_name_cde,tenant_id)
);

CREATE INDEX ix_apar_attr_name_cde ON ACTION_PARAM_ATTR_REL (attr_name_cde);
CREATE INDEX ix_apar_doc_action_rel_id ON ACTION_PARAM_ATTR_REL (doc_action_rel_id);
CREATE INDEX ix_apar_param_name_cde ON ACTION_PARAM_ATTR_REL (param_name_cde);
CREATE INDEX ix_apar_tenant_id ON ACTION_PARAM_ATTR_REL (tenant_id);

CREATE SEQUENCE sq_attribute_id MINVALUE 1000;

CREATE TABLE ATTRIBUTE
(
	attribute_id BIGINT NOT NULL DEFAULT NEXTVAL('sq_attribute_id'),
	attr_name_cde BIGINT NOT NULL,
	attr_value VARCHAR,
	extract_type_cde BIGINT NOT NULL,
	confidence_pct NUMERIC NOT NULL DEFAULT -1,
	tenant_id VARCHAR(50) NOT NULL,
	create_by VARCHAR(50),
	create_dtm TIMESTAMP(3),
	last_mod_by VARCHAR(50),
	last_mod_dtm TIMESTAMP(3),
	end_dtm TIMESTAMP(3),
	PRIMARY KEY (attribute_id),
	FOREIGN KEY (attr_name_cde,tenant_id) REFERENCES ATTRIBUTE_NAME (attr_name_cde,tenant_id),
	FOREIGN KEY (tenant_id) REFERENCES TENANT (tenant_id),
	FOREIGN KEY (extract_type_cde) REFERENCES EXTRACT_TYPE_VAL (extract_type_cde)
);

CREATE INDEX ix_attr_attr_name_cde ON ATTRIBUTE (attr_name_cde);
CREATE INDEX ix_attr_extract_type_cde ON ATTRIBUTE (extract_type_cde);
CREATE INDEX ix_attr_tenant_id ON ATTRIBUTE (tenant_id);

CREATE SEQUENCE sq_app_var_id MINVALUE 1000;
CREATE TABLE APP_VARIABLE
(
	app_var_id BIGINT NOT NULL DEFAULT NEXTVAL('sq_app_var_id'),
	app_var_key VARCHAR NOT NULL,
	app_var_value VARCHAR,
	tenant_id VARCHAR(50) NOT NULL,
	create_by VARCHAR(50),
	create_dtm TIMESTAMP(3),
	last_mod_by VARCHAR(50),
	last_mod_dtm TIMESTAMP(3),
	end_dtm TIMESTAMP(3),
	PRIMARY KEY (app_var_id),
	FOREIGN KEY (tenant_id) REFERENCES TENANT (tenant_id)
);
CREATE INDEX ix_app_var_key ON APP_VARIABLE (app_var_key);


CREATE SEQUENCE sq_doc_attr_rel_id MINVALUE 1000;

CREATE TABLE DOC_ATTR_REL
(
	doc_attr_rel_id BIGINT NOT NULL DEFAULT NEXTVAL('sq_doc_attr_rel_id'),
	doc_id BIGINT NOT NULL,
	attribute_id BIGINT NOT NULL,
	tenant_id VARCHAR(50) NOT NULL,
	create_by VARCHAR(50),
	create_dtm TIMESTAMP(3),
	last_mod_by VARCHAR(50),
	last_mod_dtm TIMESTAMP(3),
	end_dtm TIMESTAMP(3),
	PRIMARY KEY (doc_attr_rel_id),
	FOREIGN KEY (attribute_id) REFERENCES ATTRIBUTE (attribute_id),
	FOREIGN KEY (doc_id) REFERENCES DOCUMENT (doc_id),
	FOREIGN KEY (tenant_id) REFERENCES TENANT (tenant_id)
);

CREATE INDEX ix_doc_attr_rel_attribute_id ON DOC_ATTR_REL (attribute_id);
CREATE INDEX ix_doc_attr_rel_doc_id ON DOC_ATTR_REL (doc_id);
CREATE INDEX ix_doc_attr_rel_tenant_id ON DOC_ATTR_REL (tenant_id);

CREATE SEQUENCE sq_attachment_id MINVALUE 1000;

CREATE TABLE ATTACHMENT
(
	attachment_id BIGINT NOT NULL DEFAULT NEXTVAL('sq_attachment_id'),
	logical_name VARCHAR(255),
	physical_name VARCHAR(255),
	is_inline_image BOOLEAN DEFAULT FALSE,
	is_primary BOOLEAN NOT NULL,	
	sequence_num bigint NOT NULL,
	tenant_id VARCHAR(50) NOT NULL,
	end_dtm TIMESTAMP(3),
	create_by VARCHAR(50),
	create_dtm TIMESTAMP(3),
	last_mod_by VARCHAR(50),
	last_mod_dtm TIMESTAMP(3),
	extract_type_cde bigint NOT NULL,
	group_name VARCHAR(50) NOT NULL,
	PRIMARY KEY (attachment_id),
	FOREIGN KEY (tenant_id) REFERENCES TENANT (tenant_id),
	FOREIGN KEY (extract_type_cde) REFERENCES EXTRACT_TYPE_VAL (extract_type_cde)
);
CREATE SEQUENCE sq_atta_atta_rel_id MINVALUE 1000;
CREATE TABLE ATTA_ATTA_REL
(
	atta_atta_rel_id BIGINT NOT NULL DEFAULT NEXTVAL('sq_atta_atta_rel_id'),
	attachment_id1 BIGINT NOT NULL,
	attachment_id2 BIGINT NOT NULL,
	tenant_id VARCHAR(50) NOT NULL,
	atta_rel_type_cde BIGINT NOT NULL,
	end_dtm TIMESTAMP(3),
	create_by VARCHAR(50),
	create_dtm TIMESTAMP(3),
	last_mod_by VARCHAR(50),
	last_mod_dtm TIMESTAMP(3),
	PRIMARY KEY (atta_atta_rel_id),
	FOREIGN KEY (attachment_id1) REFERENCES ATTACHMENT (attachment_id),
	FOREIGN KEY (attachment_id2) REFERENCES ATTACHMENT (attachment_id),
	FOREIGN KEY (tenant_id) REFERENCES TENANT (tenant_id)
);
CREATE INDEX ix_attachment_tenant_id ON ATTACHMENT (tenant_id);

CREATE SEQUENCE sq_doc_event_rel_id MINVALUE 1000;

CREATE TABLE DOC_EVENT_REL
(
	doc_event_rel_id BIGINT NOT NULL DEFAULT NEXTVAL('sq_doc_event_rel_id'),
	doc_id BIGINT NOT NULL,
	event_type_cde BIGINT NOT NULL,
	tenant_id VARCHAR(50) NOT NULL,
	event_dtm TIMESTAMP(3),
	create_by VARCHAR(50),
	create_dtm TIMESTAMP(3),
	last_mod_by VARCHAR(50),
	last_mod_dtm TIMESTAMP(3),
	PRIMARY KEY (doc_event_rel_id),
	FOREIGN KEY (tenant_id) REFERENCES TENANT (tenant_id),
	FOREIGN KEY (doc_id) REFERENCES DOCUMENT (doc_id),
	FOREIGN KEY (event_type_cde) REFERENCES EVENT_TYPE_VAL (event_type_cde)
);

CREATE INDEX ix_der_doc_id ON DOC_EVENT_REL (doc_id);
CREATE INDEX ix_der_event_type_cde ON DOC_EVENT_REL (event_type_cde);
CREATE INDEX ix_der_tenant_id ON DOC_EVENT_REL (tenant_id);
CREATE INDEX ix_der_doc_id_tenant_id ON DOC_EVENT_REL (doc_id,tenant_id);

CREATE SEQUENCE sq_email_outbound_id MINVALUE 1000;

CREATE TABLE EMAIL_OUTBOUND
(
	email_outbound_id BIGINT NOT NULL DEFAULT NEXTVAL('sq_email_outbound_id'),
	doc_id BIGINT NOT NULL,
	email_to VARCHAR(2000),
	email_cc VARCHAR(2000),
	email_bcc VARCHAR(2000),
	email_subject VARCHAR(255),
	email_body_text VARCHAR(2000),
	email_body_html VARCHAR,
	tenant_id VARCHAR(50) NOT NULL,
	email_sent_dtm TIMESTAMP(3),
	end_dtm TIMESTAMP(3),
	task_status_cde BIGINT NOT NULL,
	create_by VARCHAR(50),
	create_dtm TIMESTAMP(3),
	last_mod_by VARCHAR(50),
	last_mod_dtm TIMESTAMP(3),
	PRIMARY KEY (email_outbound_id),
	FOREIGN KEY (doc_id) REFERENCES DOCUMENT (doc_id),
	FOREIGN KEY (tenant_id) REFERENCES TENANT (tenant_id),
	FOREIGN KEY (task_status_cde) REFERENCES TASK_STATUS_VAL (task_status_cde)
);

CREATE INDEX ix_eo_doc_id ON EMAIL_OUTBOUND (doc_id);
CREATE INDEX ix_eo_task_status_cde ON EMAIL_OUTBOUND (task_status_cde);
CREATE INDEX ix_eo_tenant_id ON EMAIL_OUTBOUND (tenant_id);

CREATE SEQUENCE sq_email_out_attach_rel_id MINVALUE 1000;

CREATE TABLE EMAIL_OUTBOUND_ATTACH_REL
(
	email_outbound_attach_rel_id BIGINT NOT NULL DEFAULT NEXTVAL('sq_email_out_attach_rel_id'),
	email_outbound_id BIGINT NOT NULL,
	attachment_id BIGINT NOT NULL,
	tenant_id VARCHAR(50) NOT NULL,
	end_dtm TIMESTAMP(3),
	create_by VARCHAR(50),
	create_dtm TIMESTAMP(3),
	last_mod_by VARCHAR(50),
	last_mod_dtm TIMESTAMP(3),
	PRIMARY KEY (email_outbound_attach_rel_id),
	FOREIGN KEY (email_outbound_id) REFERENCES EMAIL_OUTBOUND (email_outbound_id),
	FOREIGN KEY (tenant_id) REFERENCES TENANT (tenant_id),
	FOREIGN KEY (attachment_id) REFERENCES ATTACHMENT (attachment_id)
);

CREATE INDEX ix_eoar_email_outbound_id ON EMAIL_OUTBOUND_ATTACH_REL (email_outbound_id);
CREATE INDEX ix_eoar_attachment_id ON EMAIL_OUTBOUND_ATTACH_REL (attachment_id);
CREATE INDEX ix_eoar_tenant_id ON EMAIL_OUTBOUND_ATTACH_REL (tenant_id);

CREATE SEQUENCE sq_doc_attachment_rel_id MINVALUE 1000;

CREATE TABLE DOC_ATTACHMENT_REL
(
	doc_attachment_rel_id BIGINT NOT NULL DEFAULT NEXTVAL('sq_doc_attachment_rel_id'),
	doc_id BIGINT NOT NULL,
	attachment_id BIGINT NOT NULL,
	tenant_id VARCHAR(50) NOT NULL,
	end_dtm TIMESTAMP(3),
	create_by VARCHAR(50),
	create_dtm TIMESTAMP(3),
	last_mod_by VARCHAR(50),
	last_mod_dtm TIMESTAMP(3),
	PRIMARY KEY (doc_attachment_rel_id),
	FOREIGN KEY (doc_id) REFERENCES DOCUMENT (doc_id),
	FOREIGN KEY (tenant_id) REFERENCES TENANT (tenant_id),
	FOREIGN KEY (attachment_id) REFERENCES ATTACHMENT (attachment_id)
);

CREATE INDEX ix_doc_att_rel_doc_id ON DOC_ATTACHMENT_REL (doc_id);
CREATE INDEX ix_doc_att_rel_attachment_id ON DOC_ATTACHMENT_REL (attachment_id);
CREATE INDEX ix_doc_att_rel_tenant_id ON DOC_ATTACHMENT_REL (tenant_id);

CREATE SEQUENCE sq_app_audit_id MINVALUE 10000;

CREATE TABLE APP_AUDIT
(
	app_audit_id BIGINT NOT NULL DEFAULT NEXTVAL('sq_app_audit_id'),
	entity_name VARCHAR(255),
	entity_value VARCHAR(255),
	audit_login_id VARCHAR(255),
	audit_message VARCHAR,
	current_value VARCHAR,
	previous_value VARCHAR,
	tenant_id VARCHAR(50) NOT NULL,
	audit_event_dtm TIMESTAMP(3),
	create_by VARCHAR(50),
	create_dtm TIMESTAMP(3),
	last_mod_by VARCHAR(50),
	last_mod_dtm TIMESTAMP(3),
	PRIMARY KEY (app_audit_id),
	FOREIGN KEY (tenant_id) REFERENCES TENANT (tenant_id)
);

CREATE INDEX ix_audit_tenant_id ON APP_AUDIT (tenant_id);

CREATE SEQUENCE sq_user_audit_rel_id MINVALUE 10000;

CREATE TABLE USER_AUDIT_REL
(
	user_audit_rel_id BIGINT NOT NULL DEFAULT NEXTVAL('sq_user_audit_rel_id'),
	app_audit_id BIGINT NOT NULL,
	app_user_id BIGINT NOT NULL,
	tenant_id VARCHAR(50) NOT NULL,
	create_by VARCHAR(50),
	create_dtm TIMESTAMP(3),
	last_mod_by VARCHAR(50),
	last_mod_dtm TIMESTAMP(3),
	PRIMARY KEY (user_audit_rel_id),
	FOREIGN KEY (app_audit_id) REFERENCES APP_AUDIT (app_audit_id),
	FOREIGN KEY (tenant_id) REFERENCES TENANT (tenant_id),
	FOREIGN KEY (app_user_id) REFERENCES APP_USER (app_user_id)
);

CREATE INDEX ix_user_audit_rel_tenant_id ON USER_AUDIT_REL (tenant_id);

CREATE SEQUENCE sq_doc_audit_rel_id MINVALUE 10000;

CREATE TABLE DOC_AUDIT_REL
(
	doc_audit_rel_id BIGINT NOT NULL DEFAULT NEXTVAL('sq_doc_audit_rel_id'),
	app_audit_id BIGINT NOT NULL,
	doc_id BIGINT NOT NULL,
	tenant_id VARCHAR(50) NOT NULL,
	create_by VARCHAR(50),
	create_dtm TIMESTAMP(3),
	last_mod_by VARCHAR(50),
	last_mod_dtm TIMESTAMP(3),
	PRIMARY KEY (doc_audit_rel_id),
	FOREIGN KEY (app_audit_id) REFERENCES APP_AUDIT (app_audit_id),
	FOREIGN KEY (tenant_id) REFERENCES TENANT (tenant_id),
	FOREIGN KEY (doc_id) REFERENCES DOCUMENT (doc_id)
);

CREATE INDEX ix_doc_audit_rel_tenant_id ON DOC_AUDIT_REL (tenant_id);

CREATE SEQUENCE sq_attr_name_value_map_id MINVALUE 1000;

CREATE TABLE ATTR_NAME_VALUE_MAPPING
(
	attr_name_value_map_id BIGINT NOT NULL DEFAULT NEXTVAL('sq_attr_name_value_map_id'),
	attr_name_cde BIGINT NOT NULL,
	attr_name_value VARCHAR(255),
	tenant_id VARCHAR(50) NOT NULL,
	sequence_num bigint NOT NULL,
	end_dtm timestamp(3),
	create_by VARCHAR(50),
	create_dtm TIMESTAMP(3),
	last_mod_by VARCHAR(50),
	last_mod_dtm TIMESTAMP(3),
	PRIMARY KEY (attr_name_value_map_id),
	FOREIGN KEY (attr_name_cde,tenant_id) REFERENCES ATTRIBUTE_NAME (attr_name_cde,tenant_id),
	FOREIGN KEY (tenant_id) REFERENCES TENANT (tenant_id)
);

CREATE INDEX ix_anvm_tenant_id ON ATTR_NAME_VALUE_MAPPING (tenant_id);

CREATE SEQUENCE sq_attachment_attr_rel_id MINVALUE 1000;

CREATE TABLE ATTACHMENT_ATTR_REL
(
	attachment_attr_rel_id BIGINT NOT NULL DEFAULT NEXTVAL('sq_attachment_attr_rel_id'),
	attachment_id BIGINT NOT NULL,
	attribute_id BIGINT NOT NULL,
	tenant_id VARCHAR(50) NOT NULL,
	create_by VARCHAR(50),
	create_dtm TIMESTAMP(3),
	last_mod_by VARCHAR(50),
	last_mod_dtm TIMESTAMP(3),
	end_dtm TIMESTAMP(3),
	PRIMARY KEY (attachment_attr_rel_id),
	FOREIGN KEY (attribute_id) REFERENCES ATTRIBUTE (attribute_id),
	FOREIGN KEY (attachment_id) REFERENCES ATTACHMENT (attachment_id),
	FOREIGN KEY (tenant_id) REFERENCES TENANT (tenant_id)
);

CREATE INDEX ix_aar_attribute_id ON ATTACHMENT_ATTR_REL (attribute_id);
CREATE INDEX ix_aar_doc_id ON ATTACHMENT_ATTR_REL (attachment_id);
CREATE INDEX ix_aar_tenant_id ON ATTACHMENT_ATTR_REL (tenant_id);

set search_path to docwbmain;

CREATE SEQUENCE sq_attr_src_id MINVALUE 1000;

CREATE TABLE ATTRIBUTE_SOURCE
(
	attr_src_id BIGINT NOT NULL DEFAULT NEXTVAL('sq_attr_src_id'),
	doc_id BIGINT NOT NULL,
	record jsonb,
	tenant_id VARCHAR(50) NOT NULL,
	create_by VARCHAR(50),
	create_dtm TIMESTAMP(3),
	last_mod_by VARCHAR(50),
	last_mod_dtm TIMESTAMP(3),
	end_dtm TIMESTAMP(3),
	PRIMARY KEY (attr_src_id),
	FOREIGN KEY (doc_id) REFERENCES DOCUMENT (doc_id),
	FOREIGN KEY (tenant_id) REFERENCES TENANT (tenant_id)
);

CREATE INDEX ix_as_doc_id ON ATTRIBUTE_SOURCE (doc_id);
CREATE INDEX ix_as_tenant_id ON ATTRIBUTE_SOURCE (tenant_id);

CREATE FUNCTION FN_CAPTURE_UPDATER()
RETURNS TRIGGER AS
$$
DECLARE
    pkey_col_name varchar := '';
    where_block varchar := '';
    where_condition varchar := '';
    sql_block varchar := '';
    cursor_pkey CURSOR (table_name VARCHAR) FOR 
    SELECT a.attname
    FROM pg_index i JOIN pg_attribute a
        ON a.attrelid = i.indrelid
        AND a.attnum = ANY(i.indkey)
    WHERE i.indrelid = table_name::regclass 
        AND i.indisprimary;
    cols_to_update varchar;
    audit_user_name varchar;
    application_name varchar;
BEGIN
    OPEN cursor_pkey(TG_TABLE_NAME);
    LOOP
        FETCH cursor_pkey INTO pkey_col_name;
        EXIT WHEN NOT FOUND;
        where_condition = pkey_col_name || ' = $3.' || pkey_col_name; 
        IF where_block = '' THEN
            where_block = where_condition;
        ELSE 
            where_block = where_block || ' AND ' || where_condition;
        END IF;
    END LOOP;
    CLOSE cursor_pkey;

    SELECT current_setting('application_name') into application_name;

    -- For connections made by user (using IDE etc.), capture logged in user name
    audit_user_name = current_user;

    -- For connections made by application, capture user name from SQL query
    -- as this represents real end user
    IF application_name IS NOT NULL AND LENGTH(TRIM(application_name))>0 THEN
        IF TG_OP = 'INSERT' AND NEW.CREATE_BY IS NOT NULL THEN
            audit_user_name = NEW.CREATE_BY;
        ELSEIF TG_OP = 'UPDATE' AND NEW.LAST_MOD_BY IS NOT NULL THEN
            audit_user_name = NEW.LAST_MOD_BY;  
        END IF;
    END IF;

    IF TG_OP IN ('INSERT','UPDATE') THEN
        IF TG_OP = 'INSERT' THEN
            cols_to_update = 'CREATE_BY = $1, CREATE_DTM = $2';
        ELSEIF TG_OP = 'UPDATE' THEN
            cols_to_update = 'LAST_MOD_BY = $1, LAST_MOD_DTM = $2';
        END IF;
        sql_block = 'UPDATE ' || TG_TABLE_NAME || ' SET ' || cols_to_update || ' WHERE ' || where_block || ';';
        --RAISE notice 'TRIGGER SQL: %', sql_block;
        EXECUTE sql_block USING audit_user_name, current_timestamp, NEW;
    END IF;
    RETURN NULL;
END;
$$
LANGUAGE plpgsql;

CREATE TRIGGER TG_TENANT
  AFTER INSERT OR UPDATE ON TENANT
  FOR EACH ROW
  WHEN ((pg_trigger_depth() = 0))
  EXECUTE PROCEDURE FN_CAPTURE_UPDATER();

CREATE TRIGGER TG_ACTION_NAME
	AFTER INSERT OR UPDATE ON ACTION_NAME
	FOR EACH ROW
	WHEN(pg_trigger_depth() = 0)
	EXECUTE PROCEDURE FN_CAPTURE_UPDATER();

CREATE TRIGGER TG_APP_USER
	AFTER INSERT OR UPDATE ON APP_USER
	FOR EACH ROW
	WHEN(pg_trigger_depth() = 0)
	EXECUTE PROCEDURE FN_CAPTURE_UPDATER();

CREATE TRIGGER TG_ATTRIBUTE_NAME
	AFTER INSERT OR UPDATE ON ATTRIBUTE_NAME
	FOR EACH ROW
	WHEN(pg_trigger_depth() = 0)
	EXECUTE PROCEDURE FN_CAPTURE_UPDATER();

CREATE TRIGGER TG_DOC_ACTION_REL
	AFTER INSERT OR UPDATE ON DOC_ACTION_REL
	FOR EACH ROW
	WHEN(pg_trigger_depth() = 0)
	EXECUTE PROCEDURE FN_CAPTURE_UPDATER();

CREATE TRIGGER TG_DOCUMENT_TYPE_VAL
	AFTER INSERT OR UPDATE ON DOCUMENT_TYPE_VAL
	FOR EACH ROW
	WHEN(pg_trigger_depth() = 0)
	EXECUTE PROCEDURE FN_CAPTURE_UPDATER();

CREATE TRIGGER TG_DOCUMENT
	AFTER INSERT OR UPDATE ON DOCUMENT
	FOR EACH ROW
	WHEN(pg_trigger_depth() = 0)
	EXECUTE PROCEDURE FN_CAPTURE_UPDATER();

CREATE TRIGGER TG_EVENT_TYPE_VAL
	AFTER INSERT OR UPDATE ON EVENT_TYPE_VAL
	FOR EACH ROW
	WHEN(pg_trigger_depth() = 0)
	EXECUTE PROCEDURE FN_CAPTURE_UPDATER();

CREATE TRIGGER TG_EXTRACT_TYPE_VAL
	AFTER INSERT OR UPDATE ON EXTRACT_TYPE_VAL
	FOR EACH ROW
	WHEN(pg_trigger_depth() = 0)
	EXECUTE PROCEDURE FN_CAPTURE_UPDATER();

CREATE TRIGGER TG_LOCK_STATUS_VAL
	AFTER INSERT OR UPDATE ON LOCK_STATUS_VAL
	FOR EACH ROW
	WHEN(pg_trigger_depth() = 0)
	EXECUTE PROCEDURE FN_CAPTURE_UPDATER();

CREATE TRIGGER TG_PARAM_NAME
	AFTER INSERT OR UPDATE ON PARAM_NAME
	FOR EACH ROW
	WHEN(pg_trigger_depth() = 0)
	EXECUTE PROCEDURE FN_CAPTURE_UPDATER();

CREATE TRIGGER TG_QUEUE_NAME
	AFTER INSERT OR UPDATE ON QUEUE_NAME
	FOR EACH ROW
	WHEN(pg_trigger_depth() = 0)
	EXECUTE PROCEDURE FN_CAPTURE_UPDATER();

CREATE TRIGGER TG_ROLE_TYPE_VAL
	AFTER INSERT OR UPDATE ON ROLE_TYPE_VAL
	FOR EACH ROW
	WHEN(pg_trigger_depth() = 0)
	EXECUTE PROCEDURE FN_CAPTURE_UPDATER();

CREATE TRIGGER TG_DOC_ROLE_TYPE_VAL
	AFTER INSERT OR UPDATE ON DOC_ROLE_TYPE_VAL
	FOR EACH ROW
	WHEN(pg_trigger_depth() = 0)
	EXECUTE PROCEDURE FN_CAPTURE_UPDATER();

CREATE TRIGGER TG_TASK_STATUS_VAL
	AFTER INSERT OR UPDATE ON TASK_STATUS_VAL
	FOR EACH ROW
	WHEN(pg_trigger_depth() = 0)
	EXECUTE PROCEDURE FN_CAPTURE_UPDATER();

CREATE TRIGGER TG_TASK_TYPE_VAL
	AFTER INSERT OR UPDATE ON TASK_TYPE_VAL
	FOR EACH ROW
	WHEN(pg_trigger_depth() = 0)
	EXECUTE PROCEDURE FN_CAPTURE_UPDATER();

CREATE TRIGGER TG_USER_TYPE_VAL
	AFTER INSERT OR UPDATE ON USER_TYPE_VAL
	FOR EACH ROW
	WHEN(pg_trigger_depth() = 0)
	EXECUTE PROCEDURE FN_CAPTURE_UPDATER();

CREATE TRIGGER TG_ACTION_PARAM_ATTR_MAPPING
	AFTER INSERT OR UPDATE ON ACTION_PARAM_ATTR_MAPPING
	FOR EACH ROW
	WHEN(pg_trigger_depth() = 0)
	EXECUTE PROCEDURE FN_CAPTURE_UPDATER();

CREATE TRIGGER TG_APP_USER_QUEUE_REL
	AFTER INSERT OR UPDATE ON APP_USER_QUEUE_REL
	FOR EACH ROW
	WHEN(pg_trigger_depth() = 0)
	EXECUTE PROCEDURE FN_CAPTURE_UPDATER();
	
CREATE TRIGGER TG_APP_USER_ROLE_REL
	AFTER INSERT OR UPDATE ON APP_USER_ROLE_REL
	FOR EACH ROW
	WHEN(pg_trigger_depth() = 0)
	EXECUTE PROCEDURE FN_CAPTURE_UPDATER();
	
CREATE TRIGGER TG_DOC_APP_USER_REL
	AFTER INSERT OR UPDATE ON DOC_APP_USER_REL
	FOR EACH ROW
	WHEN(pg_trigger_depth() = 0)
	EXECUTE PROCEDURE FN_CAPTURE_UPDATER();

CREATE TRIGGER TG_ACTION_PARAM_ATTR_REL
	AFTER INSERT OR UPDATE ON ACTION_PARAM_ATTR_REL
	FOR EACH ROW
	WHEN(pg_trigger_depth() = 0)
	EXECUTE PROCEDURE FN_CAPTURE_UPDATER();

CREATE TRIGGER TG_DOC_ATTR_REL
	AFTER INSERT OR UPDATE ON DOC_ATTR_REL
	FOR EACH ROW
	WHEN(pg_trigger_depth() = 0)
	EXECUTE PROCEDURE FN_CAPTURE_UPDATER();

CREATE TRIGGER TG_ATTACHMENT
	AFTER INSERT OR UPDATE ON ATTACHMENT
	FOR EACH ROW
	WHEN(pg_trigger_depth() = 0)
	EXECUTE PROCEDURE FN_CAPTURE_UPDATER();

CREATE TRIGGER TG_DOC_EVENT_REL
	AFTER INSERT OR UPDATE ON DOC_EVENT_REL
	FOR EACH ROW
	WHEN(pg_trigger_depth() = 0)
	EXECUTE PROCEDURE FN_CAPTURE_UPDATER();

CREATE TRIGGER TG_EMAIL_OUTBOUND
	AFTER INSERT OR UPDATE ON EMAIL_OUTBOUND
	FOR EACH ROW
	WHEN(pg_trigger_depth() = 0)
	EXECUTE PROCEDURE FN_CAPTURE_UPDATER();
	
CREATE TRIGGER TG_EMAIL_OUTBOUND_ATTACH_REL
	AFTER INSERT OR UPDATE ON EMAIL_OUTBOUND_ATTACH_REL
	FOR EACH ROW
	WHEN(pg_trigger_depth() = 0)
	EXECUTE PROCEDURE FN_CAPTURE_UPDATER();

CREATE TRIGGER TG_DOC_ATTACHMENT_REL
	AFTER INSERT OR UPDATE ON DOC_ATTACHMENT_REL
	FOR EACH ROW
	WHEN(pg_trigger_depth() = 0)
	EXECUTE PROCEDURE FN_CAPTURE_UPDATER();

CREATE TRIGGER TG_APP_AUDIT
	AFTER INSERT OR UPDATE ON APP_AUDIT
	FOR EACH ROW
	WHEN(pg_trigger_depth() = 0)
	EXECUTE PROCEDURE FN_CAPTURE_UPDATER();
	
CREATE TRIGGER TG_USER_AUDIT_REL
	AFTER INSERT OR UPDATE ON USER_AUDIT_REL
	FOR EACH ROW
	WHEN(pg_trigger_depth() = 0)
	EXECUTE PROCEDURE FN_CAPTURE_UPDATER();
	
CREATE TRIGGER TG_DOC_AUDIT_REL
	AFTER INSERT OR UPDATE ON DOC_AUDIT_REL
	FOR EACH ROW
	WHEN(pg_trigger_depth() = 0)
	EXECUTE PROCEDURE FN_CAPTURE_UPDATER();
	
CREATE TRIGGER TG_ATTR_NAME_VALUE_MAPPING
	AFTER INSERT OR UPDATE ON ATTR_NAME_VALUE_MAPPING
	FOR EACH ROW
	WHEN(pg_trigger_depth() = 0)
	EXECUTE PROCEDURE FN_CAPTURE_UPDATER();

CREATE TRIGGER TG_ATTRIBUTE
	AFTER INSERT OR UPDATE ON ATTRIBUTE
	FOR EACH ROW
	WHEN(pg_trigger_depth() = 0)
	EXECUTE PROCEDURE FN_CAPTURE_UPDATER();

CREATE TRIGGER TG_APP_VARIABLE
	AFTER INSERT OR UPDATE ON APP_VARIABLE
	FOR EACH ROW
	WHEN(pg_trigger_depth() = 0)
	EXECUTE PROCEDURE FN_CAPTURE_UPDATER();
	
CREATE TRIGGER TG_ATTACHMENT_ATTR_REL
	AFTER INSERT OR UPDATE ON ATTACHMENT_ATTR_REL
	FOR EACH ROW
	WHEN(pg_trigger_depth() = 0)
	EXECUTE PROCEDURE FN_CAPTURE_UPDATER();

CREATE TRIGGER TG_ATTRIBUTE_SOURCE
	AFTER INSERT OR UPDATE ON ATTRIBUTE_SOURCE
	FOR EACH ROW
	WHEN(pg_trigger_depth() = 0)
	EXECUTE PROCEDURE FN_CAPTURE_UPDATER();
	
CREATE TRIGGER TG_ATTA_ATTA_REL
	AFTER INSERT OR UPDATE ON ATTA_ATTA_REL
	FOR EACH ROW
	WHEN(pg_trigger_depth() = 0)
	EXECUTE PROCEDURE FN_CAPTURE_UPDATER();