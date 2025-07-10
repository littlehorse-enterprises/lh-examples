import asyncio

from littlehorse import create_workflow_spec
from littlehorse.config import LHConfig
from littlehorse.model import RunWfRequest, VariableValue
from littlehorse.workflow import Workflow, WorkflowThread
from utils.constants import TaskDefNames, VariableNames, WorkflowNames
from utils.logger import logger
from utils.worker_registry import WorkerRegistry
from workers import *

# The logic for our WfSpec (workflow) lives in this function!

def get_workflow() -> Workflow:

    def sales_email_personalization(wf: WorkflowThread) -> None:
        # Inputs
        customer_id = wf.declare_str(VariableNames.CUSTOMER_ID).required().searchable()
        instructions = wf.declare_str(VariableNames.INSTRUCTIONS).required()

        # Internal Variables
        feedback = wf.declare_str(VariableNames.FEEDBACK)
        customer_data = wf.execute(TaskDefNames.FETCH_CUSTOMER_CRM_DATA, customer_id, retries=3)
        
        previous_interactions = wf.declare_json_arr(VariableNames.PREVIOUS_INTERACTIONS, [])

        def do_while_body(dwt: WorkflowThread) -> None:
            email = dwt.execute(TaskDefNames.GENERATE_EMAIL, customer_data, instructions, previous_interactions, retries=3)
            ai_feedback = dwt.execute(TaskDefNames.APPROVE_EMAIL, email, retries=3)
            feedback.assign(ai_feedback)

            previous_interactions.assign(previous_interactions.add(wf.format("email: {0}, feedback: {1}", email, ai_feedback)))

        wf.do_while(feedback.is_not_equal_to(""), do_while_body)

    return Workflow(WorkflowNames.SALES_EMAIL_PERSONALIZATION, sales_email_personalization)


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

    logger.info("Running sample workflow")
    client.RunWf(RunWfRequest(
        wf_spec_name=WorkflowNames.SALES_EMAIL_PERSONALIZATION,
        variables={
            VariableNames.CUSTOMER_ID: VariableValue(str="834792"),
            VariableNames.INSTRUCTIONS: VariableValue(str="Write a personalized email introducing our new AI-powered workflow automation platform.")
        }
    ))

    print(get_workflow().compile())

    await WorkerRegistry.start_all(config)


if __name__ == '__main__':
    asyncio.run(main())
