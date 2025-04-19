import asyncio

import littlehorse
from workers import *
from littlehorse import create_task_def, create_workflow_spec
from littlehorse.config import LHConfig
from littlehorse.model import DeleteTaskDefRequest, LHErrorType, TaskDefId
from littlehorse.worker import LHTaskWorker
from littlehorse.workflow import Workflow, WorkflowThread
from utils.constants import TaskDefNames, ThreadNames, VariableNames, WorkflowNames
from utils.worker_registry import WorkerRegistry
from utils.logger import logger

# The logic for our WfSpec (workflow) lives in this function!
def get_workflow() -> Workflow:

    def startup_generator(wf: WorkflowThread) -> None:
        worker_prompts = wf.declare_json_arr(VariableNames.WORKER_PROMPTS, ["array", "of", "prompts"])

        wf.execute(TaskDefNames.ORCHESTRATE_TOPICS, "")

        def delegate_workers(thread: WorkflowThread) -> None:
            thread.execute(TaskDefNames.DELEGATE_WORKER, "")

        worker_threads = wf.spawn_thread_for_each(worker_prompts, delegate_workers, ThreadNames.DELEGATE_WORKERS)
        wf.wait_for_threads(worker_threads)

        wf.execute(TaskDefNames.SYNTHESIZE_REPORTS, "")

    # Provide the name of the WfSpec and a function which has the logic.
    return Workflow(WorkflowNames.STARTUP_GENERATOR, startup_generator)

async def main() -> None:
    # Config
    config = LHConfig()
    stub = config.stub()

    # WorkerRegistry.delete_all(stub)

    # Register
    logger.info("Registering TaskDefs and a WfSpec")
    WorkerRegistry.register_all(config)
    create_workflow_spec(get_workflow(), config)

    # Start Task Workers
    logger.info("Starting Task Worker")
    await WorkerRegistry.start_all(config)


if __name__ == '__main__':
    asyncio.run(main())
