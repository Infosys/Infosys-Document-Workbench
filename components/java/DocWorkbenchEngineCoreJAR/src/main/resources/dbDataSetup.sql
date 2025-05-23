/**** Document Workbench Core ****/

SET search_path TO workflow;

INSERT INTO EXECUTOR_TYPE_VAL (executor_type_cde, executor_type_txt) VALUES (1, 'Document Downloader');
INSERT INTO EXECUTOR_TYPE_VAL (executor_type_cde, executor_type_txt) VALUES (2, 'Attribute Extractor');
INSERT INTO EXECUTOR_TYPE_VAL (executor_type_cde, executor_type_txt) VALUES (3, 'Case Opener');
INSERT INTO EXECUTOR_TYPE_VAL (executor_type_cde, executor_type_txt) VALUES (4, 'Action Executor');
INSERT INTO EXECUTOR_TYPE_VAL (executor_type_cde, executor_type_txt) VALUES (5, 'Outbound Email Sender');
INSERT INTO EXECUTOR_TYPE_VAL (executor_type_cde, executor_type_txt) VALUES (6, 'Action Script Executor');
INSERT INTO EXECUTOR_TYPE_VAL (executor_type_cde, executor_type_txt) VALUES (7, 'Action Script Result Updater');
INSERT INTO EXECUTOR_TYPE_VAL (executor_type_cde, executor_type_txt) VALUES (8, 'Re Extract Data Action Executor');

INSERT INTO EVENT_TYPE_VAL (event_type_cde, event_type_txt) VALUES (100, 'Work Started');
INSERT INTO EVENT_TYPE_VAL (event_type_cde, event_type_txt) VALUES (400, 'Online Jobs Triggered');
INSERT INTO EVENT_TYPE_VAL (event_type_cde, event_type_txt) VALUES (500, 'Online Jobs Completed');
INSERT INTO EVENT_TYPE_VAL (event_type_cde, event_type_txt) VALUES (600, 'Batch Jobs Triggered');
INSERT INTO EVENT_TYPE_VAL (event_type_cde, event_type_txt) VALUES (700, 'Batch Jobs Completed');
INSERT INTO EVENT_TYPE_VAL (event_type_cde, event_type_txt) VALUES (800, 'Work Completed');

INSERT INTO STATUS_TYPE_VAL (status_type_cde, status_type_txt) VALUES (1,'QUEUED');
INSERT INTO STATUS_TYPE_VAL (status_type_cde, status_type_txt) VALUES (2,'SUCCESS');
INSERT INTO STATUS_TYPE_VAL (status_type_cde, status_type_txt) VALUES (3,'FAILED');
