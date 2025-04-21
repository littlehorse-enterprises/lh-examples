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
        # Inputs
        customer_id = wf.declare_str(VariableNames.CUSTOMER_ID).required()
        instructions = wf.declare_str(VariableNames.INSTRUCTIONS).required()

        # Internal Variables
        feedback = wf.declare_str(VariableNames.FEEDBACK)
        customer_data = wf.execute(TaskDefNames.FETCH_CUSTOMER_CRM_DATA, customer_id)
        previous_email = wf.declare_str(VariableNames.PREVIOUS_EMAIL)

        def do_while_body(dwt: WorkflowThread) -> None:
            email = dwt.execute(TaskDefNames.GENERATE_EMAIL, customer_data, instructions, feedback, previous_email)
            previous_email.assign(email)
            
            ai_evaluator_feedback = dwt.execute(TaskDefNames.APPROVE_EMAIL, email)
            feedback.assign(ai_evaluator_feedback)

        wf.do_while(feedback.is_not_equal_to(""), do_while_body)

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
