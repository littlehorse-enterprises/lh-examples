import asyncio
import logging

import littlehorse
from littlehorse import create_task_def, create_workflow_spec
from littlehorse.config import LHConfig
from littlehorse.model import DeleteTaskDefRequest, LHErrorType, TaskDefId
from littlehorse.worker import LHTaskWorker
from littlehorse.workflow import Workflow, WorkflowThread
from workers import ai_router

logging.basicConfig(level=logging.INFO)

# The logic for our WfSpec (workflow) lives in this function!
def get_workflow() -> Workflow:

    def customer_support_routing_workflow(wf: WorkflowThread) -> None:
        # Inputs
        customer_message = wf.declare_str("customer-message").required()

        route = wf.execute("ai-router", customer_message)
        
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

        # wf.do_if(
        #     identity_verified.is_equal_to(True),
        #     if_body,
        #     else_body
        # )

    # Provide the name of the WfSpec and a function which has the logic.
    return Workflow("customer-support-routing", customer_support_routing_workflow)

AI_ROUTER_TASK = "ai-router"

async def main() -> None:
    logging.info("Registering TaskDefs and a WfSpec")
    config = LHConfig()
    stub = config.stub()
    wf = get_workflow()
    
    stub.DeleteTaskDef(DeleteTaskDefRequest(id=TaskDefId(name=AI_ROUTER_TASK)))
    create_task_def(ai_router, AI_ROUTER_TASK, config)

    create_workflow_spec(wf, config)

    logging.info("Starting Task Worker!")

    await littlehorse.start(LHTaskWorker(ai_router, AI_ROUTER_TASK, config))

if __name__ == '__main__':
    asyncio.run(main())
