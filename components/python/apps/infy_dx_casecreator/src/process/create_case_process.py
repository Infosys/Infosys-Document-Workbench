# ===============================================================================================================#
# Copyright 2025 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

import copy
import traceback

from common.file_util import FileUtil
from common.logger_factory import LoggerFactory
from helper.docwb_sdk_helper import DocwbSdkHelper
from process.telemetry_process import TelemetryProcess, LogLevel
from service.docwb_sdk_service import DocwbSdkService

logger = LoggerFactory().get_logger()

TELEMETRY_STARTED = "started"
TELEMETRY_ENDED = "ended"


class CreateCaseProcess():
    def __init__(self, case_creator_config: dict) -> None:
        self.__case_creator_config = case_creator_config
        self.__docwb_sdk_service_obj = DocwbSdkService(
            self.__case_creator_config)
        self.__telemetry_process = None

    def execute(self, doc_data_list: list, summary_dict: dict,
                previous_run_response_dict: dict, telemetry_process_obj: TelemetryProcess):
        group_telemetry_dict = {}
        self.__telemetry_process = telemetry_process_obj
        case_attribute_dict = copy.deepcopy(self.__case_creator_config)
        doc_group_id = doc_data_list[0]['doc_group_id']
        primary_record_list = []
        remaining_record_list = []
        # Check for exactly 1 primary record
        if len(doc_data_list) == 1:
            primary_record_list.append(doc_data_list[0])
        else:
            for doc_data_dictn in doc_data_list:
                if(doc_data_dictn["docwb_config_attribute"]["is_primary_document"]):
                    primary_record_list.append(doc_data_dictn)
                else:
                    remaining_record_list.append(doc_data_dictn)

        if not primary_record_list:
            raise Exception(
                f"No record found with 'is_primary_document' set as True for group {doc_group_id}")
        elif len(primary_record_list) > 1:
            raise Exception(
                f"{len(primary_record_list)} records found with 'is_primary_document' set as True for group {doc_group_id}")

        # To store primary_record as first record in a new list
        _doc_data_list = primary_record_list + remaining_record_list
        doc_data_dict = _doc_data_list[0]
        try:
            # ---------------------- Telemetry: START Event ----------------------
            telemetry_data = {'telemetry': doc_data_dict['telemetry']}
            for doc_data_dict_temp in _doc_data_list:
                group_telemetry_dict[doc_data_dict_temp['doc_id']
                                     ] = TELEMETRY_STARTED
                self.__telemetry_process.post_telemetry_event_start(
                    doc_data_dict_temp['doc_batch_id'], doc_data_dict_temp['doc_id'], telemetry_data)

            # --------------------- rerun validation ---------------------
            # While rerunning the same request file, avoid creating new case if it is already created.
            prev_run_case_data = {}
            if previous_run_response_dict:
                prev_run_case_data = self.__get_prev_created_case(
                    _doc_data_list, previous_run_response_dict)

            if not prev_run_case_data:
                # Check if related_case_num exists
                prev_run_case_data = self.__get_related_case(_doc_data_list)

            # --------------------- prepare case creation basic data ---------------------
            if not prev_run_case_data.get('queue_name_cde'):
                self.__manage_doc_queue(doc_data_dict, case_attribute_dict)
            else:
                case_attribute_dict['doc_queue_name_cde'] = prev_run_case_data['queue_name_cde']

            # --------------------- create case with attributes ---------------------
            if not prev_run_case_data.get('case_num'):
                self.__create_case(
                    doc_data_dict, case_attribute_dict)
            else:
                case_attribute_dict['doc_id'] = prev_run_case_data['case_num']
                case_attribute_dict['created_dtm'] = prev_run_case_data['created_dtm']

            # --------------------- Add attachments and related attributes ---------------------
            prev_run_success_doc_id_list = [
                x.get('doc_id') for x in prev_run_case_data.get('success_records', [])]

            # To maintain parent_attachment_id for _doc_data_list
            case_attribute_dict['parent_attachment_id'] = None
            for index in range(0, len(_doc_data_list)):
                doc_data_dict1 = _doc_data_list[index]
                # If record was successfully processed in prev run, then skip
                if doc_data_dict1['doc_id'] in prev_run_success_doc_id_list:
                    continue
                self.__add_attachment_to_case(
                    doc_data_dict1, case_attribute_dict)
                self.__add_attachment_attributes(
                    doc_data_dict1, case_attribute_dict)

            # --------------------- case events ---------------------
            if not prev_run_case_data.get('case_num'):
                self.__insert_events(case_attribute_dict)
                # unused method hence commenting it
                # if case_attribute_dict.get('add_audit', None):
                #     self.__add_audit(case_attribute_dict)
                self.__manage_case_assignment(
                    case_attribute_dict, doc_data_dict)
                self.__update_case_status_to_FYR(
                    case_attribute_dict, doc_data_dict)
                self.__add_audit_message(doc_data_dict, case_attribute_dict)
                self.__close_case(doc_data_dict, case_attribute_dict)

            # --------------------- batch summary ---------------------

            for doc_data_dict in _doc_data_list:
                self.__update_summary(
                    summary_dict, doc_data_dict['doc_id'], "Success", "case_creator")
                _doc_id = doc_data_dict['doc_id']
                # For previous successful records, copy case data from prev record
                if _doc_id in prev_run_success_doc_id_list:
                    prev_success_doc_data = [x for x in prev_run_case_data.get(
                        'success_records', []) if x.get('doc_id') == _doc_id][0]
                    doc_data_dict["docwb_case_data"] = prev_success_doc_data['docwb_case_data']
                else:
                    doc_data_dict["docwb_case_data"] = {
                        'created_dtm': case_attribute_dict['created_dtm'],
                        'case_num': case_attribute_dict['doc_id'],
                        'doc_queue_name_cde': case_attribute_dict['doc_queue_name_cde'],
                        'docwbdxfiledb_path': ''
                    }
                # ---------------------- Telemetry: LOG Event ----------------------
                context_data_list = [{"type": "case_num", "id": case_attribute_dict.get('doc_id', 0)},
                                     {"type": "doc_queue_name_cde", "id": case_attribute_dict.get('doc_queue_name_cde', 0)}]
                self.__telemetry_process.post_telemetry_event_log(
                    doc_data_dict['doc_batch_id'], doc_data_dict['doc_id'], LogLevel.INFO, message="Success",
                    additional_config_param=telemetry_data, context_data=context_data_list)
                # ---------------------- Telemetry: END Event ----------------------
                self.__telemetry_process.post_telemetry_event_end(
                    doc_data_dict['doc_batch_id'], doc_data_dict['doc_id'], telemetry_data)
                group_telemetry_dict[doc_data_dict['doc_id']] = TELEMETRY_ENDED

            return _doc_data_list, summary_dict
        except Exception:
            full_trace_error = traceback.format_exc()
            logger.error(full_trace_error)
            self.__update_summary(
                summary_dict, doc_data_dict['doc_id'], "Failed", "case_creator")

            # ---------------------- Telemetry Events ----------------------
            for doc_data_dict_temp in _doc_data_list:
                if group_telemetry_dict.get(doc_data_dict_temp['doc_id']) == TELEMETRY_STARTED:
                    # ---------------------- Telemetry: :LOG Event ----------------------
                    self.__telemetry_process.post_telemetry_event_log(
                        doc_data_dict_temp['doc_batch_id'], doc_data_dict_temp['doc_id'], LogLevel.ERROR, message="Failed",
                        additional_config_param=telemetry_data)
                    # ---------------------- Telemetry: END Event ----------------------
                    self.__telemetry_process.post_telemetry_event_end(
                        doc_data_dict_temp['doc_batch_id'], doc_data_dict_temp['doc_id'], telemetry_data)
            return None, summary_dict

    def __manage_case_assignment(self, case_attribute_dict, doc_data_dict):
        docwb_config_attribute = doc_data_dict.get(
            'docwb_config_attribute', {})
        if docwb_config_attribute.get('assign_case_to_user_login_id'):
            self.__docwb_sdk_service_obj.assign_case_to_user(
                case_attribute_dict['doc_id'],
                app_user_login_id=docwb_config_attribute.get('assign_case_to_user_login_id'))

    def __get_queue_list(self, refresh=False):
        return self.__docwb_sdk_service_obj.get_queue_list(refresh)

    def __manage_doc_queue(self, doc_data_dict, case_attribute_dict, is_queue_create=False):
        def _get_queue_cde():
            available_queues = [queue for queue in self.__get_queue_list() if queue.get(
                "txt") == docwb_config_attribute.get("doc_queue_name")]
            if available_queues:
                return available_queues[0].get("cde")
            return 0

        docwb_config_attribute = doc_data_dict.get('docwb_config_attribute')
        if not docwb_config_attribute:
            return
        # get exist queue cde for queue name
        queue_cde = _get_queue_cde()
        if queue_cde == 0 and is_queue_create:
            # add new queue
            self.__docwb_sdk_service_obj.add_new_queue(
                case_attribute_dict.get('doc_type_cde'), docwb_config_attribute.get("doc_queue_name"))
            queue_cde = _get_queue_cde()
            logger.info(
                f"New queue added for queue name - {docwb_config_attribute.get('doc_queue_name')}")
        logger.info(
            f"Queue name - {docwb_config_attribute.get('doc_queue_name')}, Queue cde - {queue_cde}")
        case_attribute_dict['doc_queue_name_cde'] = queue_cde

    def manage_new_queue(self, raw_extracted_data_dict_list):
        queue_names_temp = []
        for doc_data in raw_extracted_data_dict_list:
            try:
                docwb_config_attribute = doc_data.get(
                    'docwb_config_attribute', {})
                queue_name = docwb_config_attribute.get("doc_queue_name")
                if not docwb_config_attribute or not queue_name:
                    continue
                if not queue_name in queue_names_temp:
                    queue_names_temp.append(queue_name)
                    self.__manage_doc_queue(
                        doc_data, copy.deepcopy(self.__case_creator_config), is_queue_create=True)
            except Exception as e:
                logger.error(e)

    def manage_queue_assignment(self, raw_extracted_data_dict_list):
        available_queue_dict = {q_data['txt']: q_data['cde']
                                for q_data in self.__get_queue_list(True)}
        prev_done_list = []
        for doc_data_temp in raw_extracted_data_dict_list:
            queue_name = doc_data_temp.get(
                'docwb_config_attribute', {}).get("doc_queue_name")
            queue_name_cde = available_queue_dict.get(queue_name, 0)
            if queue_name_cde == 0:
                continue
            self.__manage_doc_queue_assignment(
                doc_data_temp, queue_name_cde, prev_done_list)

    def __manage_doc_queue_assignment(self, doc_data_dict, queue_cde, prev_done_list):
        def _is_exist(key):
            if key in prev_done_list:
                return True
            else:
                return False

        def _update_list(key, can_update):
            if can_update:
                prev_done_list.append(key)

        docwb_config_attribute = doc_data_dict.get(
            'docwb_config_attribute', {})
        for user_login_id in docwb_config_attribute.get("assign_user_login_id_to_queue", []):
            if not user_login_id or _is_exist(f"{queue_cde}_{user_login_id}"):
                continue
            update_status = self.__docwb_sdk_service_obj.assign_queue_to_user(
                queue_cde, app_user_login_id=user_login_id)
            _update_list(f"{queue_cde}_{user_login_id}", update_status)
        for user_id in docwb_config_attribute.get("assign_user_id_to_queue", []):
            if user_id <= 0 or _is_exist(f"{queue_cde}_{user_login_id}"):
                continue
            update_status = self.__docwb_sdk_service_obj.assign_queue_to_user(
                queue_cde, app_user_id=user_id)
            _update_list(f"{queue_cde}_{user_login_id}", update_status)

    def __create_case(self, doc_data_dict: dict, case_attribute_dict: dict):
        # -------------------- Document level system/custom attributes -------------------
        should_raise_category_excep = True if case_attribute_dict['doc_type_cde'] == 1 else False
        case_attributes = []
        filtered_business_attributes = [
            x for x in doc_data_dict['business_attributes'] if x['docwb_attribute_type_cde'] == 1]
        filtered_business_attributes = [
            x for x in filtered_business_attributes if not x.get('cancelled', False)]
        for docwb_case_attr in filtered_business_attributes:
            if docwb_case_attr['docwb_attribute_name_cde'] == 19:
                should_raise_category_excep = False
            case_attributes.append(DocwbSdkHelper.make_attribute_data(
                docwb_case_attr['docwb_attribute_name_cde'], docwb_case_attr['text'],
                docwb_case_attr['docwb_extract_type_cde'],
                docwb_case_attr.get('text_list_confidence_pct', -1)))
        if should_raise_category_excep:
            raise Exception(
                'Not found mandatory attribute Category(19) in Business Attributes.')

        # -------------------- Create case with the document levele attributes -------------------
        case_attribute_dict['doc_id'] = self.__docwb_sdk_service_obj.create_case(
            case_attribute_dict['doc_queue_name_cde'], case_attributes)
        case_attribute_dict['created_dtm'] = FileUtil.get_current_datetime()

    def __add_attachment_to_case(self, doc_data_dict: dict, case_attribute_dict: dict):
        docwb_config_attribute = doc_data_dict.get(
            'docwb_config_attribute', {})
        group_name = None
        is_primary = docwb_config_attribute['is_primary_document']
        if docwb_config_attribute.get('attachment_file_paths'):
            attachment_file_paths_list = docwb_config_attribute['attachment_file_paths']
        else:
            attachment_file_paths_list = [
                docwb_config_attribute['attachment_file_path']]

        for idx, attachment_path in enumerate(attachment_file_paths_list):
            attachment_data = DocwbSdkHelper.make_attachment_data(
                attachment_path)
            attachment_array = self.__docwb_sdk_service_obj.create_attachment(
                case_attribute_dict['doc_id'], [attachment_data], is_primary, group_name)
            group_name = attachment_array[0]['groupName']
            attachment_id = attachment_array[0]['attachmentId']
            # first file in the group is assumed to be presentable part of the subsequent file.
            # attributes extracted have to be associated to this file.
            if idx == 0:
                case_attribute_dict['attachment_id'] = attachment_id
            if idx == 0 and is_primary:
                case_attribute_dict['parent_attachment_id'] = attachment_id
                is_primary = False
            else:
                child_attachment_id = attachment_id
                self.__docwb_sdk_service_obj.add_attachment_relation(
                    case_attribute_dict['parent_attachment_id'], child_attachment_id, case_attribute_dict['doc_id'])

    def __add_attachment_attributes(self, doc_data_dict: dict, case_attribute_dict: dict):
        data_list = {'attributes': [], 'annotations': []}
        # -------------------- Attachment - Extracted attributes --------------------
        should_raise_doctype_excep = True if case_attribute_dict['doc_type_cde'] == 2 else False
        add_business_attribute_annotation = doc_data_dict['docwb_config_attribute'].get(
            'add_business_attribute_annotation', True)
        filtered_business_attributes = [
            x for x in doc_data_dict['business_attributes'] if x['docwb_attribute_type_cde'] == 2]
        filtered_business_attributes = [
            x for x in filtered_business_attributes if not x.get('cancelled', False)]
        for attr_data in filtered_business_attributes:
            if attr_data['docwb_attribute_name_cde'] == 31:
                should_raise_doctype_excep = False
            if not add_business_attribute_annotation:
                occurence_num = None
            else:
                occurence_num = 0 if not attr_data['docwb_attribute_name_cde'] == 31 else -1
            DocwbSdkHelper.add_attribute_data_to_list(
                data_list, attr_data, occurence_num)

        if should_raise_doctype_excep:
            raise Exception(
                'Not found mandatory attribute DocumentType(31) in Business Attributes.')

        if len(data_list['attributes']) > 0:
            self.__docwb_sdk_service_obj.create_attributes_for_attachment(
                case_attribute_dict['attachment_id'], data_list['attributes'], case_attribute_dict['doc_id'])
        if len(data_list['annotations']) > 0:
            self.__docwb_sdk_service_obj.create_annotations_for_attachment(
                case_attribute_dict['attachment_id'], data_list['annotations'], case_attribute_dict['doc_id'])

    def __insert_events(self, case_attribute_dict: dict):
        # ------------------- Don't change event order -------------------
        self.__docwb_sdk_service_obj.insert_doc_event(
            case_attribute_dict['doc_id'], DocwbSdkService.EnumEventType.ATTRIBUTES_EXTRACTED)
        self.__docwb_sdk_service_obj.insert_doc_event(
            case_attribute_dict['doc_id'], DocwbSdkService.EnumEventType.CASE_OPENED)
        self.__docwb_sdk_service_obj.update_task_status(
            case_attribute_dict['doc_id'], DocwbSdkService.EnumTaskStatusType.YET_TO_START)

    def __add_audit(self, case_attribute_dict: dict, audit_message: str = 'Document added',
                    current_value: str = '', previous_value: str = ''):
        self.__docwb_sdk_service_obj.add_audit_record(
            case_attribute_dict['doc_id'], audit_message, current_value, previous_value)

    def __update_case_status_to_FYR(self, case_attribute_dict: dict, doc_data_dict: dict):
        docwb_config_attribute = doc_data_dict.get(
            'docwb_config_attribute', {})
        if docwb_config_attribute.get('update_case_to_for_your_review'):
            self.__docwb_sdk_service_obj.update_task_status(
                case_attribute_dict['doc_id'],
                self.__docwb_sdk_service_obj.EnumTaskStatusType.FOR_YOUR_REVIEW)

    def __update_summary(self, summary_dict, doc_id, status, column_name):
        if summary_dict.get(column_name):
            summary_dict[column_name][doc_id] = status
        else:
            summary_dict[column_name] = {
                doc_id: status
            }

    def __add_audit_message(self, doc_data, case_attribute_dict, previous_value: str = ''):
        docwb_config_attribute = doc_data.get('docwb_config_attribute', {})
        audit_data_list = docwb_config_attribute.get("audit_data", [])
        for audit_data in audit_data_list:
            try:
                audit_message = audit_data['activity'].strip()
                current_value = audit_data['value'].strip()
                if len(audit_message) == 0:
                    logger.info(f'Skipping audit_data {audit_data}')
                    continue
                doc_id = case_attribute_dict["doc_id"]
                self.__docwb_sdk_service_obj.add_audit_record(
                    doc_id, audit_message, current_value, previous_value)
            except Exception as e:
                logger.error(e)

    def __close_case(self, doc_data, case_attribute_dict):
        docwb_config_attribute = doc_data.get('docwb_config_attribute', {})
        case_closed = docwb_config_attribute.get("update_case_to_closed")
        if case_closed:
            doc_id = case_attribute_dict["doc_id"]
            app_user_id = docwb_config_attribute.get(
                "assign_user_id_to_queue", 0)
            queue_name_cde = case_attribute_dict['doc_queue_name_cde']
            app_user_login_id = docwb_config_attribute.get(
                'assign_case_to_user_login_id')
            self.__docwb_sdk_service_obj.close_case(
                doc_id, app_user_id, queue_name_cde, app_user_login_id)

    def __get_related_case(self, doc_data_list: list) -> dict:
        prev_run_case_data = {
            'case_num': None,
            'created_dtm': None,
            'queue_name_cde': None,
            'success_records': [],
            'failed_records': []
        }
        for doc_data_dict in doc_data_list:
            case_num_exist = doc_data_dict["docwb_config_attribute"].get(
                "related_case_num")
            if case_num_exist:
                prev_run_case_data['case_num'] = case_num_exist
                prev_run_case_data['failed_records'] = doc_data_list
                break

        return prev_run_case_data

    def __get_prev_created_case(self, doc_data_list: list, previous_run_response_dict: dict) -> dict:
        prev_run_case_data = {
            'case_num': None,
            'created_dtm': None,
            'queue_name_cde': None,
            'success_records': [],
            'failed_records': []
        }
        prev_run_doc_data_list = []
        for doc_id in [x['doc_id'] for x in doc_data_list]:
            prev_run_doc_data_list.append(
                previous_run_response_dict.get(doc_id))

        doc_group_id = doc_data_list[0]['doc_group_id']
        prev_run_case_num_list = []
        prev_run_case_created_dtm = None
        prev_run_queue_name_cde = None
        prev_failed_doc_data_list = []
        prev_success_doc_data_list = []
        for prev_run_doc_data in prev_run_doc_data_list:
            if prev_run_doc_data:
                doc_id = prev_run_doc_data['doc_id']
                _prev_case_num = prev_run_doc_data.get(
                    'docwb_case_data', {}).get('case_num')
                if _prev_case_num:
                    prev_run_case_num_list.append(_prev_case_num)
                    prev_run_case_created_dtm = prev_run_doc_data.get(
                        'docwb_case_data', {}).get('created_dtm')
                    prev_run_queue_name_cde = prev_run_doc_data.get(
                        'docwb_case_data', {}).get('doc_queue_name_cde')
                    prev_success_doc_data_list.append(prev_run_doc_data)
                else:
                    prev_failed_doc_data_list.append(prev_run_doc_data)

        prev_run_case_data['success_records'] = prev_success_doc_data_list
        prev_run_case_data['failed_records'] = prev_failed_doc_data_list

        prev_run_case_num_list = list(set(prev_run_case_num_list))
        if len(prev_run_case_num_list) == 1:
            prev_run_case_data['case_num'] = prev_run_case_num_list[0]
            prev_run_case_data['created_dtm'] = prev_run_case_created_dtm
            prev_run_case_data['queue_name_cde'] = prev_run_queue_name_cde
        elif len(prev_run_case_num_list) > 1:
            message = f"Multiple docwb/emailwb case nums {prev_run_case_num_list} found for group {doc_group_id} ."
            message += "Expected is 1 for failure rerun."
            raise Exception(message)

        return prev_run_case_data
