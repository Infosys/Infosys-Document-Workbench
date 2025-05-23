# ===============================================================================================================#
# Copyright 2020 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

import json
import glob
from os import path
import os
from time import time
from datetime import datetime
import uuid
import re
import math
from pathlib import Path
import time
import shutil
import copy
import cv2
import infy_common_utils.format_converter as format_converter
from infy_common_utils.format_converter import FormatConverter, ConvertAction
from common.app_const import *


numeric_file_pattern = re.compile(r'.*?(\d+).*?')


class FileUtil:
    @staticmethod
    def get_order(file):
        match = numeric_file_pattern.match(Path(file).name)
        if not match:
            return math.inf
        return int(match.groups()[0])

    @staticmethod
    def get_files(folderpath, file_format="*"):
        '''
        Param:
            folderpath: str
            desc: root folder path to find the file_format type files

            file_format: str
            desc: one or comma separated values

        '''
        found_files = []
        for filter_by in str(file_format).split(","):
            found_files += sorted(glob.glob(folderpath +
                                            "/" + filter_by), key=FileUtil.get_order)
        return found_files

    @staticmethod
    def read_file(file_path):
        text = ""
        try:
            with open(file_path, 'r') as file:
                text = file.read().rstrip()
        except Exception:
            pass
        return text

    @staticmethod
    def create_dirs_if_absent(dir_name):
        '''
        Creates directories recursively if it doesn't exist.
        The dir_name can be relative or absolute

        Parameters:
            dir_name (string): Relative or absolute path of the directory
        '''
        dir_path = dir_name
        try:
            if not path.isdir(dir_path):
                os.makedirs(dir_path)
        except Exception as e:
            pass

        return dir_path

    @staticmethod
    def load_json(file_path):
        data = None
        with open(file_path) as file:
            data = json.load(file)

        if(not data):
            raise Exception(
                "Error while loading the {} file.".format(file_path))
        return data

    @staticmethod
    def imgs_to_pdf(tool_path, from_files_full_path, out_file_full_path, water_mark="NOT ORIGINAL"):
        format_converter.format_converter_jar_home = os.path.dirname(
            tool_path)
        config_param_dict = {
            "to_file": os.path.abspath(out_file_full_path),
            "water_mark": water_mark
        }
        return FormatConverter.execute(
            os.path.abspath(from_files_full_path), convert_action=ConvertAction.IMG_TO_PDF,
            config_param_dict=config_param_dict)

    @staticmethod
    def get_uuid():
        return str(uuid.uuid4())

    @classmethod
    def get_new_short_uuid(cls):
        return FileUtil.get_uuid()[:8]

    @classmethod
    def get_attr_id(cls, doc_id: str):
        return f"{doc_id[:8]}_{FileUtil.get_new_short_uuid()}"

    @classmethod
    def get_attr_val_id(cls, attr_id: str):
        return f"{attr_id}_{FileUtil.get_new_short_uuid()}"

    @staticmethod
    def write_bytes_to_file(bytes, output_file):
        with open(output_file, "wb") as f:
            f.write(bytes)
        return

    @staticmethod
    def write_to_file(content, output_file, mode="w"):
        with open(output_file, mode) as f:
            f.write(content)
        return

    @staticmethod
    def archive_file(output_file_path, ext=".json"):
        try:
            if os.path.exists(output_file_path):
                suffix = FileUtil.get_datetime_str() + ext
                new_name = f'{output_file_path.replace(ext,"")}_{suffix}'
                os.rename(output_file_path, new_name)
        except Exception as e:
            print(e)

    @staticmethod
    def write_to_json(content, output_file, is_exist_archive=False):
        if is_exist_archive:
            FileUtil.archive_file(output_file)
        with open(output_file, "w") as outfile:
            json.dump(content, outfile, indent=4)

    @staticmethod
    def remove_files(file_list):
        for file in file_list:
            os.remove(file)

    @staticmethod
    def remove_dir(dir_path):
        if os.path.isdir(dir_path):
            shutil.rmtree(dir_path)
        else:
            raise ValueError("Invalid directory path: {}".format(dir_path))    

    @staticmethod
    def move_file(source, destination):
        derived_file = destination + "/"+path.split(source)[1]
        try:
            shutil.move(source, destination)
        except Exception as e:
            derived_file = None
        return derived_file

    @staticmethod
    def copy_file(source, destination):
        derived_file = destination
        try:
            if os.path.realpath(source) != os.path.realpath(destination):
                shutil.copy(source, destination)
        except Exception as e:
            derived_file = None
        return derived_file

    @staticmethod
    def get_file_path_detail(input_file_path):
        file_dir_path, file_name = os.path.split(input_file_path)
        file_name_no_ext, file_ext = os.path.splitext(file_name)
        file_dir = os.path.split(file_dir_path)[1]
        return {"fileDirPath": str(file_dir_path),
                "fileDir": str(file_dir),
                "fileName": str(file_name),
                "fileExtension": str(file_ext),
                "fileNameWithoutExt": str(file_name_no_ext)}

    @staticmethod
    def update_state(file, from_state=ProcessState.INPROGRESS, to_state=EMPTY, content=""):
        changed_file = file.replace(
            from_state+FileExtension.TXT, to_state+FileExtension.TXT)
        os.rename(file, changed_file)
        log_text = '{}  {}\n'.format(FileUtil.get_time_str(
        ), "Status updated from {} to {}".format(from_state, to_state))
        if content:
            log_text += '{}  {}\n'.format(FileUtil.get_time_str(), content)
        FileUtil.write_to_file(log_text, changed_file, mode="a")
        uuid = path.splitext(
            path.split(changed_file)[1])[0]
        return changed_file, uuid.replace(to_state, EMPTY)

    @staticmethod
    def get_time_str(format="%Y-%m-%d %H:%M:%S"):
        return time.strftime(format)

    @staticmethod
    def get_datetime_str(format="%Y%m%d_%H%M%S_%f"):
        return datetime.now().strftime(format)[:-3]

    @staticmethod
    def get_current_datetime():
        return FileUtil.get_datetime_str(format="%Y-%m-%d %H:%M:%S.%f")

    @staticmethod
    def get_image_width_height(image_file):
        im = cv2.imread(image_file)
        return im.shape[1], im.shape[0]

    @staticmethod
    def get_updated_config_dict(from_dict, default_dict):
        config_dict_temp = copy.deepcopy(default_dict)
        for key in from_dict:
            if type(from_dict[key]) == dict:
                config_dict_temp[key] = FileUtil.get_updated_config_dict(
                    from_dict[key], config_dict_temp[key])
            else:
                config_dict_temp[key] = from_dict[key]
        return config_dict_temp
