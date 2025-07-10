package io.littlehorse.customer.support.workflows;

import io.littlehorse.customer.support.Constants;
import io.littlehorse.sdk.common.proto.Comparator;
import io.littlehorse.sdk.wfsdk.NodeOutput;
import io.littlehorse.sdk.wfsdk.TaskNodeOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.WorkflowThread;

public class AddNoteToCustomerWorkflow {

    public void workflowSpec(WorkflowThread wf) {
        // Declare input variables
        WfRunVariable customerEmail =
                wf.declareStr("customer-email").searchable().required();
        WfRunVariable note = wf.declareStr("note").required();
        WfRunVariable agentId = wf.declareStr("agent-id").searchable().withDefault("ai-agent");

        // Step 1: Analyze the note content using LLM
        NodeOutput analysis = wf.execute(Constants.ANALYZE_CUSTOMER_NOTE_TASK, note)
                .withRetries(5)
                .timeout(30);

        // Step 2: Handle sensitive information if present
        wf.doIfElse(
                wf.condition(analysis.jsonPath("$.containsSensitiveInfo"), Comparator.EQUALS, true),
                ifHandler -> {
                    // Redact sensitive information
                    TaskNodeOutput redactedNote = ifHandler
                            .execute(Constants.REDACT_SENSITIVE_INFO_TASK, analysis.jsonPath("$.text"))
                            .withRetries(5);

                    // Add the redacted note to customer record
                    ifHandler
                            .execute(
                                    Constants.ADD_NOTE_TO_CUSTOMER_TASK,
                                    customerEmail,
                                    redactedNote,
                                    agentId)
                            .withRetries(5);
                },
                elseHandler -> {
                    // Add the original note to customer record
                    elseHandler
                            .execute(
                                    Constants.ADD_NOTE_TO_CUSTOMER_TASK,
                                    customerEmail,
                                    analysis.jsonPath("$.text"),
                                    agentId)
                            .withRetries(5);
                });

        // Step 3: Schedule follow-up if required
        wf.doIf(wf.condition(analysis.jsonPath("$.followUpTimestamp"), Comparator.NOT_EQUALS, 0), ifHandler -> {
            // Create follow-up task
            ifHandler.execute(
                    Constants.SCHEDULE_FOLLOW_UP_TASK,
                    customerEmail,
                    analysis.jsonPath("$.followUpTimestamp"),
                    "Follow up regarding: " + analysis.jsonPath("$.text")).withRetries(5);

            // Send notification email
            ifHandler.execute(
                    Constants.SEND_EMAIL_TASK,
                    customerEmail,
                    "Follow-up scheduled",
                    "We've scheduled a follow-up on " + analysis.jsonPath("$.followUpTimestamp")).withRetries(5);
        });
    }

    public Workflow getWorkflow() {
        return Workflow.newWorkflow(Constants.ADD_NOTE_TO_CUSTOMER_WORKFLOW, this::workflowSpec);
    }
}
