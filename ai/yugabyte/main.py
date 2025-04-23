from RAG import RAG_Agent
import os
import time
import random

from langchain.chat_models import init_chat_model
from process_data import process, load_pdf, chunk_text, embed_and_store

ASTRONOMY = "./data/Astronomy"
OCEANOGRAPHY = "./data/Oceanography"
EVOLUTION = "./data/Evolution"

if __name__ == "__main__":

    Astronomy_agent = RAG_Agent(
        file_path=ASTRONOMY,
        collection_name="Astronomy"
    )

    Oceanography_agent = RAG_Agent(
        file_path=OCEANOGRAPHY,
        collection_name="Oceanography"
    )

    Evolution_agent = RAG_Agent(
        file_path=EVOLUTION,
        collection_name="Evolution"
    )



    agents = [
        ("Astronomy", ASTRONOMY, Astronomy_agent),
        ("Oceanography", OCEANOGRAPHY, Oceanography_agent),
        ("Evolution", EVOLUTION, Evolution_agent),
    ]


    progress_trackers = {
        "Astronomy": 0,
        "Oceanography": 0,
        "Evolution": 0,
    }

    summaries = []

    text_to_summarize = ""

    while True:  
        topic, path, agent = random.choice(agents)
        print(f"Selected topic: {topic}")

        pdfs = sorted(os.listdir(path))
        tracker = progress_trackers[topic]

        if tracker >= len(pdfs):
            print(f"All files in {topic} have been processed.")
            continue

        pdf = pdfs[tracker]
        file_path = os.path.join(path, pdf)
        print(f"Processing {file_path}")
        process(file_path, topic) #--> yuga

        text_to_summarize = text_to_summarize + agent.generate() + "\n\n" #auto called 


        llm = init_chat_model("openai:gpt-4.1")
        
        promtpt = f""" Give me a detailed summary of the provided text: 
        
                        {text_to_summarize}""" 

        answer = llm.invoke(promtpt)
        print(answer.content)


        progress_trackers[topic] += 1

        time.sleep(random.randint(5, 20))