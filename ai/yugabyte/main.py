from process_data import load_pdf, chunk_text, embed_and_store, generate, store_summary, get_workflow
import sys
import hashlib
import numpy as np
import os
import time

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
    LHTaskWorker(embed_and_store, "embed-and-store", config),
    LHTaskWorker(generate, "generate-summary", config),
    LHTaskWorker(store_summary, "store-summary", config)
]

async def get_pdf_hash(pdf_path):

    with open(pdf_path, "rb") as f:
        pdf_bytes = f.read()
    return hashlib.sha256(pdf_bytes).hexdigest()

async def register_tasks():

    create_task_def(load_pdf, "load-pdf", config)
    create_task_def(chunk_text, "chunk-text", config)
    create_task_def(embed_and_store, "embed-and-store", config)
    create_task_def(generate, "generate-summary", config)
    create_task_def(store_summary, "store-summary", config)



async def main():

    create_workflow_spec(get_workflow(), config)

    progress_trackers = {
            "Astronomy": 0,
            "Oceanography": 0,
            "Evolution": 0,
        }
    
    count = 0

    while count < 3:

        topic = np.random.choice(['Astronomy', 'Oceanography', 'Evolution'])
        pdfs = sorted(os.listdir("./data/" + topic))

        tracker = progress_trackers[topic]
        if tracker >= len(pdfs):
            print(f"All files in {topic} have been processed.")
            count+= 1
            continue


        pdf = pdfs[tracker]
        file_path = os.path.join(topic, pdf)

        pdf_hash = await get_pdf_hash("./data/" + file_path)

        client.RunWf(RunWfRequest(
                    wf_spec_name="load-chunk-embed-pdf",
                    variables={"s3-id": VariableValue(str=file_path)},
                    id=pdf_hash
            ))
        
        progress_trackers[topic] += 1
        time.sleep(5)

async def start_workers():
    littlehorse.start(*workers)

if __name__ == "__main__":
    asyncio.run(register_tasks())
    asyncio.run(start_workers())
    asyncio.run(main())
