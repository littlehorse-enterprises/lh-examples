import asyncio
import logging

import littlehorse
from littlehorse import create_task_def, create_workflow_spec
from littlehorse.config import LHConfig
from littlehorse.model import PutUserTaskDefRequest, UserTaskField, VariableType
from littlehorse.worker import LHTaskWorker
from littlehorse.workflow import Workflow, WorkflowThread
from workers import ai_router, fetch_customer_data, general_question, technical_support

logging.basicConfig(level=logging.INFO)

# The logic for our WfSpec (workflow) lives in this function!
def get_workflow() -> Workflow:

    def wfSpec(wf: WorkflowThread) -> None:
        # Inputs
        customer_message = wf.declare_str("customer-message").required()
        customer_id = wf.declare_str("customer-id").required().searchable()
        
        # Internal Variables
        route = wf.declare_str("route").searchable()
        customer_data = wf.declare_json_obj("customer-data")

        customer_data.assign(wf.execute("fetch-customer-data", customer_id, retries=3))

        route.assign(wf.execute("ai-router", customer_message, retries=3, timeout_seconds=60))

        def handle_general_question(wf: WorkflowThread) -> None:
            wf.execute("general-question", customer_message, customer_data, retries=3, timeout_seconds=60)

        def handle_technical_support(wf: WorkflowThread) -> None:
            wf.execute("technical-support", customer_message, customer_data, retries=3, timeout_seconds=60)

        def handle_other(wf: WorkflowThread) -> None:
            wf.assign_user_task("other", user_group="customer-support-team").with_notes(wf.format("Customer Data:\n{0}\n\nCustomer Message:\n{1}", customer_data, customer_message))

        wf.do_if(route.is_equal_to("general-question"), handle_general_question)
        wf.do_if(route.is_equal_to("technical-support"), handle_technical_support)
        wf.do_if(route.is_equal_to("other"), handle_other)

    # Provide the name of the WfSpec and a function which has the logic.
    return Workflow("customer-support-routing", wfSpec)

AI_ROUTER_TASK = "ai-router"
GENERAL_QUESTION_TASK = "general-question"
FETCH_CUSTOMER_DATA_TASK = "fetch-customer-data"
TECHNICAL_SUPPORT_TASK = "technical-support"

put_user_task_def_req = PutUserTaskDefRequest(
        name="other",
        fields=[
            UserTaskField(
                name="isResolved",
                description="Is the request resolved?",
                display_name="Resolved?",
                required=True,
                type=VariableType.BOOL
            ),
        ]
    )

async def main() -> None:
    logging.info("Registering TaskDefs and a WfSpec")
    config = LHConfig()
    client = config.stub()
    wf = get_workflow()

    # Create the metadata
    client.PutUserTaskDef(put_user_task_def_req)

    create_task_def(ai_router, AI_ROUTER_TASK, config)
    create_task_def(general_question, GENERAL_QUESTION_TASK, config)
    create_task_def(fetch_customer_data, FETCH_CUSTOMER_DATA_TASK, config)
    create_task_def(technical_support, TECHNICAL_SUPPORT_TASK, config)

    create_workflow_spec(wf, config)

    logging.info("Starting Task Worker!")

    await littlehorse.start(
        LHTaskWorker(fetch_customer_data, FETCH_CUSTOMER_DATA_TASK, config),
        LHTaskWorker(ai_router, AI_ROUTER_TASK, config),
        LHTaskWorker(general_question, GENERAL_QUESTION_TASK, config),
        LHTaskWorker(technical_support, TECHNICAL_SUPPORT_TASK, config)
    )

if __name__ == '__main__':
    asyncio.run(main())
