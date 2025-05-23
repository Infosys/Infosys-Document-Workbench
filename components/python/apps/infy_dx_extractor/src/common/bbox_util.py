# ===============================================================================================================#
# Copyright 2020 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

class BboxUtil:
    """Class to work with bboxes"""
    @classmethod
    def get_intersecting_bbox(cls, bbox_1, bbox_2) -> list:
        """Returns intersecting bbox of two bboxes"""
        x1, y1, x2, y2 = bbox_1
        x3, y3, x4, y4 = bbox_2
        # max of left-top
        x_inter1 = max(x1, x3)
        y_inter1 = max(y1, y3)
        # min of right-bottom
        x_inter2 = min(x2, x4)
        y_inter2 = min(y2, y4)
        # min of left-top and max of right-bottom
        return [min(x_inter1, x_inter2), min(y_inter1, y_inter2),
                max(x_inter1, x_inter2), max(y_inter1, y_inter2)]

    @classmethod
    def get_vserialize_bbox(cls, bbox_list) -> list:
        """Vertically concatinating the [x,y,w,h] format bboxes."""
        serialize_bbox_list = []
        for bbox in bbox_list:
            x1, y1, w, h = bbox
            if serialize_bbox_list:
                y1 = y1+serialize_bbox_list[-1][3]
            serialize_bbox_list.append([x1, y1, x1+w, y1+h])
        return serialize_bbox_list

    @classmethod
    def vresize_points_and_covnert_to_bbox(cls, points, top) -> list:
        """Vertically resize [x1,y1,x2,y2] points."""
        x1, y1, x2, y2 = points
        y1 = abs(y1-top)
        y2 = abs(y2-top)
        return [x1, y1, abs(x2-x1), y2]

    @classmethod
    def adjust_bbox_and_convert_to_points(cls, bbox, top=0) -> list:
        """Adjusting bbox and then converting to points. Currently it accepts adjust point of 'top'."""
        x, y, w, h = bbox
        y += top
        return [x, y, x+w, y+h]

    @classmethod
    def convert_points_to_bbox(cls, points):
        x1, y1, x2, y2 = points
        return [x1, y1, abs(x2-x1), abs(y2-y1)]
