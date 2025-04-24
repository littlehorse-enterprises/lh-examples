import asyncio

from littlehorse import create_workflow_spec
from littlehorse.config import LHConfig
from littlehorse.model import RunWfRequest, VariableType, VariableValue
from littlehorse.workflow import Workflow, WorkflowThread
from utils.constants import (TaskDefNames, ThreadNames, VariableNames,
                             WorkflowNames)
from utils.logger import logger
from utils.worker_registry import WorkerRegistry
from workers import *


# The logic for our WfSpec (workflow) lives in this function!
def get_workflow() -> Workflow:

    def startup_generator(wf: WorkflowThread) -> None:
        initial_prompt = wf.declare_str(
            VariableNames.INITIAL_PROMPT).required()
        worker_prompts = wf.declare_json_arr(VariableNames.WORKER_PROMPTS)

        orchestrated_worker_prompts = wf.execute(
            TaskDefNames.ORCHESTRATE_TOPICS, initial_prompt)

        worker_prompts.assign(
            orchestrated_worker_prompts.with_json_path("$.topics"))

        # Declare entrypoint variable
        worker_content = wf.declare_json_arr(
            VariableNames.WORKER_CONTENT, ["here2"])

        def delegate_workers(thread: WorkflowThread) -> None:
            worker_prompt = thread.add_variable("INPUT", VariableType.STR)
            content = thread.execute(
                TaskDefNames.DELEGATE_WORKER, worker_prompt)

            # Append to entrypoint variable
            worker_content.add("item")

        worker_threads = wf.spawn_thread_for_each(
            worker_prompts, delegate_workers, ThreadNames.DELEGATE_WORKERS)

        wf.wait_for_threads(worker_threads)

        wf.execute(TaskDefNames.SYNTHESIZE_REPORTS, "")

    # Provide the name of the WfSpec and a function which has the logic.
    return Workflow(WorkflowNames.STARTUP_GENERATOR, startup_generator)


async def main() -> None:
    config = LHConfig()
    client = config.stub()

    # Run this incase you need to delete all the TaskDefs
    # WorkerRegistry.delete_all(client) 

    logger.info("Registering LittleHorse metadata.")
    WorkerRegistry.register_all(config)
    create_workflow_spec(get_workflow(), config)

    # Wait for the workflow to be registered
    await asyncio.sleep(1)

    client.RunWf(RunWfRequest(
        wf_spec_name=WorkflowNames.STARTUP_GENERATOR,
        variables={
            VariableNames.INITIAL_PROMPT: VariableValue(str="I want to start a boba matcha business in Las Vegas."),
        }
    ))

    await WorkerRegistry.start_all(config)


if __name__ == '__main__':
    asyncio.run(main())
