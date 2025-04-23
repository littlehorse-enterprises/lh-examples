from process_data import load_pdf, chunk_text, embed_and_store, get_workflow
import sys
import hashlib

from littlehorse.config import LHConfig
from littlehorse.worker import LHTaskWorker
from littlehorse import create_task_def, create_workflow_spec
import littlehorse
import asyncio
from littlehorse.workflow import Workflow
from littlehorse.model import RunWfRequest, VariableValue

config = LHConfig()
client = config.stub()

workers = [
    LHTaskWorker(load_pdf, "load-pdf", config),
    LHTaskWorker(chunk_text, "chunk-text", config),
    LHTaskWorker(embed_and_store, "embed-and-store", config)
]

async def get_pdf_hash(pdf_path):

    with open(pdf_path, "rb") as f:
        pdf_bytes = f.read()
    return hashlib.sha256(pdf_bytes).hexdigest()

async def main():
    file_path = "book.pdf"
    pdf_hash = await get_pdf_hash("./data/" + file_path)

    create_task_def(load_pdf, "load-pdf", config, timeout=100),
    create_task_def(chunk_text, "chunk-text", config),
    create_task_def(embed_and_store, "embed-and-store", config)

    create_workflow_spec(get_workflow(), config)

    client.RunWf(RunWfRequest(
            wf_spec_name="load-chunk-embed-pdf",
            variables={"s3-id": VariableValue(str=file_path)},
            id=pdf_hash
    ))

    await littlehorse.start(workers[0], workers[1], workers[2])

if __name__ == "__main__":
    asyncio.run(main())