# ===============================================================================================================#
# Copyright 2023 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

import email
import json


class EmailParserService():
    def __init__(self, file_path) -> None:
        self.__result_data = {}
        if file_path.lower().endswith(".json"):
            with open(file_path, 'r') as json_file:
                json_dict = json.load(json_file)
                self.__result_data = json_dict.get('email')
            if not self.is_valid_email():
                raise Exception("Not a email file")
        elif file_path.lower().endswith(".txt"):
            msg = email.message_from_file(open(file_path))
            try:
                if msg['from'] and msg['to'] and msg['subject']:
                    self.__result_data = msg
            except:
                raise Exception("Not a email file")
        else:
            raise Exception("Not a email file")

    def is_valid_email(self):
        result_data = False
        if self.__result_data:
            email_prop = ['from', 'to', 'subject']
            found_prop = [x for x in email_prop if x in self.__result_data]
            if len(found_prop) == len(email_prop):
                result_data = True
        return result_data

    def get_from(self):
        result = self.__result_data['from']
        result = result[0] if isinstance(result, list) else result
        return result.split("<")[0].strip()

    def get_from_id(self):
        result = self.__result_data['from']
        result = result[0] if isinstance(result, list) else result
        return result.split("<")[1].strip()[:-1]

    def get_to(self):
        result = self.__result_data['to']
        result = result[0] if isinstance(result, list) else result
        return result.split("<")[0].strip()

    def get_to_id(self):
        result = self.__result_data['to']
        result = result[0] if isinstance(result, list) else result
        return result.split("<")[1].strip()[:-1]

    def get_subject(self):
        return self.__result_data['subject'].strip()

    def get_cc(self):
        return self.__result_data['cc']

    def get_body(self):
        result = self.__result_data.get('body_txt')
        return result if result else self.__result_data.get_payload()

    def get_received_date(self):
        return self.__result_data.get('sent_date')
