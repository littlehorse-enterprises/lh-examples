import os
from typing import Any

from dotenv import load_dotenv
from langchain.chat_models import init_chat_model
from langchain_openai import OpenAIEmbeddings
from langchain_postgres import PGVector
from littlehorse.workflow import Comparator, Workflow, WorkflowThread

load_dotenv() 
CONNECT = os.getenv("CONNECT")
if not os.getenv("OPENAI_API_KEY"):
    print("Please set the `OPENAI_API_KEY` variable in your `.env` file.")
    os._exit(1)

llm = init_chat_model(model="gpt-4o-mini")

async def invoke_ai(history: list[Any], context: str) -> str: # Input is actually a list[str]
    
    prompt_text = f"""Human: You are an International Trade and Tariff Regulations expert who can answer questions only based on the context provided below.

                    Answer the question STRICTLY based on the US Tariff data context provided in html tag below.

                    Do not assume or retrieve any information outside of the context.

                    Keep the answer concise, using three sentences maximum for text responses.

                    Think step-by-step to ensure accuracy based on the context provided.

                    If multiple results exist (e.g., multiple tariff rates, product categories, exemptions), present them as a bulleted list, numbered list, or in a table.

                    If numerical comparisons or trends are evident in the context, you may present a simple chart or table to make the information easier to understand.

                    Use tables for structured data (e.g., tariff codes, product descriptions, rates) and simple bar or line charts for comparisons (e.g., tariff rate trends).

                    Do not add any extra commentary, apologies, summaries, or retrievals beyond the provided context.

                    Do not start the response with "Here is a summary" or similar phrasing.

                    If the context is empty, respond with: The context provided to me is empty.

                    Always prioritize clarity and relevance in the format you choose (plain text, table, or chart).

                    Here is the context:
                    <context>
                    {context}
                    </context>

                    
                    Chat History (0 or odd indexes is user, even indexes are the AI's response):
                    {history}
                """
    
    answer = llm.invoke(prompt_text)

    return answer.content


async def retrieve(question: str) -> str:
    embedding_model = OpenAIEmbeddings(model="text-embedding-ada-002")
    vector_store = PGVector(
        embeddings=embedding_model,
        connection=CONNECT,
    )
    question = "tarrifs, economy, national, US, country, impact".join("\n\n" + question)
    retrieved_docs = vector_store.similarity_search(question)
    docs_content = "\n\n".join(doc.page_content for doc in retrieved_docs)

    return docs_content


async def post_webhook(history: list[Any]) -> None: # Input is actually a list[str]
    return None

def chat_workflow() -> Workflow:

    def wfSpec(wf: WorkflowThread) -> None:
        # Input Variables
        initial_user_message = wf.declare_str("initial-user-message").required()
        history = wf.declare_json_arr("chat-history")
        history.assign([])
        
        history.assign(history.add(initial_user_message))
        
        context = wf.execute("retrieve-context", initial_user_message)
        
        answer = wf.execute("invoke-ai", history, context, timeout_seconds=100)
        history.assign(history.add(answer))

        wf.execute("post-webhook", history)

        def chat_loop(loop_body: WorkflowThread) -> None:
            user_message = loop_body.wait_for_event("user-message")
            history.assign(history.add(user_message))
            
            context = loop_body.execute("retrieve-context", user_message)
            
            answer = loop_body.execute("invoke-ai", history, context, timeout_seconds=100)
            history.assign(history.add(answer))

            loop_body.execute("post-webhook", history)

        wf.do_while(wf.condition(True, Comparator.EQUALS ,True), chat_loop)

    return Workflow("chat-with-llm", wfSpec)