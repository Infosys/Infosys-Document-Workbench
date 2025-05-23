# ===============================================================================================================#
# Copyright 2025 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

import argparse
import json
import re

VERSION = '0.0.1'


class Color():
    DEFAULT = '\033[0m'
    YELLOW = '\033[93m'
    GREEN = '\033[92m'
    RED = '\033[91m'
    CYAN = '\033[96m'
    GRAY = '\033[90m'


INPUT_DATA_DICT = {
    "config_file_path": None,
    "key": None,
    "token": None,
    "value": None
}


def __parse_input():
    input_data_dict = INPUT_DATA_DICT.copy()
    parser = argparse.ArgumentParser()
    parser.add_argument("--config_file_path", default=None, required=True)
    parser.add_argument("--key", default=None, required=False)
    parser.add_argument("--token", default=None, required=False)
    parser.add_argument("--value", default=None, required=True)
    args = parser.parse_args()
    input_data_dict['config_file_path'] = args.config_file_path
    input_data_dict['key'] = args.key
    input_data_dict['token'] = args.token
    input_data_dict['value'] = args.value
    return input_data_dict


def __load_config_file(file_path):
    data = None
    line_ending = None
    with open(file_path, 'rb') as f:
        content = f.read()
        if b'\r\n' in content:
            line_ending = '\r\n'
        elif b'\n' in content:
            line_ending = '\n'
        data = content.decode('utf-8')
    try:
        data = json.loads(data)
    except:
        pass
    return data, line_ending


def __save_config_file(file_path, data):

    if isinstance(data, dict):
        data = json.dumps(data, indent=4)

    with open(file_path, 'wb') as f:
        f.write(data.encode('utf-8'))


def __replace_value(data, old_value, new_value):
    if isinstance(data, dict):
        for key, value in data.items():
            if isinstance(value, dict):
                __replace_value(value, old_value, new_value)
            elif isinstance(value, list):
                for item in value:
                    if isinstance(item, dict):
                        __replace_value(item, old_value, new_value)
                    else:
                        if item == old_value:
                            value[value.index(item)] = new_value
            elif value == old_value:
                data[key] = new_value
    elif isinstance(data, list):
        for item in data:
            __replace_value(item, old_value, new_value)


def do_processing():
    input_data_dict = __parse_input()
    config_file_path = input_data_dict['config_file_path']
    content, line_ending = __load_config_file(config_file_path)

    if not isinstance(content, dict) and input_data_dict['key']:
        lines = content.split(line_ending)
        for idx, line in enumerate(lines):
            if re.match(r'^\s*#', line):
                pass
            elif re.match(r'^\s*$', line):
                pass
            else:
                key, value = line.split('=', 1)
                if key.strip() == input_data_dict['key']:
                    print(f"{Color.GRAY}Key: {key}{Color.DEFAULT}")
                    line = key + '=' + input_data_dict['value']
                    message = f"{Color.GRAY}Old value: {value}"
                    message += f" | New value: {input_data_dict['value']}{Color.DEFAULT}"
                    print(message)
                lines[idx] = line
        content = line_ending.join(lines)

    if input_data_dict['token']:
        __replace_value(
            content, input_data_dict['token'], input_data_dict['value'])
        message = f"{Color.GRAY}Old value: {input_data_dict['token']}"
        message += f" | New value: {input_data_dict['value']}{Color.DEFAULT}"
        print(message)

    __save_config_file(config_file_path, content)


if __name__ == '__main__':
    # Uncomment for unit testing
    # import sys
    # sys.argv = ['<leave blank>',
    #             '--config_file_path',
    #             r'config_file_updater\application.properties',
    #             '--key',
    #             'jdbc.username',
    #             '--value',
    #             'root1']
    # sys.argv = ['<leave blank>',
    #             '--config_file_path',
    #             r'config_file_updater\application.json',
    #             '--key',
    #             'country.tenantConfig.tenantIds',
    #             '--value',
    #             'GGG']
    # sys.argv = ['<leave blank>',
    #             '--config_file_path',
    #             r'config_file_updater\application.json',
    #             '--token',
    #             '<TenantId>',
    #             '--value',
    #             'GGG']
    do_processing()
