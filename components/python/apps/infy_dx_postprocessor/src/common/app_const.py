# ===============================================================================================================#
# Copyright 2020 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

L_DEFAULT = 'DEFAULT'
SERVICE_NAME = 'service_name'
VERSION = 'service_version'
OUTPUT_FILE_SUFFIX = 'output_file_suffix'


class ProcessState(object):
    INPROGRESS = '_inprogress'
    COMPLETED = '_completed'
    ERROR = '_error'


class FileFormat(object):
    HOCR = "hocr"
    PDF = "pdf"
    CSV = "csv"
    TXT = "txt"
    ANY = "*"


class FileExtension(object):
    HOCR = ".hocr"
    PDF = ".pdf"
    JPG = ".jpg"
    TXT = ".txt"
    CSV = ".csv"
