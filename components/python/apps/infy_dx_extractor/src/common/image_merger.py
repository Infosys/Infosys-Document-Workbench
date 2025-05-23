# ===============================================================================================================#
# Copyright 2020 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

import cv2
import os
from common.file_util import FileUtil
from common.app_const import *
from common.ainauto_logger_factory import AinautoLoggerFactory
logger = AinautoLoggerFactory().get_logger()
REGION_BBOX = {"bbox": [], "page": 1}


class ImageMerger:
    def __init__(self, image_file_path, output_path=None):
        self.img_file_path = image_file_path
        self.output_path = output_path

    def merge_images_for(self, region_bbox=[REGION_BBOX]):
        merged_img_path = error = None
        try:
            cropped_img_list = []
            region_bbox.sort(key=lambda x: (x[ConfProp.PAGE]))
            output_path_temp = FileUtil.create_dirs_if_absent(
                self.output_path if self.output_path else "{}/merged_images".format(self.img_file_path))
            for page_bbox in region_bbox:
                l, t, w, h = page_bbox[ResProp.BBOX]
                page_img = cv2.imread(
                    self.img_file_path+"/"+str(page_bbox[ConfProp.PAGE])+".jpg")
                crop_img = page_img[t:t+h, l:l+w]
                new_img_name = self._concat_page_bbox(page_bbox)
                new_img_path = output_path_temp+"/" + new_img_name+".jpg"
                cv2.imwrite(new_img_path, crop_img)
                cropped_img_list.append(new_img_path)
            cat_img = self._vconcat_resize(cropped_img_list)
            first_page = self._concat_page_bbox(region_bbox[0])
            last_page = self._concat_page_bbox(region_bbox[len(region_bbox)-1])
            merged_img_path = "{}/fp{}-lp{}.jpg".format(
                output_path_temp, first_page, last_page)
            cv2.imwrite(merged_img_path, cat_img)
            FileUtil.remove_files(cropped_img_list)
        except Exception as e:
            logger.error(e)
            error = e.args[0]
        return {"output": {"imagePath": merged_img_path}, "error": error}

    def _concat_page_bbox(self, p_bbox_obj):
        l, t, w, h = [str(int) for int in p_bbox_obj[ResProp.BBOX]]
        return "{}_l{}t{}w{}h{}".format(str(p_bbox_obj[ConfProp.PAGE]), l, t, w, h)

    def _vconcat_resize(self, img_path_list, interpolation=cv2.INTER_CUBIC):
        # take minimum width
        img_list = []
        for image in img_path_list:
            if not os.path.exists(image):
                raise Exception("Provide valid image file paths.")
            img_list.append(cv2.imread(image))

        w_min = min(img.shape[1] for img in img_list)
        # resizing images
        im_list_resize = [cv2.resize(img, (w_min, int(
            img.shape[0] * w_min / img.shape[1])), interpolation=interpolation) for img in img_list]
        return cv2.vconcat(im_list_resize)
