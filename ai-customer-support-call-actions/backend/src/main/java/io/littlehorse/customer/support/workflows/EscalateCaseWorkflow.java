package io.littlehorse.customer.support.workflows;

import io.littlehorse.customer.support.LHConstants;
import io.littlehorse.sdk.common.proto.Comparator;
import io.littlehorse.sdk.wfsdk.NodeOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.WorkflowThread;

public class EscalateCaseWorkflow {

    public void workflowSpec(WorkflowThread wf) {
        // Declare input variables
        WfRunVariable caseId = wf.declareStr("case-id").searchable().required();
        WfRunVariable customerEmail =
                wf.declareStr("customer-email").searchable().required();
        WfRunVariable reason = wf.declareStr("reason").required();
        WfRunVariable department = wf.declareStr("department").searchable().required();
        WfRunVariable priority = wf.declareStr("priority").searchable().withDefault("medium");
        WfRunVariable agentNotes = wf.declareStr("agent-notes");

        // Declare internal variables
        WfRunVariable escalationResult = wf.declareStr("escalation-result");

        // Step 1: Process the escalation
        // Execute the task to escalate the case with retries
        NodeOutput escalationTaskResult = wf.execute(
                        LHConstants.ESCALATE_CASE_TASK, caseId, customerEmail, reason, department, priority, agentNotes)
                .withRetries(5)
                .timeout(60);

        escalationResult.assign(escalationTaskResult);

        // Step 2: Notify customer about the escalation
        wf.doIf(wf.condition(escalationTaskResult, Comparator.NOT_EQUALS, null), notifyHandler -> {
            notifyHandler
                    .execute(
                            LHConstants.SEND_EMAIL_TASK,
                            customerEmail,
                            "Your case has been escalated",
                            "Your case " + caseId + " has been escalated to the " + department + " department with "
                                    + priority + " priority.")
                    .withRetries(5);
        });

        // Step 3: Log the escalation for audit purposes
        wf.execute(LHConstants.AUDIT_LOG_TASK, caseId, "Case escalated to " + department + " department")
                .withRetries(5);
    }

    public Workflow getWorkflow() {
        return Workflow.newWorkflow(LHConstants.ESCALATE_CASE_WORKFLOW, this::workflowSpec);
    }
}
