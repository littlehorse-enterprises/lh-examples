from process_data import load_pdf, chunk_text, embed_and_store, generate, store_summary, get_workflow
import sys
import hashlib
import numpy as np
import os
import time

import psycopg2
from littlehorse.config import LHConfig
from littlehorse.worker import LHTaskWorker
from littlehorse import create_task_def, create_workflow_spec
import littlehorse
import asyncio
from littlehorse.workflow import Workflow, WorkflowThread
from littlehorse.model import RunWfRequest, VariableValue, ScheduleWfRequest
from database import CONNECT

from langchain.chat_models import init_chat_model


config = LHConfig()
client = config.stub()

async def get_pdf_hash(pdf_path):

    with open(pdf_path, "rb") as f:
        pdf_bytes = f.read()
    return hashlib.sha256(pdf_bytes).hexdigest()


async def summarize_all() -> str:
    text_to_summarize = ""
    with psycopg2.connect(CONNECT) as conn:
        with conn.cursor() as cur:
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



def get_summary_workflow() -> Workflow:

    def wfSpec(wf: WorkflowThread) -> None:

        wf.execute("summarize-all", timeout_seconds=100, retries=3)

    return Workflow("summarize-all-text", wfSpec)

    

async def main():

    create_workflow_spec(get_workflow(), config)
    create_workflow_spec(get_summary_workflow(), config)


    progress_trackers = {
            "Astronomy": 0,
            "Oceanography": 0,
            "Evolution": 0,
        }
    
    client.ScheduleWf(ScheduleWfRequest(
            wf_spec_name="summarize-all-text",
            cron_expression="*/5 * * * *"))
    

    count = 0
    
    while count < 3:

        topic = np.random.choice(['Astronomy', 'Oceanography', 'Evolution'])
        pdfs = sorted(os.listdir("./data/" + topic))

        tracker = progress_trackers[topic]
        if tracker >= len(pdfs):
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
        time.sleep(15)
        

if __name__ == "__main__":
    asyncio.run(main())
    
