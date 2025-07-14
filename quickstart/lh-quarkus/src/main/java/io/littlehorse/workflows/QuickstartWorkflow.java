package io.littlehorse.workflows;

import io.littlehorse.quarkus.workflow.LHWorkflow;
import io.littlehorse.quarkus.workflow.LHWorkflowDefinition;
import io.littlehorse.sdk.common.proto.LHErrorType;
import io.littlehorse.sdk.wfsdk.NodeOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.WorkflowThread;

@LHWorkflow(QuickstartWorkflow.QUICKSTART_WORKFLOW)
public class QuickstartWorkflow implements LHWorkflowDefinition {
    public static final String QUICKSTART_WORKFLOW = "quickstart";

    public static final String IDENTITY_VERIFIED_EVENT = "identity-verified";
    public static final String VERIFY_IDENTITY_TASK = "verify-identity";
    public static final String NOTIFY_CUSTOMER_VERIFIED_TASK = "notify-customer-verified";
    public static final String NOTIFY_CUSTOMER_NOT_VERIFIED_TASK = "notify-customer-not-verified";

    public static final String APPROVAL_STATUS = "approval-status";

    public static final String FULL_NAME = "full-name";
    public static final String EMAIL = "email";
    public static final String SSN = "ssn";
    public static final String IS_IDENTITY_VERIFIED = "is-identity-verified";

    @Override
    public void define(WorkflowThread wf) {
        // Declare the input variables for the workflow.
        //
        // Using .searchable() allows us to search for WfRun's based on the value of
        // these variables, and .required() makes it required to pass the variable
        // as input.
        WfRunVariable fullName = wf.declareStr(FULL_NAME).searchable().required();
        WfRunVariable email = wf.declareStr(EMAIL).searchable().required();

        // Social Security Numbers are sensitive, so we mask the variable with
        // `.masked()`.
        WfRunVariable ssn = wf.declareInt(SSN).masked().required();

        // Internal variable representing whether the customer's identity has been
        // verified.
        WfRunVariable identityVerified = wf.declareBool(IS_IDENTITY_VERIFIED);
        WfRunVariable approvalStatus = wf.declareStr(APPROVAL_STATUS).withDefault("PENDING");

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
            approvalStatus.assign("REJECTED");
            handler.execute(NOTIFY_CUSTOMER_NOT_VERIFIED_TASK, fullName, email);
            handler.fail("customer-not-verified", "Unable to verify customer identity in time.");
        });

        // Assign the output of the ExternalEvent to the `identityVerified` variable.
        identityVerified.assign(identityVerificationResult);

        // Notify the customer if their identity was verified or not
        wf.doIf(identityVerified.isEqualTo(true), ifBody -> {
                    approvalStatus.assign("APPROVED");
                    ifBody.execute(NOTIFY_CUSTOMER_VERIFIED_TASK, fullName, email);
                    // If you want, you could add more tasks or workflow steps in here
                    // using the `ifBody` just like we use `wf` above.
                    //
                    // Also note that you could pass in a function pointer rather than
                    // a lambda if you want to better organize the code.
                })
                .doElse(elseBody -> {
                    approvalStatus.assign("REJECTED");
                    elseBody.execute(NOTIFY_CUSTOMER_NOT_VERIFIED_TASK, fullName, email);
                });
    }
}
