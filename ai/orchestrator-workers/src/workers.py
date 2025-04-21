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
            content="You are an expert research coordinator who specializes in breaking down complex business challenges into specific, actionable research topics. Your role is to generate focused prompts that will guide other AI workers in conducting thorough research. Each prompt should be clear, specific, and designed to yield valuable insights that can be synthesized into a comprehensive business plan."),
        HumanMessage(content=prompt)
    ])

    return response


@worker(TaskDefNames.DELEGATE_WORKER)
async def delegate_worker(prompt: str) -> str:
    response = llm.invoke([
        SystemMessage(
            content="You are an expert research analyst who specializes in conducting thorough investigations on specific business topics. Your role is to analyze the given research prompt, gather relevant information, and provide comprehensive insights. Focus on delivering factual, well-structured information that contributes to the overall business plan. Keep your responses clear, concise, and directly address the research topic at hand. Be consice and limit your response to 3 points."),
        HumanMessage(content=prompt)
    ])
    return response.content


@worker(TaskDefNames.SYNTHESIZE_REPORTS)
async def synthesize_reports(_: str) -> str:
    pass
