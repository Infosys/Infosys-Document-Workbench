# ===============================================================================================================#
# Copyright 2020 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#


AZURE_SUB_REQ_LOG = "azure_read_submit_req_log_path"
AZURE_READ_API_DELAY = "azure_read_api_delay_sec"
PREPROCESS_CONFIG_FILE_PATHS = "preprocess_config_file_paths"
MAX_WORKERS = "max_workers"
EMPTY = ""
# Lable
L_WORD_DIR = 'work_dir'
L_DEFAULT = 'DEFAULT'
SERVICE_NAME = 'service_name'
VERSION = 'service_version'
OUTPUT_FILE_SUFFIX = 'output_file_suffix'


class OcrType(object):
    ABBYY = "abbyy"
    AZURE_READ = "azure_read"
    AZURE_OCR = "azure_ocr"
    TESSERACT = "tesseract"


class OcrTypeLabel(object):
    TESSERACT = 'tesseract'
    ABBYY = 'abbyy'


class ImageFileSizeMb(object):
    AZURE_READ = 'max_size_limit_mb_azure_read'
    AZURE_OCR = 'max_size_limit_mb_azure_ocr'
    TESSERACT = 'max_size_limit_mb_tesseract'
    ABBYY = 'max_size_limit_mb_abbyy'


class ImagePixel(object):
    AZURE_READ = 'min_max_dimension_azure_read'
    AZURE_OCR = 'min_max_dimension_azure_ocr'
    TESSERACT = 'min_max_dimension_tesseract'
    ABBYY = 'min_max_dimension_abbyy'


class ConfProp(object):
    ENABLED = 'enabled'
    IN = 'input'
    OUT = 'output'
    WORK_LOCATION = 'work_folder_path'
    LOCATION = 'location'
    DOC_TYPE = 'documentType'
    OCR_TYPE = 'ocrType'
    OCR_PROVIDER_ENABLED_DICT = 'ocrDataServiceProviderEnabled'
    PROFILE = 'profile'
    CATEGORY = 'category'
    DEFAULT_SETTINGS = 'defaultSettings'
    OCR_PROVIDER_SETTINGS = 'ocrProviderSettings'
    DOC_TEMPLATES = 'documentTemplates'

    INPUT_ROOT_FOLDER = 'input_path_root'
    OCR_TOOL = 'ocr_tool'
    RECORDS = 'records'
    OUTPUT_ROOT_FOLDER = 'output_path_root'
    INPUT_FILE_TYPE = 'input_file_type'
    OUTPUT_LOCATION = 'output_location'
    INPUT_FILE_PATHS = 'input_file_paths'
    PAGES = 'pages'


class ProcessState(object):
    NEW = '_new'
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
    XML = ".xml"
    ZIP = ".zip"


class DocType(object):
    SEARCHABLE_PDF = "searchablePdf"
    SCANNED_PDF = "scannedPdf"
    IMAGE = "image"
    TEXT = "txt"
    HOCR = "hocr"
    XML = "xml"
