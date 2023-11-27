package lh.demo.it.request.wf;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.Comparator;
import io.littlehorse.sdk.common.proto.LHPublicApiGrpc.LHPublicApiBlockingStub;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.usertask.UserTaskSchema;
import io.littlehorse.sdk.wfsdk.LHFormatString;
import io.littlehorse.sdk.wfsdk.UserTaskOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.WorkflowThread;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import java.io.IOException;
import java.util.logging.Logger;

public class ITRequestWorkflow {

    Logger log = Logger.getLogger(ITRequestWorkflow.class.getName());

    private LHPublicApiBlockingStub client;

    private static final String WF_NAME = "it-request";
    public static final String EMAIL_TASK_NAME = "send-email";
    public static final String APPROVAL_FORM = "it-request-approval";

    public ITRequestWorkflow(LHConfig config) throws IOException {
        this.client = config.getBlockingStub();
    }

    public void registerWorkflowSpec() throws IOException {
        UserTaskSchema approvalForm = new UserTaskSchema(new ApprovalForm(), APPROVAL_FORM);

        log.info("Deploying User Task Schemas");
        client.putUserTaskDef(approvalForm.compile());

        log.info("Deploying workflow!");
        Workflow wf = new WorkflowImpl(WF_NAME, this::wfFunc);
        wf.registerWfSpec(client);
    }

    /*
     * This function defines the logic of our WfSpec.
     */
    public void wfFunc(WorkflowThread wf) {
        // First: We define the variables used by this WfSpec.
        // .required() makes a variable required as input.
        // .searchable() allows us to search that variable by its value.

        // We want to be able to search it-requests by the email of the person who requests it.
        // Also, we require that the email is provided as an input variable.
        WfRunVariable requesterEmail =
                wf.addVariable("requester-email", VariableType.STR).searchable().required();

        // The item is required as an input variable. However, we don't search by the item yet.
        WfRunVariable item =
                wf.addVariable("item-description", VariableType.STR).required();

        @SuppressWarnings("unused")
        WfRunVariable justification =
                wf.addVariable("justification", VariableType.STR).required();

        // The status is an enum of 'PENDING', 'APPROVED', and 'REJECTED', and it is not an
        // input variable, so it's not `.required()`. However, it is `.searchable()` because we want to
        // be able to search for it-requests by status.
        //
        // We set the default value to 'PENDING' when we start the workflow.
        WfRunVariable status = wf.addVariable("status", "PENDING").searchable();

        // Internal variable
        WfRunVariable isApproved = wf.addVariable("is-approved", VariableType.BOOL);

        // Send a User Task to the finance department and have them approve the request.
        // We assign it to the finance group, not to an individual user.
        String assignedUserId = null;
        String userGroup = "finance";
        UserTaskOutput financeUserTaskOutput = wf.assignUserTask(APPROVAL_FORM, assignedUserId, userGroup);

        // Save the value of the output into our variable.
        wf.mutate(isApproved, VariableMutationType.ASSIGN, financeUserTaskOutput.jsonPath("$.isApproved"));

        wf.doIfElse(
                wf.condition(isApproved, Comparator.EQUALS, true),
                // If the request is approved, then this lambda is executed
                ifBody -> {
                    // Create the email body using a LittleHorse Format String
                    LHFormatString emailContent =
                            ifBody.format("Dear {0}, your request for {1} has been approved!", requesterEmail, item);
                    // You can also pass in hard-coded
                    String emailSubject = "Your IT Request";

                    // Send the email!
                    ifBody.execute(ITRequestWorkflow.EMAIL_TASK_NAME, requesterEmail, emailSubject, emailContent);

                    // Save the status as approved
                    ifBody.mutate(status, VariableMutationType.ASSIGN, "APPROVED");
                },
                // If the request is denied, then this lambda is executed.
                elseBody -> {
                    // Create the email body
                    LHFormatString emailContent =
                            elseBody.format("Dear {0}, your request for {1} has been approved!", requesterEmail, item);

                    // Send the email!
                    elseBody.execute(
                            ITRequestWorkflow.EMAIL_TASK_NAME, requesterEmail, "Your IT Request", emailContent);

                    // Save the status as rejected
                    elseBody.mutate(status, VariableMutationType.ASSIGN, "REJECTED");
                });
    }
}
