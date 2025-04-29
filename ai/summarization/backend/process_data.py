import os
from typing import Any

import psycopg2
from dotenv import load_dotenv
from langchain.chat_models import init_chat_model
from langchain_community.document_loaders import PyPDFLoader
from langchain_core.documents import Document
from langchain_core.prompts import ChatPromptTemplate
from langchain_openai import OpenAIEmbeddings
from langchain_postgres import PGVector
from langchain_text_splitters import RecursiveCharacterTextSplitter
from littlehorse.workflow import Workflow, WorkflowThread

load_dotenv() 
CONNECT = os.getenv("CONNECT")
if not os.getenv("OPENAI_API_KEY"):
    print("Please set the `OPENAI_API_KEY` variable in your `.env` file.")
    os._exit(1)

async def load_pdf(s3_key: str) -> list[Any]:

    loader = PyPDFLoader("./data/" + s3_key)
    pages = []
    for page in loader.lazy_load():
        pages.append(page.page_content)

    return pages #list of str


async def chunk_text(pages: list[Any]) -> list[Any]: #Input and output is a list[str]

    text_splitter = RecursiveCharacterTextSplitter(
        chunk_size=1000,
        chunk_overlap=100, 
    )

    chunks = text_splitter.split_documents([Document(page) for page in pages])
    return [chunk.page_content for chunk in chunks] #list of str 


async def embed_and_store(chunks: list[Any]) -> None:

    embedding_model = OpenAIEmbeddings(model="text-embedding-ada-002")

    vector_store = PGVector(
        embeddings=embedding_model,
        connection=CONNECT,
    )

    document_ids = vector_store.add_documents(documents=[Document(chunk) for chunk in chunks])

##RAG CODE
def retrieve():
        embedding_model = OpenAIEmbeddings(model="text-embedding-ada-002")
        vector_store = PGVector(
            embeddings=embedding_model,
            connection=CONNECT,
        )
        question = """give me a detailed summary of {context}""" #key words specific to field here
        retrieved_docs = vector_store.similarity_search(question)
        docs_content = "\n\n".join(doc.page_content for doc in retrieved_docs)

        return docs_content

async def generate() -> str:
    prompt_text = """    
                You are an AI Assistant tasked with understanding detailed
                information about recent information from text and tables. You are to answer the question based on the 
                context provided to you. You must not go beyond the context given to you.

                Context:
                {context}

                Question:
                {question}
                """

    prompt = ChatPromptTemplate.from_template(prompt_text)

    prompt = prompt.invoke(
        {
            "context": retrieve(),
            "question": "give me a detailed summary of the context"
        }
    )

    llm = init_chat_model("openai:gpt-4o-mini")
    answer = llm.invoke(prompt)

    return answer.content

async def store_summary(summary: str) -> str:
     
     with psycopg2.connect(CONNECT) as conn:

        with conn.cursor() as cur:
            # Create the table if it doesn't exist
            cur.execute(
                """
                CREATE TABLE IF NOT EXISTS summaries (
                    id SERIAL PRIMARY KEY,
                    summary TEXT
                )
                """
            )
            cur.execute(
                """
                INSERT INTO summaries (summary)
                VALUES (%s)
                """,
                (summary,)
            )
        conn.commit()

def get_workflow() -> Workflow:

    def wfSpec(wf: WorkflowThread) -> None:

        s3_id = wf.declare_str("s3-id").required()

        pages = wf.execute("load-pdf", s3_id, timeout_seconds=300, retries=3)
        chunks = wf.execute("chunk-text", pages, retries=3)
        wf.execute("embed-and-store", chunks, retries=3)
        summary = wf.execute("generate-summary", retries=3)
        wf.execute("store-summary", summary, retries=3)

    return Workflow("load-chunk-embed-pdf", wfSpec)
