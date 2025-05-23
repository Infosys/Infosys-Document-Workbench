# ===============================================================================================================#
# Copyright 2025 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

import os
from datetime import datetime, timezone
from infy_docwb_sdk.data import (
    annotation_data, attachment_data, attribute_data)
from common.logger_factory import LoggerFactory

logger = LoggerFactory().get_logger()


class DocwbSdkHelper():

    @classmethod
    def make_attribute_data(cls, attr_name_cde, attr_value, extract_type_cde, confidence_pct=-1):
        attribute_data_obj = attribute_data.AttributeData()
        attribute_data_obj.set_attr_name_cde(attr_name_cde)
        attribute_data_obj.set_attr_value(attr_value)
        attribute_data_obj.set_confidence_pct(confidence_pct)
        attribute_data_obj.set_extract_type_cde(extract_type_cde)
        return attribute_data_obj

    @classmethod
    def make_attachment_data(cls, physical_path):
        attachment_data_obj = attachment_data.AttachmentData()
        attachment_data_obj.set_logical_name(os.path.split(physical_path)[1])
        attachment_data_obj.set_physical_path(
            os.path.abspath(physical_path))
        attachment_data_obj.set_extract_type_cde(1)
        return attachment_data_obj

    @classmethod
    def add_attribute_data_to_list(cls, data_list, attr_data, occurence_num=None):
        attr_name_txt = attr_data['name']
        attr_name_cde = attr_data['docwb_attribute_name_cde']
        attr_value = attr_data['text']
        extract_type_cde = attr_data['docwb_extract_type_cde']
        confidence_pct = attr_data.get('text_list_confidence_pct', -1)
        attribute_list = data_list['attributes']
        annotations = data_list['annotations']

        attribute_data_obj = attribute_data.AttributeData()
        attribute_data_obj.set_attr_value(attr_value)
        attribute_data_obj.set_confidence_pct(confidence_pct)
        attribute_data_obj.set_extract_type_cde(extract_type_cde)
        sub_attributes = []
        if attr_name_cde == 44:
            attributes: attribute_data.AttributeData = []
            attribute_data_obj.set_attr_name_txt(attr_name_txt)
            if attribute_list != []:
                attributes: attribute_data.AttributeData = list(filter(
                    lambda attribute: attribute.get_attr_name_cde() == 44, attribute_list))
            if attributes is None or attributes == []:
                sub_attributes = []
                sub_attributes.append(attribute_data_obj)
                attribute_data_parent_obj = attribute_data.AttributeData()
                attribute_data_parent_obj.set_attr_name_cde(
                    attr_name_cde)
                attribute_data_parent_obj.set_confidence_pct(-1)
                attribute_data_parent_obj.set_extract_type_cde(1)
                attribute_data_parent_obj.set_attributes(sub_attributes)
                attribute_list.append(attribute_data_parent_obj)
            else:
                attribute: attribute_data.AttributeData = attributes[0]
                sub_attributes = attribute.get_attributes()
                sub_attributes.append(attribute_data_obj)
        else:
            attribute_data_obj.set_attr_name_cde(attr_name_cde)
            attribute_list.append(attribute_data_obj)

        if not occurence_num == None:
            for source_data in attr_data.get("text_bbox_list",[]):
                attr_val = source_data.get('text', '')
                if not attr_name_txt:
                    continue
                annotation_data_obj = annotation_data.AnnotationData()
                annotation_data_obj.set_label(attr_name_txt)
                annotation_data_obj.set_value(attr_val)
                annotation_data_obj.set_occurrence_num(occurence_num)
                if attr_data["text_bbox_list"]:
                    annotation_data_obj.set_page(source_data.get("page", 0))
                    annotation_data_obj.set_page_bbox(
                        source_data.get("page_bbox", []))
                    annotation_data_obj.set_source_bbox(
                        source_data.get("source_bbox", []))
                if not attr_val and (occurence_num < 0 or not annotation_data_obj.get_source_bbox() or len(annotation_data_obj.get_source_bbox()) != 4):
                    logger.warning(
                        f"For an attribute {attr_name_txt} annotation, value or source bbox should be populated")
                    continue
                annotations.append(annotation_data_obj)

    @classmethod
    def make_attribute_data_from_email_string(cls, email_string) -> list:
        splitted_val = email_string.split('\n')
        from_val = None
        from_id_val = None
        to_val = None
        to_id_val = None
        received_val = None
        subject_val = None
        content_val = ''

        for val in splitted_val:
            if val != '':
                index = val.find(':')
                if index != -1:
                    if val[:index].lower() == 'from':
                        from_id_val = val[index+1:]
                    elif val[:index].lower() == 'to':
                        to_id_val = val[index+1:]
                    elif val[:index].lower() == 'received':
                        received_val = val[index+1:]
                    elif val[:index].lower() == 'subject':
                        subject_val = val[index+1:]
                else:
                    content_val += val + "\n"
            else:
                content_val += val + "\n"
        if from_id_val is not None:
            from_val = from_id_val.split("@")[0]
        if to_id_val is not None:
            to_ids = to_id_val.split(";")
            for to_id in to_ids:
                if to_val is None:
                    to_val = ''
                to_val += to_id.split("@")[0] + ';'

        if received_val is None:
            received_val = datetime.now(
                timezone.utc).astimezone().strftime("%a %b %d %Y %H:%M:%S")

        attributes = []
        # attr_name_cde, attr_value, extract_type_cde, confidence_pct = -1
        attributes.append(cls.make_attribute_data(1, from_val, 1, 100))
        attributes.append(cls.make_attribute_data(2, received_val, 1, 100))
        attributes.append(cls.make_attribute_data(3, subject_val, 1, 100))
        attributes.append(cls.make_attribute_data(4, to_val, 1, 100))
        attributes.append(cls.make_attribute_data(5, to_id_val, 1, 100))
        attributes.append(cls.make_attribute_data(9, content_val, 1, 100))
        attributes.append(cls.make_attribute_data(20, from_id_val, 1, 100))

        return attributes
