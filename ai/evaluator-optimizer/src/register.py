import asyncio

import littlehorse
from workers import *
from littlehorse import create_task_def, create_workflow_spec
from littlehorse.config import LHConfig
from littlehorse.model import DeleteTaskDefRequest, LHErrorType, TaskDefId, VariableType
from littlehorse.worker import LHTaskWorker
from littlehorse.workflow import Workflow, WorkflowThread
from utils.constants import TaskDefNames, ThreadNames, VariableNames, WorkflowNames
from utils.worker_registry import WorkerRegistry
from utils.logger import logger

# The logic for our WfSpec (workflow) lives in this function!


def get_workflow() -> Workflow:

    def sales_email_personalization(wf: WorkflowThread) -> None:
        customer_id = wf.declare_str(VariableNames.CUSTOMER_ID).required()
        approved_email = wf.declare_bool(
            VariableNames.APPROVED_EMAIL, False)

        crm_data = wf.execute(
            TaskDefNames.FETCH_CUSTOMER_CRM_DATA, customer_id)

        def do_while_body(dwt: WorkflowThread) -> None:
            email = dwt.execute(TaskDefNames.GENERATE_EMAIL, crm_data)
            approved = dwt.execute(TaskDefNames.APPROVE_EMAIL, email)
            approved_email.assign(approved)

        wf.do_while(approved_email.is_equal_to(False), do_while_body)

    return Workflow(WorkflowNames.SALES_EMAIL_PERSONALIZATION, sales_email_personalization)


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
