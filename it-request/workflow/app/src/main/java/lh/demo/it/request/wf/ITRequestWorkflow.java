package lh.demo.it.request.wf;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.Comparator;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.usertask.UserTaskSchema;
import io.littlehorse.sdk.wfsdk.LHFormatString;
import io.littlehorse.sdk.wfsdk.UserTaskOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.WorkflowThread;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ITRequestWorkflow {

    private LittleHorseBlockingStub client;

    private static final Logger log = LoggerFactory.getLogger(App.class);
    private static final String WF_NAME = "it-request";
    public static final String EMAIL_TASK_NAME = "send-email";
    public static final String APPROVAL_FORM = "it-request-approval";

    public ITRequestWorkflow(LHConfig config) throws IOException {
        this.client = config.getBlockingStub();
    }

    public void registerWorkflowSpec() {
        UserTaskSchema approvalForm = new UserTaskSchema(new ApprovalForm(), APPROVAL_FORM);

        log.info("Deploying User Task Schemas");
        client.putUserTaskDef(approvalForm.compile());

        log.info("Deploying workflow!");
        Workflow wf = new WorkflowImpl(WF_NAME, this::wfFunc);
        wf.registerWfSpec(client);
    }

    /*
     * This function defines the logic of our WfSpec.
     *
     * First: We define the variables used by this WfSpec.
     *  - .required() makes a variable required as input.
     *  - .searchable() allows us to search that variable by its value.
     *
     *  The status is an enum of 'PENDING', 'APPROVED', and 'REJECTED', and it is not an
     *  input variable, so it's not `.required()`. However, it is `.searchable()` because we want to
     *  be able to search for it-requests by status.
     *
     * After defining our variables, the first action we take in the workflow is assigning a
     * User Task. In this case, we do not assign it to a specific userId; rather, we assign it to
     * the 'finance' userGroup.
     *
     * We save the `UserTaskOutput.jsonPath("$.isApproved") into an internal variable.
     *
     * Depending on whether the request was approved, we either:
     * - schedule the `send-email` task to notify the requester that the request was accepted, OR
     * - schedule the `send-email` task to notify the requester that the request was rejected.
     *
     * In both cases, the input parameters to the `.execute()` method determine the content
     * of the email. See `common-tasks/java-send-email/` for info on how the Task Worker works.
     */
    public void wfFunc(WorkflowThread wf) {

        WfRunVariable requesterEmail =
                wf.addVariable("requester-email", VariableType.STR).searchable().required();
        WfRunVariable item =
                wf.addVariable("item-description", VariableType.STR).required();

        // Set status as PENDING when we start workflow.
        WfRunVariable status = wf.addVariable("status", "PENDING").searchable();
        WfRunVariable isApproved = wf.addVariable("is-approved", VariableType.BOOL);

        // Send a User Task to the finance userGroup and have them approve the request.
        String assignedUserId = null;
        String userGroup = "finance";
        UserTaskOutput financeUserTaskOutput = wf.assignUserTask(APPROVAL_FORM, assignedUserId, userGroup);

        // Save the value of the output into our variable.
        wf.mutate(isApproved, VariableMutationType.ASSIGN, financeUserTaskOutput.jsonPath("$.isApproved"));
        wf.mutate(status, VariableMutationType.ASSIGN, "PROCESSING");

        wf.doIfElse(
                wf.condition(isApproved, Comparator.EQUALS, true),
                ifBody -> {
                    LHFormatString emailContent =
                            ifBody.format("Dear {0}, your request for {1} has been approved!", requesterEmail, item);
                    String emailSubject = "Your IT Request";

                    ifBody.execute(ITRequestWorkflow.EMAIL_TASK_NAME, requesterEmail, emailSubject, emailContent);
                    ifBody.mutate(status, VariableMutationType.ASSIGN, "APPROVED");
                },
                // If the request is denied, then this lambda is executed.
                elseBody -> {
                    LHFormatString emailContent =
                            elseBody.format("Dear {0}, your request for {1} has been approved!", requesterEmail, item);
                    elseBody.execute(
                            ITRequestWorkflow.EMAIL_TASK_NAME, requesterEmail, "Your IT Request", emailContent);
                    elseBody.mutate(status, VariableMutationType.ASSIGN, "REJECTED");
                });
    }
}
