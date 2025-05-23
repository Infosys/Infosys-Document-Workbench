# ===============================================================================================================#
# Copyright 2025 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

import re
import math
from typing import Any, Dict, List, Sequence
from pydantic import BaseModel, Extra, ValidationError, validator, root_validator
from pydantic import BaseModel as PydanticBaseModel
from pydantic.utils import ROOT_KEY
from pydantic.errors import PydanticValueError
from fastapi.exceptions import RequestValidationError
from pydantic.error_wrappers import ErrorWrapper
from typing import Optional

from common.constants import ErrorCode, ErrorNT


class BaseModel(PydanticBaseModel):
    def __init__(__pydantic_self__, **data: Any) -> None:
        if __pydantic_self__.__custom_root_type__ and data.keys() != {ROOT_KEY}:
            data = {ROOT_KEY: data}
        super().__init__(**data)


class ResponseDataStr(BaseModel):
    response: str
    responseCde: int
    responseMsg: str
    timestamp: str
    responseTimeInSecs: float


class ResponseData(BaseModel):
    response: dict
    responseCde: int
    responseMsg: str
    timestamp: str
    responseTimeInSecs: float


class ResponseDataList(BaseModel):
    response: List[dict]
    responseCde: int
    responseMsg: str
    timestamp: str
    responseTimeInSecs: float


class GenerateEmbeddingRequestData(BaseModel):
    text: str
    modelName: str


class EmbeddingData(BaseModel):
    modelName: Optional[str] = None
    size: Optional[int] = 0
    embedding: Optional[List[float]] = []


class GenerateEmbeddingResponseData(ResponseData):
    response: EmbeddingData


class DocBasedQueryRequestData(BaseModel):
    db_name: List
    question: str
    top_k: int = 0
    temperature: float = 0.5
    from_cache: bool = True
    retriever_only: bool = False
    filter_metadata: dict = {}


class DocBasedTemplateQueryRequestData(DocBasedQueryRequestData):
    content: List


class Parameter(BaseModel):
    temperature: float


class LLMPromptDetail(BaseModel):
    prompt_template: str
    context: str
    query: str
    parameters: Parameter


class LLMResponseDetail(BaseModel):
    response: str
    from_cache: bool


class SourceMetadataDetail(BaseModel):
    chunk_id: str
    bbox_format: str
    bbox: Optional[List]
    doc_name: str


class DocBasedQueryResponseData(BaseModel):
    doc_id: str
    doc_name: str
    answer: str
    chunk_id: str
    page_num: int
    segment_num: int
    # bbox: List
    source_metadata: List[SourceMetadataDetail] = []
    embedding_model_name: str
    distance_metric: str
    top_k: int
    top_k_list: List
    top_k_aggregated: int
    llm_model_name: str
    llm_total_attempts: int
    llm_response: LLMResponseDetail
    llm_prompt: LLMPromptDetail
    version: str
    error: str


class QueryResponseData(BaseModel):
    answers: List[DocBasedQueryResponseData]


class ValidationError(PydanticValueError):
    def __init__(self, error_detail: ErrorNT, **ctx: any) -> None:
        super().__init__(**ctx)
        PydanticValueError.code = str(error_detail.value.code)
        PydanticValueError.msg_template = error_detail.value.message


class DocumentData(BaseModel):
    file_name: str
