from littlehorse.worker import LHTaskWorker
from process_data import load_pdf, chunk_text, embed_and_store, generate, store_summary
from littlehorse.config import LHConfig
from littlehorse import create_task_def
import littlehorse
import asyncio
from main import summarize_all

config = LHConfig()

create_task_def(load_pdf, "load-pdf", config)
create_task_def(chunk_text, "chunk-text", config)
create_task_def(embed_and_store, "embed-and-store", config)
create_task_def(generate, "generate-summary", config)
create_task_def(store_summary, "store-summary", config)
create_task_def(summarize_all, "summarize-all", config)


workers = [
    LHTaskWorker(load_pdf, "load-pdf", config),
    LHTaskWorker(chunk_text, "chunk-text", config),
    LHTaskWorker(embed_and_store, "embed-and-store", config),
    LHTaskWorker(generate, "generate-summary", config),
    LHTaskWorker(store_summary, "store-summary", config),
    LHTaskWorker(summarize_all, "summarize-all", config)
]

async def start_workers():
    await littlehorse.start(*workers)

if __name__ == "__main__":
    asyncio.run(start_workers())