import logging

from littlehorse import (create_external_event_def, create_task_def,
                         create_workflow_spec)
from littlehorse.config import LHConfig
from littlehorse.model import LHErrorType
from littlehorse.workflow import Workflow, WorkflowThread

from quickstart.workers import (notify_customer_not_verified,
                               notify_customer_verified, verify_identity)

logging.basicConfig(level=logging.INFO)

# The logic for our WfSpec (worfklow) lives in this function!
def get_workflow() -> Workflow:

    def quickstart_workflow(wf: WorkflowThread) -> None:
        first_name = wf.declare_str("first-name").searchable().required()
        last_name = wf.declare_str("last-name").searchable().required()
        ssn = wf.declare_int("ssn").masked().required()

        identity_verified = wf.declare_bool("identity-verified").searchable()

        wf.execute("verify-identity", first_name, last_name, ssn, retries=3)

        identity_verification_result = wf.wait_for_event("identity-verified", timeout=60 * 60 * 24 * 3)

        def handle_error(handler: WorkflowThread) -> None:
            handler.execute("notify-customer-not-verified", first_name, last_name)
            handler.fail("customer-not-verified", "Unable to verify customer identity in time.")

        wf.handle_error(identity_verification_result, handle_error, LHErrorType.TIMEOUT)

        identity_verified.assign(identity_verification_result)

        def if_body(body: WorkflowThread) -> None:
            body.execute("notify-customer-verified", first_name, last_name)

        def else_body(body: WorkflowThread) -> None:
            body.execute("notify-customer-not-verified", first_name, last_name)

        wf.do_if(
            identity_verified.is_equal_to(True),
            if_body,
            else_body
        )

    # Provide the name of the WfSpec and a function which has the logic.
    return Workflow("quickstart", quickstart_workflow)

def main() -> None:
    logging.info("Registering TaskDefs, a WfSpec and a ExternalEventDef")
    config = LHConfig()
    wf = get_workflow()

    create_external_event_def("identity-verified", config)
    
    create_task_def(verify_identity, "verify-identity", config)
    create_task_def(notify_customer_verified, "notify-customer-verified", config)
    create_task_def(notify_customer_not_verified, "notify-customer-not-verified", config)

    create_workflow_spec(wf, config)

if __name__ == "__main__":
    main()