import asyncio

import littlehorse
from littlehorse import create_task_def, create_workflow_spec
from littlehorse.config import LHConfig
from littlehorse.model import DeleteTaskDefRequest, LHErrorType, TaskDefId
from littlehorse.worker import LHTaskWorker
from littlehorse.workflow import Workflow, WorkflowThread
from utils.constants import TaskDefNames, WorkflowNames
from utils.worker_registry import WorkerRegistry
from workers import orchestrate_topics
from utils.logger import logger

# The logic for our WfSpec (workflow) lives in this function!
def get_workflow() -> Workflow:

    def startup_generator(wf: WorkflowThread) -> None:
        wf.execute(TaskDefNames.ORCHESTRATE_TOPICS, "")
        wf.execute(TaskDefNames.DELEGATE_WORKERS, "")
        wf.execute(TaskDefNames.SYNTHESIZE_REPORTS, "")

        # first_name = wf.declare_str("first-name").searchable().required()
        # last_name = wf.declare_str("last-name").searchable().required()
        # ssn = wf.declare_int("ssn").masked().required()
        # identity_verified = wf.declare_bool("identity-verified").searchable()
        # wf.execute("verify-identity", first_name, last_name, ssn, retries=3)
        # identity_verification_result = wf.wait_for_event("identity-verified", timeout=60 * 60 * 24 * 3)
        # def handle_error(handler: WorkflowThread) -> None:
        #     handler.execute("notify-customer-not-verified", first_name, last_name)
        #     handler.fail("customer-not-verified", "Unable to verify customer identity in time.")
        # wf.handle_error(identity_verification_result, handle_error, LHErrorType.TIMEOUT)
        # identity_verified.assign(identity_verification_result)
        # def if_body(body: WorkflowThread) -> None:
        #     body.execute("notify-customer-verified", first_name, last_name)
        # def else_body(body: WorkflowThread) -> None:
        #     body.execute("notify-customer-not-verified", first_name, last_name)
        # wf.do_if(identity_verified.is_equal_to(True), if_body, else_body)

    # Provide the name of the WfSpec and a function which has the logic.
    return Workflow(WorkflowNames.STARTUP_GENERATOR, startup_generator)

async def main() -> None:
    # Config
    config = LHConfig()
    stub = config.stub()

    # Delete existing TaskDefs
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
