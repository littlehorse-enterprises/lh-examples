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

def get_object_size(obj) -> str:
    """Returns the size of an object in a human-readable format."""
    size_bytes = sys.getsizeof(obj)
    if size_bytes < 1024:
        return f"{size_bytes} bytes"
    elif size_bytes < 1024 ** 2:
        return f"{size_bytes / 1024:.2f} KB"
    else:
        return f"{size_bytes / (1024 ** 2):.2f} MB"


async def main():
    file_path = "Astronomy/s41550-025-02480-3.pdf"

    pdf_hash = await get_pdf_hash("./data/" + file_path)

    create_task_def(load_pdf, "load-pdf", config, timeout=100),
    create_task_def(chunk_text, "chunk-text", config),
    create_task_def(embed_and_store, "embed-and-store", config)

    create_workflow_spec(get_workflow(), config)

    request = RunWfRequest(
                wf_spec_name="load-chunk-embed-pdf",
                variables={"s3-id": VariableValue(str=file_path)},
                id=pdf_hash
        )
    print(request)
    client.RunWf(request)

    await littlehorse.start(workers[0], workers[1], workers[2])

if __name__ == "__main__":
    asyncio.run(main())