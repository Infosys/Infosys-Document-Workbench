# ===============================================================================================================#
# Copyright 2021 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#


class ExtractProps:
    def __init__(self):
        self._is_orig_file_is_pdf = False
        self._original_file = None
        self._image_file_path = None
        self._supporting_file_dir = None
        self._output_dir = None
        self._merged_page_no = None
        self._value_type = None
        self._multipage_no_list = []
        self._multipage_bbox_list = []

    @property
    def is_original_file_is_pdf(self):
        return self._is_orig_file_is_pdf

    @is_original_file_is_pdf.setter
    def is_original_file_is_pdf(self, value):
        self._is_orig_file_is_pdf = value

    @property
    def original_file(self):
        return self._original_file

    @original_file.setter
    def original_file(self, value):
        self._original_file = value

    @property
    def image_file_path(self):
        return self._image_file_path

    @image_file_path.setter
    def image_file_path(self, value):
        self._image_file_path = value

    @property
    def supporting_file_dir(self):
        return self._supporting_file_dir

    @supporting_file_dir.setter
    def supporting_file_dir(self, value):
        self._supporting_file_dir = value

    @property
    def output_dir(self):
        return self._output_dir

    @output_dir.setter
    def output_dir(self, value):
        self._output_dir = value

    @property
    def merged_page_no(self):
        return self._merged_page_no

    @merged_page_no.setter
    def merged_page_no(self, value):
        self._merged_page_no = value

    @property
    def value_type(self):
        return self._value_type

    @value_type.setter
    def value_type(self, value):
        self._value_type = value

    @property
    def multipage_no_list(self):
        return self._multipage_no_list

    @multipage_no_list.setter
    def multipage_no_list(self, value):
        self._multipage_no_list = value

    @property
    def multipage_bbox_list(self):
        return self._multipage_bbox_list

    @multipage_bbox_list.setter
    def multipage_bbox_list(self, value):
        self._multipage_bbox_list = value
