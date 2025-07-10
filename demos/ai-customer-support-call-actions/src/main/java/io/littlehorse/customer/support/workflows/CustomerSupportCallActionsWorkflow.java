package io.littlehorse.customer.support.workflows;

import io.littlehorse.customer.support.Constants;
import io.littlehorse.sdk.common.proto.Comparator;
import io.littlehorse.sdk.wfsdk.NodeOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.WorkflowThread;

public class CustomerSupportCallActionsWorkflow {
    /**
     * This method defines the document processing workflow logic
     */
    public void workflowSpec(WorkflowThread wf) {
        // Declare input variables
        WfRunVariable callId = wf.declareStr("call-id").searchable().required();
        WfRunVariable customerEmail = wf.declareStr("customer-email").searchable().required();

        // Step 1: Transcribe the call
        NodeOutput transcript = wf.execute(Constants.TRANSCRIBE_CALL_TASK, callId, customerEmail).withRetries(5)
                .timeout(60 * 5);

        // Step 2: Determine if there are any actions to take
        NodeOutput actionsToTake = wf.execute(Constants.DETERMINE_ACTIONS_TASK, transcript).withRetries(5)
                .timeout(60 * 5);

        // Step 3: Either run workflows or create user task
        wf.doIf(wf.condition(actionsToTake, Comparator.NOT_EQUALS, null), ifHandler -> {
            // Run the identified workflows
            ifHandler.execute(Constants.RUN_WORKFLOWS_TASK, actionsToTake).withRetries(5);
        });

        // Require a human to verify that no actions are needed to be taken
    }

    /**
     * Returns a LittleHorse Workflow wrapper object that can be used to register
     * the WfSpec.
     */
    public Workflow getWorkflow() {
        return Workflow.newWorkflow(Constants.WORKFLOW_NAME, this::workflowSpec);
    }
}
