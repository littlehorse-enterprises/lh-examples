package io.littlehorse.quickstart;

import io.littlehorse.sdk.common.proto.LHErrorType;
import io.littlehorse.sdk.wfsdk.NodeOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.WorkflowThread;

public class QuickstartWorkflow {
    final String WORKFLOW_NAME = "quickstart";
    final String IDENTITY_VERIFIED_EVENT = "identity-verified";
    final String VERIFY_IDENTITY_TASK = "verify-identity";
    final String NOTIFY_CUSTOMER_VERIFIED_TASK = "notify-customer-verified";
    final String NOTIFY_CUSTOMER_NOT_VERIFIED_TASK = "notify-customer-not-verified";

    /*
     * This method defines the logic of our workflow
     */
    public void quickstartWf(WorkflowThread wf) {
        // Declare the input variables for the workflow.
        //
        // Using .searchable() allows us to search for WfRun's based on the value of
        // these variables, and .required() makes it required to pass the variable
        // as input.
        WfRunVariable fullName = wf.declareStr("full-name").searchable().required();
        WfRunVariable email = wf.declareStr("email").searchable().required();

        // Social Security Numbers are sensitive, so we mask the variable with `.masked()`.
        WfRunVariable ssn = wf.declareInt("ssn").masked().required();

        // Internal variable representing whether the customer's identity has been verified.
        WfRunVariable identityVerified = wf.declareBool("identity-verified").searchable();

        // Call the verify-identity task and retry it up to 3 times if it fails
        wf.execute(VERIFY_IDENTITY_TASK, fullName, email, ssn).withRetries(3);

        // Make the WfRun wait until the event is posted or if the timeout is reached
        NodeOutput identityVerificationResult = wf.waitForEvent(IDENTITY_VERIFIED_EVENT)
                .timeout(60 * 5) // 5 minute timeout
                // Using a correlation id allows you to post the callback/ `ExternalEvent`
                // without knowing the WfRun ID that is waiting for it. In this case,
                // we correlate based on the email address of the customer.
                .withCorrelationId(email)
                // This line tells the `Workflow` object to automatically register the
                // `ExternalEventDef` for us.
                .registeredAs(Boolean.class);

        wf.handleError(identityVerificationResult, LHErrorType.TIMEOUT, handler -> {
            handler.execute(NOTIFY_CUSTOMER_NOT_VERIFIED_TASK, fullName, email);
            handler.fail("customer-not-verified", "Unable to verify customer identity in time.");
        });

        // Assign the output of the ExternalEvent to the `identityVerified` variable.
        identityVerified.assign(identityVerificationResult);

        // Notify the customer if their identity was verified or not
        wf.doIf(identityVerified.isEqualTo(true), ifBody -> {
                    ifBody.execute(NOTIFY_CUSTOMER_VERIFIED_TASK, fullName, email);
                    // If you want, you could add more tasks or workflow steps in here
                    // using the `ifBody` just like we use `wf` above.
                    //
                    // Also note that you could pass in a function pointer rather than
                    // a lambda if you want to better organize the code.
                })
                .doElse(elseBody -> {
                    elseBody.execute(NOTIFY_CUSTOMER_NOT_VERIFIED_TASK, fullName, email);
                });
    }

    /*
     * This method returns a LittleHorse `Workflow` wrapper object that can be
     * used to register the WfSpec to the LH Server.
     */
    public Workflow getWorkflow() {
        return Workflow.newWorkflow(WORKFLOW_NAME, this::quickstartWf);
    }
}
