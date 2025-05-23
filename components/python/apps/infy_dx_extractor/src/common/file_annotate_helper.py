# ===============================================================================================================#
# Copyright 2020 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

import copy
import os
import re
import shutil
from itertools import groupby
from operator import itemgetter
import time
import cv2

from common.app_const import *
from common.file_util import FileUtil
from common.process_helper import ProcessHelper as phelper
from common.response import Response

from common.ainauto_logger_factory import AinautoLoggerFactory

logger = AinautoLoggerFactory().get_logger()

class FileAnnotateHelper:

    def __init__(self):
        self.converter_path = os.environ["FORMAT_CONVERTER_HOME"]

    def highlight_and_manage_bbox_file(self, save_copy_obj, img_file_path, extracted_attr_list, out_location):
        # input files copied to output location along with extracted data region highlighted.
        if extracted_attr_list and len(extracted_attr_list) > 0 and save_copy_obj and (phelper.get_val(save_copy_obj, ConfProp.REG_BBOX) or phelper.get_val(save_copy_obj, ConfProp.ANC_BBOX)):
            file_to_highlight_dir = self._copy_files_to_bbox_highlight(
                img_file_path, out_location)
            attr_list, debug_info_list = [], []
            for ext_data_obj in extracted_attr_list:
                for i, val in enumerate(ext_data_obj[ResProp.VALS]):
                    if not val[ResProp.MSG][ResProp.ERROR]:
                        if ResProp.TXT_OBJ in val:
                            attr_list.append(val[ResProp.TXT_OBJ][0])
                        elif ResProp.TAB_OBJ in val:
                            attr_list.append(val[ResProp.TAB_OBJ])
                        else:
                            attr_list.append(
                                val[ResProp.SLCTMRK_OBJ])
                        keys = list(
                            ext_data_obj[ResProp.ADD_DATA][ResProp.DEBUG_INFO].keys())
                        for k in keys:
                            debug_info_list.append(
                                {k: ext_data_obj[ResProp.ADD_DATA][ResProp.DEBUG_INFO][k][0][i]})

            attr_list = sorted(attr_list,
                               key=itemgetter(ResProp.PAGE))
            for page_num, pg_wise_attrs in groupby(attr_list, key=itemgetter(ResProp.PAGE)):
                try:
                    file_to_mark = FileUtil.get_files(
                        file_to_highlight_dir, f"{page_num}{BBOX_FILE_SUFFIX}.*")[0]
                    img_copy = self._highlight_bbox(
                        save_copy_obj, cv2.imread(file_to_mark), pg_wise_attrs)
                    if img_copy is not None:
                        cv2.imwrite(file_to_mark, img_copy)
                except Exception as e:
                    logger.error(e)

            self._manage_bbox_files(file_to_highlight_dir)

    def _copy_files_to_bbox_highlight(self, input, out_location):
        try:
            out_folder = "{}/{}".format(out_location, str(os.path.split(
                input)[1]).replace(SUPPORTING_DIR_SUFFIX, BBOX_FILE_SUFFIX))
            temp_out_location = FileUtil.create_dirs_if_absent(out_folder)
            for file in FileUtil.get_files(input):
                if os.path.isdir(file):
                    for subfile in FileUtil.get_files(file, file_format=FileFormat.JPG):
                        shutil.copy(subfile, self._get_bbox_copy_name(
                            subfile, temp_out_location))
                elif not (str(file).endswith(OcrFileExtension.HOCR) or str(file).endswith(OcrFileExtension.XML)):
                    shutil.copy(file, self._get_bbox_copy_name(
                        file, temp_out_location))
        except Exception as e:
            logger.error(e)
        return temp_out_location

    def _get_bbox_copy_name(self, file, out_dir):
        file_obj = FileUtil.get_file_path_detail(file)
        return f"{out_dir}/{file_obj['fileNameWithoutExt']}{BBOX_FILE_SUFFIX}{file_obj['fileExtension']}"

    def _highlight_bbox(self, save_copy_obj, img_obj, extract_data_list):
        # img_copy = None
        for exe_data in extract_data_list:
            try:
                d_info = copy.deepcopy(exe_data[ResProp.DEBUG_INFO])
                if d_info:
                    default_scaling_factor = {
                        'hor': 1,
                        'ver': 1
                    }
                    skip_lib_list = ["ocr_parser", "common_utils"]
                    for key, value in d_info.items():
                        # Getting value_bbox for highlighting bbox of extracted signature or handwritten
                        if key == "object_detector":
                            for field in value['output']['fields']:
                                value_bbox = field["field_value_bbox"]
                                label = field["field_value"]
                                img_obj = self._highlight_area(
                                    value_bbox, default_scaling_factor, img_obj, label=label)
                        elif key not in skip_lib_list and value["input"]:
                            scaling_factor = value["input"].get(
                                "scaling_factor", default_scaling_factor)
                            if phelper.get_val(save_copy_obj, ConfProp.REG_BBOX):
                                value_bbox = self._get_bbox_from_debug_info(
                                    value["input"])
                                if value_bbox and len(value_bbox) > 0:
                                    label = exe_data[ResProp.LABEL] if phelper.get_val(
                                        save_copy_obj, ConfProp.REG_LABEL) else None
                                    img_obj = self._highlight_area(
                                        value_bbox, scaling_factor, img_obj, label=label)

                    anchor_info = d_info.get("ocr_parser", None)
                    if phelper.get_val(save_copy_obj, ConfProp.ANC_BBOX) and anchor_info and anchor_info["output"]:
                        for anc_txt_bbox in anchor_info["output"][ResProp.ANC_TXT_BBOX]:
                            img_obj = self._highlight_area(
                                anc_txt_bbox[ResProp.BBOX], scaling_factor, img_obj, color="blue")
            except Exception as e:
                logger.error(e)
        return img_obj

    def _get_bbox_from_debug_info(self, d):
        value = None
        for k, v in d.items():
            if str(k).endswith(BBOX_FILE_SUFFIX):
                value = v
            else:
                value = self._get_by_instance(v)
            if value:
                return value

    def _get_by_instance(self, v):
        if isinstance(v, dict):
            return self._get_bbox_from_debug_info(v)
        elif isinstance(v, list):
            value = None
            for item in v:
                if isinstance(item, dict):
                    value = self._get_bbox_from_debug_info(item)
                else:
                    value = self._get_by_instance(item)
                if value:
                    return value

    def _highlight_area(self, val_bbox, scaling_factor, img_obj, color="red", label=None):
        (l, t, w, h) = self._scaling_bbox_for(val_bbox, scaling_factor)
        if color == "red":
            color_code = (0, 0, 255)
        elif color == "blue":
            color_code = (255, 0, 0)
        thickness = 4
        img_copy = cv2.rectangle(
            img_obj, (l, t), (l + w, t + h), color_code, thickness)
        if label:
            cv2.putText(img_copy, label, (l, t-10),
                        cv2.FONT_HERSHEY_SIMPLEX, 0.9, color_code, 2)
        return img_copy

    def _scaling_bbox_for(self, bbox, scaling_factor):
        return [round(val*scaling_factor['hor'])
                if pos % 2 == 0 else round(val*scaling_factor['ver']) for pos, val in enumerate(bbox)]

    def _manage_bbox_files(self, input):
        try:
            if os.path.isdir(input):
                in_file = str(input).replace(BBOX_FILE_SUFFIX, "")
                file_path_obj = FileUtil.get_file_path_detail(in_file)
                if file_path_obj["fileExtension"].lower() == FileExtension.PDF:
                    temp_out_location = f"{str(in_file).replace(file_path_obj['fileExtension'], BBOX_FILE_SUFFIX)}.pdf"
                else:
                    # Currently images to PDF bbox copy allowed only for PDF input files.
                    return

                merge_img, ori_img = [], []
                for h_imge in FileUtil.get_files(input):
                    h_imge = os.path.abspath(h_imge)
                    file_name = FileUtil.get_file_path_detail(h_imge)[
                        "fileName"]
                    if bool(re.match(r"^fp.*_.*-lp.*_.*_bbox.jpg$", file_name)):
                        merge_img.append(h_imge)
                    elif bool(re.match(r"^[0-9]+_bbox.jpg$", file_name)):
                        ori_img.append(h_imge)
                from_files_full_path = '|'.join(ori_img)
                FileUtil.imgs_to_pdf(
                    self.converter_path, from_files_full_path, temp_out_location)
                if len(merge_img) > 0:
                    [os.remove(f) for f in ori_img]
                else:
                    shutil.rmtree(input)
        except Exception as e:
            logger.error(e)
