import asyncio
import hashlib
import os

import littlehorse
from dotenv import load_dotenv
from langchain.chat_models import init_chat_model
from littlehorse import create_task_def, create_workflow_spec
from littlehorse.config import LHConfig
from littlehorse.model import (PutExternalEventDefRequest, RunWfRequest,
                               VariableValue)
from littlehorse.worker import LHTaskWorker
from workflows.chat_session import (chat_workflow, invoke_ai, post_webhook,
                                    retrieve)
from workflows.process_data import (chunk_text, embed_and_store,
                                    get_process_data_workflow, load_pdf)

config = LHConfig()
client = config.stub()

load_dotenv()
CONNECT = os.getenv("CONNECT")

workers = [
    LHTaskWorker(load_pdf, "load-pdf", config),
    LHTaskWorker(chunk_text, "chunk-text", config),
    LHTaskWorker(embed_and_store, "embed-and-store", config),
    LHTaskWorker(retrieve, "retrieve-context", config),
    LHTaskWorker(invoke_ai, "invoke-ai", config),
    LHTaskWorker(post_webhook, "post-webhook", config)
    
]

llm = init_chat_model("openai:gpt-4o-mini")

create_task_def(load_pdf, "load-pdf", config)
create_task_def(chunk_text, "chunk-text", config)
create_task_def(embed_and_store, "embed-and-store", config)

create_workflow_spec(get_process_data_workflow(), config)

client.PutExternalEventDef(PutExternalEventDefRequest(
    name="user-message",
))
create_task_def(retrieve, "retrieve-context", config)
create_task_def(invoke_ai, "invoke-ai", config)
create_task_def(post_webhook, "post-webhook", config)

create_workflow_spec(chat_workflow(), config)

async def start_workers():
    await littlehorse.start(*workers)

async def process_data():
    file_paths = ["GMO Quarterly Letter.pdf", "Tax Insights.pdf"]
    for file_path in file_paths:
        pdf_hash = hashlib.sha256(file_path.encode()).hexdigest()
        client.RunWf(RunWfRequest(
                        wf_spec_name="load-chunk-embed-pdf",
                        variables={"s3-id": VariableValue(str=file_path)},
                        id=pdf_hash
                ))
        
async def start_and_process():
    await asyncio.gather(start_workers(), process_data())

if __name__ == "__main__":
    asyncio.run(start_and_process())
