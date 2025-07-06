import logging

from littlehorse import (create_external_event_def, create_task_def,
                         create_workflow_spec)
from littlehorse.config import LHConfig
from littlehorse.model import *
from littlehorse.workflow import Workflow, WorkflowThread

from quickstart.workers import (notify_customer_not_verified,
                               notify_customer_verified, verify_identity)

logging.basicConfig(level=logging.INFO)

# The logic for our WfSpec (worfklow) lives in this function!
def get_workflow() -> Workflow:

    def quickstart_workflow(wf: WorkflowThread) -> None:
        # Declare the input variables for the workflow.
        #
        # Using .searchable() allows us to search for WfRun's based on the value of
        # these variables, and .required() makes it required to pass the variable
        # as input.
        full_name = wf.declare_str("full-name").searchable().required()
        email = wf.declare_str("email").searchable().required()

        # Social Security Numbers are sensitive, so we mask the variable with `.masked()`.
        ssn = wf.declare_int("ssn").masked().required()

        identity_verified = wf.declare_bool("identity-verified").searchable()

        # Call the verify-identity task and retry it up to 3 times if it fails
        wf.execute("verify-identity", full_name, email, ssn, retries=3)

        # Make the WfRun wait until the event is posted or if the timeout is reached
        identity_verification_result = wf.wait_for_event(
            "identity-verified",
            timeout=60 * 60 * 24 * 3,
            # Using a correlation id allows you to post the callback/ `ExternalEvent`
            # without knowing the WfRun ID that is waiting for it. In this case,
            # we correlate based on the email address of the customer.
            correlation_id=email,
        )

        def handle_error(handler: WorkflowThread) -> None:
            handler.execute("notify-customer-not-verified", full_name, email)
            handler.fail("customer-not-verified", "Unable to verify customer identity in time.")

        wf.handle_error(identity_verification_result, handle_error, LHErrorType.TIMEOUT)

        # Assign the output of the ExternalEvent to the `identityVerified` variable.
        identity_verified.assign(identity_verification_result)

        def if_body(body: WorkflowThread) -> None:
            body.execute("notify-customer-verified", full_name, email)

        def else_body(body: WorkflowThread) -> None:
            body.execute("notify-customer-not-verified", full_name, email)

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

    config.stub().PutExternalEventDef(PutExternalEventDefRequest(
        correlated_event_config=CorrelatedEventConfig(),
        content_type=ReturnType(return_type=TypeDefinition(type=VariableType.BOOL)),
        name="identity-verified",
    ))

    create_task_def(verify_identity, "verify-identity", config)
    create_task_def(notify_customer_verified, "notify-customer-verified", config)
    create_task_def(notify_customer_not_verified, "notify-customer-not-verified", config)

    create_workflow_spec(wf, config)

if __name__ == "__main__":
    main()