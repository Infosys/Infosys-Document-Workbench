# ===============================================================================================================#
# Copyright 2023 Infosys Ltd.                                                                                    #
# Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at  #
# http://www.apache.org/licenses/                                                                                #
# ===============================================================================================================#

import getpass
import os
from common.common_util import Singleton
try:
    from haystack.document_stores import InMemoryDocumentStore
    from haystack.nodes import BM25Retriever
    from haystack.nodes.reader import FARMReader
    from haystack.pipelines import ExtractiveQAPipeline
    from haystack.pipelines.standard_pipelines import TextIndexingPipeline

    from haystack.document_stores.base import BaseDocumentStore
    from haystack.nodes import PreProcessor, TextConverter
    from haystack.pipelines.base import Pipeline
    from haystack.pipelines.standard_pipelines import BaseStandardPipeline
    from haystack.nodes import (TextConverter, FileTypeClassifier, PDFToTextConverter,
                                MarkdownConverter, DocxToTextConverter, PreProcessor)
except:
    pass
    # haystack installation is required.


class DocumentIndexingPipeline(BaseStandardPipeline):
    def __init__(
        self,
        document_store: BaseDocumentStore
    ):
        """
        Initialize a basic Pipeline that converts files into Documents and indexes them into a DocumentStore.

        :param document_store: The DocumentStore to index the Documents into.
        """

        self.document_store = document_store
        self.preprocessor = PreProcessor()
        file_type_classifier = FileTypeClassifier()

        self.text_converter = TextConverter()
        # pdf_converter = PDFToTextConverter()
        self.docx_converter = DocxToTextConverter()
        # md_converter = MarkdownConverter()

        self.pipeline = Pipeline()
        self.pipeline.add_node(component=file_type_classifier,
                               name="FileTypeClassifier", inputs=["File"])

        self.pipeline.add_node(component=self.text_converter, name="TextConverter",
                               inputs=["FileTypeClassifier.output_1"])
        # self.pipeline.add_node(component=pdf_converter, name="PdfConverter",
        #                        inputs=["FileTypeClassifier.output_2"])
        # self.pipeline.add_node(component=md_converter, name="MarkdownConverter",
        #                        inputs=["FileTypeClassifier.output_3"])
        self.pipeline.add_node(component=self.docx_converter, name="DocxConverter",
                               inputs=["FileTypeClassifier.output_4"])

        # self.pipeline.add_node(component=preprocessor, name="Preprocessor",
        #                        inputs=["TextConverter", "PdfConverter", "MarkdownConverter", "DocxConverter"])

        self.pipeline.add_node(component=self.preprocessor, name="Preprocessor",
                               inputs=["TextConverter", "DocxConverter"])
        self.pipeline.add_node(component=self.document_store,
                               name="DocumentStore", inputs=["Preprocessor"])

    def run(self, file_path):
        return self.pipeline.run(file_paths=[file_path])

    def run_batch(self, file_paths):
        return self.pipeline.run_batch(file_paths=file_paths)


class QuestionAnsweringService(metaclass=Singleton):
    def __init__(self) -> None:
        # create document store and index documents
        self.document_store = InMemoryDocumentStore(use_bm25=True)
        # document_store.add_documents(your_documents)
        retriever = BM25Retriever(self.document_store)
        SP_MODEL_PATH = fr"D:\ML\deepset\roberta-base-squad2"
        reader = FARMReader(model_name_or_path=SP_MODEL_PATH, use_gpu=False)
        self.pipe = ExtractiveQAPipeline(reader, retriever)

    def get_answer(self, file, query):
        # read the file and convert to document format
        indexing_pipeline = TextIndexingPipeline(self.document_store)
        indexing_pipeline = DocumentIndexingPipeline(self.document_store)
        indexing_pipeline.run_batch(file_paths=[file])
        # perform the search and get the top answer
        prediction = self.pipe.run(
            query=query,
            params={
                "Retriever": {"top_k": 5},
                "Reader": {"top_k": 1}
            }
        )
        answer = prediction["answers"][0].answer if len(
            prediction["answers"]) > 0 else "No answer found."
        return answer
