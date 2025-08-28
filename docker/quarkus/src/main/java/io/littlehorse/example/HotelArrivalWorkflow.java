package io.littlehorse.example;

import io.littlehorse.quarkus.workflow.LHWorkflow;
import io.littlehorse.quarkus.workflow.LHWorkflowDefinition;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.WorkflowThread;

@LHWorkflow(HotelArrivalWorkflow.WORKFLOW_NAME)
public class HotelArrivalWorkflow implements LHWorkflowDefinition {
    public static final String WORKFLOW_NAME = "customer-arrival";

    /*
     * This method defines the logic of our workflow
     */
    @Override
    public void define(WorkflowThread wf) {
        WfRunVariable customer = wf.declareStr("customer-id").searchable().required();
        WfRunVariable isPlatinum = wf.declareBool("is-platinum").searchable();

        // Fetch customer profile and determine if platinum
        isPlatinum.assign(wf.execute("fetch-customer", customer));

        wf.doIf(isPlatinum.isEqualTo(true), handler -> {
            handler.execute("send-special-welcome", customer);
        });

        wf.execute("process-arrival", customer);
    }
}
