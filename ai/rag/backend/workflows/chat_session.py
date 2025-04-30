from langchain.chat_models import init_chat_model
from littlehorse.workflow import Comparator, Workflow, WorkflowThread
from process_data import retrieve
from typing import Any

llm = init_chat_model(model="gpt-4o-mini")

async def invoke_ai(history: list[Any], context: str) -> str:
    
    question = history[-1]

    context_chunks = await retrieve()
    context = "\n\n".join(context_chunks)
    context = context.join("\n\n" + history[:-1])



    prompt = f"""
        You are an assistant helping answer questions based on the following PDF content:

        {context}

        User question: {question}
        Answer:
        """
        
    answer = llm.invoke(prompt)
    print(f"\nAssistant: {answer.content}\n")


async def post_webhook() -> None:
    return None


def chat_workflow() -> Workflow:

    def wfSpec(wf: WorkflowThread) -> None:
        # Input Variables
        initial_user_message = wf.declare_str("initial-user-message").required()
        history = wf.declare_json_arr("chat-history", [])

        history.assign(history.add(initial_user_message))
        
        context = wf.execute("retrieve-context", initial_user_message, timeout_seconds=100)
        
        answer = wf.execute("invoke-ai", history, context, timeout_seconds=100)
        history.assign(history.add(answer))

        wf.execute("post-webhook", history, timeout_seconds=100)

        def chat_loop(loop_body: WorkflowThread) -> None:
            user_message = loop_body.wait_for_event("user-message")
            history.assign(history.add(user_message))
            
            context = loop_body.execute("retrieve-context", user_message, timeout_seconds=100)
            
            answer = loop_body.execute("invoke-ai", history, context, timeout_seconds=100)
            history.assign(history.add(answer))

            loop_body.execute("post-webhook", history, timeout_seconds=100)



        wf.do_while(wf.condition(True, Comparator.EQUALS ,True), chat_loop)

        

    return Workflow("chat-with-llm", wfSpec)