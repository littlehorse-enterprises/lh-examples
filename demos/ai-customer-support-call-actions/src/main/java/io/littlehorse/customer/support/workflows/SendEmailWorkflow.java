package io.littlehorse.customer.support.workflows;

import io.littlehorse.customer.support.Constants;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.WorkflowThread;

public class SendEmailWorkflow {

    public void workflowSpec(WorkflowThread wf) {
        // Declare input variables
        WfRunVariable email = wf.declareStr("email").required();
        WfRunVariable subject = wf.declareStr("subject").required();
        WfRunVariable content = wf.declareStr("content").required();

        // Execute the task to send the follow-up email
        wf.execute(Constants.SEND_EMAIL_TASK, email, subject, content).withRetries(5);
    }

    public Workflow getWorkflow() {
        return Workflow.newWorkflow(Constants.SEND_EMAIL_WORKFLOW, this::workflowSpec);
    }
}
