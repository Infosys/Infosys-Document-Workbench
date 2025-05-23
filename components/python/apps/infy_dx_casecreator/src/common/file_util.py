# ===============================================================================================================#
# Copyright 2020 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

import json
import glob
import configparser
from os import path
import os
import uuid
import shutil
from datetime import datetime


class FileUtil:
    @staticmethod
    def get_file_exe(fullpath):
        return str(path.splitext(path.split(fullpath)[1])[1])

    @staticmethod
    def get_images(folderpath, file_format):
        found_files = []
        for type in str(file_format).split(","):
            found_files += glob.glob(folderpath + "/" + type)
        return found_files

    @staticmethod
    def get_files(folderpath, filter_by="*.pdf"):
        return glob.glob(folderpath + "/" + filter_by)

    @staticmethod
    def read_file():
        pass

    @staticmethod
    def create_dirs_if_absent(dir_name):
        '''
        Creates directories recursively if it doesn't exist.
        The dir_name can be relative or absolute

        Parameters:
            dir_name (string): Relative or absolute path of the directory
        '''
        dir_path = dir_name
        if not path.isdir(dir_path):
            os.makedirs(dir_path)

        return dir_path

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
    def get_config_parser(config_file):
        config_parser = configparser.ConfigParser()
        config_parser.read(config_file)
        return config_parser

    @staticmethod
    def load_json(file_path):
        data = None
        with open(file_path) as file:
            data = json.load(file)

        if(not data):
            raise Exception(f'Error while loading json file {file_path}')
        return data

    @staticmethod
    def get_uuid():
        return str(uuid.uuid1())

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
    def move_all(source, destination):
        for file in glob.glob(source+"/*.*"):
            FileUtil.move_file(file, destination)

    @staticmethod
    def move_file(source, destination):
        try:
            shutil.move(source, destination)
        except Exception as e:
            pass

    @staticmethod
    def move_to_work_dir(work_input_location, uuid, img_file):
        work_input_location, _ = FileUtil.create_uuid_dir(
            work_input_location, uuid)
        derived_file = work_input_location + \
            "/"+path.split(img_file)[1]
        FileUtil.move_file(img_file, derived_file)
        return derived_file

    @staticmethod
    def create_uuid_dir(work_input_location, uuid=None):
        if not uuid:
            uuid = FileUtil.get_uuid()
        work_input_location = FileUtil.create_dirs_if_absent(
            work_input_location + "/" + uuid)
        return work_input_location, uuid

    @staticmethod
    def is_file_path_valid(file_path):
        file_path_abs = file_path
        if not path.isabs(file_path_abs):
            file_path_abs = path.abspath(file_path_abs)
        return path.isfile(file_path_abs)

    @staticmethod
    def get_datetime_str(format="%Y%m%d_%H%M%S_%f"):
        return datetime.now().strftime(format)[:-3]

    @staticmethod
    def get_current_datetime():
        return FileUtil.get_datetime_str(format="%Y-%m-%d %H:%M:%S.%f")

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

    @classmethod
    def read_json(cls, json_file):
        with open(json_file, 'r', encoding='utf8') as file_reder:
            yield json.load(file_reder)

    @staticmethod
    def remove_files(file_list):
        for file in file_list:
            os.remove(file)
