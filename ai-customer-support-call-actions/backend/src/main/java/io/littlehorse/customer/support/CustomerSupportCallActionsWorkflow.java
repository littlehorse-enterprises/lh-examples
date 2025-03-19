package io.littlehorse.customer.support;

import io.littlehorse.sdk.common.proto.Comparator;
import io.littlehorse.sdk.wfsdk.NodeOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.WorkflowThread;

public class CustomerSupportCallActionsWorkflow {
    /**
     * This method defines the document processing workflow logic
     */
    public void customerSupportCallActionsWf(WorkflowThread wf) {
        // Declare input variables
        WfRunVariable callId = wf.declareStr("call-id").searchable().required();
        WfRunVariable customerId = wf.declareStr("customer-id").searchable().required();

        // Step 1: Transcribe the call
        NodeOutput transcript = wf.execute(LHConstants.TRANSCRIBE_CALL_TASK).timeout(60 * 5);
        
        // Step 2: Determine if there are any actions to take
        NodeOutput actionsToTake = wf.execute(LHConstants.DETERMINE_ACTIONS_TASK, transcript, customerId, callId).timeout(60 * 5);

        // Step 3: Either run workflows or create user task
        wf.doIf(wf.condition(actionsToTake, Comparator.NOT_EQUALS, null),
                ifHandler -> {
                    // Run the identified workflows
                    ifHandler.execute(LHConstants.RUN_WORKFLOWS_TASK, actionsToTake);
                });

        // Require a human to verify that no actions are needed to be taken

    }

    /**
     * Returns a LittleHorse Workflow wrapper object that can be used to register
     * the WfSpec.
     */
    public Workflow getWorkflow() {
        return Workflow.newWorkflow(LHConstants.WORKFLOW_NAME, this::customerSupportCallActionsWf);
    }
}
