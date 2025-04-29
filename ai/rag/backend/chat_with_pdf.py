from process_data import retrieve
from langchain.chat_models import init_chat_model

from littlehorse.workflow import Workflow, WorkflowThread

llm = init_chat_model(model="gpt-4o-mini")


async def invoke_chat(question: str, context: str) -> str:

    # print("How can I help? (or 'exit')\n")

    context_chunks = await retrieve()
    context = "\n\n".join(context_chunks)


    prompt = f"""
        You are an assistant helping answer questions based on the following PDF content:

        {context}

        User question: {question}
        Answer:
        """
        
    answer = llm.invoke(prompt)
    print(f"\nAssistant: {answer.content}\n")
    

def chat_workflow() -> Workflow:

    def wfSpec(wf: WorkflowThread) -> None:


        input = wf.declare_str("user-question").required()

        context = wf.execute("retrieve-context", timeout_seconds=100)
        wf.execute("invoke-chat", input, context, timeout_seconds=100)
        

    return Workflow("chat-with-llm", wfSpec)