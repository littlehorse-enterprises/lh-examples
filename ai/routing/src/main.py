import asyncio

from littlehorse import create_workflow_spec
from littlehorse.config import LHConfig
from littlehorse.model import (PutUserTaskDefRequest, RunWfRequest,
                               UserTaskField, VariableMutationType,
                               VariableType, VariableValue)
from littlehorse.workflow import Workflow, WorkflowThread
from utils.constants import (TaskDefNames, UserTaskDefNames, VariableNames,
                             WorkflowNames)
from utils.logger import logger
from utils.worker_registry import WorkerRegistry
from workers import *


# The logic for our WfSpec (workflow) lives in this function!
def get_workflow() -> Workflow:

    def wfSpec(wf: WorkflowThread) -> None:
        # Inputs to the workflow
        customer_id = wf.declare_str(VariableNames.CUSTOMER_ID).required().searchable()
        customer_message = wf.declare_str(VariableNames.CUSTOMER_MESSAGE).required()
        
        customer_data = wf.declare_json_obj(VariableNames.CUSTOMER_DATA)
        customer_data.assign(wf.execute(TaskDefNames.FETCH_CUSTOMER_DATA, customer_id, retries=3))
        
        route = wf.declare_str(VariableNames.ROUTE).searchable()
        route.assign(wf.execute(TaskDefNames.AI_ROUTER, customer_message, retries=3))
        
        email_request = wf.declare_json_obj(VariableNames.EMAIL_REQUEST)
        def handle_general_question(wf: WorkflowThread) -> None:
            email_request.assign(wf.execute(TaskDefNames.GENERAL_QUESTION, customer_message, customer_data, retries=3))

        def handle_technical_support(wf: WorkflowThread) -> None:
            email_request.assign(wf.execute(TaskDefNames.TECHNICAL_SUPPORT, customer_message, customer_data, retries=3))

        def handle_other(wf: WorkflowThread) -> None:
            email_request.assign(wf.assign_user_task(UserTaskDefNames.OTHER, user_group="customer-support-team").with_notes(wf.format("Customer Data:\n{0}\n\nCustomer Message:\n{1}", customer_data, customer_message)))

        wf.do_if(route.is_equal_to(TaskDefNames.GENERAL_QUESTION), handle_general_question)
        wf.do_if(route.is_equal_to(TaskDefNames.TECHNICAL_SUPPORT), handle_technical_support)
        wf.do_if(route.is_equal_to(UserTaskDefNames.OTHER), handle_other)

        email_request.with_json_path("$.to").assign(customer_data.with_json_path("$.email"))

        wf.execute(TaskDefNames.SEND_EMAIL, email_request, retries=3)

    # Provide the name of the WfSpec and a function which has the logic.
    return Workflow(WorkflowNames.CUSTOMER_SERVICE_ROUTING, wfSpec)


put_user_task_def_req = PutUserTaskDefRequest(
        name=UserTaskDefNames.OTHER,
        fields=[
            UserTaskField(
                name="subject",
                description="The subject of the email",
                display_name="Subject",
                required=True,
                type=VariableType.STR
            ),
            UserTaskField(
                name="body",
                description="The body of the email",
                display_name="Body",
                required=True,
                type=VariableType.STR
            )
        ]
    )

async def main() -> None:
    config = LHConfig()
    client = config.stub()

    # Run this incase you need to delete all the TaskDefs
    # WorkerRegistry.delete_all(client)

    logger.info("Registering LittleHorse metadata.")
    WorkerRegistry.register_all(config)
    client.PutUserTaskDef(put_user_task_def_req)
    create_workflow_spec(get_workflow(), config)

    # Wait for the workflow to be registered
    await asyncio.sleep(1)

    client.RunWf(RunWfRequest(
        wf_spec_name=WorkflowNames.CUSTOMER_SERVICE_ROUTING,
        variables={
            VariableNames.CUSTOMER_MESSAGE: VariableValue(str="How can I cancel my subscription?"),
            VariableNames.CUSTOMER_ID: VariableValue(str="123456")
        }
    ))

    await WorkerRegistry.start_all(config)

if __name__ == '__main__':
    asyncio.run(main())
