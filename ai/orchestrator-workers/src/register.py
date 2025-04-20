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
        initial_prompt = wf.declare_str(
            VariableNames.INITIAL_PROMPT).required()
        worker_prompts = wf.declare_json_obj(VariableNames.WORKER_PROMPTS)

        orchestrated_worker_prompts = wf.execute(
            TaskDefNames.ORCHESTRATE_TOPICS, initial_prompt)

        logger.info(
            f"Orchestrated worker prompts: {orchestrated_worker_prompts}")
        logger.info(
            f"Type of orchestrated worker prompts: {type(orchestrated_worker_prompts)}")

        worker_prompts.assign(orchestrated_worker_prompts)

        # Create a dictionary from the topics array

        def delegate_workers(thread: WorkflowThread) -> None:
            print(thread)
            # thread.execute(TaskDefNames.DELEGATE_WORKER, "")

        worker_threads = wf.spawn_thread_for_each(worker_prompts.with_json_path(
            "$.topics"), delegate_workers, ThreadNames.DELEGATE_WORKERS)
        wf.wait_for_threads(worker_threads)

        wf.execute(TaskDefNames.SYNTHESIZE_REPORTS, "")

    # Provide the name of the WfSpec and a function which has the logic.
    return Workflow(WorkflowNames.STARTUP_GENERATOR, startup_generator)


async def main() -> None:
    # Config
    config = LHConfig()

    # stub = config.stub()
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
