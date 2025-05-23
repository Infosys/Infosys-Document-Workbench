# ===============================================================================================================#
# Copyright 2020 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

TESSA_PATH = "tesseract_path"
POPPLER_PATH = "poppler_path"
EMPTY = ""
# Lable
L_WORD_DIR = 'work_dir'
L_DEFAULT = 'DEFAULT'

SUPPORTING_DIR_SUFFIX = "_files"
BBOX_FILE_SUFFIX = "_bbox"
MERGED_IMG_DIR_NAME = "merged_images"
OUTPUT_FILE_SUFFIX = 'output_file_suffix'


class OcrTypeLabel(object):
    TESSERACT = 'tesseract'
    ABBYY = 'abbyy'
    AZURE_READ = 'azure_read'
    AZURE_OCR = 'azure_ocr'


class SessionKey(object):
    EXT_CONFIG_FILE_DATA = 'extractor_config_file_data'
    MASTER_CONFIG_FILE_DATA = 'master_config_file_data'


class ConfProp(object):
    PROFILE = 'profile'
    ENABLED = 'enabled'
    IN = 'input'
    OUT = 'output'
    WORK_LOCATION = 'workLocation'
    LOCATION = 'location'
    CATEGORY = 'category'
    DOC_TYPE = 'documentType'
    OCR_TYPE = 'ocrType'
    FREE_TXT = 'freeText'
    ATTRS = 'attributes'
    NAMED_REG = 'namedRegions'
    VAL_TYPE = 'valueType'
    VAL_DEF = 'valueRegionDefinition'
    ADDITIONAL_PROP = 'additionalProperties'
    TABLE_DEF = 'tableOrganization'
    LABEL = 'label'
    PAGE = 'page'
    ATTRS_FROM_VAL = 'attributesFromValue'
    NAME = 'name'
    VALUE = 'value'
    PATTERN = 'pattern'
    RULE_CLASS = 'ruleClassName'
    SAVE_COPY_WITH = 'saveCopyWith'
    REG_BBOX = "regionBbox"
    REG_LABEL = "regionLabel"
    ANC_BBOX = "anchorBbox"
    DOC_TEMPLATES = "documentTemplates"
    SUBTRACT_REG = "subtractRegions"
    REG_DEFINITION = "regionDefinition"
    ANNOTATE_OUT_DIR = "annotateOutDir"
    ATTRS_DEF = "attributeDefinitions"
    ATTRS_RULES = "attributeRules"
    ATTRS_EXT_PROVIDER = "attributeExternalProvider"
    SERV_NAME = "service_name"
    SERV_VER = "service_version"
    RECORDS = "records"


class ResProp(object):
    PAGE = "page"
    BBOX = "bbox"
    REGIONS = "regions"
    REG_BBOX = "regionBBox"
    DEBUG_INFO = "debugInfo"
    LABEL = "label"
    ANC_INFO = "anchor_info"
    ANC_TXT_BBOX = "anchorTextBBox"

    ID = "id"
    ATTR_NAME = "name"
    VALS = "values"
    MSG = "message"
    ERROR = "error"
    WARN = "warning"
    INFO = "info"
    TYPE = "type"
    TXT_OBJ = "text_obj_list"
    SLCTMRK_OBJ = "selection_mark_obj_list"
    TAB_OBJ = "table_obj"
    bbox = "bounding_box"
    HNDWRTN = "handwritten"
    TXT = "text"
    STATE = "state"
    ADD_DATA = "additional_data"
    RD_FND = "rd_found"
    RD_NT_FND = "rd_not_found"
    OCR_TOOL = "ocr_tool"
    OCR_IN_TYPE = "ocr_tool_input_file_type"
    EXT_TECH = "extraction_technique"
    DEF = "default"
    CONF = "confidencePct"
    SLCTD = "selected"


class ProcessState(object):
    NEW = '_new'
    INPROGRESS = '_inprogress'
    COMPLETED = '_completed'
    ERROR = '_error'


class FileFormat(object):
    HOCR = "*.hocr"
    XML = "*.xml"
    PDF = "*.pdf"
    CSV = "*.csv"
    TXT = "*.txt"
    ANY = "*.*"
    JPG = "*.jpg"
    JSON = "*.json"


class OcrFileExtension(object):
    HOCR = ".hocr"
    XML = ".xml"


class FileExtension(object):
    PDF = ".pdf"
    JPG = ".jpg"
    TXT = ".txt"
    CSV = ".csv"
    JSON = ".json"


class DocType(object):
    SEARCHABLE_PDF = "searchablePdf"
    SCANNED_PDF = "scannedPdf"
    IMAGE = "image"
    TEXT = "txt"


class ValType(object):
    B_LESS_TABLE = 'borderlessTable'
    B_TABLE = 'table'
    LANG = 'lang'
    TXT = 'text'
    CHECKBOX = 'checkbox'
    RADIO = 'radio'
    PARAGRAPH = 'paragraph'
    SIGNATURE = 'signature'
    HANDWRITTEN = 'handwritten'
