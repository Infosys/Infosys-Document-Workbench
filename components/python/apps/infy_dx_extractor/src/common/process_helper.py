# ===============================================================================================================#
# Copyright 2020 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

import os
import re
from common.app_const import *
from common.file_util import FileUtil
from common.bbox_util import BboxUtil


class ProcessHelper:
    @staticmethod
    def get_val(object, key):
        return object[key] if object and key in object else None

    @staticmethod
    def is_range_within_exist_file(merged_page_dir, multi_page_res_bbox):
        found_file = None
        multi_page_res_bbox.sort(key=lambda x: (x[ResProp.PAGE]))
        for file in FileUtil.get_files(merged_page_dir, file_format=FileFormat.JPG):
            try:
                file_name = os.path.splitext(os.path.split(file)[1])[0]
                first_page, last_page = file_name.split("-")
                fpno, fp_bbox = first_page.split("_")
                fpno, fp_bbox = int(fpno.replace(
                    "fp", "")), re.findall(r'[0-9]+', fp_bbox)
                lpno, lp_bbox = last_page.split("_")
                lpno, lp_bbox = int(lpno.replace(
                    "lp", "")), re.findall(r'[0-9]+', lp_bbox)
                loop_count = 0
                for i, mp_r_bbox in enumerate(multi_page_res_bbox):
                    mr_res_pno, mr_res_bbox = int(
                        mp_r_bbox[ResProp.PAGE]), mp_r_bbox[ResProp.BBOX]
                    if mr_res_pno >= fpno and mr_res_pno <= lpno:
                        if (fpno == mr_res_pno) and not (int(mr_res_bbox[1]) >= int(fp_bbox[1]) and int(mr_res_bbox[3]) <= int(fp_bbox[3])):
                            break
                        if (lpno == mr_res_pno) and not (int(mr_res_bbox[1]) >= int(lp_bbox[1]) and int(mr_res_bbox[3]) <= int(lp_bbox[3])):
                            break
                    else:
                        break
                    loop_count = i
                if loop_count == len(multi_page_res_bbox)-1:
                    found_file = file
            except Exception as e:
                pass
        return found_file

    @staticmethod
    def concat_page_bbox(p_bbox_obj):
        return str(p_bbox_obj[ResProp.PAGE])+"_" + "".join([str(int) for int in p_bbox_obj[ResProp.BBOX]])

    @staticmethod
    def get_demerged_bboxes(multipage_bbox_list, merged_bbox):
        demerged_bbox_list = []
        # 1. serialize bbox
        vserialize_bbox_list = BboxUtil.get_vserialize_bbox(
            multipage_bbox_list)
        # print(f"vserialize_bbox_list : {vserialize_bbox_list}")
        # 2. adjust bbox and get points
        adjust_top_point = vserialize_bbox_list[0][1]
        interested_coordinates = BboxUtil.adjust_bbox_and_convert_to_points(
            merged_bbox, adjust_top_point)
        # print(f"interested_coordinates : {interested_coordinates}")
        # 3. get intersection points
        for idx, s_bbox in enumerate(vserialize_bbox_list):
            intersect_points = BboxUtil.get_intersecting_bbox(
                s_bbox, interested_coordinates)
            # 4. get resize points from second
            if idx > 0:
                intersect_bbox = BboxUtil.vresize_points_and_covnert_to_bbox(
                    intersect_points, vserialize_bbox_list[0][3])
            else:
                intersect_bbox = BboxUtil.convert_points_to_bbox(
                    intersect_points)
            # 5. create bbox list
            demerged_bbox_list.append(intersect_bbox)

        # print(f"demerged_bbox_list : {demerged_bbox_list}")
        return demerged_bbox_list

# TODO(): Use it for quick test
# if __name__ == "__main__":
#     import cv2
#     import matplotlib.pyplot as plt
#     def highlight(bbox, image_copy, show=False):
#         l, t, w, h = bbox
#         red = (255, 0, 0)
#         blue = (0, 0, 255)
#         img_copy = cv2.rectangle(image_copy, (l, t), (l+w, t+h),
#                                  color=red if show else blue, thickness=4)
#         if show == True:
#             plt.imshow(img_copy)
#             plt.show()
#

#     pg1_bbox = [0, 2259, 2550, 875]
#     pg2_bbox = [0, 164, 2550, 896]
#     mer_img_interest_bbox = [161, 0, 2389, 1570]

#     result = ProcessHelper.get_demerged_bboxes(
#         [pg1_bbox, pg2_bbox], mer_img_interest_bbox)
#     print(result)
#     for idx, bbox in enumerate(result):
#         page_no = idx+3
#         img = f"D:/INFYGITHUB/ainautosolutions/workbenchlibraries/stackXtractor/work/service_agreement_native/6aba0452-da49-11eb-845a-a0a4c5ec0f26/general-service-agreement.pdf_files/{str(page_no)}.jpg"
#         image_copy = cv2.imread(img)
#         highlight(bbox, image_copy, show=True)
