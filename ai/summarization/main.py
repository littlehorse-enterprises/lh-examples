import asyncio
import hashlib
import os
import time

import littlehorse
import numpy as np
import psycopg2
from dotenv import load_dotenv
from langchain.chat_models import init_chat_model
from littlehorse import create_task_def, create_workflow_spec
from littlehorse.config import LHConfig
from littlehorse.model import RunWfRequest, ScheduleWfRequest, VariableValue
from littlehorse.worker import LHTaskWorker
from littlehorse.workflow import Workflow, WorkflowThread
from process_data import (chunk_text, embed_and_store, generate, get_workflow,
                          load_pdf, store_summary)

load_dotenv()

CONNECT = os.getenv("CONNECT")
if not CONNECT:
    print("Please set the `CONNECT` variable in your `.env` file.")

config = LHConfig()
client = config.stub()

async def summarize_all() -> str:
    text_to_summarize = ""
    with psycopg2.connect(CONNECT) as conn:
        with conn.cursor() as cur:
            cur.execute(
                """
                CREATE TABLE IF NOT EXISTS summaries (
                    id SERIAL PRIMARY KEY,
                    summary TEXT
                )
                """
            )
            cur.execute("SELECT * FROM summaries")
            rows = cur.fetchall()
            for row in rows:
                text_to_summarize += row[1] + "\n"
                
    llm = init_chat_model("openai:gpt-4.1-mini")

    promtpt = f""" Give me a detailed summary of the provided text: 

                        {text_to_summarize}""" 

    answer = llm.invoke(promtpt)

    # print(answer.content)
    return answer.content


workers = [
    LHTaskWorker(load_pdf, "load-pdf", config),
    LHTaskWorker(chunk_text, "chunk-text", config),
    LHTaskWorker(embed_and_store, "embed-and-store", config),
    LHTaskWorker(generate, "generate-summary", config),
    LHTaskWorker(store_summary, "store-summary", config),
    LHTaskWorker(summarize_all, "summarize-all", config)
]

async def get_pdf_hash(pdf_path):

    with open(pdf_path, "rb") as f:
        pdf_bytes = f.read()
    return hashlib.sha256(pdf_bytes).hexdigest()

def get_summary_workflow() -> Workflow:

    def wfSpec(wf: WorkflowThread) -> None:

        wf.execute("summarize-all", timeout_seconds=100, retries=3)

    return Workflow("summarize-all-text", wfSpec)

async def main():
    print("Registering TaskDefs")
    create_task_def(load_pdf, "load-pdf", config)
    create_task_def(chunk_text, "chunk-text", config)
    create_task_def(embed_and_store, "embed-and-store", config)
    create_task_def(generate, "generate-summary", config)
    create_task_def(store_summary, "store-summary", config)
    create_task_def(summarize_all, "summarize-all", config)

    print("Registering WfSpecs")
    create_workflow_spec(get_summary_workflow(), config)
    create_workflow_spec(get_workflow(), config)

    progress_trackers = {
        "astronomy": 0,
        "oceanography": 0,
        "evolution": 0,
    }

    client.ScheduleWf(ScheduleWfRequest(
            wf_spec_name="summarize-all-text",
            cron_expression="*/5 * * * *"))
    
    count = 0
    
    while count < 3:

        topic = np.random.choice(['astronomy', 'oceanography', 'evolution'])
        pdfs = sorted(os.listdir("./data/" + topic))

        tracker = progress_trackers[topic]
        if tracker >= len(pdfs):
            count += 1
            continue


        pdf = pdfs[tracker]
        file_path = os.path.join(topic, pdf)

        pdf_hash = await get_pdf_hash("./data/" + file_path)

        print(f"Receieved realtime data about {topic} from {file_path}. Running workflow...")
        client.RunWf(RunWfRequest(
                    wf_spec_name="load-chunk-embed-pdf",
                    variables={"s3-id": VariableValue(str=file_path)},
                    id=pdf_hash
            ))
        
        progress_trackers[topic] += 1
        time.sleep(15)

async def start_workers():
    print("Starting Task Workers...")
    await littlehorse.start(*workers)

async def run_all():
    await asyncio.gather(main(), start_workers())

if __name__ == "__main__":
    asyncio.run(run_all())
