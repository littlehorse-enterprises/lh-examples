from model import llm
from langchain.schema import HumanMessage, SystemMessage
from utils.worker_registry import worker
from utils.constants import TaskDefNames
from utils.logger import logger


@worker(TaskDefNames.ORCHESTRATE_TOPICS)
async def orchestrate_topics(prompt: str) -> str:
    logger.info(f"Orchestrating topics for prompt: {prompt}")
    chat = llm.with_structured_output({
        "name": "research_orchestration",
        "description": "A number of prompts for other AI LLM workers that represent specific topics to research to form a full business plan.",
        "parameters": {
            "type": "object",
            "properties": {
                "topics": {
                    "type": "array",
                    "items": {
                        "type": "string",
                        "description": "A prompt for an AI LLM worker that will research the specific topic and compile information for."
                    }
                }
            },
            "required": ["topics"]
        }
    })

    response = chat.invoke([
        SystemMessage(
            content="You are an experienced and helpful AI orchesrator who will create a series of prompts for other AI LLM workers that represent specific topics to research to form a full business plan."),
        HumanMessage(content=prompt)
    ])

    return response


@worker(TaskDefNames.DELEGATE_WORKER)
async def delegate_worker(_: str) -> str:
    pass


@worker(TaskDefNames.SYNTHESIZE_REPORTS)
async def synthesize_reports(_: str) -> str:
    pass
