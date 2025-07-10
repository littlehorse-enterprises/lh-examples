import os
from typing import Any

from dotenv import load_dotenv
from langchain_community.document_loaders import PyPDFLoader
from langchain_core.documents import Document
from langchain_openai import OpenAIEmbeddings
from langchain_postgres import PGVector
from langchain_text_splitters import RecursiveCharacterTextSplitter
from littlehorse.workflow import Workflow, WorkflowThread

load_dotenv() 
CONNECT = os.getenv("CONNECT")
if not os.getenv("OPENAI_API_KEY"):
    print("Please set the `OPENAI_API_KEY` variable in your `.env` file.")
    os._exit(1)


async def load_pdf(s3_key: str) -> list[Any]: # Output is actually a list[str]

    loader = PyPDFLoader("./temp/" + s3_key)
    pages = []
    for page in loader.lazy_load():
        pages.append(page.page_content)

    return pages #list of str


async def chunk_text(pages: list[Any]) -> list[Any]: # Input and output are actually a list[str]

    text_splitter = RecursiveCharacterTextSplitter(
        chunk_size=1000,
        chunk_overlap=100, 
    )

    chunks = text_splitter.split_documents([Document(page) for page in pages])
    return [chunk.page_content for chunk in chunks] #list of str 


async def embed_and_store(chunks: list[Any]) -> None: # Input is actually a list[str]

    embedding_model = OpenAIEmbeddings(model="text-embedding-ada-002")

    vector_store = PGVector(
        embeddings=embedding_model,
        connection=CONNECT,
    )

    vector_store.add_documents(documents=[Document(chunk) for chunk in chunks])


def get_process_data_workflow() -> Workflow:

    def wfSpec(wf: WorkflowThread) -> None:
        s3_id = wf.declare_str("s3-id").required().searchable()

        pages = wf.execute("load-pdf", s3_id, retries=3)
        chunks = wf.execute("chunk-text", pages, retries=3)
        wf.execute("embed-and-store", chunks, retries=3, timeout_seconds=60)

    return Workflow("load-chunk-embed-pdf", wfSpec)