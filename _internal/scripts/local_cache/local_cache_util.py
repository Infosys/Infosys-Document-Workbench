# ===============================================================================================================#
# Copyright 2025 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

import argparse
import json
import os

VERSION = '0.0.1'


class Color():
    DEFAULT = '\033[0m'
    YELLOW = '\033[93m'
    GREEN = '\033[92m'
    RED = '\033[91m'
    CYAN = '\033[96m'
    GRAY = '\033[90m'


class Action():
    GET = 'get'
    SET = 'set'
    DEL = 'del'


INPUT_DATA_DICT = {
    "action": None,
    "bucket": None,
    "key": None,
    "value": None
}


class LocalCacheUtil():

    __bucket_data_file_name = "bucket_data.json"

    def __init__(self):
        user_directory = os.path.expanduser("~")
        cache_folder_path = os.path.join(user_directory, ".cache", "infosys")
        if not os.path.exists(cache_folder_path):
            os.makedirs(cache_folder_path)
        self.__cache_folder_path = cache_folder_path

    def set(self, bucket, key, value):
        bucket_data = self.__get_bucket_data(bucket)
        bucket_data[key] = value
        self.__save_bucket_data(bucket, bucket_data)

    def get(self, bucket, key):
        error = None
        bucket_data = self.__get_bucket_data(bucket)
        if bucket_data is None or key not in bucket_data:
            error = f"ERROR: Key not found: {key}"
        return bucket_data.get(key, None), error

    def delete(self, bucket, key):
        error, status = None, False
        bucket_data = self.__get_bucket_data(bucket)
        if key not in bucket_data:
            error = f"ERROR: Key not found: {key}"
        else:
            del bucket_data[key]
            status = True
            self.__save_bucket_data(bucket, bucket_data)
        return status, error

    def __get_bucket_data(self, bucket):
        data = {}
        bucket_data_path = os.path.join(
            self.__cache_folder_path, bucket, self.__bucket_data_file_name)
        if os.path.exists(bucket_data_path):
            with open(bucket_data_path, 'r', encoding='utf-8') as f:
                data = f.read()
                data = json.loads(data)
        return data

    def __save_bucket_data(self, bucket, data):
        bucket_data_path = os.path.join(
            self.__cache_folder_path, bucket, self.__bucket_data_file_name)
        if not os.path.exists(os.path.dirname(bucket_data_path)):
            os.makedirs(os.path.dirname(bucket_data_path))
        with open(bucket_data_path, 'w', encoding='utf-8') as f:
            f.write(json.dumps(data, indent=4))


def __parse_input():
    input_data_dict = INPUT_DATA_DICT.copy()
    parser = argparse.ArgumentParser()
    parser.add_argument("action", type=str, choices=[
                        Action.GET, Action.SET, Action.DEL])
    parser.add_argument("--bucket", "-b", default=None, required=True)
    parser.add_argument("--key", "-k", default=None, required=False)
    parser.add_argument("--value", "-v", default=None, required=False)
    args = parser.parse_args()
    input_data_dict['action'] = args.action
    input_data_dict['bucket'] = args.bucket
    input_data_dict['key'] = args.key
    input_data_dict['value'] = args.value
    return input_data_dict


def do_processing():
    input_data_dict = __parse_input()
    key = input_data_dict['key']
    cache_manager = LocalCacheUtil()
    if input_data_dict['action'] == Action.GET:
        value, error = cache_manager.get(
            input_data_dict['bucket'], key)
        if error:
            print(f"{error}")
        else:
            print(f"{key}={value}")
    elif input_data_dict['action'] == Action.SET:
        cache_manager.set(
            input_data_dict['bucket'], key, input_data_dict['value'])
    elif input_data_dict['action'] == Action.DEL:
        status, error = cache_manager.delete(input_data_dict['bucket'], key)
        if not status:
            print(f"{error}")
        else:
            print(f"Deleted key: {key}")


if __name__ == '__main__':
    # Uncomment for unit testing
    # import sys
    # cmd_str = "<leave_blank> set -b app1 -k key1 -v value1"
    # cmd_str = "<leave_blank> set -b app1 -k key2 -v value2"
    # cmd_str = "<leave_blank> del -b app1 -k key2"
    # cmd_str = "<leave_blank> get -b app1 -k key1"
    # cmd_str = "<leave_blank> set -b app2 -k key1 -v value1"
    # cmd_str = "<leave_blank> get -b app2 -k key1"
    # sys.argv = cmd_str.split()

    do_processing()
