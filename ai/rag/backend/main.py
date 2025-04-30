import os
import asyncio
import littlehorse
from littlehorse.workflow import Workflow, WorkflowThread
from littlehorse.config import LHConfig
from littlehorse import create_workflow_spec, create_task_def
from littlehorse.model import RunWfRequest, ScheduleWfRequest, VariableValue
from littlehorse.worker import LHTaskWorker

from langchain.chat_models import init_chat_model
from langchain_openai import OpenAIEmbeddings
from langchain_postgres import PGVector
from langchain.memory import ConversationBufferMemory
from langchain.schema import SystemMessage, HumanMessage

from dotenv import load_dotenv

from workflows.process_data import get_process_data_workflow, load_pdf, chunk_text, embed_and_store, generate, store_summary, retrieve
from workflows.chat_session import chat_workflow, invoke_ai, post_webhook

config = LHConfig()
client = config.stub()

load_dotenv()
CONNECT = os.getenv("CONNECT")

workers = [
    LHTaskWorker(load_pdf, "load-pdf", config),
    LHTaskWorker(chunk_text, "chunk-text", config),
    LHTaskWorker(embed_and_store, "embed-and-store", config),
    LHTaskWorker(generate, "generate-summary", config),
    LHTaskWorker(store_summary, "store-summary", config),
    LHTaskWorker(retrieve, "retrieve-context", config),
    LHTaskWorker(invoke_ai, "invoke-ai", config),
    LHTaskWorker(post_webhook, "post-webhook", config)
    
]

llm = init_chat_model("openai:gpt-4o-mini")

create_task_def(load_pdf, "load-pdf", config)
create_task_def(chunk_text, "chunk-text", config)
create_task_def(embed_and_store, "embed-and-store", config)
create_task_def(generate, "generate-summary", config)
create_task_def(store_summary, "store-summary", config)

create_workflow_spec(get_process_data_workflow(), config)


create_task_def(retrieve, "retrieve-context", config)
create_task_def(invoke_ai, "invoke-ai", config)
create_task_def(post_webhook, "post-webhook", config)

create_workflow_spec(chat_workflow(), config)


async def start_workers():
    await littlehorse.start(*workers)

async def process_data():

    file_path = "evolution/s41467-021-23804-7.pdf"

    client.RunWf(RunWfRequest(
                    wf_spec_name="load-chunk-embed-pdf",
                    variables={"s3-id": VariableValue(str=file_path)},
                    # id=pdf_hash
            ))
    

async def async_input(prompt: str = ""):
    return await asyncio.to_thread(input, prompt)
        
async def start_and_process():
    await asyncio.gather(start_workers(), process_data())

if __name__ == "__main__":
    asyncio.run(start_and_process())
