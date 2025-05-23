# ===============================================================================================================#
# Copyright 2023 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

from enum import Enum
from collections import namedtuple

ErrorNT = namedtuple('Error', ['code', 'message'])


class ErrorCode(Enum):
    MAX_QTY_LESS_THAN_MIN_QTY = ErrorNT(
        1001, 'maxQty must be not be less than minQty')

    PIPELINE_NAME_ACCEPTS = ErrorNT(
        1016, 'Pipeline name should be in smaller case and can accept only a-z, -, numbers e.g my-pipeline-testing-001'
    )
    PIPELINE_INPUTARTIFACTS = ErrorNT(
        1017, 'Input Artifacts not empty')
