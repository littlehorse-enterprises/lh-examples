from utils.model import model
from utils.worker_registry import worker
from utils.constants import TaskDefNames

@worker(TaskDefNames.ORCHESTRATE_TOPICS)
async def orchestrate_topics(prompt: str) -> str:
    return prompt

@worker(TaskDefNames.DELEGATE_WORKERS)
async def delegate_workers(_: str) -> str:
  pass

@worker(TaskDefNames.SYNTHESIZE_REPORTS)
async def synthesize_reports(_: str) -> str:
  pass