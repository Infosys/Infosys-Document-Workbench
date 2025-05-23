SET search_path TO workflow;

CREATE TABLE EXECUTOR_TYPE_VAL
(
	executor_type_cde BIGINT NOT NULL,
	executor_type_txt VARCHAR(255),
	create_by VARCHAR(50),
	create_dtm TIMESTAMP(3),
	last_mod_by VARCHAR(50),
	last_mod_dtm TIMESTAMP(3),
	PRIMARY KEY (executor_type_cde)
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

CREATE SEQUENCE sq_execution_id MINVALUE 1000000000;

CREATE TABLE EXECUTION
(
	execution_id BIGINT NOT NULL DEFAULT NEXTVAL('sq_execution_id'),
	executor_type_cde BIGINT NOT NULL,
	execution_title VARCHAR(255),
	start_dtm TIMESTAMP(3),
	end_dtm TIMESTAMP(3),
	run_duration_secs NUMERIC NOT NULL DEFAULT -1,
	client VARCHAR(50) NOT NULL,
	tenant_id VARCHAR(50) NOT NULL,
	create_by VARCHAR(50),
	create_dtm TIMESTAMP(3),
	last_mod_by VARCHAR(50),
	last_mod_dtm TIMESTAMP(3),
	PRIMARY KEY (execution_id),
	FOREIGN KEY (executor_type_cde) REFERENCES EXECUTOR_TYPE_VAL (executor_type_cde)
);

CREATE INDEX ix_exec_executor_type_cde ON EXECUTION (executor_type_cde);

CREATE SEQUENCE sq_exec_event_rel_id MINVALUE 1000000000;

CREATE TABLE EXECUTION_EVENT_REL 
(
	exec_event_rel_id BIGINT NOT NULL DEFAULT NEXTVAL('sq_exec_event_rel_id'),
	execution_id BIGINT NOT NULL,
	event_type_cde BIGINT NOT NULL,
	event_dtm TIMESTAMP(3),
	event_msg VARCHAR(255),
	create_by VARCHAR(50),
	create_dtm TIMESTAMP(3),
	last_mod_by VARCHAR(50),
	last_mod_dtm TIMESTAMP(3),
	PRIMARY KEY (exec_event_rel_id),
	FOREIGN KEY (execution_id) REFERENCES EXECUTION (execution_id),
	FOREIGN KEY (event_type_cde) REFERENCES EVENT_TYPE_VAL (event_type_cde)
);

CREATE INDEX ix_eer_execution_id ON EXECUTION_EVENT_REL (execution_id);
CREATE INDEX ix_eer_event_type_cde ON EXECUTION_EVENT_REL (event_type_cde);

CREATE TABLE STATUS_TYPE_VAL
(
	status_type_cde BIGINT NOT NULL,
	status_type_txt VARCHAR(255),
	create_by VARCHAR(50),
	create_dtm TIMESTAMP(3),
	last_mod_by VARCHAR(50),
	last_mod_dtm TIMESTAMP(3),
	PRIMARY KEY (status_type_cde)
);

CREATE SEQUENCE sq_transaction_id MINVALUE 1000000000;

CREATE TABLE TRANSACTION
(
	transaction_id BIGINT NOT NULL DEFAULT NEXTVAL('sq_transaction_id'),
	transaction_ext_id VARCHAR(255) NOT NULL,
	status_type_cde bigint NOT NULL,
	transaction_ext_start_dtm TIMESTAMP(3),
	transaction_ext_end_dtm TIMESTAMP(3),
	key_name VARCHAR(255) NOT NULL,
	key_value VARCHAR(255) NOT NULL,
	transaction_ext_status_txt VARCHAR(255),
	transaction_ext_message VARCHAR(255),
	end_dtm TIMESTAMP(3),
	create_by VARCHAR(50),
	create_dtm TIMESTAMP(3),
	last_mod_by VARCHAR(50),
	last_mod_dtm TIMESTAMP(3),
	PRIMARY KEY (transaction_id),
	FOREIGN KEY (status_type_cde) REFERENCES STATUS_TYPE_VAL (status_type_cde)
);

CREATE INDEX ix_trans_transaction_ext_id ON TRANSACTION (transaction_ext_id);

CREATE SEQUENCE sq_key_val_map_id MINVALUE 1000; 

CREATE TABLE KEY_VAL_MAP
(
	key_val_map_id BIGINT NOT NULL DEFAULT NEXTVAL('sq_key_val_map_id'),
	key VARCHAR(100) NOT NULL,
	value VARCHAR(255) NOT NULL,
	end_dtm TIMESTAMP(3),
	create_by VARCHAR(50),
	create_dtm TIMESTAMP(3),
	last_mod_by VARCHAR(50),
	last_mod_dtm TIMESTAMP(3),
	PRIMARY KEY (key_val_map_id),
	UNIQUE(key)
);


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

CREATE TRIGGER TG_EXECUTOR_TYPE_VAL
	AFTER INSERT OR UPDATE ON EXECUTOR_TYPE_VAL
	FOR EACH ROW
	WHEN(pg_trigger_depth() = 0)
	EXECUTE PROCEDURE FN_CAPTURE_UPDATER();
	
CREATE TRIGGER TG_EVENT_TYPE_VAL
	AFTER INSERT OR UPDATE ON EVENT_TYPE_VAL
	FOR EACH ROW
	WHEN(pg_trigger_depth() = 0)
	EXECUTE PROCEDURE FN_CAPTURE_UPDATER();
	
CREATE TRIGGER TG_EXECUTION
	AFTER INSERT OR UPDATE ON EXECUTION
	FOR EACH ROW
	WHEN(pg_trigger_depth() = 0)
	EXECUTE PROCEDURE FN_CAPTURE_UPDATER();	

CREATE TRIGGER TG_EXECUTION_EVENT_REL
	AFTER INSERT OR UPDATE ON EXECUTION_EVENT_REL
	FOR EACH ROW
	WHEN(pg_trigger_depth() = 0)
	EXECUTE PROCEDURE FN_CAPTURE_UPDATER();	
	
CREATE TRIGGER TG_STATUS_TYPE_VAL
	AFTER INSERT OR UPDATE ON STATUS_TYPE_VAL
	FOR EACH ROW
	WHEN(pg_trigger_depth() = 0)
	EXECUTE PROCEDURE FN_CAPTURE_UPDATER();
	
CREATE TRIGGER TG_TRANSACTION
	AFTER INSERT OR UPDATE ON TRANSACTION
	FOR EACH ROW
	WHEN(pg_trigger_depth() = 0)
	EXECUTE PROCEDURE FN_CAPTURE_UPDATER();
	
CREATE TRIGGER TG_KEY_VAL_MAP
	AFTER INSERT OR UPDATE ON KEY_VAL_MAP
	FOR EACH ROW
	WHEN(pg_trigger_depth() = 0)
	EXECUTE PROCEDURE FN_CAPTURE_UPDATER();
	
	