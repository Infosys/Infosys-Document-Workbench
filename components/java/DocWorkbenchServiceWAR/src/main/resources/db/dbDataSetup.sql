/**** Document Workbench Core ****/

SET search_path TO docwbmain;

INSERT INTO document_type_val (doc_type_cde, doc_type_txt) VALUES (1, 'Email');
INSERT INTO document_type_val (doc_type_cde, doc_type_txt) VALUES (2, 'File');

INSERT INTO extract_type_val (extract_type_cde, extract_type_txt) VALUES (1, 'Direct Copy');
INSERT INTO extract_type_val (extract_type_cde, extract_type_txt) VALUES (2, 'Custom Logic');
INSERT INTO extract_type_val (extract_type_cde, extract_type_txt) VALUES (3, 'Manually Corrected');

INSERT INTO lock_status_val (lock_status_cde, lock_status_txt) VALUES (1, 'Unlocked');
INSERT INTO lock_status_val (lock_status_cde, lock_status_txt) VALUES (2, 'Locked');

INSERT INTO task_status_val (task_status_cde,task_status_txt) VALUES (50, 'Undefined');
INSERT INTO task_status_val (task_status_cde,task_status_txt) VALUES (100, 'Yet to Start');
INSERT INTO task_status_val (task_status_cde,task_status_txt) VALUES (200, 'In-Progress');
INSERT INTO task_status_val (task_status_cde,task_status_txt) VALUES (300, 'On-Hold');
INSERT INTO task_status_val (task_status_cde,task_status_txt) VALUES (400, 'For Your Review');
INSERT INTO task_status_val (task_status_cde,task_status_txt) VALUES (450, 'For Your Rework');
INSERT INTO task_status_val (task_status_cde,task_status_txt) VALUES (500, 'Retry Later');
INSERT INTO task_status_val (task_status_cde,task_status_txt) VALUES (900, 'Complete');
INSERT INTO task_status_val (task_status_cde,task_status_txt) VALUES (901, 'Failed');

INSERT INTO task_type_val (task_type_cde, task_type_txt) VALUES (1, 'system');
INSERT INTO task_type_val (task_type_cde, task_type_txt) VALUES (2, 'user');

--INSERT INTO role_type_val (role_type_cde, role_type_txt) VALUES (1, 'ROLE_ADMIN');
--INSERT INTO role_type_val (role_type_cde, role_type_txt) VALUES (2, 'ROLE_AGENT');
--INSERT INTO role_type_val (role_type_cde, role_type_txt) VALUES (3, 'ROLE_MANAGER');
INSERT INTO role_type_val (role_type_cde, role_type_txt) VALUES (101, 'ADMIN');
INSERT INTO role_type_val (role_type_cde, role_type_txt) VALUES (102, 'AGENT');
INSERT INTO role_type_val (role_type_cde, role_type_txt) VALUES (103, 'MANAGER');
INSERT INTO role_type_val (role_type_cde, role_type_txt) VALUES (104, 'GUEST');
INSERT INTO role_type_val (role_type_cde, role_type_txt) VALUES (105, 'SERVICE');

INSERT INTO doc_role_type_val (doc_role_type_cde, doc_role_type_txt) VALUES (1, 'CaseOwner');
INSERT INTO doc_role_type_val (doc_role_type_cde, doc_role_type_txt) VALUES (2, 'CaseReviewer');

INSERT INTO event_type_val (event_type_cde, event_type_txt) VALUES (100, 'Document Created');
INSERT INTO event_type_val (event_type_cde, event_type_txt) VALUES (150, 'Attributes Extracted - Pending');
INSERT INTO event_type_val (event_type_cde, event_type_txt) VALUES (200, 'Attributes Extracted');
INSERT INTO event_type_val (event_type_cde, event_type_txt) VALUES (300, 'Case Opened');
INSERT INTO event_type_val (event_type_cde, event_type_txt) VALUES (400, 'Case Assigned');
INSERT INTO event_type_val (event_type_cde, event_type_txt) VALUES (500, 'Action Created');
INSERT INTO event_type_val (event_type_cde, event_type_txt) VALUES (600, 'Action Completed');
INSERT INTO event_type_val (event_type_cde, event_type_txt) VALUES (700, 'Email Sent');
INSERT INTO event_type_val (event_type_cde, event_type_txt) VALUES (800, 'Case Closed');

INSERT INTO USER_TYPE_VAL(user_type_cde, user_type_txt) values (1, 'User');
INSERT INTO USER_TYPE_VAL(user_type_cde, user_type_txt) values (2, 'Service');
INSERT INTO USER_TYPE_VAL(user_type_cde, user_type_txt) values (3, 'Bot');