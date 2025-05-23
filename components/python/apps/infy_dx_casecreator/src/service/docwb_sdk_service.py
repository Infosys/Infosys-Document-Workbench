# ===============================================================================================================#
# Copyright 2025 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

import uuid

from common.logger_factory import LoggerFactory
from infy_docwb_sdk import (annotation_service, attachment_service,
                            attribute_service, audit_service, auth_service,
                            document_service, queue_service, user_service)
from infy_docwb_sdk.data import (annotation_data, attachment_data,
                                 attribute_data, audit_data, document_data)


from common.logger_factory import LoggerFactory
logger = LoggerFactory().get_logger()


class DocwbSdkService():
    EnumEventType = document_service.EnumEventType
    EnumTaskStatusType = document_service.EnumTaskStatusType

    def __init__(self, config_params_dict):
        self.__end_point = config_params_dict["end_point"]
        self.__username = config_params_dict["username"]
        self.__password = config_params_dict["password"]
        self.__tenant_id = config_params_dict["tenant_id"]
        self.__doc_type_cde = config_params_dict["doc_type_cde"]
        self.__queue_list = []
        self.__authenticate()

    def __authenticate(self):
        auth_obj = auth_service.AuthService(logger=logger)
        auth_token = auth_obj.get_auth_token(
            self.__end_point, self.__username, self.__password, self.__tenant_id)
        if len(auth_token) == 0:
            raise Exception("Authentication failed!!")
        logger.debug("Authentication successful")
        self.__document_service = document_service.DocumentService(
            auth_token, self.__end_point, logger=logger)
        self.__attribute_service = attribute_service.AttributeService(
            auth_token, self.__end_point, logger=logger)
        self.__attachment_service = attachment_service.AttachmentService(
            auth_token, self.__end_point, logger=logger)
        self.__annotation_service = annotation_service.AnnotationService(
            auth_token, self.__end_point, logger=logger)
        self.__audit_service = audit_service.AuditService(
            auth_token, self.__end_point, logger=logger)
        self.__queue_service = queue_service.QueueService(
            auth_token, self.__end_point, logger=logger)
        self.__user_service = user_service.UserService(
            auth_token, self.__end_point, logger=logger)

    def create_case(self, queue_name_cde, attributes):
        document_data_obj = document_data.DocumentData()
        document_data_obj.set_attributes(attributes)
        document_data_obj.set_doc_location(str(uuid.uuid1()))
        document_data_obj.set_doc_type_cde(self.__doc_type_cde)
        document_data_obj.set_lock_status_cde(1)
        document_data_obj.set_queue_name_cde(queue_name_cde)
        doc_id = self.__document_service.add_document_with_attributes(
            document_data_obj)
        logger.debug(f"Document created {doc_id}")
        # Auto insert the following events
        self.insert_doc_event(doc_id, self.EnumEventType.DOCUMENT_CREATED)
        self.insert_doc_event(
            doc_id, self.EnumEventType.ATTRIBUTES_EXTRACTED_PENDING)
        return doc_id

    def create_attachment(self, doc_id, attachments, is_primary, group_name=None):
        # add attachment call
        document_data_obj = document_data.DocumentData()
        document_data_obj.set_doc_id(doc_id)
        document_data_obj.set_attachments(attachments)
        document_data_obj.set_group_name(group_name)
        document_data_obj.set_is_primary(is_primary)

        response_array = self.__attachment_service.add_attachment(
            document_data_obj, False)
        logger.debug(
            f"Attachment added for doc_id = {doc_id, response_array}")
        return response_array

    def create_attributes_for_attachment(self, attachment_id, attributes, doc_id):
        attachment_list = []
        attachment_data_obj = attachment_data.AttachmentData()
        attachment_data_obj.set_attachment_id(attachment_id)
        attachment_data_obj.set_attributes(attributes)
        attachment_list.append(attachment_data_obj)

        document_data_obj = document_data.DocumentData()
        document_data_obj.set_doc_id(doc_id)
        document_data_obj.set_attachments(attachment_list)
        self.__attribute_service.add_attributes(document_data_obj)
        logger.debug(
            f"Attributes added for attachment_id ={attachment_id}")

    def create_annotations_for_attachment(self, attachment_id, annotations, doc_id):
        attachment_list = []
        attachment_data_obj = attachment_data.AttachmentData()
        attachment_data_obj.set_attachment_id(attachment_id)
        attachment_data_obj.set_annotations(annotations)
        attachment_list.append(attachment_data_obj)

        document_data_obj = document_data.DocumentData()
        document_data_obj.set_doc_id(doc_id)
        document_data_obj.set_attachments(attachment_list)
        self.__annotation_service.add_annotations(document_data_obj)
        logger.debug(
            f"Annotations added for attachment_id = {attachment_id}")

    def insert_doc_event(self, doc_id, doc_event_type):
        # insert event type
        self.__document_service.insert_document_event_type(
            doc_id, doc_event_type)
        logger.debug(f"Case event inserted {doc_event_type}")

    def get_queue_list(self, refresh=False):
        # cache queue_list
        if not self.__queue_list or refresh:
            response = self.__queue_service.get_queue_list()
            logger.debug(f"get_queue_list refreshed")
            self.__queue_list = response
        return self.__queue_list

    def add_new_queue(self, doc_type_cde, queue_name_txt, queue_name_cde=0):
        response = self.__queue_service.add_new_queue(
            doc_type_cde, queue_name_txt, queue_name_cde)
        logger.debug(f"add_new_queue status ={response.status_code}")
        if response.status_code == 200:
            self.get_queue_list(refresh=True)

    def assign_queue_to_user(self, queue_name_cde: int, app_user_id: int = 0, app_user_login_id: str = ''):
        try:
            response = self.__user_service.add_user_to_queue(
                app_user_id, queue_name_cde, app_user_login_id)
            logger.debug(
                f"assign_queue_to_user status ={response.status_code}")
            return True
        except Exception as e:
            logger.error(e)
            return False

    def update_task_status(self, doc_id, doc_task_type_cde):
        self.__document_service.update_document_status(
            doc_id, doc_task_type_cde)
        logger.debug(f"Case status updated {doc_task_type_cde}")

    def add_audit_record(self, doc_id, audit_message, current_value, previous_value):
        audit_data_obj_list = []
        audit_data_obj = audit_data.AuditData()
        audit_data_obj.set_entity_name(None)
        audit_data_obj.set_entity_value(None)
        audit_data_obj.set_audit_message(audit_message)
        audit_data_obj.set_current_value(current_value)
        audit_data_obj.set_previous_value(previous_value)
        audit_data_obj_list.append(audit_data_obj)

        response = self.__audit_service.add_document_audit(
            doc_id, audit_data_obj_list)
        logger.debug(f"add_document_audit status ={response.status_code}")

    def assign_case_to_user(self, doc_id: int, app_user_id: int = 0, app_user_login_id: str = '', prev_app_user_login_id: str = ''):
        try:
            is_case_assigned = self.__document_service.assign_case(
                doc_id, app_user_id, app_user_login_id, prev_app_user_login_id)
            logger.debug(
                f"Case assigned to user_id ={app_user_id} ? {is_case_assigned}")
            logger.debug(
                f"Case assigned to user_login_id ={app_user_login_id} ? {is_case_assigned}")
        except Exception as e:
            logger.error(e)
            logger.error("Please check on case-user assignment permission.")

    def close_case(self, doc_id, app_user_id, queue_name_cde, app_user_login_id):
        try:
            self.__document_service.close_case(
                doc_id, app_user_id, queue_name_cde, app_user_login_id)
            logger.debug(f"Case {doc_id} closed")
        except Exception as e:
            logger.error(e)
            logger.error("Could not close case due to error.")

    def add_attachment_relation(self, attachment_id_1, attachment_id_2, doc_id, atta_rel_type_cde=1):
        # add parent child relation among two attachments
        attaAttaRelId = self.__attachment_service.add_atta_atta_relation(
            atta_rel_type_cde, attachment_id_1, attachment_id_2, doc_id)
        logger.debug(
            f"Parent child relation added for doc_id = {doc_id, attaAttaRelId}")
        return attaAttaRelId
